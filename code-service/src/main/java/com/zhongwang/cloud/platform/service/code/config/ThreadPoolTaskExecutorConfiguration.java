package com.zhongwang.cloud.platform.service.code.config;

import com.google.common.base.Strings;
import com.zhongwang.cloud.platform.service.code.common.util.RejectedExecutionPolicy;
import com.zhongwang.cloud.platform.service.code.config.bean.ThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableConfigurationProperties(ThreadPool.class)
public class ThreadPoolTaskExecutorConfiguration {

    @Autowired
    private ThreadPool threadPool;

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor myTaskAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPool.getCorePoolSize());
        executor.setMaxPoolSize(threadPool.getMaxPoolSize());
        executor.setQueueCapacity(threadPool.getQueueCapacity());
        executor.setKeepAliveSeconds(threadPool.getKeepAliveSeconds());
        if (!Strings.isNullOrEmpty(threadPool.getThreadNamePrefix())) {
            executor.setThreadNamePrefix(threadPool.getThreadNamePrefix());
        }
        executor.setRejectedExecutionHandler(
                RejectedExecutionPolicy.rejectedExecutionPolicy(threadPool.getRejectedExecutionHandler()));
        executor.initialize();
        return executor;
    }

}
