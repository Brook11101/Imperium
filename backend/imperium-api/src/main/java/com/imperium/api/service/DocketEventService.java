package com.imperium.api.service;

import com.imperium.domain.entity.DocketEventEntity;
import com.imperium.domain.entity.OutboxEventEntity;
import com.imperium.domain.mapper.DocketEventMapper;
import com.imperium.domain.mapper.OutboxEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 议案事件记录服务
 */
@Service
@RequiredArgsConstructor
public class DocketEventService {

    private final DocketEventMapper docketEventMapper;
    private final OutboxEventMapper outboxEventMapper;

    public void record(String docketId, String eventType, String actorType, String actorId, Map<String, Object> payload) {
        DocketEventEntity event = new DocketEventEntity();
        event.setDocketId(docketId);
        event.setEventType(eventType);
        event.setActorType(actorType);
        event.setActorId(actorId);
        event.setPayloadJson(payload);
        event.setCreatedAt(LocalDateTime.now());
        docketEventMapper.insert(event);

        OutboxEventEntity outbox = new OutboxEventEntity();
        outbox.setAggregateType("DOCKET");
        outbox.setAggregateId(docketId);
        outbox.setTopic("imperium-domain-topic");
        outbox.setTag(eventType);
        outbox.setPayloadJson(payload);
        outbox.setPublishStatus("PENDING");
        outbox.setRetryCount(0);
        outbox.setCreatedAt(LocalDateTime.now());
        outboxEventMapper.insert(outbox);
    }
}
