import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { ECharts, EChartsOption, SetOptionOpts } from 'echarts'
import { debounce } from 'lodash-es'

interface ChartOptions {
  // 图表配置
  theme?: string | object
  initOptions?: {
    width?: number
    height?: number
    devicePixelRatio?: number
    renderer?: 'canvas' | 'svg'
    locale?: string
  }
  setOptions?: SetOptionOpts
  
  // 自适应
  autoResize?: boolean
  resizeDebounce?: number
  
  // 事件
  onReady?: (instance: ECharts) => void
  onClick?: (params: any) => void
  onMouseOver?: (params: any) => void
  onMouseOut?: (params: any) => void
  onDataZoom?: (params: any) => void
  onLegendSelectChanged?: (params: any) => void
}

/**
 * 图表组合式函数
 * @param options 图表配置选项
 * @returns 图表相关的状态和方法
 */
export function useChart(options: ChartOptions = {}) {
  // 图表实例
  const chartInstance = ref<ECharts | null>(null)
  
  // 容器引用
  const chartContainer = ref<HTMLElement | null>(null)
  
  // 图表状态
  const isLoading = ref(false)
  const isReady = ref(false)
  const error = ref<string | null>(null)
  
  // 默认配置
  const defaultOptions: ChartOptions = {
    autoResize: true,
    resizeDebounce: 300,
    ...options
  }
  
  // 初始化图表
  const initChart = () => {
    if (!chartContainer.value) {
      error.value = '图表容器未找到'
      return
    }
    
    try {
      // 销毁现有实例
      if (chartInstance.value) {
        chartInstance.value.dispose()
      }
      
      // 创建新实例
      chartInstance.value = echarts.init(
        chartContainer.value,
        defaultOptions.theme,
        defaultOptions.initOptions
      )
      
      // 注册事件
      registerEvents()
      
      isReady.value = true
      error.value = null
      
      // 回调函数
      defaultOptions.onReady?.(chartInstance.value)
    } catch (err) {
      console.error('初始化图表失败:', err)
      error.value = '初始化图表失败'
      isReady.value = false
    }
  }
  
  // 注册事件
  const registerEvents = () => {
    if (!chartInstance.value) return
    
    const instance = chartInstance.value
    
    // 点击事件
    instance.on('click', (params: any) => {
      defaultOptions.onClick?.(params)
    })
    
    // 鼠标移入事件
    instance.on('mouseover', (params: any) => {
      defaultOptions.onMouseOver?.(params)
    })
    
    // 鼠标移出事件
    instance.on('mouseout', (params: any) => {
      defaultOptions.onMouseOut?.(params)
    })
    
    // 数据缩放事件
    instance.on('datazoom', (params: any) => {
      defaultOptions.onDataZoom?.(params)
    })
    
    // 图例选择变化事件
    instance.on('legendselectchanged', (params: any) => {
      defaultOptions.onLegendSelectChanged?.(params)
    })
  }
  
  // 设置图表选项
  const setChartOption = (option: EChartsOption, opts?: SetOptionOpts) => {
    if (!chartInstance.value) {
      initChart()
    }
    
    if (chartInstance.value) {
      try {
        chartInstance.value.setOption(option, opts || defaultOptions.setOptions)
        error.value = null
      } catch (err) {
        console.error('设置图表选项失败:', err)
        error.value = '设置图表选项失败'
      }
    }
  }
  
  // 获取图表实例
  const getChartInstance = (): ECharts | null => {
    return chartInstance.value
  }
  
  // 重新渲染图表
  const resizeChart = () => {
    if (chartInstance.value) {
      try {
        chartInstance.value.resize()
      } catch (err) {
        console.error('重新渲染图表失败:', err)
      }
    }
  }
  
  // 显示加载动画
  const showLoading = (type?: string, opts?: object) => {
    if (chartInstance.value) {
      isLoading.value = true
      chartInstance.value.showLoading(type, opts)
    }
  }
  
  // 隐藏加载动画
  const hideLoading = () => {
    if (chartInstance.value) {
      isLoading.value = false
      chartInstance.value.hideLoading()
    }
  }
  
  // 清空图表
  const clearChart = () => {
    if (chartInstance.value) {
      chartInstance.value.clear()
    }
  }
  
  // 销毁图表
  const disposeChart = () => {
    if (chartInstance.value) {
      chartInstance.value.dispose()
      chartInstance.value = null
      isReady.value = false
    }
  }
  
  // 自适应调整大小
  const handleResize = debounce(() => {
    resizeChart()
  }, defaultOptions.resizeDebounce)
  
  // 监听容器大小变化
  if (defaultOptions.autoResize) {
    const resizeObserver = new ResizeObserver(handleResize)
    
    onMounted(() => {
      if (chartContainer.value) {
        resizeObserver.observe(chartContainer.value)
      }
    })
    
    onUnmounted(() => {
      if (chartContainer.value) {
        resizeObserver.unobserve(chartContainer.value)
      }
      resizeObserver.disconnect()
    })
  }
  
  // 监听容器引用变化
  watch(chartContainer, (newContainer) => {
    if (newContainer) {
      nextTick(() => {
        initChart()
      })
    }
  }, { immediate: true })
  
  // 组件卸载时销毁图表
  onUnmounted(() => {
    disposeChart()
    
    // 移除窗口resize监听
    if (defaultOptions.autoResize) {
      window.removeEventListener('resize', handleResize)
    }
  })
  
  return {
    // 引用和状态
    chartContainer,
    chartInstance,
    isLoading,
    isReady,
    error,
    
    // 方法
    initChart,
    setChartOption,
    getChartInstance,
    resizeChart,
    showLoading,
    hideLoading,
    clearChart,
    disposeChart
  }
}

/**
 * 生成通用的图表颜色
 */
export function getChartColors(count: number = 6): string[] {
  const baseColors = [
    '#5470c6', // 蓝色
    '#91cc75', // 绿色
    '#fac858', // 黄色
    '#ee6666', // 红色
    '#73c0de', // 浅蓝色
    '#3ba272', // 深绿色
    '#fc8452', // 橙色
    '#9a60b4', // 紫色
    '#ea7ccc', // 粉色
    '#60c0dd'  // 青色
  ]
  
  if (count <= baseColors.length) {
    return baseColors.slice(0, count)
  }
  
  // 如果需要的颜色超过基础颜色，生成渐变色
  const colors = [...baseColors]
  for (let i = baseColors.length; i < count; i++) {
    const hue = (i * 137.508) % 360 // 黄金角度
    colors.push(`hsl(${hue}, 70%, 60%)`)
  }
  
  return colors
}

/**
 * 生成通用的图表配置
 */
export function getCommonChartOption(): Partial<EChartsOption> {
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#dcdfe6',
      borderWidth: 1,
      textStyle: {
        color: '#606266'
      },
      axisPointer: {
        type: 'shadow',
        shadowStyle: {
          color: 'rgba(150, 150, 150, 0.1)'
        }
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    legend: {
      textStyle: {
        color: '#606266'
      },
      itemWidth: 12,
      itemHeight: 12
    },
    textStyle: {
      fontFamily: 'inherit',
      color: '#606266'
    }
  }
}