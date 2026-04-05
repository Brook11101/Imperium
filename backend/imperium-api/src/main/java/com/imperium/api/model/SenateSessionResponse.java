package com.imperium.api.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 元老院会话响应
 */
public record SenateSessionResponse(
    String id,
    String docketId,
    String status,
    String recommendedMotion,
    List<String> consensus,
    List<String> disputes,
    List<SenateOpinionResponse> opinions,
    LocalDateTime openedAt,
    LocalDateTime closedAt
) {
}
