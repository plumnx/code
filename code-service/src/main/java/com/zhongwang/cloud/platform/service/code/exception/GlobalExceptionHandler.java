package com.zhongwang.cloud.platform.service.code.exception;

import com.zhongwang.cloud.platform.bamboo.web.exception.AbstractGlobalExceptionHandler;
import com.zhongwang.cloud.platform.security.common.exception.ZwOAuth2ExceptionRender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.provider.error.OAuth2ExceptionRenderer;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    @Autowired
    public GlobalExceptionHandler(ServerProperties serverProperties) {
        super(serverProperties);
    }

    /**
     * 处理OAuth2认证相关的异常。详细请参照 {@link AbstractOAuth2SecurityExceptionHandler}
     *
     * @return {@link OAuth2ExceptionRenderer} 实现
     */
    @Bean
    public OAuth2ExceptionRenderer oAuth2ExceptionRender() {
        return new ZwOAuth2ExceptionRender();
    }

}
