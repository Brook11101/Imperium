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
 * 归档记录实体
 */
@Data
@TableName(value = "archive_record", autoResultMap = true)
public class ArchiveRecordEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String docketId;

    private String finalSummary;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> artifactsJson;

    private LocalDateTime archivedAt;
}
