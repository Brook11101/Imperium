package com.imperium.api.model;

import com.imperium.domain.model.RoleCode;

/**
 * Agent 进度回调请求
 */
public record AgentProgressCallbackRequest(
    String docketId,
    RoleCode roleCode,
    String executionTaskId,
    Integer progressPercent,
    String outputSummary
) {
}
