package com.gymflow.service.impl.settings;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.settings.webUser.WebUserQueryDTO;
import com.gymflow.dto.settings.webUser.WebUserBasicDTO;
import com.gymflow.dto.settings.webUser.WebUserDetailDTO;
import com.gymflow.entity.auth.Role;
import com.gymflow.entity.settings.WebUser;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.auth.RoleMapper;
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
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebUserServiceImpl implements WebUserService {

    private final WebUserMapper webUserMapper;
    private final RoleMapper roleMapper; // 注入 RoleMapper
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

        if (queryDTO.getRole() != null) {
            queryWrapper.eq(WebUser::getRoleId, queryDTO.getRole().longValue());
        }

        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(WebUser::getStatus, queryDTO.getStatus());
        }

        queryWrapper.orderByDesc(WebUser::getCreateTime);

        // 执行分页查询
        IPage<WebUser> userPage = webUserMapper.selectPage(page, queryWrapper);

        // 获取所有角色信息（用于转换）
        List<Role> roles = roleMapper.selectList(null);
        Map<Long, Role> roleMap = roles.stream().collect(Collectors.toMap(Role::getId, r -> r));

        // 转换为VO列表
        List<WebUserListVO> voList = userPage.getRecords().stream()
                .map(user -> convertToWebUserListVO(user, roleMap))
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

        // 获取角色信息
        Role role = null;
        if (user.getRoleId() != null) {
            role = roleMapper.selectById(user.getRoleId());
        }

        return convertToWebUserDetailDTO(user, role);
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

        // 验证角色是否存在
        if (userDTO.getRole() != null) {
            Role role = roleMapper.selectById(userDTO.getRole().longValue());
            if (role == null) {
                throw new BusinessException("角色不存在");
            }
        }

        // 创建用户记录
        WebUser user = new WebUser();
        user.setUsername(userDTO.getUsername());
        user.setRoleId(userDTO.getRole() != null ? userDTO.getRole().longValue() : null);
        user.setStatus(userDTO.getStatus());

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

        // 验证角色是否存在
        if (userDTO.getRole() != null) {
            Role role = roleMapper.selectById(userDTO.getRole().longValue());
            if (role == null) {
                throw new BusinessException("角色不存在");
            }
        }

        // 更新用户信息
        user.setUsername(userDTO.getUsername());
        if (userDTO.getRole() != null) {
            user.setRoleId(userDTO.getRole().longValue());
        }
        user.setStatus(userDTO.getStatus());

        // 如果勾选了修改密码，则更新密码
        if (userDTO.getChangePassword() != null && userDTO.getChangePassword()) {
            if (!StringUtils.hasText(userDTO.getNewPassword())) {
                throw new BusinessException("新密码不能为空");
            }
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

        WebUser user = webUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查是否是最后一个老板
        if (user.getRoleId() != null) {
            Role role = roleMapper.selectById(user.getRoleId());
            if (role != null && "BOSS".equals(role.getRoleCode())) {
                LambdaQueryWrapper<WebUser> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(WebUser::getRoleId, user.getRoleId());
                Long adminCount = webUserMapper.selectCount(queryWrapper);

                if (adminCount <= 1) {
                    throw new BusinessException("至少保留一个老板账号");
                }
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

        if (status != 0 && status != 1) {
            throw new BusinessException("状态值只能是0（禁用）或1（正常）");
        }

        WebUser user = webUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 如果禁用老板账号，检查是否是最后一个
        if (status == 0 && user.getRoleId() != null) {
            Role role = roleMapper.selectById(user.getRoleId());
            if (role != null && "BOSS".equals(role.getRoleCode())) {
                LambdaQueryWrapper<WebUser> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(WebUser::getRoleId, user.getRoleId());
                queryWrapper.eq(WebUser::getStatus, 1);
                Long activeAdminCount = webUserMapper.selectCount(queryWrapper);

                if (activeAdminCount <= 1) {
                    throw new BusinessException("至少保留一个正常状态的老板账号");
                }
            }
        }

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

        WebUser user = webUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String password = StringUtils.hasText(newPassword) ? newPassword : DEFAULT_PASSWORD;
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
    private WebUserListVO convertToWebUserListVO(WebUser user, Map<Long, Role> roleMap) {
        WebUserListVO vo = new WebUserListVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());

        if (user.getRoleId() != null) {
            vo.setRole(user.getRoleId().intValue());
            Role role = roleMap.get(user.getRoleId());
            if (role != null) {
                vo.setRoleDesc(role.getRoleName());
            } else {
                vo.setRoleDesc("未知");
            }
        } else {
            vo.setRole(null);
            vo.setRoleDesc("未分配");
        }

        vo.setStatus(user.getStatus());
        vo.setStatusDesc(user.getStatus() == 1 ? "正常" : "禁用");
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());

        return vo;
    }

    private WebUserDetailDTO convertToWebUserDetailDTO(WebUser user, Role role) {
        WebUserDetailDTO dto = new WebUserDetailDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());

        if (user.getRoleId() != null) {
            dto.setRole(user.getRoleId().intValue());
            if (role != null) {
                dto.setRoleDesc(role.getRoleName());
            } else {
                dto.setRoleDesc("未知");
            }
        } else {
            dto.setRole(null);
            dto.setRoleDesc("未分配");
        }

        dto.setStatus(user.getStatus());
        dto.setStatusDesc(user.getStatus() == 1 ? "正常" : "禁用");
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());

        return dto;
    }
}