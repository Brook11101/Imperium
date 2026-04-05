package com.imperium.api.service;

import com.imperium.api.model.AgentFailureCallbackRequest;
import com.imperium.api.model.AgentProgressCallbackRequest;
import com.imperium.api.model.AgentResultCallbackRequest;
import com.imperium.api.model.ExecutionTaskBlockRequest;
import com.imperium.api.model.ExecutionTaskCompleteRequest;
import com.imperium.api.model.ExecutionTaskProgressRequest;
import com.imperium.api.model.SenateOpinionRequest;
import com.imperium.domain.model.RoleCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Agent 回调处理服务
 */
@Service
@RequiredArgsConstructor
public class AgentCallbackService {

    private static final Set<RoleCode> SENATOR_ROLES = Set.of(
        RoleCode.SENATOR_STRATEGOS,
        RoleCode.SENATOR_JURIS,
        RoleCode.SENATOR_FISCUS
    );

    private final ExecutionTaskService executionTaskService;
    private final SenateService senateService;
    private final DocketEventService docketEventService;

    public void handleProgress(AgentProgressCallbackRequest req) {
        if (req.executionTaskId() != null && !req.executionTaskId().isBlank()) {
            executionTaskService.progress(
                req.executionTaskId(),
                new ExecutionTaskProgressRequest(req.progressPercent() == null ? 0 : req.progressPercent(), req.outputSummary())
            );
            return;
        }

        docketEventService.record(req.docketId(), "AGENT_PROGRESS_REPORTED", "ROLE",
            req.roleCode() == null ? "UNKNOWN" : req.roleCode().getValue(),
            Map.of(
                "progressPercent", req.progressPercent() == null ? 0 : req.progressPercent(),
                "outputSummary", req.outputSummary() == null ? "" : req.outputSummary()
            )
        );
    }

    public void handleResult(AgentResultCallbackRequest req) {
        if (req.roleCode() != null && SENATOR_ROLES.contains(req.roleCode())) {
            senateService.submitOpinion(req.docketId(), new SenateOpinionRequest(
                req.roleCode(),
                req.stance() == null ? "NEUTRAL" : req.stance(),
                req.summary() == null ? "自动回调意见" : req.summary(),
                req.details()
            ));
            return;
        }

        if (req.executionTaskId() != null && !req.executionTaskId().isBlank()) {
            executionTaskService.complete(req.executionTaskId(), new ExecutionTaskCompleteRequest(req.outputSummary()));
            return;
        }

        docketEventService.record(req.docketId(), "AGENT_RESULT_REPORTED", "ROLE",
            req.roleCode() == null ? "UNKNOWN" : req.roleCode().getValue(),
            Map.of(
                "summary", req.summary() == null ? "" : req.summary(),
                "details", req.details() == null ? "" : req.details(),
                "outputSummary", req.outputSummary() == null ? "" : req.outputSummary()
            )
        );
    }

    public void handleFailure(AgentFailureCallbackRequest req) {
        if (req.executionTaskId() != null && !req.executionTaskId().isBlank()) {
            executionTaskService.block(req.executionTaskId(), new ExecutionTaskBlockRequest(
                req.reason() == null ? "Agent execution failed" : req.reason(),
                req.details()
            ));
            return;
        }

        docketEventService.record(req.docketId(), "AGENT_EXECUTION_FAILED", "ROLE",
            req.roleCode() == null ? "UNKNOWN" : req.roleCode().getValue(),
            Map.of(
                "reason", req.reason() == null ? "" : req.reason(),
                "details", req.details() == null ? "" : req.details()
            )
        );
    }
}
