package com.zhongwang.cloud.platform.service.code.rule.service;

import com.zhongwang.cloud.platform.service.code.exception.CodePersistentException;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleSerial;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

@Service
public class GenerateDBSerialUpdateService {

    private final CodeRuleSerialService codeRuleSerialService;

    @Autowired
    public GenerateDBSerialUpdateService(CodeRuleSerialService codeRuleSerialService) {
        this.codeRuleSerialService = codeRuleSerialService;
    }

    /**
     * 生成流水号到数据库
     *
     * @param codeQuery
     * @param serialUnionValueFormat
     * @param serialUnionValue
     * @param showUnionValueFormat
     * @param showUnionValue
     * @param size
     * @param initSerialNo
     * @return
     */
    @Transactional(value = REQUIRES_NEW, rollbackOn = Exception.class)
    public int makeSerialNoToDb(CodeQuery codeQuery,
                                String serialUnionValueFormat, String serialUnionValue, String showUnionValueFormat, String showUnionValue,
                                int size, int initSerialNo) throws CodePersistentException {
        CodeRuleSerial codeRuleSerial = this.codeRuleSerialService.findCodeRuleSerialByParamsForPessimistic(codeQuery.getPkid(), serialUnionValue, showUnionValue);
        if (null == codeRuleSerial) {
            codeRuleSerial = new CodeRuleSerial(codeQuery.getPkid(), serialUnionValueFormat, serialUnionValue, showUnionValueFormat, showUnionValue, initSerialNo + size);
        } else {
            initSerialNo = codeRuleSerial.getCodeMaxValue();
            codeRuleSerial.setSerialUnionValueFormat(serialUnionValueFormat);
            codeRuleSerial.setShowUnionValueFormat(showUnionValueFormat);
            codeRuleSerial.setCodeMaxValue(initSerialNo + size);
        }
        this.codeRuleSerialService.saveOrUpdateCodeMaxValue(codeRuleSerial.addProperties(codeQuery));
        return initSerialNo;
    }

}
