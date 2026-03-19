// 二维码组件逻辑
import drawQrcode from '../../utils/qrcode' // 需要引入二维码生成库

Component({
  properties: {
    text: {
      type: String,
      value: '',
      observer: 'draw'
    },
    size: {
      type: Number,
      value: 200
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
    canvasId: 'qrcode-' + Math.random().toString(36).substr(2, 9)
  },

  lifetimes: {
    attached() {
      this.draw()
    }
  },

  methods: {
    draw() {
      const { text, size, color, backgroundColor, canvasId } = this.properties
      
      if (!text) return
      
      const query = this.createSelectorQuery()
      query.select('#' + canvasId).fields({ node: true, size: true })
      
      query.exec((res) => {
        const canvas = res[0]?.node
        if (!canvas) return
        
        // 使用二维码库绘制
        drawQrcode({
          canvas,
          text,
          width: size,
          height: size,
          colorDark: color,
          colorLight: backgroundColor,
          correctLevel: 1 // 纠错级别
        })
      })
    }
  }
})