// 订单卡片组件逻辑
Component({
  properties: {
    order: {
      type: Object,
      value: {}
    },
    showActions: {
      type: Boolean,
      value: true
    }
  },

  methods: {
    onTap() {
      this.triggerEvent('tap', { order: this.properties.order })
    },

    onPayTap() {
      this.triggerEvent('pay', { order: this.properties.order })
    },

    onCancelTap() {
      this.triggerEvent('cancel', { order: this.properties.order })
    },

    getPaymentStatusText() {
      return this.properties.order.paymentStatus === 1 ? '已支付' : '待支付'
    },

    getOrderStatusClass() {
      const status = this.properties.order.orderStatus
      const classMap: Record<string, string> = {
        'PENDING': 'status-pending',
        'PROCESSING': 'status-info',
        'COMPLETED': 'status-success',
        'CANCELLED': 'status-warning',
        'REFUNDED': 'status-warning'
      }
      return classMap[status] || ''
    },

    getOrderStatusText() {
      return this.properties.order.orderStatusDesc
    }
  }
})