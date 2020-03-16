package com.zhongwang.cloud.platform.service.code.rule.entity;

import com.google.common.base.Strings;
import com.zhongwang.cloud.platform.service.code.common.entity.BaseEntity;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * 平台-基础数据管理-编码规则-子表2-流水
 */
@Entity
@Table(name = "plt_base_code_rule_serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class CodeRuleSerial extends BaseEntity {

    // 编码规则主表主键
    @Column(name = "head_pkid")
    private String headPkid;

    // 显示流水编码
    @Column(name = "serial_union_value")
    private String serialUnionValue;

    // 显示流水编码格式
    @Column(name = "serial_union_value_format")
    private String serialUnionValueFormat;

    // 隐藏流水编码
    @Column(name = "show_union_value")
    private String showUnionValue;

    // 隐藏流水编码格式
    @Column(name = "show_union_value_format")
    private String showUnionValueFormat;

    // 流水号
    @Column(name = "code_max_value")
    private Integer codeMaxValue;

    // 扩展
    @Column(name = "ext0")
    private String ext0;

    // 扩展
    @Column(name = "ext1")
    private String ext1;

    public CodeRuleSerial() {
    }

    public CodeRuleSerial(String headPkid, String serialUnionValueFormat, String serialUnionValue, String showUnionValueFormat, String showUnionValue, Integer codeMaxValue) {
        this.headPkid = headPkid;
        this.serialUnionValueFormat = serialUnionValueFormat;
        this.serialUnionValue = serialUnionValue;
        this.showUnionValueFormat = showUnionValueFormat;
        this.showUnionValue = showUnionValue;
        this.codeMaxValue = codeMaxValue;
    }

    public CodeRuleSerial withCodeMaxValue(Integer codeMaxValue) {
        this.setCodeMaxValue(codeMaxValue);
        return this;
    }

    public CodeRuleSerial withSerialUnionValue(String serialUnionValue) {
        this.serialUnionValue = serialUnionValue;
        return this;
    }

    /**
     * 补充默认参数
     *
     * @param codeQuery
     * @return
     */
    public CodeRuleSerial addProperties(CodeQuery codeQuery) {
        this.setCompPkid(codeQuery.getCompPkid());
        this.setSystemCode(codeQuery.getSystemCode());
        this.setModuleCode(codeQuery.getModuleCode());
        if (Strings.isNullOrEmpty(this.getMakeUser())) {
            this.setMakeUser(codeQuery.getOperator());
            this.setMakeTime(new Date());
        }
        this.setModifyUser(codeQuery.getOperator());
        this.setModifyTime(new Date());
        this.setFlagDelete((short) 0);

        return this;
    }

}
