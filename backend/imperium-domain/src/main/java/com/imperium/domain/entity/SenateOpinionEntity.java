package com.imperium.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.imperium.domain.model.RoleCode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 元老意见实体
 */
@Data
@TableName("senate_opinion")
public class SenateOpinionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;

    private String docketId;

    private RoleCode agentId;

    private String stance;

    private String summary;

    private String details;

    private LocalDateTime generatedAt;
}
