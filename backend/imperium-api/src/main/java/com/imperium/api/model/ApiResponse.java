package com.imperium.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一 API 响应结构
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String code, String message, T data, String traceId) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("OK", "success", data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null, null);
    }
}
