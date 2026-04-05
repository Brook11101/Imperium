package com.imperium.api.model;

/**
 * 执行任务完成请求
 */
public record ExecutionTaskCompleteRequest(
    String outputSummary
) {
}
