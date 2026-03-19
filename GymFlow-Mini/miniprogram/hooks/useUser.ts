// 用户相关自定义Hook

import { userStore } from '../stores/user.store'
import { messageStore } from '../stores/message.store'
import { bookingStore } from '../stores/booking.store'
import { orderStore } from '../stores/order.store'
import { logout as logoutApi } from '../services/api/auth.api'
import { showModal, showSuccess, showError } from '../utils/wx-util'

export const useUser = () => {
  /**
   * 退出登录
   */
  const logout = async (showConfirm: boolean = true) => {
    try {
      if (showConfirm) {
        const confirm = await showModal({
          title: '提示',
          content: '确定要退出登录吗？'
        })
        if (!confirm) return
      }

      // 调用登出
      await logoutApi()
      
      // 清除所有store
      userStore.logout()
      messageStore.reset()
      bookingStore.reset()
      orderStore.reset()
      
      showSuccess('已退出登录')
      
      // 跳转到登录页
      setTimeout(() => {
        wx.reLaunch({
          url: '/pages/common/login/index'
        })
      }, 1500)
    } catch (error: any) {
      showError(error.message || '退出登录失败')
    }
  }

  /**
   * 检查登录状态
   */
  const checkLogin = (): boolean => {
    if (!userStore.isLogin) {
      wx.navigateTo({
        url: '/pages/common/login/index'
      })
      return false
    }
    return true
  }

  /**
   * 获取当前用户角色
   */
  const getUserRole = () => {
    return userStore.role
  }

  /**
   * 判断是否为会员
   */
  const isMember = (): boolean => {
    return userStore.role === 'MEMBER'
  }

  /**
   * 判断是否为教练
   */
  const isCoach = (): boolean => {
    return userStore.role === 'COACH'
  }

  /**
   * 获取会员ID
   */
  const getMemberId = (): number | undefined => {
    return userStore.memberId
  }

  /**
   * 获取教练ID
   */
  const getCoachId = (): number | undefined => {
    return userStore.coachId
  }

  /**
   * 刷新用户信息
   */
  const refreshUserInfo = async () => {
    try {
      if (isMember()) {
        // 重新获取会员信息
        const { getMyMemberInfo } = await import('../services/api/member.api')
        const memberInfo = await getMyMemberInfo()
        userStore.setMemberInfo(memberInfo)
      } else if (isCoach()) {
        // 重新获取教练信息
        const { getMyCoachInfo } = await import('../services/api/coach.api')
        const coachInfo = await getMyCoachInfo()
        userStore.setCoachInfo(coachInfo)
      }
    } catch (error: any) {
      console.error('刷新用户信息失败:', error)
    }
  }

  return {
    userInfo: userStore.userInfo,
    memberInfo: userStore.memberInfo,
    coachInfo: userStore.coachInfo,
    isLogin: userStore.isLogin,
    role: userStore.role,
    memberId: userStore.memberId,
    coachId: userStore.coachId,
    phone: userStore.phone,
    realName: userStore.realName,
    logout,
    checkLogin,
    getUserRole,
    isMember,
    isCoach,
    getMemberId,
    getCoachId,
    refreshUserInfo
  }
}