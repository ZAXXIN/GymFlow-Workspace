// 统计卡片组件逻辑
Component({
  properties: {
    title: {
      type: String,
      value: ''
    },
    value: {
      type: [String, Number],
      value: ''
    },
    unit: {
      type: String,
      value: ''
    },
    icon: {
      type: String,
      value: ''
    },
    trend: {
      type: Number,
      value: 0
    },
    trendText: {
      type: String,
      value: ''
    },
    color: {
      type: String,
      value: '#07c160'
    }
  },

  methods: {
    onTap() {
      this.triggerEvent('tap')
    }
  }
})