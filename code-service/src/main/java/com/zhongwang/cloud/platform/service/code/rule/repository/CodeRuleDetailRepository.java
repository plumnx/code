package com.zhongwang.cloud.platform.service.code.rule.repository;

import com.zhongwang.cloud.platform.service.code.common.repository.BaseRepository;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleDetail;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 规则明细资源
 */
@Repository
public interface CodeRuleDetailRepository extends BaseRepository<CodeRuleDetail> {

    @Modifying
    @Query("delete from CodeRuleDetail where headPkid = ?1")
    void deleteByHeadPkid(String headPkid);

}
