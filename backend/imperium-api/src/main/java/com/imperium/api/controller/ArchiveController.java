package com.imperium.api.controller;

import com.imperium.api.model.ApiResponse;
import com.imperium.api.model.ArchiveRecordResponse;
import com.imperium.api.service.AuditArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 归档接口
 */
@Tag(name = "Archive", description = "归档接口")
@RestController
@RequestMapping("/api/archives")
@RequiredArgsConstructor
public class ArchiveController {

    private final AuditArchiveService auditArchiveService;

    @Operation(summary = "查询归档列表")
    @GetMapping
    public ApiResponse<List<ArchiveRecordResponse>> list() {
        return ApiResponse.success(auditArchiveService.listArchives());
    }

    @Operation(summary = "查询归档详情")
    @GetMapping("/{docketId}")
    public ApiResponse<ArchiveRecordResponse> detail(@PathVariable String docketId) {
        return ApiResponse.success(auditArchiveService.getArchive(docketId));
    }
}
