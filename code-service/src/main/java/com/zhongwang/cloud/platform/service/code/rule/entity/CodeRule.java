package com.zhongwang.cloud.platform.service.code.rule.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.zhongwang.cloud.platform.service.code.common.entity.BaseEntity;
import com.zhongwang.cloud.platform.service.code.common.validator.InsertAvaliableGroup;
import com.zhongwang.cloud.platform.service.code.common.validator.UpdateAvaliableGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.zhongwang.cloud.platform.service.code.common.util.Dates.parseUTCTxtToDate;

/**
 * 平台-基础数据管理-编码规则
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "plt_base_code_rule", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"rule_code", "system_code", "flag_delete_token"})
})
public class CodeRule extends BaseEntity {

    // 规则编码
    @NotBlank(message = "{VALIDATOR_RULE_CODE_NOT_NULL}", groups = {InsertAvaliableGroup.class, UpdateAvaliableGroup.class})
    @Column(name = "rule_code")
    private String ruleCode;

    // 规则名称
    @NotBlank(message = "{VALIDATOR_RULE_NAME_NOT_NULL}", groups = {InsertAvaliableGroup.class, UpdateAvaliableGroup.class})
    @Column(name = "rule_name")
    private String ruleName;

    // 所属业务
    @NotBlank(message = "{VALIDATOR_BELONG_BUSINESS_NOT_NULL}", groups = {InsertAvaliableGroup.class, UpdateAvaliableGroup.class})
    @Column(name = "belong_business")
    private String belongBusiness;

    // 编码类型
    @NotBlank(message = "{VALIDATOR_CODE_TYPE_NOT_NULL}", groups = {InsertAvaliableGroup.class, UpdateAvaliableGroup.class})
    @Column(name = "code_type")
    private String codeType;

    // 生效日期
    @Column(name = "effective_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT+8")
    @NotNull(message = "{VALIDATOR_EFFECTIVE_DATE_NOT_NULL}", groups = {InsertAvaliableGroup.class, UpdateAvaliableGroup.class})
    private Date effectiveDate;

    // 失效日期
    @Column(name = "expiration_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT+8")
    @NotNull(message = "{VALIDATOR_EXPIRATION_DATE_NOT_NULL}", groups = {InsertAvaliableGroup.class, UpdateAvaliableGroup.class})
    private Date expirationDate;

    // 所属组织(可是部门、工厂、车间、公司等)
    @Column(name = "belong_org_pkid")
    @Setter
    @Getter
    private String belongOrgPkid;

    // 扩展字段
    @Column(name = "ext0")
    @Setter
    @Getter
    private String ext0;

    // 扩展字段1
    @Column(name = "ext1")
    @Setter
    @Getter
    private String ext1;

    // 备注
    @Column(name = "remark")
    @Setter
    @Getter
    private String remark;

    // 明细对象
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "head_pkid")
    @Where(clause = "flag_delete = 0")
//    @OrderBy("flag_sort asc")
    @Setter
    @Getter
    private List<CodeRuleDetail> codeRuleDetails = Lists.newArrayList();

    @Transient
    @Setter
    @Getter
    private String operator;

    public CodeRule() {
    }

    public CodeRule(String ruleCode, String ruleName, String effectiveDate, String expirationDate, String systemCode, String moduleCode) throws ParseException {
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        if(!Strings.isNullOrEmpty(effectiveDate)) {
            this.effectiveDate = parseUTCTxtToDate(effectiveDate);
        }
        if(!Strings.isNullOrEmpty(expirationDate)) {
            this.expirationDate = parseUTCTxtToDate(expirationDate);
        }
        super.setSystemCode(systemCode);
        super.setModuleCode(moduleCode);
    }

    public void addCodeRuleDetail(CodeRuleDetail... codeRuleDetails) {
        this.codeRuleDetails.addAll(Arrays.asList(codeRuleDetails));
    }

    public CodeRule withCodeRuleDetail(CodeRuleDetail... codeRuleDetails) {
        this.codeRuleDetails.addAll(Arrays.asList(codeRuleDetails));
        return this;
    }

    public CodeRule withFlagDelete(Short flagDelete) {
        this.setFlagDelete(flagDelete);
        return this;
    }

    public CodeRule withEffectiveDate(Date effectiveDate) {
        this.setEffectiveDate(effectiveDate);
        return this;
    }

    public CodeRule withExpirationDate(Date expirationDate) {
        this.setExpirationDate(expirationDate);
        return this;
    }

    public CodeRule withPkid(String pkid) {
        this.setPkid(pkid);
        return this;
    }

    public CodeRule withSystemCode(String systemCode) {
        this.setSystemCode(systemCode);
        return this;
    }

    public CodeRule withModuleCode(String moduleCode) {
        this.setModuleCode(moduleCode);
        return this;
    }

    public CodeRule withRuleCode(String ruleCode) {
        this.setRuleCode(ruleCode);
        return this;
    }

    public CodeRule withRuleName(String ruleName) {
        this.setRuleName(ruleName);
        return this;
    }

}
