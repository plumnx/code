package com.zhongwang.cloud.platform.service.code.rule.service;

import com.zhongwang.cloud.platform.bamboo.common.exception.NotFoundException;
import com.zhongwang.cloud.platform.service.code.common.CodeConst;
import com.zhongwang.cloud.platform.service.code.config.bean.CodeSerialPolicy;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule_;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.zhongwang.cloud.platform.service.code.common.CodeConst.SYS_COMMON_STATUS.NOT_DELETE;
import static com.zhongwang.cloud.platform.service.code.rule.entity.CodeRule_.*;

@Service
@EnableConfigurationProperties({CodeSerialPolicy.class})
public class CodeRuleQueryService {

    @Autowired
    private CodeRuleService codeRuleService;

    @Autowired
    private CodeRuleRepository codeRuleRepository;

    @Autowired
    private CodeSerialPolicy codeSerialPolicy;

    /**
     * 根据主键查找规则及明细对象
     *
     * @param pkid
     * @return
     */
    public CodeRule selectCodeRuleAndCodeRuleDetailByPkid(String pkid) throws NotFoundException {
//        return this.selectCodeRuleAndCodeRuleDetailByPkid(pkid, new Date());
        return selectCodeRuleFromDbOrCacheThenValidateIt(pkid, new Date());
    }

    /**
     * 根据主键查找规则及明细对象，优先从缓存读取s
     * @param pkid
     * @param date
     * @return
     * @throws NotFoundException
     */
    public CodeRule selectCodeRuleFromDbOrCacheThenValidateIt(String pkid, Date date) throws NotFoundException {
        CodeRule codeRule = codeRuleService.findValidateCodeRule(pkid);

        if(codeRule == null ||
                codeRule.getFlagDelete().shortValue() == CodeConst.SYS_COMMON_STATUS.DELETE ||
                codeRule.getEffectiveDate().compareTo(date) > 0 ||
                codeRule.getExpirationDate().compareTo(date) < 0) {
            throw new NotFoundException("the code rule need request pkid!");
        }
        return codeRule;
    }

    /**
     * 根据主键查找规则及明细对象
     *
     * @param pkid
     * @return
     */
    public CodeRule selectCodeRuleAndCodeRuleDetailByPkid(String pkid, Date date) throws NotFoundException {
        if (null == pkid) {
            throw new NotFoundException("the code rule need request pkid!");
        }
        return codeRuleRepository.findOne((root, query, cb) -> query.where(
                cb.and(cb.equal(root.get(CodeRule_.pkid), pkid),
                        cb.equal(root.get(flagDelete), NOT_DELETE),
                        cb.lessThanOrEqualTo(root.get(effectiveDate), date),
                        cb.greaterThanOrEqualTo(root.get(expirationDate), date))
        ).getRestriction());
    }

}
