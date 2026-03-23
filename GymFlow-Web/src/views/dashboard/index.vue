<template>
  <div class="dashboard-container">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <h1 class="page-title">仪表盘</h1>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="small"
          @change="handleDateChange"
        />
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stats-card">
        <template #header>
          <div class="stats-card-header">
            <el-icon class="stats-icon member"><User /></el-icon>
            <span class="stats-title">总会员数</span>
          </div>
        </template>
        <div class="stats-content">
          <div class="stats-number">{{ stats?.totalMembers || 0 }}</div>
          <div class="stats-trend">
            <el-icon v-if="memberTrend > 0" class="trend-up"><Top /></el-icon>
            <el-icon v-else-if="memberTrend < 0" class="trend-down"><Bottom /></el-icon>
            <span class="trend-text">{{ Math.abs(memberTrend) }}% 较上月</span>
          </div>
        </div>
      </el-card>
      
      <el-card class="stats-card">
        <template #header>
          <div class="stats-card-header">
            <el-icon class="stats-icon coach"><UserFilled /></el-icon>
            <span class="stats-title">教练数</span>
          </div>
        </template>
        <div class="stats-content">
          <div class="stats-number">{{ stats?.totalCoaches || 0 }}</div>
          <div class="stats-trend">
            <el-icon v-if="coachTrend > 0" class="trend-up"><Top /></el-icon>
            <el-icon v-else-if="coachTrend < 0" class="trend-down"><Bottom /></el-icon>
            <span class="trend-text">{{ Math.abs(coachTrend) }}% 较上月</span>
          </div>
        </div>
      </el-card>
      
      <el-card class="stats-card">
        <template #header>
          <div class="stats-card-header">
            <el-icon class="stats-icon revenue"><Money /></el-icon>
            <span class="stats-title">今日营收</span>
          </div>
        </template>
        <div class="stats-content">
          <div class="stats-number">¥{{ formatRevenue(stats?.todayRevenue || 0) }}</div>
          <div class="stats-trend">
            <el-icon v-if="revenueTrendValue > 0" class="trend-up"><Top /></el-icon>
            <el-icon v-else-if="revenueTrendValue < 0" class="trend-down"><Bottom /></el-icon>
            <span class="trend-text">{{ Math.abs(revenueTrendValue) }}% 较昨日</span>
          </div>
        </div>
      </el-card>
      
      <el-card class="stats-card">
        <template #header>
          <div class="stats-card-header">
            <el-icon class="stats-icon attendance"><Check /></el-icon>
            <span class="stats-title">今日签到</span>
          </div>
        </template>
        <div class="stats-content">
          <div class="stats-number">{{ stats?.todayCheckIns || 0 }}</div>
          <div class="stats-trend">
            <el-icon v-if="attendanceTrend > 0" class="trend-up"><Top /></el-icon>
            <el-icon v-else-if="attendanceTrend < 0" class="trend-down"><Bottom /></el-icon>
            <span class="trend-text">{{ Math.abs(attendanceTrend) }}% 较昨日</span>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 营收趋势 -->
        <el-col :xs="24" :sm="24" :md="16">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3 class="chart-title">营收趋势</h3>
                <div class="chart-actions">
                  <el-radio-group v-model="revenuePeriod" size="small" @change="handlePeriodChange">
                    <el-radio-button label="week">周</el-radio-button>
                    <el-radio-button label="month">月</el-radio-button>
                    <el-radio-button label="year">年</el-radio-button>
                  </el-radio-group>
                </div>
              </div>
            </template>
            <div class="chart-container">
              <div ref="revenueChartRef" class="chart" style="height: 300px;"></div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 课程分类分布 -->
        <el-col :xs="24" :sm="24" :md="8">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3 class="chart-title">课程分类</h3>
              </div>
            </template>
            <div class="chart-container">
              <div ref="courseChartRef" class="chart" style="height: 300px;"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 今日课程 -->
    <div class="today-courses">
      <el-card>
        <template #header>
          <div class="courses-header">
            <h3>今日课程</h3>
            <el-button type="text" @click="viewAllCourses">
              查看全部
            </el-button>
          </div>
        </template>
        <el-table :data="todayCourses" style="width: 100%" v-loading="coursesLoading">
          <el-table-column prop="courseNo" label="课程编号" width="120" />
          <el-table-column prop="name" label="课程名称" />
          <el-table-column prop="coachName" label="教练" width="100" />
          <el-table-column prop="startTime" label="时间" width="120">
            <template #default="{ row }">
              {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="currentBookings" label="预约/容量" width="120">
            <template #default="{ row }">
              {{ row.currentBookings }}/{{ row.capacity }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button type="text" size="small" @click="viewCourseDetail(row.id)">
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Refresh, Top, Bottom, User, UserFilled, Money, Check, Calendar, ShoppingCart } from '@element-plus/icons-vue'
import { useDashboardStore } from '@/stores/dashboard'

const router = useRouter()
const dashboardStore = useDashboardStore()

// 图表引用
const revenueChartRef = ref<HTMLElement>()
const courseChartRef = ref<HTMLElement>()
let revenueChart: echarts.ECharts | null = null
let courseChart: echarts.ECharts | null = null

// 日期范围
const dateRange = ref<[Date, Date]>([
  new Date(new Date().setDate(new Date().getDate() - 6)),
  new Date()
])

// 状态
const loading = computed(() => dashboardStore.loading)
const coursesLoading = computed(() => dashboardStore.coursesLoading)
const stats = computed(() => dashboardStore.stats)
const revenueTrend = computed(() => dashboardStore.revenueTrend)
const courseCategories = computed(() => dashboardStore.courseCategories)
const todayCourses = computed(() => dashboardStore.todayCourses)
const quickStats = computed(() => dashboardStore.quickStats)

// 趋势数据
const memberTrend = computed(() => dashboardStore.memberTrend)
const coachTrend = computed(() => dashboardStore.coachTrend)
const revenueTrendValue = computed(() => dashboardStore.revenueTrendValue)
const attendanceTrend = computed(() => dashboardStore.attendanceTrend)

// 营收周期
const revenuePeriod = ref('week')

// 格式化函数
const formatRevenue = (value: number) => {
  return value.toFixed(2)
}

// 初始化数据
const initData = async () => {
  try {
    await Promise.all([
      dashboardStore.fetchDashboardStats(),
      dashboardStore.fetchRevenueTrend(revenuePeriod.value),
      dashboardStore.fetchCourseCategoryStats(),
      dashboardStore.fetchTodayCourses(),
      dashboardStore.fetchQuickStats()
    ])
    
    // 等待 DOM 更新后再初始化图表
    await nextTick()
    initRevenueChart()
    initCourseChart()
  } catch (error) {
    console.error('初始化仪表盘数据失败:', error)
  }
}

// 刷新数据
const refreshData = async () => {
  await dashboardStore.refreshAllData()
  // 刷新图表
  updateRevenueChart()
  updateCourseChart()
}

// 日期变化
const handleDateChange = (dates: [Date, Date]) => {
  if (dates && dates.length === 2) {
    const startDate = dates[0].toISOString().split('T')[0]
    const endDate = dates[1].toISOString().split('T')[0]
    dashboardStore.fetchRevenueTrend(revenuePeriod.value, startDate, endDate)
      .then(() => {
        updateRevenueChart()
      })
  }
}

// 周期变化
const handlePeriodChange = (period: string) => {
  revenuePeriod.value = period
  dashboardStore.fetchRevenueTrend(period)
    .then(() => {
      updateRevenueChart()
    })
}

// 初始化营收图表
const initRevenueChart = () => {
  if (!revenueChartRef.value || !revenueTrend.value) return
  
  revenueChart = echarts.init(revenueChartRef.value)
  
  // 检查是否所有值都是0
  const allZero = revenueTrend.value.values.every(v => v === 0)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const data = params[0]
        const value = typeof data.value === 'number' ? data.value : 0
        const formattedValue = value >= 10000 
          ? `${(value / 10000).toFixed(1)}万` 
          : `¥${value.toFixed(2)}`
        return `${data.name}<br/>营收：${formattedValue}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: revenueTrend.value?.categories || [],
      axisLabel: {
        rotate: revenuePeriod.value === 'year' ? 0 : 30,
        fontSize: 12,
        margin: 8
      }
    },
    yAxis: {
      type: 'value',
      name: '金额 (¥)',
      axisLabel: {
        formatter: (value: number) => {
          if (value >= 10000) {
            return (value / 10000).toFixed(1) + '万'
          }
          return value === 0 ? '0' : value
        }
      },
      scale: true,
      min: 'dataMin',
      max: 'dataMax'
    },
    title: allZero ? {
      text: '暂无营收数据',
      left: 'center',
      top: '50%',
      textStyle: {
        fontSize: 14,
        color: '#999'
      }
    } : null,
    series: [
      {
        name: '营收',
        type: 'bar',
        data: revenueTrend.value?.values || [],
        itemStyle: {
          color: '#07c160',
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: 30,
        label: {
          show: true,
          position: 'top',
          formatter: (params: any) => {
            const value = typeof params.value === 'number' ? params.value : 0
            if (value === 0) return ''
            return value >= 10000 
              ? `¥${(value / 10000).toFixed(1)}万` 
              : `¥${value.toFixed(0)}`
          },
          fontSize: 11,
          color: '#666'
        },
        emphasis: {
          itemStyle: {
            color: '#07c160'
          }
        }
      }
    ]
  }
  
  revenueChart.setOption(option)
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    revenueChart?.resize()
  })
}

// 更新营收图表
const updateRevenueChart = () => {
  if (!revenueChart || !revenueTrend.value) return
  
  const allZero = revenueTrend.value.values.every(v => v === 0)
  
  revenueChart.setOption({
    title: allZero ? {
      text: '暂无营收数据',
      left: 'center',
      top: '50%',
      textStyle: {
        fontSize: 14,
        color: '#999'
      }
    } : null,
    xAxis: {
      data: revenueTrend.value.categories || []
    },
    yAxis: {
      scale: true,
      min: 'dataMin',
      max: 'dataMax'
    },
    series: [
      {
        data: revenueTrend.value.values || []
      }
    ]
  })
}

// 初始化课程分类图表
const initCourseChart = () => {
  if (!courseChartRef.value || !courseCategories.value?.length) return
  
  courseChart = echarts.init(courseChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        return `${params.name}<br/>数量：${params.value} (${params.percent}%)`
      }
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center',
      itemWidth: 10,
      itemHeight: 10,
      formatter: (name: string) => {
        const item = courseCategories.value?.find(c => c.category === name)
        return `${name} ${item?.value || 0} (${item?.percentage?.toFixed(1) || 0}%)`
      }
    },
    series: [
      {
        name: '课程分类',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: courseCategories.value?.map(item => ({
          name: item.category,
          value: item.value,
          itemStyle: {
            color: item.color
          }
        })) || []
      }
    ]
  }
  
  courseChart.setOption(option)
  
  window.addEventListener('resize', () => {
    courseChart?.resize()
  })
}

// 更新课程分类图表
const updateCourseChart = () => {
  if (!courseChart || !courseCategories.value?.length) return
  
  courseChart.setOption({
    series: [
      {
        data: courseCategories.value.map(item => ({
          name: item.category,
          value: item.value,
          itemStyle: {
            color: item.color
          }
        }))
      }
    ],
    legend: {
      formatter: (name: string) => {
        const item = courseCategories.value?.find(c => c.category === name)
        return `${name} ${item?.value || 0} (${item?.percentage?.toFixed(1) || 0}%)`
      }
    }
  })
}

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'scheduled': return 'primary'
    case 'in-progress': return 'warning'
    case 'completed': return 'success'
    case 'cancelled': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'scheduled': return '待开始'
    case 'in-progress': return '进行中'
    case 'completed': return '已完成'
    case 'cancelled': return '已取消'
    default: return '未知'
  }
}

// 格式化时间
const formatTime = (time: string) => {
  return dashboardStore.formatTime(time)
}

// 快速操作
const handleAddMember = () => {
  router.push('/member/add')
}

const handleAddCourse = () => {
  router.push('/course/schedule')
}

const handleCheckIn = () => {
  router.push('/checkIn/list')
}

const handleViewOrders = () => {
  router.push('/order/list')
}

const viewAllCourses = () => {
  router.push('/course/list')
}

const viewCourseDetail = (id: number) => {
  router.push(`/course/detail/${id}`)
}

// 监听数据变化
watch(courseCategories, () => {
  nextTick(() => {
    updateCourseChart()
  })
}, { deep: true })

watch(revenueTrend, () => {
  nextTick(() => {
    updateRevenueChart()
  })
}, { deep: true })

// 生命周期
onMounted(() => {
  initData()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  
  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: var(--gymflow-text-primary);
      margin: 0;
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    margin-bottom: 24px;
    
    .stats-card {
      border-radius: 12px;
      border: 1px solid var(--gymflow-border);
      transition: transform 0.3s, box-shadow 0.3s;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
      }
      
      :deep(.el-card__header) {
        padding: 16px 20px;
        border-bottom: 1px solid var(--gymflow-border);
      }
      
      .stats-card-header {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .stats-icon {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 40px;
          height: 40px;
          border-radius: 10px;
          font-size: 20px;
          
          &.member {
            background: rgba(102, 126, 234, 0.1);
            color: #667eea;
          }
          
          &.coach {
            background: rgba(52, 152, 219, 0.1);
            color: #3498db;
          }
          
          &.revenue {
            background: rgba(46, 204, 113, 0.1);
            color: #2ecc71;
          }
          
          &.attendance {
            background: rgba(155, 89, 182, 0.1);
            color: #9b59b6;
          }
        }
        
        .stats-title {
          font-size: 14px;
          color: var(--gymflow-text-secondary);
          font-weight: 500;
        }
      }
      
      .stats-content {
        padding: 16px 0;
        
        .stats-number {
          font-size: 32px;
          font-weight: 700;
          color: var(--gymflow-text-primary);
          margin-bottom: 8px;
          line-height: 1;
        }
        
        .stats-trend {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          
          .trend-up {
            color: #2ecc71;
          }
          
          .trend-down {
            color: #e74c3c;
          }
          
          .trend-text {
            color: var(--gymflow-text-secondary);
          }
        }
      }
    }
  }
  
  .charts-section {
    margin-bottom: 24px;
    
    .chart-card {
      border-radius: 12px;
      height: 100%;
      
      :deep(.el-card__header) {
        padding: 16px 20px;
        border-bottom: 1px solid var(--gymflow-border);
      }
      
      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .chart-title {
          font-size: 16px;
          font-weight: 600;
          color: var(--gymflow-text-primary);
          margin: 0;
        }
      }
      
      .chart-container {
        .chart {
          width: 100%;
        }
      }
    }
  }
  
  .today-courses {
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid var(--gymflow-border);
    }
    
    .courses-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
    
    .dashboard-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }
    
    .stats-grid {
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    }
  }
}
</style>