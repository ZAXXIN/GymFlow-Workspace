package com.gymflow.controller.settings;

import com.gymflow.common.Result;
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
    @Operation(summary = "分页查询用户列表", description = "根据条件分页查询后台用户列表")
    public Result<PageResultVO<WebUserListVO>> getUserList(@Valid @RequestBody WebUserQueryDTO queryDTO) {
        try {
            PageResultVO<WebUserListVO> result = webUserService.getUserList(queryDTO);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询用户列表失败：{}", e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/detail/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public Result<WebUserDetailDTO> getUserDetail(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        try {
            WebUserDetailDTO detail = webUserService.getUserDetail(userId);
            return Result.success("查询成功", detail);
        } catch (Exception e) {
            log.error("获取用户详情失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @PostMapping("/add")
    @Operation(summary = "新增用户", description = "添加新后台用户")
    public Result<Long> addUser(@Valid @RequestBody WebUserBasicDTO userDTO) {
        try {
            Long userId = webUserService.addUser(userDTO);
            return Result.success("添加成功", userId);
        } catch (Exception e) {
            log.error("添加用户失败：{}", e.getMessage(), e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    @PutMapping("/update/{userId}")
    @Operation(summary = "编辑用户", description = "更新用户信息")
    public Result<Void> updateUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @Valid @RequestBody WebUserBasicDTO userDTO) {
        try {
            webUserService.updateUser(userId, userDTO);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新用户失败：{}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{userId}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        try {
            webUserService.deleteUser(userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除用户失败：{}", e.getMessage(), e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PutMapping("/updateStatus/{userId}")
    @Operation(summary = "更新用户状态", description = "更新用户状态：0-禁用，1-正常")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @RequestParam @NotNull Integer status) {
        try {
            webUserService.updateUserStatus(userId, status);
            return Result.success("更新状态成功");
        } catch (Exception e) {
            log.error("更新用户状态失败：{}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @PutMapping("/resetPassword/{userId}")
    @Operation(summary = "重置密码", description = "重置用户密码为默认密码")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        try {
            webUserService.resetPassword(userId, null);
            return Result.success("重置密码成功");
        } catch (Exception e) {
            log.error("重置密码失败：{}", e.getMessage(), e);
            return Result.error("重置失败：" + e.getMessage());
        }
    }

    @PutMapping("/customResetPassword/{userId}")
    @Operation(summary = "自定义重置密码", description = "将用户密码重置为指定密码")
    public Result<Void> customResetPassword(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @RequestParam @NotNull String newPassword) {
        try {
            webUserService.resetPassword(userId, newPassword);
            return Result.success("密码重置成功");
        } catch (Exception e) {
            log.error("自定义重置密码失败：{}", e.getMessage(), e);
            return Result.error("重置失败：" + e.getMessage());
        }
    }

    @GetMapping("/check-username")
    @Operation(summary = "检查用户名是否存在", description = "检查用户名是否已被使用")
    public Result<Boolean> checkUsernameExists(
            @RequestParam String username,
            @RequestParam(required = false) Long excludeUserId) {
        try {
            boolean exists = webUserService.checkUsernameExists(username, excludeUserId);
            return Result.success("查询成功", exists);
        } catch (Exception e) {
            log.error("检查用户名失败：{}", e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}