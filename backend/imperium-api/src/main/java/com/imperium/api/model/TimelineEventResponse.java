package com.imperium.api.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 议案时间线事件响应
 */
public record TimelineEventResponse(
    Long id,
    String docketId,
    String eventType,
    String actorType,
    String actorId,
    Map<String, Object> payload,
    LocalDateTime createdAt
) {
}
