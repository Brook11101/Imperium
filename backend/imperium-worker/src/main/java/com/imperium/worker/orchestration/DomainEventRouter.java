package com.imperium.worker.orchestration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imperium.domain.entity.SenateSessionEntity;
import com.imperium.domain.mapper.SenateSessionMapper;
import com.imperium.domain.model.RoleCode;
import com.imperium.worker.model.DomainEventEnvelope;
import com.imperium.worker.openclaw.AgentDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 领域事件路由器，可供 MQ consumer 和本地 outbox 分发复用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventRouter {

    private final AgentDispatchService agentDispatchService;
    private final SenateSessionMapper senateSessionMapper;
    private final OrchestrationClient orchestrationClient;

    public void route(String tag, DomainEventEnvelope envelope) {
        switch (tag) {
            case "DOCKET_CREATED" -> safe(() -> orchestrationClient.triage(envelope.aggregateId()), tag, envelope.aggregateId());
            case "DELEGATION_CREATED" -> safe(() -> agentDispatchService.dispatchPendingExecutionTasks(envelope.aggregateId()), tag, envelope.aggregateId());
            case "SENATE_SESSION_OPENED" -> dispatchSenators(envelope.aggregateId());
            case "SENATE_OPINION_RECORDED", "SENATE_SESSION_CLOSED" -> tryFinalizeSenate(envelope.aggregateId());
            case "STATE_CHANGED" -> handleStateChanged(envelope);
            default -> log.debug("忽略领域事件：tag={} aggregateId={}", tag, envelope.aggregateId());
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

        safe(() -> agentDispatchService.dispatchSenateRole(docketId, session.getId(), RoleCode.SENATOR_STRATEGOS), "SENATOR_STRATEGOS", docketId);
        safe(() -> agentDispatchService.dispatchSenateRole(docketId, session.getId(), RoleCode.SENATOR_JURIS), "SENATOR_JURIS", docketId);
        safe(() -> agentDispatchService.dispatchSenateRole(docketId, session.getId(), RoleCode.SENATOR_FISCUS), "SENATOR_FISCUS", docketId);
    }

    private void tryFinalizeSenate(String docketId) {
        SenateSessionEntity session = senateSessionMapper.selectOne(
            new LambdaQueryWrapper<SenateSessionEntity>()
                .eq(SenateSessionEntity::getDocketId, docketId)
                .eq(SenateSessionEntity::getStatus, "CLOSED")
                .orderByDesc(SenateSessionEntity::getOpenedAt)
                .last("LIMIT 1")
        );
        if (session != null) {
            safe(() -> orchestrationClient.finalizeSenate(docketId), "FINALIZE_SENATE", docketId);
        }
    }

    private void handleStateChanged(DomainEventEnvelope envelope) {
        Object to = envelope.payload() == null ? null : envelope.payload().get("to");
        if (!(to instanceof String toState)) {
            return;
        }
        switch (toState) {
            case "IN_SENATE" -> safe(() -> orchestrationClient.openSenate(envelope.aggregateId()), "OPEN_SENATE", envelope.aggregateId());
            case "VETO_REVIEW" -> safe(() -> agentDispatchService.dispatchTribuneRole(envelope.aggregateId()), "TRIBUNE", envelope.aggregateId());
            case "UNDER_AUDIT" -> safe(() -> agentDispatchService.dispatchAuditRoles(envelope.aggregateId()), "AUDIT", envelope.aggregateId());
            default -> {
            }
        }
    }

    private void safe(ThrowingRunnable runnable, String stage, String docketId) {
        try {
            runnable.run();
        } catch (Throwable ex) {
            log.warn("领域事件路由失败：stage={} docketId={} cause={}", stage, docketId, ex.getMessage(), ex);
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }
}
