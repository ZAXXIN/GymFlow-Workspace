// 加载中组件逻辑
Component({
  properties: {
    show: {
      type: Boolean,
      value: false
    },
    text: {
      type: String,
      value: '加载中...'
    },
    type: {
      type: String,
      value: 'spinner' // spinner | circular
    },
    fullscreen: {
      type: Boolean,
      value: false
    }
  },

  data: {
    isShow: false
  },

  observers: {
    'show': function(show) {
      this.setData({ isShow: show })
    }
  },

  methods: {
    // 防止点击穿透
    onTap() {
      return false
    }
  }
})