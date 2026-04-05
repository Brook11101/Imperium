package com.imperium.api.model;

import com.imperium.domain.model.RoleCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 元老意见提交请求
 */
public record SenateOpinionRequest(
    @NotNull(message = "元老角色不能为空")
    RoleCode roleCode,

    @NotBlank(message = "立场不能为空")
    String stance,

    @NotBlank(message = "摘要不能为空")
    String summary,

    String details
) {
}
