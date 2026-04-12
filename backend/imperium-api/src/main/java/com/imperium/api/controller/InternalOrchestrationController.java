package com.imperium.api.controller;

import com.imperium.api.model.ApiResponse;
import com.imperium.api.service.WorkflowAutomationService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Worker 内部编排接口
 */
@Hidden
@RestController
@RequestMapping("/internal/orchestration/dockets")
@RequiredArgsConstructor
public class InternalOrchestrationController {

    private final WorkflowAutomationService workflowAutomationService;

    @Value("${imperium.internal.secret:${imperium.agent.callback-secret:dev-secret-change-in-prod}}")
    private String internalSecret;

    @PostMapping("/{id}/triage")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> triage(
        @RequestHeader("X-Imperium-Internal-Secret") String secret,
        @PathVariable String id
    ) {
        verifySecret(secret);
        workflowAutomationService.autoTriage(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/finalize-senate")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> finalizeSenate(
        @RequestHeader("X-Imperium-Internal-Secret") String secret,
        @PathVariable String id
    ) {
        verifySecret(secret);
        workflowAutomationService.finalizeSenate(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/auto-caesar-approve")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> autoCaesarApprove(
        @RequestHeader("X-Imperium-Internal-Secret") String secret,
        @PathVariable String id
    ) {
        verifySecret(secret);
        workflowAutomationService.autoCaesarApprove(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/auto-delegate")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> autoDelegate(
        @RequestHeader("X-Imperium-Internal-Secret") String secret,
        @PathVariable String id
    ) {
        verifySecret(secret);
        workflowAutomationService.autoDelegate(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/auto-complete-execution")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> autoCompleteExecution(
        @RequestHeader("X-Imperium-Internal-Secret") String secret,
        @PathVariable String id
    ) {
        verifySecret(secret);
        workflowAutomationService.autoCompleteExecution(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/auto-pass-audit")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> autoPassAudit(
        @RequestHeader("X-Imperium-Internal-Secret") String secret,
        @PathVariable String id
    ) {
        verifySecret(secret);
        workflowAutomationService.autoPassAudit(id);
        return ApiResponse.success(null);
    }

    private void verifySecret(String secret) {
        if (!internalSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid internal secret");
        }
    }
}
