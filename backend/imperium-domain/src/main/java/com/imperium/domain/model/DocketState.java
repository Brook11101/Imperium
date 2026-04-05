package com.imperium.domain.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 议案状态枚举
 * 对应产品文档：03-运行模式与状态机.md
 */
public enum DocketState {

    EDICT_ISSUED("EDICT_ISSUED"),
    TRIAGED("TRIAGED"),
    IN_SENATE("IN_SENATE"),
    DEBATING("DEBATING"),
    VETO_REVIEW("VETO_REVIEW"),
    AWAITING_CAESAR("AWAITING_CAESAR"),
    MANDATED("MANDATED"),
    DELEGATED("DELEGATED"),
    IN_EXECUTION("IN_EXECUTION"),
    UNDER_AUDIT("UNDER_AUDIT"),
    ARCHIVED("ARCHIVED"),
    REJECTED("REJECTED"),
    SUSPENDED("SUSPENDED"),
    REVOKED("REVOKED");

    @EnumValue
    @JsonValue
    private final String value;

    DocketState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isTerminal() {
        return this == ARCHIVED || this == REJECTED || this == REVOKED;
    }
}
