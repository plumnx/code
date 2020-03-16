package com.zhongwang.cloud.platform.service.code.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;

public abstract class CommonControllerTest {

    protected MockMvc mockMvc;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    public void init(Object... controllers) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
        jacksonMessageConverter.setObjectMapper(objectMapper);

        this.mockMvc = MockMvcBuilders.
                standaloneSetup(controllers).
                setMessageConverters(jacksonMessageConverter).
                build();
    }

}
