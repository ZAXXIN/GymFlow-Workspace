import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDictData, getDictOptions } from '@/api/system'
import type { DictItem, DictData, DictOptions } from '@/types'

interface DictCache {
  data: DictItem[]
  timestamp: number
}

interface DictOptions {
  // 字典配置
  dictCode: string
  immediate?: boolean
  cache?: boolean
  cacheDuration?: number // 缓存时长（毫秒），默认30分钟
  
  // 事件回调
  onLoaded?: (data: DictItem[]) => void
  onError?: (error: any) => void
}

/**
 * 字典数据组合式函数
 * @param dictCode 字典编码
 * @param options 配置选项
 * @returns 字典相关的状态和方法
 */
export function useDict(dictCode: string): {
  dictData: DictItem[]
  loading: boolean
  error: string | null
  refresh: () => Promise<void>
  getLabel: (value: string | number) => string
  getValue: (label: string) => string | number | undefined
  getItem: (value: string | number) => DictItem | undefined
}

export function useDict(options: DictOptions): {
  dictData: DictItem[]
  loading: boolean
  error: string | null
  refresh: () => Promise<void>
  getLabel: (value: string | number) => string
  getValue: (label: string) => string | number | undefined
  getItem: (value: string | number) => DictItem | undefined
}

export function useDict(arg1: string | DictOptions, arg2?: DictOptions) {
  // 参数处理
  let dictCode: string
  let options: DictOptions
  
  if (typeof arg1 === 'string') {
    dictCode = arg1
    options = arg2 || {}
  } else {
    dictCode = arg1.dictCode
    options = arg1
  }
  
  // 默认配置
  const defaultOptions: DictOptions = {
    dictCode,
    immediate: true,
    cache: true,
    cacheDuration: 30 * 60 * 1000, // 30分钟
    ...options
  }
  
  // 字典数据
  const dictData = ref<DictItem[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  
  // 缓存字典
  const dictCache = reactive<Record<string, DictCache>>({})
  
  // 从缓存获取字典数据
  const getFromCache = (): DictItem[] | null => {
    if (!defaultOptions.cache) return null
    
    const cache = dictCache[dictCode]
    if (!cache) return null
    
    const now = Date.now()
    if (now - cache.timestamp > defaultOptions.cacheDuration!) {
      // 缓存已过期
      delete dictCache[dictCode]
      return null
    }
    
    return cache.data
  }
  
  // 保存到缓存
  const saveToCache = (data: DictItem[]) => {
    if (!defaultOptions.cache) return
    
    dictCache[dictCode] = {
      data,
      timestamp: Date.now()
    }
  }
  
  // 加载字典数据
  const loadDictData = async (forceRefresh: boolean = false): Promise<void> => {
    // 如果强制刷新，清除缓存
    if (forceRefresh && defaultOptions.cache) {
      delete dictCache[dictCode]
    }
    
    // 检查缓存
    if (!forceRefresh && defaultOptions.cache) {
      const cachedData = getFromCache()
      if (cachedData) {
        dictData.value = cachedData
        return
      }
    }
    
    try {
      loading.value = true
      error.value = null
      
      const response = await getDictData(dictCode)
      
      if (response.code === 200 && response.data) {
        dictData.value = response.data
        
        // 保存到缓存
        saveToCache(response.data)
        
        // 回调函数
        defaultOptions.onLoaded?.(response.data)
      } else {
        throw new Error(response.message || '获取字典数据失败')
      }
    } catch (err: any) {
      console.error(`加载字典 ${dictCode} 失败:`, err)
      error.value = err.message || '加载字典数据失败'
      
      // 回调函数
      defaultOptions.onError?.(err)
      
      ElMessage.error(`加载字典数据失败: ${err.message}`)
    } finally {
      loading.value = false
    }
  }
  
  // 刷新字典数据
  const refreshDict = async (): Promise<void> => {
    await loadDictData(true)
  }
  
  // 根据值获取标签
  const getLabelByValue = (value: string | number): string => {
    const item = dictData.value.find(item => item.value === value || item.value === String(value))
    return item?.label || String(value)
  }
  
  // 根据标签获取值
  const getValueByLabel = (label: string): string | number | undefined => {
    const item = dictData.value.find(item => item.label === label)
    return item?.value
  }
  
  // 根据值获取字典项
  const getItemByValue = (value: string | number): DictItem | undefined => {
    return dictData.value.find(item => item.value === value || item.value === String(value))
  }
  
  // 获取字典选项（用于Element Plus的选择器）
  const getDictOptions = computed(() => {
    return dictData.value.map(item => ({
      label: item.label,
      value: item.value,
      ...item
    }))
  })
  
  // 检查字典是否包含某个值
  const containsValue = (value: string | number): boolean => {
    return dictData.value.some(item => item.value === value || item.value === String(value))
  }
  
  // 检查字典是否包含某个标签
  const containsLabel = (label: string): boolean => {
    return dictData.value.some(item => item.label === label)
  }
  
  // 获取字典描述（用于显示）
  const getDictDescription = (): string => {
    if (dictData.value.length === 0) return '无数据'
    
    return dictData.value.map(item => `${item.label}(${item.value})`).join(', ')
  }
  
  // 监听字典编码变化
  watch(
    () => dictCode,
    (newDictCode) => {
      if (newDictCode) {
        loadDictData()
      }
    }
  )
  
  // 初始化加载
  if (defaultOptions.immediate) {
    loadDictData()
  }
  
  return {
    // 状态
    dictData,
    loading,
    error,
    
    // 方法
    loadDictData,
    refresh: refreshDict,
    getLabel: getLabelByValue,
    getValue: getValueByLabel,
    getItem: getItemByValue,
    
    // 计算属性
    options: getDictOptions,
    
    // 工具方法
    containsValue,
    containsLabel,
    getDictDescription
  }
}

/**
 * 批量加载多个字典
 * @param dictCodes 字典编码数组
 * @returns 字典数据对象
 */
export function useDicts(dictCodes: string[]) {
  const dicts = reactive<Record<string, ReturnType<typeof useDict>>>({})
  
  // 初始化字典
  dictCodes.forEach(code => {
    dicts[code] = useDict(code)
  })
  
  // 批量加载字典
  const loadAllDicts = async () => {
    const promises = dictCodes.map(code => dicts[code].loadDictData())
    await Promise.all(promises)
  }
  
  // 批量刷新字典
  const refreshAllDicts = async () => {
    const promises = dictCodes.map(code => dicts[code].refresh())
    await Promise.all(promises)
  }
  
  // 检查所有字典是否已加载
  const allLoaded = computed(() => {
    return dictCodes.every(code => !dicts[code].loading && dicts[code].dictData.length > 0)
  })
  
  // 检查是否有错误
  const hasError = computed(() => {
    return dictCodes.some(code => dicts[code].error)
  })
  
  return {
    dicts,
    loadAllDicts,
    refreshAllDicts,
    allLoaded,
    hasError
  }
}

/**
 * 常用字典编码常量
 */
export const DICT_CODES = {
  // 会员相关
  MEMBER_STATUS: 'member_status', // 会员状态
  MEMBER_LEVEL: 'member_level',   // 会员等级
  MEMBER_SOURCE: 'member_source', // 会员来源
  
  // 教练相关
  COACH_LEVEL: 'coach_level',     // 教练等级
  COACH_STATUS: 'coach_status',   // 教练状态
  
  // 课程相关
  COURSE_TYPE: 'course_type',     // 课程类型
  COURSE_STATUS: 'course_status', // 课程状态
  COURSE_LEVEL: 'course_level',   // 课程难度
  
  // 订单相关
  ORDER_STATUS: 'order_status',   // 订单状态
  PAYMENT_METHOD: 'payment_method', // 支付方式
  PAYMENT_STATUS: 'payment_status', // 支付状态
  
  // 通用
  GENDER: 'gender',               // 性别
  YES_NO: 'yes_no',               // 是否
  ENABLE_STATUS: 'enable_status'  // 启用状态
} as const