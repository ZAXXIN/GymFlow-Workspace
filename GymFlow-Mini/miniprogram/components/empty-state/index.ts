// 空状态组件逻辑
Component({
  properties: {
    show: {
      type: Boolean,
      value: false
    },
    text: {
      type: String,
      value: '暂无数据'
    },
    image: {
      type: String,
      value: ''
    },
    buttonText: {
      type: String,
      value: ''
    }
  },

  methods: {
    onButtonTap() {
      this.triggerEvent('buttonTap')
    }
  }
})