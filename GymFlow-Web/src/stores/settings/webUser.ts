import { defineStore } from 'pinia'
import { ref } from 'vue'
import { webUserApi } from '@/api/settings'
import type { 
  WebUserQueryParams, 
  WebUserBasicDTO, 
  WebUserListVO, 
  WebUserDetailDTO,
  PageResultVO
} from '@/api/settings'

export const useWebUserStore = defineStore('webUser', () => {
  const userList = ref<WebUserListVO[]>([])
  const currentUser = ref<WebUserDetailDTO | null>(null)
  const total = ref(0)
  const loading = ref(false)
  const pageInfo = ref({
    pageNum: 1,
    pageSize: 10,
    totalPages: 0
  })
  
  /**
   * 分页查询用户列表
   */
  const fetchUserList = async (params: WebUserQueryParams = {}) => {
    try {
      loading.value = true
      const queryParams = {
        pageNum: params.pageNum || pageInfo.value.pageNum,
        pageSize: params.pageSize || pageInfo.value.pageSize,
        ...params
      }
      
      const response = await webUserApi.getUserList(queryParams)
      if (response.code === 200) {
        userList.value = response.data.list
        total.value = response.data.total
        pageInfo.value = {
          pageNum: response.data.pageNum,
          pageSize: response.data.pageSize,
          totalPages: response.data.pages || Math.ceil(response.data.total / response.data.pageSize)
        }
      }
      return response.data
    } catch (error) {
      console.error('获取用户列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户详情
   */
  const fetchUserDetail = async (userId: number) => {
    try {
      loading.value = true
      const response = await webUserApi.getUserDetail(userId)
      if (response.code === 200) {
        currentUser.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取用户详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加用户
   */
  const addUser = async (data: WebUserBasicDTO) => {
    try {
      loading.value = true
      const response = await webUserApi.addUser(data)
      if (response.code === 200) {
        // 添加成功后重新加载列表
        await fetchUserList({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
      }
      return response
    } catch (error) {
      console.error('添加用户失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新用户
   */
  const updateUser = async (userId: number, data: WebUserBasicDTO) => {
    try {
      loading.value = true
      const response = await webUserApi.updateUser(userId, data)
      if (response.code === 200) {
        // 更新成功后刷新当前用户详情
        if (currentUser.value?.id === userId) {
          await fetchUserDetail(userId)
        }
        // 刷新列表
        await fetchUserList({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
      }
      return response
    } catch (error) {
      console.error('更新用户失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除用户
   */
  const deleteUser = async (userId: number) => {
    try {
      loading.value = true
      const response = await webUserApi.deleteUser(userId)
      if (response.code === 200) {
        // 从本地列表中移除
        userList.value = userList.value.filter(item => item.id !== userId)
        total.value -= 1
        
        // 如果删除的是当前查看的用户，清空当前用户数据
        if (currentUser.value?.id === userId) {
          currentUser.value = null
        }
      }
      return response
    } catch (error) {
      console.error('删除用户失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新用户状态
   */
  const updateUserStatus = async (userId: number, status: number) => {
    try {
      loading.value = true
      const response = await webUserApi.updateUserStatus(userId, status)
      if (response.code === 200) {
        // 更新列表中的状态
        const index = userList.value.findIndex(item => item.id === userId)
        if (index !== -1) {
          userList.value[index].status = status
          userList.value[index].statusDesc = status === 1 ? '正常' : '禁用'
        }
        // 更新当前用户的状态
        if (currentUser.value?.id === userId) {
          currentUser.value.status = status
          currentUser.value.statusDesc = status === 1 ? '正常' : '禁用'
        }
      }
      return response
    } catch (error) {
      console.error('更新用户状态失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置密码
   */
  const resetPassword = async (userId: number) => {
    try {
      loading.value = true
      const response = await webUserApi.resetPassword(userId)
      return response
    } catch (error) {
      console.error('重置密码失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 检查用户名是否存在
   */
  const checkUsernameExists = async (username: string, excludeUserId?: number): Promise<boolean> => {
    try {
      const response = await webUserApi.checkUsernameExists(username, excludeUserId)
      if (response.code === 200) {
        return response.data
      }
      return false
    } catch (error) {
      console.error('检查用户名失败:', error)
      return false
    }
  }

  /**
   * 设置分页信息
   */
  const setPageInfo = (pageNum: number, pageSize: number) => {
    pageInfo.value.pageNum = pageNum
    pageInfo.value.pageSize = pageSize
  }

  /**
   * 清空当前用户数据
   */
  const clearCurrentUser = () => {
    currentUser.value = null
  }

  /**
   * 重置状态
   */
  const resetState = () => {
    userList.value = []
    currentUser.value = null
    total.value = 0
    loading.value = false
    pageInfo.value = {
      pageNum: 1,
      pageSize: 10,
      totalPages: 0
    }
  }

  // Getters
  const hasNextPage = () => {
    return pageInfo.value.pageNum < pageInfo.value.totalPages
  }

  const hasPrevPage = () => {
    return pageInfo.value.pageNum > 1
  }

  // 格式化用户列表
  const formattedUserList = () => {
    return userList.value.map(user => ({
      ...user,
      createTimeFormatted: user.createTime ? new Date(user.createTime).toLocaleString() : '-',
      updateTimeFormatted: user.updateTime ? new Date(user.updateTime).toLocaleString() : '-'
    }))
  }

  return {
    // 状态
    userList,
    currentUser,
    total,
    loading,
    pageInfo,
    
    // Actions
    fetchUserList,
    fetchUserDetail,
    addUser,
    updateUser,
    deleteUser,
    updateUserStatus,
    resetPassword,
    checkUsernameExists,
    setPageInfo,
    clearCurrentUser,
    resetState,
    
    // Getters
    hasNextPage,
    hasPrevPage,
    formattedUserList
  }
})