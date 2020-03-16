package com.zhongwang.cloud.platform.service.code.rule.service;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.zhongwang.cloud.platform.bamboo.common.exception.NotFoundException;
import com.zhongwang.cloud.platform.service.code.common.service.RedisService;
import com.zhongwang.cloud.platform.service.code.config.bean.CodeSerialPolicy;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleDetailRepository;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.inject.Inject;
import java.text.ParseException;

import static com.zhongwang.cloud.platform.service.code.common.CodeConst.CodeSerialStrategy.DB;
import static com.zhongwang.cloud.platform.service.code.rule.entity.MakeEntityForTest.buildCodeRule;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

//@SpringBootTest(webEnvironment = MOCK, properties = {
//        "spring.cloud.config.enabled:false",
//        "spring.config.name:unit"
//})
//@RunWith(SpringJUnit4ClassRunner.class)
//@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class })
//@EnableConfigurationProperties({CodeSerialPolicy.class})
public class CodeRuleServiceCacheWithDbUnitTest {

//    @Inject
    private CodeRuleService codeRuleService;

//    @Inject
    private CodeRuleRepository codeRuleRepository;

//    @Inject
    private CodeRuleDetailRepository codeRuleDetailRepository;

//    @Inject
    private RedisService redisService;

//    @Inject
    private CodeSerialPolicy codeSerialPolicy;

//    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_find_code_rule_cache_with_real_data() {
        if(DB.name().equals(codeSerialPolicy.getStrategy())) {
            return;
        }

        redisService.flushDb();
        assertNull(redisService.getKey(CACHE_KEY));

        codeRuleService.findCodeRule(EXAMPLE_PKID);
        assertNotNull(redisService.getKey(CACHE_KEY));
    }

//    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_find_validate_code_rule_cache_with_real_data() {
        if(DB.name().equals(codeSerialPolicy.getStrategy())) {
            return;
        }

        redisService.flushDb();
        assertNull(redisService.getKey(CACHE_KEY));

        codeRuleService.findValidateCodeRule(EXAMPLE_PKID);
        assertNotNull(redisService.getKey(CACHE_KEY));
    }

//    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_update_code_rule_cache_with_real_data() throws ParseException, NotFoundException {
        if(DB.name().equals(codeSerialPolicy.getStrategy())) {
            return;
        }

        redisService.flushDb();
        codeRuleService.findValidateCodeRule(EXAMPLE_PKID);
        assertNotNull(redisService.getKey(CACHE_KEY));

        CodeRule codeRule = buildCodeRule();
        codeRule.setPkid(EXAMPLE_PKID);
        codeRule.setMakeUser("some one");
        codeRule.setModifyUser("some one");
        codeRule.getCodeRuleDetails().forEach(codeRuleDetail -> {
            codeRuleDetail.setPkid(randomUUID().toString().replaceAll("-", ""));
        });
        codeRuleService.updateCodeRule(codeRule);
        assertNull(redisService.getKey(CACHE_KEY));
    }

//    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_delete_code_rule_cache_with_real_data() throws ParseException, NotFoundException {
        if(DB.name().equals(codeSerialPolicy.getStrategy())) {
            return;
        }

        redisService.flushDb();
        codeRuleService.findValidateCodeRule(EXAMPLE_PKID);
        assertNotNull(redisService.getKey(CACHE_KEY));

        codeRuleService.deleteCodeRule(EXAMPLE_PKID, "some operator");
        assertNull(redisService.getKey(CACHE_KEY));
    }

//    @After
    public void clearRedisCache() {
        redisService.flushDb();
    }

    private static final String EXAMPLE_PKID = "c3a1104097a9450aaf957c5865b72820";
    private static final String CACHE_KEY = "code-class com.zhongwang.cloud.platform.service.code.rule.service.CodeRuleService" + EXAMPLE_PKID;

}
