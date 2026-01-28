import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { type User, type UserRole } from '@/types'
import { StorageKeys } from '@/utils/constants'
import { authApi } from '@/api/auth'
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

// 定义登录响应数据类型
type LoginResponseData = {
  token: string
  user: User
}

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem(StorageKeys.TOKEN))
  const userInfo = ref<User | null>(JSON.parse(localStorage.getItem(StorageKeys.USER_INFO) || 'null'))
  const loading = ref(false)

  // Getter
  const isLoggedIn = computed(() => !!token.value)
  const userRole = computed(() => userInfo.value?.role || null)
  const isAdmin = computed(() => userRole.value === UserRole.ADMIN)
  const isCoach = computed(() => userRole.value === UserRole.COACH)
  const isMember = computed(() => userRole.value === UserRole.MEMBER)

  // Actions
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem(StorageKeys.TOKEN, newToken)
  }

  const setUserInfo = (info: User) => {
    userInfo.value = info
    localStorage.setItem(StorageKeys.USER_INFO, JSON.stringify(info))
  }

  const clearAuth = () => {
    token.value = null
    userInfo.value = null
    localStorage.removeItem(StorageKeys.TOKEN)
    localStorage.removeItem(StorageKeys.USER_INFO)
  }

  // 登录（核心修正：接收对象参数，适配后端响应格式）
  const login = async (params: LoginParams) => {
    try {
      loading.value = true
      // 调用接口，传递{ username, password }对象（确保是字符串类型）
      const response: ApiResponse<LoginResponseData> = await authApi.login(params)
      
      // 处理后端响应（适配标准的code/data/message格式）
      if (response.code === 200 && response.data) {
        // 存储token和用户信息
        setToken(response.data.token)
        setUserInfo(response.data.user)
        ElMessage.success('登录成功')
        return { success: true, ...response }
      } else {
        ElMessage.error(response.message || '登录失败')
        return { success: false, ...response }
      }
    } catch (error: any) {
      console.error('Login failed:', error)
      // 统一错误返回格式，避免前端报错
      const errMsg = error.message || '网络异常，登录失败'
      ElMessage.error(errMsg)
      return { success: false, code: 500, message: errMsg, data: null }
    } finally {
      loading.value = false
    }
  }

  // 登出（优化：添加提示）
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

  // 刷新用户信息（适配后端响应格式）
  const refreshUserInfo = async () => {
    try {
      const response: ApiResponse<User> = await authApi.getUserInfo()
      if (response.code === 200 && response.data) {
        setUserInfo(response.data)
        return response.data
      }
      throw new Error(response.message || '刷新用户信息失败')
    } catch (error) {
      console.error('Refresh user info failed:', error)
      throw error // 抛出错误，让调用方处理
    }
  }

  // 检查登录状态
  const checkAuth = () => {
    if (!token.value) {
      return false
    }
    // 可选：添加token过期检查（比如解析token的过期时间）
    // const tokenExp = getTokenExpiration(token.value)
    // return tokenExp > Date.now()
    return true
  }

  return {
    // 状态
    token,
    userInfo,
    loading,
    
    // Getter
    isLoggedIn,
    userRole,
    isAdmin,
    isCoach,
    isMember,
    
    // Actions
    setToken,
    setUserInfo,
    clearAuth,
    login,
    logout,
    refreshUserInfo,
    checkAuth
  }
})