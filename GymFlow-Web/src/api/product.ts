import request from '@/utils/request'
import type { 
  ProductQueryDTO, 
  ProductBasicDTO, 
  ProductFullDTO,
  ProductListVO,
  PageResultVO,
  ApiResponse
} from '@/types/product'

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
  }
}