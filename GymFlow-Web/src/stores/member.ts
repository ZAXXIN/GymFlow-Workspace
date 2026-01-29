import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { 
  MemberQueryDTO,
  MemberListVO,
  PageResult,
  MemberFullDTO,
  MemberBasicDTO,
  HealthRecordDTO,
  MemberCardDTO,
  MemberAddRequest,
  MemberUpdateRequest
} from '@/types/member'
import { memberApi } from '@/api/member'

export const useMemberStore = defineStore('member', () => {
  // 状态
  const members = ref<MemberListVO[]>([])
  const currentMember = ref<MemberFullDTO | null>(null)
  const healthRecords = ref<HealthRecordDTO[]>([])
  const memberCards = ref<MemberCardDTO[]>([])
  const total = ref(0)
  const loading = ref(false)
  const pageInfo = ref({
    pageNum: 1,
    pageSize: 10,
    totalPages: 0
  })

  // Actions
  
  /**
   * 分页查询会员列表
   */
  const fetchMembers = async (params: MemberQueryDTO = {}) => {
    try {
      loading.value = true
      const queryParams = {
        pageNum: params.pageNum || 1,
        pageSize: params.pageSize || 10,
        ...params
      }
      
      const response = await memberApi.getMemberList(queryParams)
      if (response.code === 200) {
        members.value = response.data.list
        total.value = response.data.total
        pageInfo.value = {
          pageNum: response.data.pageNum,
          pageSize: response.data.pageSize,
          totalPages: response.data.pages
        }
      }
      return response.data
    } catch (error) {
      console.error('获取会员列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取会员详情
   */
  const fetchMemberDetail = async (memberId: number) => {
    try {
      loading.value = true
      const response = await memberApi.getMemberDetail(memberId)
      if (response.code === 200) {
        currentMember.value = response.data
        
        // 同时存储相关的健康档案、会员卡等信息
        healthRecords.value = response.data.healthRecords || []
        memberCards.value = response.data.memberCards || []
      }
      return response.data
    } catch (error) {
      console.error('获取会员详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加会员
   */
  const addMember = async (memberData: MemberAddRequest) => {
    try {
      loading.value = true
      const response = await memberApi.addMember(memberData)
      if (response.code === 200) {
        // 添加成功后重新加载列表
        await fetchMembers({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
      }
      return response
    } catch (error) {
      console.error('添加会员失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新会员信息
   */
  const updateMember = async (memberId: number, memberData: MemberUpdateRequest) => {
    try {
      loading.value = true
      const response = await memberApi.updateMember(memberId, memberData)
      if (response.code === 200) {
        // 更新成功后刷新当前会员详情
        if (currentMember.value?.id === memberId) {
          await fetchMemberDetail(memberId)
        }
        // 刷新列表
        await fetchMembers({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
      }
      return response
    } catch (error) {
      console.error('更新会员信息失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除会员（软删除）
   */
  const deleteMember = async (memberId: number) => {
    try {
      loading.value = true
      const response = await memberApi.deleteMember(memberId)
      if (response.code === 200) {
        // 从本地列表中移除
        members.value = members.value.filter(member => member.id !== memberId)
        total.value -= 1
        
        // 如果删除的是当前查看的会员，清空当前会员数据
        if (currentMember.value?.id === memberId) {
          currentMember.value = null
          healthRecords.value = []
          memberCards.value = []
        }
      }
      return response
    } catch (error) {
      console.error('删除会员失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 批量删除会员
   */
  const batchDeleteMembers = async (memberIds: number[]) => {
    try {
      loading.value = true
      const response = await memberApi.batchDeleteMember(memberIds)
      if (response.code === 200) {
        // 从本地列表中移除
        members.value = members.value.filter(member => !memberIds.includes(member.id))
        total.value -= memberIds.length
        
        // 如果删除的包含当前查看的会员，清空当前会员数据
        if (currentMember.value && memberIds.includes(currentMember.value.id)) {
          currentMember.value = null
          healthRecords.value = []
          memberCards.value = []
        }
      }
      return response
    } catch (error) {
      console.error('批量删除会员失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 续费会员卡
   */
  const renewMemberCard = async (memberId: number, cardData: MemberCardDTO) => {
    try {
      loading.value = true
      const response = await memberApi.renewMemberCard(memberId, cardData)
      if (response.code === 200) {
        // 续费成功后刷新会员详情
        await fetchMemberDetail(memberId)
      }
      return response
    } catch (error) {
      console.error('续费会员卡失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取健康档案列表
   */
  const fetchHealthRecords = async (memberId: number) => {
    try {
      loading.value = true
      const response = await memberApi.getHealthRecords(memberId)
      if (response.code === 200) {
        healthRecords.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取健康档案失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加健康档案
   */
  const addHealthRecord = async (memberId: number, healthData: HealthRecordDTO) => {
    try {
      loading.value = true
      const response = await memberApi.addHealthRecord(memberId, healthData)
      if (response.code === 200) {
        // 添加成功后刷新健康档案列表
        await fetchHealthRecords(memberId)
      }
      return response
    } catch (error) {
      console.error('添加健康档案失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 检查用户名是否存在
   */
  const checkUsernameExists = async (username: string): Promise<boolean> => {
    try {
      const response = await memberApi.checkUsernameExists(username)
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
   * 清空当前会员数据
   */
  const clearCurrentMember = () => {
    currentMember.value = null
    healthRecords.value = []
    memberCards.value = []
  }

  /**
   * 重置状态
   */
  const resetState = () => {
    members.value = []
    currentMember.value = null
    healthRecords.value = []
    memberCards.value = []
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

  return {
    // 状态
    members,
    currentMember,
    healthRecords,
    memberCards,
    total,
    loading,
    pageInfo,
    
    // Actions
    fetchMembers,
    fetchMemberDetail,
    addMember,
    updateMember,
    deleteMember,
    batchDeleteMembers,
    renewMemberCard,
    fetchHealthRecords,
    addHealthRecord,
    checkUsernameExists,
    setPageInfo,
    clearCurrentMember,
    resetState,
    
    // Getters
    hasNextPage,
    hasPrevPage
  }
})