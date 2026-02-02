import { defineStore } from 'pinia'
import { ref } from 'vue'
import { orderApi } from '@/api/order'
import type {
  OrderQueryParams,
  OrderBasicDTO,
  OrderStatusDTO,
  OrderListVO,
  OrderDetailVO,
  PageResultVO
} from '@/types/order'

export const useOrderStore = defineStore('order', () => {
  // 状态
  const orderList = ref<OrderListVO[]>([])
  const currentOrder = ref<OrderDetailVO | null>(null)
  const total = ref(0)
  const loading = ref(false)
  const pageInfo = ref({
    pageNum: 1,
    pageSize: 10,
    totalPages: 0
  })

  // Actions

  /**
   * 分页查询订单列表
   */
  const fetchOrderList = async (params: OrderQueryParams = {}) => {
    try {
      loading.value = true
      const queryParams = {
        pageNum: params.pageNum || pageInfo.value.pageNum,
        pageSize: params.pageSize || pageInfo.value.pageSize,
        ...params
      }

      const response = await orderApi.getOrderList(queryParams)
      if (response.code === 200) {
        orderList.value = response.data.list
        total.value = response.data.total
        pageInfo.value = {
          pageNum: response.data.pageNum,
          pageSize: response.data.pageSize,
          totalPages: response.data.pages || Math.ceil(response.data.total / response.data.pageSize)
        }
      }
      return response.data
    } catch (error) {
      console.error('获取订单列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取订单详情
   */
  const fetchOrderDetail = async (orderId: number) => {
    try {
      loading.value = true
      const response = await orderApi.getOrderDetail(orderId)
      if (response.code === 200) {
        currentOrder.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取订单详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建订单
   */
  const createOrder = async (data: OrderBasicDTO) => {
    try {
      loading.value = true
      const response = await orderApi.createOrder(data)
      if (response.code === 200) {
        // 创建成功后重新加载列表
        await fetchOrderList({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
      }
      return response
    } catch (error) {
      console.error('创建订单失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新订单信息
   */
  const updateOrder = async (orderId: number, data: OrderBasicDTO) => {
    try {
      loading.value = true
      const response = await orderApi.updateOrder(orderId, data)
      if (response.code === 200) {
        // 更新成功后刷新当前订单详情
        if (currentOrder.value?.id === orderId) {
          await fetchOrderDetail(orderId)
        }
        // 刷新列表
        await fetchOrderList({
          pageNum: pageInfo.value.pageNum,
          pageSize: pageInfo.value.pageSize
        })
      }
      return response
    } catch (error) {
      console.error('更新订单失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新订单状态
   */
  const updateOrderStatus = async (orderId: number, data: OrderStatusDTO) => {
    try {
      loading.value = true
      const response = await orderApi.updateOrderStatus(orderId, data)
      if (response.code === 200) {
        // 更新列表中的状态
        const index = orderList.value.findIndex(item => item.id === orderId)
        if (index !== -1) {
          orderList.value[index].orderStatus = data.orderStatus
          orderList.value[index].orderStatusDesc = getOrderStatusDesc(data.orderStatus)
        }
        // 更新当前订单的状态
        if (currentOrder.value?.id === orderId) {
          currentOrder.value.orderStatus = data.orderStatus
          currentOrder.value.orderStatusDesc = getOrderStatusDesc(data.orderStatus)
        }
      }
      return response
    } catch (error) {
      console.error('更新订单状态失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 取消订单
   */
  const cancelOrder = async (orderId: number, reason?: string) => {
    try {
      loading.value = true
      const response = await orderApi.cancelOrder(orderId, reason)
      if (response.code === 200) {
        // 更新状态
        await updateOrderStatus(orderId, { orderStatus: 'CANCELLED', remark: reason })
      }
      return response
    } catch (error) {
      console.error('取消订单失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 完成订单
   */
  const completeOrder = async (orderId: number) => {
    try {
      loading.value = true
      const response = await orderApi.completeOrder(orderId)
      if (response.code === 200) {
        // 更新状态
        await updateOrderStatus(orderId, { orderStatus: 'COMPLETED' })
      }
      return response
    } catch (error) {
      console.error('完成订单失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除订单
   */
  const deleteOrder = async (orderId: number) => {
    try {
      loading.value = true
      const response = await orderApi.deleteOrder(orderId)
      if (response.code === 200) {
        // 从本地列表中移除
        orderList.value = orderList.value.filter(item => item.id !== orderId)
        total.value -= 1

        // 如果删除的是当前查看的订单，清空当前订单数据
        if (currentOrder.value?.id === orderId) {
          currentOrder.value = null
        }
      }
      return response
    } catch (error) {
      console.error('删除订单失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 批量删除订单
   */
  const batchDeleteOrder = async (ids: number[]) => {
    try {
      loading.value = true
      const response = await orderApi.batchDeleteOrder(ids)
      if (response.code === 200) {
        // 从本地列表中移除
        orderList.value = orderList.value.filter(item => !ids.includes(item.id))
        total.value -= ids.length

        // 如果删除的包含当前查看的订单，清空当前订单数据
        if (currentOrder.value && ids.includes(currentOrder.value.id)) {
          currentOrder.value = null
        }
      }
      return response
    } catch (error) {
      console.error('批量删除订单失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 订单支付
   */
  const payOrder = async (orderId: number, paymentMethod?: string) => {
    try {
      loading.value = true
      const response = await orderApi.payOrder(orderId, paymentMethod)
      if (response.code === 200) {
        // 支付成功后更新状态
        const index = orderList.value.findIndex(item => item.id === orderId)
        if (index !== -1) {
          orderList.value[index].paymentStatus = 1
          orderList.value[index].paymentStatusDesc = '已支付'
          orderList.value[index].orderStatus = 'PROCESSING'
          orderList.value[index].orderStatusDesc = '处理中'
        }
      }
      return response
    } catch (error) {
      console.error('订单支付失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 订单退款
   */
  const refundOrder = async (orderId: number, refundAmount: number, reason?: string) => {
    try {
      loading.value = true
      const response = await orderApi.refundOrder(orderId, refundAmount, reason)
      if (response.code === 200) {
        // 退款成功后更新状态
        await updateOrderStatus(orderId, { orderStatus: 'REFUNDED', remark: reason })
      }
      return response
    } catch (error) {
      console.error('订单退款失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取会员订单列表
   */
  const fetchMemberOrders = async (memberId: number, params: OrderQueryParams = {}) => {
    try {
      loading.value = true
      const queryParams = {
        pageNum: params.pageNum || pageInfo.value.pageNum,
        pageSize: params.pageSize || pageInfo.value.pageSize,
        ...params
      }

      const response = await orderApi.getMemberOrders(memberId, queryParams)
      if (response.code === 200) {
        orderList.value = response.data.list
        total.value = response.data.total
        pageInfo.value = {
          pageNum: response.data.pageNum,
          pageSize: response.data.pageSize,
          totalPages: response.data.pages || Math.ceil(response.data.total / response.data.pageSize)
        }
      }
      return response.data
    } catch (error) {
      console.error('获取会员订单列表失败:', error)
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
   * 清空当前订单数据
   */
  const clearCurrentOrder = () => {
    currentOrder.value = null
  }

  /**
   * 重置状态
   */
  const resetState = () => {
    orderList.value = []
    currentOrder.value = null
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

  /**
   * 格式化订单列表
   */
  const formattedOrderList = () => {
    return orderList.value.map(order => ({
      ...order,
      totalAmountFormatted: order.totalAmount ? `¥${order.totalAmount.toFixed(2)}` : '-',
      actualAmountFormatted: order.actualAmount ? `¥${order.actualAmount.toFixed(2)}` : '-',
      createTimeFormatted: order.createTime ? new Date(order.createTime).toLocaleString() : '-',
      paymentTimeFormatted: order.paymentTime ? new Date(order.paymentTime).toLocaleString() : '-',
      orderTypeDesc: getOrderTypeDesc(order.orderType),
      paymentStatusDesc: getPaymentStatusDesc(order.paymentStatus),
      orderStatusDesc: getOrderStatusDesc(order.orderStatus)
    }))
  }

  // 辅助函数
  const getOrderTypeDesc = (type?: number) => {
    if (type === undefined) return '未知'
    switch (type) {
      case 0: return '会籍卡'
      case 1: return '私教课'
      case 2: return '团课'
      case 3: return '相关产品'
      default: return '未知'
    }
  }

  const getPaymentStatusDesc = (status: number) => {
    switch (status) {
      case 0: return '待支付'
      case 1: return '已支付'
      default: return '未知'
    }
  }

  const getOrderStatusDesc = (status: string) => {
    switch (status) {
      case 'PENDING': return '待处理'
      case 'PROCESSING': return '处理中'
      case 'COMPLETED': return '已完成'
      case 'CANCELLED': return '已取消'
      case 'REFUNDED': return '已退款'
      case 'DELETED': return '已删除'
      default: return '未知'
    }
  }

  return {
    // 状态
    orderList,
    currentOrder,
    total,
    loading,
    pageInfo,

    // Actions
    fetchOrderList,
    fetchOrderDetail,
    createOrder,
    updateOrder,
    updateOrderStatus,
    cancelOrder,
    completeOrder,
    deleteOrder,
    batchDeleteOrder,
    payOrder,
    refundOrder,
    fetchMemberOrders,
    setPageInfo,
    clearCurrentOrder,
    resetState,

    // Getters
    hasNextPage,
    hasPrevPage,
    formattedOrderList
  }
})