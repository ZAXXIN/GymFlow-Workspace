// Tab导航组件逻辑
Component({
  properties: {
    tabs: {
      type: Array,
      value: []
    },
    current: {
      type: Number,
      value: 0
    },
    fixed: {
      type: Boolean,
      value: false
    },
    scrollable: {
      type: Boolean,
      value: true
    },
    lineColor: {
      type: String,
      value: '#07c160'
    }
  },

  data: {
    scrollLeft: 0
  },

  methods: {
    onTabTap(e: WechatMiniprogram.TouchEvent) {
      const { index } = e.currentTarget.dataset
      if (index === this.properties.current) return
      
      this.triggerEvent('change', { index })
    },

    // 计算滚动位置
    calculateScrollLeft(index: number) {
      const query = this.createSelectorQuery()
      query.selectAll('.tab-nav-item').boundingClientRect()
      query.select('.tab-nav').boundingClientRect()
      
      query.exec((res) => {
        const items = res[0] as any[]
        const nav = res[1] as any
        
        if (items && items.length > index) {
          let scrollLeft = 0
          for (let i = 0; i < index; i++) {
            scrollLeft += items[i].width
          }
          
          // 居中对齐
          const itemWidth = items[index].width
          scrollLeft = scrollLeft + itemWidth / 2 - nav.width / 2
          
          this.setData({ scrollLeft: Math.max(0, scrollLeft) })
        }
      })
    }
  },

  observers: {
    'current': function(current) {
      if (this.properties.scrollable) {
        this.calculateScrollLeft(current)
      }
    }
  }
})