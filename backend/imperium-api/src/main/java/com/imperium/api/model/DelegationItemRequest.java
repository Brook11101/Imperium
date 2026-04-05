package com.imperium.api.model;

import com.imperium.domain.model.RoleCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 单个派发项请求
 */
public record DelegationItemRequest(
    @NotNull(message = "目标角色不能为空")
    RoleCode roleCode,

    @NotBlank(message = "执行目标不能为空")
    String objective,

    List<String> dependsOn
) {
}
