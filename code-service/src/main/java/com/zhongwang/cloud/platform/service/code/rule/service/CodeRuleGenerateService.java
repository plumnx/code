//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.zhongwang.cloud.platform.service.code.rule.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zhongwang.cloud.platform.bamboo.common.UUIDs;
import com.zhongwang.cloud.platform.bamboo.common.exception.AbstractException;
import com.zhongwang.cloud.platform.bamboo.common.exception.NotFoundException;
import com.zhongwang.cloud.platform.service.code.common.entity.BaseEntity;
import com.zhongwang.cloud.platform.service.code.config.BatchThreadPoolExecutorConfiguration;
import com.zhongwang.cloud.platform.service.code.config.bean.BatchThreadPool;
import com.zhongwang.cloud.platform.service.code.config.bean.CodeSerialPolicy;
import com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException;
import com.zhongwang.cloud.platform.service.code.exception.CodeProductException;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleDetail;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.*;
import com.zhongwang.cloud.platform.service.code.rule.lock.CodeSegmentGenerator;
import com.zhongwang.cloud.platform.service.code.rule.lock.RuleBuilder;
import com.zhongwang.cloud.platform.service.code.rule.lock.SegmentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 规则编码生成服务
 */
@Slf4j
@Service
@EnableConfigurationProperties({CodeSerialPolicy.class, BatchThreadPool.class})
public class CodeRuleGenerateService {

    private final CodeRuleQueryService codeRuleQueryService;

    private final CodeSerialPolicy codeSerialPolicy;

    private final List<GenerateSerialService> generateSerialServices;

    private final BatchThreadPoolExecutorConfiguration.ThreadPool threadPool;

    private final BatchThreadPool batchThreadPool;

    @Autowired
    public CodeRuleGenerateService(CodeRuleQueryService codeRuleQueryService, CodeSerialPolicy codeSerialPolicy, List<GenerateSerialService> generateSerialServices, BatchThreadPoolExecutorConfiguration.ThreadPool threadPool, BatchThreadPool batchThreadPool) {
        this.codeRuleQueryService = codeRuleQueryService;
        this.codeSerialPolicy = codeSerialPolicy;
        this.generateSerialServices = generateSerialServices;
        this.threadPool = threadPool;
        this.batchThreadPool = batchThreadPool;
    }

    /**
     * 生成编码（单个）
     *
     * @param codeQuery
     * @return
     * @throws AbstractException
     */
    public CodeResult generateCode(CodeQuery codeQuery) throws Exception {
        List<CodeResult> codeResults = this.generateCode(codeQuery, 1, false);
        if (null == codeResults || codeResults.isEmpty()) {
            throw new CodeProductException("product empty code, something error !");
        }
        return codeResults.get(0);
    }

    /**
     * 生成编码（样例）
     *
     * @param codeQuery
     * @return
     */
    public CodeResult generateSample(CodeQuery codeQuery) throws Exception {
        List<CodeResult> codeResults = this.generateCode(codeQuery, 1, true);
        if (null == codeResults || codeResults.isEmpty()) {
            throw new CodeProductException("product empty code, something error !");
        }
        return codeResults.get(0);
    }

    /**
     * 生成编码（批量）
     *
     * @param codeMultiplyQuery
     * @return
     * @throws AbstractException
     */
    public List<CodeResult> generateCode(CodeMultiplyQuery codeMultiplyQuery) throws Exception {
        return this.generateCode(codeMultiplyQuery, codeMultiplyQuery.getSize(), false);
    }


    /**
     * 生成编码（批量）
      * @param codeBatchQuery
     * @return
     */
    public Map<String, Object> generateCode(CodeBatchQuery codeBatchQuery) {
        Map<String, CodeRule> codeRuleCache = Maps.newHashMap();
        Map<String, CodeConstruct> codes = codeBatchQuery.getCodes();
        Map<String, Object> codeResults = Maps.newHashMap();
        for (String code : codes.keySet()) {
            collectCodeResult(codeBatchQuery, codeResults, codes, code, codeRuleCache);
        }
        return codeResults;
    }

    /**
     * 生成编码（分片）
     * @param codeBatchQuery
     * @return
     */
    public Map<String, Object> generateCodePartition(CodeBatchQuery codeBatchQuery) throws InterruptedException {
        if(batchThreadPool.getPartitionNum() <= 0) {
            return this.generateCode(codeBatchQuery);
        }
        // 线程非安全Map，避免 segment 锁，最大化减少线程争抢
        Map<String, CodeRule> codeRuleCache = new HashMap<>();
        Map<String, Object> codeResults = new ConcurrentHashMap<>();
        Map<String, CodeConstruct> codes = codeBatchQuery.getCodes();

        List<String> codeList = Lists.newArrayList(codes.keySet());
        List<List<String>> codePartitions = Lists.partition(codeList, batchThreadPool.getPartitionNum());
        CountDownLatch countDownLatch = new CountDownLatch(codePartitions.size());

        for(List<String> codePartition: codePartitions) {
            if (threadPool.getAlivedCount() > 1) {
                threadPool.execute(() -> {
                    for(String code: codePartition) {
                        collectCodeResult(codeBatchQuery, codeResults, codes, code, codeRuleCache);
                    }
                    countDownLatch.countDown();
                });
            } else {
                for(String code: codePartition) {
                    collectCodeResult(codeBatchQuery, codeResults, codes, code, codeRuleCache);
                }
                countDownLatch.countDown();
            }
        }

        countDownLatch.await(batchThreadPool.getAwaitTimeout(), TimeUnit.SECONDS);
        return codeResults;
    }

    /**
     * 收集编码处理结果
     * @param codeBatchQuery
     * @param codeResults
     * @param codes
     * @param code
     * @param codeRuleCache
     */
    private void collectCodeResult(CodeBatchQuery codeBatchQuery,
                                   Map<String, Object> codeResults,
                                   Map<String, CodeConstruct> codes,
                                   String code,
                                   Map<String, CodeRule> codeRuleCache) {
        try {
            CodeMultiplyQuery codeMultiplyQuery = new CodeMultiplyQuery(codeBatchQuery, codes.get(code));
            CodeRule codeRule = codeRuleCache.get(codeMultiplyQuery.getPkid());
            if(null == codeRule) {
                codeRule = codeRuleQueryService.selectCodeRuleAndCodeRuleDetailByPkid(codeMultiplyQuery.getPkid());
                if (null == codeRule) {
                    throw new NotFoundException("the code rule cannot found!");
                }
                codeRuleCache.put(codeMultiplyQuery.getPkid(), codeRule);
            }

            codeResults.put(code, this.generateCode(codeMultiplyQuery, codeMultiplyQuery.getSize(), false, codeRule));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            codeResults.put(code, e.getMessage());
        }
    }

    /**
     * 生成编码
     *
     * @param codeQuery
     * @param size
     * @return
     * @throws AbstractException
     */
    private List<CodeResult> generateCode(CodeQuery codeQuery, int size, boolean isSample) throws Exception {
        // 1. query rule and detail by rule.pkid
        CodeRule codeRule = codeRuleQueryService.selectCodeRuleAndCodeRuleDetailByPkid(codeQuery.getPkid());
        if (null == codeRule) {
            throw new NotFoundException("the code rule cannot found!");
        }
        return generateCode(codeQuery, size, isSample, codeRule);
    }

    /**
     * 生成编码
     * @param codeQuery
     * @param size
     * @param isSample
     * @param codeRule
     * @return
     * @throws Exception
     */
    private List<CodeResult> generateCode(CodeQuery codeQuery, int size, boolean isSample, CodeRule codeRule) throws Exception {
        List<CodeSegment> list = Lists.newArrayList();
        CodeRuleDetail serialCodeRuleDetail = null;
        RuleBuilder serialCodeBuilder = null;
        // 1.对 codeRule 中的 CodeRuleDetail 集合，按照升序的方式排序
        codeRule.getCodeRuleDetails().sort(Comparator.comparing(BaseEntity::getFlagSort));
        // 2.遍历 CodeRuleDetail 集合，将其转化为规则构造器 RuleBuilder
        // 2.1从 CodeRuleDetail 集合中提取出流水段，并赋值给 serialCodeRuleDetail 对象
        // 2.2从 CodeRuleDetail 集合中提取出流水段构造器，并赋值给 serialCodeBuilder 对象
        for (CodeRuleDetail codeRuleDetail : codeRule.getCodeRuleDetails()) {
            RuleBuilder codeBuilder = CodeSegmentGenerator.getRuleBuilder(codeRuleDetail, codeQuery.getParameters());
            if (SegmentType.SERIAL_VARIABLE.getType().toString().equals(codeRuleDetail.getSectionType())) {
                serialCodeRuleDetail = codeRuleDetail;
                serialCodeBuilder = codeBuilder;
            }
            list.add(codeBuilder.getCodeSegment());
        }

        // 3. 检查流水构造器是否被赋值，否则抛出异常，编码规则必须存在流水段。
        Integer serialNo = null;
        if (serialCodeBuilder != null) {
            // 3.1 根据流水段的格式设置初始值（流水段可以包含起始值，否则从0开始）
            int initSerialNo = Integer.valueOf(
                    Optional.ofNullable(serialCodeRuleDetail.getSectionValue()).orElse("0"));
            // 3.2 根据策略，从对应的流水生成服务生成编码，一共有：数据库、数据库 + 缓存（不适用了）、缓存
            for(GenerateSerialService generateSerialService : generateSerialServices) {
                if(generateSerialService.accept(isSample, codeSerialPolicy.getCodeSerialStrategy())) {
                    serialNo = generateSerialService.generate(codeQuery, initSerialNo, size, list);
                    break;
                }
            }
        }
        // 4.组合流水号和其余编码段
        return makeCodes(size, list, serialCodeBuilder, serialNo, codeQuery);
    }

    /**
     * 生成序列号
     *
     * @param size
     * @param list
     * @param serialCodeBuilder
     * @param serialNo
     * @param codeQuery
     * @return
     * @throws CodeInvalidParametersException
     */
    private List<CodeResult> makeCodes(final int size, final List<CodeSegment> list, RuleBuilder serialCodeBuilder, final Integer serialNo, final CodeQuery codeQuery)
            throws CodeInvalidParametersException {
        List<CodeResult> codes = Lists.newArrayList();
        String code;
        Map<String, String> segmentCodes;

        // product codes
        for (int i = 1; i < size + 1; ++i) {
            final String fSerialNo = this.generateSerialNo(serialCodeBuilder, serialNo, i);
            code = String.join("",
                    list.stream().
                            filter(CodeSegment::isShow).
                            map((codeSegment) -> codeSegment.isSerialVariableType() ? fSerialNo : Optional.ofNullable(codeSegment.getValue()).orElse("")).
                            collect(Collectors.toList()));
            if (codeQuery.getSegmentPkids() == null || codeQuery.getSegmentPkids().isEmpty()) {
                segmentCodes = null;
            } else {
                segmentCodes = list.stream().
                        filter(codeSegment -> codeQuery.getSegmentPkids().contains(codeSegment.getPkid())).
                        collect(Collectors.toMap(
                                CodeSegment::getPkid,
                                codeSegment -> codeSegment.isSerialVariableType() ? fSerialNo : Optional.ofNullable(codeSegment.getValue()).orElse("")));
            }
            codes.add(new CodeResult(code, segmentCodes));
        }
        return codes;
    }

    /**
     * 重置流水构造器
     *
     * @param ruleBuilder
     * @param num
     * @return
     * @throws CodeInvalidParametersException
     */
    private String generateSerialNo(RuleBuilder ruleBuilder, Integer num, Integer addNum) throws CodeInvalidParametersException {
        if(null != ruleBuilder && null != num && null != addNum) {
            ruleBuilder.updateValue(num + addNum);
            return ruleBuilder.getValue().toString();
        }
        return null;
    }

}
