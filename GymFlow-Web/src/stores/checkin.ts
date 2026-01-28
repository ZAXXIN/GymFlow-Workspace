import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CheckIn, QueryParams, PaginatedResponse } from '@/types'
import { checkInApi } from '@/api/checkIn'

export const useCheckInStore = defineStore('checkin', () => {
  // 状态
  const checkIns = ref<CheckIn[]>([])
  const total = ref(0)
  const loading = ref(false)

  // Actions
  const fetchCheckIns = async (params: QueryParams = {}) => {
    try {
      loading.value = true
      const response: PaginatedResponse<CheckIn> = await checkInApi.getCheckIns(params)
      checkIns.value = response.items
      total.value = response.total
      return response
    } catch (error) {
      console.error('Fetch check-ins failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const createCheckIn = async (checkInData: Partial<CheckIn>) => {
    try {
      loading.value = true
      const response = await checkInApi.createCheckIn(checkInData)
      return response
    } catch (error) {
      console.error('Create check-in failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const updateCheckIn = async (id: number, checkInData: Partial<CheckIn>) => {
    try {
      loading.value = true
      const response = await checkInApi.updateCheckIn(id, checkInData)
      // 更新本地数据
      const index = checkIns.value.findIndex(checkIn => checkIn.id === id)
      if (index !== -1) {
        checkIns.value[index] = { ...checkIns.value[index], ...response }
      }
      return response
    } catch (error) {
      console.error('Update check-in failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const deleteCheckIn = async (id: number) => {
    try {
      loading.value = true
      await checkInApi.deleteCheckIn(id)
      // 从本地删除
      checkIns.value = checkIns.value.filter(checkIn => checkIn.id !== id)
      total.value -= 1
    } catch (error) {
      console.error('Delete check-in failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const getCheckInStatistics = async (params: QueryParams = {}) => {
    try {
      loading.value = true
      const response = await checkInApi.getStatistics(params)
      return response
    } catch (error) {
      console.error('Get check-in statistics failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const exportCheckIns = async (params: QueryParams = {}) => {
    try {
      loading.value = true
      const response = await checkInApi.exportCheckIns(params)
      return response
    } catch (error) {
      console.error('Export check-ins failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const resetCheckInState = () => {
    checkIns.value = []
    total.value = 0
  }

  return {
    // 状态
    checkIns,
    total,
    loading,
    
    // Actions
    fetchCheckIns,
    createCheckIn,
    updateCheckIn,
    deleteCheckIn,
    getCheckInStatistics,
    exportCheckIns,
    resetCheckInState
  }
})