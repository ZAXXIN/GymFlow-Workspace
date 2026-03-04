import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'

// 角色DTO
export interface RoleDTO {
  id?: number
  roleName: string
  roleCode: string
  description?: string
  status: number
  createTime?: string
  updateTime?: string
}

// 权限树DTO
export interface PermissionTreeDTO {
  id: number
  permissionName: string
  permissionCode: string
  parentId: number
  module?: string
  type: number
  sortOrder?: number
  status: number
  children?: PermissionTreeDTO[]
}

// 角色权限更新DTO
export interface RolePermissionUpdateDTO {
  permissionIds: number[]
}

// 角色权限API
export const rolePermissionApi = {
  /**
   * 获取角色列表
   */
  getRoleList(): Promise<ApiResponse<RoleDTO[]>> {
    return request({
      url: '/settings/roles',
      method: 'GET'
    })
  },

  /**
   * 获取角色详情
   */
  getRoleDetail(roleId: number): Promise<ApiResponse<RoleDTO>> {
    return request({
      url: `/settings/roles/${roleId}`,
      method: 'GET'
    })
  },

  /**
   * 新增角色
   */
  addRole(data: RoleDTO): Promise<ApiResponse<number>> {
    return request({
      url: '/settings/roles',
      method: 'POST',
      data
    })
  },

  /**
   * 更新角色
   */
  updateRole(roleId: number, data: RoleDTO): Promise<ApiResponse> {
    return request({
      url: `/settings/roles/${roleId}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 删除角色
   */
  deleteRole(roleId: number): Promise<ApiResponse> {
    return request({
      url: `/settings/roles/${roleId}`,
      method: 'DELETE'
    })
  },

  /**
   * 获取权限树
   */
  getPermissionTree(): Promise<ApiResponse<PermissionTreeDTO[]>> {
    return request({
      url: '/settings/permissions/tree',
      method: 'GET'
    })
  },

  /**
   * 获取角色的权限详情列表
   */
  getRolePermissions(roleId: number): Promise<ApiResponse<RolePermissionDetailDTO[]>> {
    return request({
      url: `/settings/roles/${roleId}/permissions`,
      method: 'GET'
    })
  },

  /**
   * 获取角色的权限ID列表
   */
  getRolePermissionIds(roleId: number): Promise<ApiResponse<number[]>> {
    return request({
      url: `/settings/roles/${roleId}/permission-ids`,
      method: 'GET'
    })
  },

  /**
   * 更新角色的权限
   */
  updateRolePermissions(roleId: number, data: RolePermissionUpdateDTO): Promise<ApiResponse> {
    return request({
      url: `/settings/roles/${roleId}/permissions`,
      method: 'PUT',
      data
    })
  }
}