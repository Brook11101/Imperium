package com.imperium.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imperium.domain.entity.ExecutionTaskEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 执行任务表 Mapper
 */
@Mapper
public interface ExecutionTaskMapper extends BaseMapper<ExecutionTaskEntity> {
}
