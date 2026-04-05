package com.imperium.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 保民官审查实体
 */
@Data
@TableName(value = "tribune_review", autoResultMap = true)
public class TribuneReviewEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String docketId;

    private String reviewResult;

    private String reason;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> notesJson;

    private LocalDateTime createdAt;
}
