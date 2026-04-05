package com.imperium.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imperium.domain.entity.AgentCallLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Agent 调用日志表 Mapper
 */
@Mapper
public interface AgentCallLogMapper extends BaseMapper<AgentCallLogEntity> {
}
