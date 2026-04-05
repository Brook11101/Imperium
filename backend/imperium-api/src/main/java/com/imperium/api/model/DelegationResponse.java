package com.imperium.api.model;

import com.imperium.domain.model.RoleCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 派发项响应
 */
public record DelegationResponse(
    String id,
    String docketId,
    RoleCode roleCode,
    String objective,
    String status,
    List<String> dependsOn,
    String executionTaskId,
    LocalDateTime assignedAt,
    LocalDateTime completedAt
) {
}
