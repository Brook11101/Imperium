package com.imperium.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imperium.domain.entity.SenateSessionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 元老院会话表 Mapper
 */
@Mapper
public interface SenateSessionMapper extends BaseMapper<SenateSessionEntity> {
}
