package com.zhongwang.cloud.platform.service.code.rule.entity;

import com.zhongwang.cloud.platform.service.code.rule.entity.base.BaseCodeRuleDetailSegment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 平台-基础数据管理-编码规则-子表
 */
@Entity
@Table(name = "plt_base_code_rule_detail")
@Data
@EqualsAndHashCode(callSuper = false)
public class CodeRuleDetail extends BaseCodeRuleDetailSegment {

    // 编码规则主表主键
    @Column(name = "head_pkid")
    private String headPkid;

    // 扩展0
    @Column(name = "ext0")
    private String ext0;

    // 扩展1
    @Column(name = "ext1")
    private String ext1;

    @Override
    public CodeRuleDetail withFlagShow(Integer flagShow) {
        return (CodeRuleDetail) super.withFlagShow(flagShow);
    }

    @Override
    public CodeRuleDetail withFlagSerial(Integer flagSerial) {
        return (CodeRuleDetail) super.withFlagSerial(flagSerial);
    }

    public CodeRuleDetail withPkid(String pkid) {
        this.setPkid(pkid);
        return this;
    }

    public CodeRuleDetail withHeadPkid(String headPkid) {
        this.headPkid = headPkid;
        return this;
    }

}
