// 日期选择器组件逻辑
Component({
  properties: {
    value: {
      type: String,
      value: ''
    },
    start: {
      type: String,
      value: '2020-01-01'
    },
    end: {
      type: String,
      value: '2030-12-31'
    },
    fields: {
      type: String,
      value: 'day' // year | month | day
    },
    placeholder: {
      type: String,
      value: '选择日期'
    },
    disabled: {
      type: Boolean,
      value: false
    }
  },

  data: {
    showPicker: false,
    pickerValue: [] as string[]
  },

  methods: {
    onTap() {
      if (this.properties.disabled) return
      
      this.setData({ 
        showPicker: true,
        pickerValue: this.properties.value ? [this.properties.value] : []
      })
    },

    onPickerChange(e: WechatMiniprogram.PickerChangeEvent) {
      const { value } = e.detail
      this.setData({ pickerValue: value })
      this.triggerEvent('change', { value: value[0] })
    },

    onPickerCancel() {
      this.setData({ showPicker: false })
    },

    onPickerConfirm() {
      const { pickerValue } = this.data
      this.setData({ 
        showPicker: false,
        value: pickerValue[0] || ''
      })
      this.triggerEvent('confirm', { value: pickerValue[0] || '' })
    }
  }
})