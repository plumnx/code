package com.zhongwang.cloud.platform.service.code.rule.entity.vo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

@Getter@Setter
public class RemoveData {

    @NotEmpty
    private List<String> pkids;

    @NotBlank
    private String operator;

    public RemoveData() {
    }

    public RemoveData(List<String> pkids, String operator) {
        this.pkids = pkids;
        this.operator = operator;
    }
}
