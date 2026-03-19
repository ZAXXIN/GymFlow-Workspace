// 搜索结果页面
import { searchProducts, searchCourses, searchAll } from '../../../services/api/search.api'
import { userStore } from '../../../stores/user.store'
import { formatTime } from '../../../utils/date'
import { showToast, showModal, showLoading, hideLoading } from '../../../utils/wx-util'

Page({
  data: {
    // 搜索关键词
    keyword: '',
    
    // 当前Tab 0-全部 1-商品 2-课程
    activeTab: 0,
    
    // 排序方式
    sortType: 'default', // default-默认 price-价格 sales-销量
    sortText: '默认排序',
    
    // 商品列表
    products: [] as any[],
    productTotal: 0,
    
    // 课程列表
    courses: [] as any[],
    courseTotal: 0,
    
    // 分页
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    
    // 是否显示筛选栏
    showFilter: false,
    
    // 加载状态
    loading: false
  },

  onLoad(options: any) {
    const { keyword } = options
    
    if (keyword) {
      this.setData({ 
        keyword: decodeURIComponent(keyword),
        showFilter: true
      })
      this.search()
    }
  },

  /**
   * 执行搜索
   */
  async search(refresh: boolean = false) {
    const { keyword, activeTab, sortType, pageNum, pageSize, loading, hasMore } = this.data
    
    if (!keyword) {
      showToast('请输入搜索关键词', 'none')
      return
    }
    
    if (loading) return
    if (!refresh && !hasMore) return
    
    this.setData({ loading: true })
    
    try {
      let result
      
      if (activeTab === 0) {
        // 综合搜索
        result = await searchAll({
          keyword,
          pageNum: refresh ? 1 : pageNum,
          pageSize
        })
        
        this.setData({
          products: refresh ? result.products.list : [...this.data.products, ...result.products.list],
          productTotal: result.products.total,
          courses: refresh ? result.courses.list : [...this.data.courses, ...result.courses.list],
          courseTotal: result.courses.total,
          hasMore: result.products.pageNum < result.products.pages || result.courses.pageNum < result.courses.pages,
          pageNum: (refresh ? 1 : pageNum) + 1,
          loading: false
        })
        
      } else if (activeTab === 1) {
        // 搜索商品
        result = await searchProducts({
          keyword,
          pageNum: refresh ? 1 : pageNum,
          pageSize
        })
        
        // 根据排序方式处理
        let productList = result.list
        if (sortType === 'price') {
          productList = this.sortByPrice(productList)
        } else if (sortType === 'sales') {
          productList = this.sortBySales(productList)
        }
        
        this.setData({
          products: refresh ? productList : [...this.data.products, ...productList],
          productTotal: result.total,
          hasMore: result.pageNum < result.pages,
          pageNum: (refresh ? 1 : pageNum) + 1,
          loading: false
        })
        
      } else if (activeTab === 2) {
        // 搜索课程
        result = await searchCourses({
          keyword,
          pageNum: refresh ? 1 : pageNum,
          pageSize
        })
        
        this.setData({
          courses: refresh ? result.list : [...this.data.courses, ...result.list],
          courseTotal: result.total,
          hasMore: result.pageNum < result.pages,
          pageNum: (refresh ? 1 : pageNum) + 1,
          loading: false
        })
      }
      
    } catch (error: any) {
      console.error('搜索失败:', error)
      this.setData({ loading: false })
      showToast(error.message || '搜索失败', 'none')
    }
  },

  /**
   * 搜索输入
   */
  onSearch(e: any) {
    const { value } = e.detail
    if (!value) {
      showToast('请输入搜索关键词', 'none')
      return
    }
    
    this.setData({
      keyword: value,
      pageNum: 1,
      hasMore: true,
      showFilter: true
    })
    
    this.search(true)
  },

  /**
   * 清除搜索
   */
  onClear() {
    this.setData({
      keyword: '',
      products: [],
      courses: [],
      showFilter: false
    })
  },

  /**
   * Tab切换
   */
  onTabChange(e: any) {
    const { index } = e.currentTarget.dataset
    this.setData({ 
      activeTab: parseInt(index),
      pageNum: 1,
      hasMore: true
    }, () => {
      this.search(true)
    })
  },

  /**
   * 排序切换
   */
  onSortChange() {
    const { sortType } = this.data
    
    const items = ['默认排序', '价格从低到高', '价格从高到低', '销量优先']
    const values = ['default', 'price-asc', 'price-desc', 'sales']
    
    wx.showActionSheet({
      itemList: items,
      success: (res) => {
        const index = res.tapIndex
        this.setData({ 
          sortType: values[index],
          sortText: items[index],
          pageNum: 1,
          hasMore: true
        }, () => {
          this.search(true)
        })
      }
    })
  },

  /**
   * 按价格排序
   */
  sortByPrice(list: any[]): any[] {
    const { sortType } = this.data
    const result = [...list]
    
    if (sortType === 'price-asc') {
      result.sort((a, b) => a.currentPrice - b.currentPrice)
    } else if (sortType === 'price-desc') {
      result.sort((a, b) => b.currentPrice - a.currentPrice)
    }
    
    return result
  },

  /**
   * 按销量排序
   */
  sortBySales(list: any[]): any[] {
    const result = [...list]
    result.sort((a, b) => (b.salesVolume || 0) - (a.salesVolume || 0))
    return result
  },

  /**
   * 点击商品
   */
  onProductTap(e: any) {
    const { product } = e.detail
    wx.navigateTo({
      url: `/pages/common/product-detail/index?id=${product.id}`
    })
  },

  /**
   * 购买商品
   */
  onProductBuy(e: any) {
    const { product } = e.detail
    
    // 检查登录状态
    if (!userStore.isLogin) {
      wx.navigateTo({
        url: '/pages/common/login/index'
      })
      return
    }
    
    wx.navigateTo({
      url: `/pages/common/product-detail/index?id=${product.id}&action=buy`
    })
  },

  /**
   * 点击课程
   */
  onCourseTap(e: any) {
    const { course } = e.currentTarget.dataset
    
    wx.navigateTo({
      url: `/pages/common/booking-detail/index?id=${course.scheduleId}`
    })
  },

  /**
   * 格式化时间
   */
  formatTime(time: string): string {
    return formatTime(time)
  },

  /**
   * 上拉加载更多
   */
  onReachBottom() {
    this.search(false)
  }
})