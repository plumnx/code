package com.zhongwang.cloud.platform.service.code.rule.service;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.zhongwang.cloud.platform.bamboo.common.exception.NotFoundException;
import com.zhongwang.cloud.platform.bamboo.common.json.Json;
import com.zhongwang.cloud.platform.service.code.common.service.RedisService;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleDetailRepository;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleRepository;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.inject.Inject;
import java.text.ParseException;

import static com.zhongwang.cloud.platform.service.code.common.CodeConst.SYS_COMMON_STATUS.DELETE;
import static com.zhongwang.cloud.platform.service.code.common.CodeConst.SYS_COMMON_STATUS.NOT_DELETE;
import static com.zhongwang.cloud.platform.service.code.common.CodeConst.SystemCode.MES;
import static com.zhongwang.cloud.platform.service.code.common.util.Dates.parseUTCTxtToDate;
import static com.zhongwang.cloud.platform.service.code.rule.entity.MakeEntityForTest.*;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@SpringBootTest(webEnvironment = MOCK, properties = {
        "spring.cloud.config.enabled:false",
        "spring.config.name:unit"
})
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
public class CodeRuleServiceTestWithDbUnitTest {

    @Inject
    private CodeRuleRepository codeRuleRepository;

    @Inject
    private CodeRuleDetailRepository codeRuleDetailRepository;

    @Inject
    private CodeRuleService codeRuleService;

    @Inject
    private RedisService redisService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_insert_code_rule_with_real_data_should_be_ok() throws ParseException {
        redisService.flushDb();

        assertNotNull(codeRuleService.insertCodeRule(buildCodeRule()));
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_update_code_rule_with_real_data_should_be_failure() throws ParseException, NotFoundException {
        redisService.flushDb();

        assertNotNull(codeRuleService.updateCodeRule(buildCodeRule().withPkid(EXAMPLE_PKID)));
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_update_not_exists_code_rule_with_real_data_should_be_failure() throws ParseException, NotFoundException {
        expectedException.expect(NotFoundException.class);
        redisService.flushDb();

        assertNotNull(codeRuleService.updateCodeRule(buildCodeRule().withPkid("some pkid")));
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_delete_code_rule_with_real_data_should_be_ok() throws NotFoundException {
        redisService.flushDb();

        assertTrue(codeRuleRepository.findOne(EXAMPLE_PKID).getFlagDelete().shortValue() == NOT_DELETE);

        codeRuleService.deleteCodeRule(EXAMPLE_PKID, "some one");

        assertFalse(codeRuleRepository.findOne(EXAMPLE_PKID).getFlagDelete().shortValue() == NOT_DELETE);
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_delete_not_exists_code_rule_with_real_data_should_be_ok() throws NotFoundException {
        redisService.flushDb();

        expectedException.expect(NotFoundException.class);

        assertTrue(codeRuleRepository.findAll().stream().
                anyMatch(rule -> rule.getFlagDelete().shortValue() == NOT_DELETE));

        codeRuleService.deleteCodeRule("some pkid", "some one");
    }

    /*
     *
     * 分页
     *
     */

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_001.xml"})
    public void test_query_by_page_should_be_pk() {
        Pageable pageable = new PageRequest(0, 10);
        Page<CodeRule> page = codeRuleService.queryByPage(pageable, new CodeRule().withSystemCode(MES.name()));

        assertNotNull(page);
        assertNotNull(page.getContent());
        assertEquals(19, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertEquals(0, page.getNumber());
        assertEquals(10, page.getNumberOfElements());
        assertEquals(10, page.getSize());
        assertTrue(page.isFirst());
        assertFalse(page.isLast());
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_001.xml"})
    public void test_query_by_page_date_duration_should_be_pk() throws ParseException {
        Pageable pageable = new PageRequest(0, 10);
        Page<CodeRule> page = codeRuleService.queryByPage(pageable,
                new CodeRule().
                        withEffectiveDate(parseUTCTxtToDate("2017-01-01T00:00:00.000Z")).
                        withExpirationDate(parseUTCTxtToDate("2020-12-31T00:00:00.000Z")).
                        withSystemCode(MES.name()));

        assertNotNull(page);
        assertNotNull(page.getContent());
        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
        assertEquals(0, page.getNumber());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(10, page.getSize());
        assertTrue(page.isFirst());
        assertTrue(page.isLast());
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_001.xml"})
    public void test_query_by_page_like_code_name_duration_should_be_pk() throws ParseException {
        Pageable pageable = new PageRequest(0, 10);
        Page<CodeRule> page = codeRuleService.queryByPage(pageable,
                new CodeRule().
                        withRuleCode("-code1-").
                        withRuleName("-name1-").
                        withSystemCode(MES.name()));

        assertNotNull(page);
        assertNotNull(page.getContent());
        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
        assertEquals(0, page.getNumber());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(10, page.getSize());
        assertTrue(page.isFirst());
        assertTrue(page.isLast());
    }

    /*
     *
     * 按照主键查询明细
     *
     */
    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_001.xml"})
    public void test_find_by_pkid_and_flag_delete() throws ParseException, NotFoundException {
        CodeRule param = buildCodeRule();
        param.getCodeRuleDetails().
                removeIf(codeRuleDetail -> codeRuleDetail.getFlagDelete() == DELETE.shortValue());

        CodeRule codeRule = codeRuleService.insertCodeRule(param);
        CodeRule codeRuleDb = codeRuleService.findByPkidAndFlagDelete(codeRule.getPkid());

        assertNotNull(codeRuleDb);
        assertEquals(codeRule.getPkid(), codeRuleDb.getPkid());
        assertEquals(codeRule.getRuleCode(), codeRuleDb.getRuleCode());
        assertEquals(codeRule.getRuleName(), codeRuleDb.getRuleName());
        assertEquals(codeRule.getEffectiveDate(), codeRuleDb.getEffectiveDate());
        assertEquals(codeRule.getExpirationDate(), codeRuleDb.getExpirationDate());
        assertEquals(codeRule.getSystemCode(), codeRuleDb.getSystemCode());
        assertEquals(codeRule.getModuleCode(), codeRuleDb.getModuleCode());

        assertFalse(codeRuleDb.getCodeRuleDetails().isEmpty());

        codeRule.getCodeRuleDetails().forEach(codeRuleDetail -> {
            codeRuleDetail.setHeadPkid(codeRule.getPkid());
        });
        assertEquals(Json.writeValueAsString(codeRule.getCodeRuleDetails()),
                Json.writeValueAsString(codeRuleDb.getCodeRuleDetails()));
    }


    /*
     *
     * 按照编码查询明细
     *
     */

    @Test
    public void test_find_by_rule_code_and_flag_delete() throws ParseException, NotFoundException {
        CodeRule param = buildCodeRule().withRuleCode(buildPkid());
        param.getCodeRuleDetails().
                removeIf(codeRuleDetail -> codeRuleDetail.getFlagDelete() == DELETE.shortValue());

        CodeRule codeRule = codeRuleService.insertCodeRule(param);
        CodeRule codeRuleDb = codeRuleService.findByRuleCodeAndFlagDelete(codeRule.getSystemCode(), codeRule.getRuleCode());

        assertNotNull(codeRuleDb);
        assertEquals(codeRule.getPkid(), codeRuleDb.getPkid());
        assertEquals(codeRule.getRuleCode(), codeRuleDb.getRuleCode());
        assertEquals(codeRule.getRuleName(), codeRuleDb.getRuleName());
        assertEquals(codeRule.getEffectiveDate(), codeRuleDb.getEffectiveDate());
        assertEquals(codeRule.getExpirationDate(), codeRuleDb.getExpirationDate());
        assertEquals(codeRule.getSystemCode(), codeRuleDb.getSystemCode());
        assertEquals(codeRule.getModuleCode(), codeRuleDb.getModuleCode());

        codeRule.getCodeRuleDetails().forEach(codeRuleDetail -> {
            codeRuleDetail.setHeadPkid(codeRule.getPkid());
        });
        assertEquals(Json.writeValueAsString(codeRule.getCodeRuleDetails()),
                Json.writeValueAsString(codeRuleDb.getCodeRuleDetails()));
    }

    @After
    public void clearRedisCache() {
        redisService.flushDb();
    }

    private static String EXAMPLE_PKID = "c3a1104097a9450aaf957c5865b72820";

}
