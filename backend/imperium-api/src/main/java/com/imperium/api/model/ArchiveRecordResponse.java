package com.imperium.api.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 归档记录响应
 */
public record ArchiveRecordResponse(
    Long id,
    String docketId,
    String title,
    String finalSummary,
    List<String> artifacts,
    LocalDateTime archivedAt
) {
}
