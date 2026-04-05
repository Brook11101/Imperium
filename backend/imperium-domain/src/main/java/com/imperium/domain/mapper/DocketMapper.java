package com.imperium.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imperium.domain.entity.DocketEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 议案主表 Mapper
 */
@Mapper
public interface DocketMapper extends BaseMapper<DocketEntity> {
}
