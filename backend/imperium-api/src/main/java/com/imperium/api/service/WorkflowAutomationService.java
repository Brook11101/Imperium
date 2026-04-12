package com.imperium.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imperium.api.exception.DocketException;
import com.imperium.api.model.TransitionRequest;
import com.imperium.domain.entity.DocketEntity;
import com.imperium.domain.entity.SenateSessionEntity;
import com.imperium.domain.mapper.DocketMapper;
import com.imperium.domain.mapper.SenateSessionMapper;
import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.OperatingMode;
import com.imperium.domain.model.RoleCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统自动编排服务
 */
@Service
@RequiredArgsConstructor
public class WorkflowAutomationService {

    private final DocketMapper docketMapper;
    private final SenateSessionMapper senateSessionMapper;
    private final DocketService docketService;

    @Transactional
    public void autoTriage(String docketId) {
        DocketEntity docket = requireDocket(docketId);
        if (docket.getState() != DocketState.EDICT_ISSUED) {
            return;
        }

        docket.setSummary(buildSummary(docket.getEdictRaw()));
        docketMapper.updateById(docket);

        docketService.transition(docketId, new TransitionRequest(DocketState.TRIAGED, RoleCode.PRAECO, "自动完成传令官分流与规范化处理"));
        if (docket.getMode() == OperatingMode.DIRECT_DECREE) {
            docketService.transition(docketId, new TransitionRequest(DocketState.AWAITING_CAESAR, RoleCode.PRAECO, "直接法令模式，自动跳过元老院"));
        } else {
            docketService.transition(docketId, new TransitionRequest(DocketState.IN_SENATE, RoleCode.PRAECO, "自动送入元老院议程"));
        }
    }

    @Transactional
    public void finalizeSenate(String docketId) {
        DocketEntity docket = requireDocket(docketId);
        if (docket.getState() != DocketState.DEBATING && docket.getState() != DocketState.IN_SENATE) {
            return;
        }

        SenateSessionEntity session = senateSessionMapper.selectOne(
            new LambdaQueryWrapper<SenateSessionEntity>()
                .eq(SenateSessionEntity::getDocketId, docketId)
                .eq(SenateSessionEntity::getStatus, "CLOSED")
                .orderByDesc(SenateSessionEntity::getOpenedAt)
                .last("LIMIT 1")
        );
        if (session == null) {
            return;
        }

        docketService.transition(docketId, new TransitionRequest(DocketState.VETO_REVIEW, RoleCode.TRIBUNE, "元老院会话已闭合，自动进入保民官审查"));
    }

    private DocketEntity requireDocket(String docketId) {
        DocketEntity docket = docketMapper.selectById(docketId);
        if (docket == null) {
            throw DocketException.notFound(docketId);
        }
        return docket;
    }

    private String buildSummary(String edictRaw) {
        if (edictRaw == null || edictRaw.isBlank()) {
            return "";
        }
        String normalized = edictRaw.strip().replace('\n', ' ');
        return normalized.length() > 160 ? normalized.substring(0, 160) + "…" : normalized;
    }
}
