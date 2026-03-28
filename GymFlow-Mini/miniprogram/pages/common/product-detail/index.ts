// 商品详情页面
import { getProductDetail } from '../../../services/api/product.api'
import { createOrder } from '../../../services/api/order.api'
import { userStore } from '../../../stores/user.store'
import{ orderStore} from '../../../stores/order.store'
import { showToast, showLoading, hideLoading, showModal } from '../../../utils/wx-util'

Page({
  data: {
    // 商品ID
    productId: 0,

    // 操作类型（buy-购买 renew-续费）
    action: 'buy',

    // 商品信息
    product: {
      id: 0,
      productName: '',
      productType: 0,
      productTypeDesc: '',
      images: [] as string[],
      originalPrice: 0,
      currentPrice: 0,
      stockQuantity: 0,
      salesVolume: 0,
      unit: '',
      validityDays: 0,
      description: '',
      detail: null
    },

    // 加载状态
    loading: true
  },

  onLoad(options: any) {
    const { id, action } = options

    if (!id) {
      showToast('参数错误', 'none')
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
      return
    }

    this.setData({
      productId: parseInt(id),
      action: action || 'buy'
    })

    this.loadProductDetail()
  },

  /**
   * 加载商品详情
   */
  async loadProductDetail() {
    this.setData({ loading: true })

    try {
      const { productId } = this.data
      const detail = await getProductDetail(productId)

      this.setData({
        product: detail,
        loading: false
      })

    } catch (error: any) {
      console.error('加载商品详情失败:', error)
      this.setData({ loading: false })
      showToast(error.message || '加载失败', 'none')
    }
  },

  /**
   * 立即购买
   */
  async onBuyNow() {
    const { product, action } = this.data

    // 检查登录状态
    if (!userStore.isLogin) {
      const confirm = await showModal({
        title: '提示',
        content: '请先登录后再购买'
      })

      if (confirm) {
        wx.navigateTo({
          url: '/pages/common/login/index'
        })
      }
      return
    }

    // 检查库存
    if (product.productType === 3 && product.stockQuantity <= 0) {
      showToast('库存不足', 'none')
      return
    }

    // if (action === 'renew') {
    //   // 续费操作
    //   this.renewProduct()
    // } else {
    // 购买操作
    this.buyProduct()
    // }
  },

  /**
 * 购买商品
 */
  async buyProduct() {
    const { product } = this.data
    const memberId = userStore.memberId

    if (!memberId) {
      showToast('用户信息错误', 'none')
      return
    }

    showLoading('创建订单中...')

    try {
      // 创建订单
      const result = await createOrder({
        memberId,
        orderType: product.productType,
        orderItems: [{
          productId: product.id,
          productName: product.productName,
          productType: product.productType,
          quantity: 1,
          unitPrice: product.currentPrice,
          totalSessions: product.totalSessions,
          validityDays: product.validityDays
        }],
        remark: ''
      })

      hideLoading()
      const orderId = result
      console.log(orderId, '商品详情的订单id')
      // 弹出选择框
      const confirm = await showModal({
        title: '提示',
        content: `订单创建成功，需支付 ¥${product.currentPrice}`,
        confirmText: '立即支付',
        cancelText: '稍后支付'
      })

      if (confirm) {
        // 立即支付 - 调用 store 中的 payOrder 方法
        showLoading('支付中...')
        console.log(orderId, "我是订单ID")
        try {
          await orderStore.payOrder(orderId)
          hideLoading()
          showToast('支付成功', 'success')

          // 跳转到订单详情
          setTimeout(() => {
            wx.navigateTo({
              url: `/pages/common/order-detail/index?id=${orderId}`
            })
          }, 1500)
        } catch (payError: any) {
          hideLoading()
          showToast(payError.message || '支付失败', 'none')
        }
      } else {
        // 稍后支付 - 跳转到订单详情
        wx.navigateTo({
          url: `/pages/common/order-detail/index?id=${orderId}`
        })
      }

    } catch (error: any) {
      hideLoading()
      showToast(error.message || '创建订单失败', 'none')
    }
  },

  /**
   * 续费商品
   */
  // async renewProduct() {
  //   const { product } = this.data

  //   showToast('续费功能开发中', 'none')
  // },

  /**
   * 获取商品标签样式类
   */
  getProductTagClass(type: number): string {
    const map: Record<number, string> = {
      0: 'product-detail-tag-0',
      1: 'product-detail-tag-1',
      2: 'product-detail-tag-2',
      3: 'product-detail-tag-3'
    }
    return map[type] || 'product-detail-tag-0'
  }
})