package com.zhongwang.cloud.platform.service.code.rule.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 编码请求实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CodeQuery", description = "编码请求实体")
public class CodeQuery implements Serializable {

    // 主键
    @NotBlank
    @ApiModelProperty(value = "编码配置主键")
    private String pkid;

    // 公司主键
    @NotBlank
    @ApiModelProperty(value = "公司主键")
    @JsonProperty("comp_pkid")
    private String compPkid;

    // 系统标识位
    @NotBlank
    @ApiModelProperty(value = "系统标识位")
    @JsonProperty("system_code")
    private String systemCode;

    // 模块标识位
    @NotBlank
    @ApiModelProperty(value = "模块标识位")
    @JsonProperty("module_code")
    private String moduleCode;

    // 操作用户
    @NotBlank
    @ApiModelProperty(value = "操作用户")
    private String operator;

    // 扩展参数
    @ApiModelProperty(value = "扩展参数键值对")
    private Map<String, String> parameters;

    // 段主键
    @ApiModelProperty(value = "段主键集合")
    @JsonProperty("segment_pkids")
    private List<String> segmentPkids;

    public CodeQuery withPkid(String pkid) {
        this.setPkid(pkid);
        return this;
    }

    public CodeQuery withParameters(Map<String, String> parameters) {
        this.setParameters(parameters);
        return this;
    }

}