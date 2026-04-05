package com.imperium.api.controller;

import com.imperium.api.model.ApiResponse;
import com.imperium.api.model.ExecutionTaskBlockRequest;
import com.imperium.api.model.ExecutionTaskCompleteRequest;
import com.imperium.api.model.ExecutionTaskProgressRequest;
import com.imperium.api.model.ExecutionTaskResponse;
import com.imperium.api.service.ExecutionTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 执行任务接口
 */
@Tag(name = "ExecutionTasks", description = "执行任务接口")
@RestController
@RequiredArgsConstructor
public class ExecutionTaskController {

    private final ExecutionTaskService executionTaskService;

    @Operation(summary = "查询议案执行任务列表")
    @GetMapping("/api/dockets/{id}/execution-tasks")
    public ApiResponse<List<ExecutionTaskResponse>> listByDocket(@PathVariable String id) {
        return ApiResponse.success(executionTaskService.listByDocket(id));
    }

    @Operation(summary = "更新执行任务进度")
    @PostMapping("/api/execution-tasks/{id}/progress")
    public ApiResponse<ExecutionTaskResponse> progress(
        @PathVariable String id,
        @Valid @RequestBody ExecutionTaskProgressRequest req
    ) {
        return ApiResponse.success(executionTaskService.progress(id, req));
    }

    @Operation(summary = "阻塞执行任务")
    @PostMapping("/api/execution-tasks/{id}/block")
    public ApiResponse<ExecutionTaskResponse> block(
        @PathVariable String id,
        @Valid @RequestBody ExecutionTaskBlockRequest req
    ) {
        return ApiResponse.success(executionTaskService.block(id, req));
    }

    @Operation(summary = "完成执行任务")
    @PostMapping("/api/execution-tasks/{id}/complete")
    public ApiResponse<ExecutionTaskResponse> complete(
        @PathVariable String id,
        @Valid @RequestBody ExecutionTaskCompleteRequest req
    ) {
        return ApiResponse.success(executionTaskService.complete(id, req));
    }
}
