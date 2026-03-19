// app.ts
import { userStore } from './stores/user.store'
import { configStore } from './stores/config.store'
import { messageStore } from './stores/message.store'

App<IAppOption>({
  globalData: {
    systemInfo: null,
    userStore: userStore,  // 直接使用导入的实例
    configStore: configStore,
    messageStore: messageStore
  },

  onLaunch() {
    console.log('GymFlow-Mini 启动')
    // 获取系统信息
    this.getSystemInfo()
    // 加载系统配置（无论是否登录都需要）
    this.loadSystemConfig()
    // 检查登录状态
    this.checkLoginStatus()
  },

  onShow() {
    console.log('小程序显示')
    // 只有在登录状态下才更新未读消息数
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

  /**
   * 获取系统信息
   */
  getSystemInfo() {
    try {
      const systemInfo = wx.getSystemInfoSync()
      this.globalData.systemInfo = systemInfo
      console.log('系统信息:', systemInfo)
    } catch (error) {
      console.error('获取系统信息失败:', error)
    }
  },
  
  /**
   * 加载系统配置
   */
  async loadSystemConfig() {
    try {
      // 加载系统配置（包括签到规则等）
      await configStore.loadSystemConfig()
      this.globalData.systemConfigLoaded = true
      console.log('系统配置加载成功')
    } catch (error) {
      console.error('加载系统配置失败:', error)
      // 使用默认配置
      this.globalData.systemConfigLoaded = true
    }
  },

  /**
   * 检查登录状态
   */
  checkLoginStatus() {
    const token = wx.getStorageSync('token')
    if (token) {
      console.log('已登录，token存在')
      // 验证token有效性（可选）
      this.validateToken(token)
    } else {
      console.log('未登录，跳转登录页')
      // 不自动跳转，由页面自行处理
    }
  },

  /**
   * 验证token有效性
   */
  async validateToken(token: string) {
    try {
      // 这里可以调用接口验证token
      console.log('验证token')
    } catch (error) {
      console.error('token验证失败', error)
      wx.removeStorageSync('token')
      wx.removeStorageSync('userInfo')
    }
  },

  /**
   * 更新未读消息数
   */
  async updateUnreadMessageCount() {
    if (!wx.getStorageSync('token')) return
    
    try {
      // 获取未读消息数
      // const { data } = await wxRequest.get('/mini/message/unread-count');
      // this.globalData.messageStore?.setUnreadCount(data);
    } catch (error) {
      console.error('获取未读消息数失败', error)
    }
  }
})

// IAppOption 类型定义
interface IAppOption {
  globalData: {
    systemInfo: wx.SystemInfo | null
    userStore: any  // 或者导入对应的类型
    configStore: any
    messageStore: any
  }
}