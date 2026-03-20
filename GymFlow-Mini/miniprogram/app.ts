// app.ts
import { userStore } from './stores/user.store'
import { configStore } from './stores/config.store'
import { messageStore } from './stores/message.store'

App<IAppOption>({
  globalData: {
    systemInfo: null,
    userStore: userStore,
    configStore: configStore,
    messageStore: messageStore
  },

  onLaunch() {
    console.log('GymFlow-Mini 启动')
    this.getSystemInfo()
    this.loadSystemConfig()
    this.checkLoginStatus()
  },

  onShow() {
    console.log('小程序显示')
    if (wx.getStorageSync('token')) {
      this.updateUnreadMessageCount()
    }
  },

  onHide() {
    console.log('小程序隐藏')
  },

  onError(error: string) {
    console.error('小程序错误:', error)
  },

  getSystemInfo() {
    try {
      const systemInfo = wx.getSystemInfoSync()
      this.globalData.systemInfo = systemInfo
      console.log('系统信息:', systemInfo)
    } catch (error) {
      console.error('获取系统信息失败:', error)
    }
  },
  
  async loadSystemConfig() {
    try {
      await configStore.loadSystemConfig()
      this.globalData.systemConfigLoaded = true
      console.log('系统配置加载成功')
    } catch (error) {
      console.error('加载系统配置失败:', error)
      this.globalData.systemConfigLoaded = true
    }
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token')
    if (token) {
      console.log('已登录，token存在')
      this.validateToken(token)
    } else {
      console.log('未登录')
    }
  },

  async validateToken(token: string) {
    try {
      console.log('验证token')
    } catch (error) {
      console.error('token验证失败', error)
      wx.removeStorageSync('token')
      wx.removeStorageSync('userInfo')
    }
  },

  async updateUnreadMessageCount() {
    if (!wx.getStorageSync('token')) return
    try {
      await messageStore.refreshUnreadCount()
    } catch (error) {
      console.error('获取未读消息数失败', error)
    }
  }
})

interface IAppOption {
  globalData: {
    systemInfo: wx.SystemInfo | null
    userStore: any
    configStore: any
    messageStore: any
    systemConfigLoaded?: boolean
  }
}