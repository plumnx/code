package com.zhongwang.cloud.platform.service.code.rule.service;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.google.common.collect.Maps;
import com.zhongwang.cloud.platform.service.code.common.service.RedisService;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeQuery;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeResult;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.zhongwang.cloud.platform.service.code.rule.entity.MakeEntityForTest.buildCodeQuery;
import static com.zhongwang.cloud.platform.service.code.rule.entity.MakeEntityForTest.changeCodeQueryParameters;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@SpringBootTest(webEnvironment = MOCK, properties = {
        "spring.cloud.config.enabled:false",
        "spring.config.name:unit"
})
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class })
public class GenerateCodeWithDbUnitTest {

    @Inject
    private CodeRuleRepository codeRuleRepository;

    @Inject
    private CodeRuleGenerateService codeRuleGenerateService;

    @Inject
    private RedisService redisService;

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_001.xml"})
//    @ExpectedDatabase(assertionMode= NON_STRICT,value="/dbunit/CODE_UNIT_TEST_001_RESULT.xml")
    public void generate_code_rule_should_be_exactly() throws Exception {
        List<CodeRule> codeRules = codeRuleRepository.findAll();
        for(CodeRule codeRule: codeRules) {
            redisService.flushDb();

            CodeResult codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(codeRule.getPkid()));
            assertEquals(codeRule.getRemark(), codeResult.getCode());
        }
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_002.xml"})
    public void generate_code_rule_duplicatily_should_be_exactly() throws Exception {
        CodeQuery codeQuery;
        List<CodeRule> codeRules = codeRuleRepository.findAll();
        for(CodeRule codeRule: codeRules) {
            redisService.flushDb();

            String[] remarks = codeRule.getRemark().split(",");
            for(int i = 0; i < remarks.length; i++) {
                if(i % 2 == 0) {
                    codeQuery = buildCodeQuery().withPkid(codeRule.getPkid());
                } else {
                    codeQuery = buildCodeQuery().withPkid(codeRule.getPkid()).withParameters(changeCodeQueryParameters());
                }
                CodeResult codeResult = codeRuleGenerateService.generateCode(codeQuery);
                assertEquals(remarks[i], codeResult.getCode());
            }
        }
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_003.xml"})
    public void generate_code_rule_duplicatilyT_should_be_exactly() throws Exception {
        Map<String, String> parameters = Maps.newHashMap();
        CodeResult codeResult;
        List<CodeRule> codeRules = codeRuleRepository.findAll();
        for(CodeRule codeRule: codeRules) {
            redisService.flushDb();

            String[] remarks = codeRule.getRemark().split(",");

            parameters.clear();
            parameters.put("Field", "A");
            parameters.put("SysVariable", "SysVariable");
            codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(codeRule.getPkid()).withParameters(parameters));
            assertEquals(remarks[0], codeResult.getCode());

            parameters.clear();
            parameters.put("Field", "B");
            parameters.put("SysVariable", "SysVariable");
            codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(codeRule.getPkid()).withParameters(parameters));
            assertEquals(remarks[1], codeResult.getCode());

            parameters.clear();
            parameters.put("Field", "C");
            parameters.put("SysVariable", "SysVariable");
            codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(codeRule.getPkid()).withParameters(parameters));
            assertEquals(remarks[2], codeResult.getCode());
        }
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_004.xml"})
    public void generate_code_rule_duplicatilyF_should_be_exactly() throws Exception {
        Map<String, String> parameters = Maps.newHashMap();
        CodeResult codeResult;
        List<CodeRule> codeRules = codeRuleRepository.findAll();
        for(CodeRule codeRule: codeRules) {
            redisService.flushDb();

            String[] remarks = codeRule.getRemark().split(",");

            parameters.clear();
            parameters.put("Field", "A");
            parameters.put("SysVariable", "1");
            codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(codeRule.getPkid()).withParameters(parameters));
            assertEquals(remarks[0], codeResult.getCode());

            parameters.clear();
            parameters.put("Field", "A");
            parameters.put("SysVariable", "1");
            codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(codeRule.getPkid()).withParameters(parameters));
            assertEquals(remarks[1], codeResult.getCode());

            parameters.clear();
            parameters.put("Field", "A");
            parameters.put("SysVariable", "2");
            codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(codeRule.getPkid()).withParameters(parameters));
            assertEquals(remarks[2], codeResult.getCode());

            parameters.clear();
            parameters.put("Field", "B");
            parameters.put("SysVariable", "2");
            codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(codeRule.getPkid()).withParameters(parameters));
            assertEquals(remarks[3], codeResult.getCode());
        }
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_008.xml"})
    public void generate_code_rule_without_Serial_should_be_exactly() throws Exception {
        Map<String, String> parameters = Maps.newHashMap();
        CodeResult codeResult;
        List<CodeRule> codeRules = codeRuleRepository.findAll();
        for(CodeRule codeRule: codeRules) {
            redisService.flushDb();

            String[] remarks = codeRule.getRemark().split(",");

            parameters.clear();
            parameters.put("Field", "C");
            parameters.put("SysVariable", "1");
            codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(codeRule.getPkid()).withParameters(parameters));
            assertEquals(remarks[0], codeResult.getCode());

            parameters.clear();
            parameters.put("Field", "C");
            parameters.put("SysVariable", "1");
            codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(codeRule.getPkid()).withParameters(parameters));
            assertEquals(remarks[1], codeResult.getCode());

            parameters.clear();
            parameters.put("Field", "C");
            parameters.put("SysVariable", "2");
            codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(codeRule.getPkid()).withParameters(parameters));
            assertEquals(remarks[2], codeResult.getCode());

            parameters.clear();
            parameters.put("Field", "D");
            parameters.put("SysVariable", "2");
            codeResult = codeRuleGenerateService.generateCode(buildCodeQuery().withPkid(codeRule.getPkid()).withParameters(parameters));
            assertEquals(remarks[3], codeResult.getCode());
        }
    }

    @After
    public void clearRedisCache() {
        redisService.flushDb();
    }

}
