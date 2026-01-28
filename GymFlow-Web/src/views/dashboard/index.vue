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
          <div class="stats-number">¥{{ stats?.todayRevenue || 0 }}</div>
          <div class="stats-trend">
            <el-icon v-if="revenueTrend > 0" class="trend-up"><Top /></el-icon>
            <el-icon v-else-if="revenueTrend < 0" class="trend-down"><Bottom /></el-icon>
            <span class="trend-text">{{ Math.abs(revenueTrend) }}% 较昨日</span>
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
    
    <!-- 快速操作 -->
    <div class="quick-actions">
      <el-card>
        <template #header>
          <h3>快速操作</h3>
        </template>
        <div class="actions-grid">
          <el-button class="action-btn" type="primary" @click="handleAddMember">
            <el-icon><User /></el-icon>
            新增会员
          </el-button>
          <el-button class="action-btn" type="success" @click="handleAddCourse">
            <el-icon><Calendar /></el-icon>
            排课
          </el-button>
          <el-button class="action-btn" type="warning" @click="handleCheckIn">
            <el-icon><Check /></el-icon>
            签到管理
          </el-button>
          <el-button class="action-btn" type="info" @click="handleViewOrders">
            <el-icon><ShoppingCart /></el-icon>
            查看订单
          </el-button>
        </div>
      </el-card>
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
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { useDashboardStore } from '@/stores/dashboard'
import { useCourseStore } from '@/stores/course'
import { formatTime } from '@/utils'
import type { Course, CourseStatus } from '@/types'

// Store
const dashboardStore = useDashboardStore()
const courseStore = useCourseStore()

// Router
const router = useRouter()

// 状态
const loading = ref(false)
const coursesLoading = ref(false)
const dateRange = ref<[Date, Date]>([
  new Date(new Date().setDate(new Date().getDate() - 7)),
  new Date()
])
const revenuePeriod = ref('week')

// 图表引用
const revenueChartRef = ref<HTMLElement>()
const courseChartRef = ref<HTMLElement>()

// 图表实例
let revenueChart: echarts.ECharts | null = null
let courseChart: echarts.ECharts | null = null

// Computed
const stats = ref(dashboardStore.stats)
const todayCourses = ref<Course[]>([])
const memberTrend = ref(12.5)
const coachTrend = ref(5.2)
const revenueTrend = ref(18.3)
const attendanceTrend = ref(8.7)

// Methods
const refreshData = async () => {
  try {
    loading.value = true
    await dashboardStore.refreshData()
    stats.value = dashboardStore.stats
    await loadTodayCourses()
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('数据刷新失败')
    console.error('刷新数据失败:', error)
  } finally {
    loading.value = false
  }
}

const loadTodayCourses = async () => {
  try {
    coursesLoading.value = true
    const today = new Date().toISOString().split('T')[0]
    const response = await courseStore.fetchCourses({
      startDate: today,
      endDate: today,
      pageSize: 5
    })
    todayCourses.value = response.items
  } catch (error) {
    console.error('加载今日课程失败:', error)
  } finally {
    coursesLoading.value = false
  }
}

const handleDateChange = () => {
  refreshData()
}

const handlePeriodChange = (period: string) => {
  revenuePeriod.value = period
  initRevenueChart()
}

const getStatusType = (status: CourseStatus) => {
  switch (status) {
    case 'SCHEDULED':
      return 'primary'
    case 'IN_PROGRESS':
      return 'warning'
    case 'COMPLETED':
      return 'success'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status: CourseStatus) => {
  switch (status) {
    case 'SCHEDULED':
      return '已安排'
    case 'IN_PROGRESS':
      return '进行中'
    case 'COMPLETED':
      return '已完成'
    case 'CANCELLED':
      return '已取消'
    default:
      return '未知'
  }
}

// 初始化营收趋势图表
const initRevenueChart = () => {
  if (!revenueChartRef.value) return
  
  if (revenueChart) {
    revenueChart.dispose()
  }
  
  revenueChart = echarts.init(revenueChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>¥{c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: revenuePeriod.value === 'week' 
        ? ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        : revenuePeriod.value === 'month'
        ? Array.from({ length: 30 }, (_, i) => `${i + 1}日`)
        : ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '营收',
        type: 'line',
        smooth: true,
        data: revenuePeriod.value === 'week'
          ? [3200, 2800, 3500, 4200, 3800, 4500, 5200]
          : revenuePeriod.value === 'month'
          ? Array.from({ length: 30 }, () => Math.floor(Math.random() * 5000) + 2000)
          : [45000, 52000, 48000, 55000, 62000, 58000, 65000, 70000, 68000, 75000, 80000, 85000],
        itemStyle: {
          color: '#00b894'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 184, 148, 0.3)' },
            { offset: 1, color: 'rgba(0, 184, 148, 0.1)' }
          ])
        }
      }
    ]
  }
  
  revenueChart.setOption(option)
}

// 初始化课程分类图表
const initCourseChart = () => {
  if (!courseChartRef.value) return
  
  if (courseChart) {
    courseChart.dispose()
  }
  
  courseChart = echarts.init(courseChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      data: ['瑜伽', '普拉提', '综合体能', '动感单车', '力量训练', '有氧运动', '私教课']
    },
    series: [
      {
        name: '课程分类',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 335, name: '瑜伽', itemStyle: { color: '#5470c6' } },
          { value: 310, name: '普拉提', itemStyle: { color: '#91cc75' } },
          { value: 234, name: '综合体能', itemStyle: { color: '#fac858' } },
          { value: 135, name: '动感单车', itemStyle: { color: '#ee6666' } },
          { value: 154, name: '力量训练', itemStyle: { color: '#73c0de' } },
          { value: 98, name: '有氧运动', itemStyle: { color: '#3ba272' } },
          { value: 67, name: '私教课', itemStyle: { color: '#fc8452' } }
        ]
      }
    ]
  }
  
  courseChart.setOption(option)
}

// 快速操作
const handleAddMember = () => {
  router.push('/members/create')
}

const handleAddCourse = () => {
  router.push('/courses/create')
}

const handleCheckIn = () => {
  router.push('/checkin')
}

const handleViewOrders = () => {
  router.push('/orders')
}

const viewAllCourses = () => {
  router.push('/courses')
}

const viewCourseDetail = (id: number) => {
  router.push(`/courses/${id}`)
}

// 图表自适应
const handleResize = () => {
  if (revenueChart) {
    revenueChart.resize()
  }
  if (courseChart) {
    courseChart.resize()
  }
}

// 生命周期
onMounted(async () => {
  await refreshData()
  
  await nextTick()
  initRevenueChart()
  initCourseChart()
  
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (revenueChart) {
    revenueChart.dispose()
  }
  if (courseChart) {
    courseChart.dispose()
  }
  window.removeEventListener('resize', handleResize)
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
  
  .quick-actions {
    margin-bottom: 24px;
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid var(--gymflow-border);
    }
    
    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      
      .action-btn {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100px;
        padding: 20px;
        border-radius: 12px;
        font-size: 16px;
        transition: transform 0.3s;
        
        &:hover {
          transform: translateY(-4px);
        }
        
        .el-icon {
          font-size: 24px;
          margin-bottom: 12px;
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