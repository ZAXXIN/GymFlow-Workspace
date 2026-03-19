// 会员端首页逻辑
import { userStore } from '../../../stores/user.store'
import { configStore } from '../../../stores/config.store'
import { getCurrentReminder } from '../../../services/api/checkin.api'
import { getProductList } from '../../../services/api/product.api'
import { showToast } from '../../../utils/wx-util'

Page({
  data: {
    // 用户信息
    userInfo: null,
    
    // 消息存储（用于显示红点）
    messageStore: null,
    
    // 当前提醒
    currentReminder: null,
    
    // 分类数据（按productType）
    categories: [
      { id: 0, name: '全部', type: null },
      { id: 1, name: '会籍卡', type: 0 },
      { id: 2, name: '私教课', type: 1 },
      { id: 3, name: '团课', type: 2 },
      { id: 4, name: '相关产品', type: 3 }
    ],
    categoryNames: ['全部', '会籍卡', '私教课', '团课', '相关产品'],
    activeCategory: 0, // 索引，0-全部
    
    // 商品数据
    products: [],
    loading: false,
    hasMore: true,
    pageNum: 1,
    pageSize: 10,
    
    // 搜索关键字
    keyword: ''
  },

  onLoad: function() {
    // 获取app实例
    const app = getApp()
    console.log('会员信息:', userStore.userInfo)
    
    this.setData({ 
      userInfo: userStore.userInfo,
      messageStore: app.globalData.messageStore 
    })
    
    this.initData()
  },

  onShow: function() {
    // 每次显示页面时刷新提醒
    this.loadCurrentReminder()
  },

  /**
   * 获取当前日期
   */
  getDate: function() {
    const date = new Date()
    const year = date.getFullYear()
    const month = date.getMonth() + 1
    const day = date.getDate()
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const weekday = weekdays[date.getDay()]
    
    return `${year}年${month}月${day}日 ${weekday}`
  },

  /**
   * 初始化数据
   */
  initData: function() {
    // 加载商品
    this.loadProducts(true)
    
    // 加载提醒
    this.loadCurrentReminder()
  },

  /**
   * 加载商品列表
   */
  loadProducts: function(refresh) {
    var that = this
    if (this.data.loading) return
    if (!refresh && !this.data.hasMore) return
    
    this.setData({ loading: true })
    
    var params = {
      pageNum: refresh ? 1 : this.data.pageNum,
      pageSize: this.data.pageSize,
      status: 1 // 只显示在售商品
    }
    
    // 按商品类型筛选
    var activeCategory = this.data.activeCategory
    if (activeCategory > 0) {
      var category = this.data.categories[activeCategory]
      if (category && category.type !== null) {
        params.productType = category.type
      }
    }
    
    // 关键字搜索
    if (this.data.keyword) {
      params.productName = this.data.keyword
    }
    
    console.log('请求参数:', params)
    
    getProductList(params).then(function(result) {
      console.log('商品列表返回:', result)
      that.setData({
        products: refresh ? result.list : [...that.data.products, ...result.list],
        hasMore: result.pageNum < result.pages,
        pageNum: (refresh ? 1 : that.data.pageNum) + 1,
        loading: false
      })
    }).catch(function(error) {
      console.error('加载商品失败:', error)
      that.setData({ loading: false })
    })
  },

  /**
   * 加载当前提醒
   */
  loadCurrentReminder: function() {
    var that = this
    getCurrentReminder().then(function(reminder) {
      // 根据后端返回的数据结构调整
      if (reminder && reminder.type === 'MEMBER') {
        that.setData({ currentReminder: reminder })
      } else {
        that.setData({ currentReminder: null })
      }
    }).catch(function(error) {
      console.error('加载提醒失败:', error)
    })
  },

  /**
   * 分类切换
   */
  onCategoryChange: function(e) {
    var that = this
    var index = e.detail.index
    
    this.setData({ 
      activeCategory: index,
      pageNum: 1,
      products: []
    }, function() {
      that.loadProducts(true)
    })
  },

  /**
   * 搜索输入
   */
  onSearchInput: function(e) {
    this.setData({ keyword: e.detail.value })
  },

  /**
   * 执行搜索
   */
  onSearch: function(e) {
    var that = this
    var value = e.detail.value
    this.setData({ 
      keyword: value,
      pageNum: 1,
      products: []
    }, function() {
      that.loadProducts(true)
    })
  },

  /**
   * 清除搜索
   */
  onSearchClear: function() {
    var that = this
    this.setData({ 
      keyword: '',
      pageNum: 1,
      products: []
    }, function() {
      that.loadProducts(true)
    })
  },

  /**
   * 点击提醒
   */
  onReminderTap: function() {
    var currentReminder = this.data.currentReminder
    if (currentReminder && currentReminder.bookingId) {
      wx.navigateTo({
        url: '/pages/member/checkin-code/index?bookingId=' + currentReminder.bookingId
      })
    }
  },

  /**
   * 点击商品
   */
  onProductTap: function(e) {
    var product = e.currentTarget.dataset.product || e.detail.product
    if (!product) return
    
    wx.navigateTo({
      url: '/pages/common/product-detail/index?id=' + product.id
    })
  },

  /**
   * 购买商品
   */
  onProductBuy: function(e) {
    var product = e.currentTarget.dataset.product || e.detail.product
    if (!product) return
    
    // 检查登录状态
    if (!userStore.isLogin) {
      wx.navigateTo({
        url: '/pages/common/login/index'
      })
      return
    }
    
    wx.navigateTo({
      url: '/pages/common/product-detail/index?id=' + product.id + '&action=buy'
    })
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh: function() {
    this.loadProducts(true)
    this.loadCurrentReminder()
    wx.stopPullDownRefresh()
  },

  /**
   * 上拉加载更多
   */
  onReachBottom: function() {
    this.loadProducts(false)
  },

  /**
   * 跳转到消息列表
   */
  goToMessage: function() {
    wx.navigateTo({
      url: '/pages/common/message-list/index'
    })
  }
})