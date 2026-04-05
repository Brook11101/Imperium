package com.imperium.api.model;

import com.imperium.domain.model.RoleCode;

/**
 * Agent 失败回调请求
 */
public record AgentFailureCallbackRequest(
    String docketId,
    RoleCode roleCode,
    String executionTaskId,
    String reason,
    String details
) {
}
