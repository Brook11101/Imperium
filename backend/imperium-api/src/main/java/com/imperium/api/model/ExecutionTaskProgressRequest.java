package com.imperium.api.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 执行任务进度更新请求
 */
public record ExecutionTaskProgressRequest(
    @NotNull(message = "进度不能为空")
    @Min(value = 0, message = "进度不能小于 0")
    @Max(value = 100, message = "进度不能大于 100")
    Integer progressPercent,

    String outputSummary
) {
}
