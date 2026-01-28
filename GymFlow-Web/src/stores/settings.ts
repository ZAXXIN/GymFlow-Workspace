import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { StorageKeys } from '@/utils/constants'

interface Settings {
  theme: 'light' | 'dark'
  language: 'zh-CN' | 'en-US'
  pageSize: number
  notification: boolean
  sound: boolean
  sidebarCollapsed: boolean
  showSearchBar: boolean
  layoutMode: 'default' | 'compact'
  primaryColor: string
}

export const useSettingsStore = defineStore('settings', () => {
  // 默认设置
  const defaultSettings: Settings = {
    theme: 'light',
    language: 'zh-CN',
    pageSize: 10,
    notification: true,
    sound: false,
    sidebarCollapsed: false,
    showSearchBar: true,
    layoutMode: 'default',
    primaryColor: '#667eea'
  }

  // 状态
  const settings = ref<Settings>(
    JSON.parse(localStorage.getItem(StorageKeys.SETTINGS) || JSON.stringify(defaultSettings))
  )

  // 计算属性
  const sidebarCollapsed = computed(() => settings.value.sidebarCollapsed)
  const showSearchBar = computed(() => settings.value.showSearchBar)
  const showNotifications = computed(() => settings.value.notification)
  const currentTheme = computed(() => settings.value.theme)

  // Actions
  const updateSettings = (newSettings: Partial<Settings>) => {
    const oldSettings = { ...settings.value }
    settings.value = { ...settings.value, ...newSettings }
    
    // 保存到localStorage
    localStorage.setItem(StorageKeys.SETTINGS, JSON.stringify(settings.value))
    
    // 应用主题变化
    if (newSettings.theme && newSettings.theme !== oldSettings.theme) {
      applyTheme(newSettings.theme)
    }
    
    // 应用主要颜色变化
    if (newSettings.primaryColor && newSettings.primaryColor !== oldSettings.primaryColor) {
      applyPrimaryColor(newSettings.primaryColor)
    }
  }

  const resetSettings = () => {
    settings.value = defaultSettings
    localStorage.setItem(StorageKeys.SETTINGS, JSON.stringify(defaultSettings))
    applyTheme(defaultSettings.theme)
    applyPrimaryColor(defaultSettings.primaryColor)
  }

  const applyTheme = (theme: 'light' | 'dark') => {
    if (theme === 'dark') {
      document.documentElement.setAttribute('data-theme', 'dark')
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.removeAttribute('data-theme')
      document.documentElement.classList.remove('dark')
    }
  }

  const applyPrimaryColor = (color: string) => {
    document.documentElement.style.setProperty('--gymflow-primary', color)
    // 可以根据color生成渐变色
    const gradient = `linear-gradient(135deg, ${color} 0%, ${adjustColor(color, -20)} 100%)`
    document.documentElement.style.setProperty('--gymflow-gradient', gradient)
  }

  const toggleTheme = () => {
    const newTheme = settings.value.theme === 'light' ? 'dark' : 'light'
    updateSettings({ theme: newTheme })
  }

  // ✅ 新增：切换侧边栏折叠状态
  const toggleSidebar = () => {
    const newCollapsedState = !settings.value.sidebarCollapsed
    updateSettings({ sidebarCollapsed: newCollapsedState })
  }

  // ✅ 新增：设置侧边栏状态（可选）
  const setSidebarCollapsed = (collapsed: boolean) => {
    updateSettings({ sidebarCollapsed: collapsed })
  }

  const toggleSearchBar = () => {
    updateSettings({ showSearchBar: !settings.value.showSearchBar })
  }

  const toggleNotifications = () => {
    updateSettings({ notification: !settings.value.notification })
  }

  // 工具函数
  const adjustColor = (color: string, amount: number): string => {
    // 简单的颜色调整函数
    // 实际项目中可以引入第三方颜色库如 chroma-js
    return color // 这里返回原色，实际需要实现颜色调整逻辑
  }

  // 初始化时应用设置
  const initializeSettings = () => {
    applyTheme(settings.value.theme)
    applyPrimaryColor(settings.value.primaryColor)
  }

  // 执行初始化
  initializeSettings()

  return {
    // 状态
    settings,
    
    // 计算属性
    sidebarCollapsed,
    showSearchBar,
    showNotifications,
    currentTheme,
    
    // Actions
    updateSettings,
    resetSettings,
    toggleTheme,
    toggleSidebar,
    setSidebarCollapsed,
    toggleSearchBar,
    toggleNotifications
  }
})