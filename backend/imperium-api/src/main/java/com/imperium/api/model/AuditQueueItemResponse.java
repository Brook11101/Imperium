package com.imperium.api.model;

import com.imperium.domain.model.RoleCode;

import java.time.LocalDateTime;

/**
 * 审计队列项响应
 */
public record AuditQueueItemResponse(
    String docketId,
    String title,
    String mode,
    String priority,
    String riskLevel,
    RoleCode currentOwner,
    LocalDateTime updatedAt
) {
}
