package com.imperium.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imperium.domain.entity.AuditRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计记录表 Mapper
 */
@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecordEntity> {
}
