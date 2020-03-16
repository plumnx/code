package com.zhongwang.cloud.platform.service.code.common.repository;

import com.zhongwang.cloud.platform.service.code.common.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 存储基类
 *
 * @param <T>
 */
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, String>, JpaSpecificationExecutor<T> {

}
