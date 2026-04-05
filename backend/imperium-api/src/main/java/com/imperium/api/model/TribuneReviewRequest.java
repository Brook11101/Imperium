package com.imperium.api.model;

import java.util.List;

/**
 * 保民官审查请求
 */
public record TribuneReviewRequest(
    String reason,
    List<String> notes
) {
}
