// 图表卡片组件逻辑
Component({
  properties: {
    title: {
      type: String,
      value: ''
    },
    data: {
      type: Array,
      value: []
    },
    categories: {
      type: Array,
      value: []
    },
    type: {
      type: String,
      value: 'line' // line | bar
    },
    height: {
      type: Number,
      value: 300
    },
    showLegend: {
      type: Boolean,
      value: true
    },
    showGrid: {
      type: Boolean,
      value: true
    }
  },

  data: {
    chartId: 'chart-' + Math.random().toString(36).substr(2, 9),
    chart: null as any
  },

  lifetimes: {
    attached() {
      // 延迟绘制，等待canvas创建
      setTimeout(() => this.drawChart(), 100)
    }
  },

  methods: {
    drawChart() {
      // 这里需要使用图表库，如 echarts-for-weixin
      // 示例代码，实际需要根据使用的库来调整
      const query = this.createSelectorQuery()
      query.select('#' + this.data.chartId).fields({ node: true, size: true })
      
      query.exec((res) => {
        const canvas = res[0]?.node
        if (!canvas) return
        
        // 假设使用某个图表库
        const ctx = canvas.getContext('2d')
        
        // 绘制图表逻辑...
        this.drawLineChart(ctx)
      })
    },

    drawLineChart(ctx: any) {
      const { data, categories, height, width = 300 } = this.properties
      
      // 简化的图表绘制示例
      ctx.clearRect(0, 0, width, height)
      
      // 绘制网格
      if (this.properties.showGrid) {
        ctx.strokeStyle = '#eee'
        ctx.lineWidth = 1
        
        for (let i = 0; i <= 5; i++) {
          const y = height - 40 - i * (height - 80) / 5
          ctx.beginPath()
          ctx.moveTo(40, y)
          ctx.lineTo(width - 20, y)
          ctx.stroke()
        }
      }
      
      // 绘制数据点
      if (data && data.length > 0) {
        ctx.strokeStyle = '#07c160'
        ctx.lineWidth = 2
        ctx.beginPath()
        
        const points = data.map((value, index) => {
          const x = 40 + index * (width - 60) / (data.length - 1)
          const y = height - 40 - (value / Math.max(...data)) * (height - 80)
          return { x, y }
        })
        
        // 绘制折线
        points.forEach((point, index) => {
          if (index === 0) {
            ctx.moveTo(point.x, point.y)
          } else {
            ctx.lineTo(point.x, point.y)
          }
        })
        ctx.stroke()
        
        // 绘制点
        points.forEach(point => {
          ctx.fillStyle = '#07c160'
          ctx.beginPath()
          ctx.arc(point.x, point.y, 4, 0, 2 * Math.PI)
          ctx.fill()
        })
      }
      
      // 绘制分类标签
      if (categories && categories.length > 0) {
        ctx.fillStyle = '#999'
        ctx.font = '10px Arial'
        
        categories.forEach((category, index) => {
          const x = 40 + index * (width - 60) / (categories.length - 1) - 20
          const y = height - 20
          ctx.fillText(category, x, y)
        })
      }
      
      // 绘制Y轴标签
      ctx.fillStyle = '#999'
      ctx.font = '10px Arial'
      ctx.fillText('0', 10, height - 40)
      ctx.fillText(Math.max(...data).toString(), 10, 20)
    },

    refresh() {
      this.drawChart()
    }
  }
})