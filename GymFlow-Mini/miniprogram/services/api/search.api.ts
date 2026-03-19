// 搜索相关API

import { wxRequest } from '../../utils/request'
import { SearchParams, PageResult } from '../types/common.types'
import { Product } from '../types/product.types'
import { CourseSchedule } from '../types/booking.types'

/**
 * 搜索商品
 * GET /mini/search/products
 */
export const searchProducts = (params: SearchParams) => {
  return wxRequest.get<PageResult<Product>>('/mini/search/products', {
    keyword: params.keyword,
    pageNum: params.pageNum || 1,
    pageSize: params.pageSize || 10
  })
}

/**
 * 搜索课程
 * GET /mini/search/courses
 */
export const searchCourses = (params: SearchParams) => {
  return wxRequest.get<PageResult<CourseSchedule>>('/mini/search/courses', {
    keyword: params.keyword,
    pageNum: params.pageNum || 1,
    pageSize: params.pageSize || 10
  })
}

/**
 * 综合搜索
 * GET /mini/search/all
 */
export const searchAll = (params: SearchParams) => {
  return wxRequest.get<{
    products: PageResult<Product>
    courses: PageResult<CourseSchedule>
  }>('/mini/search/all', {
    keyword: params.keyword,
    pageNum: params.pageNum || 1,
    pageSize: params.pageSize || 10
  })
}

/**
 * 获取热门搜索
 * GET /mini/search/hot
 */
export const getHotSearches = () => {
  return wxRequest.get<string[]>('/mini/search/hot')
}