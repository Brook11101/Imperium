package com.imperium.api.model;

import java.util.List;

/**
 * 审计动作请求
 */
public record AuditActionRequest(
    List<String> riskNotes,
    List<String> qualityNotes,
    String finalSummary,
    List<String> artifacts
) {
}
