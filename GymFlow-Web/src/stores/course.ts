import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Course, QueryParams, PaginatedResponse } from '@/types'
import { courseApi } from '@/api/course'

export const useCourseStore = defineStore('course', () => {
  // 状态
  const courses = ref<Course[]>([])
  const currentCourse = ref<Course | null>(null)
  const total = ref(0)
  const loading = ref(false)

  // Actions
  const fetchCourses = async (params: QueryParams = {}) => {
    try {
      loading.value = true
      const response: PaginatedResponse<Course> = await courseApi.getCourses(params)
      courses.value = response.items
      total.value = response.total
      return response
    } catch (error) {
      console.error('Fetch courses failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchCourseById = async (id: number) => {
    try {
      loading.value = true
      const response = await courseApi.getCourseById(id)
      currentCourse.value = response
      return response
    } catch (error) {
      console.error('Fetch course by id failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const createCourse = async (courseData: Partial<Course>) => {
    try {
      loading.value = true
      const response = await courseApi.createCourse(courseData)
      return response
    } catch (error) {
      console.error('Create course failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const updateCourse = async (id: number, courseData: Partial<Course>) => {
    try {
      loading.value = true
      const response = await courseApi.updateCourse(id, courseData)
      // 更新本地数据
      const index = courses.value.findIndex(course => course.id === id)
      if (index !== -1) {
        courses.value[index] = { ...courses.value[index], ...response }
      }
      return response
    } catch (error) {
      console.error('Update course failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const deleteCourse = async (id: number) => {
    try {
      loading.value = true
      await courseApi.deleteCourse(id)
      // 从本地删除
      courses.value = courses.value.filter(course => course.id !== id)
      total.value -= 1
    } catch (error) {
      console.error('Delete course failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const getCourseSchedule = async (params: QueryParams = {}) => {
    try {
      loading.value = true
      const response = await courseApi.getSchedule(params)
      return response
    } catch (error) {
      console.error('Get course schedule failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const resetCourseState = () => {
    courses.value = []
    currentCourse.value = null
    total.value = 0
  }

  return {
    // 状态
    courses,
    currentCourse,
    total,
    loading,
    
    // Actions
    fetchCourses,
    fetchCourseById,
    createCourse,
    updateCourse,
    deleteCourse,
    getCourseSchedule,
    resetCourseState
  }
})