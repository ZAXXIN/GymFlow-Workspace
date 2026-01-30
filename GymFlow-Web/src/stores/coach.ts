// src/store/modules/coach.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { coachApi } from '@/api/coach'
import type { 
  CoachQueryParams, 
  CoachBasicDTO, 
  CoachScheduleDTO,
  CoachListVO,
  CoachDetail,
  CoachScheduleList,
  CoachCourseList,
  PageResultVO
} from '@/types/coach'

export const useCoachStore = defineStore('coach', () => {
  const coachList = ref<CoachListVO[]>([])
  const currentCoach = ref<CoachDetail | null>(null)
  const coachSchedules = ref<CoachScheduleList[]>([])
  const coachCourses = ref<CoachCourseList[]>([])
  const total = ref(0)
  const loading = ref(false)
  const pageInfo = ref({
    pageNum: 1,
    pageSize: 10,
    totalPages: 0
  })
  
  /**
   * 分页查询教练列表
   */
  const fetchCoachList = async (params: CoachQueryParams = {}) => {
    try {
      loading.value = true
      const queryParams = {
        pageNum: params.pageNum || pageInfo.value.pageNum,
        pageSize: params.pageSize || pageInfo.value.pageSize,
        ...params
      }
      
      const response = await coachApi.getCoachList(queryParams)
      if (response.code === 200) {
        coachList.value = response.data.list
        total.value = response.data.total
        pageInfo.value = {
          pageNum: response.data.pageNum,
          pageSize: response.data.pageSize,
          totalPages: response.data.pages || Math.ceil(response.data.total / response.data.pageSize)
        }
      }
      return response.data
    } catch (error) {
      console.error('获取教练列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加教练
   */
  const addCoach = async (data: CoachBasicDTO) => {
    try {
      loading.value = true
      const response = await coachApi.addCoach(data)
      if (response.code === 200) {
        // 添加成功后重新加载列表
        await fetchCoachList({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
      }
      return response
    } catch (error) {
      console.error('添加教练失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新教练信息
   */
  const updateCoach = async (coachId: number, data: CoachBasicDTO) => {
    try {
      loading.value = true
      const response = await coachApi.updateCoach(coachId, data)
      if (response.code === 200) {
        // 更新成功后刷新当前教练详情
        if (currentCoach.value?.id === coachId) {
          await fetchCoachDetail(coachId)
        }
        // 刷新列表
        await fetchCoachList({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
      }
      return response
    } catch (error) {
      console.error('更新教练失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除教练
   */
  const deleteCoach = async (coachId: number) => {
    try {
      loading.value = true
      const response = await coachApi.deleteCoach(coachId)
      if (response.code === 200) {
        // 从本地列表中移除
        coachList.value = coachList.value.filter(item => item.id !== coachId)
        total.value -= 1
        
        // 如果删除的是当前查看的教练，清空当前教练数据
        if (currentCoach.value?.id === coachId) {
          currentCoach.value = null
          coachSchedules.value = []
          coachCourses.value = []
        }
      }
      return response
    } catch (error) {
      console.error('删除教练失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 批量删除教练
   */
  const batchDeleteCoach = async (ids: number[]) => {
    try {
      loading.value = true
      const response = await coachApi.batchDeleteCoach(ids)
      if (response.code === 200) {
        // 从本地列表中移除
        coachList.value = coachList.value.filter(item => !ids.includes(item.id))
        total.value -= ids.length
        
        // 如果删除的包含当前查看的教练，清空当前教练数据
        if (currentCoach.value && ids.includes(currentCoach.value.id)) {
          currentCoach.value = null
          coachSchedules.value = []
          coachCourses.value = []
        }
      }
      return response
    } catch (error) {
      console.error('批量删除教练失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新教练状态
   */
  const updateCoachStatus = async (coachId: number, status: number) => {
    try {
      loading.value = true
      const response = await coachApi.updateCoachStatus(coachId, status)
      if (response.code === 200) {
        // 更新列表中的状态
        const index = coachList.value.findIndex(item => item.id === coachId)
        if (index !== -1) {
          coachList.value[index].status = status
          coachList.value[index].statusDesc = status === 1 ? '在职' : '离职'
        }
        // 更新当前教练的状态
        if (currentCoach.value?.id === coachId) {
          currentCoach.value.status = status
          currentCoach.value.statusDesc = status === 1 ? '在职' : '离职'
        }
      }
      return response
    } catch (error) {
      console.error('更新教练状态失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取教练详情
   */
  const fetchCoachDetail = async (coachId: number) => {
    try {
      loading.value = true
      const response = await coachApi.getCoachDetail(coachId)
      if (response.code === 200) {
        currentCoach.value = response.data
        // 同时加载该教练的排班和课程
        await fetchCoachSchedules(coachId)
        await fetchCoachCourses(coachId)
      }
      return response.data
    } catch (error) {
      console.error('获取教练详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取教练排班列表
   */
  const fetchCoachSchedules = async (coachId: number) => {
    try {
      loading.value = true
      const response = await coachApi.getCoachSchedules(coachId)
      if (response.code === 200) {
        coachSchedules.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取教练排班失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加教练排班
   */
  const addCoachSchedule = async (coachId: number, data: CoachScheduleDTO) => {
    try {
      loading.value = true
      const response = await coachApi.addCoachSchedule(coachId, data)
      if (response.code === 200) {
        // 添加成功后刷新排班列表
        await fetchCoachSchedules(coachId)
      }
      return response
    } catch (error) {
      console.error('添加教练排班失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新教练排班
   */
  const updateCoachSchedule = async (scheduleId: number, data: CoachScheduleDTO) => {
    try {
      loading.value = true
      const response = await coachApi.updateCoachSchedule(scheduleId, data)
      if (response.code === 200) {
        // 更新成功后刷新当前教练的排班列表
        if (currentCoach.value) {
          await fetchCoachSchedules(currentCoach.value.id)
        }
      }
      return response
    } catch (error) {
      console.error('更新教练排班失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除教练排班
   */
  const deleteCoachSchedule = async (scheduleId: number) => {
    try {
      loading.value = true
      const response = await coachApi.deleteCoachSchedule(scheduleId)
      if (response.code === 200) {
        // 从本地排班列表中移除
        coachSchedules.value = coachSchedules.value.filter(item => item.id !== scheduleId)
      }
      return response
    } catch (error) {
      console.error('删除教练排班失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取教练课程列表
   */
  const fetchCoachCourses = async (coachId: number) => {
    try {
      loading.value = true
      const response = await coachApi.getCoachCourses(coachId)
      if (response.code === 200) {
        coachCourses.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取教练课程失败:', error)
      throw error
    } finally {
      loading.value = false
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
   * 清空当前教练数据
   */
  const clearCurrentCoach = () => {
    currentCoach.value = null
    coachSchedules.value = []
    coachCourses.value = []
  }

  /**
   * 重置状态
   */
  const resetState = () => {
    coachList.value = []
    currentCoach.value = null
    coachSchedules.value = []
    coachCourses.value = []
    total.value = 0
    loading.value = false
    pageInfo.value = {
      pageNum: 1,
      pageSize: 10,
      totalPages: 0
    }
  }

  // Getters（函数式定义，和member.ts一致）
  const hasNextPage = () => {
    return pageInfo.value.pageNum < pageInfo.value.totalPages
  }

  const hasPrevPage = () => {
    return pageInfo.value.pageNum > 1
  }

  // 格式化教练列表（替代原有的formattedCoachList getter）
  const formattedCoachList = () => {
    return coachList.value.map(coach => ({
      ...coach,
      hourlyRateFormatted: coach.hourlyRate ? `¥${coach.hourlyRate}/小时` : '-',
      totalIncomeFormatted: coach.totalIncome ? `¥${coach.totalIncome}` : '-',
      ratingFormatted: coach.rating ? `${coach.rating}分` : '-',
      createTimeFormatted: coach.createTime ? new Date(coach.createTime).toLocaleDateString() : '-',
      statusDesc: coach.status === 1 ? '在职' : '离职'
    }))
  }

  return {
    // 状态
    coachList,
    currentCoach,
    coachSchedules,
    coachCourses,
    total,
    loading,
    pageInfo,
    
    // Actions
    fetchCoachList,
    addCoach,
    updateCoach,
    deleteCoach,
    batchDeleteCoach,
    updateCoachStatus,
    fetchCoachDetail,
    fetchCoachSchedules,
    addCoachSchedule,
    updateCoachSchedule,
    deleteCoachSchedule,
    fetchCoachCourses,
    setPageInfo,
    clearCurrentCoach,
    resetState,
    
    // Getters
    hasNextPage,
    hasPrevPage,
    formattedCoachList
  }
})