import { defineStore } from 'pinia'
import { ref } from 'vue'
import { StorageKeys } from '@/utils/constants'

interface Settings {
  theme: 'light' | 'dark'
  language: 'zh-CN' | 'en-US'
  pageSize: number
  notification: boolean
  sound: boolean
}

export const useSettingsStore = defineStore('settings', () => {
  // 默认设置
  const defaultSettings: Settings = {
    theme: 'light',
    language: 'zh-CN',
    pageSize: 10,
    notification: true,
    sound: false
  }

  // 状态
  const settings = ref<Settings>(
    JSON.parse(localStorage.getItem(StorageKeys.SETTINGS) || JSON.stringify(defaultSettings))
  )

  // Actions
  const updateSettings = (newSettings: Partial<Settings>) => {
    settings.value = { ...settings.value, ...newSettings }
    localStorage.setItem(StorageKeys.SETTINGS, JSON.stringify(settings.value))
    
    // 应用主题
    if (newSettings.theme) {
      applyTheme(newSettings.theme)
    }
  }

  const resetSettings = () => {
    settings.value = defaultSettings
    localStorage.setItem(StorageKeys.SETTINGS, JSON.stringify(defaultSettings))
    applyTheme(defaultSettings.theme)
  }

  const applyTheme = (theme: 'light' | 'dark') => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  const toggleTheme = () => {
    const newTheme = settings.value.theme === 'light' ? 'dark' : 'light'
    updateSettings({ theme: newTheme })
  }

  // 初始化时应用主题
  applyTheme(settings.value.theme)

  return {
    // 状态
    settings,
    
    // Actions
    updateSettings,
    resetSettings,
    toggleTheme
  }
})