package com.zhongwang.cloud.platform.service.code.config.bean;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter@Getter
@ConfigurationProperties(prefix = "code.config.threadPool")
public class ThreadPool {

    private String threadNamePrefix;

    private int corePoolSize;

    private int maxPoolSize;

    private int queueCapacity;

    private int keepAliveSeconds;

    private String rejectedExecutionHandler;

}
