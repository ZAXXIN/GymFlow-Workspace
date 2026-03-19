package com.gymflow.service.mini;

import com.gymflow.dto.mini.MiniCheckinRuleDTO;
import com.gymflow.dto.settings.System.SystemConfigResponseDTO;

public interface MiniConfigService {

    /**
     * 获取系统配置
     */
    SystemConfigResponseDTO getSystemConfig();

    /**
     * 获取签到规则
     */
    MiniCheckinRuleDTO getCheckinRules();
}