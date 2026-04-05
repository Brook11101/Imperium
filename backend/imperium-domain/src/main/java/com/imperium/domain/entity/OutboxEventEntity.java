package com.imperium.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Outbox 事件实体
 * 保证本地事务与消息投递的一致性
 */
@Data
@TableName(value = "outbox_event", autoResultMap = true)
public class OutboxEventEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String aggregateType;

    private String aggregateId;

    private String topic;

    private String tag;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> payloadJson;

    private String publishStatus;

    private Integer retryCount;

    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;
}
