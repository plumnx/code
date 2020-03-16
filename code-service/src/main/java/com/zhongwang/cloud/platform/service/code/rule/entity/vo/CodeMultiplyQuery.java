//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.zhongwang.cloud.platform.service.code.rule.entity.vo;

import com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException.Error.PROPERTY_NULL_OR_FORMAT_WRONG;

/**
 * 编码请求实体（批量）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CodeQuery", description = "编码请求实体")
public class CodeMultiplyQuery extends CodeQuery implements Serializable {

    // 请求编码数
    @ApiModelProperty(value = "本次生成的编码数")
    @NotNull
    private Integer size;

    public CodeMultiplyQuery() {
    }

    public CodeMultiplyQuery(CodeBatchQuery codeBatchQuery, CodeConstruct codeConstruct) throws CodeInvalidParametersException {
        if(codeConstruct == null) {
            throw new CodeInvalidParametersException(PROPERTY_NULL_OR_FORMAT_WRONG);
        }

        this.setPkid(codeConstruct.getPkid());
        this.setSize(codeConstruct.getSize());
        this.setSegmentPkids(codeConstruct.getSegmentPkids());
        this.setParameters(codeConstruct.getParameters());

        this.setCompPkid(codeBatchQuery.getCompPkid());
        this.setModuleCode(codeBatchQuery.getModuleCode());
        this.setOperator(codeBatchQuery.getOperator());
        this.setSystemCode(codeBatchQuery.getSystemCode());
    }

    public CodeMultiplyQuery withPkid(String pkid) {
        this.setPkid(pkid);
        return this;
    }
    public CodeMultiplyQuery withSize(Integer size) {
        this.size = size;
        return this;
    }

}
