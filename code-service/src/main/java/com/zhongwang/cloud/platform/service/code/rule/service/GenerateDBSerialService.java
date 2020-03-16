package com.zhongwang.cloud.platform.service.code.rule.service;

import com.zhongwang.cloud.platform.service.code.common.CodeConst.CodeSerialStrategy;
import com.zhongwang.cloud.platform.service.code.common.lock.DistributedLocker;
import com.zhongwang.cloud.platform.service.code.exception.CodePersistentException;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleSerial;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeQuery;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeSegment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

import static com.zhongwang.cloud.platform.service.code.common.util.Caches.CodeSerial.key;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

/**
 * 数据库序号生成器
 */
@Service
public class GenerateDBSerialService implements GenerateSerialService {

    private final DistributedLocker<Integer> distributedLocker;

    private final CodeRuleSerialService codeRuleSerialService;

    private final GenerateDBSerialUpdateService generateDBSerialUpdateService;

    @Autowired
    public GenerateDBSerialService(DistributedLocker<Integer> distributedLocker, CodeRuleSerialService codeRuleSerialService, GenerateDBSerialUpdateService generateDBSerialUpdateService) {
        this.distributedLocker = distributedLocker;
        this.codeRuleSerialService = codeRuleSerialService;
        this.generateDBSerialUpdateService = generateDBSerialUpdateService;
    }

    @Override
    public boolean accept(Boolean isSample, CodeSerialStrategy codeSerialStrategy) {
        if (!isSample) {
            return CodeSerialStrategy.DB.equals(codeSerialStrategy);
        }
        return false;
    }

    /**
     * 基于显示段格式、现实段值、隐藏段格式、隐藏段值的组合增加分布式锁，在同步块内进行乐观锁更新，生成新的流水到数据库
     * @param codeQuery （本次）流水请求对象
     * @param initSerialNo 初始化流水值
     * @param size 生成流水个数
     * @param list 流水段对象
     * @return
     * @throws Exception
     */
    @Override
    public int generate(CodeQuery codeQuery, int initSerialNo, int size, List<CodeSegment> list) throws Exception {
        return analysisCodeSegments(list, (serialUnionValueFormat, serialUnionValue, showUnionValueFormat, showUnionValue) -> {
            Integer result = distributedLocker.lockAndReturn(
                    () -> generateDBSerialUpdateService.makeSerialNoToDb(
                            codeQuery, serialUnionValueFormat, serialUnionValue, showUnionValueFormat, showUnionValue, size, initSerialNo),
                    key(codeQuery.getPkid(), serialUnionValue, showUnionValue)
            );
            if (result != null) {
                return result;
            }
            return 0;
        });
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
            codeRuleSerial = new CodeRuleSerial(codeQuery.getPkid(), serialUnionValueFormat, serialUnionValue, showUnionValueFormat, showUnionValue,
                    initSerialNo + size);
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
