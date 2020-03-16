package com.zhongwang.cloud.platform.service.code.common.entity;

import com.zhongwang.cloud.platform.service.code.common.validator.InsertAvaliableGroup;
import com.zhongwang.cloud.platform.service.code.common.validator.UpdateAvaliableGroup;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity extends UUIDEntity {

    /**
     * 有效状态（封存状态、启用状态） 0-无效；1-有效。
     */
    @Column(name = "flag_status")
    private Short flagStatus = 1;

    /**
     * 排序字段，整数, 需要手工排序时使用，项目组可以根据特殊情况设置特殊算法，预留足够大字段
     */
    @Column(name = "flag_sort")
    private Long flagSort;

    /**
     * 逻辑删除标志, 0-正常；1-逻辑删除
     */
    @Column(name = "flag_delete")
    private Short flagDelete = 0;

    /**
     * 用于唯一规则校验，记录未删除时默认为“NA”，记录删除后设置为UUID。
     * 与诸如编码（需要唯一规则的字段（组）），设置联合唯一索引。
     */
    @Column(name = "flag_delete_token")
    private String flagDeleteToken = "0";

    /**
     * 并发修改版本号，bigint型
     */
    @Column(name = "flag_version")
    @Version
    private Long flagVersion = 0L;

    /**
     * 记录制作人，记录人员的pk_id
     */
    @Column(name = "make_user")
    private String makeUser;

    /**
     * 记录创建时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "make_time", updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private Date makeTime;

    /**
     * 记录最后修改人
     */
    @Column(name = "modify_user")
    private String modifyUser;

    /**
     * 记录最后修改时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modify_time")
    @org.hibernate.annotations.UpdateTimestamp
    private Date modifyTime;

    /**
     * 业务所属公司或组织ID
     */
    @Column(name = "comp_pkid")
    private String compPkid;

    // 系统标识
    @Column(name = "system_code")
    @NotBlank(message = "{VALIDATOR_SYSTEM_CODE_NOT_NULL}",
            groups = {InsertAvaliableGroup.class, UpdateAvaliableGroup.class})
    private String systemCode;

    // 模块标识
    @Column(name = "module_code")
    private String moduleCode;

}
