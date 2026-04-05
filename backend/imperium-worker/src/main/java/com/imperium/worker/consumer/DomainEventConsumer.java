package com.imperium.worker.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imperium.domain.entity.SenateSessionEntity;
import com.imperium.domain.mapper.SenateSessionMapper;
import com.imperium.domain.model.RoleCode;
import com.imperium.worker.model.DomainEventEnvelope;
import com.imperium.worker.openclaw.AgentDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 领域事件消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "imperium-domain-topic", consumerGroup = "imperium-domain-consumer")
public class DomainEventConsumer implements RocketMQListener<MessageExt> {

    private final ObjectMapper objectMapper;
    private final AgentDispatchService agentDispatchService;
    private final SenateSessionMapper senateSessionMapper;

    @Override
    public void onMessage(MessageExt message) {
        try {
            DomainEventEnvelope envelope = objectMapper.readValue(new String(message.getBody(), StandardCharsets.UTF_8), DomainEventEnvelope.class);
            String tag = message.getTags();
            switch (tag) {
                case "DELEGATION_CREATED" -> agentDispatchService.dispatchPendingExecutionTasks(envelope.aggregateId());
                case "SENATE_SESSION_OPENED" -> dispatchSenators(envelope.aggregateId());
                default -> log.debug("忽略领域事件：tag={} aggregateId={}", tag, envelope.aggregateId());
            }
        } catch (Exception ex) {
            log.warn("处理领域事件失败：{}", ex.getMessage(), ex);
        }
    }

    private void dispatchSenators(String docketId) {
        SenateSessionEntity session = senateSessionMapper.selectOne(
            new LambdaQueryWrapper<SenateSessionEntity>()
                .eq(SenateSessionEntity::getDocketId, docketId)
                .eq(SenateSessionEntity::getStatus, "OPEN")
                .orderByDesc(SenateSessionEntity::getOpenedAt)
                .last("LIMIT 1")
        );
        if (session == null) {
            return;
        }

        agentDispatchService.dispatchSenateRole(docketId, session.getId(), RoleCode.SENATOR_STRATEGOS);
        agentDispatchService.dispatchSenateRole(docketId, session.getId(), RoleCode.SENATOR_JURIS);
        agentDispatchService.dispatchSenateRole(docketId, session.getId(), RoleCode.SENATOR_FISCUS);
    }
}
