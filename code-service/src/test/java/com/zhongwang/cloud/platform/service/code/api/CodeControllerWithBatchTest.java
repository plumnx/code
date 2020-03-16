package com.zhongwang.cloud.platform.service.code.api;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.zhongwang.cloud.platform.bamboo.common.json.Json.writeValueAsString;
import static com.zhongwang.cloud.platform.service.code.rule.entity.MakeEntityForTest.buildCodeQuery;
import static com.zhongwang.cloud.platform.service.code.rule.entity.MakeEntityForTest.buildUserInfo;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertFalse;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = MOCK, properties = {
        "spring.cloud.config.enabled:false",
        "spring.config.name:unit"
})
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
public class CodeControllerWithBatchTest {

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
    public void test_generate_flow_should_be_ok() throws Exception {
        List<String> codes = Lists.newArrayList();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                try {
                    codes.add(this.mockMvc.perform(
                            post("/code").
                                    contentType(APPLICATION_JSON_UTF8).
                                    content(requireNonNull(writeValueAsString(buildCodeQuery().withPkid(SINGLE_PKID)))).
                                    accept(APPLICATION_JSON_UTF8)).
                            andExpect(status().isOk()).
                            andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) {
                assertFalse(codes.stream().anyMatch(Strings::isNullOrEmpty));
                break;
            }
            Thread.sleep(1000);
        }
    }

    private static final String SINGLE_PKID = "c3a1104097a9450aaf957c5865b7282f";

}
