package com.imperium.api.controller;

import com.imperium.api.model.ApiResponse;
import com.imperium.api.model.SenateOpinionRequest;
import com.imperium.api.model.SenateSessionResponse;
import com.imperium.api.service.SenateService;
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

/**
 * 元老院接口
 */
@Tag(name = "Senate", description = "元老院审议接口")
@RestController
@RequestMapping("/api/dockets/{docketId}/senate")
@RequiredArgsConstructor
public class SenateController {

    private final SenateService senateService;

    @Operation(summary = "开启元老院会话")
    @PostMapping("/open")
    public ApiResponse<SenateSessionResponse> open(@PathVariable String docketId) {
        return ApiResponse.success(senateService.openSession(docketId));
    }

    @Operation(summary = "查询元老院会话")
    @GetMapping
    public ApiResponse<SenateSessionResponse> get(@PathVariable String docketId) {
        return ApiResponse.success(senateService.getSession(docketId));
    }

    @Operation(summary = "提交元老意见")
    @PostMapping("/opinions")
    public ApiResponse<SenateSessionResponse> opinion(
        @PathVariable String docketId,
        @Valid @RequestBody SenateOpinionRequest req
    ) {
        return ApiResponse.success(senateService.submitOpinion(docketId, req));
    }
}
