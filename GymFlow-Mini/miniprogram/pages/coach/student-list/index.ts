// 教练端课程学员列表页面
import { TabBarHelper } from '../../../utils/tabbar-helper'
import { getCourseStudents } from '../../../services/api/coach.api'
import { showToast, showModal, showLoading, hideLoading } from '../../../utils/wx-util'
import { configStore } from '../../../stores/config.store'

Page({
  data: {
    selectedTab: 0,
    // 课程ID
    scheduleId: 0,

    // 课程信息（从上一页传入）
    courseInfo: {
      courseName: '',
      courseType: 0,
      scheduleDate: '',
      startTime: '',
      endTime: '',
      currentEnrollment: 0
    },

    // 学员列表（原始数据）
    students: [] as any[],

    // 显示用的学员列表（根据activeTab过滤后）
    displayStudents: [] as any[],

    // 当前选中Tab 0-全部 1-待签到 2-已签到
    activeTab: 0,

    // 加载状态
    loading: true,

    // 是否可以核销
    // canCheckin: false,

    // 统计数字
    totalCount: 0,
    pendingCount: 0,
    checkedCount: 0
  },

  onLoad(options: any) {
    // 获取当前页面在 TabBar 中的索引
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const pagePath = '/' + currentPage.route
    this.setData({
      selectedTab: TabBarHelper.getSelectedIndex(pagePath)
    })

    const { scheduleId, courseName, courseType, scheduleDate, startTime, endTime } = options

    if (!scheduleId) {
      showToast('参数错误', 'none')
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
      return
    }

    // 从上一页传入的课程信息
    this.setData({
      scheduleId: parseInt(scheduleId),
      courseInfo: {
        courseName: decodeURIComponent(courseName || ''),
        courseType: parseInt(courseType || '0'),
        scheduleDate: scheduleDate || '',
        startTime: startTime || '',
        endTime: endTime || '',
        currentEnrollment: 0
      }
    })

    console.log(this.data.courseInfo, 'courseInfo')

    this.initData()
  },

  /**
   * 初始化数据
   */
  async initData() {
    this.setData({ loading: true })

    try {
      await this.loadStudents()
      // this.checkCheckinStatus()
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
    const { scheduleId, courseInfo } = this.data
    console.log(courseInfo, 'courseInfo')
    try {
      const result = await getCourseStudents(scheduleId)
      console.log(result)

      // 计算统计数字
      let pending = 0
      let checked = 0
      for (let i = 0; i < result.length; i++) {
        if (result[i].bookingStatus == 0 || result[i].bookingStatus == 4) {
          pending++
        } else if (result[i].bookingStatus == 1) {
          checked++
        }
      }

      // 更新当前报名人数和学员列表
      this.setData({
        'courseInfo.currentEnrollment': result.length,
        students: result,
        totalCount: result.length,
        pendingCount: pending,
        checkedCount: checked
      })

      // 根据当前activeTab更新显示列表
      this.updateDisplayStudents()

      console.log(this.data.students)
    } catch (error) {
      console.error('加载学员列表失败:', error)
      throw error
    }
  },

  /**
   * 根据activeTab更新显示列表
   */
  updateDisplayStudents() {
    const { students, activeTab } = this.data

    let displayStudents: any[] = []
    if (activeTab == 0) {
      displayStudents = students
    } else if (activeTab == 1) {
      displayStudents = students.filter((s: any) => s.bookingStatus == 0 || s.bookingStatus == 4)
    } else {
      displayStudents = students.filter((s: any) => s.bookingStatus == 1)
    }

    this.setData({ displayStudents })
  },

  /**
   * 检查是否可核销
   */
  // checkCheckinStatus() {
  //   const { courseInfo } = this.data

  //   if (!courseInfo.scheduleDate || !courseInfo.startTime) {
  //     this.setData({ canCheckin: false })
  //     return
  //   }

  //   const canCheckin = configStore.canCheckin(
  //     courseInfo.scheduleDate,
  //     courseInfo.startTime
  //   )

  //   this.setData({ canCheckin })
  // },

  /**
   * Tab切换
   */
  onTabChange(e: any) {
    console.log('tab切换', e)
    const { index } = e.currentTarget.dataset
    this.setData({ activeTab: parseInt(index) }, () => {
      this.updateDisplayStudents()
    })
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

})