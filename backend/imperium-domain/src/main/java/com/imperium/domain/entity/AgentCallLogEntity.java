package com.imperium.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 调用日志实体
 */
@Data
@TableName("agent_call_log")
public class AgentCallLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String docketId;

    private String roleCode;

    private String commandLine;

    private Integer exitCode;

    private String stdoutText;

    private String stderrText;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;
}
