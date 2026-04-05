package com.imperium.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 议案事件实体（制度轨迹）
 */
@Data
@TableName(value = "docket_event", autoResultMap = true)
public class DocketEventEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String docketId;

    private String eventType;

    private String actorType;

    private String actorId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> payloadJson;

    private LocalDateTime createdAt;
}
