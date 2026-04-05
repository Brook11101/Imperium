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
 * 审计记录实体
 */
@Data
@TableName(value = "audit_record", autoResultMap = true)
public class AuditRecordEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String docketId;

    private String auditResult;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> riskNotesJson;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> qualityNotesJson;

    private LocalDateTime createdAt;
}
