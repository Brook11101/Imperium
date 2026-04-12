package com.imperium.domain.statemachine;

import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.OperatingMode;
import com.imperium.domain.model.RoleCode;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * 状态流转定义
 */
@Data
@Builder
public class DocketTransition {

    /** 起始状态 */
    private DocketState from;

    /** 目标状态 */
    private DocketState to;

    /** 允许触发的角色（空集合表示任意角色均可） */
    private Set<RoleCode> allowedActors;

    /** 该流转仅在以下运行模式下允许（空集合表示不限模式） */
    private Set<OperatingMode> allowedModes;

    /** 备注说明 */
    private String description;

    public DocketState getFrom() {
        return from;
    }

    public DocketState getTo() {
        return to;
    }

    public Set<RoleCode> getAllowedActors() {
        return allowedActors;
    }

    public Set<OperatingMode> getAllowedModes() {
        return allowedModes;
    }

    public String getDescription() {
        return description;
    }
}
