package com.imperium.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imperium.api.exception.DocketException;
import com.imperium.api.model.*;
import com.imperium.domain.entity.DocketEntity;
import com.imperium.domain.entity.DocketEventEntity;
import com.imperium.domain.entity.OutboxEventEntity;
import com.imperium.domain.entity.CaesarDecisionEntity;
import com.imperium.domain.entity.TribuneReviewEntity;
import com.imperium.domain.mapper.CaesarDecisionMapper;
import com.imperium.domain.mapper.DocketEventMapper;
import com.imperium.domain.mapper.DocketMapper;
import com.imperium.domain.mapper.OutboxEventMapper;
import com.imperium.domain.mapper.TribuneReviewMapper;
import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.OperatingMode;
import com.imperium.domain.model.RoleCode;
import com.imperium.domain.statemachine.DocketStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 议案业务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocketService {

    private final DocketMapper docketMapper;
    private final DocketEventMapper docketEventMapper;
    private final OutboxEventMapper outboxEventMapper;
    private final TribuneReviewMapper tribuneReviewMapper;
    private final CaesarDecisionMapper caesarDecisionMapper;

    /**
     * 创建议案（恺撒发布法令）
     */
    @Transactional
    public DocketDetailResponse create(CreateDocketRequest req) {
        String id = generateId();
        DocketEntity entity = new DocketEntity();
        entity.setId(id);
        entity.setTitle(extractTitle(req.edictRaw()));
        entity.setMode(req.mode());
        entity.setState(DocketState.EDICT_ISSUED);
        entity.setPriority(req.priority() != null ? req.priority() : "NORMAL");
        entity.setRiskLevel("LOW");
        entity.setCurrentOwner(RoleCode.PRAECO);
        entity.setEdictRaw(req.edictRaw());
        entity.setLastProgressAt(LocalDateTime.now());
        entity.setRetryCount(0);
        entity.setEscalationLevel(0);

        docketMapper.insert(entity);

        // 写入创建事件
        recordEvent(id, "DOCKET_CREATED", "CAESAR", "CAESAR",
            Map.of("mode", req.mode().getValue(), "edictRaw", req.edictRaw()));

        log.info("议案已创建：{} mode={}", id, req.mode());
        return toDetail(entity);
    }

    /**
     * 查询议案列表
     */
    public List<DocketOverviewResponse> list(DocketState state, RoleCode owner) {
        LambdaQueryWrapper<DocketEntity> wrapper = new LambdaQueryWrapper<DocketEntity>()
            .orderByDesc(DocketEntity::getUpdatedAt);
        if (state != null) {
            wrapper.eq(DocketEntity::getState, state);
        }
        if (owner != null) {
            wrapper.eq(DocketEntity::getCurrentOwner, owner);
        }
        return docketMapper.selectList(wrapper).stream()
            .map(this::toOverview)
            .toList();
    }

    /**
     * 查询议案详情
     */
    public DocketDetailResponse get(String id) {
        DocketEntity entity = findOrThrow(id);
        return toDetail(entity);
    }

    /**
     * 查询议案时间线
     */
    public List<TimelineEventResponse> timeline(String id) {
        findOrThrow(id);
        return docketEventMapper.selectList(
                new LambdaQueryWrapper<DocketEventEntity>()
                    .eq(DocketEventEntity::getDocketId, id)
                    .orderByAsc(DocketEventEntity::getCreatedAt)
            ).stream()
            .map(event -> new TimelineEventResponse(
                event.getId(),
                event.getDocketId(),
                event.getEventType(),
                event.getActorType(),
                event.getActorId(),
                event.getPayloadJson(),
                event.getCreatedAt()
            ))
            .toList();
    }

    @Transactional
    public DocketDetailResponse tribuneApprove(String id, TribuneReviewRequest req) {
        recordTribuneReview(id, "APPROVED", req);
        return transitionByRole(id, DocketState.AWAITING_CAESAR, RoleCode.TRIBUNE, "保民官审查通过");
    }

    @Transactional
    public DocketDetailResponse tribuneReject(String id, TribuneReviewRequest req) {
        recordTribuneReview(id, "REJECTED", req);
        return transitionByRole(id, DocketState.REJECTED, RoleCode.TRIBUNE, safeText(req == null ? null : req.reason(), "保民官否决议案"));
    }

    @Transactional
    public DocketDetailResponse tribuneReturn(String id, TribuneReviewRequest req) {
        recordTribuneReview(id, "RETURNED", req);
        return transitionByRole(id, DocketState.IN_SENATE, RoleCode.TRIBUNE, safeText(req == null ? null : req.reason(), "保民官退回元老院重议"));
    }

    @Transactional
    public DocketDetailResponse caesarApprove(String id, CaesarDecisionRequest req) {
        recordCaesarDecision(id, "APPROVE", req, false);
        return transitionByRole(id, DocketState.MANDATED, RoleCode.CAESAR, safeText(req == null ? null : req.comment(), "恺撒批准议案"));
    }

    @Transactional
    public DocketDetailResponse caesarReject(String id, CaesarDecisionRequest req) {
        recordCaesarDecision(id, "RETURN_SENATE", req, false);
        return transitionByRole(id, DocketState.IN_SENATE, RoleCode.CAESAR, safeText(req == null ? null : req.comment(), "恺撒退回元老院"));
    }

    @Transactional
    public DocketDetailResponse caesarOverride(String id, CaesarDecisionRequest req) {
        recordCaesarDecision(id, "OVERRIDE", req, true);
        return transitionByRole(id, DocketState.MANDATED, RoleCode.CAESAR, safeText(req == null ? null : req.comment(), "恺撒强制批准议案"));
    }

    @Transactional
    public DocketDetailResponse caesarRestrict(String id, CaesarDecisionRequest req) {
        recordCaesarDecision(id, "RESTRICT", req, false);
        return transitionByRole(id, DocketState.MANDATED, RoleCode.CAESAR, safeText(req.comment(), "恺撒附加限制后批准"));
    }

    /**
     * 状态推进
     */
    @Transactional
    public DocketDetailResponse transition(String id, TransitionRequest req) {
        return transitionByRole(id, req.targetState(), req.actor(), req.comment());
    }

    /**
     * 暂停议案（恺撒专属）
     */
    @Transactional
    public DocketDetailResponse suspend(String id, String comment) {
        DocketEntity entity = findOrThrow(id);
        if (entity.getState().isTerminal() || entity.getState() == DocketState.SUSPENDED) {
            throw new DocketException("INVALID_OPERATION", "当前状态不允许暂停");
        }
        entity.setSuspendedFromState(entity.getState());
        entity.setState(DocketState.SUSPENDED);
        entity.setCurrentOwner(RoleCode.CAESAR);
        entity.setLastProgressAt(LocalDateTime.now());
        docketMapper.updateById(entity);

        recordEvent(id, "DOCKET_SUSPENDED", "CAESAR", "CAESAR",
            Map.of("suspendedFrom", entity.getSuspendedFromState().getValue(),
                   "comment", comment != null ? comment : ""));
        return toDetail(entity);
    }

    /**
     * 恢复已暂停议案
     */
    @Transactional
    public DocketDetailResponse resume(String id) {
        DocketEntity entity = findOrThrow(id);
        if (entity.getState() != DocketState.SUSPENDED) {
            throw new DocketException("INVALID_OPERATION", "议案未处于暂停状态");
        }
        DocketState restoreState = entity.getSuspendedFromState();
        entity.setState(restoreState);
        entity.setSuspendedFromState(null);
        entity.setCurrentOwner(resolveOwnerForState(restoreState, entity.getMode()));
        entity.setLastProgressAt(LocalDateTime.now());
        docketMapper.updateById(entity);

        recordEvent(id, "DOCKET_RESUMED", "CAESAR", "CAESAR",
            Map.of("resumedTo", restoreState.getValue()));
        return toDetail(entity);
    }

    /**
     * 撤销议案（恺撒专属）
     */
    @Transactional
    public DocketDetailResponse revoke(String id, String comment) {
        DocketEntity entity = findOrThrow(id);
        if (entity.getState().isTerminal()) {
            throw new DocketException("INVALID_OPERATION", "议案已处于终态，无法撤销");
        }
        entity.setState(DocketState.REVOKED);
        entity.setCurrentOwner(RoleCode.CAESAR);
        entity.setLastProgressAt(LocalDateTime.now());
        docketMapper.updateById(entity);

        recordEvent(id, "DOCKET_REVOKED", "CAESAR", "CAESAR",
            Map.of("comment", comment != null ? comment : ""));
        return toDetail(entity);
    }

    // ── 内部方法 ─────────────────────────────────────────────

    private DocketEntity findOrThrow(String id) {
        DocketEntity entity = docketMapper.selectById(id);
        if (entity == null) {
            throw DocketException.notFound(id);
        }
        return entity;
    }

    private DocketDetailResponse transitionByRole(String id, DocketState targetState, RoleCode actor, String comment) {
        DocketEntity entity = findOrThrow(id);

        boolean valid = DocketStateMachine.isValid(entity.getState(), targetState, actor, entity.getMode());
        if (!valid) {
            throw DocketException.invalidTransition(entity.getState().getValue(), targetState.getValue());
        }

        DocketState prevState = entity.getState();
        entity.setState(targetState);
        entity.setCurrentOwner(resolveOwnerForState(targetState, entity.getMode()));
        entity.setLastProgressAt(LocalDateTime.now());
        entity.setRetryCount(0);
        docketMapper.updateById(entity);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("from", prevState.getValue());
        payload.put("to", targetState.getValue());
        payload.put("comment", safeText(comment, ""));
        recordEvent(id, "STATE_CHANGED", "ROLE", actor.getValue(), payload);

        log.info("议案状态推进：{} {} → {} by {}", id, prevState, targetState, actor);
        return toDetail(entity);
    }

    private void recordTribuneReview(String docketId, String reviewResult, TribuneReviewRequest req) {
        TribuneReviewEntity review = new TribuneReviewEntity();
        review.setDocketId(docketId);
        review.setReviewResult(reviewResult);
        review.setReason(req == null ? null : req.reason());
        review.setNotesJson(req == null || req.notes() == null ? List.of() : req.notes());
        review.setCreatedAt(LocalDateTime.now());
        tribuneReviewMapper.insert(review);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reviewResult", reviewResult);
        payload.put("reason", safeText(req == null ? null : req.reason(), ""));
        payload.put("notes", req == null || req.notes() == null ? List.of() : req.notes());
        recordEvent(docketId, "TRIBUNE_REVIEW_MADE", "ROLE", RoleCode.TRIBUNE.getValue(), payload);
    }

    private void recordCaesarDecision(String docketId, String decisionType, CaesarDecisionRequest req, boolean isOverride) {
        CaesarDecisionEntity decision = new CaesarDecisionEntity();
        decision.setDocketId(docketId);
        decision.setDecisionType(decisionType);
        decision.setComment(req == null ? null : req.comment());
        decision.setConstraintsJson(req == null || req.constraints() == null ? List.of() : req.constraints());
        decision.setIsOverride(isOverride ? 1 : 0);
        decision.setCreatedAt(LocalDateTime.now());
        caesarDecisionMapper.insert(decision);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("decisionType", decisionType);
        payload.put("comment", safeText(req == null ? null : req.comment(), ""));
        payload.put("constraints", req == null || req.constraints() == null ? List.of() : req.constraints());
        payload.put("isOverride", isOverride);
        recordEvent(docketId, "CAESAR_DECISION_MADE", "CAESAR", RoleCode.CAESAR.getValue(), payload);
    }

    private void recordEvent(String docketId, String eventType, String actorType,
                             String actorId, Map<String, Object> payload) {
        DocketEventEntity event = new DocketEventEntity();
        event.setDocketId(docketId);
        event.setEventType(eventType);
        event.setActorType(actorType);
        event.setActorId(actorId);
        event.setPayloadJson(payload);
        event.setCreatedAt(LocalDateTime.now());
        docketEventMapper.insert(event);

        OutboxEventEntity outbox = new OutboxEventEntity();
        outbox.setAggregateType("DOCKET");
        outbox.setAggregateId(docketId);
        outbox.setTopic("imperium-domain-topic");
        outbox.setTag(eventType);
        outbox.setPayloadJson(payload);
        outbox.setPublishStatus("PENDING");
        outbox.setRetryCount(0);
        outbox.setCreatedAt(LocalDateTime.now());
        outboxEventMapper.insert(outbox);
    }

    private String generateId() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Use a random suffix to avoid collisions across restarts and future multi-node deployments.
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "IMP-%s-%s".formatted(date, suffix);
    }

    private String extractTitle(String edictRaw) {
        if (edictRaw == null || edictRaw.isBlank()) return "未命名议案";
        String trimmed = edictRaw.strip();
        return trimmed.length() > 64 ? trimmed.substring(0, 64) + "…" : trimmed;
    }

    private RoleCode resolveOwnerForState(DocketState state, OperatingMode mode) {
        return switch (state) {
            case EDICT_ISSUED, TRIAGED -> RoleCode.PRAECO;
            case IN_SENATE, DEBATING -> RoleCode.SENATOR_STRATEGOS;
            case VETO_REVIEW -> RoleCode.TRIBUNE;
            case AWAITING_CAESAR -> RoleCode.CAESAR;
            case MANDATED, DELEGATED -> RoleCode.CONSUL;
            case IN_EXECUTION -> RoleCode.LEGATUS;
            case UNDER_AUDIT -> mode == OperatingMode.TRIBUNE_LOCK ? RoleCode.TRIBUNE : RoleCode.PRAETOR;
            case ARCHIVED -> RoleCode.SCRIBA;
            case REJECTED, REVOKED, SUSPENDED -> RoleCode.CAESAR;
        };
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private DocketOverviewResponse toOverview(DocketEntity e) {
        return new DocketOverviewResponse(
            e.getId(), e.getTitle(), e.getState(), e.getMode(),
            e.getPriority(), e.getRiskLevel(), e.getCurrentOwner(),
            e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    private DocketDetailResponse toDetail(DocketEntity e) {
        return new DocketDetailResponse(
            e.getId(), e.getTitle(), e.getState(), e.getMode(),
            e.getPriority(), e.getRiskLevel(), e.getCurrentOwner(),
            e.getEdictRaw(), e.getSummary(),
            e.getLastProgressAt(), e.getRetryCount(), e.getEscalationLevel(),
            e.getCreatedAt(), e.getUpdatedAt(),
            DocketStateMachine.availableTransitions(e.getState(), e.getMode())
        );
    }
}
