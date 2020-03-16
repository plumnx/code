package com.zhongwang.cloud.platform.service.code.rule.service;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zhongwang.cloud.platform.service.code.common.service.RedisService;
import com.zhongwang.cloud.platform.service.code.config.bean.CodeSerialPolicy;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleSerial;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeMultiplyQuery;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeQuery;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeResult;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleDetailRepository;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleRepository;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleSerialRepository;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zhongwang.cloud.platform.service.code.common.CodeConst.CodeSerialStrategy.CACHE;
import static com.zhongwang.cloud.platform.service.code.common.CodeConst.RULE_TAG.EMPTY_SHOW_UNION_VALUE;
import static com.zhongwang.cloud.platform.service.code.common.CodeConst.SYS_COMMON_STATUS.NOT_DELETE;
import static com.zhongwang.cloud.platform.service.code.common.util.Caches.CodeSerial.*;
import static com.zhongwang.cloud.platform.service.code.rule.entity.MakeEntityForTest.buildPkid;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

/**
 * 缓存测试：
 * 需要设置unit.yml中的strategy设置为CACHE
 */
@SpringBootTest(webEnvironment = MOCK, properties = {
        "spring.cloud.config.enabled:false",
        "spring.config.name:unit"
})
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
@EnableConfigurationProperties({CodeSerialPolicy.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CodeRuleGenerateServiceInCacheTest {

    @Inject
    private CodeRuleGenerateService codeRuleGenerateService;

    @Inject
    private CodeRuleService codeRuleService;

    @Inject
    private CodeRuleRepository codeRuleRepository;

    @Inject
    private CodeRuleSerialService codeRuleSerialService;

    @Inject
    private CodeRuleDetailRepository codeRuleDetailRepository;

    @Inject
    private CodeRuleSerialRepository codeRuleSerialRepository;

    @Inject
    private RedisService redisService;

    @Inject
    private CodeSerialPolicy codeSerialPolicy;

    @Inject
    private RedisTemplate redisTemplate;

    @Value("${spring.redis.prefix:${spring.application.name}}:")
    private String prefix;

    private static ExecutorService executorService;

    @BeforeClass
    public static void init() {
        executorService = Executors.newFixedThreadPool(100);
    }

    @AfterClass
    public static void close() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, NANOSECONDS);
    }

    @Before
    @After
    public void clearRedisCache() {
        redisService.flushDb();
    }

    /*
     *  本单元测试考量以下内容：
     *
     *  1.并发环境
     *      1.1 单key锁定并发环境
     * 	    1.2 多key锁定并发环境
     *  2.数据库：
     * 	    2.1 存在记录
     * 	    2.2 不存在记录
     *  3.缓存
     * 	    3.1 存在记录
     * 	    3.2 不存在记录
     *  4.当前流水号：
     * 	    4.1 未超过流水边界
     * 	    4.2 超过流水边界，未超过流水上限
     * 	    4.3 超过流水上限
     *  5.提取流水个数：
     * 	    5.1 提取流水个数 + 当前流水号 < 流水边界
     * 	    5.2 流水边界 < 提取流水个数 + 当前流水号 < 流水上限
     * 	    5.3 提取流水个数 + 当前流水号 > 流水上限
     */

    /**
     * 1.并发环境（1.1）
     * 2.数据库(2.2)
     * 3.缓存（3.2）
     * 4.当前流水号（4.1）
     * 5.提取流水个数（5.1）
     * 6.多次执行
     */
    @Test
    @DatabaseSetup(value = {"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_A() throws Exception {
        assertEquals(codeSerialPolicy.getStrategy(), CACHE.name());
        final String EXAMPLE_PKID = "c3a1104097a9450aaf957c5865b72820";

        // validate result
        CodeResult codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(EXAMPLE_PKID));
        assertEquals("Const-sys-field-0001", codeResult.getCode());

        // validate db
        CodeRuleSerial codeRuleSerial = codeRuleSerialService.
                findCodeRuleSerialByParams(EXAMPLE_PKID, serialUnionValue, showUnionValue);
        assertNotNull(codeRuleSerial);
        assertEquals(1000, codeRuleSerial.getCodeMaxValue().intValue());

        // validate redis
        String key = key(serialUnionValue, showUnionValue);
        Map<String, String> entries = redisTemplate.opsForHash().entries(forCurrentHash(EXAMPLE_PKID));
        assertNotNull(entries);
        assertEquals(1, entries.get(prefix + forCurrentHashKey(key)));
        assertEquals(1000, entries.get(prefix + forLimitHashKey(key)));

        // validate result 2
        codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(EXAMPLE_PKID));
        assertEquals("Const-sys-field-0002", codeResult.getCode());

        // validate db2
        codeRuleSerial = codeRuleSerialService.
                findCodeRuleSerialByParams(EXAMPLE_PKID, serialUnionValue, showUnionValue);
        assertNotNull(codeRuleSerial);
        assertEquals(1000, codeRuleSerial.getCodeMaxValue().intValue());

        // validate redis2
        key = key(serialUnionValue, showUnionValue);
        entries = redisTemplate.opsForHash().entries(forCurrentHash(EXAMPLE_PKID));
        assertNotNull(entries);
        assertEquals(2, entries.get(prefix + forCurrentHashKey(key)));
        assertEquals(1000, entries.get(prefix + forLimitHashKey(key)));

        // validate result 3
        codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(EXAMPLE_PKID));
        assertEquals("Const-sys-field-0003", codeResult.getCode());

        // validate db3
        codeRuleSerial = codeRuleSerialService.
                findCodeRuleSerialByParams(EXAMPLE_PKID, serialUnionValue, showUnionValue);
        assertNotNull(codeRuleSerial);
        assertEquals(1000, codeRuleSerial.getCodeMaxValue().intValue());

        // validate redis3
        key = key(serialUnionValue, showUnionValue);
        entries = redisTemplate.opsForHash().entries(forCurrentHash(EXAMPLE_PKID));
        assertNotNull(entries);
        assertEquals(3, entries.get(prefix + forCurrentHashKey(key)));
        assertEquals(1000, entries.get(prefix + forLimitHashKey(key)));
    }

    /**
     * 1.并发环境（1.1）
     * 2.数据库(2.2)
     * 3.缓存（3.2）
     * 4.当前流水号（4.1）
     * 5.提取流水个数（5.1）
     */
    @Test
    @DatabaseSetup(value = {"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_B() throws Exception {
        assertEquals(codeSerialPolicy.getStrategy(), CACHE.name());
        final String EXAMPLE_PKID = "c3a1104097a9450aaf957c5865b72821";

        List<Future<CodeResult>> futures = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    return codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(EXAMPLE_PKID));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        List<String> codes = Lists.newArrayList();
        for (Future<CodeResult> future : futures) {
            codes.add(future.get().getCode());
        }

        // validate codes
        Map<String, Long> codesMap = codes.stream().collect(Collectors.groupingBy(
                Function.identity(), Collectors.counting()
        ));
        long codesMapCount = codesMap.values().stream().filter(count -> count == 1).count();
        assertEquals("Const-sys-field-0001", codes.stream().min(String::compareTo).get());
        assertEquals("Const-sys-field-0100", codes.stream().max(String::compareTo).get());
        assertEquals(100, codes.size());
        assertEquals(codes.size(), codesMapCount);

        // validate db
        CodeRuleSerial codeRuleSerial = codeRuleSerialService.
                findCodeRuleSerialByParams(EXAMPLE_PKID, serialUnionValue, showUnionValue);
        assertNotNull(codeRuleSerial);
        assertEquals(1000, codeRuleSerial.getCodeMaxValue().intValue());

        // validate redis
        String key = key(serialUnionValue, showUnionValue);
        Map<String, String> entries = redisTemplate.opsForHash().entries(forCurrentHash(EXAMPLE_PKID));
        assertNotNull(entries);
        assertEquals(100, entries.get(prefix + forCurrentHashKey(key)));
        assertEquals(1000, entries.get(prefix + forLimitHashKey(key)));
    }

    //    /**
//     * 1.并发环境（1.1）
//     * 2.数据库(2.2)
//     * 3.缓存（3.2）
//     * 4.当前流水号（4.1, 4.2, 4.3）
//     * 5.提取流水个数（5.1, 5.2, 5.3）
//     */
//    @Test
//    @DatabaseSetup(value = {"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_C() throws Exception {
        assertEquals(codeSerialPolicy.getStrategy(), CACHE.name());
        final String EXAMPLE_PKID = "c3a1104097a9450aaf957c5865b72822";

        List<Future<List<CodeResult>>> futures = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            futures.add(testCall(EXAMPLE_PKID, 20));
        }
        List<String> codes = Lists.newArrayList();
        for (Future<List<CodeResult>> future : futures) {
            codes.addAll(future.get().stream().map(CodeResult::getCode).collect(Collectors.toList()));
        }

        // validate codes
        Map<String, Long> codesMap = codes.stream().collect(Collectors.groupingBy(
                Function.identity(), Collectors.counting()
        ));
        long codesMapCount = codesMap.values().stream().filter(count -> count == 1).count();
        assertEquals("Const-sys-field-0001", codes.stream().min(String::compareTo).get());
        assertEquals("Const-sys-field-2000", codes.stream().max(String::compareTo).get());
        assertEquals(2000, codes.size());
        assertEquals(codes.size(), codesMapCount);

        // validate db
        CodeRuleSerial codeRuleSerial = codeRuleSerialService.
                findCodeRuleSerialByParams(EXAMPLE_PKID, serialUnionValue, showUnionValue);
        assertNotNull(codeRuleSerial);
        assertEquals(3000, codeRuleSerial.getCodeMaxValue().intValue());

        // validate redis
        String key = key(serialUnionValue, showUnionValue);
        Map<String, String> entries = redisTemplate.opsForHash().entries(forCurrentHash(EXAMPLE_PKID));
        assertNotNull(entries);
        assertEquals(2000, entries.get(prefix + forCurrentHashKey(key)));
        assertEquals(3000, entries.get(prefix + forLimitHashKey(key)));
    }


    /**
     * 1.并发环境（1.1）
     * 2.数据库(2.1)
     * 3.缓存（3.2）
     * 4.当前流水号（4.1, 4.2, 4.3）
     * 5.提取流水个数（5.1, 5.2, 5.3）
     */
    @Test
    @DatabaseSetup(value = {"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_D() throws Exception {
        assertEquals(codeSerialPolicy.getStrategy(), CACHE.name());
        final String EXAMPLE_PKID = "c3a1104097a9450aaf957c5865b72823";
        List<Future<List<CodeResult>>> futures = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            futures.add(testCall(EXAMPLE_PKID, 3));
        }
        List<String> codes = Lists.newArrayList();
        for (Future<List<CodeResult>> future : futures) {
            codes.addAll(future.get().stream().map(CodeResult::getCode).collect(Collectors.toList()));
        }
        // validate codes
        Map<String, Long> codesMap = codes.stream().collect(Collectors.groupingBy(
                Function.identity(), Collectors.counting()
        ));
        long codesMapCount = codesMap.values().stream().filter(count -> count == 1).count();
        assertEquals("Const-sys-field-0801", codes.stream().min(String::compareTo).get());
        assertEquals("Const-sys-field-1100", codes.stream().max(String::compareTo).get());
        assertEquals(300, codes.size());
        assertEquals(codes.size(), codesMapCount);

        // validate db
        CodeRuleSerial codeRuleSerial = codeRuleSerialService.
                findCodeRuleSerialByParams(EXAMPLE_PKID, serialUnionValue, showUnionValue);
        assertNotNull(codeRuleSerial);
        assertEquals(2000, codeRuleSerial.getCodeMaxValue().intValue());

        // validate redis
        String key = key(serialUnionValue, showUnionValue);
        Map<String, String> entries = redisTemplate.opsForHash().entries(forCurrentHash(EXAMPLE_PKID));
        assertNotNull(entries);
        assertEquals(1100, entries.get(prefix + forCurrentHashKey(key)));
        assertEquals(2000, entries.get(prefix + forLimitHashKey(key)));
    }


    /**
     * 1.并发环境（1.1）
     * 2.数据库(2.1)
     * 3.缓存（3.2）
     * 4.当前流水号（4.1, 4.2, 4.3）
     * 5.提取流水个数（5.1, 5.2, 5.3）
     */
    @Test
    @DatabaseSetup(value = {"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_E() throws Exception {
        assertEquals(codeSerialPolicy.getStrategy(), CACHE.name());
        final String EXAMPLE_PKID = "c3a1104097a9450aaf957c5865b72824";

        List<Future<List<CodeResult>>> futures = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            futures.add(testCall(EXAMPLE_PKID, 31));
        }
        List<String> codes = Lists.newArrayList();
        for (Future<List<CodeResult>> future : futures) {
            codes.addAll(future.get().stream().map(CodeResult::getCode).collect(Collectors.toList()));
        }

        // validate codes
        Map<String, Long> codesMap = codes.stream().collect(Collectors.groupingBy(
                Function.identity(), Collectors.counting()
        ));
        long codesMapCount = codesMap.values().stream().filter(count -> count == 1).count();
        assertEquals("Const-sys-field-0901", codes.stream().min(String::compareTo).get());
        assertEquals("Const-sys-field-1210", codes.stream().max(String::compareTo).get());
        assertEquals(310, codes.size());
        assertEquals(codes.size(), codesMapCount);

        // validate db
        CodeRuleSerial codeRuleSerial = codeRuleSerialService.
                findCodeRuleSerialByParams(EXAMPLE_PKID, serialUnionValue, showUnionValue);
        assertNotNull(codeRuleSerial);
        assertEquals(2000, codeRuleSerial.getCodeMaxValue().intValue());

        // validate redis
        String key = key(serialUnionValue, showUnionValue);
        Map<String, String> entries = redisTemplate.opsForHash().entries(forCurrentHash(EXAMPLE_PKID));
        assertNotNull(entries);
        assertEquals(1210, entries.get(prefix + forCurrentHashKey(key)));
        assertEquals(2000, entries.get(prefix + forLimitHashKey(key)));
    }

    /**
     * 1.并发环境（1.1）
     * 2.数据库(2.1)
     * 3.缓存（3.2）
     * 4.当前流水号（4.1, 4.2, 4.3）
     * 5.提取流水个数（5.1, 5.2, 5.3）
     */
    @Test
    @DatabaseSetup(value = {"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_F() throws Exception {
        assertEquals(codeSerialPolicy.getStrategy(), CACHE.name());
        final String EXAMPLE_PKID = "c3a1104097a9450aaf957c5865b72825";

        List<Future<List<CodeResult>>> futures = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            futures.add(testCall(EXAMPLE_PKID, 32));
        }
        List<String> codes = Lists.newArrayList();
        for (Future<List<CodeResult>> future : futures) {
            codes.addAll(future.get().stream().map(CodeResult::getCode).collect(Collectors.toList()));
        }

        // validate codes
        Map<String, Long> codesMap = codes.stream().collect(Collectors.groupingBy(
                Function.identity(), Collectors.counting()
        ));
        long codesMapCount = codesMap.values().stream().filter(count -> count == 1).count();
        assertEquals("Const-sys-field-1001", codes.stream().min(String::compareTo).get());
        assertEquals("Const-sys-field-1320", codes.stream().max(String::compareTo).get());
        assertEquals(320, codes.size());
        assertEquals(codes.size(), codesMapCount);

        // validate db
        CodeRuleSerial codeRuleSerial = codeRuleSerialService.
                findCodeRuleSerialByParams(EXAMPLE_PKID, serialUnionValue, showUnionValue);
        assertNotNull(codeRuleSerial);
        assertEquals(2000, codeRuleSerial.getCodeMaxValue().intValue());

        // validate redis
        String key = key(serialUnionValue, showUnionValue);
        Map<String, String> entries = redisTemplate.opsForHash().entries(forCurrentHash(EXAMPLE_PKID));
        assertNotNull(entries);
        assertEquals(1320, entries.get(prefix + forCurrentHashKey(key)));
        assertEquals(2000, entries.get(prefix + forLimitHashKey(key)));
    }

    /**
     * 1.并发环境（1.2）
     * 2.数据库(2.1)
     * 3.缓存（3.2）
     * 4.当前流水号（4.1, 4.2, 4.3）
     * 5.提取流水个数（5.1, 5.2, 5.3）
     */
    @Test
    @DatabaseSetup(value = {"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_G() throws Exception {
        assertEquals(codeSerialPolicy.getStrategy(), CACHE.name());
        final String EXAMPLE_PKID_1 = "c3a1104097a9450aaf957c5865b72826";
        final String EXAMPLE_PKID_2 = "c3a1104097a9450aaf957c5865b72827";

        List<Future<List<CodeResult>>> futures1 = Lists.newArrayList();
        List<Future<List<CodeResult>>> futures2 = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            futures1.add(testCall(EXAMPLE_PKID_1, 10));
            futures2.add(testCall(EXAMPLE_PKID_2, 10));
        }
        List<String> codes_1 = Lists.newArrayList();
        for (Future<List<CodeResult>> future : futures1) {
            codes_1.addAll(future.get().stream().map(CodeResult::getCode).collect(Collectors.toList()));
        }
        List<String> codes_2 = Lists.newArrayList();
        for (Future<List<CodeResult>> future : futures2) {
            codes_2.addAll(future.get().stream().map(CodeResult::getCode).collect(Collectors.toList()));
        }

        // validate codes
        Map<String, Long> codesMap = codes_1.stream().collect(Collectors.groupingBy(
                Function.identity(), Collectors.counting()
        ));
        long codesMapCount = codesMap.values().stream().filter(count -> count == 1).count();
        assertEquals("Const-sys-field-0001", codes_1.stream().min(String::compareTo).get());
        assertEquals("Const-sys-field-1000", codes_1.stream().max(String::compareTo).get());
        assertEquals(1000, codes_1.size());
        assertEquals(codes_1.size(), codesMapCount);

        codesMap = codes_2.stream().collect(Collectors.groupingBy(
                Function.identity(), Collectors.counting()
        ));
        codesMapCount = codesMap.values().stream().filter(count -> count == 1).count();
        assertEquals("Const-sys-field-0001", codes_2.stream().min(String::compareTo).get());
        assertEquals("Const-sys-field-1000", codes_2.stream().max(String::compareTo).get());
        assertEquals(1000, codes_2.size());
        assertEquals(codes_2.size(), codesMapCount);

        // validate db
        CodeRuleSerial codeRuleSerial = codeRuleSerialService.
                findCodeRuleSerialByParams(EXAMPLE_PKID_1, serialUnionValue, showUnionValue);
        assertNotNull(codeRuleSerial);
        assertEquals(2000, codeRuleSerial.getCodeMaxValue().intValue());

        // validate redis
        String key = key(serialUnionValue, showUnionValue);
        Map<String, String> entries = redisTemplate.opsForHash().entries(forCurrentHash(EXAMPLE_PKID_1));
        assertNotNull(entries);
        assertEquals(1000, entries.get(prefix + forCurrentHashKey(key)));
        assertEquals(2000, entries.get(prefix + forLimitHashKey(key)));

        // validate db
        codeRuleSerial = codeRuleSerialService.
                findCodeRuleSerialByParams(EXAMPLE_PKID_2, serialUnionValue, showUnionValue);
        assertNotNull(codeRuleSerial);
        assertEquals(2000, codeRuleSerial.getCodeMaxValue().intValue());

        // validate redis
        entries = redisTemplate.opsForHash().entries(forCurrentHash(EXAMPLE_PKID_2));
        assertNotNull(entries);
        assertEquals(1000, entries.get(prefix + forCurrentHashKey(key)));
        assertEquals(2000, entries.get(prefix + forLimitHashKey(key)));
    }

    private Future<List<CodeResult>> testCall(String EXAMPLE_PKID, int size) {
        return executorService.submit(() -> {
            try {
                return codeRuleGenerateService.generateCode(buildMultuplyCodeQuery().withPkid(EXAMPLE_PKID).withSize(size));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static final String showUnionValue = EMPTY_SHOW_UNION_VALUE;
    private static final String serialUnionValue = "Const-sys-field-<SERIAL_CODE>";

    private static CodeQuery buildCodeQuery() {
        CodeQuery codeQuery = new CodeQuery();
        codeQuery.setCompPkid("ZhongWang");
        codeQuery.setSystemCode("MES");
        codeQuery.setModuleCode("code");
        codeQuery.setOperator("040011");

        Map<String, String> parameters = Maps.newHashMap();
        parameters.put("SysVariable", "sys");
        parameters.put("Field", "field");
        codeQuery.setParameters(parameters);

        return codeQuery;
    }

    private static CodeMultiplyQuery buildMultuplyCodeQuery() {
        CodeMultiplyQuery codeMultiplyQuery = new CodeMultiplyQuery();
        codeMultiplyQuery.setCompPkid("ZhongWang");
        codeMultiplyQuery.setSystemCode("MES");
        codeMultiplyQuery.setModuleCode("code");
        codeMultiplyQuery.setOperator("040011");

        Map<String, String> parameters = Maps.newHashMap();
        parameters.put("SysVariable", "sys");
        parameters.put("Field", "field");
        codeMultiplyQuery.setParameters(parameters);

        return codeMultiplyQuery;
    }

    private static CodeRuleSerial buildCodeRuleSerial() {
        CodeRuleSerial codeRuleSerial = new CodeRuleSerial();
        codeRuleSerial.setPkid(buildPkid());
        codeRuleSerial.setShowUnionValue(showUnionValue);
        codeRuleSerial.setFlagDelete(NOT_DELETE);
        return codeRuleSerial;
    }

}
