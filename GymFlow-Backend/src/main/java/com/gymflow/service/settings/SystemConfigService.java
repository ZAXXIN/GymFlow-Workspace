package com.gymflow.service.settings;

import com.gymflow.dto.settings.System.BasicConfigDTO;
import com.gymflow.dto.settings.System.BusinessConfigDTO;
import com.gymflow.dto.settings.System.SystemConfigResponseDTO;
import com.gymflow.vo.settings.SystemConfigVO;

import java.time.LocalDateTime;

public interface SystemConfigService {

    /**
     * 获取系统配置（返回包含基本设置和业务设置的对象）
     */
    SystemConfigResponseDTO getConfig();

    /**
     * 获取系统配置视图对象（用于业务验证）
     */
    SystemConfigVO getConfigVO();

    /**
     * 更新系统配置
     */
    void updateConfig(BasicConfigDTO basicConfig, BusinessConfigDTO businessConfig);

    /**
     * 初始化系统配置（如果没有配置则创建默认配置）
     */
    void initConfig();

    /**
     * 获取签到开始时间（课程开始前多少分钟可签到）
     */
    Integer getCheckinStartMinutes();

    /**
     * 获取签到截止时间（课程开始后多少分钟截止）
     */
    Integer getCheckinEndMinutes();

    /**
     * 获取自动完成时间（课程结束后多少小时自动完成）
     */
    Integer getAutoCompleteHours();

    /**
     * 验证签到时间是否有效
     * @param courseDateTime 课程开始时间
     * @return true-可以签到，false-不在签到时间范围内
     */
    boolean canCheckIn(LocalDateTime courseDateTime);

    /**
     * 验证签到时间是否有效（带异常抛出）
     */
    void validateCheckInTime(LocalDateTime courseDateTime);
}