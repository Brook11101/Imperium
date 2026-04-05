package com.imperium.api.model;

import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.RoleCode;
import jakarta.validation.constraints.NotNull;

/**
 * 状态推进请求
 */
public record TransitionRequest(

    @NotNull(message = "目标状态不能为空")
    DocketState targetState,

    @NotNull(message = "操作角色不能为空")
    RoleCode actor,

    String comment
) {
}
