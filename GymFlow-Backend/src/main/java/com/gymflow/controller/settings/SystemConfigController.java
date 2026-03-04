package com.gymflow.controller.settings;

import com.gymflow.common.Result;
import com.gymflow.dto.setting.System.BasicConfigDTO;
import com.gymflow.dto.setting.System.BusinessConfigDTO;
import com.gymflow.dto.setting.System.SystemConfigResponseDTO;
import com.gymflow.service.settings.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalTime;

@Slf4j
@RestController
@RequestMapping("/settings/systemConfig")
@Tag(name = "系统配置管理", description = "系统全局配置相关接口")
@Validated
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    @Operation(summary = "获取系统配置", description = "获取系统全局配置信息（包含基本设置和业务设置）")
    public Result<SystemConfigResponseDTO> getConfig() {
        try {
            SystemConfigResponseDTO config = systemConfigService.getConfig();
            return Result.success("获取成功", config);
        } catch (Exception e) {
            log.error("获取系统配置失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @PutMapping
    @Operation(summary = "更新系统配置", description = "更新系统全局配置信息")
    public Result<Void> updateConfig(
            @Valid @RequestBody SystemConfigUpdateRequest request) {
        try {
            systemConfigService.updateConfig(request.getBasic(), request.getBusiness());
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新系统配置失败：{}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @PostMapping("/reset")
    @Operation(summary = "重置系统配置", description = "重置系统配置为默认值")
    public Result<Void> resetConfig() {
        try {
            // 创建默认配置的DTO
            BasicConfigDTO defaultBasic = new BasicConfigDTO();
            defaultBasic.setSystemName("GymFlow健身管理系统");
            defaultBasic.setSystemLogo("/logo.png");

            BusinessConfigDTO defaultBusiness = new BusinessConfigDTO();
            defaultBusiness.setBusinessStartTime(LocalTime.of(8, 0));
            defaultBusiness.setBusinessEndTime(LocalTime.of(22, 0));
            defaultBusiness.setCourseRenewalDays(7);
            defaultBusiness.setCourseCancelHours(2);
            defaultBusiness.setMinClassSize(5);
            defaultBusiness.setMaxClassCapacity(30);

            systemConfigService.updateConfig(defaultBasic, defaultBusiness);
            return Result.success("重置成功");
        } catch (Exception e) {
            log.error("重置系统配置失败：{}", e.getMessage(), e);
            return Result.error("重置失败：" + e.getMessage());
        }
    }

    /**
     * 系统配置更新请求体
     */
    static class SystemConfigUpdateRequest {
        @Valid
        private BasicConfigDTO basic;

        @Valid
        private BusinessConfigDTO business;

        public BasicConfigDTO getBasic() {
            return basic;
        }

        public void setBasic(BasicConfigDTO basic) {
            this.basic = basic;
        }

        public BusinessConfigDTO getBusiness() {
            return business;
        }

        public void setBusiness(BusinessConfigDTO business) {
            this.business = business;
        }
    }
}