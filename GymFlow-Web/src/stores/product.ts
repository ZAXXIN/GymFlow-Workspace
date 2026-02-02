import { defineStore } from 'pinia'
import { ref } from 'vue'
import { productApi } from '@/api/product'
import type { 
  ProductQueryDTO, 
  ProductBasicDTO, 
  ProductCategoryDTO,
  ProductFullDTO,
  ProductListVO,
  PageResultVO
} from '@/types/product'

export const useProductStore = defineStore('product', () => {
  // 状态
  const productList = ref<ProductListVO[]>([])
  const currentProduct = ref<ProductFullDTO | null>(null)
  const categories = ref<ProductCategoryDTO[]>([])
  const currentCategory = ref<ProductCategoryDTO | null>(null)
  const total = ref(0)
  const loading = ref(false)
  const pageInfo = ref({
    pageNum: 1,
    pageSize: 10,
    totalPages: 0
  })

  // Actions
  
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
        // 添加成功后重新加载列表
        await fetchProductList({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
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
        // 更新成功后刷新当前商品详情
        if (currentProduct.value?.id === productId) {
          await fetchProductDetail(productId)
        }
        // 刷新列表
        await fetchProductList({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
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
        // 从本地列表中移除
        productList.value = productList.value.filter(item => item.id !== productId)
        total.value -= 1
        
        // 如果删除的是当前查看的商品，清空当前商品数据
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
        // 更新列表中的状态
        const index = productList.value.findIndex(item => item.id === productId)
        if (index !== -1) {
          productList.value[index].status = status
          productList.value[index].statusDesc = status === 1 ? '在售' : '下架'
        }
        // 更新当前商品的状态
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
        // 更新列表中的库存
        const index = productList.value.findIndex(item => item.id === productId)
        if (index !== -1) {
          productList.value[index].stockQuantity += quantity
        }
        // 更新当前商品的库存
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
   * 获取所有商品分类
   */
  const fetchCategories = async () => {
    try {
      loading.value = true
      const response = await productApi.getAllCategories()
      if (response.code === 200) {
        categories.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取商品分类失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取分类详情
   */
  const fetchCategoryDetail = async (categoryId: number) => {
    try {
      loading.value = true
      const response = await productApi.getCategoryDetail(categoryId)
      if (response.code === 200) {
        currentCategory.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取分类详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加分类
   */
  const addCategory = async (data: ProductCategoryDTO) => {
    try {
      loading.value = true
      const response = await productApi.addCategory(data)
      if (response.code === 200) {
        // 添加成功后重新加载分类列表
        await fetchCategories()
      }
      return response
    } catch (error) {
      console.error('添加分类失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新分类
   */
  const updateCategory = async (categoryId: number, data: ProductCategoryDTO) => {
    try {
      loading.value = true
      const response = await productApi.updateCategory(categoryId, data)
      if (response.code === 200) {
        // 更新成功后刷新分类列表
        await fetchCategories()
        // 如果更新的是当前分类，刷新详情
        if (currentCategory.value?.id === categoryId) {
          await fetchCategoryDetail(categoryId)
        }
      }
      return response
    } catch (error) {
      console.error('更新分类失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除分类
   */
  const deleteCategory = async (categoryId: number) => {
    try {
      loading.value = true
      const response = await productApi.deleteCategory(categoryId)
      if (response.code === 200) {
        // 从本地列表中移除
        categories.value = removeCategoryFromTree(categories.value, categoryId)
        
        // 如果删除的是当前分类，清空当前分类数据
        if (currentCategory.value?.id === categoryId) {
          currentCategory.value = null
        }
      }
      return response
    } catch (error) {
      console.error('删除分类失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新分类状态
   */
  const updateCategoryStatus = async (categoryId: number, status: number) => {
    try {
      loading.value = true
      const response = await productApi.updateCategoryStatus(categoryId, status)
      if (response.code === 200) {
        // 更新分类列表中的状态
        updateCategoryStatusInTree(categories.value, categoryId, status)
        // 更新当前分类的状态
        if (currentCategory.value?.id === categoryId) {
          currentCategory.value.status = status
          currentCategory.value.statusDesc = status === 1 ? '启用' : '禁用'
        }
      }
      return response
    } catch (error) {
      console.error('更新分类状态失败:', error)
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
   * 清空当前分类数据
   */
  const clearCurrentCategory = () => {
    currentCategory.value = null
  }

  /**
   * 重置状态
   */
  const resetState = () => {
    productList.value = []
    currentProduct.value = null
    categories.value = []
    currentCategory.value = null
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
      createTimeFormatted: product.createTime ? new Date(product.createTime).toLocaleDateString() : '-'
    }))
  }

  // 私有辅助方法
  const removeCategoryFromTree = (tree: ProductCategoryDTO[], categoryId: number): ProductCategoryDTO[] => {
    return tree
      .filter(category => category.id !== categoryId)
      .map(category => ({
        ...category,
        children: category.children ? removeCategoryFromTree(category.children, categoryId) : []
      }))
  }

  const updateCategoryStatusInTree = (tree: ProductCategoryDTO[], categoryId: number, status: number) => {
    tree.forEach(category => {
      if (category.id === categoryId) {
        category.status = status
        category.statusDesc = status === 1 ? '启用' : '禁用'
      }
      if (category.children) {
        updateCategoryStatusInTree(category.children, categoryId, status)
      }
    })
  }

  return {
    // 状态
    productList,
    currentProduct,
    categories,
    currentCategory,
    total,
    loading,
    pageInfo,
    
    // Actions
    fetchProductList,
    fetchProductDetail,
    addProduct,
    updateProduct,
    deleteProduct,
    updateProductStatus,
    updateProductStock,
    fetchCategories,
    fetchCategoryDetail,
    addCategory,
    updateCategory,
    deleteCategory,
    updateCategoryStatus,
    setPageInfo,
    clearCurrentProduct,
    clearCurrentCategory,
    resetState,
    
    // Getters
    hasNextPage,
    hasPrevPage,
    formattedProductList
  }
})