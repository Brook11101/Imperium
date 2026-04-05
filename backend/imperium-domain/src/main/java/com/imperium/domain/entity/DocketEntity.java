package com.imperium.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.imperium.domain.model.DocketState;
import com.imperium.domain.model.OperatingMode;
import com.imperium.domain.model.RoleCode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 议案主实体（聚合根）
 * 对应数据库表：docket
 */
@Data
@TableName("docket")
public class DocketEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    private String title;

    private OperatingMode mode;

    private DocketState state;

    private String priority;

    private String riskLevel;

    private RoleCode currentOwner;

    private String edictRaw;

    private String summary;

    private String currentSenateSessionId;

    private DocketState suspendedFromState;

    private LocalDateTime lastProgressAt;

    private Integer retryCount;

    private Integer escalationLevel;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
