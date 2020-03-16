package com.zhongwang.cloud.platform.service.code.api;

import com.zhongwang.cloud.platform.service.code.common.CommonControllerTest;
import com.zhongwang.cloud.platform.service.code.common.util.Dates;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.RemoveData;
import com.zhongwang.cloud.platform.service.code.rule.service.CodeRuleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.google.common.collect.Lists.newArrayList;
import static com.zhongwang.cloud.platform.bamboo.common.json.Json.writeValueAsString;
import static com.zhongwang.cloud.platform.service.code.common.util.Dates.YYYY_MM_DD_T_HH_MM_SS_Z;
import static com.zhongwang.cloud.platform.service.code.common.util.Dates.parseText;
import static com.zhongwang.cloud.platform.service.code.rule.entity.MakeEntityForTest.*;
import static java.util.Objects.requireNonNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK, properties = {
        "spring.cloud.config.enabled:false",
        "spring.config.name:unit"
})
@AutoConfigureMockMvc
@WebAppConfiguration
public class CodeRuleControllerTest extends CommonControllerTest {

    @Mock
    private CodeRuleService codeRuleService;

    @InjectMocks
    private CodeRuleController codeRuleController;

    private MockMvc mockMvc;

    @InjectMocks
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.
                standaloneSetup(codeRuleController).
                setCustomArgumentResolvers(pageableArgumentResolver).
                setMessageConverters(jacksonMessageConverter).
                build();
    }

    /**
     * 新增
     *
     * @throws Exception
     */
    @Test
    public void test_add_code_rule_be_right() throws Exception {
        when(codeRuleService.insertCodeRule(any(CodeRule.class))).thenAnswer(
                invocation -> invocation.getArgumentAt(0, CodeRule.class));

        mockMvc.perform(
                post("/rule").
                        contentType(APPLICATION_JSON_UTF8).
                        content(requireNonNull(writeValueAsString(buildCodeRule().
                                withEffectiveDate(Dates.parseDate("2017-01-01T00:00:00.000Z", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")).
                                withExpirationDate(Dates.parseDate("2099-12-31T00:00:00.000Z", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")))))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).
                andExpect(jsonPath("pkid").exists()).
                andExpect(jsonPath("effective_date").exists()).
                andExpect(jsonPath("effective_date").value("2017-01-01T00:00:00.000Z")).
                andExpect(jsonPath("expiration_date").exists()).
                andExpect(jsonPath("expiration_date").value("2099-12-31T00:00:00.000Z"));

        verify(codeRuleService, times(1)).insertCodeRule(any(CodeRule.class));
        verifyNoMoreInteractions(codeRuleService);
    }

    /**
     * 修改
     *
     * @throws Exception
     */
    @Test
    public void test_modify_code_rule_be_right() throws Exception {
        when(codeRuleService.insertCodeRule(any(CodeRule.class))).thenReturn(buildCodeRule().withPkid("some pkid"));

        mockMvc.perform(put("/rule").contentType(APPLICATION_JSON_UTF8).content(requireNonNull(writeValueAsString(buildCodeRule())))).
                andExpect(status().isOk());

        verify(codeRuleService, times(1)).updateCodeRule(any(CodeRule.class));
        verifyNoMoreInteractions(codeRuleService);
    }

    /**
     * 删除
     *
     * @throws Exception
     */
    @Test
    public void test_delete_code_rule_be_right() throws Exception {
        when(codeRuleService.insertCodeRule(any(CodeRule.class))).thenReturn(buildCodeRule().withPkid("some pkid"));

        mockMvc.perform(delete("/rule").contentType(APPLICATION_JSON_UTF8).
                param("operator", "operator").
                content(requireNonNull(writeValueAsString(new RemoveData(newArrayList("some pkid"), "some one"))))).
                andExpect(status().isOk());

        verify(codeRuleService, times(1)).deleteCodeRule(any(), anyString());
        verifyNoMoreInteractions(codeRuleService);
    }

    /**
     * 分页查询
     */
    @Test
    public void test_query_by_page_should_be_ok() throws Exception {
        final String rule_code = "department code";
        final String rule_name = "depertment code generate rule";
        final String effective_date = "2018-01-01T00:00:00.000Z";
        final String expiration_date = "2028-12-31T23:59:59.000Z";
        final String system_code = "MES";
        final String module_code = "some departments code";
        CodeRule codeRule =
                new CodeRule(rule_code, rule_name, effective_date, expiration_date, system_code, module_code).withPkid(buildPkid());
        codeRule.setCodeRuleDetails(newArrayList(buildConstRule(), buildFieldRule(), buildSysRule(), buildSerialRule()));

        when(codeRuleService.queryByPage(
                anyObject(), anyObject())
        ).thenReturn(new PageImpl<>(newArrayList(codeRule), new PageRequest(0, 10), 1));

        mockMvc.perform(get("/rule").
                contentType(APPLICATION_JSON_UTF8).
                param("page", "0").
                param("size", "10").
                param("rule_code", rule_code).
                param("rule_name", rule_name).
                param("effective_date", effective_date).
                param("expiration_date", expiration_date).
                param("system_code", system_code).
                param("module_code", module_code).
                accept(APPLICATION_JSON_UTF8)).
                andExpect(MockMvcResultMatchers.status().isOk()).
                andExpect(jsonPath("$.content").exists()).
                andExpect(jsonPath("$.content[0]").exists()).
                andExpect(jsonPath("$.content[0].pkid").exists()).
                andExpect(jsonPath("$.content[0].rule_code").value(rule_code)).
                andExpect(jsonPath("$.content[0].rule_name").value(rule_name)).
                andExpect(jsonPath("$.content[0].effective_date").value(effective_date)).
                andExpect(jsonPath("$.content[0].expiration_date").value(expiration_date)).
                andExpect(jsonPath("$.content[0].system_code").value(system_code)).
                andExpect(jsonPath("$.content[0].module_code").value(module_code)).
                andExpect(jsonPath("$.content[0].code_rule_details").exists()).
                andExpect(jsonPath("$.first").value(true)).
                andExpect(jsonPath("$.last").value(true)).
                andExpect(jsonPath("$.number_of_elements").value(1)).
                andExpect(jsonPath("$.number").value(0)).
                andExpect(jsonPath("$.size").value(10)).
                andExpect(jsonPath("$.total_pages").value(1)).
                andExpect(jsonPath("$.total_elements").value(1));


        verify(codeRuleService, times(1)).queryByPage(anyObject(), anyObject());
        verifyNoMoreInteractions(codeRuleService);
    }

    /**
     * 明细 1
     *
     * @throws Exception Exception
     */
    @Test
    public void query_by_item_through_should_be_ok() throws Exception {
        CodeRule codeRule = buildCodeRule();

        when(codeRuleService.findByPkidAndFlagDelete(codeRule.getPkid())).thenReturn(codeRule);

        mockMvc.perform(get("/rule/" + codeRule.getPkid()).
                contentType(APPLICATION_JSON_UTF8).
                accept(APPLICATION_JSON_UTF8)).
                andExpect(MockMvcResultMatchers.status().isOk()).
                andExpect(jsonPath("$.pkid").exists()).
                andExpect(jsonPath("$.rule_code").value(codeRule.getRuleCode())).
                andExpect(jsonPath("$.rule_name").value(codeRule.getRuleName())).
                andExpect(jsonPath("$.effective_date").value(parseText(codeRule.getEffectiveDate(), YYYY_MM_DD_T_HH_MM_SS_Z))).
                andExpect(jsonPath("$.expiration_date").value(parseText(codeRule.getExpirationDate(), YYYY_MM_DD_T_HH_MM_SS_Z))).
                andExpect(jsonPath("$.system_code").value(codeRule.getSystemCode())).
                andExpect(jsonPath("$.module_code").value(codeRule.getModuleCode())).
                andExpect(jsonPath("$.code_rule_details").exists());


        verify(codeRuleService, times(1)).findByPkidAndFlagDelete(codeRule.getPkid());
        verifyNoMoreInteractions(codeRuleService);
    }

    /**
     * 明细 2
     *
     * @throws Exception Exception
     */
    @Test
    public void query_by_item_through_code_should_be_ok() throws Exception {
        CodeRule codeRule = buildCodeRule();

        when(codeRuleService.findByRuleCodeAndFlagDelete(codeRule.getSystemCode(), codeRule.getRuleCode())).thenReturn(codeRule);

        mockMvc.perform(get("/rule/action/query-by-code/" + codeRule.getSystemCode() + "/" + codeRule.getRuleCode()).
                contentType(APPLICATION_JSON_UTF8).
                accept(APPLICATION_JSON_UTF8)).
                andExpect(MockMvcResultMatchers.status().isOk()).
                andExpect(jsonPath("$.pkid").exists()).
                andExpect(jsonPath("$.rule_code").value(codeRule.getRuleCode())).
                andExpect(jsonPath("$.rule_name").value(codeRule.getRuleName())).
                andExpect(jsonPath("$.effective_date").value(parseText(codeRule.getEffectiveDate(), YYYY_MM_DD_T_HH_MM_SS_Z))).
                andExpect(jsonPath("$.expiration_date").value(parseText(codeRule.getExpirationDate(), YYYY_MM_DD_T_HH_MM_SS_Z))).
                andExpect(jsonPath("$.system_code").value(codeRule.getSystemCode())).
                andExpect(jsonPath("$.module_code").value(codeRule.getModuleCode())).
                andExpect(jsonPath("$.code_rule_details").exists());


        verify(codeRuleService, times(1)).findByRuleCodeAndFlagDelete(codeRule.getSystemCode(), codeRule.getRuleCode());
        verifyNoMoreInteractions(codeRuleService);
    }

}
