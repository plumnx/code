package com.zhongwang.cloud.platform.service.code.rule.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class CodeBatchQuery {

    // 公司主键
    @JsonProperty("comp_pkid")
    private String compPkid;

    // 系统标识位
    @JsonProperty("system_code")
    private String systemCode;

    // 模块标识位
    @JsonProperty("module_code")
    private String moduleCode;

    // 操作用户
    private String operator;

    @NotNull
    private Map<String, CodeConstruct> codes;

}
