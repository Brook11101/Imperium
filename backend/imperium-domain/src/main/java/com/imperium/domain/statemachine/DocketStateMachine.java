package com.imperium.domain.statemachine;

import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.OperatingMode;
import com.imperium.domain.model.RoleCode;

import java.util.List;
import java.util.Set;

import static com.imperium.domain.model.DocketState.*;
import static com.imperium.domain.model.OperatingMode.*;
import static com.imperium.domain.model.RoleCode.*;

/**
 * 议案状态机
 * <p>
 * 实现技术文档：02-领域模型与状态机实现.md §5
 * 采用 Enum + Rule 组合，不引入外部状态机框架。
 * <p>
 * 流转规则来源：产品文档 03-运行模式与状态机.md §5
 */
public final class DocketStateMachine {

    private static final List<DocketTransition> TRANSITIONS = List.of(

        // ── 传令官处理 ────────────────────────────────────────
        DocketTransition.builder()
            .from(EDICT_ISSUED).to(TRIAGED)
            .allowedActors(Set.of(PRAECO))
            .description("传令官完成规范化处理").build(),

        // ── 进入元老院（标准议政 / 保民官锁定模式）────────────
        DocketTransition.builder()
            .from(TRIAGED).to(IN_SENATE)
            .allowedActors(Set.of(PRAECO))
            .allowedModes(Set.of(STANDARD_SENATE, TRIBUNE_LOCK))
            .description("议案进入元老院").build(),

        // ── 直接法令快速路径 ──────────────────────────────────
        DocketTransition.builder()
            .from(TRIAGED).to(AWAITING_CAESAR)
            .allowedActors(Set.of(PRAECO))
            .allowedModes(Set.of(DIRECT_DECREE))
            .description("直接法令跳过元老院，等待恺撒确认").build(),

        // ── 元老院辩论 ────────────────────────────────────────
        DocketTransition.builder()
            .from(IN_SENATE).to(DEBATING)
            .allowedActors(Set.of(SENATOR_STRATEGOS, SENATOR_JURIS, SENATOR_FISCUS))
            .description("元老院开始辩论").build(),

        // ── 保民官审查 ────────────────────────────────────────
        DocketTransition.builder()
            .from(DEBATING).to(VETO_REVIEW)
            .allowedActors(Set.of(TRIBUNE))
            .description("进入保民官审查").build(),

        // ── 保民官放行 → 等待恺撒 ─────────────────────────────
        DocketTransition.builder()
            .from(VETO_REVIEW).to(AWAITING_CAESAR)
            .allowedActors(Set.of(TRIBUNE))
            .description("保民官审查通过，呈恺撒裁决").build(),

        // ── 保民官退回重议 ────────────────────────────────────
        DocketTransition.builder()
            .from(VETO_REVIEW).to(IN_SENATE)
            .allowedActors(Set.of(TRIBUNE))
            .description("保民官退回元老院重议").build(),

        // ── 保民官否决 ────────────────────────────────────────
        DocketTransition.builder()
            .from(VETO_REVIEW).to(REJECTED)
            .allowedActors(Set.of(TRIBUNE))
            .description("保民官否决议案").build(),

        // ── 恺撒批准 → 授权 ──────────────────────────────────
        DocketTransition.builder()
            .from(AWAITING_CAESAR).to(MANDATED)
            .allowedActors(Set.of(CAESAR))
            .description("恺撒批准，议案获得授权").build(),

        // ── 恺撒退回元老院 ────────────────────────────────────
        DocketTransition.builder()
            .from(AWAITING_CAESAR).to(IN_SENATE)
            .allowedActors(Set.of(CAESAR))
            .description("恺撒退回元老院重议").build(),

        // ── 恺撒撤销 ─────────────────────────────────────────
        DocketTransition.builder()
            .from(AWAITING_CAESAR).to(REVOKED)
            .allowedActors(Set.of(CAESAR))
            .description("恺撒撤销议案").build(),

        // ── 执政官派发 ────────────────────────────────────────
        DocketTransition.builder()
            .from(MANDATED).to(DELEGATED)
            .allowedActors(Set.of(CONSUL))
            .description("执政官完成派发").build(),

        // ── 开始执行 ──────────────────────────────────────────
        DocketTransition.builder()
            .from(DELEGATED).to(IN_EXECUTION)
            .allowedActors(Set.of(CONSUL, LEGATUS, PRAETOR, AEDILE, QUAESTOR, GOVERNOR))
            .description("执行角色开始执行").build(),

        // ── 执行完成，进入审计 ────────────────────────────────
        DocketTransition.builder()
            .from(IN_EXECUTION).to(UNDER_AUDIT)
            .allowedActors(Set.of(CONSUL, PRAETOR, SCRIBA))
            .description("执行完成，进入审计").build(),

        // ── 审计通过，归档 ────────────────────────────────────
        DocketTransition.builder()
            .from(UNDER_AUDIT).to(ARCHIVED)
            .allowedActors(Set.of(PRAETOR, SCRIBA))
            .description("审计通过，议案归档").build(),

        // ── 审计回流，重新执行 ────────────────────────────────
        DocketTransition.builder()
            .from(UNDER_AUDIT).to(IN_EXECUTION)
            .allowedActors(Set.of(PRAETOR, SCRIBA))
            .description("审计发现问题，退回执行层整改").build(),

        // ── 审计升级至恺撒 ────────────────────────────────────
        DocketTransition.builder()
            .from(UNDER_AUDIT).to(AWAITING_CAESAR)
            .allowedActors(Set.of(PRAETOR, SCRIBA, TRIBUNE))
            .description("审计发现战略性争议，升级至恺撒").build()
    );

    private DocketStateMachine() {
    }

    /**
     * 校验状态流转是否合法
     *
     * @param from   当前状态
     * @param to     目标状态
     * @param actor  操作角色
     * @param mode   运行模式
     * @return true 表示合法
     */
    public static boolean isValid(DocketState from, DocketState to, RoleCode actor, OperatingMode mode) {
        if (from.isTerminal()) {
            return false;
        }
        // 暂停 → 任意非终态（恢复）由调用方单独处理
        if (from == SUSPENDED) {
            return false;
        }
        // 任意非终态 → 暂停 / 撤销（由恺撒专属处理）
        if (to == SUSPENDED || to == REVOKED) {
            return actor == CAESAR;
        }
        return TRANSITIONS.stream().anyMatch(t ->
            t.getFrom() == from
                && t.getTo() == to
                && (t.getAllowedActors() == null || t.getAllowedActors().isEmpty() || t.getAllowedActors().contains(actor))
                && (t.getAllowedModes() == null || t.getAllowedModes().isEmpty() || t.getAllowedModes().contains(mode))
        );
    }

    /**
     * 查询指定状态下、指定角色在指定模式下可以流转到哪些目标状态
     */
    public static List<DocketState> availableTransitions(DocketState from, RoleCode actor, OperatingMode mode) {
        if (from.isTerminal() || from == SUSPENDED) {
            return List.of();
        }
        return TRANSITIONS.stream()
            .filter(t -> t.getFrom() == from)
            .filter(t -> t.getAllowedActors() == null || t.getAllowedActors().isEmpty() || t.getAllowedActors().contains(actor))
            .filter(t -> t.getAllowedModes() == null || t.getAllowedModes().isEmpty() || t.getAllowedModes().contains(mode))
            .map(DocketTransition::getTo)
            .toList();
    }
}
