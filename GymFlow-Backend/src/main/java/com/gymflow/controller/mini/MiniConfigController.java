package com.gymflow.controller.mini;

import com.gymflow.common.Result;
import com.gymflow.dto.mini.MiniCheckinRuleDTO;
import com.gymflow.dto.settings.System.SystemConfigResponseDTO;
import com.gymflow.service.mini.MiniConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/mini/config")
@Tag(name = "小程序-配置", description = "系统配置相关接口")
@Validated
@RequiredArgsConstructor
public class MiniConfigController {

    private final MiniConfigService miniConfigService;

    @GetMapping("/system")
    @Operation(summary = "获取系统配置")
    public Result<SystemConfigResponseDTO> getSystemConfig() {
        SystemConfigResponseDTO config = miniConfigService.getSystemConfig();
        return Result.success("获取成功", config);
    }

    @GetMapping("/checkin-rules")
    @Operation(summary = "获取签到规则")
    public Result<MiniCheckinRuleDTO> getCheckinRules() {
        MiniCheckinRuleDTO rules = miniConfigService.getCheckinRules();
        return Result.success("获取成功", rules);
    }
}