package com.imperium.api.model;

import java.util.List;

/**
 * 恺撒裁决请求
 */
public record CaesarDecisionRequest(
    String comment,
    List<String> constraints
) {
}
