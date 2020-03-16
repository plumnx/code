package com.zhongwang.cloud.platform.service.code.rule.service;

import com.zhongwang.cloud.platform.service.code.common.CodeConst.CodeSerialStrategy;
import com.zhongwang.cloud.platform.service.code.common.lock.DistributedLocker;
import com.zhongwang.cloud.platform.service.code.config.bean.CodeSerialPolicy;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleSerial;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeQuery;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.zhongwang.cloud.platform.service.code.common.util.Caches.CodeSerial.*;
import static org.apache.commons.collections.CollectionUtils.collect;

/**
 * 缓存序号生成器
 */
@Slf4j
@Service
@EnableConfigurationProperties({CodeSerialPolicy.class})
@ConditionalOnExpression("'${code.config.serial.policy.strategy}' == 'CACHE' || '${code.config.serial.policy.strategy}' == 'DB_CACHE'")
public class GenerateCacheSerialService implements GenerateSerialService {

    private final RedisTemplate redisTemplate;

    private final CodeRuleSerialService codeRuleSerialService;

    private final ThreadPoolTaskExecutor taskExecutor;

    private final RedisScript<Integer[]> fetchCodeRuleSerialNoScript;

    private final CodeSerialPolicy codeSerialPolicy;

    private final DistributedLocker distributedLocker;

    @Value("${spring.redis.prefix:${spring.application.name}}:")
    private String prefix;

    @Autowired(required = false)
    public GenerateCacheSerialService(RedisTemplate redisTemplate, CodeRuleSerialService codeRuleSerialService, ThreadPoolTaskExecutor taskExecutor, RedisScript<Integer[]> fetchCodeRuleSerialNoScript, CodeSerialPolicy codeSerialPolicy, DistributedLocker distributedLocker) {
        this.redisTemplate = redisTemplate;
        this.codeRuleSerialService = codeRuleSerialService;
        this.taskExecutor = taskExecutor;
        this.fetchCodeRuleSerialNoScript = fetchCodeRuleSerialNoScript;
        this.codeSerialPolicy = codeSerialPolicy;
        this.distributedLocker = distributedLocker;
    }

    @Override
    public boolean accept(Boolean isSample, CodeSerialStrategy codeSerialStrategy) {
        if (!isSample) {
            return CodeSerialStrategy.CACHE.equals(codeSerialStrategy);
        }
        return false;
    }

    /**
     * 生成流水号（从cache中获取最新流水号，加1（或size）后，保存到cache中并定期同步数据库）
     *
     * @param codeQuery
     * @param initSerialNo
     * @param size
     * @param list
     * @return
     */
    @Override
    public int generate(final CodeQuery codeQuery, final int initSerialNo, final int size, final List<CodeSegment> list) throws Exception {
        return analysisCodeSegments(list, (serialUnionValueFormat, serialUnionValue, showUnionValueFormat, showUnionValue) -> {
            String key = key(serialUnionValue, showUnionValue);
            List<Integer> parameters = this.fetchSerialNoFromCache(
                    forCurrentHash(codeQuery.getPkid()), forCurrentHashKey(key), forLimitHashKey(key), size);

            if (parameters != null && parameters.get(0) == 1) {
                this.asyncExtensiveSerialMaxRange(
                        codeQuery, serialUnionValueFormat, serialUnionValue, showUnionValueFormat, showUnionValue,
                        initSerialNo, size, parameters);
                return parameters.get(1);
            } else {
                this.extensiveSerialMaxRange(
                        codeQuery, serialUnionValueFormat, serialUnionValue, showUnionValueFormat, showUnionValue,
                        initSerialNo, size);
            }
            return generate(codeQuery, initSerialNo, size, list);
        });
    }

    /**
     * 从缓存中获取当前流水号和流水上限
     *
     * @param hashName
     * @param currentHashKey
     * @param limitHashKey
     * @param fetchSize
     * @return [
     * 状态标记：1.成功；0，失败，
     * 当前流水号
     * 最大流水号，与数据库的当前流水号一致
     * ]
     */
    public List<Integer> fetchSerialNoFromCache(final String hashName, final String currentHashKey, final String limitHashKey, final int fetchSize) {
        Object evalValue = redisTemplate.execute(
                fetchCodeRuleSerialNoScript, newArrayList(hashName, currentHashKey, limitHashKey), fetchSize);
        if (evalValue == null) {
            return null;
        }
        return (List<Integer>) collect((Collection) evalValue, input -> Integer.valueOf(input.toString()));
    }

    /**
     * 扩展流水上限（数据库和缓存）
     * 1.如果缓存不存在（cacheParameters = null），则进行流水初始化操作
     * 1.1 如果数据库也不存在，整体初始化
     * 1.2 如果数据库存在，则根据数据库上限进行初始化
     * <p>
     * 2.如果缓存记录存在，则验证缓存记录是否符合扩展需要
     * 2.1 符合，则提升流水上限
     * 2.2 不符合，则跳过返回
     * 3.如果当前编码规则正在处理，则线程直接放过自旋，同步段内生成redis缓存，调用端需要循环调用。
     *
     * @param codeQuery
     * @param serialUnionValueFormat
     * @param serialUnionValue
     * @param showUnionValueFormat
     * @param showUnionValue
     * @param initSerialNo
     */
    public void extensiveSerialMaxRange(
            final CodeQuery codeQuery,
            final String serialUnionValueFormat, final String serialUnionValue, final String showUnionValueFormat, final String showUnionValue,
            int initSerialNo, final int fetchSize) throws Exception {
        // initial cache
        if (!distributedLocker.isLocked(key(codeQuery.getPkid(), serialUnionValue, showUnionValue))) {
            distributedLocker.lock(() -> {
                String key = key(serialUnionValue, showUnionValue);

                final Object currentSerialNo = redisTemplate.opsForHash().get(forCurrentHash(codeQuery.getPkid()), prefix + forCurrentHashKey(key));
                final Object limitSerialNo = redisTemplate.opsForHash().get(forCurrentHash(codeQuery.getPkid()), prefix + forLimitHashKey(key));
                CodeRuleSerial codeRuleSerial = this.codeRuleSerialService.findCodeRuleSerialByParams(codeQuery.getPkid(), serialUnionValue, showUnionValue);
                if (null == currentSerialNo || null == limitSerialNo) {
                    if (null == codeRuleSerial) {
                        // db and cache doesn't have serial no
                        int codeMaxValue = calcLimitSerialNo(0, 0, fetchSize);

                        codeRuleSerial = new CodeRuleSerial(
                                codeQuery.getPkid(), serialUnionValueFormat, serialUnionValue, showUnionValueFormat, showUnionValue, codeMaxValue);
                        this.codeRuleSerialService.
                                saveCodeRuleSerial(codeRuleSerial.addProperties(codeQuery));

                        redisTemplate.opsForHash().put(forCurrentHash(codeQuery.getPkid()), prefix + forCurrentHashKey(key), initSerialNo);
                        redisTemplate.opsForHash().put(forCurrentHash(codeQuery.getPkid()), prefix + forLimitHashKey(key), codeMaxValue);
                    } else {
                        // db has serial no, but cache doesn't have
                        int currentSerialNoInCache = codeRuleSerial.getCodeMaxValue();
                        int codeMaxValue = calcLimitSerialNo(currentSerialNoInCache, 0, fetchSize);
                        codeRuleSerial.setCodeMaxValue(codeMaxValue);
                        codeRuleSerial.setSerialUnionValueFormat(serialUnionValueFormat);
                        codeRuleSerial.setShowUnionValueFormat(showUnionValueFormat);
                        this.codeRuleSerialService.
                                saveCodeRuleSerial(codeRuleSerial.addProperties(codeQuery));

                        redisTemplate.opsForHash().put(forCurrentHash(codeQuery.getPkid()), prefix + forCurrentHashKey(key), currentSerialNoInCache);
                        redisTemplate.opsForHash().put(forCurrentHash(codeQuery.getPkid()), prefix + forLimitHashKey(key), codeMaxValue);
                    }
                } else {
                    // db and cache have cache, but need to calc
                    final int currentSerialNoInt = (int) currentSerialNo;
                    final int limitSerialNoInt = (int) limitSerialNo;
                    if (null != codeRuleSerial.getCodeMaxValue()) {
                        int codeMaxValue = calcLimitSerialNo(currentSerialNoInt, limitSerialNoInt, fetchSize);
                        if (codeMaxValue > limitSerialNoInt) {
                            codeRuleSerial.setHeadPkid(codeQuery.getPkid());
                            codeRuleSerial.setSerialUnionValueFormat(serialUnionValueFormat);
                            codeRuleSerial.setSerialUnionValue(serialUnionValue);
                            codeRuleSerial.setShowUnionValueFormat(showUnionValueFormat);
                            codeRuleSerial.setShowUnionValue(showUnionValue);
                            codeRuleSerial.setCodeMaxValue(codeMaxValue);
                            this.codeRuleSerialService.
                                    saveCodeRuleSerial(codeRuleSerial.addProperties(codeQuery));

                            redisTemplate.opsForHash().put(forCurrentHash(codeQuery.getPkid()), prefix + forLimitHashKey(key), codeMaxValue);
                        }
                    }
                }
            }, key(codeQuery.getPkid(), serialUnionValue, showUnionValue));
        }
    }

    /**
     * 计算流水最大值：
     * 1：
     * 流水上限 < 当前流水号 + 本次提取流水个数，则：提高流水上限到下个自增上限的整数倍，并且刚好大于当前流水号与本次提取流水个数之和
     * 2：
     * 当前流水号 + 本次提取流水个数 > 与流水上限 - 流水边界，则提高流水上限到下个自增上限的整数倍
     *
     * @param currentSerialNo 当前值
     * @param limitSerialNo   当前值上限
     * @param fetchSize       基于当前值，本次还需获取的数量
     * @return
     */
    private int calcLimitSerialNo(int currentSerialNo, int limitSerialNo, int fetchSize) {
        int newLimitSerialNo = limitSerialNo;
        if (currentSerialNo + fetchSize >= newLimitSerialNo) {
            newLimitSerialNo = ((currentSerialNo + fetchSize) / codeSerialPolicy.getIncreateLimitNum() + 1) * codeSerialPolicy.getIncreateLimitNum();
        }
        if ((currentSerialNo + fetchSize + codeSerialPolicy.getDiffNum()) >= newLimitSerialNo) {
            newLimitSerialNo = newLimitSerialNo + codeSerialPolicy.getIncreateLimitNum();
        }
        return newLimitSerialNo;
    }

    /**
     * 异步扩展流水上限（数据库和缓存）
     * 如果该编码规则已被加锁，代表此时有其他线程正在处理，则放弃本次操作
     */
    private void asyncExtensiveSerialMaxRange(
            final CodeQuery codeQuery,
            final String serialUnionValueFormat, final String serialUnionValue, final String showUnionValueFormat, final String showUnionValue,
            int initSerialNo, final int size, final List<Integer> cacheParameters) {
        final int currentSerialNo = cacheParameters.get(1);
        final int limitSerialNo = cacheParameters.get(2);

        if (currentSerialNo + codeSerialPolicy.getMarginNum() >= limitSerialNo) {
            if (!distributedLocker.isLocked(key(codeQuery.getPkid(), serialUnionValue, showUnionValue))) {
                taskExecutor.execute(() -> {
                    try {
                        extensiveSerialMaxRange(
                                codeQuery, serialUnionValueFormat, serialUnionValue, showUnionValueFormat, showUnionValue,
                                initSerialNo, size);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                });
            }
        }
    }

}
