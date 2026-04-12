package com.imperium.worker.recovery;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imperium.domain.entity.ExecutionTaskEntity;
import com.imperium.domain.mapper.ExecutionTaskMapper;
import com.imperium.worker.openclaw.AgentDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 恢复卡住的执行任务派发
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionDispatchRecoveryJob {

    private final ExecutionTaskMapper executionTaskMapper;
    private final AgentDispatchService agentDispatchService;

    @Value("${imperium.recovery.dispatch-timeout-sec:180}")
    private long dispatchTimeoutSec;

    @Scheduled(fixedDelayString = "${imperium.recovery.interval-ms:30000}")
    public void recoverStaleDispatches() {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusSeconds(dispatchTimeoutSec);
            List<ExecutionTaskEntity> staleTasks = executionTaskMapper.selectList(
                new LambdaQueryWrapper<ExecutionTaskEntity>()
                    .in(ExecutionTaskEntity::getStatus, List.of("DISPATCHING", "FAILED"))
                    .lt(ExecutionTaskEntity::getUpdatedAt, threshold)
            );

            if (staleTasks.isEmpty()) {
                return;
            }

            Set<String> docketIds = new HashSet<>();
            for (ExecutionTaskEntity task : staleTasks) {
                task.setStatus("PENDING");
                task.setUpdatedAt(LocalDateTime.now());
                executionTaskMapper.updateById(task);
                docketIds.add(task.getDocketId());
                log.warn("恢复执行任务到 PENDING: taskId={} docketId={}", task.getId(), task.getDocketId());
            }

            for (String docketId : docketIds) {
                agentDispatchService.dispatchPendingExecutionTasks(docketId);
            }
        } catch (Throwable ex) {
            log.warn("执行恢复任务异常：{}", ex.getMessage(), ex);
        }
    }
}
