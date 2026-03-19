package com.gymflow.service.impl.mini;

import com.gymflow.dto.mini.MiniCheckinRuleDTO;
import com.gymflow.dto.settings.System.BasicConfigDTO;
import com.gymflow.dto.settings.System.BusinessConfigDTO;
import com.gymflow.dto.settings.System.SystemConfigResponseDTO;
import com.gymflow.entity.settings.SystemConfig;
import com.gymflow.mapper.settings.SystemConfigMapper;
import com.gymflow.service.mini.MiniConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiniConfigServiceImpl implements MiniConfigService {

    private final SystemConfigMapper systemConfigMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 默认配置值
    private static final String DEFAULT_SYSTEM_NAME = "GymFlow健身管理系统";
    private static final String DEFAULT_SYSTEM_LOGO = "/logo.png";
    private static final int DEFAULT_CHECKIN_START_MINUTES = 30;
    private static final int DEFAULT_CHECKIN_END_MINUTES = 0;
    private static final int DEFAULT_AUTO_COMPLETE_HOURS = 1;

    @Override
    public SystemConfigResponseDTO getSystemConfig() {
        SystemConfig config = systemConfigMapper.selectConfig();

        if (config == null) {
            return getDefaultConfigResponse();
        }

        SystemConfigResponseDTO response = new SystemConfigResponseDTO();

        // 设置基本设置
        BasicConfigDTO basicConfig = new BasicConfigDTO();
        basicConfig.setSystemName(config.getSystemName());
        basicConfig.setSystemLogo(config.getSystemLogo());
        response.setBasic(basicConfig);

        // 设置业务设置
        BusinessConfigDTO businessConfig = new BusinessConfigDTO();
        businessConfig.setBusinessStartTime(config.getBusinessStartTime());
        businessConfig.setBusinessEndTime(config.getBusinessEndTime());
        businessConfig.setCourseRenewalDays(config.getCourseRenewalDays());
        businessConfig.setCourseCancelHours(config.getCourseCancelHours());
        businessConfig.setMinClassSize(config.getMinClassSize());
        businessConfig.setMaxClassCapacity(config.getMaxClassCapacity());

        // 设置签到配置
        businessConfig.setCheckinStartMinutes(config.getCheckinStartMinutes());
        businessConfig.setCheckinEndMinutes(config.getCheckinEndMinutes());
        businessConfig.setAutoCompleteHours(config.getAutoCompleteHours());

        response.setBusiness(businessConfig);

        // 设置更新时间
        if (config.getUpdateTime() != null) {
            response.setUpdateTime(config.getUpdateTime().format(DATE_TIME_FORMATTER));
        }

        return response;
    }

    @Override
    public MiniCheckinRuleDTO getCheckinRules() {
        SystemConfig config = systemConfigMapper.selectConfig();

        MiniCheckinRuleDTO ruleDTO = new MiniCheckinRuleDTO();

        if (config != null) {
            ruleDTO.setCheckinStartMinutes(config.getCheckinStartMinutes());
            ruleDTO.setCheckinEndMinutes(config.getCheckinEndMinutes());
            ruleDTO.setAutoCompleteHours(config.getAutoCompleteHours());
        } else {
            // 使用默认值
            ruleDTO.setCheckinStartMinutes(DEFAULT_CHECKIN_START_MINUTES);
            ruleDTO.setCheckinEndMinutes(DEFAULT_CHECKIN_END_MINUTES);
            ruleDTO.setAutoCompleteHours(DEFAULT_AUTO_COMPLETE_HOURS);
        }

        return ruleDTO;
    }

    /**
     * 获取默认配置的响应DTO
     */
    private SystemConfigResponseDTO getDefaultConfigResponse() {
        SystemConfigResponseDTO response = new SystemConfigResponseDTO();

        // 默认基本设置
        BasicConfigDTO basicConfig = new BasicConfigDTO();
        basicConfig.setSystemName(DEFAULT_SYSTEM_NAME);
        basicConfig.setSystemLogo(DEFAULT_SYSTEM_LOGO);
        response.setBasic(basicConfig);

        // 默认业务设置 - 使用空对象，实际应该从其他地方获取默认值
        BusinessConfigDTO businessConfig = new BusinessConfigDTO();
        businessConfig.setCheckinStartMinutes(DEFAULT_CHECKIN_START_MINUTES);
        businessConfig.setCheckinEndMinutes(DEFAULT_CHECKIN_END_MINUTES);
        businessConfig.setAutoCompleteHours(DEFAULT_AUTO_COMPLETE_HOURS);

        response.setBusiness(businessConfig);
        response.setUpdateTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));

        return response;
    }
}