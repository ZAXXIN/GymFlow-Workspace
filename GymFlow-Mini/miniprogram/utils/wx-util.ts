// 微信API封装工具

/**
 * 显示提示
 */
export const showToast = (title: string, icon: 'success' | 'error' | 'none' = 'none') => {
  wx.showToast({
    title,
    icon,
    duration: 2000
  })
}

/**
 * 显示成功提示
 */
export const showSuccess = (title: string) => {
  showToast(title, 'success')
}

/**
 * 显示错误提示
 */
export const showError = (title: string) => {
  showToast(title, 'error')
}

/**
 * 显示加载中
 */
export const showLoading = (title: string = '加载中...') => {
  wx.showLoading({
    title,
    mask: true
  })
}

/**
 * 隐藏加载中
 */
export const hideLoading = () => {
  wx.hideLoading()
}

/**
 * 显示模态框
 */
export const showModal = (options: {
  title: string
  content: string
  confirmText?: string
  cancelText?: string
}): Promise<boolean> => {
  return new Promise((resolve) => {
    wx.showModal({
      title: options.title,
      content: options.content,
      confirmText: options.confirmText || '确定',
      cancelText: options.cancelText || '取消',
      success: (res) => {
        resolve(res.confirm)
      }
    })
  })
}

/**
 * 显示操作菜单
 */
export const showActionSheet = (items: string[]): Promise<number> => {
  return new Promise((resolve, reject) => {
    wx.showActionSheet({
      itemList: items,
      success: (res) => {
        resolve(res.tapIndex)
      },
      fail: reject
    })
  })
}

/**
 * 扫描二维码
 */
export const scanCode = (): Promise<string> => {
  return new Promise((resolve, reject) => {
    wx.scanCode({
      onlyFromCamera: false,
      scanType: ['qrCode'],
      success: (res) => {
        resolve(res.result)
      },
      fail: reject
    })
  })
}

/**
 * 拨打电话
 */
export const makePhoneCall = (phoneNumber: string) => {
  wx.makePhoneCall({
    phoneNumber
  })
}

/**
 * 选择图片
 */
export const chooseImage = (count: number = 1): Promise<string[]> => {
  return new Promise((resolve, reject) => {
    wx.chooseImage({
      count,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        resolve(res.tempFilePaths)
      },
      fail: reject
    })
  })
}

/**
 * 预览图片
 */
export const previewImage = (urls: string[], current?: string) => {
  wx.previewImage({
    urls,
    current: current || urls[0]
  })
}

/**
 * 设置导航栏标题
 */
export const setNavigationBarTitle = (title: string) => {
  wx.setNavigationBarTitle({
    title
  })
}

/**
 * 显示导航栏加载中
 */
export const showNavigationBarLoading = () => {
  wx.showNavigationBarLoading()
}

/**
 * 隐藏导航栏加载中
 */
export const hideNavigationBarLoading = () => {
  wx.hideNavigationBarLoading()
}

/**
 * 设置TabBar徽标
 */
export const setTabBarBadge = (index: number, text: string) => {
  wx.setTabBarBadge({
    index,
    text
  })
}

/**
 * 移除TabBar徽标
 */
export const removeTabBarBadge = (index: number) => {
  wx.removeTabBarBadge({
    index
  })
}

/**
 * 显示TabBar红点
 */
export const showTabBarRedDot = (index: number) => {
  wx.showTabBarRedDot({
    index
  })
}

/**
 * 隐藏TabBar红点
 */
export const hideTabBarRedDot = (index: number) => {
  wx.hideTabBarRedDot({
    index
  })
}

/**
 * 检查版本更新
 */
export const checkUpdate = () => {
  if (!wx.canIUse('getUpdateManager')) {
    return
  }
  
  const updateManager = wx.getUpdateManager()
  
  updateManager.onCheckForUpdate((res) => {
    console.log('是否有新版本:', res.hasUpdate)
  })
  
  updateManager.onUpdateReady(() => {
    showModal({
      title: '更新提示',
      content: '新版本已准备好，是否重启应用？'
    }).then((confirm) => {
      if (confirm) {
        updateManager.applyUpdate()
      }
    })
  })
  
  updateManager.onUpdateFailed(() => {
    console.log('新版本下载失败')
  })
}