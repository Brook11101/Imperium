package com.imperium.worker.model;

import java.util.Map;

/**
 * 领域事件消息体
 */
public record DomainEventEnvelope(
    Long outboxId,
    String aggregateType,
    String aggregateId,
    String tag,
    Map<String, Object> payload
) {
}
