import { defineStore } from 'pinia'
import { ref } from 'vue'
import { rolePermissionApi } from '@/api/settings/rolePermission'
import type { RoleDTO, PermissionTreeDTO } from '@/api/settings/rolePermission'
import { ElMessage } from 'element-plus'

export const useRolePermissionStore = defineStore('rolePermission', () => {
  // 状态
  const roles = ref<RoleDTO[]>([])
  const currentRole = ref<RoleDTO | null>(null)
  const permissionTree = ref<PermissionTreeDTO[]>([])
  const rolePermissions = ref<number[]>([])
  const rolePermissionDetails = ref<RolePermissionDetailDTO[]>([])
  const loading = ref(false)

  /**
   * 加载角色列表
   */
  const fetchRoles = async () => {
    try {
      loading.value = true
      const response = await rolePermissionApi.getRoleList()
      if (response.code === 200) {
        roles.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取角色列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载权限树
   */
  const fetchPermissionTree = async () => {
    try {
      const response = await rolePermissionApi.getPermissionTree()
      if (response.code === 200) {
        permissionTree.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取权限树失败:', error)
      throw error
    }
  }

  /**
   * 加载角色的权限详情
   */
  const fetchRolePermissionDetails = async (roleId: number) => {
    try {
      const response = await rolePermissionApi.getRolePermissions(roleId)
      if (response.code === 200) {
        rolePermissionDetails.value = response.data
        // 同时设置权限ID列表
        rolePermissions.value = response.data.map(item => item.permissionId)
      }
      return response.data
    } catch (error) {
      console.error('获取角色权限详情失败:', error)
      throw error
    }
  }

  /**
   * 加载角色的权限
   */
  const fetchRolePermissions = async (roleId: number) => {
    try {
      const response = await rolePermissionApi.getRolePermissionIds(roleId)
      if (response.code === 200) {
        rolePermissions.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取角色权限失败:', error)
      throw error
    }
  }

  /**
   * 新增角色
   */
  const addRole = async (data: RoleDTO) => {
    try {
      loading.value = true
      const response = await rolePermissionApi.addRole(data)
      if (response.code === 200) {
        ElMessage.success('角色添加成功')
        await fetchRoles()
      }
      return response
    } catch (error) {
      console.error('添加角色失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新角色
   */
  const updateRole = async (roleId: number, data: RoleDTO) => {
    try {
      loading.value = true
      const response = await rolePermissionApi.updateRole(roleId, data)
      if (response.code === 200) {
        ElMessage.success('角色更新成功')
        await fetchRoles()
        if (currentRole.value?.id === roleId) {
          currentRole.value = { ...currentRole.value, ...data }
        }
      }
      return response
    } catch (error) {
      console.error('更新角色失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除角色
   */
  const deleteRole = async (roleId: number) => {
    try {
      loading.value = true
      const response = await rolePermissionApi.deleteRole(roleId)
      if (response.code === 200) {
        ElMessage.success('角色删除成功')
        await fetchRoles()
        if (currentRole.value?.id === roleId) {
          currentRole.value = null
          rolePermissions.value = []
        }
      }
      return response
    } catch (error) {
      console.error('删除角色失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新角色权限
   */
  const updateRolePermissions = async (roleId: number, permissionIds: number[]) => {
    try {
      loading.value = true
      const response = await rolePermissionApi.updateRolePermissions(roleId, { permissionIds })
      if (response.code === 200) {
        ElMessage.success('权限配置保存成功')
      }
      return response
    } catch (error) {
      console.error('更新角色权限失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置当前角色
   */
  const setCurrentRole = (role: RoleDTO | null) => {
    currentRole.value = role
  }

  /**
   * 重置状态
   */
  const resetState = () => {
    roles.value = []
    currentRole.value = null
    permissionTree.value = []
    rolePermissions.value = []
    loading.value = false
  }

  return {
    // 状态
    roles,
    currentRole,
    permissionTree,
    rolePermissions,
    rolePermissionDetails,
    loading,

    // Actions
    fetchRoles,
    fetchPermissionTree,
    fetchRolePermissions,
    fetchRolePermissionDetails,
    addRole,
    updateRole,
    deleteRole,
    updateRolePermissions,
    setCurrentRole,
    resetState
  }
})