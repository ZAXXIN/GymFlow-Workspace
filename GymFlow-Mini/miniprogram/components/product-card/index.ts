// 商品卡片组件逻辑
Component({
  properties: {
    product: {
      type: Object,
      value: {}
    },
    layout: {
      type: String,
      value: 'grid' // grid | list
    }
  },

  data: {
    discountText: '0'
  },

  observers: {
    'product': function(product) {
      if (product && product.originalPrice > product.currentPrice) {
        const discount = Math.round((1 - product.currentPrice / product.originalPrice) * 100)
        this.setData({ discountText: discount })
      }
    }
  },

  methods: {
    onTap() {
      this.triggerEvent('tap', { product: this.properties.product })
    },

    onBuyTap() {
      this.triggerEvent('buy', { product: this.properties.product })
    },

    onImageError() {
      // 图片加载失败时使用默认图片
      this.setData({
        'product.images[0]': '/assets/icons/logo.jpg'
      })
    }
  }
})