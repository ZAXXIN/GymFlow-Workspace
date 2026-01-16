import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { type User, type UserRole } from '@/types'
import { StorageKeys } from '@/utils/constants'
import { authApi } from '@/api/auth'

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

  // 登录
  const login = async (username: string, password: string) => {
    try {
      loading.value = true
      const response = await authApi.login({ username, password })
      
      setToken(response.token)
      setUserInfo(response.user)
      
      return { success: true, data: response }
    } catch (error) {
      console.error('Login failed:', error)
      return { success: false, error }
    } finally {
      loading.value = false
    }
  }

  // 登出
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('Logout failed:', error)
    } finally {
      clearAuth()
    }
  }

  // 刷新用户信息
  const refreshUserInfo = async () => {
    try {
      const response = await authApi.getUserInfo()
      setUserInfo(response)
      return response
    } catch (error) {
      console.error('Refresh user info failed:', error)
      throw error
    }
  }

  // 检查登录状态
  const checkAuth = () => {
    if (!token.value) {
      return false
    }
    
    // 可以添加token过期检查逻辑
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