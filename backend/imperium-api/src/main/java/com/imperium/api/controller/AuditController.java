package com.imperium.api.controller;

import com.imperium.api.model.ApiResponse;
import com.imperium.api.model.ArchiveRecordResponse;
import com.imperium.api.model.AuditActionRequest;
import com.imperium.api.model.AuditQueueItemResponse;
import com.imperium.api.model.AuditRecordResponse;
import com.imperium.api.service.AuditArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 审计接口
 */
@Tag(name = "Audit", description = "审计与归档接口")
@RestController
@RequiredArgsConstructor
public class AuditController {

    private final AuditArchiveService auditArchiveService;

    @Operation(summary = "查询审计队列")
    @GetMapping("/api/audit/queue")
    public ApiResponse<List<AuditQueueItemResponse>> queue() {
        return ApiResponse.success(auditArchiveService.listAuditQueue());
    }

    @Operation(summary = "查询议案审计记录")
    @GetMapping("/api/dockets/{id}/audit/records")
    public ApiResponse<List<AuditRecordResponse>> records(@PathVariable String id) {
        return ApiResponse.success(auditArchiveService.listAuditRecords(id));
    }

    @Operation(summary = "审计通过并归档")
    @PostMapping("/api/dockets/{id}/audit/pass")
    public ApiResponse<ArchiveRecordResponse> pass(
        @PathVariable String id,
        @RequestBody(required = false) AuditActionRequest req
    ) {
        return ApiResponse.success(auditArchiveService.pass(id, req));
    }

    @Operation(summary = "审计退回执行")
    @PostMapping("/api/dockets/{id}/audit/return")
    public ApiResponse<AuditRecordResponse> returnToExecution(
        @PathVariable String id,
        @RequestBody(required = false) AuditActionRequest req
    ) {
        return ApiResponse.success(auditArchiveService.returnToExecution(id, req));
    }

    @Operation(summary = "审计升级至恺撒")
    @PostMapping("/api/dockets/{id}/audit/escalate")
    public ApiResponse<AuditRecordResponse> escalate(
        @PathVariable String id,
        @RequestBody(required = false) AuditActionRequest req
    ) {
        return ApiResponse.success(auditArchiveService.escalate(id, req));
    }
}
