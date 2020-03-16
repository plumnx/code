package com.zhongwang.cloud.platform.service.code.rule.entity.base;

import com.zhongwang.cloud.platform.service.code.common.entity.BaseEntity;
import com.zhongwang.cloud.platform.service.code.common.validator.InsertAvaliableGroup;
import com.zhongwang.cloud.platform.service.code.common.validator.UpdateAvaliableGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * 规则明细基类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public abstract class BaseCodeRuleDetailSegment extends BaseEntity {

    // 段类型，枚举值， 常量、系统变量、字段值、自定义SQL、流水号。
    @Column(name = "section_type")
    @NotBlank(message = "{VALIDATOR_SECTION_TYPE_NOT_NULL}", groups = {InsertAvaliableGroup.class, UpdateAvaliableGroup.class})
    protected String sectionType;

    // 段值
    @Column(name = "section_value")
    protected String sectionValue;

    // 段长度
    @Column(name = "section_length")
    protected Integer sectionLength;

    // 补位方式
    @Column(name = "supply_type")
    protected String supplyType;

    // 补位字符
    @Column(name = "supply_char")
    protected String supplyChar;

    // 日期格式化
    @Column(name = "date_format")
    protected String dateFormat;

    // 流水依据， 1 ：是流水依据；0：不是流水依据
    @Column(name = "flag_serial")
    protected Integer flagSerial;

    // 是否显示，显示：1，0：不显示
    @Column(name = "flag_show")
    protected Integer flagShow;

    // 段分隔符（两个字符）
    @Column(name = "section_separator")
    protected String sectionSeparator;

    // （处理）段值
    @Transient
    private Object value;

    public BaseCodeRuleDetailSegment withFlagShow(Integer flagShow) {
        this.setFlagShow(flagShow);
        return this;
    }

    public BaseCodeRuleDetailSegment withFlagSerial(Integer flagSerial) {
        this.setFlagSerial(flagSerial);
        return this;
    }
}
