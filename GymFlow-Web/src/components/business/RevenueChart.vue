<template>
  <div class="revenue-chart">
    <!-- 统计概览 -->
    <div class="stats-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>今日营收</span>
                <el-tag type="success" size="small">实时</el-tag>
              </div>
            </template>
            <div class="card-content">
              <div class="stat-value">¥{{ formatAmount(todayRevenue) }}</div>
              <div class="stat-change">
                <span class="change-percent" :class="getChangeClass(todayChange)">
                  {{ todayChange > 0 ? '+' : '' }}{{ todayChange }}%
                </span>
                较昨日
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>本月营收</span>
              </div>
            </template>
            <div class="card-content">
              <div class="stat-value">¥{{ formatAmount(monthRevenue) }}</div>
              <div class="stat-change">
                <span class="change-percent" :class="getChangeClass(monthChange)">
                  {{ monthChange > 0 ? '+' : '' }}{{ monthChange }}%
                </span>
                较上月
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>本年营收</span>
              </div>
            </template>
            <div class="card-content">
              <div class="stat-value">¥{{ formatAmount(yearRevenue) }}</div>
              <div class="stat-change">
                <span class="change-percent" :class="getChangeClass(yearChange)">
                  {{ yearChange > 0 ? '+' : '' }}{{ yearChange }}%
                </span>
                较去年
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>订单总数</span>
              </div>
            </template>
            <div class="card-content">
              <div class="stat-value">{{ totalOrders }}</div>
              <div class="stat-label">成交 {{ successfulOrders }} 单</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 营收趋势图 -->
      <div class="chart-section">
        <el-card shadow="never">
          <template #header>
            <div class="chart-header">
              <h3 class="chart-title">营收趋势分析</h3>
              <div class="chart-controls">
                <el-radio-group v-model="revenueTrendType" size="small">
                  <el-radio-button label="daily">每日</el-radio-button>
                  <el-radio-button label="weekly">每周</el-radio-button>
                  <el-radio-button label="monthly">每月</el-radio-button>
                </el-radio-group>
                
                <el-date-picker
                  v-model="revenueDateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                  size="small"
                  style="width: 240px; margin-left: 10px;"
                  @change="handleDateRangeChange"
                />
              </div>
            </div>
          </template>
          
          <div class="chart-wrapper" ref="revenueTrendChartRef"></div>
          
          <div class="chart-analysis" v-if="revenueAnalysis">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="analysis-item">
                  <div class="analysis-label">平均日营收</div>
                  <div class="analysis-value">¥{{ formatAmount(revenueAnalysis.avgDaily) }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="analysis-item">
                  <div class="analysis-label">最高日营收</div>
                  <div class="analysis-value">¥{{ formatAmount(revenueAnalysis.maxDaily) }}</div>
                  <div class="analysis-date">{{ revenueAnalysis.maxDate }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="analysis-item">
                  <div class="analysis-label">营收增长</div>
                  <div class="analysis-value" :class="getChangeClass(revenueAnalysis.growth)">
                    {{ revenueAnalysis.growth > 0 ? '+' : '' }}{{ revenueAnalysis.growth }}%
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </div>
      
      <!-- 营收构成分析 -->
      <div class="chart-section">
        <el-card shadow="never">
          <template #header>
            <div class="chart-header">
              <h3 class="chart-title">营收构成分析</h3>
              <div class="chart-controls">
                <el-select
                  v-model="revenueCompositionType"
                  placeholder="选择分析维度"
                  size="small"
                  style="width: 140px;"
                >
                  <el-option label="按产品类型" value="product" />
                  <el-option label="按支付方式" value="payment" />
                  <el-option label="按会员等级" value="member" />
                </el-select>
                
                <el-date-picker
                  v-model="compositionDate"
                  type="month"
                  placeholder="选择月份"
                  value-format="YYYY-MM"
                  size="small"
                  style="width: 120px; margin-left: 10px;"
                  @change="handleCompositionDateChange"
                />
              </div>
            </div>
          </template>
          
          <div class="chart-composition">
            <div class="pie-chart" ref="compositionChartRef"></div>
            <div class="composition-details">
              <el-table
                :data="compositionData"
                style="width: 100%"
                size="small"
                :show-header="false"
              >
                <el-table-column prop="name" width="120">
                  <template #default="{ row }">
                    <div class="category-item">
                      <div class="category-color" :style="{ backgroundColor: row.color }"></div>
                      <span>{{ row.name }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="amount" align="right" width="100">
                  <template #default="{ row }">
                    ¥{{ formatAmount(row.amount) }}
                  </template>
                </el-table-column>
                <el-table-column prop="percentage" align="right" width="80">
                  <template #default="{ row }">
                    {{ row.percentage }}%
                  </template>
                </el-table-column>
                <el-table-column prop="trend" align="right" width="60">
                  <template #default="{ row }">
                    <el-tag
                      :type="row.trend > 0 ? 'success' : row.trend < 0 ? 'danger' : 'info'"
                      size="small"
                    >
                      {{ row.trend > 0 ? '+' : '' }}{{ row.trend }}%
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 支付方式分析 -->
      <div class="chart-section">
        <el-card shadow="never">
          <template #header>
            <div class="chart-header">
              <h3 class="chart-title">支付方式分析</h3>
            </div>
          </template>
          
          <div class="chart-wrapper" ref="paymentChartRef"></div>
          
          <div class="payment-stats">
            <el-row :gutter="20">
              <el-col :span="6" v-for="payment in paymentStats" :key="payment.method">
                <div class="payment-item">
                  <div class="payment-method">
                    <el-icon :size="20" :color="payment.color">
                      <component :is="payment.icon" />
                    </el-icon>
                    <span>{{ payment.method }}</span>
                  </div>
                  <div class="payment-amount">¥{{ formatAmount(payment.amount) }}</div>
                  <div class="payment-percent">{{ payment.percentage }}%</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </div>
      
      <!-- 营收排行榜 -->
      <div class="chart-section">
        <el-card shadow="never">
          <template #header>
            <div class="chart-header">
              <h3 class="chart-title">销售排行榜</h3>
              <div class="chart-controls">
                <el-select
                  v-model="rankingType"
                  placeholder="选择排行类型"
                  size="small"
                  style="width: 140px;"
                >
                  <el-option label="销售顾问" value="consultant" />
                  <el-option label="教练" value="coach" />
                  <el-option label="产品" value="product" />
                </el-select>
              </div>
            </div>
          </template>
          
          <div class="ranking-list">
            <el-table
              :data="rankingData"
              style="width: 100%"
              :row-class-name="tableRowClassName"
            >
              <el-table-column label="排名" width="80" align="center">
                <template #default="{ $index }">
                  <div class="rank-number" :class="getRankClass($index + 1)">
                    {{ $index + 1 }}
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column label="名称" prop="name" />
              
              <el-table-column label="营收金额" prop="amount" align="right" width="140">
                <template #default="{ row }">
                  ¥{{ formatAmount(row.amount) }}
                </template>
              </el-table-column>
              
              <el-table-column label="订单数" prop="orders" align="center" width="100">
                <template #default="{ row }">
                  {{ row.orders }} 单
                </template>
              </el-table-column>
              
              <el-table-column label="占比" prop="percentage" align="right" width="100">
                <template #default="{ row }">
                  {{ row.percentage }}%
                </template>
              </el-table-column>
              
              <el-table-column label="趋势" prop="trend" align="right" width="100">
                <template #default="{ row }">
                  <el-tag
                    :type="row.trend > 0 ? 'success' : row.trend < 0 ? 'danger' : 'info'"
                    size="small"
                  >
                    {{ row.trend > 0 ? '+' : '' }}{{ row.trend }}%
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 数据导出 -->
    <div class="export-section">
      <el-card shadow="never">
        <template #header>
          <div class="export-header">
            <h3 class="export-title">数据导出</h3>
            <el-button
              type="primary"
              @click="exportRevenueData"
              :loading="exporting"
            >
              导出报表
            </el-button>
          </div>
        </template>
        
        <div class="export-options">
          <el-checkbox-group v-model="exportOptions">
            <el-checkbox label="revenue">营收明细</el-checkbox>
            <el-checkbox label="orders">订单明细</el-checkbox>
            <el-checkbox label="products">产品分析</el-checkbox>
            <el-checkbox label="payments">支付分析</el-checkbox>
          </el-checkbox-group>
          
          <el-date-picker
            v-model="exportDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 320px; margin-left: 20px;"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { useChart, getChartColors, getCommonChartOption } from '@/composables/useChart'

// 图表相关
const revenueTrendChartRef = ref<HTMLElement>()
const compositionChartRef = ref<HTMLElement>()
const paymentChartRef = ref<HTMLElement>()

const revenueTrendChart = useChart({ autoResize: true })
const compositionChart = useChart({ autoResize: true })
const paymentChart = useChart({ autoResize: true })

// 响应式数据
const loading = ref(false)
const exporting = ref(false)
const revenueTrendType = ref('daily')
const revenueCompositionType = ref('product')
const rankingType = ref('consultant')
const revenueDateRange = ref<[string, string]>(['', ''])
const compositionDate = ref('2024-01')
const exportDateRange = ref<[string, string]>(['', ''])
const exportOptions = ref(['revenue', 'orders'])

// 统计数据
const todayRevenue = ref(18542.50)
const todayChange = ref(12.5)
const monthRevenue = ref(289654.80)
const monthChange = ref(8.2)
const yearRevenue = ref(2987654.20)
const yearChange = ref(15.3)
const totalOrders = ref(1245)
const successfulOrders = ref(1189)

// 营收构成数据
const compositionData = ref([
  { name: '会员卡', amount: 125000, percentage: 43.2, trend: 8.5, color: '#5470c6' },
  { name: '私教课', amount: 85000, percentage: 29.4, trend: 12.3, color: '#91cc75' },
  { name: '团体课', amount: 45000, percentage: 15.6, trend: -3.2, color: '#fac858' },
  { name: '商品销售', amount: 20000, percentage: 6.9, trend: 5.1, color: '#ee6666' },
  { name: '其他', amount: 14654.8, percentage: 5.1, trend: 2.3, color: '#73c0de' }
])

// 支付方式数据
const paymentStats = ref([
  { method: '微信支付', amount: 145230, percentage: 50.1, color: '#07c160', icon: Wechat },
  { method: '支付宝', amount: 89500, percentage: 30.9, color: '#1677ff', icon: Alipay },
  { method: '银行卡', amount: 28000, percentage: 9.7, color: '#f5222d', icon: CreditCard },
  { method: '现金', amount: 15000, percentage: 5.2, color: '#fa8c16', icon: Money },
  { method: '其他', amount: 11924.8, percentage: 4.1, color: '#722ed1', icon: 'More' }
])

// 排行榜数据
const rankingData = ref([
  { name: '张顾问', amount: 125000, orders: 45, percentage: 15.2, trend: 12.5 },
  { name: '李顾问', amount: 98500, orders: 38, percentage: 11.9, trend: 8.3 },
  { name: '王教练', amount: 87600, orders: 32, percentage: 10.6, trend: 15.2 },
  { name: '刘顾问', amount: 74500, orders: 29, percentage: 9.0, trend: -2.1 },
  { name: '陈教练', amount: 68900, orders: 25, percentage: 8.3, trend: 5.7 },
  { name: '赵顾问', amount: 56700, orders: 22, percentage: 6.9, trend: 3.4 },
  { name: '周教练', amount: 48900, orders: 18, percentage: 5.9, trend: 7.8 },
  { name: '吴顾问', amount: 42300, orders: 16, percentage: 5.1, trend: -1.5 },
  { name: '郑教练', amount: 38500, orders: 14, percentage: 4.7, trend: 4.2 },
  { name: '孙顾问', amount: 32400, orders: 12, percentage: 3.9, trend: 2.1 }
])

// 计算属性
const revenueAnalysis = computed(() => {
  // 这里应该基于实际数据计算分析结果
  return {
    avgDaily: 8542.30,
    maxDaily: 24567.80,
    maxDate: '2024-01-15',
    growth: 12.5
  }
})

// 方法
const formatAmount = (amount: number) => {
  return amount.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const getChangeClass = (change: number) => {
  if (change > 0) return 'positive'
  if (change < 0) return 'negative'
  return 'neutral'
}

const handleDateRangeChange = (range: [string, string]) => {
  revenueDateRange.value = range
  loadRevenueData()
}

const handleCompositionDateChange = (date: string) => {
  compositionDate.value = date
  loadCompositionData()
}

const getRankClass = (rank: number) => {
  if (rank === 1) return 'rank-first'
  if (rank === 2) return 'rank-second'
  if (rank === 3) return 'rank-third'
  return 'rank-other'
}

const tableRowClassName = ({ rowIndex }: { rowIndex: number }) => {
  if (rowIndex < 3) {
    return `highlight-row rank-${rowIndex + 1}`
  }
  return ''
}

// 加载营收数据
const loadRevenueData = async () => {
  loading.value = true
  try {
    // 这里应该调用API获取营收数据
    // 暂时使用模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    renderRevenueCharts()
  } catch (error) {
    console.error('加载营收数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载构成数据
const loadCompositionData = async () => {
  // 这里应该根据选择的日期和维度加载数据
  renderCompositionChart()
}

// 渲染营收趋势图
const renderRevenueCharts = () => {
  // 模拟数据
  const dates = []
  const revenueData = []
  const orderData = []
  
  const startDate = new Date('2024-01-01')
  for (let i = 0; i < 30; i++) {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + i)
    dates.push(date.toISOString().split('T')[0].substring(5)) // MM-DD格式
    
    // 模拟营收数据（有周末效应）
    const isWeekend = date.getDay() === 0 || date.getDay() === 6
    const baseRevenue = isWeekend ? 12000 : 8000
    const randomFactor = 0.3 + Math.random() * 0.7
    revenueData.push(Math.round(baseRevenue * randomFactor))
    
    // 模拟订单数据
    orderData.push(Math.round(baseRevenue * randomFactor / 200))
  }
  
  // 营收趋势图
  const revenueOption = {
    ...getCommonChartOption(),
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params: any) {
        let html = `<div style="margin-bottom: 5px;">${params[0].axisValue}</div>`
        params.forEach((param: any) => {
          const value = param.value
          const seriesName = param.seriesName
          const color = param.color
          const icon = '●'
          
          html += `
            <div style="display: flex; align-items: center; margin: 3px 0;">
              <span style="display: inline-block; width: 10px; height: 10px; border-radius: 50%; background-color: ${color}; margin-right: 8px;"></span>
              <span style="flex: 1;">${seriesName}</span>
              <span style="font-weight: bold;">¥${formatAmount(value)}</span>
            </div>
          `
        })
        return html
      }
    },
    legend: {
      data: ['营收金额', '订单数量']
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '营收金额(元)',
        position: 'left'
      },
      {
        type: 'value',
        name: '订单数量(单)',
        position: 'right'
      }
    ],
    series: [
      {
        name: '营收金额',
        type: 'bar',
        data: revenueData,
        yAxisIndex: 0,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      },
      {
        name: '订单数量',
        type: 'line',
        data: orderData,
        yAxisIndex: 1,
        smooth: true,
        itemStyle: {
          color: '#ee6666'
        },
        lineStyle: {
          width: 3
        },
        symbolSize: 8
      }
    ]
  }
  
  revenueTrendChart.setChartOption(revenueOption)
  
  // 支付方式分析图
  const paymentOption = {
    ...getCommonChartOption(),
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: paymentStats.value.map(p => p.method)
    },
    yAxis: {
      type: 'value',
      name: '金额(元)'
    },
    series: [
      {
        name: '支付金额',
        type: 'bar',
        data: paymentStats.value.map(p => p.amount),
        itemStyle: {
          color: (params: any) => {
            const colors = ['#07c160', '#1677ff', '#f5222d', '#fa8c16', '#722ed1']
            return colors[params.dataIndex] || '#5470c6'
          }
        },
        label: {
          show: true,
          position: 'top',
          formatter: (params: any) => {
            const item = paymentStats.value[params.dataIndex]
            return `${item.percentage}%`
          }
        }
      }
    ]
  }
  
  paymentChart.setChartOption(paymentOption)
}

// 渲染构成分析图
const renderCompositionChart = () => {
  const compositionOption = {
    ...getCommonChartOption(),
    tooltip: {
      trigger: 'item',
      formatter: '{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center',
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '营收构成',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        data: compositionData.value.map(item => ({
          name: item.name,
          value: item.amount,
          itemStyle: {
            color: item.color
          }
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          formatter: '{b}: {d}%'
        }
      }
    ]
  }
  
  compositionChart.setChartOption(compositionOption)
}

// 导出数据
const exportRevenueData = async () => {
  if (exportOptions.value.length === 0) {
    ElMessage.warning('请选择要导出的内容')
    return
  }
  
  if (!exportDateRange.value[0] || !exportDateRange.value[1]) {
    ElMessage.warning('请选择导出日期范围')
    return
  }
  
  exporting.value = true
  
  try {
    // 这里应该实现数据导出逻辑
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    ElMessage.success({
      message: '数据导出成功',
      duration: 3000,
      showClose: true
    })
    
    // 模拟下载文件
    const blob = new Blob(['模拟的导出数据'], { type: 'text/plain' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `营收报表_${exportDateRange.value[0]}_${exportDateRange.value[1]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('导出数据失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 初始化图表
const initCharts = () => {
  revenueTrendChart.chartContainer = revenueTrendChartRef
  compositionChart.chartContainer = compositionChartRef
  paymentChart.chartContainer = paymentChartRef
}

// 生命周期
onMounted(() => {
  initCharts()
  
  // 设置默认日期范围（最近30天）
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(endDate.getDate() - 30)
  
  revenueDateRange.value = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
  
  exportDateRange.value = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
  
  // 加载数据
  loadRevenueData()
  renderCompositionChart()
})

onUnmounted(() => {
  revenueTrendChart.disposeChart()
  compositionChart.disposeChart()
  paymentChart.disposeChart()
})
</script>

<style scoped lang="scss">
.revenue-chart {
  .stats-overview {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .card-content {
      .stat-value {
        font-size: 24px;
        font-weight: bold;
        margin-bottom: 8px;
        color: #303133;
      }
      
      .stat-change {
        font-size: 12px;
        color: #909399;
        
        .change-percent {
          font-weight: bold;
          
          &.positive {
            color: #67c23a;
          }
          
          &.negative {
            color: #f56c6c;
          }
          
          &.neutral {
            color: #909399;
          }
        }
      }
      
      .stat-label {
        font-size: 12px;
        color: #909399;
      }
    }
  }
  
  .charts-container {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    margin-bottom: 20px;
    
    .chart-section {
      &:nth-child(3),
      &:nth-child(4) {
        grid-column: span 2;
      }
    }
  }
  
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .chart-title {
      margin: 0;
      font-size: 16px;
      color: #303133;
      font-weight: 500;
    }
  }
  
  .chart-wrapper {
    height: 300px;
  }
  
  .chart-analysis {
    margin-top: 20px;
    padding: 15px;
    background-color: #f8f9fa;
    border-radius: 4px;
    
    .analysis-item {
      text-align: center;
      
      .analysis-label {
        font-size: 12px;
        color: #909399;
        margin-bottom: 4px;
      }
      
      .analysis-value {
        font-size: 18px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 4px;
        
        &.positive {
          color: #67c23a;
        }
        
        &.negative {
          color: #f56c6c;
        }
      }
      
      .analysis-date {
        font-size: 12px;
        color: #909399;
      }
    }
  }
  
  .chart-composition {
    display: flex;
    height: 300px;
    
    .pie-chart {
      flex: 1;
      height: 100%;
    }
    
    .composition-details {
      flex: 1;
      padding-left: 20px;
      
      .category-item {
        display: flex;
        align-items: center;
        
        .category-color {
          width: 12px;
          height: 12px;
          border-radius: 2px;
          margin-right: 8px;
        }
      }
    }
  }
  
  .payment-stats {
    margin-top: 20px;
    
    .payment-item {
      padding: 15px;
      background-color: #f8f9fa;
      border-radius: 4px;
      text-align: center;
      
      .payment-method {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        margin-bottom: 8px;
        font-size: 14px;
        color: #606266;
      }
      
      .payment-amount {
        font-size: 18px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .payment-percent {
        font-size: 12px;
        color: #909399;
      }
    }
  }
  
  .ranking-list {
    :deep(.el-table) {
      .highlight-row {
        background-color: #f8f9fa;
      }
      
      .rank-first {
        background-color: #fff7e6;
      }
      
      .rank-second {
        background-color: #f9f9f9;
      }
      
      .rank-third {
        background-color: #f0f9ff;
      }
    }
    
    .rank-number {
      width: 28px;
      height: 28px;
      line-height: 28px;
      border-radius: 50%;
      text-align: center;
      font-weight: bold;
      margin: 0 auto;
      
      &.rank-first {
        background-color: #faad14;
        color: #ffffff;
      }
      
      &.rank-second {
        background-color: #d9d9d9;
        color: #ffffff;
      }
      
      &.rank-third {
        background-color: #ff9c6e;
        color: #ffffff;
      }
      
      &.rank-other {
        background-color: #f5f5f5;
        color: #595959;
      }
    }
  }
  
  .export-section {
    .export-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .export-title {
        margin: 0;
        font-size: 16px;
        color: #303133;
        font-weight: 500;
      }
    }
    
    .export-options {
      display: flex;
      align-items: center;
    }
  }
}

@media (max-width: 1200px) {
  .revenue-chart {
    .charts-container {
      grid-template-columns: 1fr;
      
      .chart-section {
        grid-column: span 1 !important;
      }
    }
    
    .chart-composition {
      flex-direction: column;
      height: auto;
      
      .composition-details {
        padding-left: 0;
        padding-top: 20px;
      }
    }
  }
}
</style>