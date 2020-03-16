package com.zhongwang.cloud.platform.service.code.config;

import com.zhongwang.cloud.platform.bamboo.clientsecurity.oauth2.ClientSecurityConfigurerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@ConditionalOnProperty(name = "bamboo.client.security.enabled", havingValue = "true", matchIfMissing = true)
public class ClientSecurityConfiguration extends ClientSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                    .regexMatchers("^/(?!actuator).*")
                    .and()
                .authorizeRequests()
                    .anyRequest().authenticated();
    }

}
