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
 * 恺撒裁决实体
 */
@Data
@TableName(value = "caesar_decision", autoResultMap = true)
public class CaesarDecisionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String docketId;

    private String decisionType;

    private String comment;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> constraintsJson;

    private Integer isOverride;

    private LocalDateTime createdAt;
}
