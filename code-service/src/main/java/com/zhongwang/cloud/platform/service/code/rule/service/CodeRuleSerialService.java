package com.zhongwang.cloud.platform.service.code.rule.service;

import com.google.common.base.Strings;
import com.zhongwang.cloud.platform.service.code.exception.CodePersistentException;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleSerial;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleSerial_;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleSerialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

import static com.zhongwang.cloud.platform.service.code.common.CodeConst.SYS_COMMON_STATUS.NOT_DELETE;
import static com.zhongwang.cloud.platform.service.code.exception.CodePersistentException.Error.DAO_RULE_SERIAL_SAVE_FAIL;

/**
 * 编码规则服务
 */
@Service
public class CodeRuleSerialService {

    private final EntityManager entityManager;

    private final CodeRuleSerialRepository codeRuleSerialRepository;

    @Autowired
    public CodeRuleSerialService(CodeRuleSerialRepository codeRuleSerialRepository, EntityManager entityManager) {
        this.codeRuleSerialRepository = codeRuleSerialRepository;
        this.entityManager = entityManager;
    }

    /**
     * 根据规则主键、编码显示值、编码隐藏值，索引规则流水记录
     *
     * @param headPkid
     * @param serialUnionValue
     * @param showUnionValue
     * @return
     * @throws CodePersistentException
     */
    public CodeRuleSerial findCodeRuleSerialByParams(String headPkid, String serialUnionValue, String showUnionValue) throws CodePersistentException {
        return this.codeRuleSerialRepository.findOne((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(CodeRuleSerial_.headPkid), headPkid));
            predicates.add(cb.equal(root.get(CodeRuleSerial_.serialUnionValue), serialUnionValue));
            predicates.add(cb.equal(root.get(CodeRuleSerial_.flagDelete), NOT_DELETE));

            if (!Strings.isNullOrEmpty(showUnionValue)) {
                predicates.add(cb.equal(root.get(CodeRuleSerial_.showUnionValue), showUnionValue));
            } else {
                predicates.add(cb.equal(root.get(CodeRuleSerial_.showUnionValue), ""));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    /**
     * 根据规则主键、编码显示值、编码隐藏值，索引规则流水记录，返回悲观锁记录
     *
     * @param headPkid
     * @param serialUnionValue
     * @param showUnionValue
     * @return
     */
    public CodeRuleSerial findCodeRuleSerialByParamsForPessimistic(String headPkid, String serialUnionValue, String showUnionValue) {
        String pkid = this.codeRuleSerialRepository.findPkidByParams(headPkid, serialUnionValue, showUnionValue, NOT_DELETE);
        if (null == pkid) {
            return null;
        }
        entityManager.getEntityManagerFactory().getCache().evict(CodeRuleSerial.class);
        return this.codeRuleSerialRepository.findByPkidForPessimistic(pkid);// 悲观锁
    }

    /**
     * 保存规则流水记录
     *
     * @param codeRuleSerial
     */
    public CodeRuleSerial saveCodeRuleSerial(CodeRuleSerial codeRuleSerial) {
        codeRuleSerial = this.codeRuleSerialRepository.saveAndFlush(codeRuleSerial);
        entityManager.detach(codeRuleSerial);
        entityManager.getEntityManagerFactory().getCache().evict(CodeRuleSerial.class);
        return codeRuleSerial;
    }

    public CodeRuleSerial saveOrUpdateCodeMaxValue(CodeRuleSerial codeRuleSerial) throws CodePersistentException {
        if(null == codeRuleSerial.getPkid()) {
            this.saveCodeRuleSerial(codeRuleSerial);
        } else {
            entityManager.detach(codeRuleSerial);
            entityManager.getEntityManagerFactory().getCache().evict(CodeRuleSerial.class);
            int count = this.codeRuleSerialRepository.updateCodeRuleSerialSetMaxValue(
                    codeRuleSerial.getPkid(), codeRuleSerial.getCodeMaxValue(),
                    codeRuleSerial.getSerialUnionValueFormat(), codeRuleSerial.getShowUnionValueFormat(),
                    codeRuleSerial.getFlagVersion() + 1, codeRuleSerial.getFlagVersion());
            if(count != 1) {
                throw new CodePersistentException(DAO_RULE_SERIAL_SAVE_FAIL);
            }
        }
        return codeRuleSerial;
    }

}
