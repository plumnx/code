package com.zhongwang.cloud.platform.service.code.rule.service;

import com.zhongwang.cloud.platform.service.code.common.CodeConst.CodeSerialStrategy;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeQuery;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeSegment;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 样例序号生成器
 */
@Service
public class GenerateSampleSerialService implements GenerateSerialService {

    @Override
    public boolean accept(Boolean isSample, CodeSerialStrategy codeSerialStrategy) {
        return isSample;
    }

    @Override
    public int generate(CodeQuery codeQuery, int initSerialNo, int size, List<CodeSegment> list) {
        return 0;
    }
}
