package com.zhongwang.cloud.platform.service.code.rule.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class CodeConstruct {

    @JsonProperty("pkid")
    private String pkid;

    @JsonProperty("size")
    private Integer size;

    @JsonProperty("parameters")
    private Map<String, String> parameters;

    @JsonProperty("segment_pkids")
    private List<String> segmentPkids;

}
