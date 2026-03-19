package com.gymflow.controller.settings;

import com.gymflow.common.Result;
import com.gymflow.common.annotation.PreAuthorize;
import com.gymflow.dto.settings.System.BasicConfigDTO;
import com.gymflow.dto.settings.System.BusinessConfigDTO;
import com.gymflow.dto.settings.System.SystemConfigResponseDTO;
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
    @Operation(summary = "获取系统配置")
//    @PreAuthorize("settings:config:view")  // 查看配置权限（老板和前台都有）
    public Result<SystemConfigResponseDTO> getConfig() {
        SystemConfigResponseDTO config = systemConfigService.getConfig();
        return Result.success("获取成功", config);
    }

    @PutMapping
    @Operation(summary = "更新系统配置")
    @PreAuthorize("settings:config:view,logical=AND")  // 修改配置权限（只有老板有）
    public Result<Void> updateConfig(@Valid @RequestBody SystemConfigUpdateRequest request) {
        systemConfigService.updateConfig(request.getBasic(), request.getBusiness());
        return Result.success("更新成功");
    }

    @PostMapping("/reset")
    @Operation(summary = "重置系统配置")
    @PreAuthorize("settings:config:view,logical=AND")  // 重置配置权限（只有老板有）
    public Result<Void> resetConfig() {
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
    }

    /**
     * 系统配置更新请求体
     */
    static class SystemConfigUpdateRequest {
        @Valid
        private BasicConfigDTO basic;
        @Valid
        private BusinessConfigDTO business;

        public BasicConfigDTO getBasic() { return basic; }
        public void setBasic(BasicConfigDTO basic) { this.basic = basic; }
        public BusinessConfigDTO getBusiness() { return business; }
        public void setBusiness(BusinessConfigDTO business) { this.business = business; }
    }
}