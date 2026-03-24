// 教练端课程学员列表页面
import { TabBarHelper } from '../../../utils/tabbar-helper'
import { getCourseStudents } from '../../../services/api/coach.api'
import { scanCheckin } from '../../../services/api/checkin.api'
import { formatTime, formatDateTime } from '../../../utils/date'
import { showToast, showModal, showLoading, hideLoading } from '../../../utils/wx-util'
import { configStore } from '../../../stores/config.store'

Page({
  data: {
    selectedTab: 0,
    // 课程ID
    courseId: 0,
    
    // 课程信息
    courseInfo: null as any,
    
    // 学员列表
    students: [] as any[],
    
    // 当前选中Tab 0-全部 1-待签到 2-已签到
    activeTab: 0,
    
    // 加载状态
    loading: true,
    
    // 是否可以核销
    canCheckin: false
  },

  onLoad(options: any) {
    // 获取当前页面在 TabBar 中的索引
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const pagePath = '/' + currentPage.route
    this.setData({
      selectedTab: TabBarHelper.getSelectedIndex(pagePath)
    })

    const { courseId, courseName } = options
    
    if (!courseId) {
      showToast('参数错误', 'none')
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
      return
    }
    
    this.setData({ 
      courseId: parseInt(courseId),
      'courseInfo.courseName': courseName || ''
    })
    
    this.initData()
  },

  onTabChange(e: any) {
    const { index } = e.detail
    this.setData({ selectedTab: index })
  },

  /**
   * 初始化数据
   */
  async initData() {
    this.setData({ loading: true })
    
    try {
      await this.loadStudents()
      this.checkCheckinStatus()
    } catch (error) {
      console.error('加载数据失败:', error)
    } finally {
      this.setData({ loading: false })
    }
  },

  /**
   * 加载学员列表
   */
  async loadStudents() {
    const { courseId } = this.data
    
    try {
      const result = await getCourseStudents(courseId)
      
      // 提取课程信息
      if (result.length > 0) {
        this.setData({
          courseInfo: {
            courseName: result[0].courseName,
            courseType: result[0].courseType,
            courseDate: result[0].courseDate,
            startTime: result[0].startTime,
            endTime: result[0].endTime,
            currentEnrollment: result.length
          }
        })
      }
      
      this.setData({ students: result })
      
    } catch (error) {
      console.error('加载学员列表失败:', error)
      throw error
    }
  },

  /**
   * 检查是否可核销
   */
  checkCheckinStatus() {
    const { courseInfo } = this.data
    
    if (!courseInfo || !courseInfo.courseDate || !courseInfo.startTime) {
      this.setData({ canCheckin: false })
      return
    }
    
    const canCheckin = configStore.canCheckin(
      courseInfo.courseDate,
      courseInfo.startTime
    )
    
    this.setData({ canCheckin })
  },

  /**
   * Tab切换
   */
  onTabChange(e: any) {
    const { index } = e.currentTarget.dataset
    this.setData({ activeTab: parseInt(index) })
  },

  /**
   * 点击学员
   */
  onStudentTap(e: any) {
    const { student } = e.currentTarget.dataset
    
    wx.navigateTo({
      url: `/pages/coach/member-detail/index?memberId=${student.memberId}`
    })
  },

  /**
   * 扫码核销
   */
  async onScanTap() {
    try {
      const { canCheckin } = this.data
      
      if (!canCheckin) {
        showToast('当前不在可核销时间内', 'none')
        return
      }
      
      // 调用微信扫码
      const { result } = await wx.scanCode({
        onlyFromCamera: false,
        scanType: ['qrCode']
      })
      
      // 解析二维码内容
      // 格式: gymflow://checkin?bookingId=123&code=123456
      if (!result || !result.startsWith('gymflow://checkin')) {
        showToast('无效的签到码', 'none')
        return
      }
      
      // 简单解析参数
      const bookingIdMatch = result.match(/bookingId=(\d+)/)
      const codeMatch = result.match(/code=(\d+)/)
      
      if (!bookingIdMatch || !codeMatch) {
        showToast('无效的签到码格式', 'none')
        return
      }
      
      const bookingId = parseInt(bookingIdMatch[1])
      const code = codeMatch[1]
      
      showLoading('核销中...')
      
      await scanCheckin({
        code: result
      })
      
      hideLoading()
      showToast('核销成功', 'success')
      
      // 刷新列表
      this.loadStudents()
      
    } catch (error: any) {
      hideLoading()
      if (error.errMsg && error.errMsg.indexOf('cancel') > -1) {
        // 用户取消扫码，不提示错误
        return
      }
      showToast(error.message || '核销失败', 'none')
    }
  },

  /**
   * 手动核销（预留）
   */
  async onManualCheckin(e: any) {
    const { student } = e.currentTarget.dataset
    
    if (student.bookingStatus === 1) {
      showToast('已签到', 'none')
      return
    }
    
    const confirm = await showModal({
      title: '提示',
      content: `确认将 ${student.memberName} 标记为已签到？`
    })
    
    if (!confirm) return
    
    // 这里可以调用手动核销接口
    showToast('功能开发中', 'none')
  },

  /**
   * 获取筛选后的学员列表
   */
  get filteredStudents(): any[] {
    const { students, activeTab } = this.data
    
    if (activeTab === 0) {
      return students
    } else if (activeTab === 1) {
      const result: any[] = []
      for (let i = 0; i < students.length; i++) {
        if (students[i].bookingStatus === 0) {
          result.push(students[i])
        }
      }
      return result
    } else {
      const result: any[] = []
      for (let i = 0; i < students.length; i++) {
        if (students[i].bookingStatus === 1) {
          result.push(students[i])
        }
      }
      return result
    }
  },

  /**
   * 获取待签到人数
   */
  get pendingCount(): number {
    const { students } = this.data
    let count = 0
    for (let i = 0; i < students.length; i++) {
      if (students[i].bookingStatus === 0) {
        count++
      }
    }
    return count
  },

  /**
   * 获取已签到人数
   */
  get checkedCount(): number {
    const { students } = this.data
    let count = 0
    for (let i = 0; i < students.length; i++) {
      if (students[i].bookingStatus === 1) {
        count++
      }
    }
    return count
  },

  /**
   * 获取总人数
   */
  get totalCount(): number {
    return this.data.students.length
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