package com.zhongwang.cloud.platform.service.code.rule.service;

import com.zhongwang.cloud.platform.service.code.common.CodeConst.CodeSerialStrategy;
import com.zhongwang.cloud.platform.service.code.exception.CodePersistentException;
import com.zhongwang.cloud.platform.service.code.rule.entity.method.DoMethod;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeQuery;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeSegment;
import org.apache.logging.log4j.util.Strings;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.zhongwang.cloud.platform.service.code.common.CodeConst.RULE_TAG.EMPTY_SHOW_UNION_VALUE;
import static com.zhongwang.cloud.platform.service.code.common.CodeConst.RULE_TAG.EMPTY_SHOW_UNION_VALUE_FORMAT;

/**
 * 流水号生成服务
 */
public interface GenerateSerialService {

    /**
     * 验证当前序号生成器是否满足要求
     * @param isSample
     * @param codeSerialStrategy
     * @return
     */
    boolean accept(Boolean isSample, CodeSerialStrategy codeSerialStrategy);

    /**
     * 生成最新流水号
     * @param codeQuery （本次）流水请求对象
     * @param initSerialNo 初始化流水值
     * @param size 生成流水个数
     * @param list 流水段对象
     * @return
     */
    int generate(CodeQuery codeQuery, int initSerialNo, int size, List<CodeSegment> list) throws Exception;

    /**
     * 分析编码段
     * @param list
     * @param doMethod
     * @param <T>
     * @return
     * @throws Exception
     */
    default <T> T analysisCodeSegments(List<CodeSegment> list, DoMethod<T> doMethod) throws Exception {
        String serialUnionValueFormat = String.join(",", list.stream().filter(CodeSegment::isShow).map(CodeSegment::getName).collect(Collectors.toList()));
        String serialUnionValue = String.join("", list.stream().filter(CodeSegment::isShow).map(CodeSegment::getSerialUnionValue).collect(Collectors.toList()));

        String showUnionValueFormat = String.join(",", list.stream().filter(CodeSegment::isShowUnionValue).map(CodeSegment::getName).collect(Collectors.toList()));
        showUnionValueFormat = Optional.of(showUnionValueFormat).filter(Strings::isNotEmpty).orElse(EMPTY_SHOW_UNION_VALUE_FORMAT);

        String showUnionValue = String.join(",", list.stream().filter(CodeSegment::isShowUnionValue).map(CodeSegment::getValue).collect(Collectors.toList()));
        showUnionValue = Optional.of(showUnionValue).filter(Strings::isNotEmpty).orElse(EMPTY_SHOW_UNION_VALUE);

        return doMethod.method(serialUnionValueFormat, serialUnionValue, showUnionValueFormat, showUnionValue);
    }

}
