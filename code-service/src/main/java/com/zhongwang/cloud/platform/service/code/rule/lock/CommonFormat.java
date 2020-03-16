package com.zhongwang.cloud.platform.service.code.rule.lock;

import com.google.common.base.Strings;
import com.zhongwang.cloud.platform.service.code.common.CodeConst;
import com.zhongwang.cloud.platform.service.code.common.util.Dates;
import com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException;
import com.zhongwang.cloud.platform.service.code.rule.entity.base.BaseCodeRuleDetailSegment;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import static com.zhongwang.cloud.platform.service.code.common.util.Dates.parseUTCTxtToDate;

/**
 * 格式化工具类
 */
public interface CommonFormat {

    /**
     * 格式
     * @param value
     * @param baseCodeRuleDetailSegment
     * @param parameters
     * @return
     * @throws CodeInvalidParametersException
     */
    Object format(Object value, BaseCodeRuleDetailSegment baseCodeRuleDetailSegment, Map<String, String> parameters)
            throws CodeInvalidParametersException;

}

/**
 * 日期格式化
 */
class DateFormat implements CommonFormat {

    public Object format(Object value, BaseCodeRuleDetailSegment baseCodeRuleDetail, Map<String, String> parameters)
            throws CodeInvalidParametersException {
        if (!Strings.isNullOrEmpty(baseCodeRuleDetail.getDateFormat())) {
            if (value instanceof Date) {
                return Dates.parseText((Date) value, baseCodeRuleDetail.getDateFormat());
            }
            if (value instanceof String) {
                try {
                    return Dates.parseText(parseUTCTxtToDate((String) value), baseCodeRuleDetail.getDateFormat());
                } catch (ParseException e) {
                    throw new CodeInvalidParametersException(e.getMessage(), e);
                }
            }
        }
        return value;
    }
}

/**
 * 字符串格式化
 */
class TextFormat implements CommonFormat {

    public Object format(Object value, BaseCodeRuleDetailSegment baseCodeRuleDetail, Map<String, String> parameters) {
        value = (new TextFormat.LengthFormat()).format(value, baseCodeRuleDetail, parameters);
        value = (new TextFormat.FillFormat()).format(value, baseCodeRuleDetail, parameters);
        return value;
    }

    /**
     * 补位格式化
     */
    private class FillFormat implements CommonFormat {

        private String formatText(String value, int len, boolean isLeft, String s) {
            String trimChars = String.format("%" + (len - value.length()) + "s", "").replaceAll("\\s", s);
            return isLeft ? trimChars + value : value + trimChars;
        }

        public Object format(Object value, BaseCodeRuleDetailSegment baseCodeRuleDetail, Map<String, String> parameters) {
            String _value = value.toString();
            if(baseCodeRuleDetail.getSectionLength() == null || baseCodeRuleDetail.getSectionLength() == 0) {
                return _value;
            }
            return _value.length() < baseCodeRuleDetail.getSectionLength() && !CodeConst.SupplyType.NO_TRIM.getValue().equals(baseCodeRuleDetail.getSupplyType()) ?
                    this.formatText(_value, baseCodeRuleDetail.getSectionLength(), CodeConst.SupplyType.LEFT_TRIM.getValue().equals(baseCodeRuleDetail.getSupplyType()), baseCodeRuleDetail.getSupplyChar()) :
                    _value;
        }
    }

    /**
     * 长度格式化
     */
    private class LengthFormat implements CommonFormat {

        public Object format(Object value, BaseCodeRuleDetailSegment baseCodeRuleDetail, Map<String, String> parameters) {
            String _value = value.toString();
            if (baseCodeRuleDetail.getSectionLength() != null && baseCodeRuleDetail.getSectionLength() != 0 && _value.length() > baseCodeRuleDetail.getSectionLength()) {
                _value = _value.substring(0, baseCodeRuleDetail.getSectionLength());
            }
            return _value;
        }
    }
}


/**
 * 分隔符格式化
 */
class SeparatorFormat implements CommonFormat {

    public Object format(Object value, BaseCodeRuleDetailSegment baseCodeRuleDetail, Map<String, String> parameters) {
        return !Strings.isNullOrEmpty(baseCodeRuleDetail.getSectionSeparator()) ? value + baseCodeRuleDetail.getSectionSeparator() : value;
    }

}

