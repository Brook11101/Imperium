package com.imperium.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imperium.api.exception.DocketException;
import com.imperium.api.model.SenateOpinionRequest;
import com.imperium.api.model.SenateOpinionResponse;
import com.imperium.api.model.SenateSessionResponse;
import com.imperium.api.model.TransitionRequest;
import com.imperium.domain.entity.DocketEntity;
import com.imperium.domain.entity.SenateOpinionEntity;
import com.imperium.domain.entity.SenateSessionEntity;
import com.imperium.domain.mapper.DocketMapper;
import com.imperium.domain.mapper.SenateOpinionMapper;
import com.imperium.domain.mapper.SenateSessionMapper;
import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.OperatingMode;
import com.imperium.domain.model.RoleCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 元老院服务
 */
@Service
@RequiredArgsConstructor
public class SenateService {

    private static final Set<RoleCode> SENATOR_ROLES = Set.of(
        RoleCode.SENATOR_STRATEGOS,
        RoleCode.SENATOR_JURIS,
        RoleCode.SENATOR_FISCUS
    );

    private final DocketMapper docketMapper;
    private final SenateSessionMapper senateSessionMapper;
    private final SenateOpinionMapper senateOpinionMapper;
    private final DocketService docketService;
    private final DocketEventService docketEventService;

    @Transactional
    public SenateSessionResponse openSession(String docketId) {
        DocketEntity docket = requireDocket(docketId);
        requireSenateMode(docket);
        if (docket.getState() != DocketState.IN_SENATE && docket.getState() != DocketState.DEBATING) {
            throw new DocketException("INVALID_OPERATION", "当前状态不允许开启元老院会话");
        }

        SenateSessionEntity existing = findOpenSession(docketId);
        if (existing != null) {
            return toResponse(existing, listOpinions(existing.getId()));
        }

        SenateSessionEntity session = new SenateSessionEntity();
        session.setId(generateSessionId());
        session.setDocketId(docketId);
        session.setStatus("OPEN");
        session.setOpenedAt(LocalDateTime.now());
        session.setConsensusJson(List.of());
        session.setDisputesJson(List.of());
        senateSessionMapper.insert(session);

        docket.setCurrentSenateSessionId(session.getId());
        docketMapper.updateById(docket);

        docketEventService.record(docketId, "SENATE_SESSION_OPENED", "SYSTEM", "SENATE", Map.of("sessionId", session.getId()));
        return toResponse(session, List.of());
    }

    public SenateSessionResponse getSession(String docketId) {
        requireDocket(docketId);
        SenateSessionEntity session = findLatestSession(docketId);
        if (session == null) {
            throw new DocketException("SENATE_SESSION_NOT_FOUND", "当前议案尚未建立元老院会话");
        }
        return toResponse(session, listOpinions(session.getId()));
    }

    @Transactional
    public SenateSessionResponse submitOpinion(String docketId, SenateOpinionRequest req) {
        return submitOpinion(docketId, null, req);
    }

    @Transactional
    public SenateSessionResponse submitOpinion(String docketId, String expectedSessionId, SenateOpinionRequest req) {
        DocketEntity docket = requireDocket(docketId);
        requireSenateMode(docket);
        requireSenatorRole(req.roleCode());

        SenateSessionEntity session = findOpenSession(docketId);
        if (session == null) {
            session = mapToEntity(openSession(docketId));
        }
        if (expectedSessionId != null && !expectedSessionId.isBlank() && !expectedSessionId.equals(session.getId())) {
            throw new DocketException("STALE_SENATE_CALLBACK", "回调会话已过期或不匹配当前开启的元老院会话");
        }

        if (docket.getState() == DocketState.IN_SENATE) {
            docketService.transition(docketId, new TransitionRequest(DocketState.DEBATING, req.roleCode(), "元老提交首条意见，进入正式辩论"));
        }

        SenateOpinionEntity existing = senateOpinionMapper.selectOne(
            new LambdaQueryWrapper<SenateOpinionEntity>()
                .eq(SenateOpinionEntity::getSessionId, session.getId())
                .eq(SenateOpinionEntity::getAgentId, req.roleCode())
                .last("LIMIT 1")
        );

        if (existing == null) {
            existing = new SenateOpinionEntity();
            existing.setSessionId(session.getId());
            existing.setDocketId(docketId);
            existing.setAgentId(req.roleCode());
        }
        existing.setStance(normalizeStance(req.stance()));
        existing.setSummary(req.summary());
        existing.setDetails(req.details());
        existing.setGeneratedAt(LocalDateTime.now());

        if (existing.getId() == null) {
            senateOpinionMapper.insert(existing);
        } else {
            senateOpinionMapper.updateById(existing);
        }

        List<SenateOpinionEntity> opinions = listOpinions(session.getId());
        aggregateSession(session, opinions);
        senateSessionMapper.updateById(session);

        docketEventService.record(docketId, "SENATE_OPINION_RECORDED", "ROLE", req.roleCode().getValue(), Map.of(
            "sessionId", session.getId(),
            "stance", existing.getStance(),
            "summary", existing.getSummary()
        ));
        if ("CLOSED".equals(session.getStatus())) {
            docketEventService.record(docketId, "SENATE_SESSION_CLOSED", "SYSTEM", "SENATE", Map.of(
                "sessionId", session.getId(),
                "recommendedMotion", session.getRecommendedMotion() == null ? "" : session.getRecommendedMotion()
            ));
        }

        return toResponse(session, opinions);
    }

    private void aggregateSession(SenateSessionEntity session, List<SenateOpinionEntity> opinions) {
        List<String> consensus = new ArrayList<>();
        List<String> disputes = new ArrayList<>();
        List<SenateOpinionEntity> supportOrCondition = opinions.stream()
            .filter(opinion -> opinion.getStance().equals("SUPPORT") || opinion.getStance().equals("CONDITION"))
            .toList();
        if (supportOrCondition.size() >= 2) {
            consensus = supportOrCondition.stream().map(SenateOpinionEntity::getSummary).toList();
        }

        Set<String> stanceSet = new LinkedHashSet<>();
        for (SenateOpinionEntity opinion : opinions) {
            stanceSet.add(opinion.getStance());
            if (opinion.getStance().equals("OBJECT") || stanceSet.size() > 1) {
                disputes.add(opinion.getAgentId().getValue() + ": " + opinion.getSummary());
            }
        }

        session.setConsensusJson(consensus);
        session.setDisputesJson(disputes);
        if (opinions.size() < 3) {
            session.setRecommendedMotion("COLLECT_MORE_OPINIONS");
            session.setStatus("OPEN");
            session.setClosedAt(null);
        } else if (opinions.stream().anyMatch(opinion -> opinion.getStance().equals("OBJECT"))) {
            session.setRecommendedMotion("ESCALATE_TO_TRIBUNE");
            session.setStatus("CLOSED");
            session.setClosedAt(LocalDateTime.now());
        } else {
            session.setRecommendedMotion("READY_FOR_TRIBUNE");
            session.setStatus("CLOSED");
            session.setClosedAt(LocalDateTime.now());
        }
    }

    private DocketEntity requireDocket(String docketId) {
        DocketEntity docket = docketMapper.selectById(docketId);
        if (docket == null) {
            throw DocketException.notFound(docketId);
        }
        return docket;
    }

    private void requireSenateMode(DocketEntity docket) {
        if (docket.getMode() == OperatingMode.DIRECT_DECREE) {
            throw new DocketException("INVALID_OPERATION", "直接法令模式不启用元老院会话");
        }
    }

    private void requireSenatorRole(RoleCode roleCode) {
        if (!SENATOR_ROLES.contains(roleCode)) {
            throw new DocketException("INVALID_SENATOR_ROLE", "当前角色不是合法元老：" + roleCode);
        }
    }

    private SenateSessionEntity findOpenSession(String docketId) {
        return senateSessionMapper.selectOne(
            new LambdaQueryWrapper<SenateSessionEntity>()
                .eq(SenateSessionEntity::getDocketId, docketId)
                .eq(SenateSessionEntity::getStatus, "OPEN")
                .orderByDesc(SenateSessionEntity::getOpenedAt)
                .last("LIMIT 1")
        );
    }

    private SenateSessionEntity findLatestSession(String docketId) {
        return senateSessionMapper.selectOne(
            new LambdaQueryWrapper<SenateSessionEntity>()
                .eq(SenateSessionEntity::getDocketId, docketId)
                .orderByDesc(SenateSessionEntity::getOpenedAt)
                .last("LIMIT 1")
        );
    }

    private List<SenateOpinionEntity> listOpinions(String sessionId) {
        return senateOpinionMapper.selectList(
            new LambdaQueryWrapper<SenateOpinionEntity>()
                .eq(SenateOpinionEntity::getSessionId, sessionId)
                .orderByAsc(SenateOpinionEntity::getGeneratedAt)
        );
    }

    private String normalizeStance(String stance) {
        String normalized = stance == null ? "NEUTRAL" : stance.trim().toUpperCase();
        return switch (normalized) {
            case "SUPPORT", "OBJECT", "NEUTRAL", "CONDITION" -> normalized;
            default -> throw new DocketException("INVALID_STANCE", "不支持的元老立场：" + stance);
        };
    }

    private SenateSessionResponse toResponse(SenateSessionEntity session, List<SenateOpinionEntity> opinions) {
        return new SenateSessionResponse(
            session.getId(),
            session.getDocketId(),
            session.getStatus(),
            session.getRecommendedMotion(),
            session.getConsensusJson() == null ? List.of() : session.getConsensusJson(),
            session.getDisputesJson() == null ? List.of() : session.getDisputesJson(),
            opinions.stream().map(this::toOpinionResponse).toList(),
            session.getOpenedAt(),
            session.getClosedAt()
        );
    }

    private SenateOpinionResponse toOpinionResponse(SenateOpinionEntity opinion) {
        return new SenateOpinionResponse(
            opinion.getId(),
            opinion.getAgentId(),
            opinion.getStance(),
            opinion.getSummary(),
            opinion.getDetails(),
            opinion.getGeneratedAt()
        );
    }

    private SenateSessionEntity mapToEntity(SenateSessionResponse response) {
        SenateSessionEntity session = new SenateSessionEntity();
        session.setId(response.id());
        session.setDocketId(response.docketId());
        session.setStatus(response.status());
        session.setRecommendedMotion(response.recommendedMotion());
        session.setConsensusJson(response.consensus());
        session.setDisputesJson(response.disputes());
        session.setOpenedAt(response.openedAt());
        session.setClosedAt(response.closedAt());
        return session;
    }

    private String generateSessionId() {
        return "SEN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
