// 预约卡片组件逻辑
Component({
  properties: {
    booking: {
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
      this.triggerEvent('tap', { booking: this.properties.booking })
    },

    onCancelTap() {
      this.triggerEvent('cancel', { booking: this.properties.booking })
    },

    onCheckinTap() {
      this.triggerEvent('checkin', { booking: this.properties.booking })
    },

    getStatusClass() {
      const status = this.properties.booking.bookingStatus
      const classMap: Record<number, string> = {
        0: 'status-pending',  // 待上课
        1: 'status-success',   // 已签到
        2: 'status-completed', // 已完成
        3: 'status-warning'    // 已取消
      }
      return classMap[status] || ''
    },

    getStatusText() {
      const status = this.properties.booking.bookingStatus
      const textMap: Record<number, string> = {
        0: '待上课',
        1: '已签到',
        2: '已完成',
        3: '已取消'
      }
      return textMap[status] || '未知'
    },

    formatTime(time: string) {
      if (!time) return ''
      return time.substring(11, 16) // HH:MM
    }
  }
})