package com.zhongwang.cloud.platform.service.code.api;

import com.zhongwang.cloud.platform.bamboo.common.exception.NotFoundException;
import com.zhongwang.cloud.platform.service.code.common.CodeConst;
import com.zhongwang.cloud.platform.service.code.common.CodeConst.SystemCode;
import com.zhongwang.cloud.platform.service.code.common.entity.BaseEntity;
import com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleDetail;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.RemoveData;
import com.zhongwang.cloud.platform.service.code.rule.lock.SegmentType;
import com.zhongwang.cloud.platform.service.code.rule.service.CodeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.zhongwang.cloud.platform.service.code.common.CodeConst.SYS_COMMON_STATUS.ORDER_SORT_DEFAULT;
import static com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException.Error.*;
import static com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException.check;
import static com.zhongwang.cloud.platform.service.code.rule.lock.SegmentType.isExist;
import static com.zhongwang.cloud.platform.service.code.rule.lock.SegmentType.of;
import static com.zhongwang.cloud.platform.service.code.rule.lock.SegmentType.valueOf;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping({"/rule"})
public class CodeRuleController {

    @Autowired
    private CodeRuleService codeRuleService;

    /**
     * 新增编码规则
     *
     * @return
     */
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public CodeRule addCodeRule(@Valid @RequestBody CodeRule codeRule, BindingResult bindingResult) throws
            CodeInvalidParametersException, IllegalAccessException, InstantiationException {
        if (bindingResult.hasErrors()) {
            throw new CodeInvalidParametersException(bindingResult.getFieldErrors());
        }
        validateCodeRuleDetail(codeRule);
        return codeRuleService.insertCodeRule(codeRule);
    }

    /**
     * 修改编码规则
     *
     * @return
     */
    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public void modifyCodeRule(@Valid @RequestBody CodeRule codeRule, BindingResult bindingResult) throws CodeInvalidParametersException, NotFoundException, IllegalAccessException, InstantiationException {
        if (bindingResult.hasErrors()) {
            throw new CodeInvalidParametersException(bindingResult.getFieldErrors());
        }
        validateCodeRuleDetail(codeRule);
        codeRuleService.updateCodeRule(codeRule);
    }

    /**
     * 编码规则请求对象结构验证
     * <p>
     * 规则类型支持：常量、系统变量、字段值、流水号
     * 1.  “常量”段不支持“段长度”、“补位方式”、“补位字符”、“日期格式”等格式化设置，所录入的段值即最终该段处理结果，并且段值项不能为空。
     * 2.  “系统变量”的“段值”来自于系统变量服务的编码值，支持“段长度” 、“补位方式”、“补位字符”、“日期格式”等格式化设置。该段值的处理过程是，通过将编码值在每次请求参数集合中翻译得到实际值，
     * 再通过一些格式化设置，得到最终（段）处理结果。
     * 3.  “字段值”同“系统变量”的处理方式，区别在于该段值可由用户进行自定义，而不是系统统一配置。
     * 4.  “流水号”段，不支持“段值”、“段长度”、“补位方式”、“补位字符”、“日期格式”等设置，并且在编码规则中只能存在一个。
     *
     * @param codeRule
     */
    private void validateCodeRuleDetail(CodeRule codeRule) throws CodeInvalidParametersException, InstantiationException, IllegalAccessException {
        check(codeRule != null, REQUEST_PARAMETERS_WRONG_OR_EMPTY);
        check(SystemCode.isExist(codeRule.getSystemCode()), RULE_SYSTEM_CODE_NOT_EXIST);
        List<CodeRuleDetail> codeRuleDetails = codeRule.getCodeRuleDetails();
        check((codeRuleDetails != null && !codeRuleDetails.isEmpty()), RULE_DETAIL_NOT_EMPTY);

        validateSerialSegment(codeRuleDetails);
        validateFlagSort(codeRuleDetails);

        for (CodeRuleDetail codeRuleDetail : codeRule.getCodeRuleDetails()) {
            check(isExist(codeRuleDetail.getSectionType()), RULE_DETAIL_SECTION_TYPE_NOT_CORRECT);
            of(codeRuleDetail.getSectionType()).validate(codeRuleDetail);
        }
    }

    /**
     * 验证流水段，唯一
     * @param codeRuleDetails
     * @throws CodeInvalidParametersException
     */
    private void validateSerialSegment(List<CodeRuleDetail> codeRuleDetails) throws CodeInvalidParametersException {
        check((codeRuleDetails != null && !codeRuleDetails.isEmpty()), RULE_DETAIL_SERIAL_ONLY_ONE);
        check(codeRuleDetails.stream().filter(
                codeRuleDetail -> SegmentType.SERIAL_VARIABLE.getType().toString().equals(codeRuleDetail.getSectionType())).count() == 1, RULE_DETAIL_SERIAL_ONLY_ONE);
    }

    /**
     * 排序号需要顺序排列不能重复，从1开始
     *
     * @param codeRuleDetails
     * @throws CodeInvalidParametersException
     */
    private void validateFlagSort(List<CodeRuleDetail> codeRuleDetails) throws CodeInvalidParametersException {
        List<Long> flagSorts = codeRuleDetails.stream().
                map(BaseEntity::getFlagSort).
                filter(Objects::nonNull).
                collect(Collectors.toList());

        HashSet<Long> flagSortSets = new HashSet<>(flagSorts);
        check(flagSortSets.size() == flagSorts.size(), RULE_DETAIL_ORDER_NOT_RIGHT);

        Long flagDeleteMax = flagSorts.stream().max(Long::compareTo).get();
        check(flagDeleteMax == flagSorts.size(), RULE_DETAIL_ORDER_NOT_RIGHT);

        Long flagDeleteMin = flagSorts.stream().min(Long::compareTo).get();
        check(flagDeleteMin == 1, RULE_DETAIL_ORDER_NOT_RIGHT);
    }

    /**
     * 删除编码规则
     *
     * @return
     */
    @DeleteMapping
    public void deleteCodeRule(@RequestBody RemoveData removeData) throws CodeInvalidParametersException, NotFoundException {
        if (removeData == null || removeData.getPkids() == null || removeData.getPkids().isEmpty()) {
            throw new CodeInvalidParametersException("parameters can not be empty!");
        }
        for (String pkid : removeData.getPkids()) {
            codeRuleService.deleteCodeRule(pkid, removeData.getOperator());
        }
    }

    /**
     * 根据检索条件查询消息（带分页信息）
     *
     * @param pageable        分页
     * @param rule_code       规则编码
     * @param rule_name       规则名称
     * @param effective_date  生效日期
     * @param expiration_date 失效日期
     * @param system_code     系统编码
     * @param module_code     模块编码
     * @return 分页对象
     */
    @GetMapping
    public Page<CodeRule> queryByPage(
            @PageableDefault(sort = ORDER_SORT_DEFAULT, direction = DESC) Pageable pageable,
            String rule_code, String rule_name, String effective_date, String expiration_date, String system_code, String module_code) throws ParseException {
        return codeRuleService.queryByPage(pageable,
                new CodeRule(rule_code, rule_name, effective_date, expiration_date, system_code, module_code));
    }

    /**
     * 查看明细
     *
     * @param pkid 系统主键
     * @return CodeRule
     */
    @GetMapping("/{id}")
    public CodeRule queryByPkid(@PathVariable("id") String pkid) throws NotFoundException {
        return codeRuleService.findByPkidAndFlagDelete(pkid);
    }

    /**
     * 查看明细
     *
     * @param ruleCode 系统编码
     * @return CodeRule
     */
    @GetMapping("/action/query-by-code/{systemCode}/{ruleCode}")
    public CodeRule queryByCode(@PathVariable("systemCode") String systemCode, @PathVariable("ruleCode") String ruleCode) throws NotFoundException {
        return codeRuleService.findByRuleCodeAndFlagDelete(systemCode, ruleCode);
    }

}
