package com.gymflow.controller.settings;

import com.gymflow.common.Result;
import com.gymflow.common.annotation.PreAuthorize;
import com.gymflow.dto.setting.webUser.WebUserQueryDTO;
import com.gymflow.dto.setting.webUser.WebUserBasicDTO;
import com.gymflow.dto.setting.webUser.WebUserDetailDTO;
import com.gymflow.service.settings.WebUserService;
import com.gymflow.vo.PageResultVO;
import com.gymflow.vo.settings.WebUserListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/settings/webUser")
@Tag(name = "用户管理", description = "后台系统用户管理相关接口")
@Validated
@RequiredArgsConstructor
public class WebUserController {

    private final WebUserService webUserService;

    @PostMapping("/list")
    @Operation(summary = "分页查询用户列表")
    @PreAuthorize("settings:user:view")  // 查看用户列表权限（只有老板有）
    public Result<PageResultVO<WebUserListVO>> getUserList(@Valid @RequestBody WebUserQueryDTO queryDTO) {
        PageResultVO<WebUserListVO> result = webUserService.getUserList(queryDTO);
        return Result.success("查询成功", result);
    }

    @GetMapping("/detail/{userId}")
    @Operation(summary = "获取用户详情")
    @PreAuthorize("settings:user:view")  // 查看用户详情权限（只有老板有）
    public Result<WebUserDetailDTO> getUserDetail(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        WebUserDetailDTO detail = webUserService.getUserDetail(userId);
        return Result.success("查询成功", detail);
    }

    @PostMapping("/add")
    @Operation(summary = "新增用户")
    @PreAuthorize("settings:user:add")  // 新增用户权限（只有老板有）
    public Result<Long> addUser(@Valid @RequestBody WebUserBasicDTO userDTO) {
        Long userId = webUserService.addUser(userDTO);
        return Result.success("添加成功", userId);
    }

    @PutMapping("/update/{userId}")
    @Operation(summary = "编辑用户")
    @PreAuthorize("settings:user:edit")  // 编辑用户权限（只有老板有）
    public Result<Void> updateUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @Valid @RequestBody WebUserBasicDTO userDTO) {
        webUserService.updateUser(userId, userDTO);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{userId}")
    @Operation(summary = "删除用户")
    @PreAuthorize("settings:user:delete")  // 删除用户权限（只有老板有）
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        webUserService.deleteUser(userId);
        return Result.success("删除成功");
    }

    @PutMapping("/updateStatus/{userId}")
    @Operation(summary = "更新用户状态")
    @PreAuthorize("settings:user:status")  // 修改用户状态权限（只有老板有）
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @RequestParam @NotNull Integer status) {
        webUserService.updateUserStatus(userId, status);
        return Result.success("更新状态成功");
    }

    @PutMapping("/resetPassword/{userId}")
    @Operation(summary = "重置密码")
    @PreAuthorize("settings:user:resetpwd")  // 重置密码权限（只有老板有）
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        webUserService.resetPassword(userId, null);
        return Result.success("重置密码成功");
    }

    @PutMapping("/customResetPassword/{userId}")
    @Operation(summary = "自定义重置密码")
    @PreAuthorize("settings:user:resetpwd")  // 重置密码权限（只有老板有）
    public Result<Void> customResetPassword(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @RequestParam @NotNull String newPassword) {
        webUserService.resetPassword(userId, newPassword);
        return Result.success("密码重置成功");
    }

    @GetMapping("/check-username")
    @Operation(summary = "检查用户名是否存在")
    @PreAuthorize("settings:user:add,settings:user:edit,logical=OR")  // 新增或编辑时检查
    public Result<Boolean> checkUsernameExists(
            @RequestParam String username,
            @RequestParam(required = false) Long excludeUserId) {
        boolean exists = webUserService.checkUsernameExists(username, excludeUserId);
        return Result.success("查询成功", exists);
    }
}