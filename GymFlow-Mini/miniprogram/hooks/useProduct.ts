// 商品相关自定义Hook

import { useState, useEffect } from '../lib/wx-hooks' // 假设有这样一个库，或者直接用store
import { 
  getProductList, 
  getProductDetail,
  getProductCategories 
} from '../services/api/product.api'
import { Product, ProductCategory, ProductQueryParams } from '../services/types/product.types'
import { showError, showLoading, hideLoading } from '../utils/wx-util'

// 由于小程序不支持React Hooks，这里模拟一个简单的Hook
// 实际使用中可以直接使用store或Page中的方法
export const useProduct = () => {
  /**
   * 加载商品列表
   */
  const loadProducts = async (params?: ProductQueryParams) => {
    try {
      return await getProductList(params || {})
    } catch (error: any) {
      showError(error.message || '加载商品失败')
      throw error
    }
  }

  /**
   * 获取商品详情
   */
  const fetchProductDetail = async (productId: number) => {
    try {
      showLoading()
      const detail = await getProductDetail(productId)
      hideLoading()
      return detail
    } catch (error: any) {
      hideLoading()
      showError(error.message || '获取商品详情失败')
      throw error
    }
  }

  /**
   * 加载商品分类
   */
  const loadCategories = async () => {
    try {
      const categories = await getProductCategories()
      
      // 只返回顶级分类（parent_id = 0）
      const topCategories = categories.filter(c => c.parentId === 0)
      
      return {
        all: categories,
        top: topCategories
      }
    } catch (error: any) {
      showError(error.message || '加载分类失败')
      throw error
    }
  }

  /**
   * 获取子分类
   */
  const getSubCategories = (categories: ProductCategory[], parentId: number): ProductCategory[] => {
    return categories.filter(c => c.parentId === parentId)
  }

  /**
   * 按类型获取商品
   */
  const getProductsByType = (products: Product[], type: number): Product[] => {
    return products.filter(p => p.productType === type)
  }

  /**
   * 格式化价格
   */
  const formatPrice = (price: number): string => {
    return `¥${price.toFixed(2)}`
  }

  /**
   * 计算折扣
   */
  const calculateDiscount = (original: number, current: number): number => {
    if (original <= 0) return 0
    return Math.round((1 - current / original) * 10)
  }

  return {
    loadProducts,
    getProductDetail: fetchProductDetail,
    loadCategories,
    getSubCategories,
    getProductsByType,
    formatPrice,
    calculateDiscount
  }
}