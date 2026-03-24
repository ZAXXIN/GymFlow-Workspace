// components/booking-card/index.ts

Component({
  properties: {
    booking: {
      type: Object,
      value: {},
    },
    showActions: {
      type: Boolean,
      value: true
    }
  },

  methods: {
    onTap() {
      // 触发自定义事件，传递 booking 数据
      this.triggerEvent('cardtap', { booking: this.properties.booking })
    },

    onCancelTap() {
      // 阻止冒泡，防止触发卡片的 tap 事件
      this.triggerEvent('cancel', { booking: this.properties.booking })
    },

    onCheckinTap() {
      // 阻止冒泡，防止触发卡片的 tap 事件
      this.triggerEvent('checkin', { 
        booking: this.properties.booking 
      })
    },

    getStatusClass() {
      const status = this.properties.booking.bookingStatus
      const classMap: Record<number, string> = {
        0: 'status-pending',
        1: 'status-success',
        2: 'status-completed',
        3: 'status-warning'
      }
      return classMap[status] || ''
    },

    getStatusText() {
      const status = this.properties.booking.bookingStatus
      const textMap: Record<number, string> = {
        0: '待上课',
        1: '已签到',
        2: '已完成',
        3: '已取消',
        4: '已过期'
      }
      return textMap[status] || '未知'
    }
  }
})