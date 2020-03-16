//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.zhongwang.cloud.platform.service.code.api;

import com.google.common.collect.Maps;
import com.zhongwang.cloud.platform.bamboo.common.exception.AbstractException;
import com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.*;
import com.zhongwang.cloud.platform.service.code.rule.service.CodeRuleGenerateService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 接口类
 */
@RestController
@RequestMapping({"/code"})
@Slf4j
public class CodeController {

    private final CodeRuleGenerateService codeRuleGenerateService;

    @Autowired
    public CodeController(CodeRuleGenerateService codeRuleGenerateService) {
        this.codeRuleGenerateService = codeRuleGenerateService;
    }

    /**
     * 生成编码（单个）
     *
     * @param codeQuery
     * @return
     * @throws AbstractException
     */
    @ApiOperation(value = "生成编码（单个）", notes = "根据编码配置主键及相关信息生成编码", produces = "application/json")
    @PostMapping
    public CodeResult generate(@Valid @RequestBody CodeQuery codeQuery, BindingResult bindingResult) throws Exception {
        log.debug("codeQuery:" + codeQuery.toString());
        if (bindingResult.hasErrors()) {
            throw new CodeInvalidParametersException(bindingResult.getFieldErrors().toString());
        }

        CodeResult codeResult = this.codeRuleGenerateService.generateCode(codeQuery);
        log.debug("codeResult:" + codeResult.toString());
        return codeResult;
    }

    /**
     * 生成编码（样例）
     *
     * @param codeQuery
     * @return
     */
    @ApiOperation(value = "生成编码（样例）", notes = "仅用作产生测试样例时使用，生成结果不影响实际存储", produces = "application/json")
    @PostMapping("/action/sample")
    public CodeResult generateSample(@Valid @RequestBody CodeQuery codeQuery, BindingResult bindingResult) throws Exception {
        log.debug("codeQuery:" + codeQuery.toString());
        if (bindingResult.hasErrors()) {
            throw new CodeInvalidParametersException(bindingResult.getFieldErrors().toString());
        }
        CodeResult codeResult = this.codeRuleGenerateService.generateSample(codeQuery);
        log.debug("codeResult:" + codeResult.toString());
        return codeResult;
    }

    /**
     * 生成编码（批量）
     *
     * @param codeMultiplyQuery
     * @return
     * @throws AbstractException
     */
    @ApiOperation(value = "生成编码（批量）", notes = "根据编码配置主键及相关信息批量生成编码", produces = "application/json")
    @PostMapping({"/action/multiply"})
    public List<CodeResult> generateMultiply(@Valid @RequestBody CodeMultiplyQuery codeMultiplyQuery, BindingResult bindingResult) throws Exception {
        log.debug("codeMultiplyQuery:" + codeMultiplyQuery.toString());
        if (bindingResult.hasErrors()) {
            throw new CodeInvalidParametersException(bindingResult.getFieldErrors().toString());
        }

        List<CodeResult> codeResults = this.codeRuleGenerateService.generateCode(codeMultiplyQuery);
        log.debug("codeResult:" + codeResults.toString());
        return codeResults;
    }

    /**
     * 生成编码（批量）
     *
     * @param codeBatchQuery
     * @param bindingResult
     * @return
     * @throws Exception
     */
    @PostMapping({"/action/batch"})
    public Map<String, Object> generateBatch(
            @Valid @RequestBody CodeBatchQuery codeBatchQuery, BindingResult bindingResult) throws Exception {
//        log.debug("codeBatchQuery:" + codeBatchQuery.toString());
        if (bindingResult.hasErrors()) {
            throw new CodeInvalidParametersException(bindingResult.getFieldErrors().toString());
        }

        return codeRuleGenerateService.generateCodePartition(codeBatchQuery);
    }

    /**
     * 生成编码（批量）
     *
     * @param codeBatchQuery
     * @param bindingResult
     * @return
     * @throws Exception
     */
    @PostMapping({"/action/batch/serial"})
    public Map<String, Object> generateBatch_serial(
            @Valid @RequestBody CodeBatchQuery codeBatchQuery, BindingResult bindingResult) throws Exception {
//        log.debug("codeBatchQuery:" + c odeBatchQuery.toString());
        if (bindingResult.hasErrors()) {
            throw new CodeInvalidParametersException(bindingResult.getFieldErrors().toString());
        }

//        Map<String, CodeConstruct> codes = codeBatchQuery.getCodes();
//        Map<String, Object> codeResults = Maps.newHashMap();
//        for (String code : codes.keySet()) {
//            collectCodeResult(codeBatchQuery, codeResults, code, codes.get(code));
//        }
//
//        return codeResults;
        return codeRuleGenerateService.generateCode(codeBatchQuery);
    }

}
