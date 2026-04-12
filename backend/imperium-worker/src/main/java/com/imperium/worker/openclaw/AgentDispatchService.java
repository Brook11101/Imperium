package com.imperium.worker.openclaw;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imperium.domain.entity.AgentCallLogEntity;
import com.imperium.domain.entity.DelegationEntity;
import com.imperium.domain.entity.DocketEntity;
import com.imperium.domain.entity.ExecutionTaskEntity;
import com.imperium.domain.entity.RoleConfigEntity;
import com.imperium.domain.entity.SenateOpinionEntity;
import com.imperium.domain.entity.SenateSessionEntity;
import com.imperium.domain.mapper.AgentCallLogMapper;
import com.imperium.domain.mapper.DelegationMapper;
import com.imperium.domain.mapper.DocketMapper;
import com.imperium.domain.mapper.ExecutionTaskMapper;
import com.imperium.domain.mapper.RoleConfigMapper;
import com.imperium.domain.mapper.SenateOpinionMapper;
import com.imperium.domain.mapper.SenateSessionMapper;
import com.imperium.domain.mapper.TribuneReviewMapper;
import com.imperium.domain.model.RoleCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Agent 调度服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentDispatchService {

    private final ExecutionTaskMapper executionTaskMapper;
    private final RoleConfigMapper roleConfigMapper;
    private final AgentCallLogMapper agentCallLogMapper;
    private final OpenClawCliClient openClawCliClient;
    private final DelegationMapper delegationMapper;
    private final DocketMapper docketMapper;
    private final SenateOpinionMapper senateOpinionMapper;
    private final SenateSessionMapper senateSessionMapper;
    private final TribuneReviewMapper tribuneReviewMapper;

    @Value("${imperium.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${imperium.agent.callback-secret:dev-secret-change-in-prod}")
    private String callbackSecret;

    @Value("${imperium.agent.default-timeout-sec:120}")
    private int defaultTimeoutSec;

    public void dispatchPendingExecutionTasks(String docketId) {
        List<ExecutionTaskEntity> candidates = executionTaskMapper.selectList(
            new LambdaQueryWrapper<ExecutionTaskEntity>()
                .eq(ExecutionTaskEntity::getDocketId, docketId)
                .eq(ExecutionTaskEntity::getStatus, "PENDING")
        );

        for (ExecutionTaskEntity task : candidates) {
            if (!isReadyForDispatch(task)) {
                continue;
            }
            dispatchExecutionTask(task);
        }
    }

    public void dispatchSenateRole(String docketId, String sessionId, RoleCode roleCode) {
        if (hasSenateOpinion(sessionId, roleCode)) {
            log.info("跳过元老调度：session={} role={} 已有意见", sessionId, roleCode);
            return;
        }
        RoleConfigEntity config = findRoleConfig(roleCode);
        if (config == null) {
            log.info("跳过元老调度：role={} 未配置 agentId", roleCode);
            return;
        }

        String prompt = """
            You are %s in Imperium.
            Docket ID: %s
            Senate Session ID: %s

            Your only task is to submit one senate opinion.
            Use the exec tool and curl. Do not ask follow-up questions. Do not produce long explanations.

            Choose exactly one stance from SUPPORT, OBJECT, CONDITION, NEUTRAL.

            Execute a single callback:
            curl -sS -X POST '%s/internal/agent-callback/result' \
              -H 'Content-Type: application/json' \
              -H 'X-Agent-Callback-Secret: %s' \
              -d '{"docketId":"%s","roleCode":"%s","senateSessionId":"%s","stance":"SUPPORT","summary":"<one sentence>","details":"<short details>"}'

            Replace stance/summary/details with your actual opinion.
            After the callback succeeds, respond with exactly: CALLBACK_OK
            """.formatted(roleCode.getValue(), docketId, sessionId, baseUrl, callbackSecret, docketId, roleCode.getValue(), sessionId);

        CompletableFuture.runAsync(() -> executeLoggedCall(docketId, roleCode, config.getAgentId(), prompt));
    }

    public void dispatchTribuneRole(String docketId) {
        if (hasTribuneReview(docketId)) {
            log.info("跳过保民官调度：docket={} 已存在审查记录", docketId);
            return;
        }
        RoleConfigEntity config = findRoleConfig(RoleCode.TRIBUNE);
        if (config == null) {
            log.info("跳过保民官调度：未配置 agentId");
            return;
        }

        String senateContext = buildSenateContext(docketId);
        String prompt = """
            You are TRIBUNE in Imperium.
            Docket ID: %s

            Current senate output:
            %s

            Review the current senate output and choose one action:
            - approve
            - reject
            - return

            Use the exec tool and curl. Do not ask follow-up questions.

            Call exactly one endpoint:
            - %s/api/dockets/%s/tribune/approve
            - %s/api/dockets/%s/tribune/reject
            - %s/api/dockets/%s/tribune/return

            Header: X-Agent-Callback-Secret: %s
            JSON body may contain reason and notes.
            After the callback succeeds, respond with exactly: CALLBACK_OK
            """.formatted(docketId, senateContext, baseUrl, docketId, baseUrl, docketId, baseUrl, docketId, callbackSecret);

        CompletableFuture.runAsync(() -> executeLoggedCall(docketId, RoleCode.TRIBUNE, config.getAgentId(), prompt));
    }

    public void dispatchAuditRoles(String docketId) {
        dispatchAuditRole(docketId, RoleCode.PRAETOR);
        dispatchAuditRole(docketId, RoleCode.SCRIBA);
    }

    private void dispatchAuditRole(String docketId, RoleCode roleCode) {
        RoleConfigEntity config = findRoleConfig(roleCode);
        if (config == null) {
            log.info("跳过审计调度：role={} 未配置 agentId", roleCode);
            return;
        }

        String prompt = """
            You are %s in Imperium.
            Docket ID: %s

            Use the exec tool and curl.

            If you find execution issues, call:
            POST %s/api/dockets/%s/audit/return

            If you find strategic risk, call:
            POST %s/api/dockets/%s/audit/escalate

            If the docket is acceptable, call:
            POST %s/api/dockets/%s/audit/pass

            Header: X-Agent-Callback-Secret: %s
            Body may include riskNotes, qualityNotes, finalSummary, artifacts.
            After the callback succeeds, respond with exactly: CALLBACK_OK
            """.formatted(roleCode.getValue(), docketId, baseUrl, docketId, baseUrl, docketId, baseUrl, docketId, callbackSecret);

        CompletableFuture.runAsync(() -> executeLoggedCall(docketId, roleCode, config.getAgentId(), prompt));
    }

    private void dispatchExecutionTask(ExecutionTaskEntity task) {
        RoleConfigEntity config = findRoleConfig(task.getRoleCode());
        if (config == null) {
            log.info("跳过执行任务调度：task={} role={} 未配置 agentId", task.getId(), task.getRoleCode());
            return;
        }

        task.setStatus("DISPATCHING");
        task.setUpdatedAt(LocalDateTime.now());
        executionTaskMapper.updateById(task);

        String prompt = """
            You are %s in Imperium.
            Docket ID: %s
            Execution Task ID: %s

            Use the exec tool and curl. Do not ask follow-up questions unless absolutely blocked.

            First, report progress to:
            %s/internal/agent-callback/progress

            When your work is complete, report result to:
            %s/internal/agent-callback/result

            Header: X-Agent-Callback-Secret: %s
            JSON must include docketId, roleCode, executionTaskId, progressPercent, summary, and outputSummary.
            After the final callback succeeds, respond with exactly: CALLBACK_OK
            """.formatted(task.getRoleCode().getValue(), task.getDocketId(), task.getId(), baseUrl, baseUrl, callbackSecret);

        CompletableFuture.runAsync(() -> executeLoggedCall(task.getDocketId(), task.getRoleCode(), config.getAgentId(), prompt));
    }

    private boolean isReadyForDispatch(ExecutionTaskEntity task) {
        DelegationEntity delegation = delegationMapper.selectById(task.getDelegationId());
        if (delegation == null || delegation.getDependsOnJson() == null || delegation.getDependsOnJson().isEmpty()) {
            return true;
        }

        List<String> incomplete = new ArrayList<>();
        for (String dependencyId : delegation.getDependsOnJson()) {
            DelegationEntity dependency = delegationMapper.selectById(dependencyId);
            if (dependency == null || !"COMPLETED".equals(dependency.getStatus())) {
                incomplete.add(dependencyId);
            }
        }
        if (!incomplete.isEmpty()) {
            log.info("任务 {} 暂不派发，依赖未完成: {}", task.getId(), incomplete);
            return false;
        }
        return true;
    }

    private boolean hasSenateOpinion(String sessionId, RoleCode roleCode) {
        return senateOpinionMapper.selectCount(
            new LambdaQueryWrapper<com.imperium.domain.entity.SenateOpinionEntity>()
                .eq(com.imperium.domain.entity.SenateOpinionEntity::getSessionId, sessionId)
                .eq(com.imperium.domain.entity.SenateOpinionEntity::getAgentId, roleCode)
        ) > 0;
    }

    private boolean hasTribuneReview(String docketId) {
        return tribuneReviewMapper.selectCount(
            new LambdaQueryWrapper<com.imperium.domain.entity.TribuneReviewEntity>()
                .eq(com.imperium.domain.entity.TribuneReviewEntity::getDocketId, docketId)
        ) > 0;
    }

    private String buildSenateContext(String docketId) {
        SenateSessionEntity session = senateSessionMapper.selectOne(
            new LambdaQueryWrapper<SenateSessionEntity>()
                .eq(SenateSessionEntity::getDocketId, docketId)
                .orderByDesc(SenateSessionEntity::getOpenedAt)
                .last("LIMIT 1")
        );
        if (session == null) {
            return "No senate session available.";
        }

        List<SenateOpinionEntity> opinions = senateOpinionMapper.selectList(
            new LambdaQueryWrapper<SenateOpinionEntity>()
                .eq(SenateOpinionEntity::getSessionId, session.getId())
                .orderByAsc(SenateOpinionEntity::getGeneratedAt)
        );

        StringBuilder builder = new StringBuilder();
        builder.append("sessionId=").append(session.getId())
            .append(", status=").append(session.getStatus())
            .append(", motion=").append(session.getRecommendedMotion())
            .append("\nconsensus=").append(session.getConsensusJson() == null ? List.of() : session.getConsensusJson())
            .append("\ndisputes=").append(session.getDisputesJson() == null ? List.of() : session.getDisputesJson())
            .append("\nopinions=");
        for (SenateOpinionEntity opinion : opinions) {
            builder.append("\n- ")
                .append(opinion.getAgentId().getValue())
                .append(" [").append(opinion.getStance()).append("] ")
                .append(opinion.getSummary());
        }
        return builder.toString();
    }

    private RoleConfigEntity findRoleConfig(RoleCode roleCode) {
        RoleConfigEntity config = roleConfigMapper.selectById(roleCode.getValue());
        if (config == null || config.getEnabled() == null || config.getEnabled() == 0) {
            return null;
        }
        if (config.getAgentId() == null || config.getAgentId().isBlank()) {
            return null;
        }
        return config;
    }

    private void executeLoggedCall(String docketId, RoleCode roleCode, String agentId, String prompt) {
        AgentCallLogEntity logEntity = new AgentCallLogEntity();
        logEntity.setDocketId(docketId);
        logEntity.setRoleCode(roleCode.getValue());
        logEntity.setCommandLine("openclaw agent --agent " + agentId + " -m <prompt>");
        logEntity.setStatus("RUNNING");
        logEntity.setStartedAt(LocalDateTime.now());
        agentCallLogMapper.insert(logEntity);

        try {
            OpenClawCliResult result = openClawCliClient.execute(agentId, prompt, defaultTimeoutSec);
            logEntity.setExitCode(result.exitCode());
            logEntity.setStdoutText(result.stdout());
            logEntity.setStderrText(result.stderr());
            logEntity.setStatus(result.success() ? "SUCCESS" : "FAILED");
            logEntity.setEndedAt(LocalDateTime.now());
            agentCallLogMapper.updateById(logEntity);
            updateExecutionTaskDispatchStatus(docketId, roleCode, result.success());
        } catch (IOException | InterruptedException ex) {
            logEntity.setExitCode(1);
            logEntity.setStderrText(ex.getMessage());
            logEntity.setStatus("FAILED");
            logEntity.setEndedAt(LocalDateTime.now());
            agentCallLogMapper.updateById(logEntity);
            log.warn("OpenClaw 调用失败：docket={} role={} cause={}", docketId, roleCode, ex.getMessage());
            updateExecutionTaskDispatchStatus(docketId, roleCode, false);
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void updateExecutionTaskDispatchStatus(String docketId, RoleCode roleCode, boolean success) {
        DocketEntity docket = docketMapper.selectById(docketId);
        if (docket == null) {
            return;
        }
        if (roleCode == RoleCode.LEGATUS || roleCode == RoleCode.PRAETOR || roleCode == RoleCode.AEDILE
            || roleCode == RoleCode.QUAESTOR || roleCode == RoleCode.SCRIBA || roleCode == RoleCode.GOVERNOR) {
            List<ExecutionTaskEntity> tasks = executionTaskMapper.selectList(
                new LambdaQueryWrapper<ExecutionTaskEntity>()
                    .eq(ExecutionTaskEntity::getDocketId, docketId)
                    .eq(ExecutionTaskEntity::getRoleCode, roleCode)
                    .eq(ExecutionTaskEntity::getStatus, "DISPATCHING")
            );
            for (ExecutionTaskEntity task : tasks) {
                task.setStatus(success ? "RUNNING" : "FAILED");
                task.setUpdatedAt(LocalDateTime.now());
                executionTaskMapper.updateById(task);
            }
        }
    }
}
