package com.imperium.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 元老院会话实体
 */
@Data
@TableName(value = "senate_session", autoResultMap = true)
public class SenateSessionEntity {

    @TableId
    private String id;

    private String docketId;

    private String status;

    private LocalDateTime openedAt;

    private LocalDateTime closedAt;

    private String recommendedMotion;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> consensusJson;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> disputesJson;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
