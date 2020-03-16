package com.zhongwang.cloud.platform.service.code.rule.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.zhongwang.cloud.platform.bamboo.common.exception.NotFoundException;
import com.zhongwang.cloud.platform.service.code.common.service.BaseService;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleDetail;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleDetail_;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule_;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleDetailRepository;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.zhongwang.cloud.platform.service.code.common.CodeConst.CODE_CACHE_HEADER_TITLE;
import static com.zhongwang.cloud.platform.service.code.common.CodeConst.SYS_COMMON_STATUS.NOT_DELETE;
import static com.zhongwang.cloud.platform.service.code.common.util.Strings.like;

@Service
public class CodeRuleService extends BaseService<CodeRule> {

    private final CodeRuleRepository codeRuleRepository;

    private final CodeRuleDetailRepository codeRuleDetailRepository;

    private final EntityManager entityManager;

    @Autowired
    public CodeRuleService(CodeRuleRepository codeRuleRepository, CodeRuleDetailRepository codeRuleDetailRepository, EntityManager entityManager) {
        this.codeRuleRepository = codeRuleRepository;
        this.codeRuleDetailRepository = codeRuleDetailRepository;
        this.entityManager = entityManager;
    }

    @Cacheable(value = CODE_CACHE_HEADER_TITLE, key = "'code-' + #root.targetClass + #pkid")
    public CodeRule findCodeRule(String pkid) {
        return this.codeRuleRepository.findOne(pkid);
    }

    @Cacheable(value = CODE_CACHE_HEADER_TITLE, key = "'code-' + #root.targetClass + #pkid")
    public CodeRule findValidateCodeRule(String pkid) {
//        CodeRule codeRule = codeRuleRepository.findOne((root, query, cb) -> {
//            Join<CodeRule, CodeRuleDetail> join = root.join(CodeRule_.codeRuleDetails, JoinType.LEFT);
//            List<Predicate> predicates = Lists.newArrayList();
//            predicates.add(cb.equal(root.get(CodeRule_.pkid), pkid));
//            predicates.add(cb.equal(root.get(CodeRule_.flagDelete), NOT_DELETE));
//            predicates.add(cb.equal(join.get(CodeRuleDetail_.flagDelete), NOT_DELETE));
//
//            query.orderBy(cb.asc(join.get(CodeRuleDetail_.flagSort)));
//            return cb.and(predicates.stream().toArray(Predicate[]::new));
//        });
        CodeRule codeRule = codeRuleRepository.findOne((root, query, cb) ->
                cb.and(cb.equal(root.get(CodeRule_.pkid), pkid), cb.equal(root.get(CodeRule_.flagDelete), NOT_DELETE)));
        if(null != codeRule) {
            entityManager.detach(codeRule);
        }
        return codeRule;
    }

    /**
     * 保存编码规则
     *
     * @param codeRule
     * @return
     */
    @CacheEvict(value = CODE_CACHE_HEADER_TITLE, key = "'code-' + #root.targetClass + #codeRule.pkid")
    public CodeRule insertCodeRule(CodeRule codeRule) {
        codeRule.setMakeUser(codeRule.getOperator());
        codeRule.setModifyUser(codeRule.getOperator());
        return this.insert(codeRule);
    }


    /**
     * 保存编码规则
     *
     * @param codeRule
     * @return
     */
    @CacheEvict(value = CODE_CACHE_HEADER_TITLE, key = "'code-' + #root.targetClass + #codeRule.pkid")
    public CodeRule updateCodeRule(CodeRule codeRule) throws NotFoundException {
        codeRule.setModifyUser(codeRule.getOperator());
        codeRuleDetailRepository.deleteByHeadPkid(codeRule.getPkid());
        return this.update(codeRule);
    }

    /**
     * 删除编码规则
     *
     * @param pkid
     * @param operator
     */
    @CacheEvict(value = CODE_CACHE_HEADER_TITLE, key = "'code-' + #root.targetClass + #pkid")
    public void deleteCodeRule(String pkid, String operator) throws NotFoundException {
        CodeRule codeRule = codeRuleRepository.findOne(pkid);
        if (null == codeRule) {
            throw new NotFoundException("can not found the code rule!");
        }
        this.deleteLogically(pkid, operator);
    }

    /**
     * 分页查询
     *
     * @param pageable
     * @param codeRule
     * @return
     */
    public Page<CodeRule> queryByPage(Pageable pageable, CodeRule codeRule) {
        return codeRuleRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            if (codeRule != null) {
                if (!Strings.isNullOrEmpty(codeRule.getRuleCode())) {
                    predicates.add(cb.like(root.get(CodeRule_.ruleCode), like(codeRule.getRuleCode())));
                }
                if (!Strings.isNullOrEmpty(codeRule.getRuleName())) {
                    predicates.add(cb.like(root.get(CodeRule_.ruleName), like(codeRule.getRuleName())));
                }
                if (null != codeRule.getEffectiveDate()) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get(CodeRule_.effectiveDate), codeRule.getEffectiveDate()));
                }
                if (null != codeRule.getExpirationDate()) {
                    predicates.add(cb.lessThanOrEqualTo(root.get(CodeRule_.expirationDate), codeRule.getExpirationDate()));
                }
                if (!Strings.isNullOrEmpty(codeRule.getSystemCode())) {
                    predicates.add(cb.equal(root.get(CodeRule_.systemCode), codeRule.getSystemCode()));
                }
                if (!Strings.isNullOrEmpty(codeRule.getModuleCode())) {
                    predicates.add(cb.equal(root.get(CodeRule_.moduleCode), codeRule.getModuleCode()));
                }
            }

            predicates.add(cb.equal(root.get(CodeRule_.flagDelete), NOT_DELETE));
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    /**
     * 根据条件查找数据
     *
     * @param pkid
     * @return
     */
    public CodeRule findByPkidAndFlagDelete(String pkid) throws NotFoundException {
        CodeRule codeRule = codeRuleRepository.findOne((root, query, cb) -> {
            // from
            ListJoin<CodeRule, CodeRuleDetail> join = root.join(CodeRule_.codeRuleDetails, JoinType.LEFT);
            // where
            List<Predicate> predicates = newArrayList();
            predicates.add(cb.equal(root.get(CodeRule_.flagDelete), NOT_DELETE));
            predicates.add(cb.equal(root.get(CodeRule_.pkid), pkid));
            predicates.add(cb.equal(join.get(CodeRuleDetail_.flagDelete), NOT_DELETE));
            return cb.and(predicates.toArray(new Predicate[0]));
        });
        if (null == codeRule) {
            throw new NotFoundException("Selected record does not exist!");
        }
        return codeRule;
    }

    /**
     * 根据条件查找数据
     *
     * @param systemCode
     * @param ruleCode
     * @return
     */
    public CodeRule findByRuleCodeAndFlagDelete(String systemCode, String ruleCode) throws NotFoundException {
        CodeRule codeRule = codeRuleRepository.findOne((root, query, cb) -> {
            // from
            ListJoin<CodeRule, CodeRuleDetail> join = root.join(CodeRule_.codeRuleDetails, JoinType.LEFT);
            // where
            List<Predicate> predicates = newArrayList();
            predicates.add(cb.equal(root.get(CodeRule_.flagDelete), NOT_DELETE));
            predicates.add(cb.equal(root.get(CodeRule_.systemCode), systemCode));
            predicates.add(cb.equal(root.get(CodeRule_.ruleCode), ruleCode));
            predicates.add(cb.equal(join.get(CodeRuleDetail_.flagDelete), NOT_DELETE));
            return cb.and(predicates.toArray(new Predicate[0]));
        });
        if (null == codeRule) {
            throw new NotFoundException("Selected record does not exist!");
        }
        return codeRule;
    }
}
