import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Member, QueryParams, PaginatedResponse } from '@/types'
import { memberApi } from '@/api/member'

export const useMemberStore = defineStore('member', () => {
  // 状态
  const members = ref<Member[]>([])
  const currentMember = ref<Member | null>(null)
  const total = ref(0)
  const loading = ref(false)

  // Actions
  const fetchMembers = async (params: QueryParams = {}) => {
    try {
      loading.value = true
      const response: PaginatedResponse<Member> = await memberApi.getMembers(params)
      members.value = response.items
      total.value = response.total
      return response
    } catch (error) {
      console.error('Fetch members failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchMemberById = async (id: number) => {
    try {
      loading.value = true
      const response = await memberApi.getMemberById(id)
      currentMember.value = response
      return response
    } catch (error) {
      console.error('Fetch member by id failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const createMember = async (memberData: Partial<Member>) => {
    try {
      loading.value = true
      const response = await memberApi.createMember(memberData)
      return response
    } catch (error) {
      console.error('Create member failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const updateMember = async (id: number, memberData: Partial<Member>) => {
    try {
      loading.value = true
      const response = await memberApi.updateMember(id, memberData)
      // 更新本地数据
      const index = members.value.findIndex(member => member.id === id)
      if (index !== -1) {
        members.value[index] = { ...members.value[index], ...response }
      }
      return response
    } catch (error) {
      console.error('Update member failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const deleteMember = async (id: number) => {
    try {
      loading.value = true
      await memberApi.deleteMember(id)
      // 从本地删除
      members.value = members.value.filter(member => member.id !== id)
      total.value -= 1
    } catch (error) {
      console.error('Delete member failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const exportMembers = async (params: QueryParams = {}) => {
    try {
      loading.value = true
      const response = await memberApi.exportMembers(params)
      return response
    } catch (error) {
      console.error('Export members failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const resetMemberState = () => {
    members.value = []
    currentMember.value = null
    total.value = 0
  }

  return {
    // 状态
    members,
    currentMember,
    total,
    loading,
    
    // Actions
    fetchMembers,
    fetchMemberById,
    createMember,
    updateMember,
    deleteMember,
    exportMembers,
    resetMemberState
  }
})