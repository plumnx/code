package com.zhongwang.cloud.platform.service.code.rule.lock;

import com.google.common.collect.Lists;
import com.zhongwang.cloud.platform.bamboo.common.exception.AbstractException;
import com.zhongwang.cloud.platform.service.code.common.CodeConst;
import com.zhongwang.cloud.platform.service.code.common.CodeConst.IsSerial;
import com.zhongwang.cloud.platform.service.code.common.CodeConst.IsShow;
import com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException;
import com.zhongwang.cloud.platform.service.code.exception.CodeRuleBuilderException;
import com.zhongwang.cloud.platform.service.code.rule.entity.base.BaseCodeRuleDetailSegment;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeSegment;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 规则段构造器
 */
public interface RuleBuilder {

    /**
     * 类型
     * @return
     */
    SegmentType type();

    /**
     * 初始化
     * @param baseCodeRuleDetailSegment
     * @param parameters
     * @throws CodeRuleBuilderException
     */
    void initValue(BaseCodeRuleDetailSegment baseCodeRuleDetailSegment, Map<String, String> parameters) throws CodeRuleBuilderException;

    /**
     * 更新段值
     * @param value
     */
    void updateValue(Object value);

    /**
     * 获取格式化工具集
     *
     * @return
     */
    List<CommonFormat> getFormats();

    /**
     * 获取编码段
     * @return
     * @throws AbstractException
     */
    CodeSegment getCodeSegment() throws CodeInvalidParametersException;

    /**
     * 获取段值
     * @return
     * @throws CodeInvalidParametersException
     */
    Object getValue() throws CodeInvalidParametersException;

}

/**
 * （公共）段构造器
 *  所有构造器实现的父类，提供基础实现，子类可覆写
 */
abstract class CommonRuleBuilder implements RuleBuilder {

    Object value;
    private BaseCodeRuleDetailSegment baseCodeRuleDetail;
    private Map<String, String> parameters;

    @Override
    public void initValue(BaseCodeRuleDetailSegment baseCodeRuleDetail, Map<String, String> parameters)
            throws CodeRuleBuilderException {
        this.baseCodeRuleDetail = baseCodeRuleDetail;
        this.parameters = parameters;
    }

    @Override
    public void updateValue(Object value) {
        this.value = value;
    }

    /**
     * 开始构造
     * 获取子类支持的所有格式化类型，对段值进行格式化操作
     * @throws CodeInvalidParametersException
     */
    private void build() throws CodeInvalidParametersException {
        CommonFormat commonFormat;
        for(Iterator iter = this.getFormats().iterator(); iter.hasNext();) {
            commonFormat = (CommonFormat)iter.next();
            this.value = commonFormat.format(this.value, this.baseCodeRuleDetail, this.parameters);
        }

    }

    /**
     * 返回处理后的代码段对象
     * @return
     * @throws CodeInvalidParametersException
     */
    @Override
    public CodeSegment getCodeSegment() throws CodeInvalidParametersException {
        this.build();
        return new CodeSegment(
                IsShow.YES.getValue() == this.baseCodeRuleDetail.getFlagShow(),
                IsSerial.YES.getValue() == this.baseCodeRuleDetail.getFlagSerial(),
                this.type(), this.value.toString(),
                this.baseCodeRuleDetail.getPkid());
    }

    /**
     * 返回处理后的段值
     * @return
     * @throws CodeInvalidParametersException
     */
    @Override
    public Object getValue() throws CodeInvalidParametersException {
        this.build();
        return this.value.toString();
    }
}

/**
 * 常量构造器
 */
class ConstBuilder extends CommonRuleBuilder {

    private static final List<CommonFormat> commonFormats = Lists.newArrayList(new SeparatorFormat());

    /**
     * 常量构造器仅支持分隔符格式化
     * @return
     */
    @Override
    public List<CommonFormat> getFormats() {
        return commonFormats;
    }

    @Override
    public SegmentType type() {
        return SegmentType.CONST_VARIABLE;
    }

    @Override
    public void initValue(BaseCodeRuleDetailSegment baseCodeRuleDetail, Map<String, String> parameters)
            throws CodeRuleBuilderException {
        super.initValue(baseCodeRuleDetail, parameters);
        this.value = baseCodeRuleDetail.getSectionValue();
    }

}

/**
 * 变量构造器
 */
class VariableBuilder extends CommonRuleBuilder {

    private static final List<CommonFormat> commonFormats = Lists.newArrayList(new DateFormat(), new TextFormat(),
            new SeparatorFormat());

    /**
     * 变量构造器，支持日期、文本、分隔符格式化
     * @return
     */
    @Override
    public List<CommonFormat> getFormats() {
        return commonFormats;
    }

    @Override
    public SegmentType type() {
        return SegmentType.SYS_VARIABLE;
    }

    @Override
    public void initValue(BaseCodeRuleDetailSegment baseCodeRuleDetail, Map<String, String> parameters)
            throws CodeRuleBuilderException {
        super.initValue(baseCodeRuleDetail, parameters);
        this.value = parameters.get(baseCodeRuleDetail.getSectionValue());
        if (null == this.value) {
            throw new CodeRuleBuilderException(CodeRuleBuilderException.Error.NOT_FOUND_RULE_VALUE);
        }
    }

}

/**
 * 字段构造器
 */
class FieldBuilder extends CommonRuleBuilder {

    private static final List<CommonFormat> commonFormats = Lists.newArrayList(
            new DateFormat(), new TextFormat(), new SeparatorFormat());

    /**
     * 字段构造器，支持日期、文本、分隔符格式化
     * @return
     */
    @Override
    public List<CommonFormat> getFormats() {
        return commonFormats;
    }

    @Override
    public SegmentType type() {
        return SegmentType.FIELD_VARIABLE;
    }

    @Override
    public void initValue(BaseCodeRuleDetailSegment baseCodeRuleDetail, Map<String, String> parameters)
            throws CodeRuleBuilderException {
        super.initValue(baseCodeRuleDetail, parameters);
        this.value = parameters.get(baseCodeRuleDetail.getSectionValue());
        if (null == this.value) {
            throw new CodeRuleBuilderException(CodeRuleBuilderException.Error.NOT_FOUND_RULE_VALUE);
        }
    }

}

/**
 * 流水号构造器
 */
class SerialBuilder extends CommonRuleBuilder {

    private static final List<CommonFormat> commonFormats = Lists.newArrayList(
            new TextFormat(), new SeparatorFormat());

    /**
     * 流水构造器，支持文本、分隔符格式化
     * @return
     */
    @Override
    public List<CommonFormat> getFormats() {
        return commonFormats;
    }

    @Override
    public SegmentType type() {
        return SegmentType.SERIAL_VARIABLE;
    }

    @Override
    public void initValue(BaseCodeRuleDetailSegment baseCodeRuleDetail, Map<String, String> parameters)
            throws CodeRuleBuilderException {
        super.initValue(baseCodeRuleDetail, parameters);
        this.value = baseCodeRuleDetail.getSectionValue();
        if(this.value == null) {
            this.value = 0;
        }
    }
}