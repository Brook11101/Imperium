package com.imperium.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.imperium.domain.model.RoleCode;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 执政官派发实体
 */
@Data
@TableName(value = "delegation", autoResultMap = true)
public class DelegationEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    private String docketId;

    private RoleCode roleCode;

    private String objective;

    private String status;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> dependsOnJson;

    private LocalDateTime assignedAt;

    private LocalDateTime completedAt;
}
