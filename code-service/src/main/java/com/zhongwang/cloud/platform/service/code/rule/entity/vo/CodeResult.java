package com.zhongwang.cloud.platform.service.code.rule.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CodeResult", description = "编码响应实体")
public class CodeResult implements Serializable {

    private static final long serialVersionUID = 6281832834951976329L;

    // 编码
    @ApiModelProperty(value = "编码")
    private String code;

    // 段编码
    @ApiModelProperty(value = "段编码集合")
    private Map<String, String> segmentCodes;

    public CodeResult() {
    }

    public CodeResult(String code, Map<String, String> segmentCodes) {
        this.code = code;
        this.segmentCodes = segmentCodes;
    }
}
