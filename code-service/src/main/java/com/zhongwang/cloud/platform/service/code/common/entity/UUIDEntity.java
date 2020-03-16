package com.zhongwang.cloud.platform.service.code.common.entity;

import com.zhongwang.cloud.platform.service.code.common.validator.UpdateAvaliableGroup;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class UUIDEntity {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @NotBlank(message = "{VALIDATOR_PKID_NOT_NULL}", groups = {UpdateAvaliableGroup.class})
    private String pkid;

    public String getPkid() {
        return pkid;
    }

    public void setPkid(String pkid) {
        this.pkid = pkid;
    }
}
