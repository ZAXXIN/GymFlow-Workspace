// 存储工具

/**
 * 设置存储
 */
export const setStorage = <T>(key: string, value: T): void => {
  try {
    wx.setStorageSync(key, value)
  } catch (error) {
    console.error('存储失败:', error)
  }
}

/**
 * 获取存储
 */
export const getStorage = <T>(key: string): T | null => {
  try {
    return wx.getStorageSync(key) || null
  } catch (error) {
    console.error('读取存储失败:', error)
    return null
  }
}

/**
 * 移除存储
 */
export const removeStorage = (key: string): void => {
  try {
    wx.removeStorageSync(key)
  } catch (error) {
    console.error('移除存储失败:', error)
  }
}

/**
 * 清空存储
 */
export const clearStorage = (): void => {
  try {
    wx.clearStorageSync()
  } catch (error) {
    console.error('清空存储失败:', error)
  }
}

// 存储键名常量
export const STORAGE_KEYS = {
  TOKEN: 'token',
  USER_INFO: 'userInfo',
  SEARCH_HISTORY: 'searchHistory',
  MESSAGE_LAST_READ: 'messageLastRead'
} as const

/**
 * 保存搜索历史
 */
export const saveSearchHistory = (keyword: string): void => {
  const history = getStorage<string[]>(STORAGE_KEYS.SEARCH_HISTORY) || []
  
  // 去重
  const newHistory = history.filter(item => item !== keyword)
  newHistory.unshift(keyword)
  
  // 限制数量
  if (newHistory.length > 10) {
    newHistory.pop()
  }
  
  setStorage(STORAGE_KEYS.SEARCH_HISTORY, newHistory)
}

/**
 * 获取搜索历史
 */
export const getSearchHistory = (): string[] => {
  return getStorage<string[]>(STORAGE_KEYS.SEARCH_HISTORY) || []
}

/**
 * 清除搜索历史
 */
export const clearSearchHistory = (): void => {
  removeStorage(STORAGE_KEYS.SEARCH_HISTORY)
}