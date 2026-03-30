// components/qr-code/index.ts
import QRCode from 'wxqrcode'

Component({
  properties: {
    text: {
      type: String,
      value: '',
      observer(newVal) {
        if (newVal && this.data.canvasReady) {
          setTimeout(() => this.draw(), 200)
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
    canvasId: '',
    canvasReady: false
  },

  lifetimes: {
    attached() {
      const id = 'qrcode-' + Date.now() + '-' + Math.random().toString(36).substr(2, 6)
      this.setData({ canvasId: id })

      setTimeout(() => {
        this.createSelectorQuery()
          .select(`#${id}`)
          .node()
          .exec((res) => {
            if (res && res[0] && res[0].node) {
              this.canvasNode = res[0].node
              this.setData({ canvasReady: true })
              if (this.properties.text) {
                setTimeout(() => this.draw(), 200)
              }
            }
          })
      }, 100)
    }
  },

  methods: {
    draw() {
      const { text, size, color, backgroundColor } = this.properties
      
      if (!text || !this.canvasNode) return
      
      try {
        const canvas = this.canvasNode
        const ctx = canvas.getContext('2d')
        
        // 设置 canvas 尺寸
        const dpr = wx.getSystemInfoSync().pixelRatio
        canvas.width = size * dpr
        canvas.height = size * dpr
        ctx.scale(dpr, dpr)
        
        // 使用 wxqrcode 绘制二维码
        QRCode.draw({
          ctx: ctx,
          text: text,
          width: size,
          height: size,
          colorDark: color,
          colorLight: backgroundColor,
          correctLevel: 1  // 纠错级别
        })
        
        console.log('二维码绘制成功')
      } catch (error) {
        console.error('绘制二维码失败:', error)
      }
    }
  }
})