// components/qr-code/index.ts
import drawQrcode from '../../utils/qrcode';

Component({
  properties: {
    text: {
      type: String,
      value: '',
      observer(newVal) {
        if (newVal) {
          // 延迟绘制，确保 canvas 已创建
          setTimeout(() => this.draw(), 100)
        }
      }
    },
    size: {
      type: Number,
      value: 400
    },
    color: {
      type: String,
      value: '#000000'
    },
    backgroundColor: {
      type: String,
      value: '#ffffff'
    }
  },

  data: {
    canvasId: ''
  },

  lifetimes: {
    attached() {
      // 生成唯一 canvasId
      const id = 'qrcode-' + Date.now() + '-' + Math.random().toString(36).substr(2, 6)
      this.setData({ canvasId: id })
      
      if (this.properties.text) {
        setTimeout(() => this.draw(), 100)
      }
    }
  },

  methods: {
    draw() {
      const { text, size, color, backgroundColor, canvasId } = this.properties
      const canvasIdValue = this.data.canvasId || canvasId
      
      if (!text) {
        console.warn('二维码内容为空')
        return
      }
      
      // 使用 wx.createCanvasContext 旧版 API（兼容性更好）
      const ctx = wx.createCanvasContext(canvasIdValue, this)
      
      drawQrcode({
        ctx,
        canvasId: canvasIdValue,
        text,
        width: size,
        height: size,
        colorDark: color,
        colorLight: backgroundColor,
        correctLevel: 1
      })
      
      // 延迟绘制完成
      setTimeout(() => {
        ctx.draw()
      }, 50)
    }
  }
})