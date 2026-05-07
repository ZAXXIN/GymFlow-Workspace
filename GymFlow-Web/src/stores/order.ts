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

  const createOrder = async (data: OrderBasicDTO) => {
    try {
      loading.value = true
      const response = await orderApi.createOrder(data)
      if (response.code === 200) {
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

  const updateOrder = async (orderId: number, data: OrderBasicDTO) => {
    try {
      loading.value = true
      const response = await orderApi.updateOrder(orderId, data)
      if (response.code === 200) {
        if (currentOrder.value?.id === orderId) {
          await fetchOrderDetail(orderId)
        }
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

  const updateOrderStatus = async (orderId: number, data: OrderStatusDTO) => {
    try {
      loading.value = true
      const response = await orderApi.updateOrderStatus(orderId, data)
      if (response.code === 200) {
        const index = orderList.value.findIndex(item => item.id === orderId)
        if (index !== -1) {
          orderList.value[index].status = data.status
          orderList.value[index].statusDesc = getStatusDesc(data.status)
        }
        if (currentOrder.value?.id === orderId) {
          currentOrder.value.status = data.status
          currentOrder.value.statusDesc = getStatusDesc(data.status)
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

  const cancelOrder = async (orderId: number, reason?: string) => {
    try {
      loading.value = true
      const response = await orderApi.cancelOrder(orderId, reason)
      if (response.code === 200) {
        await updateOrderStatus(orderId, { status: 'CANCELLED', remark: reason })
      }
      return response
    } catch (error) {
      console.error('取消订单失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const completeOrder = async (orderId: number) => {
    try {
      loading.value = true
      const response = await orderApi.completeOrder(orderId)
      if (response.code === 200) {
        await updateOrderStatus(orderId, { status: 'COMPLETED' })
      }
      return response
    } catch (error) {
      console.error('完成订单失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const deleteOrder = async (orderId: number) => {
    try {
      loading.value = true
      const response = await orderApi.deleteOrder(orderId)
      if (response.code === 200) {
        orderList.value = orderList.value.filter(item => item.id !== orderId)
        total.value -= 1
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

  const payOrder = async (orderId: number, paymentMethod?: string) => {
    try {
      loading.value = true
      const response = await orderApi.payOrder(orderId, paymentMethod)
      if (response.code === 200) {
        const activated = response.data === true
        if (activated) {
          // 支付成功且激活成功，后端已更新为 COMPLETED，仅需刷新数据
          await fetchOrderDetail(orderId)
          await fetchOrderList({ pageNum: pageInfo.value.pageNum, pageSize: pageInfo.value.pageSize })
          // 提示信息已在 List.vue 中处理，此处不再重复
        } else {
          // 支付成功但激活失败，后端状态为 PAID
          await fetchOrderDetail(orderId)
          await fetchOrderList({ pageNum: pageInfo.value.pageNum, pageSize: pageInfo.value.pageSize })
        }
      }
      return response
    } catch (error) {
      // console.error('订单支付失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const retryActivateOrder = async (orderId: number) => {
    try {
      loading.value = true
      const response = await orderApi.retryActivateOrder(orderId)
      if (response.code === 200 && response.data) {
        await fetchOrderDetail(orderId)
        ElMessage.success('激活成功')
      } else {
        ElMessage.error(response.message || '激活失败')
      }
      return response
    } catch (error) {
      console.error('重试激活失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

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

  const setPageInfo = (pageNum: number, pageSize: number) => {
    pageInfo.value.pageNum = pageNum
    pageInfo.value.pageSize = pageSize
  }

  const clearCurrentOrder = () => {
    currentOrder.value = null
  }

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

  const formattedOrderList = () => {
    return orderList.value.map(order => ({
      ...order,
      totalAmountFormatted: order.totalAmount ? `¥${order.totalAmount.toFixed(2)}` : '-',
      actualAmountFormatted: order.actualAmount ? `¥${order.actualAmount.toFixed(2)}` : '-',
      createTimeFormatted: order.createTime ? new Date(order.createTime).toLocaleString() : '-',
      paymentTimeFormatted: order.paymentTime ? new Date(order.paymentTime).toLocaleString() : '-',
      orderTypeDesc: getOrderTypeDesc(order.orderType),
      statusDesc: getStatusDesc(order.status)
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

  const getStatusDesc = (status: string) => {
    switch (status) {
      case 'WAIT_PAY': return '待支付'
      case 'PAID': return '已支付'
      case 'COMPLETED': return '已完成'
      case 'CANCELLED': return '已取消'
      case 'REFUNDED': return '已退款'
      default: return status
    }
  }

  return {
    orderList,
    currentOrder,
    total,
    loading,
    pageInfo,
    fetchOrderList,
    fetchOrderDetail,
    createOrder,
    updateOrder,
    updateOrderStatus,
    cancelOrder,
    completeOrder,
    deleteOrder,
    payOrder,
    retryActivateOrder,
    fetchMemberOrders,
    setPageInfo,
    clearCurrentOrder,
    resetState,
    hasNextPage,
    hasPrevPage,
    formattedOrderList
  }
})

// 需要引入 ElMessage（由于使用了 ElMessage.success/error）
import { ElMessage } from 'element-plus'