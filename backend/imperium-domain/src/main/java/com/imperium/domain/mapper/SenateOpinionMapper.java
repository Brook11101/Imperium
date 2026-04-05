package com.imperium.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imperium.domain.entity.SenateOpinionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 元老意见表 Mapper
 */
@Mapper
public interface SenateOpinionMapper extends BaseMapper<SenateOpinionEntity> {
}
