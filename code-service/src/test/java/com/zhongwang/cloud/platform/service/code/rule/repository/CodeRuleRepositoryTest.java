package com.zhongwang.cloud.platform.service.code.rule.repository;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.zhongwang.cloud.platform.bamboo.common.exception.NotFoundException;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule;
import com.zhongwang.cloud.platform.service.code.rule.service.CodeRuleQueryService;
import com.zhongwang.cloud.platform.service.code.rule.service.CodeRuleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.inject.Inject;
import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

/**
 * verify CodeRuleRepository
 */
@SpringBootTest(webEnvironment = MOCK, properties = {
        "spring.cloud.config.enabled:false",
        "spring.config.name:unit"
})
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
public class CodeRuleRepositoryTest {

    @Inject
    private CodeRuleService codeRuleService;

    @Inject
    private CodeRuleQueryService codeRuleQueryService;

    @Inject
    private CodeRuleRepository codeRuleRepository;

    /**
     * 验证正确保存的规则，应该能根据主键查询出来
     *
     * @throws ParseException
     */
    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void query_the_saved_code_rule_should_be_right() throws NotFoundException {
        CodeRule codeRule = codeRuleQueryService.selectCodeRuleAndCodeRuleDetailByPkid(SINGLE_PKID);
        assertNotNull(codeRule);
        assertEquals(SINGLE_PKID, codeRule.getPkid());
    }

    private static final String SINGLE_PKID = "c3a1104097a9450aaf957c5865b72820";

    /**
     * 验证正确保存但失效的规则，应该不能根据主键查询出来
     *
     * @throws ParseException
     */
    @Test
    @DatabaseSetup("/dbunit/CODE_UNIT_TEST_006.xml")
    public void query_the_expiry_items_should_be_null() {
        List<CodeRule> codeRules = codeRuleRepository.findAll();
        codeRules.forEach(codeRule -> {
            try {
                assertNull(codeRuleQueryService.selectCodeRuleAndCodeRuleDetailByPkid(codeRule.getPkid()));
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        });
    }

}


