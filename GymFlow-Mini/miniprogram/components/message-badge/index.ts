// 消息徽标组件逻辑
Component({
  properties: {
    count: {
      type: Number,
      value: 0
    },
    max: {
      type: Number,
      value: 99
    },
    dot: {
      type: Boolean,
      value: false
    },
    showZero: {
      type: Boolean,
      value: false
    }
  },

  data: {
    displayCount: 0
  },

  observers: {
    'count, max, showZero': function(count, max, showZero) {
      if (count === 0 && !showZero) {
        this.setData({ displayCount: 0 })
        return
      }
      
      if (count > max) {
        this.setData({ displayCount: max })
      } else {
        this.setData({ displayCount: count })
      }
    }
  }
})