import { defineStore } from 'pinia'
import { ref } from 'vue'
import { courseApi } from '@/api/course'
import type {
  CourseQueryParams,
  CourseBasicDTO,
  CourseScheduleDTO,
  CourseListVO,
  CourseDetail,
  CourseScheduleVO,
  PageResultVO
} from '@/types/course'

export const useCourseStore = defineStore('course', () => {
  const courseList = ref<CourseListVO[]>([])
  const currentCourse = ref<CourseDetail | null>(null)
  const courseSchedules = ref<CourseScheduleVO[]>([])
  const courseTimetable = ref<CourseScheduleVO[]>([])
  const total = ref(0)
  const loading = ref(false)
  const pageInfo = ref({
    pageNum: 1,
    pageSize: 10,
    totalPages: 0
  })
  
  /**
   * 分页查询课程列表
   */
  const fetchCourseList = async (params: CourseQueryParams = {}) => {
    try {
      loading.value = true
      const queryParams = {
        pageNum: params.pageNum || pageInfo.value.pageNum,
        pageSize: params.pageSize || pageInfo.value.pageSize,
        ...params
      }
      
      const response = await courseApi.getCourseList(queryParams)
      if (response.code === 200) {
        courseList.value = response.data.list
        total.value = response.data.total
        pageInfo.value = {
          pageNum: response.data.pageNum,
          pageSize: response.data.pageSize,
          totalPages: response.data.pages || Math.ceil(response.data.total / response.data.pageSize)
        }
      }
      return response.data
    } catch (error) {
      console.error('获取课程列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加课程
   */
  const addCourse = async (data: CourseBasicDTO) => {
    try {
      loading.value = true
      const response = await courseApi.addCourse(data)
      if (response.code === 200) {
        await fetchCourseList({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
      }
      return response
    } catch (error) {
      console.error('添加课程失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新课程信息
   */
  const updateCourse = async (courseId: number, data: CourseBasicDTO) => {
    try {
      loading.value = true
      const response = await courseApi.updateCourse(courseId, data)
      if (response.code === 200) {
        if (currentCourse.value?.id === courseId) {
          await fetchCourseDetail(courseId)
        }
        await fetchCourseList({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
      }
      return response
    } catch (error) {
      console.error('更新课程失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除课程
   */
  const deleteCourse = async (courseId: number) => {
    try {
      loading.value = true
      const response = await courseApi.deleteCourse(courseId)
      if (response.code === 200) {
        courseList.value = courseList.value.filter(item => item.id !== courseId)
        total.value -= 1
        
        if (currentCourse.value?.id === courseId) {
          currentCourse.value = null
          courseSchedules.value = []
        }
      }
      return response
    } catch (error) {
      console.error('删除课程失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新课程状态
   */
  const updateCourseStatus = async (courseId: number, status: number) => {
    try {
      loading.value = true
      const response = await courseApi.updateCourseStatus(courseId, status)
      if (response.code === 200) {
        const index = courseList.value.findIndex(item => item.id === courseId)
        if (index !== -1) {
          courseList.value[index].status = status
          courseList.value[index].statusDesc = status === 1 ? '正常' : '禁用'
        }
        if (currentCourse.value?.id === courseId) {
          currentCourse.value.status = status
          currentCourse.value.statusDesc = status === 1 ? '正常' : '禁用'
        }
      }
      return response
    } catch (error) {
      console.error('更新课程状态失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取课程详情
   */
  const fetchCourseDetail = async (courseId: number) => {
    try {
      loading.value = true
      const response = await courseApi.getCourseDetail(courseId)
      if (response.code === 200) {
        currentCourse.value = response.data
        await fetchCourseSchedules(courseId)
      }
      return response.data
    } catch (error) {
      console.error('获取课程详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取课程排课列表
   */
  const fetchCourseSchedules = async (courseId: number) => {
    try {
      loading.value = true
      const response = await courseApi.getCourseSchedules(courseId)
      if (response.code === 200) {
        courseSchedules.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取课程排课失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取课程表
   */
  const fetchCourseTimetable = async (startDate?: string, endDate?: string) => {
    try {
      loading.value = true
      const response = await courseApi.getCourseTimetable(startDate, endDate)
      if (response.code === 200) {
        courseTimetable.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取课程表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 课程排课（团课）
   */
  const scheduleCourse = async (data: CourseScheduleDTO) => {
    try {
      loading.value = true
      const response = await courseApi.scheduleCourse(data)
      if (response.code === 200 && currentCourse.value) {
        await fetchCourseSchedules(currentCourse.value.id)
      }
      return response
    } catch (error) {
      console.error('课程排课失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除排课
   */
  const deleteCourseSchedule = async (scheduleId: number) => {
    try {
      loading.value = true
      const response = await courseApi.deleteCourseSchedule(scheduleId)
      if (response.code === 200) {
        // 从本地列表中移除
        courseSchedules.value = courseSchedules.value.filter(
          item => item.scheduleId !== scheduleId
        )
        // 如果当前课程有排课列表，也同步更新
        if (currentCourse.value?.schedules) {
          currentCourse.value.schedules = currentCourse.value.schedules.filter(
            item => item.scheduleId !== scheduleId
          )
        }
      }
      return response
    } catch (error) {
      console.error('删除排课失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 核销课程预约
   */
  const verifyCourseBooking = async (bookingId: number, checkinMethod: number) => {
    try {
      loading.value = true
      const response = await courseApi.verifyCourseBooking(bookingId, checkinMethod)
      return response
    } catch (error) {
      console.error('核销课程预约失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 取消课程预约
   */
  const cancelCourseBooking = async (bookingId: number, reason: string) => {
    try {
      loading.value = true
      const response = await courseApi.cancelCourseBooking(bookingId, reason)
      return response
    } catch (error) {
      console.error('取消课程预约失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置分页信息
   */
  const setPageInfo = (pageNum: number, pageSize: number) => {
    pageInfo.value.pageNum = pageNum
    pageInfo.value.pageSize = pageSize
  }

  /**
   * 清空当前课程数据
   */
  const clearCurrentCourse = () => {
    currentCourse.value = null
    courseSchedules.value = []
  }

  /**
   * 重置状态
   */
  const resetState = () => {
    courseList.value = []
    currentCourse.value = null
    courseSchedules.value = []
    courseTimetable.value = []
    total.value = 0
    loading.value = false
    pageInfo.value = {
      pageNum: 1,
      pageSize: 10,
      totalPages: 0
    }
  }

  // 格式化课程列表
  const formattedCourseList = () => {
    return courseList.value.map(course => ({
      ...course,
      sessionCostFormatted: course.sessionCost ? `${course.sessionCost}课时` : '-',
      durationFormatted: course.duration ? `${course.duration}分钟` : '-',
      statusDesc: course.status === 1 ? '正常' : '禁用'
    }))
  }

  return {
    // 状态
    courseList,
    currentCourse,
    courseSchedules,
    courseTimetable,
    total,
    loading,
    pageInfo,
    
    // Actions
    fetchCourseList,
    addCourse,
    updateCourse,
    deleteCourse,
    updateCourseStatus,
    fetchCourseDetail,
    fetchCourseSchedules,
    fetchCourseTimetable,
    scheduleCourse,
    deleteCourseSchedule,
    verifyCourseBooking,
    cancelCourseBooking,
    setPageInfo,
    clearCurrentCourse,
    resetState,
    
    // Getters
    formattedCourseList
  }
})