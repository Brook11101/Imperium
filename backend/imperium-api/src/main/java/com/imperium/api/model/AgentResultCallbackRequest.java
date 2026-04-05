package com.imperium.api.model;

import com.imperium.domain.model.RoleCode;

/**
 * Agent 结果回调请求
 */
public record AgentResultCallbackRequest(
    String docketId,
    RoleCode roleCode,
    String executionTaskId,
    String senateSessionId,
    String stance,
    String summary,
    String details,
    String outputSummary,
    Integer progressPercent
) {
}
