import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Coach, QueryParams, PaginatedResponse } from '@/types'
import { coachApi } from '@/api/coach'

export const useCoachStore = defineStore('coach', () => {
  // 状态
  const coaches = ref<Coach[]>([])
  const currentCoach = ref<Coach | null>(null)
  const total = ref(0)
  const loading = ref(false)

  // Actions
  const fetchCoaches = async (params: QueryParams = {}) => {
    try {
      loading.value = true
      const response: PaginatedResponse<Coach> = await coachApi.getCoaches(params)
      coaches.value = response.items
      total.value = response.total
      return response
    } catch (error) {
      console.error('Fetch coaches failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchCoachById = async (id: number) => {
    try {
      loading.value = true
      const response = await coachApi.getCoachById(id)
      currentCoach.value = response
      return response
    } catch (error) {
      console.error('Fetch coach by id failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const createCoach = async (coachData: Partial<Coach>) => {
    try {
      loading.value = true
      const response = await coachApi.createCoach(coachData)
      return response
    } catch (error) {
      console.error('Create coach failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const updateCoach = async (id: number, coachData: Partial<Coach>) => {
    try {
      loading.value = true
      const response = await coachApi.updateCoach(id, coachData)
      // 更新本地数据
      const index = coaches.value.findIndex(coach => coach.id === id)
      if (index !== -1) {
        coaches.value[index] = { ...coaches.value[index], ...response }
      }
      return response
    } catch (error) {
      console.error('Update coach failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const deleteCoach = async (id: number) => {
    try {
      loading.value = true
      await coachApi.deleteCoach(id)
      // 从本地删除
      coaches.value = coaches.value.filter(coach => coach.id !== id)
      total.value -= 1
    } catch (error) {
      console.error('Delete coach failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const getCoachStatistics = async () => {
    try {
      loading.value = true
      const response = await coachApi.getStatistics()
      return response
    } catch (error) {
      console.error('Get coach statistics failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const resetCoachState = () => {
    coaches.value = []
    currentCoach.value = null
    total.value = 0
  }

  return {
    // 状态
    coaches,
    currentCoach,
    total,
    loading,
    
    // Actions
    fetchCoaches,
    fetchCoachById,
    createCoach,
    updateCoach,
    deleteCoach,
    getCoachStatistics,
    resetCoachState
  }
})