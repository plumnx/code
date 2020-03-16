package com.zhongwang.cloud.platform.service.code.rule.service;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.zhongwang.cloud.platform.service.code.common.service.RedisService;
import com.zhongwang.cloud.platform.service.code.config.bean.CodeSerialPolicy;
import com.zhongwang.cloud.platform.service.code.exception.CodePersistentException;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleSerial;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleRepository;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleSerialRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.List;

import static com.zhongwang.cloud.platform.service.code.common.CodeConst.CodeSerialStrategy.DB;
import static com.zhongwang.cloud.platform.service.code.common.util.Caches.CodeSerial.*;
import static com.zhongwang.cloud.platform.service.code.exception.LambdaExceptionChecked.throwingConsumerWrapper;
import static com.zhongwang.cloud.platform.service.code.rule.entity.MakeEntityForTest.buildCodeQuery;
import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@SpringBootTest(webEnvironment = MOCK, properties = {
        "spring.cloud.config.enabled:false",
        "spring.config.name:unit"
})
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
@EnableConfigurationProperties({CodeSerialPolicy.class})
public class GenerateCacheSerialServiceWithDbUnitTest {

    @Autowired
    private CodeRuleRepository codeRuleRepository;

    @Autowired
    private GenerateCacheSerialService generateCacheSerialService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CodeSerialPolicy codeSerialPolicy;

    @Value("${spring.redis.prefix:${spring.application.name}}:")
    private String prefix;

    @Test
    public void test_fetch_serial_no_from_cache_if_not_exists() {
//        if (DB.name().equals(codeSerialPolicy.getStrategy())) {
//            return;
//        }

        List<Integer> integers = generateCacheSerialService.
                fetchSerialNoFromCache(HASH_NAME, CURRENT_HASH_KEY, LIMIT_HASH_KEY, 1);
        assertNull(integers);
    }

    @Test
    public void test_fetch_serial_no_from_cache_if_exists_and_not_limit() {
//        if (DB.name().equals(codeSerialPolicy.getStrategy())) {
//            return;
//        }

        redisTemplate.opsForHash().put(HASH_NAME, prefix + CURRENT_HASH_KEY, 1);
        redisTemplate.opsForHash().put(HASH_NAME, prefix + LIMIT_HASH_KEY, 1000);

        List<Integer> integers = generateCacheSerialService.
                fetchSerialNoFromCache(HASH_NAME, CURRENT_HASH_KEY, LIMIT_HASH_KEY, 1);
        assertNotNull(integers);

        assertEquals(1, integers.get(0).intValue());
        assertEquals(1, integers.get(1).intValue());
        assertEquals(1000, integers.get(2).intValue());
        assertEquals(2, (int) redisTemplate.opsForHash().get(HASH_NAME, prefix + CURRENT_HASH_KEY));
    }

    @Test
    public void test_fetch_serial_no_from_cache_if_exists_and_violate_limit() {
//        if (DB.name().equals(codeSerialPolicy.getStrategy())) {
//            return;
//        }

        redisTemplate.opsForHash().put(HASH_NAME, prefix + CURRENT_HASH_KEY, 1000);
        redisTemplate.opsForHash().put(HASH_NAME, prefix + LIMIT_HASH_KEY, 1000);

        List<Integer> integers = generateCacheSerialService.
                fetchSerialNoFromCache(HASH_NAME, CURRENT_HASH_KEY, LIMIT_HASH_KEY, 1);

        assertNotNull(integers);
        assertEquals(0, integers.get(0).intValue());
        assertEquals(1000, integers.get(1).intValue());
        assertEquals(1000, integers.get(2).intValue());
        assertEquals(1000, redisTemplate.opsForHash().get(HASH_NAME, prefix + CURRENT_HASH_KEY));
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_007.xml"})
    public void test_init_serial_max_range_exists() {
        codeRuleRepository.findAll().forEach(throwingConsumerWrapper(codeRuleSerial -> {
            generateCacheSerialService.extensiveSerialMaxRange(
                    buildCodeQuery().withPkid(codeRuleSerial.getPkid()), SERIAL_UNION_VALUE_FORMAT, SERIAL_UNION_VALUE, SHOW_UNION_VALUE_FORMAT, SHOW_UNION_VALUE,
                    1, 1);

            String key = key(SERIAL_UNION_VALUE, SHOW_UNION_VALUE);
            assertEquals(1, redisTemplate.opsForHash().get(forCurrentHash(codeRuleSerial.getPkid()), prefix + forCurrentHashKey(key)));
        }));
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_007.xml"})
    public void test_extensive_serial_max_range_800() throws Exception {
        generateCacheSerialService.extensiveSerialMaxRange(
                buildCodeQuery().withPkid(RULE_PKID), SERIAL_UNION_VALUE_FORMAT, SERIAL_UNION_VALUE, SHOW_UNION_VALUE_FORMAT, SHOW_UNION_VALUE,
                798, 1);

        String key = key(SERIAL_UNION_VALUE, SHOW_UNION_VALUE);
        assertEquals(1000, redisTemplate.opsForHash().get(forCurrentHash(RULE_PKID), prefix + forLimitHashKey(key)));
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_007.xml"})
    public void test_extensive_serial_max_range_899() throws Exception {
        generateCacheSerialService.extensiveSerialMaxRange(
                buildCodeQuery().withPkid(RULE_PKID), SERIAL_UNION_VALUE_FORMAT, SERIAL_UNION_VALUE, SHOW_UNION_VALUE_FORMAT, SHOW_UNION_VALUE,
                900, 900);

        String key = key(SERIAL_UNION_VALUE, SHOW_UNION_VALUE);
        assertTrue(1001 < (int) redisTemplate.opsForHash().get(forCurrentHash(RULE_PKID), prefix + forLimitHashKey(key)));
    }

    private static final String RULE_PKID = "c3a1104097a9450aaf957c5865b7282g";
    private static final String SERIAL_PKID = "8a80cb816270b79b016270b7ba6d000g";
    private static final String SERIAL_UNION_VALUE = "Const-Fiel-<SERIAL_CODE>";
    private static final String SHOW_UNION_VALUE = null;
    private static final String SERIAL_UNION_VALUE_FORMAT = "CONST_VARIABLE,FIELD_VARIABLE,SERIAL_VARIABLE";
    private static final String SHOW_UNION_VALUE_FORMAT = null;
    private static final String HASH_NAME = "HASH_NAME";
    private static final String CURRENT_HASH_KEY = "CURRENT_HASH_KEY";
    private static final String LIMIT_HASH_KEY = "LIMIT_HASH_KEY";

    @After
    public void clearRedisCache() {
        redisService.flushDb();
    }

}
