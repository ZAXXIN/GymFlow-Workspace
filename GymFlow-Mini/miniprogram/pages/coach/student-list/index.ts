// 教练端课程学员列表页面
import { TabBarHelper } from '../../../utils/tabbar-helper'
import { getCourseStudents } from '../../../services/api/coach.api'
import { scanCheckin } from '../../../services/api/checkin.api'
import { showToast, showModal, showLoading, hideLoading } from '../../../utils/wx-util'
import { configStore } from '../../../stores/config.store'

Page({
  data: {
    selectedTab: 0,
    // 课程ID
    courseId: 0,
    
    // 课程信息（从上一页传入）
    courseInfo: {
      courseName: '',
      courseType: 0,
      scheduleDate: '',
      startTime: '',
      endTime: '',
      location: '',
      currentEnrollment: 0
    },
    
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

    const { courseId, courseName, courseType, scheduleDate, startTime, endTime, location } = options
    
    if (!courseId) {
      showToast('参数错误', 'none')
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
      return
    }
    
    // 从上一页传入的课程信息
    this.setData({ 
      courseId: parseInt(courseId),
      courseInfo: {
        courseName: decodeURIComponent(courseName || ''),
        courseType: parseInt(courseType || '0'),
        scheduleDate: scheduleDate || '',
        startTime: startTime || '',
        endTime: endTime || '',
        location: location ? decodeURIComponent(location) : '',
        currentEnrollment: 0
      }
    })

    console.log(this.data.courseInfo,'courseInfo')
    
    this.initData()
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
    const { courseId, courseInfo } = this.data
    console.log(courseInfo,'courseInfo')
    try {
      const result = await getCourseStudents(courseId)
      console.log(result)
      // 更新当前报名人数
      this.setData({
        'courseInfo.currentEnrollment': result.length,
        students: result
      })
      console.log(this.data.students)
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
    
    if (!courseInfo.scheduleDate || !courseInfo.startTime) {
      this.setData({ canCheckin: false })
      return
    }
    
    const canCheckin = configStore.canCheckin(
      courseInfo.scheduleDate,
      courseInfo.startTime
    )
    
    this.setData({ canCheckin })
  },

  /**
   * Tab切换
   */
  onTabChange(e: any) {
    console.log('tab')
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
        return
      }
      showToast(error.message || '核销失败', 'none')
    }
  },

  /**
   * 获取筛选后的学员列表
   */
  get filteredStudents(): any[] {
    const { students, activeTab } = this.data
    console.log(students,'filteredStudents')
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
      console.log(result)
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
    console.log(students,'student')
    let count = 0
    for (let i = 0; i < students.length; i++) {
      if (students[i].bookingStatus === 1) {
        count++
      }
    }
    console.log(count,'count')
    return count
  },

  /**
   * 获取总人数
   */
  get totalCount(): number {
    return this.data.students.length
  },
})