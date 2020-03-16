package com.zhongwang.cloud.platform.service.code;

import com.zhongwang.cloud.platform.bamboo.clientsecurity.oauth2.EnableClientSecurity;
import com.zhongwang.cloud.platform.bamboo.web.log.EnableMethodTrace;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableEurekaClient
@EnableClientSecurity
@EnableTransactionManagement
//@EnableCaching
@EnableMethodTrace
public class CodeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeServiceApplication.class, args);
    }

}
