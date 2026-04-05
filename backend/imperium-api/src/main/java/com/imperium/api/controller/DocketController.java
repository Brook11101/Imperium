package com.imperium.api.controller;

import com.imperium.api.model.*;
import com.imperium.api.service.DocketService;
import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.RoleCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 议案接口
 * 对应技术文档：05-前后端接口与页面技术方案.md §6.1
 */
@Tag(name = "Dockets", description = "议案管理接口")
@RestController
@RequestMapping("/api/dockets")
@RequiredArgsConstructor
public class DocketController {

    private final DocketService docketService;

    @Operation(summary = "创建议案（恺撒发布法令）")
    @PostMapping
    public ApiResponse<DocketDetailResponse> create(@Valid @RequestBody CreateDocketRequest req) {
        return ApiResponse.success(docketService.create(req));
    }

    @Operation(summary = "查询议案列表")
    @GetMapping
    public ApiResponse<List<DocketOverviewResponse>> list(
        @RequestParam(required = false) DocketState state,
        @RequestParam(required = false) RoleCode owner
    ) {
        return ApiResponse.success(docketService.list(state, owner));
    }

    @Operation(summary = "查询议案详情")
    @GetMapping("/{id}")
    public ApiResponse<DocketDetailResponse> get(@PathVariable String id) {
        return ApiResponse.success(docketService.get(id));
    }

    @Operation(summary = "查询议案时间线")
    @GetMapping("/{id}/timeline")
    public ApiResponse<List<TimelineEventResponse>> timeline(@PathVariable String id) {
        return ApiResponse.success(docketService.timeline(id));
    }

    @Operation(summary = "保民官审查通过")
    @PostMapping("/{id}/tribune/approve")
    public ApiResponse<DocketDetailResponse> tribuneApprove(
        @PathVariable String id,
        @RequestBody(required = false) TribuneReviewRequest req
    ) {
        return ApiResponse.success(docketService.tribuneApprove(id, req));
    }

    @Operation(summary = "保民官否决议案")
    @PostMapping("/{id}/tribune/reject")
    public ApiResponse<DocketDetailResponse> tribuneReject(
        @PathVariable String id,
        @RequestBody(required = false) TribuneReviewRequest req
    ) {
        return ApiResponse.success(docketService.tribuneReject(id, req));
    }

    @Operation(summary = "保民官退回元老院重议")
    @PostMapping("/{id}/tribune/return")
    public ApiResponse<DocketDetailResponse> tribuneReturn(
        @PathVariable String id,
        @RequestBody(required = false) TribuneReviewRequest req
    ) {
        return ApiResponse.success(docketService.tribuneReturn(id, req));
    }

    @Operation(summary = "恺撒批准议案")
    @PostMapping("/{id}/caesar/approve")
    public ApiResponse<DocketDetailResponse> caesarApprove(
        @PathVariable String id,
        @RequestBody(required = false) CaesarDecisionRequest req
    ) {
        return ApiResponse.success(docketService.caesarApprove(id, req));
    }

    @Operation(summary = "恺撒退回元老院")
    @PostMapping("/{id}/caesar/reject")
    public ApiResponse<DocketDetailResponse> caesarReject(
        @PathVariable String id,
        @RequestBody(required = false) CaesarDecisionRequest req
    ) {
        return ApiResponse.success(docketService.caesarReject(id, req));
    }

    @Operation(summary = "恺撒强制批准议案")
    @PostMapping("/{id}/caesar/override")
    public ApiResponse<DocketDetailResponse> caesarOverride(
        @PathVariable String id,
        @RequestBody(required = false) CaesarDecisionRequest req
    ) {
        return ApiResponse.success(docketService.caesarOverride(id, req));
    }

    @Operation(summary = "恺撒附加限制后批准")
    @PostMapping("/{id}/caesar/restrict")
    public ApiResponse<DocketDetailResponse> caesarRestrict(
        @PathVariable String id,
        @Valid @RequestBody CaesarDecisionRequest req
    ) {
        return ApiResponse.success(docketService.caesarRestrict(id, req));
    }

    @Operation(summary = "推进议案状态")
    @PostMapping("/{id}/transition")
    public ApiResponse<DocketDetailResponse> transition(
        @PathVariable String id,
        @Valid @RequestBody TransitionRequest req
    ) {
        return ApiResponse.success(docketService.transition(id, req));
    }

    @Operation(summary = "暂停议案（恺撒专属）")
    @PostMapping("/{id}/suspend")
    public ApiResponse<DocketDetailResponse> suspend(
        @PathVariable String id,
        @RequestParam(required = false) String comment
    ) {
        return ApiResponse.success(docketService.suspend(id, comment));
    }

    @Operation(summary = "恢复已暂停议案")
    @PostMapping("/{id}/resume")
    public ApiResponse<DocketDetailResponse> resume(@PathVariable String id) {
        return ApiResponse.success(docketService.resume(id));
    }

    @Operation(summary = "撤销议案（恺撒专属）")
    @PostMapping("/{id}/revoke")
    public ApiResponse<DocketDetailResponse> revoke(
        @PathVariable String id,
        @RequestParam(required = false) String comment
    ) {
        return ApiResponse.success(docketService.revoke(id, comment));
    }
}
