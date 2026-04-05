package com.imperium.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色配置实体
 */
@Data
@TableName(value = "role_config", autoResultMap = true)
public class RoleConfigEntity {

    @TableId
    private String roleCode;

    private String displayName;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> allowRolesJson;

    private Integer enabled;

    private String agentId;

    private String promptVersion;

    private LocalDateTime updatedAt;
}
