package com.zhongwang.cloud.platform.service.code.rule.service;

import com.zhongwang.cloud.platform.bamboo.common.exception.NotFoundException;
import com.zhongwang.cloud.platform.service.code.common.repository.BaseRepository;
import com.zhongwang.cloud.platform.service.code.common.service.RedisService;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleDetailRepository;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ReflectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.text.ParseException;

import static com.zhongwang.cloud.platform.service.code.common.CodeConst.SYS_COMMON_STATUS.*;
import static com.zhongwang.cloud.platform.service.code.rule.entity.MakeEntityForTest.buildCodeRule;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK, properties = {
        "spring.cloud.config.enabled:false",
        "spring.config.name:unit"
})
@AutoConfigureMockMvc
public class CodeRuleServiceTest {

    @Mock
    private CodeRuleRepository codeRuleRepository;

    @Mock
    private BaseRepository<CodeRule> repository;

    @Mock
    private CodeRuleDetailRepository codeRuleDetailRepository;

    private CodeRuleService codeRuleService;

    @Mock
    private EntityManager entityManager;

    @Inject
    private RedisService redisService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() throws NoSuchFieldException {
        codeRuleService = new CodeRuleService(codeRuleRepository, codeRuleDetailRepository, entityManager);

        Field field = codeRuleService.getClass().getSuperclass().getDeclaredField("repository");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, codeRuleService, repository);
    }

    /*
     *
     * 新增
     *
     */

    @Test
    public void insert_code_rule_should_be_right() throws ParseException {
        CodeRule codeRule = buildCodeRule();
        codeRule.setMakeUser("some one");
        codeRule.setModifyUser("some one");
        codeRule.getCodeRuleDetails().forEach(codeRuleDetail -> {
            codeRuleDetail.setPkid(randomUUID().toString().replaceAll("-", ""));
        });

        when(repository.saveAndFlush(any(CodeRule.class))).thenAnswer(
                invocation -> invocation.getArgumentAt(0, CodeRule.class));

        CodeRule item = codeRuleService.insertCodeRule(codeRule);
        assertItem(item);
        assertNotNull(item.getMakeUser());
    }

    /*
     *
     *  更新
     *
     */

    @Test
    public void update_code_rule_should_be_right() throws ParseException, NotFoundException {
        CodeRule codeRule = buildCodeRule();
        codeRule.setMakeUser("some one");
        codeRule.setModifyUser("some one");
        codeRule.getCodeRuleDetails().forEach(codeRuleDetail -> {
            codeRuleDetail.setPkid(randomUUID().toString().replaceAll("-", ""));
        });

        when(repository.findOne(anyString())).thenReturn(codeRule);
        when(repository.saveAndFlush(any(CodeRule.class))).thenAnswer(
                invocation -> invocation.getArgumentAt(0, CodeRule.class));

        assertItem(codeRuleService.updateCodeRule(codeRule));
    }

    @Test
    public void update_not_exists_code_rule_should_be_failure() throws ParseException, NotFoundException {
        expectedException.expect(NotFoundException.class);

        CodeRule codeRule = buildCodeRule();
        codeRule.setMakeUser("some one");
        codeRule.setModifyUser("some one");
        codeRule.getCodeRuleDetails().forEach(codeRuleDetail -> {
            codeRuleDetail.setPkid(randomUUID().toString().replaceAll("-", ""));
        });

        when(repository.findOne(anyString())).thenReturn(null);
        when(repository.saveAndFlush(any(CodeRule.class))).thenAnswer(
                invocation -> invocation.getArgumentAt(0, CodeRule.class));

        assertItem(codeRuleService.updateCodeRule(codeRule));
    }

    /*
     *
     * 删除
     *
     */

    @Test
    public void delete_code_rule_should_be_pk() throws ParseException, NotFoundException {
        CodeRule codeRule = buildCodeRule();
        codeRule.getCodeRuleDetails().forEach(codeRuleDetail -> {
            codeRuleDetail.setPkid(randomUUID().toString().replaceAll("-", ""));
        });

        when(repository.findOne(anyString())).thenReturn(codeRule);
        when(codeRuleRepository.findOne(anyString())).thenReturn(codeRule);
        when(codeRuleRepository.saveAndFlush(any(CodeRule.class))).thenAnswer(
                invocation -> invocation.getArgumentAt(0, CodeRule.class));

        codeRuleService.deleteCodeRule("some pkid", "some one");

        assertNotNull(codeRule.getFlagDelete());
        assertEquals(codeRule.getFlagDelete().shortValue(), DELETE.shortValue());
    }

    @Test
    public void delete_not_exists_code_rule_should_be_failure() throws ParseException, NotFoundException {
        expectedException.expect(NotFoundException.class);

        CodeRule codeRule = buildCodeRule();
        codeRule.getCodeRuleDetails().forEach(codeRuleDetail -> {
            codeRuleDetail.setPkid(randomUUID().toString().replaceAll("-", ""));
        });

        when(codeRuleRepository.findOne(anyString())).thenReturn(null);
        when(codeRuleRepository.saveAndFlush(any(CodeRule.class))).thenAnswer(
                invocation -> invocation.getArgumentAt(0, CodeRule.class));

        codeRuleService.deleteCodeRule("some pkid", "some one");
    }

    @After
    public void clearRedisCache() {
        redisService.flushDb();
    }

    private void assertItem(CodeRule item) {
        assertNotNull(item);
        assertNotNull(item.getFlagDelete());
        assertEquals(item.getFlagDelete().shortValue(), NOT_DELETE.shortValue());
        assertNotNull(item.getFlagStatus());
        assertEquals(item.getFlagStatus().shortValue(), VALID.shortValue());
        assertNotNull(item.getFlagSort());
        assertNotNull(item.getMakeTime());
        assertNotNull(item.getModifyTime());
        assertNotNull(item.getModifyUser());
    }

}
