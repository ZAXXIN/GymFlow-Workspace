import request from '@/utils/request'
import type { 
  ProductQueryDTO, 
  ProductBasicDTO, 
  ProductCategoryDTO,
  ProductDetailDTO,
  ProductFullDTO,
  ProductListVO,
  PageResultVO,
  ApiResponse
} from '@/types/product'

// 商品管理API
export const productApi = {
  /**
   * 分页查询商品列表
   */
  getProductList(params: ProductQueryDTO): Promise<ApiResponse<PageResultVO<ProductListVO>>> {
    return request({
      url: '/product/list',
      method: 'POST',
      data: params
    })
  },
  
  /**
   * 根据商品类型获取商品列表（用于会员卡选择）
   * @param productType 商品类型：0-会籍卡，1-私教课，2-团课
   */
  getProductsByType(productType: number): Promise<ApiResponse<ProductListVO[]>> {
    return request({
      url: '/product/list-by-type',
      method: 'GET',
      params: { productType }
    })
  },

  /**
   * 获取商品详情
   */
  getProductDetail(productId: number): Promise<ApiResponse<ProductFullDTO>> {
    return request({
      url: `/product/detail/${productId}`,
      method: 'GET'
    })
  },

  /**
   * 添加商品
   */
  addProduct(data: ProductBasicDTO): Promise<ApiResponse<number>> {
    return request({
      url: '/product/add',
      method: 'POST',
      data
    })
  },

  /**
   * 更新商品信息
   */
  updateProduct(productId: number, data: ProductBasicDTO): Promise<ApiResponse> {
    return request({
      url: `/product/update/${productId}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 删除商品
   */
  deleteProduct(productId: number): Promise<ApiResponse> {
    return request({
      url: `/product/delete/${productId}`,
      method: 'DELETE'
    })
  },

  /**
   * 更新商品状态
   */
  updateProductStatus(productId: number, status: number): Promise<ApiResponse> {
    return request({
      url: `/product/updateStatus/${productId}`,
      method: 'PUT',
      params: { status }
    })
  },

  /**
   * 更新商品库存
   */
  updateProductStock(productId: number, quantity: number): Promise<ApiResponse> {
    return request({
      url: `/product/updateStock/${productId}`,
      method: 'PUT',
      params: { quantity }
    })
  },

  /**
   * 获取所有商品分类
   */
  getAllCategories(): Promise<ApiResponse<ProductCategoryDTO[]>> {
    return request({
      url: '/product/categories',
      method: 'GET'
    })
  },

  /**
   * 获取分类详情
   */
  getCategoryDetail(categoryId: number): Promise<ApiResponse<ProductCategoryDTO>> {
    return request({
      url: `/product/category/detail/${categoryId}`,
      method: 'GET'
    })
  },

  /**
   * 添加分类
   */
  addCategory(data: ProductCategoryDTO): Promise<ApiResponse<number>> {
    return request({
      url: '/product/category/add',
      method: 'POST',
      data
    })
  },

  /**
   * 更新分类
   */
  updateCategory(categoryId: number, data: ProductCategoryDTO): Promise<ApiResponse> {
    return request({
      url: `/product/category/update/${categoryId}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 删除分类
   */
  deleteCategory(categoryId: number): Promise<ApiResponse> {
    return request({
      url: `/product/category/delete/${categoryId}`,
      method: 'DELETE'
    })
  },

  /**
   * 更新分类状态
   */
  updateCategoryStatus(categoryId: number, status: number): Promise<ApiResponse> {
    return request({
      url: `/product/category/updateStatus/${categoryId}`,
      method: 'PUT',
      params: { status }
    })
  }
}