package com.imperium.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imperium.domain.entity.DocketEventEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 议案事件表 Mapper
 */
@Mapper
public interface DocketEventMapper extends BaseMapper<DocketEventEntity> {
}
