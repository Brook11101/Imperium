package com.imperium.api.model;

import com.imperium.domain.model.RoleCode;

import java.time.LocalDateTime;

/**
 * 元老意见响应
 */
public record SenateOpinionResponse(
    Long id,
    RoleCode roleCode,
    String stance,
    String summary,
    String details,
    LocalDateTime generatedAt
) {
}
