package com.gymflow.mapper.settings;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.settings.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 获取系统配置（系统只有一条配置记录）
     */
    @Select("SELECT * FROM system_config LIMIT 1")
    SystemConfig selectConfig();
}