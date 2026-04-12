package com.imperium.worker.outbox;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imperium.domain.entity.OutboxEventEntity;
import com.imperium.domain.mapper.OutboxEventMapper;
import com.imperium.worker.model.DomainEventEnvelope;
import com.imperium.worker.orchestration.DomainEventRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox 发布任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "imperium.outbox.publisher.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisherJob {

    private final OutboxEventMapper outboxEventMapper;
    private final RocketMQTemplate rocketMQTemplate;
    private final DomainEventRouter domainEventRouter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${imperium.outbox.publisher.batch-size:50}")
    private int batchSize;

    @Value("${imperium.outbox.publisher.max-retries:10}")
    private int maxRetries;

    @Value("${imperium.outbox.local-dispatch-enabled:false}")
    private boolean localDispatchEnabled;

    @Scheduled(fixedDelayString = "${imperium.outbox.publisher.interval-ms:5000}")
    public void publishPendingEvents() {
        try {
            List<OutboxEventEntity> events = outboxEventMapper.selectList(
                new LambdaQueryWrapper<OutboxEventEntity>()
                    .in(OutboxEventEntity::getPublishStatus, List.of("PENDING", "FAILED"))
                    .lt(OutboxEventEntity::getRetryCount, maxRetries)
                    .orderByAsc(OutboxEventEntity::getCreatedAt)
                    .last("LIMIT " + batchSize)
            );

            if (!events.isEmpty()) {
                log.info("Outbox 扫描到 {} 条待发布事件", events.size());
            }

            for (OutboxEventEntity event : events) {
                publishOne(event);
            }
        } catch (Throwable ex) {
            log.warn("Outbox 定时发布任务异常：{}", ex.getMessage(), ex);
        }
    }

    private void publishOne(OutboxEventEntity event) {
        try {
            String payload = objectMapper.writeValueAsString(new DomainEventEnvelope(
                event.getId(),
                event.getAggregateType(),
                event.getAggregateId(),
                event.getTag(),
                event.getPayloadJson()
            ));
            String destination = buildDestination(event);
            if (localDispatchEnabled) {
                domainEventRouter.route(event.getTag(), new DomainEventEnvelope(
                    event.getId(),
                    event.getAggregateType(),
                    event.getAggregateId(),
                    event.getTag(),
                    event.getPayloadJson()
                ));
            } else {
                rocketMQTemplate.syncSend(destination, payload);
            }

            event.setPublishStatus("PUBLISHED");
            event.setPublishedAt(LocalDateTime.now());
            outboxEventMapper.updateById(event);

            log.info("Outbox 事件已发布：id={} destination={}", event.getId(), destination);
        } catch (JsonProcessingException ex) {
            markFailed(event, "JSON 序列化失败", ex);
        } catch (Throwable ex) {
            markFailed(event, "RocketMQ 发布失败", ex);
        }
    }

    private String buildDestination(OutboxEventEntity event) {
        if (event.getTag() == null || event.getTag().isBlank()) {
            return event.getTopic();
        }
        return event.getTopic() + ":" + event.getTag();
    }

    private void markFailed(OutboxEventEntity event, String message, Throwable ex) {
        event.setPublishStatus("FAILED");
        event.setRetryCount((event.getRetryCount() == null ? 0 : event.getRetryCount()) + 1);
        outboxEventMapper.updateById(event);
        log.warn("{}: id={} retryCount={} cause={}", message, event.getId(), event.getRetryCount(), ex.getMessage());
    }
}
