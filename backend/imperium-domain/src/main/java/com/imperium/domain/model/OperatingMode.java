package com.imperium.domain.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 运行模式枚举
 * 对应产品文档：03-运行模式与状态机.md
 */
public enum OperatingMode {

    STANDARD_SENATE("STANDARD_SENATE"),
    DIRECT_DECREE("DIRECT_DECREE"),
    TRIBUNE_LOCK("TRIBUNE_LOCK"),
    CAMPAIGN_MODE("CAMPAIGN_MODE");

    @EnumValue
    @JsonValue
    private final String value;

    OperatingMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
