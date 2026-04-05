package com.imperium.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imperium.domain.entity.ArchiveRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 归档记录表 Mapper
 */
@Mapper
public interface ArchiveRecordMapper extends BaseMapper<ArchiveRecordEntity> {
}
