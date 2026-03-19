// 格式化工具

/**
 * 格式化金额
 */
export const formatMoney = (amount: number, digits: number = 2): string => {
  return `¥${amount.toFixed(digits)}`
}

/**
 * 格式化手机号
 */
export const formatPhone = (phone: string): string => {
  if (!phone || phone.length !== 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 格式化身份证号
 */
export const formatIdCard = (idCard: string): string => {
  if (!idCard || idCard.length < 15) return idCard
  return idCard.replace(/(\d{4})\d{10}(\d{4})/, '$1**********$2')
}

/**
 * 格式化数字
 */
export const formatNumber = (num: number, digits: number = 0): string => {
  return num.toFixed(digits)
}

/**
 * 格式化百分比
 */
export const formatPercent = (num: number, digits: number = 2): string => {
  return `${(num * 100).toFixed(digits)}%`
}

/**
 * 格式化时长（分钟转可读文本）
 */
export const formatDuration = (minutes: number): string => {
  if (minutes < 60) {
    return `${minutes}分钟`
  }
  
  const hours = Math.floor(minutes / 60)
  const remainingMinutes = minutes % 60
  
  if (remainingMinutes === 0) {
    return `${hours}小时`
  }
  
  return `${hours}小时${remainingMinutes}分钟`
}

/**
 * 格式化剩余课时
 */
export const formatRemainingSessions = (total: number, used: number): string => {
  const remaining = total - used
  return `${remaining}/${total}`
}

/**
 * 格式化日期范围
 */
export const formatDateRange = (startDate: string, endDate: string): string => {
  return `${formatDate(startDate)} - ${formatDate(endDate)}`
}

/**
 * 获取预约状态文本
 */
export const getBookingStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    0: '待上课',
    1: '已签到',
    2: '已完成',
    3: '已取消'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取预约状态类型
 */
export const getBookingStatusType = (status: number): 'pending' | 'success' | 'info' | 'warning' | 'completed' => {
  const typeMap: Record<number, 'pending' | 'success' | 'info' | 'warning' | 'completed'> = {
    0: 'pending',    // 待上课
    1: 'success',    // 已签到
    2: 'completed',  // 已完成
    3: 'warning'     // 已取消
  }
  return typeMap[status] || 'pending'
}

/**
 * 获取订单状态文本
 */
export const getOrderStatusText = (status: string): string => {
  const statusMap: Record<string, string> = {
    'PENDING': '待支付',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'REFUNDED': '已退款',
    'DELETED': '已删除'
  }
  return statusMap[status] || status
}

/**
 * 获取订单状态类型
 */
export const getOrderStatusType = (status: string): 'pending' | 'success' | 'info' | 'warning' | 'completed' => {
  const typeMap: Record<string, 'pending' | 'success' | 'info' | 'warning' | 'completed'> = {
    'PENDING': 'pending',
    'PROCESSING': 'info',
    'COMPLETED': 'success',
    'CANCELLED': 'warning',
    'REFUNDED': 'warning',
    'DELETED': 'completed'
  }
  return typeMap[status] || 'pending'
}

/**
 * 获取支付状态文本
 */
export const getPaymentStatusText = (status: number): string => {
  return status === 1 ? '已支付' : '待支付'
}

/**
 * 获取商品类型文本
 */
export const getProductTypeText = (type: number): string => {
  const typeMap: Record<number, string> = {
    0: '会籍卡',
    1: '私教课',
    2: '团课',
    3: '相关产品'
  }
  return typeMap[type] || '未知'
}

/**
 * 获取性别文本
 */
export const getGenderText = (gender?: number): string => {
  if (gender === 0) return '女'
  if (gender === 1) return '男'
  return '未知'
}