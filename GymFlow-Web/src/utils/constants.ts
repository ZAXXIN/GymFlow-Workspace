// 本地存储键名
export const StorageKeys = {
  TOKEN: 'gymflow_token',
  USER_INFO: 'gymflow_user_info',
  SETTINGS: 'gymflow_settings',
  THEME: 'gymflow_theme',
  LANGUAGE: 'gymflow_language'
} as const

// 路由名称
export const RouteNames = {
  LOGIN: 'Login',
  DASHBOARD: 'Dashboard',
  MEMBER_LIST: 'MemberList',
  MEMBER_DETAIL: 'MemberDetail',
  COACH_LIST: 'CoachList',
  COACH_DETAIL: 'CoachDetail',
  COURSE_LIST: 'CourseList',
  COURSE_DETAIL: 'CourseDetail',
  ORDER_LIST: 'OrderList',
  ORDER_DETAIL: 'OrderDetail',
  CHECKIN_LIST: 'CheckInList',
  SETTINGS: 'Settings'
} as const

// 页面标题
export const PageTitles = {
  [RouteNames.LOGIN]: '登录 - GymFlow',
  [RouteNames.DASHBOARD]: '仪表盘 - GymFlow',
  [RouteNames.MEMBER_LIST]: '会员管理 - GymFlow',
  [RouteNames.MEMBER_DETAIL]: '会员详情 - GymFlow',
  [RouteNames.COACH_LIST]: '教练管理 - GymFlow',
  [RouteNames.COACH_DETAIL]: '教练详情 - GymFlow',
  [RouteNames.COURSE_LIST]: '课程管理 - GymFlow',
  [RouteNames.COURSE_DETAIL]: '课程详情 - GymFlow',
  [RouteNames.ORDER_LIST]: '订单管理 - GymFlow',
  [RouteNames.ORDER_DETAIL]: '订单详情 - GymFlow',
  [RouteNames.CHECKIN_LIST]: '签到记录 - GymFlow',
  [RouteNames.SETTINGS]: '系统设置 - GymFlow'
} as const

// 默认分页参数
export const DefaultPageParams = {
  PAGE: 1,
  PAGE_SIZE: 10,
  PAGE_SIZES: [10, 20, 50, 100]
} as const

// 日期时间格式
export const DateTimeFormats = {
  DATE: 'YYYY-MM-DD',
  TIME: 'HH:mm',
  DATETIME: 'YYYY-MM-DD HH:mm',
  FULL_DATETIME: 'YYYY-MM-DD HH:mm:ss',
  WEEKDAY: 'dddd',
  MONTH_DAY: 'MM-DD'
} as const

// 课程时间表
export const CourseSchedule = {
  MORNING: ['08:00', '09:00', '10:00', '11:00'],
  AFTERNOON: ['14:00', '15:00', '16:00', '17:00'],
  EVENING: ['18:00', '19:00', '20:00', '21:00']
} as const

// 课程难度颜色
export const DifficultyColors = {
  BEGINNER: '#67c23a',
  INTERMEDIATE: '#e6a23c',
  ADVANCED: '#f56c6c'
} as const

// 状态颜色
export const StatusColors = {
  ACTIVE: '#67c23a',
  INACTIVE: '#909399',
  SUSPENDED: '#e6a23c',
  EXPIRED: '#f56c6c',
  PENDING: '#e6a23c',
  COMPLETED: '#67c23a',
  CANCELLED: '#909399',
  SCHEDULED: '#409eff',
  IN_PROGRESS: '#e6a23c'
} as const

// 订单状态颜色
export const OrderStatusColors = {
  PENDING: '#e6a23c',
  PROCESSING: '#409eff',
  COMPLETED: '#67c23a',
  CANCELLED: '#909399',
  REFUNDED: '#f56c6c'
} as const

// 支付方式图标
export const PaymentMethodIcons = {
  WECHAT: 'i-ep-wechat',
  ALIPAY: 'i-ep-alipay',
  CASH: 'i-ep-money',
  CARD: 'i-ep-credit-card',
  BANK_TRANSFER: 'i-ep-bank-card'
} as const

// 性别选项
export const GenderOptions = [
  { label: '男', value: 1 },
  { label: '女', value: 0 }
] as const

// 是/否选项
export const YesNoOptions = [
  { label: '是', value: 1 },
  { label: '否', value: 0 }
] as const

// 会员状态选项
export const MemberStatusOptions = [
  { label: '活跃', value: 'ACTIVE' },
  { label: '不活跃', value: 'INACTIVE' },
  { label: '暂停', value: 'SUSPENDED' },
  { label: '已过期', value: 'EXPIRED' }
] as const

// 教练状态选项
export const CoachStatusOptions = [
  { label: '在职', value: 'ACTIVE' },
  { label: '离职', value: 'INACTIVE' },
  { label: '休假', value: 'ON_LEAVE' }
] as const

// 课程分类选项
export const CourseCategoryOptions = [
  { label: '瑜伽', value: 'YOGA' },
  { label: '普拉提', value: 'PILATES' },
  { label: '综合体能', value: 'CROSSFIT' },
  { label: '动感单车', value: 'SPINNING' },
  { label: '力量训练', value: 'BODYBUILDING' },
  { label: '有氧运动', value: 'AEROBICS' },
  { label: '私教课', value: 'PERSONAL_TRAINING' }
] as const

// 课程难度选项
export const CourseDifficultyOptions = [
  { label: '初级', value: 'BEGINNER' },
  { label: '中级', value: 'INTERMEDIATE' },
  { label: '高级', value: 'ADVANCED' }
] as const

// 订单类型选项
export const OrderTypeOptions = [
  { label: '会籍卡', value: 'MEMBERSHIP' },
  { label: '课程套餐', value: 'COURSE_PACKAGE' },
  { label: '私教课', value: 'PERSONAL_TRAINING' },
  { label: '商品', value: 'PRODUCT' }
] as const

// 支付方式选项
export const PaymentMethodOptions = [
  { label: '微信支付', value: 'WECHAT' },
  { label: '支付宝', value: 'ALIPAY' },
  { label: '现金', value: 'CASH' },
  { label: '刷卡', value: 'CARD' },
  { label: '银行转账', value: 'BANK_TRANSFER' }
] as const

// 上传配置
export const UploadConfig = {
  MAX_SIZE: 10 * 1024 * 1024, // 10MB
  ACCEPT_TYPES: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
  UPLOAD_URL: import.meta.env.VITE_UPLOAD_URL || '/api/upload'
} as const

// API路径常量
export const ApiPaths = {
  // 认证
  LOGIN: '/auth/login',
  LOGOUT: '/auth/logout',
  REFRESH_TOKEN: '/auth/refresh',
  USER_INFO: '/auth/me',
  
  // 会员
  MEMBERS: '/members',
  MEMBER_DETAIL: (id: number) => `/members/${id}`,
  MEMBER_STATISTICS: '/members/statistics',
  
  // 教练
  COACHES: '/coaches',
  COACH_DETAIL: (id: number) => `/coaches/${id}`,
  COACH_STATISTICS: '/coaches/statistics',
  
  // 课程
  COURSES: '/courses',
  COURSE_DETAIL: (id: number) => `/courses/${id}`,
  COURSE_SCHEDULE: '/courses/schedule',
  
  // 课程预约
  BOOKINGS: '/bookings',
  BOOKING_DETAIL: (id: number) => `/bookings/${id}`,
  CHECK_IN: (id: number) => `/bookings/${id}/check-in`,
  
  // 订单
  ORDERS: '/orders',
  ORDER_DETAIL: (id: number) => `/orders/${id}`,
  ORDER_STATISTICS: '/orders/statistics',
  
  // 签到
  CHECK_INS: '/check-ins',
  CHECK_IN_STATISTICS: '/check-ins/statistics',
  
  // 仪表盘
  DASHBOARD_STATS: '/dashboard/stats',
  DASHBOARD_CHARTS: '/dashboard/charts',
  
  // 上传
  UPLOAD: '/upload'
} as const