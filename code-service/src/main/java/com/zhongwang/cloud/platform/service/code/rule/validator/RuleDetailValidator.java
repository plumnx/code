package com.zhongwang.cloud.platform.service.code.rule.validator;

import com.google.common.base.Strings;
import com.zhongwang.cloud.platform.service.code.common.CodeConst.IsSerial;
import com.zhongwang.cloud.platform.service.code.common.CodeConst.IsShow;
import com.zhongwang.cloud.platform.service.code.common.CodeConst.SupplyType;
import com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleDetail;
import org.apache.commons.lang.StringUtils;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException.Error.*;
import static com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException.check;
import static org.apache.commons.lang.StringUtils.isNumeric;

public interface RuleDetailValidator {

    void validate(CodeRuleDetail codeRuleDetail) throws CodeInvalidParametersException;

    /**
     * “常量”段不支持“段长度”、“补位方式”、“补位字符”、“日期格式”等格式化设置，所录入的段值即最终该段处理结果，并且段值项不能为空。
     * <p>
     * 段值，必填
     * 段长度，为空
     * 补位方式，为空或不补位
     * 补位字符，为空
     * 日期格式，为空
     * 流水依据，选填，是|否
     * 显示，选填，是|否
     * 分隔符，选填
     */
    class ConstValidator implements RuleDetailValidator {

        @Override
        public void validate(CodeRuleDetail codeRuleDetail) throws CodeInvalidParametersException {
            check(null != codeRuleDetail.getSectionValue(), RULE_DETAIL_SECTION_VALUE_NOT_CORRECT);
            check(null == codeRuleDetail.getSectionLength(), RULE_DETAIL_SECTION_LENGTH_NOT_CORRECT);
            check(isNullOrEmpty(codeRuleDetail.getSupplyType())
                    || SupplyType.NO_TRIM.getValue().equals(codeRuleDetail.getSupplyType()), RULE_DETAIL_SUPPLY_TYPE_NOT_CORRECT);
            check(isNullOrEmpty(codeRuleDetail.getSupplyChar()), RULE_DETAIL_SUPPLY_CHAR_NOT_CORRECT);
            check(isNullOrEmpty(codeRuleDetail.getDateFormat()), RULE_DETAIL_DATE_FORMAT_NOT_CORRECT);
            check(IsSerial.isExist(codeRuleDetail.getFlagSerial()), RULE_DETAIL_FLAG_SERIAL_NOT_CORRECT);
            check(IsShow.isExist(codeRuleDetail.getFlagSerial()), RULE_DETAIL_FLAG_SHOW_NOT_CORRECT);
        }

    }

    /**
     * “系统变量”的“段值”来自于系统变量服务的编码值，支持“段长度” 、“补位方式”、“补位字符”、“日期格式”等格式化设置。
     * 该段值的处理过程是，通过将编码值在每次请求参数集合中翻译得到实际值，再通过一些格式化设置，得到最终（段）处理结果。
     * <p>
     * 段值，必填
     * 段长度，选填
     * 补位方式，选填，不为空并且补位方式不是“不补位”时，则要求段长度不为空
     * 补位字符，选填，不为空则补位方式必须为必填，并且不能是“不补位”
     * 日期格式，选填
     * 流水依据，选填，是|否
     * 显示，选填，是|否
     * 分隔符，选填
     */
    class SysValidator implements RuleDetailValidator {

        @Override
        public void validate(CodeRuleDetail codeRuleDetail) throws CodeInvalidParametersException {
            check(null != codeRuleDetail.getSectionValue(), RULE_DETAIL_SECTION_VALUE_NOT_CORRECT);
            if (!Strings.isNullOrEmpty(codeRuleDetail.getSupplyType())) {
                check(SupplyType.isExist(codeRuleDetail.getSupplyType()), RULE_DETAIL_SUPPLY_TYPE_NOT_CORRECT);
                if (!codeRuleDetail.getSupplyType().equals(SupplyType.NO_TRIM.getValue())) {
                    check(null != codeRuleDetail.getSectionLength(), RULE_DETAIL_SECTION_LENGTH_NOT_CORRECT);
                }
            }
            if (!Strings.isNullOrEmpty(codeRuleDetail.getSupplyChar())) {
                check(null != codeRuleDetail.getSectionType(), RULE_DETAIL_SUPPLY_TYPE_NOT_CORRECT);
                check(!SupplyType.NO_TRIM.getValue().equals(codeRuleDetail.getSectionType()), RULE_DETAIL_SUPPLY_TYPE_NOT_CORRECT);
            }
            check(IsSerial.isExist(codeRuleDetail.getFlagSerial()), RULE_DETAIL_FLAG_SERIAL_NOT_CORRECT);
            check(IsShow.isExist(codeRuleDetail.getFlagSerial()), RULE_DETAIL_FLAG_SHOW_NOT_CORRECT);
        }

    }

    /**
     * “字段值”同“系统变量”的处理方式，区别在于该段值可由用户进行自定义，而不是系统统一配置。
     */
    class FieldValidator extends SysValidator {
    }

    /**
     * “流水号”段，不支持“段值”、“段长度”、“补位方式”、“补位字符”、“日期格式”等设置，并且在编码规则中只能存在一个。
     * <p>
     * 段值，必填，必须为数值类型
     * 段长度，选填
     * 补位方式，选填，不为空并且补位方式不是“不补位”时，则要求段长度不为空
     * 补位字符，选填，不为空则补位方式必须为必填，并且不能是“不补位”
     * 日期格式，为空
     * 流水依据，必填，否
     * 显示，选填，是
     * 分隔符，选填
     */
    class SerialValidator implements RuleDetailValidator {

        @Override
        public void validate(CodeRuleDetail codeRuleDetail) throws CodeInvalidParametersException {
            check(null != codeRuleDetail.getSectionValue(), RULE_DETAIL_SECTION_VALUE_NOT_CORRECT);
            check(isNumeric(codeRuleDetail.getSectionValue()), RULE_DETAIL_SECTION_VALUE_NOT_CORRECT);
            if (!Strings.isNullOrEmpty(codeRuleDetail.getSupplyType())) {
                check(SupplyType.isExist(codeRuleDetail.getSupplyType()), RULE_DETAIL_SUPPLY_TYPE_NOT_CORRECT);
                if (!codeRuleDetail.getSupplyType().equals(SupplyType.NO_TRIM.getValue())) {
                    check(null != codeRuleDetail.getSectionLength(), RULE_DETAIL_SECTION_LENGTH_NOT_CORRECT);
                }
            }
            if (!Strings.isNullOrEmpty(codeRuleDetail.getSupplyChar())) {
                check(null != codeRuleDetail.getSectionType(), RULE_DETAIL_SUPPLY_TYPE_NOT_CORRECT);
                check(!SupplyType.NO_TRIM.getValue().equals(codeRuleDetail.getSectionType()), RULE_DETAIL_SUPPLY_TYPE_NOT_CORRECT);
            }
            check(IsSerial.NO.getValue() == codeRuleDetail.getFlagSerial(),
                    RULE_DETAIL_FLAG_SERIAL_NOT_CORRECT);
            check(IsShow.YES.getValue() == codeRuleDetail.getFlagShow(),
                    RULE_DETAIL_FLAG_SHOW_NOT_CORRECT);
        }

    }

}
