package com.imperium.api.model;

import com.imperium.domain.model.RoleCode;

import java.time.LocalDateTime;

/**
 * 执行任务响应
 */
public record ExecutionTaskResponse(
    String id,
    String docketId,
    String delegationId,
    RoleCode roleCode,
    Integer progressPercent,
    String status,
    String blockReason,
    String outputSummary,
    LocalDateTime updatedAt
) {
}
