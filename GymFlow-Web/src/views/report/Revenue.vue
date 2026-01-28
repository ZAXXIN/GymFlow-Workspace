<template>
  <div class="revenue-report-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/report' }">报表分析</el-breadcrumb-item>
          <el-breadcrumb-item>营收报表</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">营收报表分析</h1>
      </div>
      <div class="header-actions">
        <el-button @click="handleExport('excel')">
          <el-icon><Download /></el-icon>
          导出Excel
        </el-button>
        <el-button @click="handleExport('pdf')">
          <el-icon><Document /></el-icon>
          导出PDF
        </el-button>
        <el-button type="primary" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-content">
        <div class="filter-row">
          <div class="filter-group">
            <span class="filter-label">时间范围：</span>
            <el-radio-group v-model="filterParams.timeType" @change="handleTimeTypeChange">
              <el-radio-button label="today">今日</el-radio-button>
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
              <el-radio-button label="quarter">本季</el-radio-button>
              <el-radio-button label="year">本年</el-radio-button>
              <el-radio-button label="custom">自定义</el-radio-button>
            </el-radio-group>
            
            <el-date-picker
              v-model="filterParams.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 300px; margin-left: 20px"
              :disabled="filterParams.timeType !== 'custom'"
            />
          </div>
        </div>
        
        <div class="filter-row">
          <div class="filter-group">
            <span class="filter-label">营收类型：</span>
            <el-checkbox-group v-model="filterParams.revenueTypes">
              <el-checkbox label="membership">会员卡</el-checkbox>
              <el-checkbox label="course">课程收入</el-checkbox>
              <el-checkbox label="personal">私教收入</el-checkbox>
              <el-checkbox label="product">商品销售</el-checkbox>
            </el-checkbox-group>
          </div>
        </div>
        
        <div class="filter-row">
          <div class="filter-group">
            <span class="filter-label">支付方式：</span>
            <el-select
              v-model="filterParams.paymentMethods"
              multiple
              placeholder="全部支付方式"
              style="width: 300px; margin-right: 20px"
              clearable
            >
              <el-option label="微信支付" value="WECHAT" />
              <el-option label="支付宝" value="ALIPAY" />
              <el-option label="现金" value="CASH" />
              <el-option label="刷卡" value="CARD" />
              <el-option label="银行转账" value="BANK_TRANSFER" />
            </el-select>
            
            <span class="filter-label">统计维度：</span>
            <el-select
              v-model="filterParams.dimension"
              placeholder="选择统计维度"
              style="width: 120px"
            >
              <el-option label="按天" value="daily" />
              <el-option label="按周" value="weekly" />
              <el-option label="按月" value="monthly" />
            </el-select>
          </div>
        </div>
        
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total-revenue">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatCurrency(stats.totalRevenue) }}</div>
              <div class="stat-label">总营收</div>
            </div>
            <div class="stat-trend up">
              <el-icon><Top /></el-icon>
              <span>12.5%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon order-count">
              <el-icon><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.orderCount }}</div>
              <div class="stat-label">订单总数</div>
            </div>
            <div class="stat-trend up">
              <el-icon><Top /></el-icon>
              <span>8.3%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon avg-order">
              <el-icon><Histogram /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatCurrency(stats.avgOrderValue) }}</div>
              <div class="stat-label">客单价</div>
            </div>
            <div class="stat-trend down">
              <el-icon><Bottom /></el-icon>
              <span>2.1%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon new-members">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.newMembers }}</div>
              <div class="stat-label">新增会员</div>
            </div>
            <div class="stat-trend up">
              <el-icon><Top /></el-icon>
              <span>15.7%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表分析 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>营收趋势分析</span>
              <div class="chart-actions">
                <el-select v-model="trendChartType" size="small" style="width: 100px">
                  <el-option label="折线图" value="line" />
                  <el-option label="柱状图" value="bar" />
                  <el-option label="面积图" value="area" />
                </el-select>
              </div>
            </div>
          </template>
          <div ref="revenueTrendChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>营收构成分析</span>
              <div class="chart-actions">
                <el-select v-model="compositionChartType" size="small" style="width: 100px">
                  <el-option label="饼图" value="pie" />
                  <el-option label="环形图" value="ring" />
                  <el-option label="玫瑰图" value="rose" />
                </el-select>
              </div>
            </div>
          </template>
          <div ref="revenueCompositionChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 支付方式分析 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>支付方式分析</span>
            </div>
          </template>
          <div ref="paymentChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>营收明细数据</span>
          <div class="table-actions">
            <el-button size="small" @click="toggleExpandAll">
              {{ isAllExpanded ? '收起全部' : '展开全部' }}
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="revenueData"
        style="width: 100%"
        row-key="date"
        :default-expand-all="isAllExpanded"
        v-loading="loading"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div v-if="row.children && row.children.length > 0" class="expand-table">
              <el-table :data="row.children" style="width: 100%">
                <el-table-column prop="time" label="时间" width="120" />
                <el-table-column prop="orderNo" label="订单号" width="180" />
                <el-table-column prop="memberName" label="会员" width="100" />
                <el-table-column prop="type" label="类型" width="100">
                  <template #default="{ row: childRow }">
                    <el-tag :type="getRevenueTypeTag(childRow.type)" size="small">
                      {{ getRevenueTypeText(childRow.type) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="amount" label="金额" width="120" align="right">
                  <template #default="{ row: childRow }">
                    {{ formatCurrency(childRow.amount) }}
                  </template>
                </el-table-column>
                <el-table-column prop="paymentMethod" label="支付方式" width="120">
                  <template #default="{ row: childRow }">
                    {{ getPaymentMethodText(childRow.paymentMethod) }}
                  </template>
                </el-table-column>
                <el-table-column prop="operator" label="操作员" width="100" />
                <el-table-column prop="notes" label="备注" />
              </el-table>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="date" label="日期" width="120" fixed />
        <el-table-column prop="membershipRevenue" label="会员卡收入" width="120" align="right">
          <template #default="{ row }">
            {{ formatCurrency(row.membershipRevenue) }}
          </template>
        </el-table-column>
        <el-table-column prop="courseRevenue" label="课程收入" width="120" align="right">
          <template #default="{ row }">
            {{ formatCurrency(row.courseRevenue) }}
          </template>
        </el-table-column>
        <el-table-column prop="personalRevenue" label="私教收入" width="120" align="right">
          <template #default="{ row }">
            {{ formatCurrency(row.personalRevenue) }}
          </template>
        </el-table-column>
        <el-table-column prop="productRevenue" label="商品收入" width="120" align="right">
          <template #default="{ row }">
            {{ formatCurrency(row.productRevenue) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalRevenue" label="合计" width="120" align="right">
          <template #default="{ row }">
            <span class="total-amount">{{ formatCurrency(row.totalRevenue) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数" width="100" align="center" />
        <el-table-column prop="avgOrderValue" label="客单价" width="120" align="right">
          <template #default="{ row }">
            {{ formatCurrency(row.avgOrderValue) }}
          </template>
        </el-table-column>
        <el-table-column prop="growthRate" label="增长率" width="100" align="center">
          <template #default="{ row }">
            <span :class="getGrowthClass(row.growthRate)">
              {{ row.growthRate > 0 ? '+' : '' }}{{ (row.growthRate * 100).toFixed(1) }}%
            </span>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="table-footer">
        <div class="summary-row">
          <span class="summary-label">总计：</span>
          <span class="summary-value">{{ formatCurrency(stats.totalRevenue) }}</span>
          <span class="summary-label">订单总数：</span>
          <span class="summary-value">{{ stats.orderCount }}</span>
          <span class="summary-label">平均客单价：</span>
          <span class="summary-value">{{ formatCurrency(stats.avgOrderValue) }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'

// 筛选参数
const filterParams = reactive({
  timeType: 'month',
  dateRange: [] as string[],
  revenueTypes: ['membership', 'course', 'personal', 'product'],
  paymentMethods: [] as string[],
  dimension: 'daily'
})

// 统计数据
const stats = reactive({
  totalRevenue: 0,
  orderCount: 0,
  avgOrderValue: 0,
  newMembers: 0
})

// 图表引用
const revenueTrendChart = ref<HTMLElement>()
const revenueCompositionChart = ref<HTMLElement>()
const paymentChart = ref<HTMLElement>()

// 图表类型
const trendChartType = ref('line')
const compositionChartType = ref('pie')

// 表格数据
const revenueData = ref<any[]>([])
const loading = ref(false)
const isAllExpanded = ref(false)

// 格式化货币
const formatCurrency = (amount: number) => {
  return `¥${amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

// 获取营收类型标签样式
const getRevenueTypeTag = (type: string) => {
  const map: Record<string, string> = {
    membership: 'success',
    course: 'primary',
    personal: 'warning',
    product: 'info'
  }
  return map[type] || 'info'
}

// 获取营收类型文本
const getRevenueTypeText = (type: string) => {
  const map: Record<string, string> = {
    membership: '会员卡',
    course: '课程',
    personal: '私教',
    product: '商品'
  }
  return map[type] || '其他'
}

// 获取支付方式文本
const getPaymentMethodText = (method: string) => {
  const map: Record<string, string> = {
    WECHAT: '微信支付',
    ALIPAY: '支付宝',
    CASH: '现金',
    CARD: '刷卡',
    BANK_TRANSFER: '银行转账'
  }
  return map[method] || method
}

// 获取增长率样式
const getGrowthClass = (rate: number) => {
  return rate > 0 ? 'growth-up' : rate < 0 ? 'growth-down' : 'growth-neutral'
}

// 时间类型变化
const handleTimeTypeChange = (type: string) => {
  const today = new Date()
  const startOfWeek = new Date(today.setDate(today.getDate() - today.getDay() + 1))
  const startOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
  const startOfYear = new Date(today.getFullYear(), 0, 1)
  
  switch (type) {
    case 'today':
      const todayStr = new Date().toISOString().split('T')[0]
      filterParams.dateRange = [todayStr, todayStr]
      break
    case 'week':
      filterParams.dateRange = [
        startOfWeek.toISOString().split('T')[0],
        new Date().toISOString().split('T')[0]
      ]
      break
    case 'month':
      filterParams.dateRange = [
        startOfMonth.toISOString().split('T')[0],
        new Date().toISOString().split('T')[0]
      ]
      break
    case 'year':
      filterParams.dateRange = [
        startOfYear.toISOString().split('T')[0],
        new Date().toISOString().split('T')[0]
      ]
      break
  }
  
  if (type !== 'custom') {
    handleSearch()
  }
}

// 查询数据
const handleSearch = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 更新统计数据
    stats.totalRevenue = 156789.50
    stats.orderCount = 245
    stats.avgOrderValue = 639.96
    stats.newMembers = 38
    
    // 更新表格数据
    revenueData.value = [
      {
        date: '2023-06-01',
        membershipRevenue: 28900,
        courseRevenue: 56700,
        personalRevenue: 45800,
        productRevenue: 25389.5,
        totalRevenue: 156789.5,
        orderCount: 45,
        avgOrderValue: 3484.21,
        growthRate: 0.125,
        children: [
          {
            time: '09:30',
            orderNo: 'ORD20230601001',
            memberName: '张三',
            type: 'membership',
            amount: 4999,
            paymentMethod: 'WECHAT',
            operator: '张前台',
            notes: '年卡会员'
          },
          {
            time: '14:20',
            orderNo: 'ORD20230601002',
            memberName: '李四',
            type: 'personal',
            amount: 5000,
            paymentMethod: 'ALIPAY',
            operator: '李教练',
            notes: '私教课程10节'
          }
        ]
      },
      {
        date: '2023-05-31',
        membershipRevenue: 23900,
        courseRevenue: 51200,
        personalRevenue: 42000,
        productRevenue: 22150,
        totalRevenue: 139250,
        orderCount: 38,
        avgOrderValue: 3664.47,
        growthRate: 0.083,
        children: []
      }
    ]
    
    // 初始化图表
    initCharts()
    
    ElMessage.success('数据加载成功')
  } catch (error) {
    console.error('数据加载失败:', error)
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选
const handleReset = () => {
  filterParams.timeType = 'month'
  filterParams.dateRange = []
  filterParams.revenueTypes = ['membership', 'course', 'personal', 'product']
  filterParams.paymentMethods = []
  filterParams.dimension = 'daily'
  handleSearch()
}

// 刷新数据
const handleRefresh = () => {
  handleSearch()
}

// 导出数据
const handleExport = (type: string) => {
  ElMessage.info(`${type === 'excel' ? 'Excel' : 'PDF'}导出功能开发中`)
}

// 切换展开全部
const toggleExpandAll = () => {
  isAllExpanded.value = !isAllExpanded.value
}

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    // 营收趋势图表
    if (revenueTrendChart.value) {
      const trendChart = echarts.init(revenueTrendChart.value)
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: (params: any) => {
            let html = `<div style="font-weight: bold; margin-bottom: 5px">${params[0].axisValue}</div>`
            params.forEach((item: any) => {
              html += `
                <div style="display: flex; align-items: center; margin: 5px 0">
                  <span style="display: inline-block; width: 10px; height: 10px; border-radius: 50%; background: ${item.color}; margin-right: 5px"></span>
                  <span style="flex: 1">${item.seriesName}</span>
                  <span style="font-weight: bold; color: #303133">${formatCurrency(item.value)}</span>
                </div>
              `
            })
            return html
          }
        },
        legend: {
          data: ['总营收', '会员卡收入', '课程收入', '私教收入', '商品收入']
        },
        xAxis: {
          type: 'category',
          data: ['6/1', '6/2', '6/3', '6/4', '6/5', '6/6', '6/7', '6/8', '6/9', '6/10']
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: (value: number) => formatCurrency(value)
          }
        },
        series: [
          {
            name: '总营收',
            type: trendChartType.value,
            data: [156789, 142300, 168900, 189200, 174500, 193400, 201500, 187600, 195400, 210800],
            smooth: true,
            lineStyle: { width: 3 },
            itemStyle: { color: '#409eff' }
          },
          {
            name: '会员卡收入',
            type: trendChartType.value,
            data: [28900, 25600, 31200, 34500, 29800, 36700, 38900, 32400, 35600, 41200],
            smooth: true,
            itemStyle: { color: '#67c23a' }
          },
          {
            name: '课程收入',
            type: trendChartType.value,
            data: [56700, 51200, 58900, 63400, 59800, 65700, 68900, 62400, 65600, 71200],
            smooth: true,
            itemStyle: { color: '#e6a23c' }
          }
        ]
      }
      trendChart.setOption(option)
      
      // 监听窗口变化
      window.addEventListener('resize', () => {
        trendChart.resize()
      })
    }
    
    // 营收构成图表
    if (revenueCompositionChart.value) {
      const compositionChart = echarts.init(revenueCompositionChart.value)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: (params: any) => {
            return `${params.name}<br/>${params.marker} ${params.percent}% (${formatCurrency(params.value)})`
          }
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'center'
        },
        series: [
          {
            name: '营收构成',
            type: compositionChartType.value,
            radius: compositionChartType.value === 'ring' ? ['40%', '70%'] : '70%',
            data: [
              { value: 28900, name: '会员卡收入', itemStyle: { color: '#67c23a' } },
              { value: 56700, name: '课程收入', itemStyle: { color: '#e6a23c' } },
              { value: 45800, name: '私教收入', itemStyle: { color: '#409eff' } },
              { value: 25389.5, name: '商品收入', itemStyle: { color: '#f56c6c' } }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      compositionChart.setOption(option)
      
      window.addEventListener('resize', () => {
        compositionChart.resize()
      })
    }
    
    // 支付方式图表
    if (paymentChart.value) {
      const paymentChartInstance = echarts.init(paymentChart.value)
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['微信支付', '支付宝', '现金', '刷卡', '银行转账']
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: (value: number) => formatCurrency(value)
          }
        },
        series: [
          {
            name: '支付金额',
            type: 'bar',
            data: [85600, 48900, 15300, 4200, 2789.5],
            itemStyle: {
              color: (params: any) => {
                const colors = ['#07c160', '#1677ff', '#f56c6c', '#e6a23c', '#909399']
                return colors[params.dataIndex] || '#409eff'
              }
            },
            label: {
              show: true,
              position: 'top',
              formatter: (params: any) => formatCurrency(params.value)
            }
          }
        ]
      }
      paymentChartInstance.setOption(option)
      
      window.addEventListener('resize', () => {
        paymentChartInstance.resize()
      })
    }
  })
}

// 监听图表类型变化
watch([trendChartType, compositionChartType], () => {
  initCharts()
})

// 初始化
onMounted(() => {
  // 设置默认日期范围（本月）
  const today = new Date()
  const startOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
  filterParams.dateRange = [
    startOfMonth.toISOString().split('T')[0],
    today.toISOString().split('T')[0]
  ]
  
  handleSearch()
})
</script>

<style scoped lang="scss">
.revenue-report-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  .header-left {
    .page-title {
      margin: 10px 0 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
  }

  .header-actions {
    display: flex;
    gap: 10px;
  }
}

.filter-card {
  margin-bottom: 20px;

  .filter-content {
    .filter-row {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }

      .filter-group {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 10px;

        .filter-label {
          font-size: 14px;
          color: #606266;
          white-space: nowrap;
        }
      }
    }

    .filter-actions {
      display: flex;
      justify-content: center;
      padding-top: 20px;
      border-top: 1px solid #ebeef5;
    }
  }
}

.stats-overview {
  margin-bottom: 20px;
}

.stat-card {
  border: 1px solid #ebeef5;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .stat-content {
    display: flex;
    align-items: center;
    padding: 10px 0;

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20px;
      font-size: 28px;

      &.total-revenue {
        background: rgba(64, 158, 255, 0.1);
        color: #409eff;
      }

      &.order-count {
        background: rgba(103, 194, 58, 0.1);
        color: #67c23a;
      }

      &.avg-order {
        background: rgba(230, 162, 60, 0.1);
        color: #e6a23c;
      }

      &.new-members {
        background: rgba(245, 108, 108, 0.1);
        color: #f56c6c;
      }
    }

    .stat-info {
      flex: 1;

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
        line-height: 1;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }

    .stat-trend {
      display: flex;
      align-items: center;
      font-size: 14px;
      font-weight: 500;

      &.up {
        color: #67c23a;
      }

      &.down {
        color: #f56c6c;
      }

      .el-icon {
        margin-right: 4px;
      }
    }
  }
}

.chart-section {
  margin-bottom: 20px;
}

.chart-card {
  border: 1px solid #ebeef5;
  height: 100%;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .chart-actions {
      display: flex;
      gap: 10px;
    }
  }
}

.table-card {
  border: 1px solid #ebeef5;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .table-actions {
      display: flex;
      gap: 10px;
    }
  }

  .expand-table {
    padding: 10px;
    background: #f9fafc;
    border-radius: 4px;
    margin: 10px 0;
  }

  .total-amount {
    font-weight: 600;
    color: #409eff;
  }

  .growth-up {
    color: #67c23a;
    font-weight: 500;
  }

  .growth-down {
    color: #f56c6c;
    font-weight: 500;
  }

  .growth-neutral {
    color: #909399;
  }

  .table-footer {
    padding: 20px;
    border-top: 1px solid #ebeef5;
    background: #f9fafc;

    .summary-row {
      display: flex;
      align-items: center;
      gap: 30px;
      font-size: 14px;

      .summary-label {
        color: #606266;
      }

      .summary-value {
        font-weight: 600;
        color: #303133;
        min-width: 100px;
      }
    }
  }
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 20px;
}

:deep(.el-checkbox-group) {
  display: flex;
  gap: 15px;
}

@media (max-width: 768px) {
  .filter-row .filter-group {
    flex-direction: column;
    align-items: flex-start !important;
    
    .filter-label {
      margin-bottom: 8px;
    }
  }
  
  .stat-content {
    flex-direction: column;
    text-align: center;
    
    .stat-icon {
      margin-right: 0 !important;
      margin-bottom: 15px;
    }
    
    .stat-trend {
      margin-top: 10px;
    }
  }
  
  .summary-row {
    flex-direction: column;
    gap: 10px !important;
  }
}
</style>