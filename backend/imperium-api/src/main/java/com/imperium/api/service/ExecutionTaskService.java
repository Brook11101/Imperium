package com.imperium.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imperium.api.exception.DocketException;
import com.imperium.api.model.ExecutionTaskBlockRequest;
import com.imperium.api.model.ExecutionTaskCompleteRequest;
import com.imperium.api.model.ExecutionTaskProgressRequest;
import com.imperium.api.model.ExecutionTaskResponse;
import com.imperium.api.model.TransitionRequest;
import com.imperium.domain.entity.DelegationEntity;
import com.imperium.domain.entity.DocketEntity;
import com.imperium.domain.entity.ExecutionTaskEntity;
import com.imperium.domain.mapper.DelegationMapper;
import com.imperium.domain.mapper.DocketMapper;
import com.imperium.domain.mapper.ExecutionTaskMapper;
import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.RoleCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行任务服务
 */
@Service
@RequiredArgsConstructor
public class ExecutionTaskService {

    private final ExecutionTaskMapper executionTaskMapper;
    private final DelegationMapper delegationMapper;
    private final DocketMapper docketMapper;
    private final DocketService docketService;
    private final DocketEventService docketEventService;

    public List<ExecutionTaskResponse> listByDocket(String docketId) {
        requireDocket(docketId);
        return executionTaskMapper.selectList(
                new LambdaQueryWrapper<ExecutionTaskEntity>()
                    .eq(ExecutionTaskEntity::getDocketId, docketId)
                    .orderByAsc(ExecutionTaskEntity::getUpdatedAt)
            ).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ExecutionTaskResponse progress(String taskId, ExecutionTaskProgressRequest req) {
        ExecutionTaskEntity task = requireTask(taskId);
        if ("COMPLETED".equals(task.getStatus())) {
            throw new DocketException("INVALID_OPERATION", "已完成任务不允许再更新进度");
        }

        task.setProgressPercent(req.progressPercent());
        task.setStatus(req.progressPercent() == 0 ? "PENDING" : "RUNNING");
        task.setBlockReason(null);
        if (req.outputSummary() != null && !req.outputSummary().isBlank()) {
            task.setOutputSummary(req.outputSummary());
        }
        task.setUpdatedAt(LocalDateTime.now());
        executionTaskMapper.updateById(task);

        updateDelegationStatus(task.getDelegationId(), "IN_PROGRESS", null);
        ensureExecutionStarted(task.getDocketId(), task.getRoleCode(), "执行任务开始推进");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", task.getId());
        payload.put("progressPercent", task.getProgressPercent());
        payload.put("outputSummary", safeText(task.getOutputSummary()));
        docketEventService.record(task.getDocketId(), "EXECUTION_PROGRESS_REPORTED", "ROLE", task.getRoleCode().getValue(), payload);

        return toResponse(task);
    }

    @Transactional
    public ExecutionTaskResponse block(String taskId, ExecutionTaskBlockRequest req) {
        ExecutionTaskEntity task = requireTask(taskId);
        task.setStatus("BLOCKED");
        task.setBlockReason(req.blockReason());
        if (req.outputSummary() != null && !req.outputSummary().isBlank()) {
            task.setOutputSummary(req.outputSummary());
        }
        task.setUpdatedAt(LocalDateTime.now());
        executionTaskMapper.updateById(task);

        updateDelegationStatus(task.getDelegationId(), "BLOCKED", null);
        ensureExecutionStarted(task.getDocketId(), task.getRoleCode(), "执行任务进入阻塞");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", task.getId());
        payload.put("blockReason", task.getBlockReason());
        payload.put("outputSummary", safeText(task.getOutputSummary()));
        docketEventService.record(task.getDocketId(), "EXECUTION_TASK_BLOCKED", "ROLE", task.getRoleCode().getValue(), payload);

        return toResponse(task);
    }

    @Transactional
    public ExecutionTaskResponse complete(String taskId, ExecutionTaskCompleteRequest req) {
        ExecutionTaskEntity task = requireTask(taskId);
        task.setProgressPercent(100);
        task.setStatus("COMPLETED");
        task.setBlockReason(null);
        if (req.outputSummary() != null && !req.outputSummary().isBlank()) {
            task.setOutputSummary(req.outputSummary());
        }
        task.setUpdatedAt(LocalDateTime.now());
        executionTaskMapper.updateById(task);

        updateDelegationStatus(task.getDelegationId(), "COMPLETED", LocalDateTime.now());
        ensureExecutionStarted(task.getDocketId(), task.getRoleCode(), "执行任务完成");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", task.getId());
        payload.put("outputSummary", safeText(task.getOutputSummary()));
        docketEventService.record(task.getDocketId(), "EXECUTION_TASK_COMPLETED", "ROLE", task.getRoleCode().getValue(), payload);

        moveDocketToAuditIfAllTasksCompleted(task.getDocketId());
        return toResponse(task);
    }

    private void ensureExecutionStarted(String docketId, RoleCode actor, String comment) {
        DocketEntity docket = requireDocket(docketId);
        if (docket.getState() == DocketState.DELEGATED) {
            docketService.transition(docketId, new TransitionRequest(DocketState.IN_EXECUTION, actor, comment));
        }
    }

    private void moveDocketToAuditIfAllTasksCompleted(String docketId) {
        DocketEntity docket = requireDocket(docketId);
        if (docket.getState() != DocketState.IN_EXECUTION && docket.getState() != DocketState.DELEGATED) {
            return;
        }

        long total = executionTaskMapper.selectCount(
            new LambdaQueryWrapper<ExecutionTaskEntity>()
                .eq(ExecutionTaskEntity::getDocketId, docketId)
        );
        if (total == 0) {
            return;
        }

        long completed = executionTaskMapper.selectCount(
            new LambdaQueryWrapper<ExecutionTaskEntity>()
                .eq(ExecutionTaskEntity::getDocketId, docketId)
                .eq(ExecutionTaskEntity::getStatus, "COMPLETED")
        );
        if (completed == total) {
            docketService.transition(docketId, new TransitionRequest(DocketState.UNDER_AUDIT, RoleCode.CONSUL, "所有执行任务已完成，进入审计"));
        }
    }

    private void updateDelegationStatus(String delegationId, String status, LocalDateTime completedAt) {
        DelegationEntity delegation = delegationMapper.selectById(delegationId);
        if (delegation == null) {
            return;
        }
        delegation.setStatus(status);
        if (completedAt != null) {
            delegation.setCompletedAt(completedAt);
        }
        delegationMapper.updateById(delegation);
    }

    private DocketEntity requireDocket(String docketId) {
        DocketEntity docket = docketMapper.selectById(docketId);
        if (docket == null) {
            throw DocketException.notFound(docketId);
        }
        return docket;
    }

    private ExecutionTaskEntity requireTask(String taskId) {
        ExecutionTaskEntity task = executionTaskMapper.selectById(taskId);
        if (task == null) {
            throw new DocketException("EXECUTION_TASK_NOT_FOUND", "执行任务不存在：" + taskId);
        }
        return task;
    }

    private ExecutionTaskResponse toResponse(ExecutionTaskEntity task) {
        return new ExecutionTaskResponse(
            task.getId(),
            task.getDocketId(),
            task.getDelegationId(),
            task.getRoleCode(),
            task.getProgressPercent(),
            task.getStatus(),
            task.getBlockReason(),
            task.getOutputSummary(),
            task.getUpdatedAt()
        );
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }
}
