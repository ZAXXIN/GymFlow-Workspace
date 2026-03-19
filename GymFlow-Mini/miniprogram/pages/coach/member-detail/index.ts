// 教练端会员详情页面
import { getCoachMemberDetail } from '../../../services/api/coach.api'
import { formatTime, formatDateTime } from '../../../utils/date'
import { showToast } from '../../../utils/wx-util'

Page({
  data: {
    // 会员ID
    memberId: 0,
    
    // 会员信息
    memberInfo: {
      memberName: '',
      memberPhone: '',
      memberNo: '',
      membershipStartDate: '',
      memberCard: null,
      healthRecords: [] as any[],
      courseRecords: [] as any[]
    },
    
    // 健康档案
    healthRecords: [] as any[],
    
    // 课程记录
    courseRecords: [] as any[],
    
    // 加载状态
    loading: true
  },

  onLoad(options: any) {
    const { memberId } = options
    
    if (!memberId) {
      showToast('参数错误', 'none')
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
      return
    }
    
    this.setData({ memberId: parseInt(memberId) })
    this.loadMemberDetail()
  },

  /**
   * 加载会员详情
   */
  async loadMemberDetail() {
    this.setData({ loading: true })
    
    try {
      const { memberId } = this.data
      const detail = await getCoachMemberDetail(memberId)
      
      this.setData({
        memberInfo: {
          memberName: detail.memberName,
          memberPhone: detail.memberPhone,
          memberNo: detail.memberNo,
          membershipStartDate: detail.membershipStartDate,
          memberCard: detail.memberCard
        },
        healthRecords: detail.healthRecords || [],
        courseRecords: detail.courseRecords || [],
        loading: false
      })
      
    } catch (error: any) {
      console.error('加载会员详情失败:', error)
      this.setData({ loading: false })
      showToast(error.message || '加载失败', 'none')
    }
  },

  /**
   * 获取显示的课程记录（最多3条）
   */
  get displayCourses(): any[] {
    const { courseRecords } = this.data
    if (courseRecords.length > 3) {
      const result: any[] = []
      for (let i = 0; i < 3; i++) {
        result.push(courseRecords[i])
      }
      return result
    }
    return courseRecords
  },

  /**
   * 查看全部课程
   */
  onViewAllCourses() {
    const { memberId } = this.data
    
    wx.navigateTo({
      url: `/pages/coach/member-courses/index?memberId=${memberId}`
    })
  },

  /**
   * 添加健康档案
   */
  onAddHealthRecord() {
    const { memberId } = this.data
    
    wx.navigateTo({
      url: `/pages/coach/add-health-record/index?memberId=${memberId}`
    })
  },

  /**
   * 获取卡片状态样式类
   */
  getCardStatusClass(status: string): string {
    const map: Record<string, string> = {
      'ACTIVE': 'coach-member-card-status-active',
      'EXPIRED': 'coach-member-card-status-expired',
      'USED_UP': 'coach-member-card-status-used-up',
      'UNPAID': 'coach-member-card-status-expired'
    }
    return map[status] || 'coach-member-card-status-expired'
  },

  /**
   * 获取卡片状态文本
   */
  getCardStatusText(status: string): string {
    const map: Record<string, string> = {
      'ACTIVE': '有效',
      'EXPIRED': '已过期',
      'USED_UP': '已用完',
      'UNPAID': '未支付'
    }
    return map[status] || '未知'
  },

  /**
   * 获取预约状态样式类
   */
  getBookingStatusClass(status: number): string {
    const map: Record<number, string> = {
      0: 'coach-member-course-status-pending',
      1: 'coach-member-course-status-checked',
      2: 'coach-member-course-status-completed',
      3: 'coach-member-course-status-cancelled'
    }
    return map[status] || ''
  },

  /**
   * 获取预约状态文本
   */
  getBookingStatusText(status: number): string {
    const map: Record<number, string> = {
      0: '待上课',
      1: '已签到',
      2: '已完成',
      3: '已取消'
    }
    return map[status] || '未知'
  },

  /**
   * 格式化时间
   */
  formatTime(time: string): string {
    return formatTime(time)
  },

  /**
   * 格式化日期时间
   */
  formatDateTime(time: string): string {
    return formatDateTime(time)
  }
})