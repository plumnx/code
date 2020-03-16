//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.zhongwang.cloud.platform.service.code.rule.repository;

import com.zhongwang.cloud.platform.service.code.common.repository.BaseRepository;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleSerial;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

/**
 * 规则流水资源
 */
@Repository
public interface CodeRuleSerialRepository extends BaseRepository<CodeRuleSerial> {

    @Query("select pkid from CodeRuleSerial where headPkid=:headPkid and serialUnionValue=:serialUnionValue and showUnionValue=:showUnionValue and flagDelete=:flagDelete")
    String findPkidByParams(
            @Param("headPkid") String headPkid, @Param("serialUnionValue") String serialUnionValue, @Param("showUnionValue") String showUnionValue, @Param("flagDelete") Short flagDelete);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "30000")})
    @Query("from CodeRuleSerial where pkid=:pkid")
    CodeRuleSerial findByPkidForPessimistic(@Param("pkid") String pkid);

    @Modifying
    @Query("update CodeRuleSerial " +
                "set codeMaxValue=:codeMaxValue, " +
                "flagVersion=:newFlagVersion, " +
                "serialUnionValueFormat=:serialUnionValueFormat, " +
                "showUnionValueFormat=:showUnionValueFormat " +
            "where pkid=:pkid and flagVersion=:flagVersion")
    int updateCodeRuleSerialSetMaxValue(
            @Param("pkid") String pkid,
            @Param("codeMaxValue") Integer codeMaxValue,
            @Param("serialUnionValueFormat") String serialUnionValueFormat,
            @Param("showUnionValueFormat") String showUnionValueFormat,
            @Param("newFlagVersion") Long newFlagVersion,
            @Param("flagVersion") Long flagVersion);

}
