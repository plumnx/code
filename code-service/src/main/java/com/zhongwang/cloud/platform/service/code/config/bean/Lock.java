package com.zhongwang.cloud.platform.service.code.config.bean;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "code.config.lock")
public class Lock {

    private int time;

}
