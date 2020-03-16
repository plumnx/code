package com.zhongwang.cloud.platform.service.code.api;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.zhongwang.cloud.platform.bamboo.common.json.Json;
import com.zhongwang.cloud.platform.service.code.common.util.Dates;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.RemoveData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static com.google.common.collect.Lists.newArrayList;
import static com.zhongwang.cloud.platform.bamboo.common.json.Json.writeValueAsString;
import static com.zhongwang.cloud.platform.service.code.rule.entity.MakeEntityForTest.*;
import static java.util.Objects.requireNonNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = MOCK, properties = {
        "spring.cloud.config.enabled:false",
        "spring.config.name:unit"
})
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
public class CodeRuleControllerWithRealDataTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void init() {
        // mock mvc
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        // mock authentication userInfo
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(
                buildUserInfo(), null, null
        ));
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_add_code_rule_should_be_ok() throws Exception {
        mockMvc.perform(
                post("/rule").
                        contentType(APPLICATION_JSON_UTF8).
                        content(requireNonNull(writeValueAsString(buildCodeRule())))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).
                andExpect(jsonPath("pkid").exists());
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_modify_code_rule_should_be_ok() throws Exception {
        CodeRule codeRule = buildCodeRule();
        codeRule.setPkid(SINGLE_PKID);

        this.mockMvc.perform(
                put("/rule").
                        contentType(APPLICATION_JSON_UTF8).
                        content(requireNonNull(writeValueAsString(codeRule))).
                        accept(APPLICATION_JSON_UTF8)).
                andExpect(status().isOk());
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_delete_code_rule_should_be_ok() throws Exception {
        this.mockMvc.perform(
                delete("/rule").
                        contentType(APPLICATION_JSON_UTF8).
                        content(requireNonNull(writeValueAsString(new RemoveData(newArrayList(SINGLE_PKID), "some one")))).
                        accept(APPLICATION_JSON_UTF8)).
                andExpect(status().isOk());
    }

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_005.xml"})
    public void test_add_code_rule_and_generate_code_should_be_ok() throws Exception {
        String pkid = buildPkid();

        String content = mockMvc.perform(
                post("/rule").
                        contentType(APPLICATION_JSON_UTF8).
                        content(requireNonNull(writeValueAsString(
                                buildCodeRule().
                                        withPkid(pkid).
                                        withEffectiveDate(Dates.parseDate(Dates.parseText(new Date(), "yyyy-MM-dd'T00:00:00Z'"), "yyyy-MM-dd'T'HH:mm:ss'Z'")).
                                        withExpirationDate(Dates.parseDate(Dates.parseText(new Date(), "yyyy-MM-dd'T23:59:59Z'"), "yyyy-MM-dd'T'HH:mm:ss'Z'")))))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).
                andExpect(jsonPath("pkid").exists()).
                andExpect(jsonPath("make_user").exists()).
                andExpect(jsonPath("make_time").exists()).
                andExpect(jsonPath("modify_user").exists()).
                andExpect(jsonPath("modify_time").exists()).
                andReturn().
                getResponse().getContentAsString();

        CodeRule codeRule = Json.readValue(content, CodeRule.class);

        this.mockMvc.perform(
                post("/code").
                        contentType(APPLICATION_JSON_UTF8).
                        content(requireNonNull(writeValueAsString(buildCodeQuery().withPkid(codeRule.getPkid())))).
                        accept(APPLICATION_JSON_UTF8)).
                andExpect(status().isOk());
    }

    private static final String SINGLE_PKID = "c3a1104097a9450aaf957c5865b72820";

}
