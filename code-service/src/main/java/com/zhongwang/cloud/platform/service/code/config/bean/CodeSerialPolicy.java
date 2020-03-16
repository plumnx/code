package com.zhongwang.cloud.platform.service.code.config.bean;

import com.zhongwang.cloud.platform.service.code.common.CodeConst;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter@Getter
@ConfigurationProperties(prefix = "code.config.serial.policy")
public class CodeSerialPolicy {

    private int increateLimitNum;

    private int marginNum;

    private String strategy;

    public int getDiffNum() {
        return this.increateLimitNum - this.marginNum;
    }

    public CodeConst.CodeSerialStrategy getCodeSerialStrategy() {
        if(null != this.strategy) {
            return CodeConst.CodeSerialStrategy.of(this.strategy);
        }
        return null;
    }

}
