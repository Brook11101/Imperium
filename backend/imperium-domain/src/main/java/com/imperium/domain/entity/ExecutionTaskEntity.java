package com.imperium.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.imperium.domain.model.RoleCode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行任务实体
 */
@Data
@TableName("execution_task")
public class ExecutionTaskEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    private String delegationId;

    private String docketId;

    private RoleCode roleCode;

    private Integer progressPercent;

    private String status;

    private String blockReason;

    private String outputSummary;

    private LocalDateTime updatedAt;
}
