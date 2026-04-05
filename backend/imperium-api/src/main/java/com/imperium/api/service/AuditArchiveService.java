package com.imperium.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imperium.api.exception.DocketException;
import com.imperium.api.model.ArchiveRecordResponse;
import com.imperium.api.model.AuditActionRequest;
import com.imperium.api.model.AuditQueueItemResponse;
import com.imperium.api.model.AuditRecordResponse;
import com.imperium.api.model.TransitionRequest;
import com.imperium.domain.entity.ArchiveRecordEntity;
import com.imperium.domain.entity.AuditRecordEntity;
import com.imperium.domain.entity.DocketEntity;
import com.imperium.domain.mapper.ArchiveRecordMapper;
import com.imperium.domain.mapper.AuditRecordMapper;
import com.imperium.domain.mapper.DocketMapper;
import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.RoleCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计与归档服务
 */
@Service
@RequiredArgsConstructor
public class AuditArchiveService {

    private final DocketMapper docketMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final ArchiveRecordMapper archiveRecordMapper;
    private final DocketService docketService;
    private final DocketEventService docketEventService;

    public List<AuditQueueItemResponse> listAuditQueue() {
        return docketMapper.selectList(
                new LambdaQueryWrapper<DocketEntity>()
                    .eq(DocketEntity::getState, DocketState.UNDER_AUDIT)
                    .orderByDesc(DocketEntity::getUpdatedAt)
            ).stream()
            .map(this::toAuditQueueItem)
            .toList();
    }

    public List<AuditRecordResponse> listAuditRecords(String docketId) {
        requireDocket(docketId);
        return auditRecordMapper.selectList(
                new LambdaQueryWrapper<AuditRecordEntity>()
                    .eq(AuditRecordEntity::getDocketId, docketId)
                    .orderByAsc(AuditRecordEntity::getCreatedAt)
            ).stream()
            .map(this::toAuditRecord)
            .toList();
    }

    public List<ArchiveRecordResponse> listArchives() {
        return archiveRecordMapper.selectList(
                new LambdaQueryWrapper<ArchiveRecordEntity>()
                    .orderByDesc(ArchiveRecordEntity::getArchivedAt)
            ).stream()
            .map(this::toArchiveRecord)
            .toList();
    }

    public ArchiveRecordResponse getArchive(String docketId) {
        ArchiveRecordEntity archive = archiveRecordMapper.selectOne(
            new LambdaQueryWrapper<ArchiveRecordEntity>()
                .eq(ArchiveRecordEntity::getDocketId, docketId)
                .last("LIMIT 1")
        );
        if (archive == null) {
            throw new DocketException("ARCHIVE_NOT_FOUND", "未找到议案归档记录：" + docketId);
        }
        return toArchiveRecord(archive);
    }

    @Transactional
    public ArchiveRecordResponse pass(String docketId, AuditActionRequest req) {
        DocketEntity docket = requireAuditState(docketId);

        AuditRecordEntity record = createAuditRecord(docketId, "PASS", req);
        auditRecordMapper.insert(record);

        ArchiveRecordEntity archive = archiveRecordMapper.selectOne(
            new LambdaQueryWrapper<ArchiveRecordEntity>()
                .eq(ArchiveRecordEntity::getDocketId, docketId)
                .last("LIMIT 1")
        );
        if (archive == null) {
            archive = new ArchiveRecordEntity();
            archive.setDocketId(docketId);
        }
        archive.setFinalSummary(req != null && req.finalSummary() != null && !req.finalSummary().isBlank()
            ? req.finalSummary()
            : docket.getSummary());
        archive.setArtifactsJson(req != null && req.artifacts() != null ? req.artifacts() : List.of());
        archive.setArchivedAt(LocalDateTime.now());

        if (archive.getId() == null) {
            archiveRecordMapper.insert(archive);
        } else {
            archiveRecordMapper.updateById(archive);
        }

        docketEventService.record(docketId, "AUDIT_PASSED", "ROLE", RoleCode.PRAETOR.getValue(), buildAuditPayload(record, req));
        docketEventService.record(docketId, "ARCHIVE_WRITTEN", "ROLE", RoleCode.SCRIBA.getValue(), Map.of(
            "finalSummary", archive.getFinalSummary() == null ? "" : archive.getFinalSummary(),
            "artifacts", archive.getArtifactsJson() == null ? List.of() : archive.getArtifactsJson()
        ));

        docketService.transition(docketId, new TransitionRequest(DocketState.ARCHIVED, RoleCode.SCRIBA, "审计通过并归档"));
        return toArchiveRecord(archive);
    }

    @Transactional
    public AuditRecordResponse returnToExecution(String docketId, AuditActionRequest req) {
        requireAuditState(docketId);
        AuditRecordEntity record = createAuditRecord(docketId, "RETURN", req);
        auditRecordMapper.insert(record);
        docketEventService.record(docketId, "AUDIT_RETURNED", "ROLE", RoleCode.PRAETOR.getValue(), buildAuditPayload(record, req));
        docketService.transition(docketId, new TransitionRequest(DocketState.IN_EXECUTION, RoleCode.PRAETOR, "审计退回执行层"));
        return toAuditRecord(record);
    }

    @Transactional
    public AuditRecordResponse escalate(String docketId, AuditActionRequest req) {
        requireAuditState(docketId);
        AuditRecordEntity record = createAuditRecord(docketId, "ESCALATE", req);
        auditRecordMapper.insert(record);
        docketEventService.record(docketId, "AUDIT_ESCALATED", "ROLE", RoleCode.PRAETOR.getValue(), buildAuditPayload(record, req));
        docketService.transition(docketId, new TransitionRequest(DocketState.AWAITING_CAESAR, RoleCode.PRAETOR, "审计升级至恺撒裁决"));
        return toAuditRecord(record);
    }

    private AuditRecordEntity createAuditRecord(String docketId, String result, AuditActionRequest req) {
        AuditRecordEntity record = new AuditRecordEntity();
        record.setDocketId(docketId);
        record.setAuditResult(result);
        record.setRiskNotesJson(req != null && req.riskNotes() != null ? req.riskNotes() : List.of());
        record.setQualityNotesJson(req != null && req.qualityNotes() != null ? req.qualityNotes() : List.of());
        record.setCreatedAt(LocalDateTime.now());
        return record;
    }

    private DocketEntity requireAuditState(String docketId) {
        DocketEntity docket = requireDocket(docketId);
        if (docket.getState() != DocketState.UNDER_AUDIT) {
            throw new DocketException("INVALID_OPERATION", "当前议案未处于审计状态");
        }
        return docket;
    }

    private DocketEntity requireDocket(String docketId) {
        DocketEntity docket = docketMapper.selectById(docketId);
        if (docket == null) {
            throw DocketException.notFound(docketId);
        }
        return docket;
    }

    private Map<String, Object> buildAuditPayload(AuditRecordEntity record, AuditActionRequest req) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("auditResult", record.getAuditResult());
        payload.put("riskNotes", req != null && req.riskNotes() != null ? req.riskNotes() : List.of());
        payload.put("qualityNotes", req != null && req.qualityNotes() != null ? req.qualityNotes() : List.of());
        payload.put("finalSummary", req != null && req.finalSummary() != null ? req.finalSummary() : "");
        payload.put("artifacts", req != null && req.artifacts() != null ? req.artifacts() : List.of());
        return payload;
    }

    private AuditQueueItemResponse toAuditQueueItem(DocketEntity entity) {
        return new AuditQueueItemResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getMode().getValue(),
            entity.getPriority(),
            entity.getRiskLevel(),
            entity.getCurrentOwner(),
            entity.getUpdatedAt()
        );
    }

    private AuditRecordResponse toAuditRecord(AuditRecordEntity entity) {
        return new AuditRecordResponse(
            entity.getId(),
            entity.getDocketId(),
            entity.getAuditResult(),
            entity.getRiskNotesJson() == null ? List.of() : entity.getRiskNotesJson(),
            entity.getQualityNotesJson() == null ? List.of() : entity.getQualityNotesJson(),
            entity.getCreatedAt()
        );
    }

    private ArchiveRecordResponse toArchiveRecord(ArchiveRecordEntity entity) {
        DocketEntity docket = docketMapper.selectById(entity.getDocketId());
        return new ArchiveRecordResponse(
            entity.getId(),
            entity.getDocketId(),
            docket != null ? docket.getTitle() : entity.getDocketId(),
            entity.getFinalSummary(),
            entity.getArtifactsJson() == null ? List.of() : entity.getArtifactsJson(),
            entity.getArchivedAt()
        );
    }
}
