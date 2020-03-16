package com.zhongwang.cloud.platform.service.code.rule.lock;

import com.zhongwang.cloud.platform.bamboo.common.exception.AbstractException;
import com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException;
import com.zhongwang.cloud.platform.service.code.exception.CodeRuleBuilderException;
import com.zhongwang.cloud.platform.service.code.rule.entity.base.BaseCodeRuleDetailSegment;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeSegment;

import java.util.Map;

/**
 * 编码段构造器
 */
public class CodeSegmentGenerator {

    /**
     * 构造编码段
     * @param baseCodeRuleDetail
     * @param parameters
     * @return
     * @throws AbstractException
     */
    public static CodeSegment generator(BaseCodeRuleDetailSegment baseCodeRuleDetail, Map<String, String> parameters)
            throws CodeRuleBuilderException, CodeInvalidParametersException, IllegalAccessException, InstantiationException {
        return getRuleBuilder(baseCodeRuleDetail, parameters).getCodeSegment();
    }

    /**
     * 编码段构造器对象
     * @param baseCodeRuleDetail
     * @param parameters
     * @return
     * @throws AbstractException
     */
    public static RuleBuilder getRuleBuilder(BaseCodeRuleDetailSegment baseCodeRuleDetail, Map<String, String> parameters)
            throws CodeRuleBuilderException, InstantiationException, IllegalAccessException {
        RuleBuilder ruleBuilder = SegmentType.of(Integer.valueOf(baseCodeRuleDetail.getSectionType())).getRuleBuilder();
        ruleBuilder.initValue(baseCodeRuleDetail, parameters);
        return ruleBuilder;
    }

}
