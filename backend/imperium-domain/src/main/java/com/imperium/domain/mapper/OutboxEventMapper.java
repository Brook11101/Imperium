package com.imperium.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imperium.domain.entity.OutboxEventEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Outbox 事件表 Mapper
 */
@Mapper
public interface OutboxEventMapper extends BaseMapper<OutboxEventEntity> {
}
