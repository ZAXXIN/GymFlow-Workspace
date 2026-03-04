package com.gymflow.service.impl.settings;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymflow.dto.settings.rolePermission.PermissionTreeDTO;
import com.gymflow.dto.settings.rolePermission.RoleDTO;
import com.gymflow.dto.settings.rolePermission.RolePermissionDetailDTO;
import com.gymflow.entity.auth.Permission;
import com.gymflow.entity.auth.Role;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.auth.PermissionMapper;
import com.gymflow.mapper.auth.RoleMapper;
import com.gymflow.service.settings.RolePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public List<RoleDTO> getRoleList() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Role::getCreateTime);

        List<Role> roles = roleMapper.selectList(queryWrapper);

        return roles.stream()
                .map(this::convertToRoleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDTO getRoleDetail(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return convertToRoleDTO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addRole(RoleDTO roleDTO) {
        log.info("开始添加角色，角色名称：{}，角色编码：{}", roleDTO.getRoleName(), roleDTO.getRoleCode());

        // 检查角色编码是否已存在
        LambdaQueryWrapper<Role> codeQuery = new LambdaQueryWrapper<>();
        codeQuery.eq(Role::getRoleCode, roleDTO.getRoleCode());
        Long codeCount = roleMapper.selectCount(codeQuery);
        if (codeCount > 0) {
            throw new BusinessException("角色编码已存在");
        }

        // 检查角色名称是否已存在
        LambdaQueryWrapper<Role> nameQuery = new LambdaQueryWrapper<>();
        nameQuery.eq(Role::getRoleName, roleDTO.getRoleName());
        Long nameCount = roleMapper.selectCount(nameQuery);
        if (nameCount > 0) {
            throw new BusinessException("角色名称已存在");
        }

        // 创建角色
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);

        int result = roleMapper.insert(role);
        if (result <= 0) {
            throw new BusinessException("添加角色失败");
        }

        log.info("添加角色成功，角色ID：{}", role.getId());
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long roleId, RoleDTO roleDTO) {
        log.info("开始更新角色，角色ID：{}", roleId);

        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 不能修改BOSS角色编码
        if ("BOSS".equals(role.getRoleCode()) && !roleDTO.getRoleCode().equals(role.getRoleCode())) {
            throw new BusinessException("不能修改系统内置角色的编码");
        }

        // 检查角色编码是否已被其他角色使用
        if (!role.getRoleCode().equals(roleDTO.getRoleCode())) {
            LambdaQueryWrapper<Role> codeQuery = new LambdaQueryWrapper<>();
            codeQuery.eq(Role::getRoleCode, roleDTO.getRoleCode());
            codeQuery.ne(Role::getId, roleId);
            Long codeCount = roleMapper.selectCount(codeQuery);
            if (codeCount > 0) {
                throw new BusinessException("角色编码已被其他角色使用");
            }
        }

        // 检查角色名称是否已被其他角色使用
        if (!role.getRoleName().equals(roleDTO.getRoleName())) {
            LambdaQueryWrapper<Role> nameQuery = new LambdaQueryWrapper<>();
            nameQuery.eq(Role::getRoleName, roleDTO.getRoleName());
            nameQuery.ne(Role::getId, roleId);
            Long nameCount = roleMapper.selectCount(nameQuery);
            if (nameCount > 0) {
                throw new BusinessException("角色名称已被其他角色使用");
            }
        }

        // 更新角色
        BeanUtils.copyProperties(roleDTO, role, "id", "createTime");
        int result = roleMapper.updateById(role);
        if (result <= 0) {
            throw new BusinessException("更新角色失败");
        }

        log.info("更新角色成功，角色ID：{}", roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        log.info("开始删除角色，角色ID：{}", roleId);

        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 不能删除系统内置角色
        if ("BOSS".equals(role.getRoleCode())) {
            throw new BusinessException("不能删除系统内置角色");
        }

        // 检查是否有用户使用该角色
        Integer userCount = roleMapper.countUsersByRoleId(roleId);
        if (userCount != null && userCount > 0) {
            throw new BusinessException("该角色下还有用户，不能删除");
        }

        // 删除角色权限关联
        permissionMapper.deleteRolePermissions(roleId);

        // 删除角色
        int result = roleMapper.deleteById(roleId);
        if (result <= 0) {
            throw new BusinessException("删除角色失败");
        }

        log.info("删除角色成功，角色ID：{}", roleId);
    }

    @Override
    public List<PermissionTreeDTO> getPermissionTree() {
        // 查询所有启用的权限
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getStatus, 1);
        queryWrapper.orderByAsc(Permission::getSortOrder);

        List<Permission> permissions = permissionMapper.selectList(queryWrapper);

        // 转换为DTO并构建树形结构
        List<PermissionTreeDTO> dtoList = permissions.stream()
                .map(this::convertToPermissionTreeDTO)
                .collect(Collectors.toList());

        return buildPermissionTree(dtoList);
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        return permissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    public List<RolePermissionDetailDTO> getRolePermissionDetails(Long roleId) {
        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        return permissionMapper.selectPermissionDetailsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(Long roleId, List<Long> permissionIds) {
        log.info("更新角色权限，角色ID：{}，权限数量：{}", roleId, permissionIds.size());

        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 删除原有的权限关联
        permissionMapper.deleteRolePermissions(roleId);

        // 添加新的权限关联
        if (!CollectionUtils.isEmpty(permissionIds)) {
            int insertCount = permissionMapper.insertRolePermissions(roleId, permissionIds);
            log.info("为角色ID：{} 添加了 {} 个权限", roleId, insertCount);
        }

        log.info("更新角色权限成功，角色ID：{}", roleId);
    }

    // ========== 私有辅助方法 ==========

    private RoleDTO convertToRoleDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        BeanUtils.copyProperties(role, dto);
        return dto;
    }

    private PermissionTreeDTO convertToPermissionTreeDTO(Permission permission) {
        PermissionTreeDTO dto = new PermissionTreeDTO();
        BeanUtils.copyProperties(permission, dto);
        return dto;
    }

    private List<PermissionTreeDTO> buildPermissionTree(List<PermissionTreeDTO> permissions) {
        // 构建ID到DTO的映射
        Map<Long, PermissionTreeDTO> dtoMap = permissions.stream()
                .collect(Collectors.toMap(PermissionTreeDTO::getId, dto -> dto));

        List<PermissionTreeDTO> rootList = new ArrayList<>();

        for (PermissionTreeDTO dto : permissions) {
            if (dto.getParentId() == null || dto.getParentId() == 0L) {
                // 顶级菜单
                rootList.add(dto);
            } else {
                // 子菜单，添加到父菜单的children中
                PermissionTreeDTO parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                } else {
                    // 如果父菜单不存在，也作为顶级菜单（避免数据错误）
                    rootList.add(dto);
                }
            }
        }

        return rootList;
    }
}