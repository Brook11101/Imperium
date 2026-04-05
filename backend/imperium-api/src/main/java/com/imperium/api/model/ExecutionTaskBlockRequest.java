package com.imperium.api.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 执行任务阻塞请求
 */
public record ExecutionTaskBlockRequest(
    @NotBlank(message = "阻塞原因不能为空")
    String blockReason,

    String outputSummary
) {
}
