package com.gymflow.service.impl.settings;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.setting.webUser.WebUserQueryDTO;
import com.gymflow.dto.setting.webUser.WebUserBasicDTO;
import com.gymflow.dto.setting.webUser.WebUserDetailDTO;
import com.gymflow.entity.settings.WebUser;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.settings.WebUserMapper;
import com.gymflow.service.settings.WebUserService;
import com.gymflow.utils.BCryptUtil;
import com.gymflow.vo.PageResultVO;
import com.gymflow.vo.settings.WebUserListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebUserServiceImpl implements WebUserService {

    private final WebUserMapper webUserMapper;
    private final BCryptUtil bCryptUtil;

    // 默认密码
    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    public PageResultVO<WebUserListVO> getUserList(WebUserQueryDTO queryDTO) {
        log.info("查询用户列表，查询条件：{}", queryDTO);

        // 创建分页对象
        Page<WebUser> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<WebUser> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getUsername())) {
            queryWrapper.like(WebUser::getUsername, queryDTO.getUsername());
        }

        if (StringUtils.hasText(queryDTO.getRealName())) {
            queryWrapper.like(WebUser::getRealName, queryDTO.getRealName());
        }

        if (queryDTO.getRole() != null) {
            queryWrapper.eq(WebUser::getRole, queryDTO.getRole());
        }

        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(WebUser::getStatus, queryDTO.getStatus());
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(WebUser::getCreateTime);

        // 执行分页查询
        IPage<WebUser> userPage = webUserMapper.selectPage(page, queryWrapper);

        // 转换为VO列表
        List<WebUserListVO> voList = userPage.getRecords().stream()
                .map(this::convertToWebUserListVO)
                .collect(Collectors.toList());

        return new PageResultVO<>(voList, userPage.getTotal(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public WebUserDetailDTO getUserDetail(Long userId) {
        log.info("获取用户详情，用户ID：{}", userId);

        WebUser user = webUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return convertToWebUserDetailDTO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addUser(WebUserBasicDTO userDTO) {
        log.info("开始添加用户，用户名：{}", userDTO.getUsername());

        // 验证用户名是否已存在
        if (checkUsernameExists(userDTO.getUsername(), null)) {
            throw new BusinessException("用户名已存在");
        }

        // 验证密码不能为空
        if (!StringUtils.hasText(userDTO.getPassword())) {
            throw new BusinessException("密码不能为空");
        }

        // 创建用户记录
        WebUser user = new WebUser();
        BeanUtils.copyProperties(userDTO, user);

        // 使用BCrypt加密密码
        String password = StringUtils.hasText(userDTO.getPassword()) ?
                userDTO.getPassword() : DEFAULT_PASSWORD;
        String encodedPassword = bCryptUtil.encodePassword(password);
        user.setPassword(encodedPassword);

        int result = webUserMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException("添加用户失败");
        }

        log.info("添加用户成功，用户ID：{}，用户名：{}", user.getId(), user.getUsername());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, WebUserBasicDTO userDTO) {
        log.info("开始更新用户，用户ID：{}", userId);

        // 检查用户是否存在
        WebUser user = webUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证用户名是否已被其他用户使用
        if (!user.getUsername().equals(userDTO.getUsername()) &&
                checkUsernameExists(userDTO.getUsername(), userId)) {
            throw new BusinessException("用户名已被其他用户使用");
        }

        // 更新用户信息
        BeanUtils.copyProperties(userDTO, user, "id", "password", "createTime");

        // 如果勾选了修改密码，则更新密码
        if (userDTO.getChangePassword() != null && userDTO.getChangePassword()) {
            if (!StringUtils.hasText(userDTO.getNewPassword())) {
                throw new BusinessException("新密码不能为空");
            }
            // 使用BCrypt加密新密码
            String encodedPassword = bCryptUtil.encodePassword(userDTO.getNewPassword());
            user.setPassword(encodedPassword);
            log.info("用户密码已更新，用户ID：{}", userId);
        }

        int result = webUserMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException("更新用户失败");
        }

        log.info("更新用户成功，用户ID：{}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        log.info("开始删除用户，用户ID：{}", userId);

        // 检查用户是否存在
        WebUser user = webUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查是否是最后一个老板
        if (user.getRole() == 0) {
            LambdaQueryWrapper<WebUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WebUser::getRole, 0);
            Long adminCount = webUserMapper.selectCount(queryWrapper);

            if (adminCount <= 1) {
                throw new BusinessException("至少保留一个老板账号");
            }
        }

        int result = webUserMapper.deleteById(userId);
        if (result <= 0) {
            throw new BusinessException("删除用户失败");
        }

        log.info("删除用户成功，用户ID：{}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        log.info("更新用户状态，用户ID：{}，状态：{}", userId, status);

        // 检查状态值
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值只能是0（禁用）或1（正常）");
        }

        // 检查用户是否存在
        WebUser user = webUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 如果禁用老板账号，检查是否是最后一个
        if (status == 0 && user.getRole() == 0) {
            LambdaQueryWrapper<WebUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WebUser::getRole, 0);
            queryWrapper.eq(WebUser::getStatus, 1);
            Long activeAdminCount = webUserMapper.selectCount(queryWrapper);

            if (activeAdminCount <= 1) {
                throw new BusinessException("至少保留一个正常状态的老板账号");
            }
        }

        // 更新状态
        user.setStatus(status);
        int result = webUserMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException("更新用户状态失败");
        }

        log.info("更新用户状态成功，用户ID：{}，新状态：{}", userId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        log.info("重置用户密码，用户ID：{}", userId);

        // 检查用户是否存在
        WebUser user = webUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 设置新密码（如果newPassword为null，则使用默认密码）
        String password = StringUtils.hasText(newPassword) ? newPassword : DEFAULT_PASSWORD;
        // 使用BCrypt加密密码
        String encodedPassword = bCryptUtil.encodePassword(password);
        user.setPassword(encodedPassword);

        int result = webUserMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException("重置密码失败");
        }

        log.info("重置密码成功，用户ID：{}", userId);
    }

    @Override
    public boolean checkUsernameExists(String username, Long excludeUserId) {
        LambdaQueryWrapper<WebUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WebUser::getUsername, username);

        if (excludeUserId != null) {
            queryWrapper.ne(WebUser::getId, excludeUserId);
        }

        Long count = webUserMapper.selectCount(queryWrapper);
        return count > 0;
    }

    // ========== 私有辅助方法 ==========

    private WebUserListVO convertToWebUserListVO(WebUser user) {
        WebUserListVO vo = new WebUserListVO();
        BeanUtils.copyProperties(user, vo);

        // 设置角色描述
        vo.setRoleDesc(getRoleDesc(user.getRole()));

        // 设置状态描述
        vo.setStatusDesc(user.getStatus() == 1 ? "正常" : "禁用");

        return vo;
    }

    private WebUserDetailDTO convertToWebUserDetailDTO(WebUser user) {
        WebUserDetailDTO dto = new WebUserDetailDTO();
        BeanUtils.copyProperties(user, dto);

        // 设置角色描述
        dto.setRoleDesc(getRoleDesc(user.getRole()));

        // 设置状态描述
        dto.setStatusDesc(user.getStatus() == 1 ? "正常" : "禁用");

        return dto;
    }

    private String getRoleDesc(Integer role) {
        if (role == null) return "未知";
        switch (role) {
            case 0: return "老板";
            case 1: return "前台";
            default: return "未知";
        }
    }
}