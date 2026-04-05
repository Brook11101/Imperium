-- Imperium Database Schema V1
-- Phase 1: Core tables

-- =============================================
-- docket: 议案主表（核心聚合根）
-- =============================================
CREATE TABLE docket
(
    id                       VARCHAR(32)  NOT NULL COMMENT '议案ID，格式 IMP-YYYYMMDD-NNN',
    title                    VARCHAR(256) NOT NULL COMMENT '议案标题',
    mode                     VARCHAR(32)  NOT NULL COMMENT '运行模式：STANDARD_SENATE / DIRECT_DECREE / TRIBUNE_LOCK / CAMPAIGN_MODE',
    state                    VARCHAR(32)  NOT NULL COMMENT '当前状态',
    priority                 VARCHAR(16)  NOT NULL DEFAULT 'NORMAL' COMMENT '优先级：LOW / NORMAL / HIGH / CRITICAL',
    risk_level               VARCHAR(16)  NOT NULL DEFAULT 'LOW' COMMENT '风险等级：LOW / MEDIUM / HIGH',
    current_owner            VARCHAR(32)  NOT NULL COMMENT '当前责任角色',
    edict_raw                TEXT         NOT NULL COMMENT '恺撒原始法令文本',
    summary                  TEXT COMMENT '传令官规范化摘要',
    current_senate_session_id VARCHAR(32) COMMENT '当前元老院会话ID',
    suspended_from_state     VARCHAR(32) COMMENT '暂停前的状态（用于恢复）',
    last_progress_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后推进时间',
    retry_count              INT          NOT NULL DEFAULT 0 COMMENT '调度重试次数',
    escalation_level         INT          NOT NULL DEFAULT 0 COMMENT '升级层级',
    deleted                  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_state_owner (state, current_owner, updated_at),
    INDEX idx_mode_state (mode, state),
    INDEX idx_last_progress (last_progress_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '议案卷宗主表';

-- =============================================
-- docket_event: 议案事件表（制度轨迹）
-- =============================================
CREATE TABLE docket_event
(
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '事件ID',
    docket_id    VARCHAR(32)     NOT NULL COMMENT '所属议案ID',
    event_type   VARCHAR(64)     NOT NULL COMMENT '事件类型',
    actor_type   VARCHAR(32)     NOT NULL COMMENT '操作者类型：SYSTEM / ROLE / CAESAR',
    actor_id     VARCHAR(64)     NOT NULL COMMENT '操作者标识',
    payload_json JSON COMMENT '事件载荷',
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
    PRIMARY KEY (id),
    INDEX idx_docket_time (docket_id, created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '议案事件表';

-- =============================================
-- senate_session: 元老院会话表
-- =============================================
CREATE TABLE senate_session
(
    id                  VARCHAR(32)  NOT NULL COMMENT '会话ID',
    docket_id           VARCHAR(32)  NOT NULL COMMENT '所属议案ID',
    status              VARCHAR(32)  NOT NULL DEFAULT 'OPEN' COMMENT '会话状态：OPEN / CLOSED',
    opened_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开启时间',
    closed_at           DATETIME COMMENT '关闭时间',
    recommended_motion  TEXT COMMENT '推荐动议',
    consensus_json      JSON COMMENT '共识点列表',
    disputes_json       JSON COMMENT '争议点列表',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_docket (docket_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '元老院会话表';

-- =============================================
-- senate_opinion: 元老意见表
-- =============================================
CREATE TABLE senate_opinion
(
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '意见ID',
    session_id   VARCHAR(32)     NOT NULL COMMENT '所属会话ID',
    docket_id    VARCHAR(32)     NOT NULL COMMENT '所属议案ID',
    agent_id     VARCHAR(64)     NOT NULL COMMENT '元老角色标识',
    stance       VARCHAR(16)     NOT NULL COMMENT '立场：SUPPORT / OBJECT / NEUTRAL / CONDITION',
    summary      VARCHAR(512)    NOT NULL COMMENT '意见摘要',
    details      TEXT COMMENT '详细意见',
    generated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    PRIMARY KEY (id),
    INDEX idx_session_agent (session_id, agent_id),
    INDEX idx_docket (docket_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '元老意见表';

-- =============================================
-- tribune_review: 保民官审查表
-- =============================================
CREATE TABLE tribune_review
(
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '审查ID',
    docket_id     VARCHAR(32)     NOT NULL COMMENT '所属议案ID',
    review_result VARCHAR(32)     NOT NULL COMMENT '审查结论：APPROVED / REJECTED / RETURNED',
    reason        TEXT COMMENT '否决或退回理由',
    notes_json    JSON COMMENT '补充说明列表',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审查时间',
    PRIMARY KEY (id),
    INDEX idx_docket (docket_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '保民官审查表';

-- =============================================
-- caesar_decision: 恺撒裁决表
-- =============================================
CREATE TABLE caesar_decision
(
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '裁决ID',
    docket_id       VARCHAR(32)     NOT NULL COMMENT '所属议案ID',
    decision_type   VARCHAR(32)     NOT NULL COMMENT '裁决类型：APPROVE / REJECT / OVERRIDE / RESTRICT / RETURN_SENATE',
    comment         TEXT COMMENT '批注说明',
    constraints_json JSON COMMENT '执行约束条件列表',
    is_override     TINYINT         NOT NULL DEFAULT 0 COMMENT '是否为覆盖决定',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '裁决时间',
    PRIMARY KEY (id),
    INDEX idx_docket (docket_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '恺撒裁决表';

-- =============================================
-- delegation: 执政官派发表
-- =============================================
CREATE TABLE delegation
(
    id            VARCHAR(32)  NOT NULL COMMENT '派发ID',
    docket_id     VARCHAR(32)  NOT NULL COMMENT '所属议案ID',
    role_code     VARCHAR(32)  NOT NULL COMMENT '目标执行角色',
    objective     TEXT         NOT NULL COMMENT '执行目标',
    status        VARCHAR(32)  NOT NULL DEFAULT 'PENDING' COMMENT '派发状态：PENDING / IN_PROGRESS / COMPLETED / FAILED / BLOCKED',
    depends_on_json JSON COMMENT '依赖的其他派发ID列表',
    assigned_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '派发时间',
    completed_at  DATETIME COMMENT '完成时间',
    PRIMARY KEY (id),
    INDEX idx_docket_status (docket_id, status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '执政官派发表';

-- =============================================
-- execution_task: 执行任务表
-- =============================================
CREATE TABLE execution_task
(
    id               VARCHAR(32)  NOT NULL COMMENT '任务ID',
    delegation_id    VARCHAR(32)  NOT NULL COMMENT '所属派发ID',
    docket_id        VARCHAR(32)  NOT NULL COMMENT '所属议案ID',
    role_code        VARCHAR(32)  NOT NULL COMMENT '执行角色',
    progress_percent INT          NOT NULL DEFAULT 0 COMMENT '执行进度百分比',
    status           VARCHAR(32)  NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING / RUNNING / COMPLETED / FAILED / BLOCKED',
    block_reason     VARCHAR(512) COMMENT '阻塞原因',
    output_summary   TEXT COMMENT '执行结果摘要',
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_docket_status (docket_id, status),
    INDEX idx_delegation (delegation_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '执行任务表';

-- =============================================
-- audit_record: 审计记录表
-- =============================================
CREATE TABLE audit_record
(
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '审计ID',
    docket_id         VARCHAR(32)     NOT NULL COMMENT '所属议案ID',
    audit_result      VARCHAR(32)     NOT NULL COMMENT '审计结论：PASS / RETURN / ESCALATE',
    risk_notes_json   JSON COMMENT '风险说明列表',
    quality_notes_json JSON COMMENT '质量说明列表',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审计时间',
    PRIMARY KEY (id),
    INDEX idx_docket (docket_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '审计记录表';

-- =============================================
-- archive_record: 归档记录表
-- =============================================
CREATE TABLE archive_record
(
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '归档ID',
    docket_id      VARCHAR(32)     NOT NULL UNIQUE COMMENT '所属议案ID',
    final_summary  TEXT COMMENT '最终摘要',
    artifacts_json JSON COMMENT '产出物列表',
    archived_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    PRIMARY KEY (id),
    INDEX idx_archived_at (archived_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '归档记录表';

-- =============================================
-- agent_call_log: Agent 调用日志表
-- =============================================
CREATE TABLE agent_call_log
(
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    docket_id    VARCHAR(32)     NOT NULL COMMENT '所属议案ID',
    role_code    VARCHAR(32)     NOT NULL COMMENT '调用的角色',
    command_line TEXT COMMENT '执行的命令行',
    exit_code    INT COMMENT '退出码',
    stdout_text  MEDIUMTEXT COMMENT '标准输出',
    stderr_text  TEXT COMMENT '错误输出',
    status       VARCHAR(32)     NOT NULL COMMENT '调用状态：RUNNING / SUCCESS / FAILED / TIMEOUT',
    started_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    ended_at     DATETIME COMMENT '结束时间',
    PRIMARY KEY (id),
    INDEX idx_docket_role (docket_id, role_code, started_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Agent 调用日志表';

-- =============================================
-- outbox_event: 本地事件外发表（Outbox 模式）
-- =============================================
CREATE TABLE outbox_event
(
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '外发事件ID',
    aggregate_type VARCHAR(64)     NOT NULL COMMENT '聚合类型，如 DOCKET',
    aggregate_id   VARCHAR(32)     NOT NULL COMMENT '聚合根ID',
    topic          VARCHAR(128)    NOT NULL COMMENT 'RocketMQ Topic',
    tag            VARCHAR(64)     NOT NULL COMMENT 'RocketMQ Tag',
    payload_json   JSON            NOT NULL COMMENT '消息载荷',
    publish_status VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT '发布状态：PENDING / PUBLISHED / FAILED',
    retry_count    INT             NOT NULL DEFAULT 0 COMMENT '重试次数',
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    published_at   DATETIME COMMENT '发布时间',
    PRIMARY KEY (id),
    INDEX idx_status_created (publish_status, created_at),
    INDEX idx_aggregate (aggregate_type, aggregate_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '本地事件外发表';

-- =============================================
-- role_config: 角色配置表
-- =============================================
CREATE TABLE role_config
(
    role_code        VARCHAR(32)  NOT NULL COMMENT '角色编码',
    display_name     VARCHAR(64)  NOT NULL COMMENT '显示名称',
    allow_roles_json JSON COMMENT '允许调用的角色列表',
    enabled          TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用',
    agent_id         VARCHAR(128) COMMENT 'OpenClaw Agent ID',
    prompt_version   VARCHAR(16) COMMENT 'Prompt 版本',
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (role_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '角色配置表';

-- =============================================
-- 初始化角色配置数据
-- =============================================
INSERT INTO role_config (role_code, display_name, allow_roles_json, enabled)
VALUES ('CAESAR', '恺撒', '[]', 1),
       ('PRAECO', '传令官', '["SENATOR_STRATEGOS","SENATOR_JURIS","SENATOR_FISCUS","CONSUL"]', 1),
       ('SENATOR_STRATEGOS', '战略派元老', '["TRIBUNE","SCRIBA","CONSUL"]', 1),
       ('SENATOR_JURIS', '法理派元老', '["TRIBUNE","SCRIBA","CONSUL"]', 1),
       ('SENATOR_FISCUS', '财政派元老', '["TRIBUNE","SCRIBA","CONSUL"]', 1),
       ('TRIBUNE', '保民官', '["SCRIBA","CONSUL"]', 1),
       ('CONSUL', '执政官', '["LEGATUS","PRAETOR","AEDILE","QUAESTOR","SCRIBA","GOVERNOR"]', 1),
       ('LEGATUS', '军团使节', '["CONSUL","SCRIBA"]', 1),
       ('PRAETOR', '法务官', '["CONSUL","SCRIBA","TRIBUNE"]', 1),
       ('AEDILE', '营造官', '["CONSUL","SCRIBA"]', 1),
       ('QUAESTOR', '财务官', '["CONSUL","SCRIBA","TRIBUNE"]', 1),
       ('SCRIBA', '书记官', '["CENSOR"]', 1),
       ('CENSOR', '监察官', '["SCRIBA"]', 1),
       ('GOVERNOR', '行省总督', '["CONSUL","SCRIBA"]', 1);
