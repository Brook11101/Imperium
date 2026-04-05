package com.imperium.api.model;

import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.OperatingMode;
import com.imperium.domain.model.RoleCode;

import java.time.LocalDateTime;

/**
 * 议案概览响应（列表/看板用）
 */
public record DocketOverviewResponse(
    String id,
    String title,
    DocketState state,
    OperatingMode mode,
    String priority,
    String riskLevel,
    RoleCode currentOwner,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
