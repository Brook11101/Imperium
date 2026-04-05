package com.imperium.worker.openclaw;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imperium.domain.entity.AgentCallLogEntity;
import com.imperium.domain.entity.ExecutionTaskEntity;
import com.imperium.domain.entity.RoleConfigEntity;
import com.imperium.domain.mapper.AgentCallLogMapper;
import com.imperium.domain.mapper.ExecutionTaskMapper;
import com.imperium.domain.mapper.RoleConfigMapper;
import com.imperium.domain.model.RoleCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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

    @Value("${imperium.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${imperium.agent.callback-secret:dev-secret-change-in-prod}")
    private String callbackSecret;

    @Value("${imperium.agent.default-timeout-sec:120}")
    private int defaultTimeoutSec;

    public void dispatchPendingExecutionTasks(String docketId) {
        List<ExecutionTaskEntity> tasks = executionTaskMapper.selectList(
            new LambdaQueryWrapper<ExecutionTaskEntity>()
                .eq(ExecutionTaskEntity::getDocketId, docketId)
                .eq(ExecutionTaskEntity::getStatus, "PENDING")
        );

        for (ExecutionTaskEntity task : tasks) {
            dispatchExecutionTask(task);
        }
    }

    public void dispatchSenateRole(String docketId, String sessionId, RoleCode roleCode) {
        RoleConfigEntity config = findRoleConfig(roleCode);
        if (config == null) {
            log.info("跳过元老调度：role={} 未配置 agentId", roleCode);
            return;
        }

        String prompt = """
            You are %s in Imperium.
            Docket ID: %s
            Senate Session ID: %s

            Produce a senate opinion with stance, summary, and details.
            Then call:
            POST %s/internal/agent-callback/result
            Secret header: X-Agent-Callback-Secret = %s
            JSON body fields:
            - docketId
            - roleCode
            - senateSessionId
            - stance
            - summary
            - details
            """.formatted(roleCode.getValue(), docketId, sessionId, baseUrl, callbackSecret);

        executeLoggedCall(docketId, roleCode, config.getAgentId(), prompt);
    }

    private void dispatchExecutionTask(ExecutionTaskEntity task) {
        RoleConfigEntity config = findRoleConfig(task.getRoleCode());
        if (config == null) {
            log.info("跳过执行任务调度：task={} role={} 未配置 agentId", task.getId(), task.getRoleCode());
            return;
        }

        String prompt = """
            You are %s in Imperium.
            Docket ID: %s
            Execution Task ID: %s

            Start the assigned work and report progress through:
            POST %s/internal/agent-callback/progress
            Secret header: X-Agent-Callback-Secret = %s

            When finished, call:
            POST %s/internal/agent-callback/result
            Secret header: X-Agent-Callback-Secret = %s
            Include docketId, roleCode, executionTaskId, progressPercent, summary, and outputSummary.
            """.formatted(task.getRoleCode().getValue(), task.getDocketId(), task.getId(), baseUrl, callbackSecret, baseUrl, callbackSecret);

        executeLoggedCall(task.getDocketId(), task.getRoleCode(), config.getAgentId(), prompt);
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
        } catch (IOException | InterruptedException ex) {
            logEntity.setExitCode(1);
            logEntity.setStderrText(ex.getMessage());
            logEntity.setStatus("FAILED");
            logEntity.setEndedAt(LocalDateTime.now());
            agentCallLogMapper.updateById(logEntity);
            log.warn("OpenClaw 调用失败：docket={} role={} cause={}", docketId, roleCode, ex.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
