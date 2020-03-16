package com.zhongwang.cloud.platform.service.code.rule.lock;

import com.google.common.base.Strings;
import com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleDetail;
import com.zhongwang.cloud.platform.service.code.rule.validator.RuleDetailValidator;
import com.zhongwang.cloud.platform.service.code.rule.validator.RuleDetailValidator.ConstValidator;
import com.zhongwang.cloud.platform.service.code.rule.validator.RuleDetailValidator.FieldValidator;
import com.zhongwang.cloud.platform.service.code.rule.validator.RuleDetailValidator.SerialValidator;
import com.zhongwang.cloud.platform.service.code.rule.validator.RuleDetailValidator.SysValidator;
import org.apache.commons.lang.StringUtils;

import java.util.stream.Stream;

import static org.apache.commons.lang.StringUtils.isNumeric;

public enum SegmentType {

    CONST_VARIABLE(1, ConstBuilder.class, ConstValidator.class),
    SYS_VARIABLE(2, VariableBuilder.class, SysValidator.class),
    FIELD_VARIABLE(3, FieldBuilder.class, FieldValidator.class),
    //    SQL_VARIABLE(4, new RuleBuilder(), new RuleDetailValidator()),
    //    SCRIPT_VARIABLE(5, RuleBuilder(), new RuleDetailValidator()),
    SERIAL_VARIABLE(6, SerialBuilder.class, SerialValidator.class);

    private Integer type;
    private Class ruleBuilder;
    private Class ruleDetailValidator;

    SegmentType(Integer type, Class ruleBuilder, Class ruleDetailValidator) {
        this.type = type;
        this.ruleBuilder = ruleBuilder;
        this.ruleDetailValidator = ruleDetailValidator;
    }

    public static boolean isExist(Integer type) {
        return of(type) != null;
    }

    public static boolean isExist(String sectionType) {
        if (null == sectionType) {
            return false;
        }
        return isExist(Integer.valueOf(sectionType));
    }

    public static SegmentType of(Integer type) {
        return Stream.of(values()).filter((segment_type) -> segment_type.getType().equals(type)).findFirst().orElse(null);
    }

    public static SegmentType of(String type) {
        if(Strings.isNullOrEmpty(type) || !isNumeric(type)) {
            return null;
        }
        return of(Integer.valueOf(type));
    }

    public final RuleBuilder getRuleBuilder() throws IllegalAccessException, InstantiationException {
        return (RuleBuilder) this.ruleBuilder.newInstance();
    }

    public void validate(CodeRuleDetail codeRuleDetail) throws CodeInvalidParametersException, IllegalAccessException, InstantiationException {
        RuleDetailValidator ruleDetailValidator = ((RuleDetailValidator) this.ruleDetailValidator.newInstance());
        ruleDetailValidator.validate(codeRuleDetail);
    }

    public Integer getType() {
        return this.type;
    }

}
