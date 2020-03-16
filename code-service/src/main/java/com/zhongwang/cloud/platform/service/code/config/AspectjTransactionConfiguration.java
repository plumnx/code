package com.zhongwang.cloud.platform.service.code.config;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class AspectjTransactionConfiguration {

    private static final String transactionExecution = "execution (* com.zhongwang.cloud.platform.service.code.rule.service.*.*(..))";

    private final PlatformTransactionManager transactionManager;

    @Autowired
    public AspectjTransactionConfiguration(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Bean
    public DefaultPointcutAdvisor defaultPointcutAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(transactionExecution);

        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(pointcut);

        Properties attributes = new Properties();
        attributes.setProperty("makeSerialNoToDb*", "PROPAGATION_REQUIRES_NEW, -Exception");
        attributes.setProperty("generate*", "PROPAGATION_REQUIRED, -Exception");
        attributes.setProperty("save*", "PROPAGATION_REQUIRED, -Exception");
        attributes.setProperty("insert*", "PROPAGATION_REQUIRED, -Exception");
        attributes.setProperty("update*", "PROPAGATION_REQUIRED, -Exception");
        attributes.setProperty("delete*", "PROPAGATION_REQUIRED, -Exception");

        attributes.setProperty("select*", "PROPAGATION_REQUIRED, readOnly");
        attributes.setProperty("find*", "PROPAGATION_REQUIRED, readOnly");

        attributes.setProperty("*", "PROPAGATION_REQUIRED, readOnly");

        TransactionInterceptor advice
                = new TransactionInterceptor(transactionManager, attributes);
        advisor.setAdvice(advice);

        return advisor;
    }

}
