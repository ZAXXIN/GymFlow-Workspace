import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Order, QueryParams, PaginatedResponse } from '@/types'
import { orderApi } from '@/api/order'

export const useOrderStore = defineStore('order', () => {
  // 状态
  const orders = ref<Order[]>([])
  const currentOrder = ref<Order | null>(null)
  const total = ref(0)
  const loading = ref(false)

  // Actions
  const fetchOrders = async (params: QueryParams = {}) => {
    try {
      loading.value = true
      const response: PaginatedResponse<Order> = await orderApi.getOrders(params)
      orders.value = response.items
      total.value = response.total
      return response
    } catch (error) {
      console.error('Fetch orders failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchOrderById = async (id: number) => {
    try {
      loading.value = true
      const response = await orderApi.getOrderById(id)
      currentOrder.value = response
      return response
    } catch (error) {
      console.error('Fetch order by id failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const createOrder = async (orderData: Partial<Order>) => {
    try {
      loading.value = true
      const response = await orderApi.createOrder(orderData)
      return response
    } catch (error) {
      console.error('Create order failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const updateOrder = async (id: number, orderData: Partial<Order>) => {
    try {
      loading.value = true
      const response = await orderApi.updateOrder(id, orderData)
      // 更新本地数据
      const index = orders.value.findIndex(order => order.id === id)
      if (index !== -1) {
        orders.value[index] = { ...orders.value[index], ...response }
      }
      return response
    } catch (error) {
      console.error('Update order failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const deleteOrder = async (id: number) => {
    try {
      loading.value = true
      await orderApi.deleteOrder(id)
      // 从本地删除
      orders.value = orders.value.filter(order => order.id !== id)
      total.value -= 1
    } catch (error) {
      console.error('Delete order failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const getOrderStatistics = async (params: QueryParams = {}) => {
    try {
      loading.value = true
      const response = await orderApi.getStatistics(params)
      return response
    } catch (error) {
      console.error('Get order statistics failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const exportOrders = async (params: QueryParams = {}) => {
    try {
      loading.value = true
      const response = await orderApi.exportOrders(params)
      return response
    } catch (error) {
      console.error('Export orders failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const resetOrderState = () => {
    orders.value = []
    currentOrder.value = null
    total.value = 0
  }

  return {
    // 状态
    orders,
    currentOrder,
    total,
    loading,
    
    // Actions
    fetchOrders,
    fetchOrderById,
    createOrder,
    updateOrder,
    deleteOrder,
    getOrderStatistics,
    exportOrders,
    resetOrderState
  }
})