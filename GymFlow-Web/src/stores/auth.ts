import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { type User, type UserRole } from '@/types'
import { StorageKeys } from '@/utils/constants'
import { authApi } from '@/api/auth'
import { systemConfigApi } from '@/api/settings/systemConfig' // 导入 systemConfig API
import type { PermissionCode } from '@/types/permission'
// 导入Element Plus的提示组件（可选，用于统一提示）
import { ElMessage } from 'element-plus'

// 定义登录请求参数类型（和后端LoginDTO一致）
type LoginParams = {
  username: string
  password: string
}

// 定义后端响应的通用类型（和后端ApiResponse一致）
type ApiResponse<T = any> = {
  code: number
  message: string
  data: T
}

// 定义登录响应数据类型（根据实际返回数据调整）
type LoginResponseData = {
  userId: number
  username: string
  realName: string
  phone: string | null
  role: number
  token: string
  loginTime: string
}

// 定义权限响应数据类型
type PermissionResponseData = {
  permissions: PermissionCode[]
  menus: any[]
}

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem(StorageKeys.TOKEN))
  const userInfo = ref<User | null>(JSON.parse(localStorage.getItem(StorageKeys.USER_INFO) || 'null'))
  const permissions = ref<PermissionCode[]>(JSON.parse(localStorage.getItem('gymflow_permissions') || '[]'))
  const menus = ref<any[]>(JSON.parse(localStorage.getItem('gymflow_menus') || '[]'))
  const loading = ref(false)
  const initialized = ref(false)

  // Getter
  const isLoggedIn = computed(() => {
    // 如果有token并且已经初始化过，才认为是登录状态
    return !!token.value && initialized.value
  })
  const userRole = computed(() => userInfo.value?.role || null)
  const isAdmin = computed(() => userRole.value === 0) // 0-老板
  const isReceptionist = computed(() => userRole.value === 1) // 1-前台
  // const isCoach = computed(() => userRole.value === 2) // 2-教练
  // const isMember = computed(() => userRole.value === 3) // 3-会员

  // 权限相关 Getters
  const hasPermission = (permission: PermissionCode | PermissionCode[]): boolean => {
    if (!permission) return true
    
    // 如果是数组，检查是否包含任一权限（OR关系）
    if (Array.isArray(permission)) {
      return permission.some(p => permissions.value.includes(p))
    }
    
    return permissions.value.includes(permission)
  }

  const hasAllPermissions = (requiredPermissions: PermissionCode[]): boolean => {
    return requiredPermissions.every(p => permissions.value.includes(p))
  }

  const getPermissions = computed(() => permissions.value)
  const getMenus = computed(() => menus.value)

  // Actions
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem(StorageKeys.TOKEN, newToken)
  }

  const setUserInfo = (info: User) => {
    userInfo.value = info
    localStorage.setItem(StorageKeys.USER_INFO, JSON.stringify(info))
  }

  const setPermissions = (perms: PermissionCode[]) => {
    permissions.value = perms
    localStorage.setItem('gymflow_permissions', JSON.stringify(perms))

    // 🔥 在这里打印用户权限
    console.log('========== 用户权限列表 ==========')
    console.log('用户名:', userInfo.value?.username)
    console.log('角色:', userInfo.value?.role === 0 ? '老板' : '前台')
    console.log('权限数量:', perms.length)
    console.log('权限列表:', perms)
    console.log('================================')
  }

  const setMenus = (menuList: any[]) => {
    menus.value = menuList
    localStorage.setItem('gymflow_menus', JSON.stringify(menuList))
  }

  const clearAuth = () => {
    token.value = null
    userInfo.value = null
    permissions.value = []
    menus.value = []
    localStorage.removeItem(StorageKeys.TOKEN)
    localStorage.removeItem(StorageKeys.USER_INFO)
    localStorage.removeItem('gymflow_permissions')
    localStorage.removeItem('gymflow_menus')
  }

  /**
   * 验证token有效性 - 改为调用系统配置接口
   */
  const validateToken = async (): Promise<boolean> => {
    if (!token.value) {
      initialized.value = false
      return false
    }

    try {
      // 调用系统配置接口验证token
      const response = await systemConfigApi.getConfig()
      if (response.code === 200) {
        // 接口调用成功，说明token有效
        // 注意：这里不设置用户信息，因为系统配置接口不返回用户信息
        // 用户信息应该已经在登录时存储了
        initialized.value = true
        return true
      } else {
        // token无效，清除登录状态
        clearAuth()
        return false
      }
    } catch (error) {
      console.error('Token验证失败:', error)
      clearAuth()
      return false
    }
  }

  /**
   * 初始化认证状态（在应用启动时调用）
   */
  const initAuth = async () => {
    if (token.value) {
      await validateToken()
    } else {
      initialized.value = false
    }
  }

  /**
   * 根据角色设置默认权限
   */
  const setDefaultPermissionsByRole = (role?: number) => {
    if (role === undefined) return
    
    let defaultPerms: PermissionCode[] = []
    
    if (role === 0) { // 老板
      defaultPerms = [
        'member:view', 'member:detail', 'member:add', 'member:edit', 'member:delete', 'member:batch:delete',
        'member:card:renew', 'member:health:view', 'member:health:add',
        'coach:view', 'coach:detail', 'coach:add', 'coach:edit', 'coach:delete', 'coach:batch:delete',
        'coach:schedule:view', 'coach:schedule:set',
        'course:view', 'course:detail', 'course:add', 'course:edit', 'course:delete',
        'course:schedule:view', 'course:schedule:set', 'course:booking:add', 'course:booking:cancel',
        'checkIn:view', 'checkIn:detail', 'checkIn:member:add', 'checkIn:course:add',
        'checkIn:edit', 'checkIn:delete', 'checkIn:verify',
        'order:view', 'order:detail', 'order:add', 'order:edit', 'order:cancel', 'order:delete',
        'order:pay', 'order:refund',
        'product:view', 'product:detail', 'product:add', 'product:edit', 'product:delete',
        'product:status', 'product:stock', 'product:category:view', 'product:category:manage',
        'settings:user:view', 'settings:user:add', 'settings:user:edit', 'settings:user:delete',
        'settings:user:status', 'settings:user:resetpwd',
        'settings:config:view', 'settings:config:edit',
        'common:upload'
      ]
    } else if (role === 1) { // 前台
      defaultPerms = [
        'member:view', 'member:detail', 'member:add', 'member:edit',
        'member:card:renew', 'member:health:view', 'member:health:add',
        'coach:view', 'coach:detail', 'coach:schedule:view',
        'course:view', 'course:detail', 'course:schedule:view', 'course:booking:add', 'course:booking:cancel',
        'checkIn:view', 'checkIn:detail', 'checkIn:member:add', 'checkIn:course:add',
        'checkIn:edit', 'checkIn:verify',
        'order:view', 'order:detail', 'order:add', 'order:cancel', 'order:pay',
        'product:view', 'product:detail', 'product:category:view',
        'settings:config:view',
        'common:upload'
      ]
    } 
    
    if (defaultPerms.length > 0) {
      console.log('使用基于角色的默认权限:', role === 0 ? '老板' : '前台')
      setPermissions(defaultPerms)
    }
  }

  /**
   * 获取用户权限
   */
  const fetchUserPermissions = async () => {
    try {
      // 调用获取权限的接口
      const response: ApiResponse<PermissionResponseData> = await authApi.getUserPermissions()
      
      if (response.code === 200 && response.data) {
        setPermissions(response.data.permissions)
        setMenus(response.data.menus)
      } else {
        // 如果接口返回失败，使用默认权限
        console.warn('获取权限接口返回失败，使用默认权限')
        setDefaultPermissionsByRole(userInfo.value?.role)
      }
    } catch (error) {
      console.error('获取权限失败:', error)
      // 如果获取失败，使用基于角色的默认权限
      console.warn('获取权限接口异常，使用默认权限')
      setDefaultPermissionsByRole(userInfo.value?.role)
    }
  }

  /**
   * 登录
   */
  const login = async (params: LoginParams) => {
    try {
      loading.value = true
      const response: ApiResponse<LoginResponseData> = await authApi.login(params)
      
      console.log('登录返回数据:', response.data)
      
      if (response.code === 200 && response.data) {
        // 存储token
        setToken(response.data.token)
        
        // 存储用户信息
        const user: User = {
          id: response.data.userId,
          username: response.data.username,
          realName: response.data.realName,
          phone: response.data.phone || '',
          role: response.data.role,
          status: 1
        }
        setUserInfo(user)
        
        // 登录成功后，主动获取权限
        await fetchUserPermissions()
        
        // 标记为已初始化
        initialized.value = true
        
        ElMessage.success('登录成功')
        return { success: true, ...response }
      } else {
        ElMessage.error(response.message || '登录失败')
        return { success: false, ...response }
      }
    } catch (error: any) {
      console.error('Login failed:', error)
      const errMsg = error.message || '网络异常，登录失败'
      ElMessage.error(errMsg)
      return { success: false, code: 500, message: errMsg, data: null }
    } finally {
      loading.value = false
    }
  }

  /**
   * 登出
   */
  const logout = async () => {
    try {
      await authApi.logout()
      ElMessage.success('退出登录成功')
    } catch (error) {
      console.error('Logout failed:', error)
      ElMessage.warning('退出登录失败，已强制清除登录状态')
    } finally {
      clearAuth()
    }
  }

  /**
   * 刷新用户信息 - 现在调用系统配置接口
   */
  const refreshUserInfo = async () => {
    try {
      const response = await systemConfigApi.getConfig()
      if (response.code === 200) {
        // 系统配置接口不返回用户信息，所以只标记为已初始化
        initialized.value = true
        return userInfo.value
      }
      throw new Error(response.message || '刷新用户信息失败')
    } catch (error) {
      console.error('Refresh user info failed:', error)
      throw error
    }
  }

  /**
   * 刷新用户权限
   */
  const refreshPermissions = async () => {
    return await fetchUserPermissions()
  }

  /**
   * 检查登录状态
   */
  const checkAuth = () => {
    return !!token.value && initialized.value
  }

  return {
    // 状态
    token,
    userInfo,
    permissions,
    menus,
    loading,
    initialized,
    
    // Getter
    isLoggedIn,
    userRole,
    isAdmin,
    isReceptionist,
    // isCoach,
    // isMember,
    
    // 权限相关
    hasPermission,
    hasAllPermissions,
    getPermissions,
    getMenus,
    
    // Actions
    setToken,
    setUserInfo,
    clearAuth,
    login,
    logout,
    refreshUserInfo,
    refreshPermissions,
    checkAuth,
    initAuth,
    validateToken
  }
})