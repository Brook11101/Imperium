package com.imperium.api.model;

import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.OperatingMode;
import com.imperium.domain.model.RoleCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 议案详情响应
 */
public record DocketDetailResponse(
    String id,
    String title,
    DocketState state,
    OperatingMode mode,
    String priority,
    String riskLevel,
    RoleCode currentOwner,
    String edictRaw,
    String summary,
    LocalDateTime lastProgressAt,
    Integer retryCount,
    Integer escalationLevel,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<DocketState> availableTransitions
) {
}
