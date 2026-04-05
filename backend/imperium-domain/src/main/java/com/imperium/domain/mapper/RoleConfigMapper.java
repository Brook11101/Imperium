package com.imperium.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imperium.domain.entity.RoleConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色配置表 Mapper
 */
@Mapper
public interface RoleConfigMapper extends BaseMapper<RoleConfigEntity> {
}
