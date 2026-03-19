// 商品相关API

import { wxRequest } from '../../utils/request'
import { 
  Product, 
  ProductCategory, 
  ProductQueryParams, 
  PageResult 
} from '../types/product.types'

/**
 * 获取商品列表
 * POST /product/list
 */
export const getProductList = (params: ProductQueryParams) => {
  return wxRequest.post<PageResult<Product>>('/product/list', params, {
    showLoading: false
  })
}

/**
 * 获取商品详情
 * GET /product/detail/{productId}
 */
export const getProductDetail = (productId: number) => {
  return wxRequest.get<Product>(`/product/detail/${productId}`, null, {
    showLoading: true
  })
}

/**
 * 获取商品分类
 * GET /product/categories
 */
export const getProductCategories = () => {
  return wxRequest.get<ProductCategory[]>('/product/categories', null, {
    showLoading: false
  })
}

/**
 * 获取分类详情
 * GET /product/category/detail/{categoryId}
 */
export const getCategoryDetail = (categoryId: number) => {
  return wxRequest.get<ProductCategory>(`/product/category/detail/${categoryId}`)
}

/**
 * 搜索商品
 * GET /mini/search/products
 */
export const searchProducts = (keyword: string, pageNum: number = 1, pageSize: number = 10) => {
  return wxRequest.get<PageResult<Product>>('/mini/search/products', {
    keyword,
    pageNum,
    pageSize
  })
}