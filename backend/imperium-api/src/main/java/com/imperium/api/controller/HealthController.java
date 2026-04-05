package com.imperium.api.controller;

import com.imperium.api.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health", description = "健康检查")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success(Map.of(
                "service", "imperium-api",
                "status", "UP",
                "timestamp", OffsetDateTime.now().toString()
        ));
    }
}
