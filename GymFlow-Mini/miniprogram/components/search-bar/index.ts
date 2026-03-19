// 搜索栏组件逻辑
Component({
  properties: {
    placeholder: {
      type: String,
      value: '搜索商品、课程...'
    },
    value: {
      type: String,
      value: ''
    },
    showCancel: {
      type: Boolean,
      value: true
    },
    focus: {
      type: Boolean,
      value: false
    },
    disabled: {
      type: Boolean,
      value: false
    }
  },

  methods: {
    onInput(e: WechatMiniprogram.Input) {
      const { value } = e.detail
      this.triggerEvent('input', { value })
    },

    onConfirm(e: WechatMiniprogram.Input) {
      const { value } = e.detail
      this.triggerEvent('search', { value })
    },

    onFocus() {
      this.triggerEvent('focus')
    },

    onBlur() {
      this.triggerEvent('blur')
    },

    onClear() {
      this.setData({ value: '' })
      this.triggerEvent('clear')
      this.triggerEvent('input', { value: '' })
    },

    onCancel() {
      this.triggerEvent('cancel')
    },

    onTap() {
      if (this.properties.disabled) return
      this.triggerEvent('tap')
    }
  }
})