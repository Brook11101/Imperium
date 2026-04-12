package com.imperium.worker.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imperium.domain.entity.SenateSessionEntity;
import com.imperium.domain.mapper.SenateSessionMapper;
import com.imperium.domain.model.RoleCode;
import com.imperium.worker.model.DomainEventEnvelope;
import com.imperium.worker.orchestration.DomainEventRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 领域事件消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "imperium.events.consumer.enabled", havingValue = "true", matchIfMissing = true)
@RocketMQMessageListener(topic = "imperium-domain-topic", consumerGroup = "imperium-domain-consumer")
public class DomainEventConsumer implements RocketMQListener<MessageExt> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DomainEventRouter domainEventRouter;

    @Override
    public void onMessage(MessageExt message) {
        try {
            DomainEventEnvelope envelope = objectMapper.readValue(new String(message.getBody(), StandardCharsets.UTF_8), DomainEventEnvelope.class);
            String tag = message.getTags();
            domainEventRouter.route(tag, envelope);
        } catch (Exception ex) {
            log.warn("处理领域事件失败：{}", ex.getMessage(), ex);
        }
    }
}
