// stores/booking.store.ts
// 预约状态管理 - 包含业务逻辑
import {
  getCourseTimetable,
  getCourseScheduleDetail,
  bookGroupCourse,
  bookPrivateCourse,
  cancelBooking,
  getMemberBookings
} from '../services/api/booking.api'
import { getCourseList } from '../services/api/course.api'
import { getMyMemberInfo } from '../services/api/member.api'
import { userStore } from './user.store'

// 课程类型常量
export const COURSE_TYPE = {
  PRIVATE: 0,  // 私教课
  GROUP: 1     // 团课
}

// 会员拥有的课程类型
export const MEMBER_COURSE_TYPE = {
  PRIVATE_ONLY: 'PRIVATE_ONLY',   // 只有私教课
  GROUP_ONLY: 'GROUP_ONLY',       // 只有团课
  BOTH: 'BOTH',                   // 两者都有
  NONE: 'NONE'                    // 都没有
}

class BookingStore {
  private _myBookings: any[] = []
  private _availablePrivateCourses: any[] = []
  private _availableGroupCourses: any[] = []
  private _currentCourseType: string = MEMBER_COURSE_TYPE.NONE
  private _currentBooking: any = null
  private _loading: boolean = false
  private _hasMore: boolean = true
  private _currentPage: number = 1
  private _pageSize: number = 10

  // Getters
  get myBookings() { return this._myBookings }
  get availablePrivateCourses() { return this._availablePrivateCourses }
  get availableGroupCourses() { return this._availableGroupCourses }
  get currentCourseType() { return this._currentCourseType }
  get currentBooking() { return this._currentBooking }
  get loading() { return this._loading }
  get hasMore() { return this._hasMore }

  /**
   * 初始化：同步会员卡信息，获取会员拥有的课程类型
   */
  async initMemberCourseType() {
    try {
      const memberId = userStore.memberId
      if (!memberId) {
        this._currentCourseType = MEMBER_COURSE_TYPE.NONE
        return MEMBER_COURSE_TYPE.NONE
      }

      const memberInfo = await getMyMemberInfo()
      userStore.updateUserInfo(memberInfo)

      const cards = memberInfo.memberCards || []
      console.log('会员卡列表:', cards)

      let hasPrivate = false
      let hasGroup = false

      for (const card of cards) {
        // ✅ 使用 cardType 而不是 productType
        console.log('卡片信息:', card)
        if (card.cardType === 1 && card.status === 'ACTIVE' && (card.remainingSessions || 0) > 0) {
          hasPrivate = true
          console.log('检测到私教课卡')
        }
        if (card.cardType === 2 && card.status === 'ACTIVE' && (card.remainingSessions || 0) > 0) {
          hasGroup = true
          console.log('检测到团课卡')
        }
      }

      if (hasPrivate && hasGroup) {
        this._currentCourseType = MEMBER_COURSE_TYPE.BOTH
      } else if (hasPrivate) {
        this._currentCourseType = MEMBER_COURSE_TYPE.PRIVATE_ONLY
      } else if (hasGroup) {
        this._currentCourseType = MEMBER_COURSE_TYPE.GROUP_ONLY
      } else {
        this._currentCourseType = MEMBER_COURSE_TYPE.NONE
      }

      console.log('最终会员卡类型:', this._currentCourseType)
      return this._currentCourseType
    } catch (error) {
      console.error('同步会员卡信息失败:', error)
      this._currentCourseType = MEMBER_COURSE_TYPE.NONE
      return MEMBER_COURSE_TYPE.NONE
    }
  }

  /**
   * 加载可预约课程
   */
  // async loadAvailableCourses(params?: { date?: string; courseType?: number }) {
  //   if (this._loading) return

  //   this._loading = true

  //   try {
  //     let startDate = params?.date
  //     let endDate = params?.date

  //     if (!startDate) {
  //       const today = new Date().toISOString().split('T')[0]
  //       startDate = today
  //       endDate = today
  //     }

  //     console.log('请求课程表，日期:', startDate)

  //     const schedules = await getCourseTimetable({ startDate, endDate })

  //     console.log('课程表返回数据:', schedules)

  //     let privateList: any[] = []
  //     let groupList: any[] = []

  //     const scheduleList = Array.isArray(schedules) ? schedules : (schedules?.list || [])

  //     for (const schedule of scheduleList) {
  //       const processed = {
  //         ...schedule,
  //         scheduleId: schedule.id || schedule.scheduleId,
  //         remainingSlots: (schedule.maxCapacity || 0) - (schedule.currentEnrollment || 0),
  //         canBook: (schedule.currentEnrollment || 0) < (schedule.maxCapacity || 0),
  //         scheduleDateDisplay: this.formatDate(schedule.scheduleDate),
  //         timeRange: `${schedule.startTime?.substring(0, 5)}-${schedule.endTime?.substring(0, 5)}`,
  //         courseType: schedule.courseType,
  //         courseTypeDesc: schedule.courseType === 0 ? '私教课' : '团课',
  //         sessionCost: schedule.sessionCost || 1
  //       }

  //       if (processed.courseType === COURSE_TYPE.PRIVATE) {
  //         privateList.push(processed)
  //       } else {
  //         groupList.push(processed)
  //       }
  //     }

  //     if (params?.courseType !== undefined) {
  //       if (params.courseType === COURSE_TYPE.PRIVATE) {
  //         groupList = []
  //       } else {
  //         privateList = []
  //       }
  //     }

  //     this._availablePrivateCourses = privateList
  //     this._availableGroupCourses = groupList

  //     console.log('私教课数量:', privateList.length, '团课数量:', groupList.length)

  //     return {
  //       privateCourses: privateList,
  //       groupCourses: groupList,
  //       total: privateList.length + groupList.length
  //     }
  //   } catch (error) {
  //     console.error('加载可预约课程失败:', error)
  //     throw error
  //   } finally {
  //     this._loading = false
  //   }
  // }

  /**
 * 加载私教课列表（按需加载）
 */
  async loadPrivateCourses(params?: { date?: string }) {
    if (this._loading) return
    this._loading = true

    try {
      console.log('开始加载私教课列表')
      const result = await getCourseList({ courseType: 0 })
      console.log('私教课接口返回:', result)

      const list = result.list || []
      console.log('私教课原始数据数量:', list.length)

      // 将每个课程按教练拆分为独立的预约项
      const splitCourses: any[] = []

      for (const course of list) {
        const coaches = course.coaches || []
        if (coaches && coaches.length > 0) {
          for (const coach of coaches) {
            splitCourses.push({
              ...course,
              // scheduleId: `${course.id}_${coach.id}`,
              coachId: coach.id,
              coachName: coach.realName,
              coachSpecialty: coach.specialty,
              coachRating: coach.rating,
              coachYearsOfExperience: coach.yearsOfExperience,
              originalCourseId: course.id,
              courseType: COURSE_TYPE.PRIVATE,
              courseTypeDesc: '私教课',
              duration: course.duration || 60,
              sessionCost: course.sessionCost || 1,
              scheduleDate: '',
              scheduleDateDisplay: '预约时间',
              timeRange: '请选择上课时间',
              maxCapacity: 1,
              currentEnrollment: 0,
              remainingSlots: 1,
              canBook: true
            })
          }
        }
      }

      this._availablePrivateCourses = splitCourses
      console.log('私教课加载完成，数量:', this._availablePrivateCourses.length)

      return {
        privateCourses: this._availablePrivateCourses,
        total: this._availablePrivateCourses.length
      }
    } catch (error) {
      console.error('加载私教课失败:', error)
      this._availablePrivateCourses = []
      throw error
    } finally {
      this._loading = false
    }
  }

  /**
   * 加载团课列表（按需加载）
   */
  async loadGroupCourses(params?: { date?: string }) {
    if (this._loading) return
    this._loading = true

    try {
      console.log('开始加载团课列表，日期:', params?.date)

      let startDate = params?.date
      let endDate = params?.date
      if (!startDate) {
        const today = new Date().toISOString().split('T')[0]
        startDate = today
        endDate = today
      }

      const schedules = await getCourseTimetable({ startDate, endDate })
      console.log('团课接口返回:', schedules)

      const scheduleList = Array.isArray(schedules) ? schedules : (schedules?.list || [])
      console.log('团课原始数据数量:', scheduleList.length)

      this._availableGroupCourses = scheduleList.map((schedule: any) => ({
        ...schedule,
        scheduleId: schedule.id || schedule.scheduleId,
        remainingSlots: (schedule.maxCapacity || 0) - (schedule.currentEnrollment || 0),
        canBook: (schedule.currentEnrollment || 0) < (schedule.maxCapacity || 0),
        scheduleDateDisplay: this.formatDate(schedule.scheduleDate),
        timeRange: `${schedule.startTime?.substring(0, 5)}-${schedule.endTime?.substring(0, 5)}`,
        courseType: COURSE_TYPE.GROUP,
        courseTypeDesc: '团课',
        sessionCost: schedule.sessionCost || 1
      }))

      console.log('团课加载完成，数量:', this._availableGroupCourses.length)

      return {
        groupCourses: this._availableGroupCourses,
        total: this._availableGroupCourses.length
      }
    } catch (error) {
      console.error('加载团课失败:', error)
      this._availableGroupCourses = []
      throw error
    } finally {
      this._loading = false
    }
  }

  /**
   * 获取排课详情
   */
  async getScheduleDetail(scheduleId: number) {
    try {
      const detail = await getCourseScheduleDetail(scheduleId)
      return {
        ...detail,
        remainingSlots: (detail.maxCapacity || 0) - (detail.currentEnrollment || 0),
        scheduleDateDisplay: this.formatDate(detail.scheduleDate),
        timeRange: `${detail.startTime?.substring(0, 5)}-${detail.endTime?.substring(0, 5)}`,
        sessionCost: detail.sessionCost || 1
      }
    } catch (error) {
      console.error('获取排课详情失败:', error)
      throw error
    }
  }

  /**
   * 预约团课 - 修正参数传递方式
   */
  async bookGroupCourse(memberId: number, scheduleId: number) {
    try {
      const result = await bookGroupCourse(memberId, scheduleId)  // 直接传两个独立参数
      return result
    } catch (error) {
      console.error('预约团课失败:', error)
      throw error
    }
  }

/**
 * 预约私教课
 */
async bookPrivateCourse(
  memberId: number,
  courseId: number,      // 新增课程ID参数
  coachId: number,
  scheduleDate: string,
  startTime: string
) {
  try {
    const result = await bookPrivateCourse(memberId, courseId, coachId, scheduleDate, startTime)
    return result
  } catch (error) {
    console.error('预约私教课失败:', error)
    throw error
  }
}

  /**
   * 统一预约接口
   */
  async createBooking(memberId: number, schedule: any) {
    if (schedule.courseType === COURSE_TYPE.GROUP) {
      return this.bookGroupCourse(memberId, schedule.scheduleId)
    } else {
      return this.bookPrivateCourse(memberId,schedule.courseId, schedule.coachId, schedule.scheduleDate, schedule.startTime)
    }
  }

  /**
   * 取消预约
   */
  async cancelBooking(bookingId: number, reason: string = '用户取消') {
    try {
      await cancelBooking(bookingId, reason)
      return true
    } catch (error) {
      console.error('取消预约失败:', error)
      throw error
    }
  }

  /**
   * 加载我的预约列表
   */
  async loadMyBookings(refresh: boolean = false, status?: number | null) {
    console.log('bookingStore.loadMyBookings, status:', status)
    if (this._loading) return

    if (refresh) {
      this._currentPage = 1
      this._hasMore = true
      this._myBookings = []
    }

    if (!this._hasMore) return

    this._loading = true

    try {
      const memberId = userStore.memberId
      if (!memberId) {
        throw new Error('用户未登录')
      }

      // 构建参数对象，只添加有值的参数
      const params: Record<string, any> = {
        pageNum: this._currentPage,
        pageSize: this._pageSize
      }

      // 只有当 status 不为 null 且不为 undefined 时才添加参数
      if (status !== null && status !== undefined) {
        params.bookingStatus = status
      }

      const result = await getMemberBookings(memberId, params)

      const list = result.list || []

      if (refresh) {
        this._myBookings = list
      } else {
        this._myBookings = [...this._myBookings, ...list]
      }

      this._hasMore = result.pageNum < result.pages
      this._currentPage++

      return result
    } catch (error) {
      console.error('加载我的预约失败:', error)
      throw error
    } finally {
      this._loading = false
    }
  }

  /**
   * 格式化日期显示
   */
  private formatDate(dateStr: string): string {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    const today = new Date()
    const tomorrow = new Date(today)
    tomorrow.setDate(tomorrow.getDate() + 1)

    if (date.toDateString() === today.toDateString()) {
      return '今天'
    } else if (date.toDateString() === tomorrow.toDateString()) {
      return '明天'
    }
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }

  /**
   * 一次性加载所有课程（根据会员卡类型）
   * 私教课：调用 /course/list?courseType=0
   * 团课：调用 /course/timetable
   */
  async loadAllCourses() {
    console.log('loadAllCourses 开始执行')
    if (this._loading) {
      console.log('正在加载中，跳过')
      return
    }

    this._loading = true

    try {
      // 获取会员卡类型
      await this.initMemberCourseType()
      console.log('会员卡类型:', this._currentCourseType)

      // 根据会员卡类型决定加载哪些数据
      const needPrivate = this._currentCourseType === 'PRIVATE_ONLY' || this._currentCourseType === 'BOTH'
      const needGroup = this._currentCourseType === 'GROUP_ONLY' || this._currentCourseType === 'BOTH'

      console.log('needPrivate:', needPrivate, 'needGroup:', needGroup)

      // 并行请求
      const promises: Promise<any>[] = []

      if (needPrivate) {
        promises.push(this.loadPrivateCourseList())
      }
      if (needGroup) {
        promises.push(this.loadGroupTimetable())
      }

      await Promise.all(promises)

      console.log('加载完成，私教课:', this._availablePrivateCourses.length, '团课:', this._availableGroupCourses.length)
      console.log('私教课数据:', this._availablePrivateCourses)
      console.log('团课数据:', this._availableGroupCourses)

    } catch (error) {
      console.error('加载全部课程失败:', error)
      throw error
    } finally {
      this._loading = false
    }
  }

  /**
   * 加载私教课列表 - 将多教练课程拆分为独立项
   */
  private async loadPrivateCourseList() {
    try {
      console.log('开始加载私教课列表')
      const result = await getCourseList({ courseType: 0 })
      console.log('私教课接口返回:', result)

      const list = result.list || []
      console.log('私教课原始数据数量:', list.length)

      // 将每个课程按教练拆分为独立的预约项
      const splitCourses: any[] = []

      for (const course of list) {
        // 获取课程绑定的教练列表
        const coaches = course.coaches || []

        if (coaches && coaches.length > 0) {
          // 为每个教练创建一个独立的课程项
          for (const coach of coaches) {
            splitCourses.push({
              ...course,
              courseId: course.id,
              // 使用 课程ID_教练ID 作为唯一标识
              // scheduleId: `${course.id}_${coach.id}`,
              coachId: coach.id,
              coachName: coach.realName,
              // 教练专属字段
              coachSpecialty: coach.specialty,
              coachRating: coach.rating,
              coachYearsOfExperience: coach.yearsOfExperience,
              // 保留原课程信息
              originalCourseId: course.id,
              courseType: COURSE_TYPE.PRIVATE,
              courseTypeDesc: '私教课',
              // 时长和课时消耗从课程获取
              duration: course.duration || 60,
              sessionCost: course.sessionCost || 1,
              // 私教课没有固定日期，显示为"预约时间"
              scheduleDate: '',
              scheduleDateDisplay: '预约时间',
              timeRange: '请选择上课时间',
              // 私教课没有人数限制
              maxCapacity: 1,
              currentEnrollment: 0,
              remainingSlots: 1,
              canBook: true
            })
          }
        } else if (course.coachId) {
          // 兼容旧数据：如果只有一个教练
          splitCourses.push({
            ...course,
            courseId: course.id,
            // scheduleId: course.id,
            coachId: course.coachId,
            coachName: course.coachName,
            courseType: COURSE_TYPE.PRIVATE,
            courseTypeDesc: '私教课',
            duration: course.duration || 60,
            sessionCost: course.sessionCost || 1,
            scheduleDate: '',
            scheduleDateDisplay: '预约时间',
            timeRange: '请选择上课时间',
            maxCapacity: 1,
            currentEnrollment: 0,
            remainingSlots: 1,
            canBook: true
          })
        }
      }

      this._availablePrivateCourses = splitCourses
      console.log('私教课拆分后数量:', this._availablePrivateCourses.length)
      console.log('拆分后的私教课数据:', this._availablePrivateCourses)

    } catch (error) {
      console.error('加载私教课失败:', error)
      this._availablePrivateCourses = []
    }
  }

  /**
   * 加载团课课表
   */
  private async loadGroupTimetable() {
    try {
      console.log('开始加载团课列表')
      const schedules = await getCourseTimetable()
      console.log('团课接口返回:', schedules)

      const scheduleList = Array.isArray(schedules) ? schedules : (schedules?.list || [])
      console.log('团课原始数据数量:', scheduleList.length)

      this._availableGroupCourses = scheduleList.map((schedule: any) => ({
        ...schedule,
        scheduleId: schedule.id || schedule.scheduleId,
        remainingSlots: (schedule.maxCapacity || 0) - (schedule.currentEnrollment || 0),
        canBook: (schedule.currentEnrollment || 0) < (schedule.maxCapacity || 0),
        scheduleDateDisplay: this.formatDate(schedule.scheduleDate),
        timeRange: `${schedule.startTime?.substring(0, 5)}-${schedule.endTime?.substring(0, 5)}`,
        courseType: COURSE_TYPE.GROUP,
        courseTypeDesc: '团课',
        sessionCost: schedule.sessionCost || 1
      }))

      console.log('团课加载完成:', this._availableGroupCourses.length)
    } catch (error) {
      console.error('加载团课失败:', error)
      this._availableGroupCourses = []
    }
  }

  private getBookingStatusFromOrder(order: any): number {
    const statusMap: Record<string, number> = {
      'PENDING': 0,
      'PROCESSING': 0,
      'COMPLETED': 2,
      'CANCELLED': 3,
      'REFUNDED': 3
    }
    return statusMap[order.orderStatus] ?? 0
  }

  private getBookingStatusText(order: any): string {
    const statusMap: Record<string, string> = {
      'PENDING': '待上课',
      'PROCESSING': '待上课',
      'COMPLETED': '已完成',
      'CANCELLED': '已取消',
      'REFUNDED': '已取消',
      'EXPIRED': '已过期'
    }
    return statusMap[order.orderStatus] ?? '待上课'
  }

  /**
   * 重置状态
   */
  reset() {
    this._myBookings = []
    this._availablePrivateCourses = []
    this._availableGroupCourses = []
    this._currentCourseType = MEMBER_COURSE_TYPE.NONE
    this._currentBooking = null
    this._hasMore = true
    this._currentPage = 1
    this._loading = false
  }
}

export const bookingStore = new BookingStore()