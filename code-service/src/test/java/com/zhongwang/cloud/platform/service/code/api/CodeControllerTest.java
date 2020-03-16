package com.zhongwang.cloud.platform.service.code.api;

import com.google.common.collect.Lists;
import com.zhongwang.cloud.platform.service.code.common.CommonControllerTest;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeMultiplyQuery;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeQuery;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeResult;
import com.zhongwang.cloud.platform.service.code.rule.service.CodeRuleGenerateService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.stream.Collectors;

import static com.zhongwang.cloud.platform.bamboo.common.json.Json.writeValueAsString;
import static com.zhongwang.cloud.platform.service.code.rule.entity.MakeEntityForTest.*;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK, properties = {
        "spring.cloud.config.enabled:false",
        "spring.config.name:unit"
})
@AutoConfigureMockMvc
@WebAppConfiguration
public class CodeControllerTest extends CommonControllerTest {

    @Mock
    private CodeRuleGenerateService codeRuleGenerateService;

    @InjectMocks
    private CodeController codeController;

    @Before
    public void init() {
        super.init(codeController);
    }

    /**
     * 验证编码生成接口：
     * 1.verify rest api
     * 2.verify json path
     *
     * @throws Exception
     */
    @Test
    public void test_generate_should_be_right() throws Exception {
        CodeQuery codeQuery = buildCodeQuery();
        codeQuery.setSegmentPkids(Lists.newArrayList(buildPkid(), buildPkid(), buildPkid(), buildPkid()));

        CodeResult codeResult = buildCodeResult();
        codeResult.setSegmentCodes(codeQuery.getSegmentPkids().stream().collect(Collectors.toMap(pkid -> pkid, pkid -> pkid)));

        when(codeRuleGenerateService.generateCode(codeQuery)).thenReturn(codeResult);

        mockMvc.perform(post("/code").contentType(APPLICATION_JSON_UTF8).content(requireNonNull(writeValueAsString(codeQuery)))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).
                andExpect(jsonPath("code").exists()).
                andExpect(jsonPath("segment_codes").exists());

        verify(codeRuleGenerateService, times(1)).generateCode(codeQuery);
        verifyNoMoreInteractions(codeRuleGenerateService);
    }

    /**
     * 验证编码样例生成接口：
     * 1.verify rest api
     * 2.verify json path
     *
     * @throws Exception
     */
    @Test
    public void test_generate_sample_should_be_right() throws Exception {
        CodeQuery codeQuery = buildCodeQuery();
        codeQuery.setSegmentPkids(Lists.newArrayList(buildPkid(), buildPkid(), buildPkid(), buildPkid()));

        CodeResult codeResult = buildCodeResult();
        codeResult.setSegmentCodes(codeQuery.getSegmentPkids().stream().collect(Collectors.toMap(pkid -> pkid, pkid -> pkid)));

        when(codeRuleGenerateService.generateSample(codeQuery)).thenReturn(codeResult);

        mockMvc.perform(post("/code/action/sample").contentType(APPLICATION_JSON_UTF8).content(requireNonNull(writeValueAsString(codeQuery)))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).
                andExpect(jsonPath("code").exists()).
                andExpect(jsonPath("segment_codes").exists());

        verify(codeRuleGenerateService, times(1)).generateSample(codeQuery);
        verifyNoMoreInteractions(codeRuleGenerateService);
    }

    /**
     * 验证批量编码生成接口：
     * 1.verify rest api
     * 2.verify json path
     *
     * @throws Exception
     */
    @Test
    public void test_generate_multiply_should_be_right() throws Exception {
        CodeMultiplyQuery codeMultiplyQuery = (CodeMultiplyQuery) buildCodeMultiplyQuery();
        codeMultiplyQuery.setSegmentPkids(Lists.newArrayList(buildPkid(), buildPkid(), buildPkid(), buildPkid()));

        CodeResult codeResult = buildCodeResult();
        codeResult.setSegmentCodes(codeMultiplyQuery.getSegmentPkids().stream().collect(Collectors.toMap(pkid -> pkid, pkid -> pkid)));

        when(codeRuleGenerateService.generateCode(codeMultiplyQuery)).thenReturn(Lists.newArrayList(
                codeResult, codeResult, codeResult, codeResult, codeResult, codeResult, codeResult, codeResult, codeResult, codeResult));

        mockMvc.perform(post("/code/action/multiply").contentType(APPLICATION_JSON_UTF8).content(requireNonNull(writeValueAsString(codeMultiplyQuery)))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).
                andExpect(jsonPath("$", hasSize(10))).
                andExpect(jsonPath("$[0].code").exists()).
                andExpect(jsonPath("$[0].segment_codes").exists());

        verify(codeRuleGenerateService, times(1)).generateCode(codeMultiplyQuery);
        verifyNoMoreInteractions(codeRuleGenerateService);
    }

}
