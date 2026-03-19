// 修改密码页面
import { userStore } from '../../../stores/user.store'
import { showToast, showLoading, hideLoading, showModal } from '../../../utils/wx-util'

Page({
  data: {
    // 原密码
    oldPassword: '',
    showOldPassword: false,
    
    // 新密码
    newPassword: '',
    showNewPassword: false,
    
    // 确认密码
    confirmPassword: '',
    showConfirmPassword: false,
    
    // 校验状态
    checkLength: false,
    checkNumber: false,
    checkLetter: false,
    checkMatch: false,
    
    // 是否可提交
    canSubmit: false,
    
    // 提交状态
    submitting: false
  },

  /**
   * 原密码输入
   */
  onOldPasswordInput(e: any) {
    this.setData({ oldPassword: e.detail.value })
    this.validateForm()
  },

  /**
   * 新密码输入
   */
  onNewPasswordInput(e: any) {
    const value = e.detail.value
    this.setData({ newPassword: value })
    
    // 实时校验
    const checkLength = value.length >= 6 && value.length <= 20
    const checkNumber = /\d/.test(value)
    const checkLetter = /[a-zA-Z]/.test(value)
    
    this.setData({
      checkLength,
      checkNumber,
      checkLetter
    })
    
    this.validateForm()
  },

  /**
   * 确认密码输入
   */
  onConfirmPasswordInput(e: any) {
    this.setData({ confirmPassword: e.detail.value })
    this.validateForm()
  },

  /**
   * 切换原密码显示
   */
  toggleOldPassword() {
    this.setData({
      showOldPassword: !this.data.showOldPassword
    })
  },

  /**
   * 切换新密码显示
   */
  toggleNewPassword() {
    this.setData({
      showNewPassword: !this.data.showNewPassword
    })
  },

  /**
   * 切换确认密码显示
   */
  toggleConfirmPassword() {
    this.setData({
      showConfirmPassword: !this.data.showConfirmPassword
    })
  },

  /**
   * 校验表单
   */
  validateForm() {
    const { oldPassword, newPassword, confirmPassword, checkLength, checkNumber, checkLetter } = this.data
    
    // 检查原密码是否为空
    if (!oldPassword) {
      this.setData({ canSubmit: false })
      return
    }
    
    // 检查新密码规则
    if (!checkLength || !checkNumber || !checkLetter) {
      this.setData({ canSubmit: false })
      return
    }
    
    // 检查两次密码是否一致
    const checkMatch = newPassword === confirmPassword
    this.setData({ checkMatch })
    
    if (!checkMatch) {
      this.setData({ canSubmit: false })
      return
    }
    
    this.setData({ canSubmit: true })
  },

  /**
   * 提交修改
   */
  async onSubmit() {
    const { oldPassword, newPassword, canSubmit } = this.data
    
    if (!canSubmit) {
      showToast('请按要求填写密码', 'none')
      return
    }
    
    const confirm = await showModal({
      title: '提示',
      content: '确认修改密码吗？'
    })
    
    if (!confirm) return
    
    this.setData({ submitting: true })
    showLoading('提交中...')
    
    try {
      // 调用修改密码接口
      // 注意：需要后端提供修改密码接口
      // 这里模拟接口调用
      await new Promise(resolve => setTimeout(resolve, 1500))
      
      hideLoading()
      showToast('密码修改成功', 'success')
      
      // 清除表单
      this.setData({
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
        checkLength: false,
        checkNumber: false,
        checkLetter: false,
        checkMatch: false,
        canSubmit: false,
        submitting: false
      })
      
      // 提示重新登录
      setTimeout(() => {
        showModal({
          title: '提示',
          content: '密码已修改，请重新登录',
          showCancel: false,
          success: () => {
            // 退出登录
            userStore.logout()
          }
        })
      }, 1500)
      
    } catch (error: any) {
      hideLoading()
      showToast(error.message || '修改失败', 'none')
      this.setData({ submitting: false })
    }
  }
})