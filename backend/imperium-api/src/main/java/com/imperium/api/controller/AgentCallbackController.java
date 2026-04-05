package com.imperium.api.controller;

import com.imperium.api.model.AgentFailureCallbackRequest;
import com.imperium.api.model.AgentProgressCallbackRequest;
import com.imperium.api.model.AgentResultCallbackRequest;
import com.imperium.api.model.ApiResponse;
import com.imperium.api.service.AgentCallbackService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Agent 内部回调接口
 */
@Hidden
@RestController
@RequestMapping("/internal/agent-callback")
@RequiredArgsConstructor
public class AgentCallbackController {

    private final AgentCallbackService agentCallbackService;

    @Value("${imperium.agent.callback-secret:dev-secret-change-in-prod}")
    private String callbackSecret;

    @PostMapping("/progress")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> progress(
        @RequestHeader("X-Agent-Callback-Secret") String secret,
        @RequestBody AgentProgressCallbackRequest req
    ) {
        verifySecret(secret);
        agentCallbackService.handleProgress(req);
        return ApiResponse.success(null);
    }

    @PostMapping("/result")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> result(
        @RequestHeader("X-Agent-Callback-Secret") String secret,
        @RequestBody AgentResultCallbackRequest req
    ) {
        verifySecret(secret);
        agentCallbackService.handleResult(req);
        return ApiResponse.success(null);
    }

    @PostMapping("/fail")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> fail(
        @RequestHeader("X-Agent-Callback-Secret") String secret,
        @RequestBody AgentFailureCallbackRequest req
    ) {
        verifySecret(secret);
        agentCallbackService.handleFailure(req);
        return ApiResponse.success(null);
    }

    private void verifySecret(String secret) {
        if (!callbackSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid callback secret");
        }
    }
}
