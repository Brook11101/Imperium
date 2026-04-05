package com.imperium.api.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计记录响应
 */
public record AuditRecordResponse(
    Long id,
    String docketId,
    String auditResult,
    List<String> riskNotes,
    List<String> qualityNotes,
    LocalDateTime createdAt
) {
}
