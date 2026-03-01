// src/store/modules/checkIn.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { checkInApi } from '@/api/checkIn'
import type {
  CheckInQueryParams,
  CheckInListVO,
  CheckInDetailVO,
  CheckInStatsDTO,
  CheckInReportDTO,
  PageResultVO
} from '@/types/checkIn'

export const useCheckInStore = defineStore('checkIn', () => {
  // 状态
  const checkInList = ref<CheckInListVO[]>([])
  const currentCheckIn = ref<CheckInDetailVO | null>(null)
  const checkInStats = ref<CheckInStatsDTO | null>(null)
  const checkInReport = ref<CheckInReportDTO | null>(null)
  const total = ref(0)
  const loading = ref(false)
  const pageInfo = ref({
    pageNum: 1,
    pageSize: 10,
    totalPages: 0
  })
  
  /**
   * 分页查询签到列表
   */
  const fetchCheckInList = async (params: CheckInQueryParams = {}) => {
    try {
      loading.value = true
      const queryParams = {
        pageNum: params.pageNum || pageInfo.value.pageNum,
        pageSize: params.pageSize || pageInfo.value.pageSize,
        ...params
      }
      
      const response = await checkInApi.getCheckInList(queryParams)
      if (response.code === 200) {
        checkInList.value = response.data.list
        total.value = response.data.total
        pageInfo.value = {
          pageNum: response.data.pageNum,
          pageSize: response.data.pageSize,
          totalPages: response.data.pages || Math.ceil(response.data.total / response.data.pageSize)
        }
      }
      return response.data
    } catch (error) {
      console.error('获取签到列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取签到详情
   */
  const fetchCheckInDetail = async (checkInId: number) => {
    try {
      loading.value = true
      const response = await checkInApi.getCheckInDetail(checkInId)
      if (response.code === 200) {
        currentCheckIn.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取签到详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 会员签到（自由训练）
   */
  const memberCheckIn = async (memberId: number, checkinMethod: number, notes?: string) => {
    try {
      loading.value = true
      const response = await checkInApi.memberCheckIn(memberId, checkinMethod, notes)
      if (response.code === 200) {
        // 签到成功后重新加载列表
        await fetchCheckInList({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
      }
      return response
    } catch (error) {
      console.error('会员签到失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 课程签到
   */
  const courseCheckIn = async (bookingId: number, checkinMethod: number, notes?: string) => {
    try {
      loading.value = true
      const response = await checkInApi.courseCheckIn(bookingId, checkinMethod, notes)
      if (response.code === 200) {
        // 签到成功后重新加载列表
        await fetchCheckInList({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
      }
      return response
    } catch (error) {
      console.error('课程签到失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新签到信息
   */
  const updateCheckIn = async (checkInId: number, notes: string) => {
    try {
      loading.value = true
      const response = await checkInApi.updateCheckIn(checkInId, notes)
      if (response.code === 200) {
        // 更新成功后刷新当前签到详情
        if (currentCheckIn.value?.id === checkInId) {
          await fetchCheckInDetail(checkInId)
        }
        // 刷新列表
        await fetchCheckInList({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
      }
      return response
    } catch (error) {
      console.error('更新签到信息失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除签到记录
   */
  const deleteCheckIn = async (checkInId: number) => {
    try {
      loading.value = true
      const response = await checkInApi.deleteCheckIn(checkInId)
      if (response.code === 200) {
        // 从本地列表中移除
        checkInList.value = checkInList.value.filter(item => item.id !== checkInId)
        total.value -= 1
        
        // 如果删除的是当前查看的签到，清空当前签到数据
        if (currentCheckIn.value?.id === checkInId) {
          currentCheckIn.value = null
        }
      }
      return response
    } catch (error) {
      console.error('删除签到记录失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 批量删除签到记录
   */
  const batchDeleteCheckIns = async (checkInIds: number[]) => {
    try {
      loading.value = true
      const response = await checkInApi.batchDeleteCheckIns(checkInIds)
      if (response.code === 200) {
        // 从本地列表中移除
        checkInList.value = checkInList.value.filter(item => !checkInIds.includes(item.id))
        total.value -= checkInIds.length
        
        // 如果删除的包含当前查看的签到，清空当前签到数据
        if (currentCheckIn.value && checkInIds.includes(currentCheckIn.value.id)) {
          currentCheckIn.value = null
        }
      }
      return response
    } catch (error) {
      console.error('批量删除签到记录失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取会员签到记录
   */
  const fetchMemberCheckIns = async (memberId: number, params: CheckInQueryParams = {}) => {
    try {
      loading.value = true
      const queryParams = {
        pageNum: params.pageNum || pageInfo.value.pageNum,
        pageSize: params.pageSize || pageInfo.value.pageSize,
        ...params
      }
      
      const response = await checkInApi.getMemberCheckIns(memberId, queryParams)
      if (response.code === 200) {
        checkInList.value = response.data.list
        total.value = response.data.total
        pageInfo.value = {
          pageNum: response.data.pageNum,
          pageSize: response.data.pageSize,
          totalPages: response.data.pages || Math.ceil(response.data.total / response.data.pageSize)
        }
      }
      return response.data
    } catch (error) {
      console.error('获取会员签到记录失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取今日签到统计
   */
  const fetchTodayCheckInStats = async () => {
    try {
      loading.value = true
      const response = await checkInApi.getTodayCheckInStats()
      if (response.code === 200) {
        checkInStats.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取今日签到统计失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取签到统计报表
   */
  const fetchCheckInReport = async (params: CheckInQueryParams = {}) => {
    try {
      loading.value = true
      const response = await checkInApi.getCheckInReport(params)
      if (response.code === 200) {
        checkInReport.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取签到统计报表失败:', error)
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
   * 清空当前签到数据
   */
  const clearCurrentCheckIn = () => {
    currentCheckIn.value = null
  }

  /**
   * 清空报表数据
   */
  const clearReportData = () => {
    checkInReport.value = null
  }

  /**
   * 重置状态
   */
  const resetState = () => {
    checkInList.value = []
    currentCheckIn.value = null
    checkInStats.value = null
    checkInReport.value = null
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

  // 格式化签到列表
  const formattedCheckInList = () => {
    return checkInList.value.map(item => ({
      ...item,
      checkinTimeFormatted: item.checkinTime ? formatDateTime(item.checkinTime) : '-',
      createTimeFormatted: item.createTime ? formatDateTime(item.createTime) : '-'
    }))
  }

  // 格式化签到详情
  const formattedCurrentCheckIn = () => {
    if (!currentCheckIn.value) return null
    
    return {
      ...currentCheckIn.value,
      checkinTimeFormatted: currentCheckIn.value.checkinTime ? formatDateTime(currentCheckIn.value.checkinTime) : '-',
      bookingTimeFormatted: currentCheckIn.value.bookingTime ? formatDateTime(currentCheckIn.value.bookingTime) : '-',
      createTimeFormatted: currentCheckIn.value.createTime ? formatDateTime(currentCheckIn.value.createTime) : '-'
    }
  }

  // 辅助函数：格式化日期时间
  const formatDateTime = (dateTimeStr: string) => {
    try {
      const date = new Date(dateTimeStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    } catch (error) {
      console.error('格式化日期时间失败:', error)
      return dateTimeStr
    }
  }

  // 辅助函数：格式化日期
  const formatDate = (dateStr: string) => {
    try {
      const date = new Date(dateStr)
      return date.toLocaleDateString('zh-CN')
    } catch (error) {
      console.error('格式化日期失败:', error)
      return dateStr
    }
  }

  return {
    // 状态
    checkInList,
    currentCheckIn,
    checkInStats,
    checkInReport,
    total,
    loading,
    pageInfo,
    
    // Actions
    fetchCheckInList,
    fetchCheckInDetail,
    memberCheckIn,
    courseCheckIn,
    updateCheckIn,
    deleteCheckIn,
    batchDeleteCheckIns,
    fetchMemberCheckIns,
    fetchTodayCheckInStats,
    fetchCheckInReport,
    setPageInfo,
    clearCurrentCheckIn,
    clearReportData,
    resetState,
    
    // Getters
    hasNextPage,
    hasPrevPage,
    formattedCheckInList,
    formattedCurrentCheckIn,
    
    // 工具函数
    formatDateTime,
    formatDate
  }
})