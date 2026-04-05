package com.imperium.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imperium.domain.entity.TribuneReviewEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 保民官审查表 Mapper
 */
@Mapper
public interface TribuneReviewMapper extends BaseMapper<TribuneReviewEntity> {
}
