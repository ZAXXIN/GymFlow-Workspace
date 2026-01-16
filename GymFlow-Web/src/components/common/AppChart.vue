<template>
  <div class="app-chart">
    <!-- 图表头部 -->
    <div v-if="showHeader" class="chart-header">
      <div class="header-left">
        <h3 class="chart-title">{{ title }}</h3>
        <div v-if="subtitle" class="chart-subtitle">{{ subtitle }}</div>
      </div>
      <div v-if="showToolbar" class="header-right">
        <div class="chart-toolbar">
          <!-- 图表类型切换 -->
          <el-dropdown v-if="typeOptions.length > 0" @command="handleTypeChange">
            <el-button size="small" text>
              <el-icon><Grid /></el-icon>
              {{ getCurrentTypeName() }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="option in typeOptions"
                  :key="option.value"
                  :command="option.value"
                >
                  {{ option.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <!-- 时间范围选择 -->
          <el-select
            v-if="timeOptions.length > 0"
            v-model="currentTimeRange"
            size="small"
            style="width: 100px; margin-left: 8px"
            @change="handleTimeChange"
          >
            <el-option
              v-for="option in timeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
          
          <!-- 刷新按钮 -->
          <el-tooltip v-if="showRefresh" content="刷新数据" placement="top">
            <el-button size="small" text @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </el-tooltip>
          
          <!-- 下载按钮 -->
          <el-tooltip v-if="showDownload" content="下载图表" placement="top">
            <el-button size="small" text @click="handleDownload">
              <el-icon><Download /></el-icon>
            </el-button>
          </el-tooltip>
          
          <!-- 全屏按钮 -->
          <el-tooltip v-if="showFullscreen" :content="isFullscreen ? '退出全屏' : '全屏'" placement="top">
            <el-button size="small" text @click="toggleFullscreen">
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>
    </div>

    <!-- 图表容器 -->
    <div
      ref="chartContainer"
      :class="['chart-container', { 'fullscreen': isFullscreen }]"
      :style="{ height: chartHeight, width: '100%' }"
    ></div>

    <!-- 图表底部 -->
    <div v-if="showFooter" class="chart-footer">
      <div class="footer-left">
        <slot name="footer-left" />
      </div>
      <div class="footer-right">
        <!-- 图例 -->
        <div v-if="showLegend && legendData.length > 0" class="chart-legend">
          <div
            v-for="item in legendData"
            :key="item.name"
            class="legend-item"
            @click="toggleLegend(item)"
          >
            <div class="legend-color" :style="{ backgroundColor: item.color }"></div>
            <span class="legend-text">{{ item.name }}</span>
            <span v-if="item.value" class="legend-value">{{ formatValue(item.value) }}</span>
          </div>
        </div>
        <slot name="footer" />
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="chart-loading">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && isEmpty" class="chart-empty">
      <el-empty :description="emptyText" :image-size="emptySize">
        <slot name="empty">
          <el-button v-if="showRefresh" type="primary" size="small" @click="handleRefresh">
            刷新数据
          </el-button>
        </slot>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import type { ECharts, EChartsOption, SetOptionOpts } from 'echarts'
import { ElMessage } from 'element-plus'
import {
  Grid,
  ArrowDown,
  Refresh,
  Download,
  FullScreen
} from '@element-plus/icons-vue'

interface ChartOption extends EChartsOption {
  // 扩展属性
  [key: string]: any
}

interface ChartProps {
  // 基础配置
  option?: ChartOption
  title?: string
  subtitle?: string
  type?: string
  height?: string
  // 显示控制
  showHeader?: boolean
  showFooter?: boolean
  showToolbar?: boolean
  showLegend?: boolean
  showRefresh?: boolean
  showDownload?: boolean
  showFullscreen?: boolean
  // 功能配置
  autoResize?: boolean
  theme?: string | object
  initOptions?: any
  loading?: boolean
  emptyText?: string
  emptySize?: number
  // 数据配置
  data?: any
  legendData?: Array<{ name: string; value?: any; color?: string }>
  // 选项配置
  typeOptions?: Array<{ label: string; value: string }>
  timeOptions?: Array<{ label: string; value: string }>
  // 事件
  onInit?: (chart: ECharts) => void
  onResize?: (chart: ECharts) => void
  onRefresh?: () => void
  onDownload?: (chart: ECharts) => void
}

const props = withDefaults(defineProps<ChartProps>(), {
  option: () => ({}),
  title: '',
  subtitle: '',
  type: 'line',
  height: '400px',
  showHeader: true,
  showFooter: true,
  showToolbar: true,
  showLegend: true,
  showRefresh: true,
  showDownload: true,
  showFullscreen: true,
  autoResize: true,
  theme: 'light',
  initOptions: {},
  loading: false,
  emptyText: '暂无数据',
  emptySize: 120,
  data: null,
  legendData: () => [],
  typeOptions: () => [
    { label: '折线图', value: 'line' },
    { label: '柱状图', value: 'bar' },
    { label: '饼图', value: 'pie' },
    { label: '雷达图', value: 'radar' }
  ],
  timeOptions: () => [
    { label: '今天', value: 'today' },
    { label: '本周', value: 'week' },
    { label: '本月', value: 'month' },
    { label: '本年', value: 'year' }
  ],
  onInit: () => {},
  onResize: () => {},
  onRefresh: () => {},
  onDownload: () => {}
})

const emit = defineEmits<{
  'type-change': [type: string]
  'time-change': [timeRange: string]
  'legend-click': [legend: any]
  'chart-click': [params: any]
  'chart-dblclick': [params: any]
  'chart-mouseover': [params: any]
  'chart-mouseout': [params: any]
  'chart-ready': [chart: ECharts]
}>()

// 引用
const chartContainer = ref<HTMLElement>()
const chartInstance = ref<ECharts | null>(null)

// 状态
const currentType = ref(props.type)
const currentTimeRange = ref(props.timeOptions[0]?.value || '')
const isFullscreen = ref(false)
const resizeObserver = ref<ResizeObserver | null>(null)

// 计算属性
const chartHeight = computed(() => {
  if (isFullscreen.value) {
    return 'calc(100vh - 60px)'
  }
  return props.height
})

const isEmpty = computed(() => {
  if (!chartInstance.value) return true
  const option = chartInstance.value.getOption()
  return !option || !option.series || option.series.length === 0
})

const getCurrentTypeName = () => {
  const option = props.typeOptions.find(opt => opt.value === currentType.value)
  return option?.label || '图表类型'
}

// 格式化数值
const formatValue = (value: any) => {
  if (typeof value === 'number') {
    if (value >= 100000000) {
      return (value / 100000000).toFixed(1) + '亿'
    }
    if (value >= 10000) {
      return (value / 10000).toFixed(1) + '万'
    }
    if (value >= 1000) {
      return (value / 1000).toFixed(1) + '千'
    }
    return value.toString()
  }
  return value
}

// 初始化图表
const initChart = () => {
  if (!chartContainer.value) return

  // 销毁旧实例
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }

  try {
    // 创建新实例
    chartInstance.value = echarts.init(
      chartContainer.value,
      props.theme,
      props.initOptions
    )

    // 设置图表选项
    updateChartOption()

    // 绑定事件
    bindChartEvents()

    // 触发初始化事件
    props.onInit(chartInstance.value)
    emit('chart-ready', chartInstance.value)
  } catch (error) {
    console.error('图表初始化失败:', error)
  }
}

// 更新图表选项
const updateChartOption = () => {
  if (!chartInstance.value) return

  try {
    const option = getMergedOption()
    chartInstance.value.setOption(option, {
      notMerge: false,
      lazyUpdate: true,
      silent: false
    } as SetOptionOpts)
  } catch (error) {
    console.error('更新图表选项失败:', error)
  }
}

// 获取合并后的选项
const getMergedOption = (): ChartOption => {
  const baseOption: ChartOption = {
    title: {
      text: props.title,
      subtext: props.subtitle,
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(50, 50, 50, 0.7)',
      borderWidth: 0,
      textStyle: {
        color: '#fff'
      }
    },
    legend: {
      show: props.showLegend,
      type: 'scroll',
      bottom: 0,
      textStyle: {
        fontSize: 12
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: props.showLegend ? '60px' : '3%',
      top: props.showHeader ? '60px' : '3%',
      containLabel: true
    },
    toolbox: {
      feature: {
        saveAsImage: {
          title: '保存为图片',
          pixelRatio: 2
        },
        dataView: {
          title: '数据视图',
          readOnly: true
        },
        restore: {
          title: '还原'
        }
      }
    }
  }

  // 根据类型设置特定配置
  const typeConfig = getTypeConfig(currentType.value)
  
  // 合并选项
  return echarts.util.merge(
    baseOption,
    typeConfig,
    props.option,
    { series: getSeriesData() }
  ) as ChartOption
}

// 获取类型配置
const getTypeConfig = (type: string): ChartOption => {
  const configs: Record<string, ChartOption> = {
    line: {
      xAxis: {
        type: 'category',
        boundaryGap: false,
        axisLine: {
          lineStyle: {
            color: '#e0e6f1'
          }
        }
      },
      yAxis: {
        type: 'value',
        axisLine: {
          lineStyle: {
            color: '#e0e6f1'
          }
        },
        splitLine: {
          lineStyle: {
            type: 'dashed',
            color: '#e0e6f1'
          }
        }
      }
    },
    bar: {
      xAxis: {
        type: 'category',
        axisLine: {
          lineStyle: {
            color: '#e0e6f1'
          }
        }
      },
      yAxis: {
        type: 'value',
        axisLine: {
          lineStyle: {
            color: '#e0e6f1'
          }
        },
        splitLine: {
          lineStyle: {
            type: 'dashed',
            color: '#e0e6f1'
          }
        }
      }
    },
    pie: {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center'
      }
    },
    radar: {
      radar: {
        indicator: [],
        shape: 'polygon',
        splitNumber: 5,
        axisLine: {
          lineStyle: {
            color: 'rgba(238, 197, 102, 0.5)'
          }
        },
        splitLine: {
          lineStyle: {
            color: 'rgba(238, 197, 102, 0.5)'
          }
        },
        splitArea: {
          show: true,
          areaStyle: {
            color: ['rgba(238, 197, 102, 0.1)', 'rgba(238, 197, 102, 0.2)']
          }
        }
      }
    }
  }

  return configs[type] || configs.line
}

// 获取系列数据
const getSeriesData = () => {
  if (props.data) {
    return Array.isArray(props.data) ? props.data : [props.data]
  }

  // 默认示例数据
  const defaultSeries = {
    line: {
      name: '示例数据',
      type: 'line',
      data: [120, 132, 101, 134, 90, 230, 210],
      smooth: true,
      lineStyle: {
        width: 3
      },
      itemStyle: {
        color: '#409eff'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
        ])
      }
    },
    bar: {
      name: '示例数据',
      type: 'bar',
      data: [120, 132, 101, 134, 90, 230, 210],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      }
    },
    pie: {
      name: '示例数据',
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: 335, name: '直接访问' },
        { value: 310, name: '邮件营销' },
        { value: 234, name: '联盟广告' },
        { value: 135, name: '视频广告' },
        { value: 1548, name: '搜索引擎' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  }

  return [defaultSeries[currentType.value as keyof typeof defaultSeries] || defaultSeries.line]
}

// 绑定图表事件
const bindChartEvents = () => {
  if (!chartInstance.value) return

  chartInstance.value.on('click', (params: any) => {
    emit('chart-click', params)
  })

  chartInstance.value.on('dblclick', (params: any) => {
    emit('chart-dblclick', params)
  })

  chartInstance.value.on('mouseover', (params: any) => {
    emit('chart-mouseover', params)
  })

  chartInstance.value.on('mouseout', (params: any) => {
    emit('chart-mouseout', params)
  })
}

// 处理类型变化
const handleTypeChange = (type: string) => {
  currentType.value = type
  updateChartOption()
  emit('type-change', type)
}

// 处理时间变化
const handleTimeChange = (timeRange: string) => {
  emit('time-change', timeRange)
}

// 处理刷新
const handleRefresh = () => {
  props.onRefresh()
  emit('refresh')
}

// 处理下载
const handleDownload = () => {
  if (!chartInstance.value) {
    ElMessage.warning('图表未初始化')
    return
  }

  try {
    const url = chartInstance.value.getDataURL({
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    
    const link = document.createElement('a')
    link.href = url
    link.download = `${props.title || 'chart'}_${new Date().getTime()}.png`
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    props.onDownload(chartInstance.value)
  } catch (error) {
    console.error('下载图表失败:', error)
    ElMessage.error('下载图表失败')
  }
}

// 切换全屏
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  resizeChart()
}

// 切换图例
const toggleLegend = (legend: any) => {
  if (!chartInstance.value) return
  
  chartInstance.value.dispatchAction({
    type: 'legendToggleSelect',
    name: legend.name
  })
  
  emit('legend-click', legend)
}

// 调整图表大小
const resizeChart = () => {
  nextTick(() => {
    if (chartInstance.value) {
      chartInstance.value.resize()
      props.onResize(chartInstance.value)
    }
  })
}

// 销毁图表
const disposeChart = () => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
}

// 暴露的方法
defineExpose({
  getChartInstance: () => chartInstance.value,
  resize: resizeChart,
  refresh: updateChartOption,
  dispose: disposeChart,
  download: handleDownload,
  getDataURL: (options?: any) => {
    if (!chartInstance.value) return ''
    return chartInstance.value.getDataURL(options)
  },
  clear: () => {
    if (chartInstance.value) {
      chartInstance.value.clear()
    }
  }
})

// 监听属性变化
watch(() => props.option, () => {
  updateChartOption()
}, { deep: true })

watch(() => props.data, () => {
  updateChartOption()
}, { deep: true })

watch(() => props.loading, (loading) => {
  if (chartInstance.value) {
    if (loading) {
      chartInstance.value.showLoading()
    } else {
      chartInstance.value.hideLoading()
    }
  }
})

// 初始化
onMounted(() => {
  initChart()
  
  // 自动调整大小
  if (props.autoResize && chartContainer.value) {
    resizeObserver.value = new ResizeObserver(() => {
      resizeChart()
    })
    resizeObserver.value.observe(chartContainer.value)
  }
  
  // 监听窗口大小变化
  window.addEventListener('resize', resizeChart)
})

// 清理
onUnmounted(() => {
  if (resizeObserver.value) {
    resizeObserver.value.disconnect()
  }
  
  window.removeEventListener('resize', resizeChart)
  disposeChart()
})
</script>

<style scoped lang="scss">
.app-chart {
  position: relative;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  overflow: hidden;
  transition: all 0.3s;

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #ebeef5;

    .header-left {
      .chart-title {
        margin: 0 0 4px 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }

      .chart-subtitle {
        font-size: 14px;
        color: #909399;
      }
    }

    .header-right {
      .chart-toolbar {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
  }

  .chart-container {
    position: relative;
    transition: all 0.3s;

    &.fullscreen {
      position: fixed;
      top: 0;
      left: 0;
      z-index: 9999;
      background: #fff;
      padding: 20px;
    }
  }

  .chart-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    border-top: 1px solid #ebeef5;
    background: #fafafa;

    .footer-left,
    .footer-right {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .chart-legend {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;

      .legend-item {
        display: flex;
        align-items: center;
        gap: 6px;
        cursor: pointer;
        padding: 4px 8px;
        border-radius: 4px;
        transition: all 0.3s;

        &:hover {
          background: #f0f9ff;
        }

        .legend-color {
          width: 12px;
          height: 12px;
          border-radius: 2px;
        }

        .legend-text {
          font-size: 14px;
          color: #606266;
        }

        .legend-value {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
        }
      }
    }
  }

  .chart-loading,
  .chart-empty {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10;
  }

  .chart-loading {
    z-index: 20;
  }

  .chart-empty {
    .el-empty {
      padding: 40px 0;
    }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .app-chart {
    .chart-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;

      .header-right {
        width: 100%;
        overflow-x: auto;

        .chart-toolbar {
          flex-wrap: nowrap;
          width: max-content;
        }
      }
    }

    .chart-footer {
      flex-direction: column;
      gap: 12px;

      .chart-legend {
        width: 100%;
        overflow-x: auto;
        padding-bottom: 8px;
      }
    }
  }
}
</style>