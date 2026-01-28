import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User, QueryParams, PaginatedResponse } from '@/types'

export const useUserStore = defineStore('user', () => {
  // 状态
  const users = ref<User[]>([])
  const currentUser = ref<User | null>(null)
  const total = ref(0)
  const loading = ref(false)

  // Actions
  const fetchUsers = async (params: QueryParams = {}) => {
    try {
      loading.value = true
      const response: PaginatedResponse<User> = await userApi.getUsers(params)
      users.value = response.items
      total.value = response.total
      return response
    } catch (error) {
      console.error('Fetch users failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchUserById = async (id: number) => {
    try {
      loading.value = true
      const response = await userApi.getUserById(id)
      currentUser.value = response
      return response
    } catch (error) {
      console.error('Fetch user by id failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const createUser = async (userData: Partial<User>) => {
    try {
      loading.value = true
      const response = await userApi.createUser(userData)
      return response
    } catch (error) {
      console.error('Create user failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const updateUser = async (id: number, userData: Partial<User>) => {
    try {
      loading.value = true
      const response = await userApi.updateUser(id, userData)
      // 更新本地数据
      const index = users.value.findIndex(user => user.id === id)
      if (index !== -1) {
        users.value[index] = { ...users.value[index], ...response }
      }
      return response
    } catch (error) {
      console.error('Update user failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const deleteUser = async (id: number) => {
    try {
      loading.value = true
      await userApi.deleteUser(id)
      // 从本地删除
      users.value = users.value.filter(user => user.id !== id)
      total.value -= 1
    } catch (error) {
      console.error('Delete user failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const resetUserState = () => {
    users.value = []
    currentUser.value = null
    total.value = 0
  }

  return {
    // 状态
    users,
    currentUser,
    total,
    loading,
    
    // Actions
    fetchUsers,
    fetchUserById,
    createUser,
    updateUser,
    deleteUser,
    resetUserState
  }
})