package com.imperium.domain.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 角色编码枚举
 * 对应产品文档：02-组织角色与权责.md
 */
public enum RoleCode {

    CAESAR("CAESAR"),
    PRAECO("PRAECO"),
    SENATOR_STRATEGOS("SENATOR_STRATEGOS"),
    SENATOR_JURIS("SENATOR_JURIS"),
    SENATOR_FISCUS("SENATOR_FISCUS"),
    TRIBUNE("TRIBUNE"),
    CONSUL("CONSUL"),
    LEGATUS("LEGATUS"),
    PRAETOR("PRAETOR"),
    AEDILE("AEDILE"),
    QUAESTOR("QUAESTOR"),
    SCRIBA("SCRIBA"),
    CENSOR("CENSOR"),
    GOVERNOR("GOVERNOR");

    @EnumValue
    @JsonValue
    private final String value;

    RoleCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
