import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLoadingStore = defineStore('loading', () => {
  // 状态
  const isLoading = ref(false)
  const loadingText = ref('加载中...')

  // Actions
  const setLoading = (loading: boolean, text: string = '加载中...') => {
    isLoading.value = loading
    loadingText.value = text
  }

  const showLoading = (text: string = '加载中...') => {
    setLoading(true, text)
  }

  const hideLoading = () => {
    setLoading(false)
  }

  return {
    // 状态
    isLoading,
    loadingText,
    
    // Actions
    setLoading,
    showLoading,
    hideLoading
  }
})