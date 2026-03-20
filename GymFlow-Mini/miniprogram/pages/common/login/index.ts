// pages/common/login/index.ts
import { miniLogin } from '../../../services/api/auth.api'
import { userStore } from '../../../stores/user.store'
import { messageStore } from '../../../stores/message.store'
import { configStore } from '../../../stores/config.store'
import { showToast, showLoading, hideLoading } from '../../../utils/wx-util'
import { isValidPhone } from '../../../utils/validator'

Page({
  data: {
    phone: '17700000001',
    password: '123456',
    loading: false,
    showPassword: false,
    agreement: true,
    systemName: 'GymFlow健身',
    systemLogo: '/assets/icons/logo.jpg'
  },

  onLoad: function() {
    this.checkLoginStatus()
    
    const app = getApp()
    if (app.globalData && app.globalData.configStore) {
      this.setData({
        systemName: app.globalData.configStore.systemName,
        systemLogo: app.globalData.configStore.systemLogo
      })
    }
  },

  checkLoginStatus: function() {
    if (userStore.isLogin) {
      this.redirectToHome()
    }
  },

  redirectToHome: function() {
    const role = userStore.role
    
    if (role === 'MEMBER') {
      wx.reLaunch({
        url: '/pages/member/home/index'
      })
    } else if (role === 'COACH') {
      wx.reLaunch({
        url: '/pages/coach/home/index'
      })
    }
  },

  onPhoneInput: function(e: any) {
    this.setData({ phone: e.detail.value })
  },

  onPasswordInput: function(e: any) {
    this.setData({ password: e.detail.value })
  },

  togglePassword: function() {
    this.setData({ showPassword: !this.data.showPassword })
  },

  toggleAgreement: function() {
    this.setData({ agreement: !this.data.agreement })
  },

  viewAgreement: function() {
    wx.navigateTo({ url: '/pages/common/webview/index?url=agreement' })
  },

  viewPrivacy: function() {
    wx.navigateTo({ url: '/pages/common/webview/index?url=privacy' })
  },

  onLogin: function() {
    var that = this
    var phone = this.data.phone
    var password = this.data.password
    var agreement = this.data.agreement
    
    if (!agreement) {
      showToast('请先同意用户协议和隐私政策', 'none')
      return
    }
    
    if (!phone) {
      showToast('请输入手机号', 'none')
      return
    }
    
    if (!isValidPhone(phone)) {
      showToast('请输入正确的手机号', 'none')
      return
    }
    
    if (!password) {
      showToast('请输入密码', 'none')
      return
    }
    
    if (password.length < 6) {
      showToast('密码不能少于6位', 'none')
      return
    }
    
    this.setData({ loading: true })
    showLoading('登录中...')
    
    miniLogin({ phone, password })
      .then(function(result) {
        userStore.setUserInfo(result)
        
        return Promise.all([
          configStore.loadConfig(true),
          messageStore.init()
        ]).then(function() {
          return messageStore.refreshUnreadCount()
        }).then(function() {
          hideLoading()
          showToast('登录成功', 'success')
          
          setTimeout(function() {
            that.redirectToHome()
          }, 1500)
        })
      })
      .catch(function(error) {
        hideLoading()
        showToast(error.message || '登录失败', 'none')
        that.setData({ loading: false })
      })
  },

  onFormSubmit: function(e: any) {
    return false
  }
})