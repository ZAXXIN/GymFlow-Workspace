// service/impl/settings/SystemConfigServiceImpl.java
package com.gymflow.service.impl.settings;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymflow.dto.setting.System.BasicConfigDTO;
import com.gymflow.dto.setting.System.BusinessConfigDTO;
import com.gymflow.dto.setting.System.SystemConfigResponseDTO;
import com.gymflow.entity.settings.SystemConfig;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.settings.SystemConfigMapper;
import com.gymflow.service.settings.SystemConfigService;
import com.gymflow.vo.settings.SystemConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    // 默认配置值
    private static final String DEFAULT_SYSTEM_NAME = "GymFlow健身管理系统";
    private static final String DEFAULT_SYSTEM_LOGO = "/logo.png";
    private static final LocalTime DEFAULT_BUSINESS_START = LocalTime.of(8, 0);  // 08:00
    private static final LocalTime DEFAULT_BUSINESS_END = LocalTime.of(22, 0);   // 22:00
    private static final int DEFAULT_RENEWAL_DAYS = 7;      // 提前7天续约
    private static final int DEFAULT_CANCEL_HOURS = 2;      // 提前2小时可取消
    private static final int DEFAULT_MIN_CLASS_SIZE = 5;    // 最低5人开课
    private static final int DEFAULT_MAX_CAPACITY = 30;     // 最多30人

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public SystemConfigResponseDTO getConfig() {
        SystemConfig config = systemConfigMapper.selectConfig();

        if (config == null) {
            // 如果没有配置，返回默认配置
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
        response.setBusiness(businessConfig);

        // 设置更新时间
        if (config.getUpdateTime() != null) {
            response.setUpdateTime(config.getUpdateTime().format(DATE_TIME_FORMATTER));
        }

        return response;
    }

    @Override
    public SystemConfigVO getConfigVO() {
        SystemConfig config = systemConfigMapper.selectConfig();

        if (config == null) {
            // 如果没有配置，返回默认配置的VO
            return getDefaultConfigVO();
        }

        SystemConfigVO vo = new SystemConfigVO();
        BeanUtils.copyProperties(config, vo);

        if (config.getUpdateTime() != null) {
            vo.setUpdateTime(config.getUpdateTime().format(DATE_TIME_FORMATTER));
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(BasicConfigDTO basicConfig, BusinessConfigDTO businessConfig) {
        log.info("开始更新系统配置");

        // 验证营业时间
        if (businessConfig.getBusinessStartTime().isAfter(businessConfig.getBusinessEndTime())) {
            throw new BusinessException("营业开始时间不能晚于结束时间");
        }

        // 验证课程容量
        if (businessConfig.getMinClassSize() > businessConfig.getMaxClassCapacity()) {
            throw new BusinessException("最低开课人数不能大于最大课程容量");
        }

        // 获取现有配置
        SystemConfig config = systemConfigMapper.selectConfig();

        if (config == null) {
            // 如果没有配置，创建新配置
            config = new SystemConfig();

            // 设置基本设置
            config.setSystemName(basicConfig.getSystemName());
            config.setSystemLogo(basicConfig.getSystemLogo());

            // 设置业务设置
            config.setBusinessStartTime(businessConfig.getBusinessStartTime());
            config.setBusinessEndTime(businessConfig.getBusinessEndTime());
            config.setCourseRenewalDays(businessConfig.getCourseRenewalDays());
            config.setCourseCancelHours(businessConfig.getCourseCancelHours());
            config.setMinClassSize(businessConfig.getMinClassSize());
            config.setMaxClassCapacity(businessConfig.getMaxClassCapacity());

            systemConfigMapper.insert(config);
            log.info("创建系统配置成功");
        } else {
            // 更新现有配置
            // 更新基本设置
            config.setSystemName(basicConfig.getSystemName());
            config.setSystemLogo(basicConfig.getSystemLogo());

            // 更新业务设置
            config.setBusinessStartTime(businessConfig.getBusinessStartTime());
            config.setBusinessEndTime(businessConfig.getBusinessEndTime());
            config.setCourseRenewalDays(businessConfig.getCourseRenewalDays());
            config.setCourseCancelHours(businessConfig.getCourseCancelHours());
            config.setMinClassSize(businessConfig.getMinClassSize());
            config.setMaxClassCapacity(businessConfig.getMaxClassCapacity());

            systemConfigMapper.updateById(config);
            log.info("更新系统配置成功");
        }
    }

    @Override
    @PostConstruct
    @Transactional(rollbackFor = Exception.class)
    public void initConfig() {
        // 系统启动时检查配置是否存在，如果不存在则创建默认配置
        SystemConfig config = systemConfigMapper.selectConfig();

        if (config == null) {
            log.info("系统配置不存在，创建默认配置");

            config = new SystemConfig();

            // 设置默认基本设置
            config.setSystemName(DEFAULT_SYSTEM_NAME);
            config.setSystemLogo(DEFAULT_SYSTEM_LOGO);

            // 设置默认业务设置
            config.setBusinessStartTime(DEFAULT_BUSINESS_START);
            config.setBusinessEndTime(DEFAULT_BUSINESS_END);
            config.setCourseRenewalDays(DEFAULT_RENEWAL_DAYS);
            config.setCourseCancelHours(DEFAULT_CANCEL_HOURS);
            config.setMinClassSize(DEFAULT_MIN_CLASS_SIZE);
            config.setMaxClassCapacity(DEFAULT_MAX_CAPACITY);

            systemConfigMapper.insert(config);
            log.info("默认系统配置创建成功");
        }
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

        // 默认业务设置
        BusinessConfigDTO businessConfig = new BusinessConfigDTO();
        businessConfig.setBusinessStartTime(DEFAULT_BUSINESS_START);
        businessConfig.setBusinessEndTime(DEFAULT_BUSINESS_END);
        businessConfig.setCourseRenewalDays(DEFAULT_RENEWAL_DAYS);
        businessConfig.setCourseCancelHours(DEFAULT_CANCEL_HOURS);
        businessConfig.setMinClassSize(DEFAULT_MIN_CLASS_SIZE);
        businessConfig.setMaxClassCapacity(DEFAULT_MAX_CAPACITY);
        response.setBusiness(businessConfig);

        response.setUpdateTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));

        return response;
    }

    /**
     * 获取默认配置的VO
     */
    private SystemConfigVO getDefaultConfigVO() {
        SystemConfigVO vo = new SystemConfigVO();

        // 默认基本设置
        vo.setSystemName(DEFAULT_SYSTEM_NAME);
        vo.setSystemLogo(DEFAULT_SYSTEM_LOGO);

        // 默认业务设置
        vo.setBusinessStartTime(DEFAULT_BUSINESS_START);
        vo.setBusinessEndTime(DEFAULT_BUSINESS_END);
        vo.setCourseRenewalDays(DEFAULT_RENEWAL_DAYS);
        vo.setCourseCancelHours(DEFAULT_CANCEL_HOURS);
        vo.setMinClassSize(DEFAULT_MIN_CLASS_SIZE);
        vo.setMaxClassCapacity(DEFAULT_MAX_CAPACITY);

        vo.setUpdateTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));

        return vo;
    }
}