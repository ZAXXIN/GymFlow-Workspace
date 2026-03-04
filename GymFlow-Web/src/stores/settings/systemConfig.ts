import { defineStore } from 'pinia'
import { ref } from 'vue'
import { systemConfigApi } from '@/api/settings/systemConfig'
import type { 
  BasicConfigDTO, 
  BusinessConfigDTO, 
  SystemConfigResponseDTO 
} from '@/api/settings/systemConfig'
import { ElMessage } from 'element-plus'

export const useSystemConfigStore = defineStore('systemConfig', () => {
  const config = ref<SystemConfigResponseDTO | null>(null)
  const loading = ref(false)

  /**
   * 获取系统配置
   */
  const fetchConfig = async () => {
    try {
      loading.value = true
      const response = await systemConfigApi.getConfig()
      if (response.code === 200) {
        config.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取系统配置失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新系统配置
   */
  const updateConfig = async (basic: BasicConfigDTO, business: BusinessConfigDTO) => {
    try {
      loading.value = true
      const response = await systemConfigApi.updateConfig({ basic, business })
      if (response.code === 200) {
        // 更新成功后重新获取配置
        await fetchConfig()
        ElMessage.success('系统配置更新成功')
      }
      return response
    } catch (error) {
      console.error('更新系统配置失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置系统配置
   */
  const resetConfig = async () => {
    try {
      loading.value = true
      const response = await systemConfigApi.resetConfig()
      if (response.code === 200) {
        await fetchConfig()
        ElMessage.success('系统配置重置成功')
      }
      return response
    } catch (error) {
      console.error('重置系统配置失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 上传Logo
   */
  const uploadLogo = async (file: File): Promise<string> => {
    try {
      loading.value = true
      const response = await systemConfigApi.uploadLogo(file)
      if (response.code === 200 && response.data?.url) {
        return response.data.url
      }
      throw new Error('上传失败')
    } catch (error) {
      console.error('上传Logo失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // Getters
  const getBasicConfig = (): BasicConfigDTO | null => {
    return config.value?.basic || null
  }

  const getBusinessConfig = (): BusinessConfigDTO | null => {
    return config.value?.business || null
  }

  const getSystemName = (): string => {
    return config.value?.basic?.systemName || 'GymFlow健身管理系统'
  }

  const getSystemLogo = (): string => {
    return config.value?.basic?.systemLogo || ''
  }

  const getBusinessHours = (): string => {
    const business = config.value?.business
    if (!business) return '08:00 - 22:00'
    const start = business.businessStartTime.substring(0, 5)
    const end = business.businessEndTime.substring(0, 5)
    return `${start} - ${end}`
  }

  return {
    // 状态
    config,
    loading,
    
    // Actions
    fetchConfig,
    updateConfig,
    resetConfig,
    uploadLogo,
    
    // Getters
    getBasicConfig,
    getBusinessConfig,
    getSystemName,
    getSystemLogo,
    getBusinessHours
  }
})