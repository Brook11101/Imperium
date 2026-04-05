package com.imperium.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.imperium.api.exception.DocketException;
import com.imperium.api.model.*;
import com.imperium.domain.entity.DocketEntity;
import com.imperium.domain.entity.DocketEventEntity;
import com.imperium.domain.mapper.DocketEventMapper;
import com.imperium.domain.mapper.DocketMapper;
import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.RoleCode;
import com.imperium.domain.statemachine.DocketStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 议案业务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocketService {

    private final DocketMapper docketMapper;
    private final DocketEventMapper docketEventMapper;

    // 简单的当日序号计数器（生产环境应改用数据库序列）
    private static final AtomicInteger dailySeq = new AtomicInteger(0);

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
     * 状态推进
     */
    @Transactional
    public DocketDetailResponse transition(String id, TransitionRequest req) {
        DocketEntity entity = findOrThrow(id);

        boolean valid = DocketStateMachine.isValid(
            entity.getState(), req.targetState(), req.actor(), entity.getMode());

        if (!valid) {
            throw DocketException.invalidTransition(
                entity.getState().getValue(), req.targetState().getValue());
        }

        DocketState prevState = entity.getState();
        entity.setState(req.targetState());
        entity.setCurrentOwner(resolveNextOwner(req.targetState()));
        entity.setLastProgressAt(LocalDateTime.now());
        entity.setRetryCount(0);
        docketMapper.updateById(entity);

        recordEvent(id, "STATE_CHANGED", "ROLE", req.actor().getValue(),
            Map.of("from", prevState.getValue(),
                   "to", req.targetState().getValue(),
                   "comment", req.comment() != null ? req.comment() : ""));

        log.info("议案状态推进：{} {} → {} by {}", id, prevState, req.targetState(), req.actor());
        return toDetail(entity);
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
    }

    private String generateId() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = dailySeq.incrementAndGet() % 1000;
        return "IMP-%s-%03d".formatted(date, seq);
    }

    private String extractTitle(String edictRaw) {
        if (edictRaw == null || edictRaw.isBlank()) return "未命名议案";
        String trimmed = edictRaw.strip();
        return trimmed.length() > 64 ? trimmed.substring(0, 64) + "…" : trimmed;
    }

    private RoleCode resolveNextOwner(DocketState state) {
        return switch (state) {
            case EDICT_ISSUED -> RoleCode.PRAECO;
            case TRIAGED, IN_SENATE, DEBATING -> RoleCode.SENATOR_STRATEGOS;
            case VETO_REVIEW -> RoleCode.TRIBUNE;
            case AWAITING_CAESAR -> RoleCode.CAESAR;
            case MANDATED, DELEGATED -> RoleCode.CONSUL;
            case IN_EXECUTION -> RoleCode.LEGATUS;
            case UNDER_AUDIT -> RoleCode.PRAETOR;
            case ARCHIVED -> RoleCode.SCRIBA;
            default -> RoleCode.CAESAR;
        };
    }

    private DocketOverviewResponse toOverview(DocketEntity e) {
        return new DocketOverviewResponse(
            e.getId(), e.getTitle(), e.getState(), e.getMode(),
            e.getPriority(), e.getRiskLevel(), e.getCurrentOwner(),
            e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    private DocketDetailResponse toDetail(DocketEntity e) {
        List<DocketState> available = DocketStateMachine.availableTransitions(
            e.getState(), e.getCurrentOwner(), e.getMode());
        return new DocketDetailResponse(
            e.getId(), e.getTitle(), e.getState(), e.getMode(),
            e.getPriority(), e.getRiskLevel(), e.getCurrentOwner(),
            e.getEdictRaw(), e.getSummary(),
            e.getLastProgressAt(), e.getRetryCount(), e.getEscalationLevel(),
            e.getCreatedAt(), e.getUpdatedAt(), available
        );
    }
}
