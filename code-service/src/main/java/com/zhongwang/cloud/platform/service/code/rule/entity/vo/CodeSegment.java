package com.zhongwang.cloud.platform.service.code.rule.entity.vo;

import com.zhongwang.cloud.platform.service.code.common.CodeConst;
import com.zhongwang.cloud.platform.service.code.rule.lock.SegmentType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.beans.ConstructorProperties;

/**
 * 流水编码段
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CodeSegment {

    // 段配置主键
    private String pkid;

    // 显示
    private boolean isShow;

    // 流水
    private boolean isSerial;

    // 段类型名称
    private SegmentType segmentType;

    // 段值
    private String value;

    /**
     * 是否流水段
     * @return
     */
    public boolean isSerialVariableType() {
        return SegmentType.SERIAL_VARIABLE.equals(this.segmentType);
    }

    /**
     * 是否显示流水
     * @return
     */
    public boolean isShowUnionValue() {
        return !this.isShow && this.isSerial;
    }

    /**
     * 获取显示流水值
     * @return
     */
    public String getSerialUnionValue() {
        if (this.isSerialVariableType()) {
            return CodeConst.RULE_TAG.RULE_TYPE_IS_SERIAL;
        } else if (this.isShow && this.isSerial) {
            return this.value;
        } else {
            return this.isShow ? CodeConst.RULE_TAG.IS_SHOW_NOT_SERIAL : "";
        }
    }

    @ConstructorProperties({"isShow", "isSerial", "name", "value"})
    public CodeSegment(boolean isShow, boolean isSerial, SegmentType segmentType, String value, String pkid) {
        this.isShow = isShow;
        this.isSerial = isSerial;
        this.segmentType = segmentType;
        this.value = value;
        this.pkid = pkid;
    }

    public String getName() {
        return segmentType.name();
    }

}
