import { defineStore } from 'pinia'
import { ref } from 'vue'
import { productApi } from '@/api/product'
import type { 
  ProductQueryDTO, 
  ProductBasicDTO, 
  ProductFullDTO,
  ProductListVO,
  PageResultVO
} from '@/types/product'

export const useProductStore = defineStore('product', () => {
  const productList = ref<ProductListVO[]>([])
  const currentProduct = ref<ProductFullDTO | null>(null)
  const total = ref(0)
  const loading = ref(false)
  const pageInfo = ref({
    pageNum: 1,
    pageSize: 10,
    totalPages: 0
  })

  /**
   * 分页查询商品列表
   */
  const fetchProductList = async (params: ProductQueryDTO = {}) => {
    try {
      loading.value = true
      const queryParams = {
        pageNum: params.pageNum || pageInfo.value.pageNum,
        pageSize: params.pageSize || pageInfo.value.pageSize,
        ...params
      }
      
      const response = await productApi.getProductList(queryParams)
      if (response.code === 200) {
        productList.value = response.data.list
        total.value = response.data.total
        pageInfo.value = {
          pageNum: response.data.pageNum,
          pageSize: response.data.pageSize,
          totalPages: response.data.pages || Math.ceil(response.data.total / response.data.pageSize)
        }
      }
      return response.data
    } catch (error) {
      console.error('获取商品列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取商品详情
   */
  const fetchProductDetail = async (productId: number) => {
    try {
      loading.value = true
      const response = await productApi.getProductDetail(productId)
      if (response.code === 200) {
        currentProduct.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取商品详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加商品
   */
  const addProduct = async (data: ProductBasicDTO) => {
    try {
      loading.value = true
      const response = await productApi.addProduct(data)
      if (response.code === 200) {
        // await fetchProductList({
        //   pageNum: pageInfo.value.pageNum,
        //   pageSize: pageInfo.value.pageSize
        // })
      }
      return response
    } catch (error) {
      console.error('添加商品失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新商品信息
   */
  const updateProduct = async (productId: number, data: ProductBasicDTO) => {
    try {
      loading.value = true
      const response = await productApi.updateProduct(productId, data)
      if (response.code === 200) {
        // if (currentProduct.value?.id === productId) {
        //   await fetchProductDetail(productId)
        // }
        // await fetchProductList({
        //   pageNum: pageInfo.value.pageNum,
        //   pageSize: pageInfo.value.pageSize
        // })
      }
      return response
    } catch (error) {
      console.error('更新商品失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除商品
   */
  const deleteProduct = async (productId: number) => {
    try {
      loading.value = true
      const response = await productApi.deleteProduct(productId)
      if (response.code === 200) {
        productList.value = productList.value.filter(item => item.id !== productId)
        total.value -= 1
        
        if (currentProduct.value?.id === productId) {
          currentProduct.value = null
        }
      }
      return response
    } catch (error) {
      console.error('删除商品失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新商品状态
   */
  const updateProductStatus = async (productId: number, status: number) => {
    try {
      loading.value = true
      const response = await productApi.updateProductStatus(productId, status)
      if (response.code === 200) {
        const index = productList.value.findIndex(item => item.id === productId)
        if (index !== -1) {
          productList.value[index].status = status
          productList.value[index].statusDesc = status === 1 ? '在售' : '下架'
        }
        if (currentProduct.value?.id === productId) {
          currentProduct.value.status = status
          currentProduct.value.statusDesc = status === 1 ? '在售' : '下架'
        }
      }
      return response
    } catch (error) {
      console.error('更新商品状态失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新商品库存
   */
  const updateProductStock = async (productId: number, quantity: number) => {
    try {
      loading.value = true
      const response = await productApi.updateProductStock(productId, quantity)
      if (response.code === 200) {
        const index = productList.value.findIndex(item => item.id === productId)
        if (index !== -1) {
          productList.value[index].stockQuantity += quantity
        }
        if (currentProduct.value?.id === productId) {
          currentProduct.value.stockQuantity += quantity
        }
      }
      return response
    } catch (error) {
      console.error('更新商品库存失败:', error)
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
   * 清空当前商品数据
   */
  const clearCurrentProduct = () => {
    currentProduct.value = null
  }

  /**
   * 重置状态
   */
  const resetState = () => {
    productList.value = []
    currentProduct.value = null
    total.value = 0
    loading.value = false
    pageInfo.value = {
      pageNum: 1,
      pageSize: 10,
      totalPages: 0
    }
  }

  // Getters
  const hasNextPage = () => {
    return pageInfo.value.pageNum < pageInfo.value.totalPages
  }

  const hasPrevPage = () => {
    return pageInfo.value.pageNum > 1
  }

  // 格式化商品列表
  const formattedProductList = () => {
    return productList.value.map(product => ({
      ...product,
      priceFormatted: product.currentPrice ? `¥${product.currentPrice.toFixed(2)}` : '-',
      originalPriceFormatted: product.originalPrice ? `¥${product.originalPrice.toFixed(2)}` : '-',
      discountFormatted: product.discount ? `${product.discount.toFixed(1)}折` : '-',
      stockStatus: product.stockQuantity > 0 ? '有货' : '缺货',
      stockStatusClass: product.stockQuantity > 0 ? 'text-success' : 'text-danger',
      statusDesc: product.status === 1 ? '在售' : '下架',
      statusClass: product.status === 1 ? 'status-active' : 'status-inactive',
      createTimeFormatted: product.createTime ? new Date(product.createTime).toLocaleDateString() : '-',
      totalSessionsFormatted: product.totalSessions ? `${product.totalSessions}节` : '-',
      validityDaysFormatted: product.validityDays ? `${product.validityDays}天` : '-'
    }))
  }

  return {
    productList,
    currentProduct,
    total,
    loading,
    pageInfo,
    
    fetchProductList,
    fetchProductDetail,
    addProduct,
    updateProduct,
    deleteProduct,
    updateProductStatus,
    updateProductStock,
    setPageInfo,
    clearCurrentProduct,
    resetState,
    
    hasNextPage,
    hasPrevPage,
    formattedProductList
  }
})