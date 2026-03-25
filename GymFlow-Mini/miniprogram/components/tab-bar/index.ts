import { userStore } from '../../stores/user.store'
import { messageStore } from '../../stores/message.store'

Component({
  properties: {
    selected: {
      type: Number,
      value: 0
    }
  },

  data: {
    show: true,
    list: [] as any[],
    // 会员 TabBar 配置
    memberTabList: [
      {
        pagePath: '/pages/member/home/index',
        text: '首页',
        iconPath: '/assets/icons/member/home.png',
        selectedIconPath: '/assets/icons/member/home-active.png'
      },
      {
        pagePath: '/pages/member/booking/index',
        text: '预约',
        iconPath: '/assets/icons/member/booking.png',
        selectedIconPath: '/assets/icons/member/booking-active.png'
      },
      {
        pagePath: '/pages/member/my/index',
        text: '我的',
        iconPath: '/assets/icons/member/my.png',
        selectedIconPath: '/assets/icons/member/my-active.png'
      }
    ],
    // 教练 TabBar 配置
    coachTabList: [
      {
        pagePath: '/pages/coach/home/index',
        text: '工作台',
        iconPath: '/assets/icons/member/home.png',
        selectedIconPath: '/assets/icons/member/home-active.png'
      },
      // {
      //   pagePath: '/pages/coach/student-list/index',
      //   text: '学员',
      //   iconPath: '/assets/icons/coach/student.png',
      //   selectedIconPath: '/assets/icons/coach/student-active.png'
      // },
      // {
      //   pagePath: '/pages/coach/finance/index',
      //   text: '财务',
      //   iconPath: '/assets/icons/coach/finance.png',
      //   selectedIconPath: '/assets/icons/coach/finance-active.png'
      // },
      {
        pagePath: '/pages/coach/my/index',
        text: '我的',
        iconPath: '/assets/icons/member/my.png',
        selectedIconPath: '/assets/icons/member/my-active.png'
      }
    ]
  },

  lifetimes: {
    attached() {
      this.updateTabBarByRole()
      this.loadUnreadCount()
    }
  },

  pageLifetimes: {
    show() {
      // 每次页面显示时更新未读消息数
      this.loadUnreadCount()
    }
  },

  methods: {
    /**
     * 根据用户角色更新 TabBar
     */
    updateTabBarByRole() {
      const role = userStore.role
      let list: any[] = []
      
      if (role === 'COACH') {
        list = JSON.parse(JSON.stringify(this.data.coachTabList))
      } else {
        list = JSON.parse(JSON.stringify(this.data.memberTabList))
      }
      
      this.setData({ list })
    },

    /**
     * 加载未读消息数
     */
    async loadUnreadCount() {
      // 只在"我的" Tab 显示未读消息数
      const unreadCount = messageStore.unreadCount
      const list = this.data.list
      
      if (list.length > 0) {
        // 找到"我的"对应的 Tab（通常是最后一个）
        const lastIndex = list.length - 1
        list[lastIndex].badge = unreadCount > 0 ? unreadCount : 0
        this.setData({ list })
      }
    },

    /**
     * 切换 Tab
     */
    switchTab(e: any) {
      const { index, path } = e.currentTarget.dataset
      const currentPath = this.getCurrentPagePath()
      
      // 如果点击的是当前页面，不跳转
      if (currentPath === path) {
        return
      }
      
      // 触发父组件的切换事件
      this.triggerEvent('change', { index, path })
      
      // 跳转页面
      wx.switchTab({
        url: path,
        fail: () => {
          wx.reLaunch({ url: path })
        }
      })
    },

    /**
     * 获取当前页面路径
     */
    getCurrentPagePath() {
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      return '/' + currentPage.route
    }
  }
})