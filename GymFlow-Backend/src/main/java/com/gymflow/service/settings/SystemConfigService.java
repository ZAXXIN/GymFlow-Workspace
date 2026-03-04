package com.gymflow.service.settings;

import com.gymflow.dto.setting.System.BasicConfigDTO;
import com.gymflow.dto.setting.System.BusinessConfigDTO;
import com.gymflow.dto.setting.System.SystemConfigResponseDTO;
import com.gymflow.vo.settings.SystemConfigVO;

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
}