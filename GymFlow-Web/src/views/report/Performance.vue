<template>
  <div class="performance-report-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/report' }">报表分析</el-breadcrumb-item>
          <el-breadcrumb-item>绩效报表</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">团队绩效分析</h1>
      </div>
      <div class="header-actions">
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出报告
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
            <span class="filter-label">绩效维度：</span>
            <el-select
              v-model="filterParams.dimension"
              placeholder="选择绩效维度"
              style="width: 200px; margin-right: 20px"
            >
              <el-option label="综合绩效" value="overall" />
              <el-option label="课时绩效" value="sessions" />
              <el-option label="销售绩效" value="sales" />
              <el-option label="客户满意度" value="satisfaction" />
              <el-option label="出勤率" value="attendance" />
            </el-select>
            
            <span class="filter-label">教练级别：</span>
            <el-select
              v-model="filterParams.coachLevel"
              placeholder="全部级别"
              style="width: 120px; margin-right: 20px"
              clearable
            >
              <el-option label="初级教练" value="JUNIOR" />
              <el-option label="中级教练" value="INTERMEDIATE" />
              <el-option label="高级教练" value="SENIOR" />
              <el-option label="明星教练" value="STAR" />
            </el-select>
            
            <span class="filter-label">显示排名：</span>
            <el-select
              v-model="filterParams.rankingCount"
              placeholder="显示数量"
              style="width: 100px"
            >
              <el-option label="前5名" :value="5" />
              <el-option label="前10名" :value="10" />
              <el-option label="前20名" :value="20" />
              <el-option label="全部" :value="0" />
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

    <!-- KPI统计概览 -->
    <el-row :gutter="20" class="kpi-overview">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-icon total-sessions">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-value">{{ formatNumber(stats.totalSessions) }}</div>
              <div class="kpi-label">总课时数</div>
              <div class="kpi-trend up">
                <el-icon><Top /></el-icon>
                <span>同比 +15.2%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-icon total-revenue">
              <el-icon><Money /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-value">{{ formatCurrency(stats.totalRevenue) }}</div>
              <div class="kpi-label">总销售额</div>
              <div class="kpi-trend up">
                <el-icon><Top /></el-icon>
                <span>同比 +22.8%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-icon avg-rating">
              <el-icon><Star /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-value">{{ stats.avgRating.toFixed(1) }}</div>
              <div class="kpi-label">平均评分</div>
              <div class="kpi-trend up">
                <el-icon><Top /></el-icon>
                <span>环比 +0.3</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-icon completion-rate">
              <el-icon><Finished /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-value">{{ (stats.completionRate * 100).toFixed(1) }}%</div>
              <div class="kpi-label">课程完成率</div>
              <div class="kpi-trend down">
                <el-icon><Bottom /></el-icon>
                <span>环比 -1.2%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 教练绩效排名 -->
    <el-card shadow="never" class="ranking-card">
      <template #header>
        <div class="card-header">
          <span>教练绩效排行榜</span>
          <div class="ranking-actions">
            <el-select v-model="rankingSortBy" size="small" style="width: 120px">
              <el-option label="综合评分" value="overall" />
              <el-option label="课时数" value="sessions" />
              <el-option label="销售额" value="revenue" />
              <el-option label="客户评分" value="rating" />
            </el-select>
            <el-button size="small" @click="handleRankingExport">
              <el-icon><Download /></el-icon>
              导出排名
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="ranking-table">
        <div class="ranking-header">
          <div class="header-cell rank">排名</div>
          <div class="header-cell coach">教练</div>
          <div class="header-cell sessions">课时数</div>
          <div class="header-cell revenue">销售额</div>
          <div class="header-cell rating">评分</div>
          <div class="header-cell completion">完成率</div>
          <div class="header-cell commission">佣金收入</div>
          <div class="header-cell action">操作</div>
        </div>
        
        <div 
          v-for="(coach, index) in coachRankings" 
          :key="coach.id"
          class="ranking-row"
          :class="{ 'top-3': index < 3 }"
        >
          <div class="row-cell rank">
            <div class="rank-number" :class="getRankClass(index + 1)">
              {{ index + 1 }}
            </div>
          </div>
          
          <div class="row-cell coach">
            <div class="coach-info">
              <el-avatar :size="36" :src="coach.avatar">
                {{ coach.name.charAt(0) }}
              </el-avatar>
              <div class="coach-details">
                <div class="coach-name">{{ coach.name }}</div>
                <div class="coach-level">
                  <el-tag :type="getLevelTag(coach.level)" size="small">
                    {{ getLevelText(coach.level) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
          
          <div class="row-cell sessions">
            <div class="metric-value">{{ formatNumber(coach.sessions) }}</div>
            <div class="metric-label">课时</div>
          </div>
          
          <div class="row-cell revenue">
            <div class="metric-value">{{ formatCurrency(coach.revenue) }}</div>
            <div class="metric-label">销售额</div>
          </div>
          
          <div class="row-cell rating">
            <div class="rating-display">
              <el-rate
                v-model="coach.rating"
                disabled
                allow-half
                size="small"
              />
              <span class="rating-value">{{ coach.rating.toFixed(1) }}</span>
            </div>
          </div>
          
          <div class="row-cell completion">
            <div class="progress-container">
              <div class="progress-text">{{ (coach.completionRate * 100).toFixed(1) }}%</div>
              <el-progress
                :percentage="coach.completionRate * 100"
                :color="getProgressColor(coach.completionRate)"
                :show-text="false"
              />
            </div>
          </div>
          
          <div class="row-cell commission">
            <div class="metric-value">{{ formatCurrency(coach.commission) }}</div>
            <div class="metric-label">佣金</div>
          </div>
          
          <div class="row-cell action">
            <el-button type="text" size="small" @click="viewCoachDetail(coach)">
              详情
            </el-button>
            <el-button type="text" size="small" @click="viewCoachReport(coach)">
              报告
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 图表分析 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>绩效趋势分析</span>
            </div>
          </template>
          <div ref="performanceTrendChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>各维度绩效分布</span>
              <div class="chart-actions">
                <el-select v-model="radarDimensions" size="small" style="width: 120px" multiple>
                  <el-option label="课时数" value="sessions" />
                  <el-option label="销售额" value="revenue" />
                  <el-option label="客户评分" value="rating" />
                  <el-option label="完成率" value="completion" />
                  <el-option label="新增会员" value="newMembers" />
                </el-select>
              </div>
            </div>
          </template>
          <div ref="performanceRadarChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细绩效分析 -->
    <el-card shadow="never" class="analysis-card">
      <template #header>
        <div class="card-header">
          <span>详细绩效分析</span>
          <el-tabs v-model="analysisTab" class="analysis-tabs">
            <el-tab-pane label="月度对比" name="monthly" />
            <el-tab-pane label="季度趋势" name="quarterly" />
            <el-tab-pane label="年度报告" name="yearly" />
          </el-tabs>
        </div>
      </template>
      
      <div v-if="analysisTab === 'monthly'" class="analysis-content">
        <div class="monthly-comparison">
          <div class="comparison-chart">
            <div ref="monthlyComparisonChart" style="height: 300px;"></div>
          </div>
          <div class="comparison-summary">
            <div class="summary-item">
              <div class="summary-label">本月最佳教练</div>
              <div class="summary-value">{{ monthlySummary.bestCoach }}</div>
              <div class="summary-desc">综合评分 {{ monthlySummary.bestScore.toFixed(1) }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">课时增长最快</div>
              <div class="summary-value">{{ monthlySummary.mostImproved }}</div>
              <div class="summary-desc">增长率 +{{ (monthlySummary.improvementRate * 100).toFixed(1) }}%</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">平均绩效分</div>
              <div class="summary-value">{{ monthlySummary.avgPerformance.toFixed(1) }}</div>
              <div class="summary-desc">{{ monthlySummary.trend > 0 ? '↑' : '↓' }} 环比 {{ Math.abs(monthlySummary.trend * 100).toFixed(1) }}%</div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="analysisTab === 'quarterly'" class="analysis-content">
        <div class="quarterly-trend">
          <div ref="quarterlyTrendChart" style="height: 350px;"></div>
        </div>
      </div>
      
      <div v-if="analysisTab === 'yearly'" class="analysis-content">
        <div class="yearly-report">
          <div class="report-summary">
            <h4>年度绩效总结报告</h4>
            <div class="report-stats">
              <div class="report-stat">
                <div class="stat-value">{{ formatNumber(yearlyReport.totalSessions) }}</div>
                <div class="stat-label">年度总课时</div>
              </div>
              <div class="report-stat">
                <div class="stat-value">{{ formatCurrency(yearlyReport.totalRevenue) }}</div>
                <div class="stat-label">年度总销售额</div>
              </div>
              <div class="report-stat">
                <div class="stat-value">{{ yearlyReport.avgRating.toFixed(1) }}</div>
                <div class="stat-label">平均客户评分</div>
              </div>
              <div class="report-stat">
                <div class="stat-value">{{ yearlyReport.topPerformer }}</div>
                <div class="stat-label">年度最佳教练</div>
              </div>
            </div>
          </div>
          <div class="report-chart">
            <div ref="yearlyReportChart" style="height: 300px;"></div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 绩效奖励 -->
    <el-card shadow="never" class="rewards-card">
      <template #header>
        <div class="card-header">
          <span>本月绩效奖励</span>
          <el-button type="primary" size="small" @click="handleDistributeRewards">
            发放奖励
          </el-button>
        </div>
      </template>
      
      <div class="rewards-list">
        <div v-for="reward in performanceRewards" :key="reward.coachId" class="reward-item">
          <div class="reward-rank">
            <div class="rank-badge" :class="getRankClass(reward.rank)">
              {{ reward.rank }}
            </div>
          </div>
          <div class="reward-coach">
            <div class="coach-name">{{ reward.coachName }}</div>
            <div class="coach-performance">
              <span class="performance-score">绩效分: {{ reward.performanceScore.toFixed(1) }}</span>
              <el-tag :type="getRewardLevelTag(reward.rewardLevel)" size="small">
                {{ getRewardLevelText(reward.rewardLevel) }}
              </el-tag>
            </div>
          </div>
          <div class="reward-amount">
            <div class="amount-value">{{ formatCurrency(reward.rewardAmount) }}</div>
            <div class="amount-label">奖励金额</div>
          </div>
          <div class="reward-commission">
            <div class="commission-value">{{ formatCurrency(reward.commissionAmount) }}</div>
            <div class="commission-label">预估佣金</div>
          </div>
          <div class="reward-actions">
            <el-button type="text" size="small" @click="viewRewardDetail(reward)">
              详情
            </el-button>
            <el-button type="text" size="small" @click="adjustReward(reward)">
              调整
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  Download,
  Refresh,
  Search,
  RefreshRight,
  Timer,
  Money,
  Star,
  Finished,
  Top,
  Bottom
} from '@element-plus/icons-vue'

const router = useRouter()

// 筛选参数
const filterParams = reactive({
  timeType: 'month',
  dateRange: [] as string[],
  dimension: 'overall',
  coachLevel: '',
  rankingCount: 10
})

// 统计数据
const stats = reactive({
  totalSessions: 0,
  totalRevenue: 0,
  avgRating: 0,
  completionRate: 0
})

// 图表引用
const performanceTrendChart = ref<HTMLElement>()
const performanceRadarChart = ref<HTMLElement>()
const monthlyComparisonChart = ref<HTMLElement>()
const quarterlyTrendChart = ref<HTMLElement>()
const yearlyReportChart = ref<HTMLElement>()

// 教练排名数据
const coachRankings = ref<any[]>([])
const rankingSortBy = ref('overall')

// 雷达图维度
const radarDimensions = ref(['sessions', 'revenue', 'rating', 'completion'])

// 分析标签页
const analysisTab = ref('monthly')

// 月度总结
const monthlySummary = reactive({
  bestCoach: '',
  bestScore: 0,
  mostImproved: '',
  improvementRate: 0,
  avgPerformance: 0,
  trend: 0
})

// 年度报告
const yearlyReport = reactive({
  totalSessions: 0,
  totalRevenue: 0,
  avgRating: 0,
  topPerformer: ''
})

// 绩效奖励
const performanceRewards = ref<any[]>([])

// 格式化数字
const formatNumber = (num: number) => {
  return num.toLocaleString('zh-CN')
}

// 格式化货币
const formatCurrency = (amount: number) => {
  return `¥${amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

// 获取排名样式
const getRankClass = (rank: number) => {
  if (rank === 1) return 'rank-gold'
  if (rank === 2) return 'rank-silver'
  if (rank === 3) return 'rank-bronze'
  return 'rank-normal'
}

// 获取教练级别标签样式
const getLevelTag = (level: string) => {
  const map: Record<string, string> = {
    JUNIOR: 'info',
    INTERMEDIATE: 'primary',
    SENIOR: 'success',
    STAR: 'warning'
  }
  return map[level] || 'info'
}

// 获取教练级别文本
const getLevelText = (level: string) => {
  const map: Record<string, string> = {
    JUNIOR: '初级',
    INTERMEDIATE: '中级',
    SENIOR: '高级',
    STAR: '明星'
  }
  return map[level] || '未知'
}

// 获取进度条颜色
const getProgressColor = (rate: number) => {
  if (rate >= 0.9) return '#67c23a'
  if (rate >= 0.8) return '#e6a23c'
  if (rate >= 0.7) return '#409eff'
  return '#f56c6c'
}

// 获取奖励级别标签样式
const getRewardLevelTag = (level: string) => {
  const map: Record<string, string> = {
    S: 'warning',
    A: 'success',
    B: 'primary',
    C: 'info'
  }
  return map[level] || 'info'
}

// 获取奖励级别文本
const getRewardLevelText = (level: string) => {
  return `${level}级奖励`
}

// 时间类型变化
const handleTimeTypeChange = (type: string) => {
  const today = new Date()
  const startOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
  const startOfQuarter = new Date(today.getFullYear(), Math.floor(today.getMonth() / 3) * 3, 1)
  const startOfYear = new Date(today.getFullYear(), 0, 1)
  
  switch (type) {
    case 'month':
      filterParams.dateRange = [
        startOfMonth.toISOString().split('T')[0],
        today.toISOString().split('T')[0]
      ]
      break
    case 'quarter':
      filterParams.dateRange = [
        startOfQuarter.toISOString().split('T')[0],
        today.toISOString().split('T')[0]
      ]
      break
    case 'year':
      filterParams.dateRange = [
        startOfYear.toISOString().split('T')[0],
        today.toISOString().split('T')[0]
      ]
      break
  }
  
  if (type !== 'custom') {
    handleSearch()
  }
}

// 查询数据
const handleSearch = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 更新统计数据
    stats.totalSessions = 2856
    stats.totalRevenue = 856900
    stats.avgRating = 4.7
    stats.completionRate = 0.92
    
    // 更新教练排名数据
    coachRankings.value = [
      {
        id: 1,
        name: '张教练',
        avatar: '',
        level: 'SENIOR',
        sessions: 156,
        revenue: 125600,
        rating: 4.9,
        completionRate: 0.98,
        commission: 25120
      },
      {
        id: 2,
        name: '李教练',
        avatar: '',
        level: 'STAR',
        sessions: 142,
        revenue: 118900,
        rating: 4.8,
        completionRate: 0.96,
        commission: 23780
      },
      {
        id: 3,
        name: '王教练',
        avatar: '',
        level: 'SENIOR',
        sessions: 135,
        revenue: 109800,
        rating: 4.7,
        completionRate: 0.94,
        commission: 21960
      },
      {
        id: 4,
        name: '刘教练',
        avatar: '',
        level: 'INTERMEDIATE',
        sessions: 128,
        revenue: 98600,
        rating: 4.6,
        completionRate: 0.92,
        commission: 19720
      },
      {
        id: 5,
        name: '陈教练',
        avatar: '',
        level: 'SENIOR',
        sessions: 121,
        revenue: 92500,
        rating: 4.8,
        completionRate: 0.95,
        commission: 18500
      }
    ]
    
    // 更新月度总结
    monthlySummary.bestCoach = '张教练'
    monthlySummary.bestScore = 4.9
    monthlySummary.mostImproved = '李教练'
    monthlySummary.improvementRate = 0.152
    monthlySummary.avgPerformance = 4.76
    monthlySummary.trend = 0.023
    
    // 更新年度报告
    yearlyReport.totalSessions = 12560
    yearlyReport.totalRevenue = 4258900
    yearlyReport.avgRating = 4.7
    yearlyReport.topPerformer = '张教练'
    
    // 更新绩效奖励
    performanceRewards.value = [
      {
        coachId: 1,
        coachName: '张教练',
        rank: 1,
        performanceScore: 96.5,
        rewardLevel: 'S',
        rewardAmount: 5000,
        commissionAmount: 25120
      },
      {
        coachId: 2,
        coachName: '李教练',
        rank: 2,
        performanceScore: 92.8,
        rewardLevel: 'A',
        rewardAmount: 3000,
        commissionAmount: 23780
      },
      {
        coachId: 3,
        coachName: '王教练',
        rank: 3,
        performanceScore: 89.3,
        rewardLevel: 'B',
        rewardAmount: 2000,
        commissionAmount: 21960
      },
      {
        coachId: 4,
        coachName: '刘教练',
        rank: 4,
        performanceScore: 85.6,
        rewardLevel: 'B',
        rewardAmount: 1500,
        commissionAmount: 19720
      },
      {
        coachId: 5,
        coachName: '陈教练',
        rank: 5,
        performanceScore: 83.2,
        rewardLevel: 'C',
        rewardAmount: 1000,
        commissionAmount: 18500
      }
    ]
    
    // 初始化图表
    initCharts()
    
    ElMessage.success('数据加载成功')
  } catch (error) {
    console.error('数据加载失败:', error)
    ElMessage.error('数据加载失败')
  }
}

// 重置筛选
const handleReset = () => {
  filterParams.timeType = 'month'
  filterParams.dateRange = []
  filterParams.dimension = 'overall'
  filterParams.coachLevel = ''
  filterParams.rankingCount = 10
  handleSearch()
}

// 刷新数据
const handleRefresh = () => {
  handleSearch()
}

// 导出数据
const handleExport = () => {
  ElMessage.info('绩效报告导出功能开发中')
}

// 导出排名
const handleRankingExport = () => {
  ElMessage.info('排名数据导出功能开发中')
}

// 查看教练详情
const viewCoachDetail = (coach: any) => {
  router.push(`/coach/detail/${coach.id}`)
}

// 查看教练报告
const viewCoachReport = (coach: any) => {
  ElMessage.info(`查看 ${coach.name} 的详细绩效报告`)
}

// 查看奖励详情
const viewRewardDetail = (reward: any) => {
  ElMessage.info(`查看 ${reward.coachName} 的奖励详情`)
}

// 调整奖励
const adjustReward = (reward: any) => {
  ElMessage.info(`调整 ${reward.coachName} 的奖励金额`)
}

// 发放奖励
const handleDistributeRewards = () => {
  ElMessage.info('批量发放奖励功能开发中')
}

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    // 绩效趋势图表
    if (performanceTrendChart.value) {
      const trendChart = echarts.init(performanceTrendChart.value)
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
                  <span style="font-weight: bold; color: #303133">${item.value}</span>
                </div>
              `
            })
            return html
          }
        },
        legend: {
          data: ['综合绩效', '课时绩效', '销售绩效', '客户满意度']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
        },
        yAxis: {
          type: 'value',
          name: '绩效分'
        },
        series: [
          {
            name: '综合绩效',
            type: 'line',
            data: [85, 86, 88, 87, 89, 90, 92, 91, 93, 94, 95, 96],
            smooth: true,
            lineStyle: { width: 3 },
            itemStyle: { color: '#409eff' }
          },
          {
            name: '课时绩效',
            type: 'line',
            data: [82, 84, 85, 86, 88, 89, 90, 91, 92, 93, 94, 95],
            smooth: true,
            itemStyle: { color: '#67c23a' }
          },
          {
            name: '销售绩效',
            type: 'line',
            data: [80, 82, 84, 85, 87, 88, 90, 91, 92, 93, 94, 95],
            smooth: true,
            itemStyle: { color: '#e6a23c' }
          }
        ]
      }
      trendChart.setOption(option)
      
      window.addEventListener('resize', () => {
        trendChart.resize()
      })
    }
    
    // 雷达图
    if (performanceRadarChart.value) {
      const radarChart = echarts.init(performanceRadarChart.value)
      const option = {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          data: ['张教练', '李教练', '王教练', '行业平均']
        },
        radar: {
          indicator: [
            { name: '课时数', max: 200 },
            { name: '销售额', max: 150000 },
            { name: '客户评分', max: 5 },
            { name: '完成率', max: 1 },
            { name: '新增会员', max: 30 }
          ]
        },
        series: [
          {
            type: 'radar',
            data: [
              {
                value: [156, 125600, 4.9, 0.98, 25],
                name: '张教练',
                itemStyle: { color: '#409eff' },
                areaStyle: { color: 'rgba(64, 158, 255, 0.1)' }
              },
              {
                value: [142, 118900, 4.8, 0.96, 22],
                name: '李教练',
                itemStyle: { color: '#67c23a' },
                areaStyle: { color: 'rgba(103, 194, 58, 0.1)' }
              },
              {
                value: [135, 109800, 4.7, 0.94, 20],
                name: '王教练',
                itemStyle: { color: '#e6a23c' },
                areaStyle: { color: 'rgba(230, 162, 60, 0.1)' }
              },
              {
                value: [120, 95000, 4.5, 0.9, 18],
                name: '行业平均',
                itemStyle: { color: '#909399' },
                lineStyle: { type: 'dashed' }
              }
            ]
          }
        ]
      }
      radarChart.setOption(option)
      
      window.addEventListener('resize', () => {
        radarChart.resize()
      })
    }
    
    // 月度对比图表
    if (monthlyComparisonChart.value && analysisTab.value === 'monthly') {
      const comparisonChart = echarts.init(monthlyComparisonChart.value)
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
          data: ['张教练', '李教练', '王教练', '刘教练', '陈教练', '赵教练', '孙教练']
        },
        yAxis: {
          type: 'value',
          name: '绩效分'
        },
        series: [
          {
            name: '上月',
            type: 'bar',
            data: [88.5, 86.2, 84.8, 83.1, 81.5, 79.8, 78.2],
            itemStyle: { color: '#a0cfff' },
            barGap: '30%'
          },
          {
            name: '本月',
            type: 'bar',
            data: [92.3, 89.7, 87.5, 85.6, 83.9, 81.2, 79.5],
            itemStyle: { color: '#409eff' }
          }
        ]
      }
      comparisonChart.setOption(option)
      
      window.addEventListener('resize', () => {
        comparisonChart.resize()
      })
    }
    
    // 季度趋势图表
    if (quarterlyTrendChart.value && analysisTab.value === 'quarterly') {
      const quarterlyChart = echarts.init(quarterlyTrendChart.value)
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['第一季度', '第二季度', '第三季度', '第四季度']
        },
        xAxis: {
          type: 'category',
          data: ['课时数', '销售额', '客户评分', '完成率', '新增会员']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '第一季度',
            type: 'line',
            stack: '总量',
            data: [320, 285600, 4.6, 0.89, 65],
            itemStyle: { color: '#409eff' }
          },
          {
            name: '第二季度',
            type: 'line',
            stack: '总量',
            data: [356, 325800, 4.7, 0.91, 72],
            itemStyle: { color: '#67c23a' }
          },
          {
            name: '第三季度',
            type: 'line',
            stack: '总量',
            data: [412, 398500, 4.8, 0.93, 85],
            itemStyle: { color: '#e6a23c' }
          },
          {
            name: '第四季度',
            type: 'line',
            stack: '总量',
            data: [468, 456000, 4.8, 0.95, 98],
            itemStyle: { color: '#f56c6c' }
          }
        ]
      }
      quarterlyChart.setOption(option)
      
      window.addEventListener('resize', () => {
        quarterlyChart.resize()
      })
    }
    
    // 年度报告图表
    if (yearlyReportChart.value && analysisTab.value === 'yearly') {
      const yearlyChart = echarts.init(yearlyReportChart.value)
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
        },
        yAxis: [
          {
            type: 'value',
            name: '课时数',
            position: 'left'
          },
          {
            type: 'value',
            name: '销售额（万元）',
            position: 'right',
            axisLabel: {
              formatter: (value: number) => (value / 10000).toFixed(0)
            }
          }
        ],
        series: [
          {
            name: '课时数',
            type: 'bar',
            data: [265, 242, 298, 325, 356, 412, 398, 425, 468, 512, 498, 525],
            itemStyle: { color: '#409eff' },
            yAxisIndex: 0
          },
          {
            name: '销售额',
            type: 'line',
            data: [285600, 265800, 325600, 356800, 398500, 456200, 425600, 468900, 512300, 568700, 525400, 598600],
            itemStyle: { color: '#67c23a' },
            yAxisIndex: 1
          }
        ]
      }
      yearlyChart.setOption(option)
      
      window.addEventListener('resize', () => {
        yearlyChart.resize()
      })
    }
  })
}

// 监听分析标签页变化
watch(analysisTab, () => {
  setTimeout(() => {
    initCharts()
  }, 100)
})

// 监听筛选参数变化
watch([filterParams, rankingSortBy, radarDimensions], () => {
  handleSearch()
}, { deep: true })

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
.performance-report-container {
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

.kpi-overview {
  margin-bottom: 20px;
}

.kpi-card {
  border: 1px solid #ebeef5;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .kpi-content {
    display: flex;
    align-items: center;
    padding: 10px 0;

    .kpi-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20px;
      font-size: 28px;

      &.total-sessions {
        background: rgba(64, 158, 255, 0.1);
        color: #409eff;
      }

      &.total-revenue {
        background: rgba(103, 194, 58, 0.1);
        color: #67c23a;
      }

      &.avg-rating {
        background: rgba(230, 162, 60, 0.1);
        color: #e6a23c;
      }

      &.completion-rate {
        background: rgba(245, 108, 108, 0.1);
        color: #f56c6c;
      }
    }

    .kpi-info {
      flex: 1;

      .kpi-value {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
        line-height: 1;
      }

      .kpi-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 6px;
      }

      .kpi-trend {
        display: flex;
        align-items: center;
        font-size: 12px;
        font-weight: 500;

        &.up {
          color: #67c23a;
        }

        &.down {
          color: #f56c6c;
        }

        .el-icon {
          margin-right: 4px;
          font-size: 12px;
        }
      }
    }
  }
}

.ranking-card {
  margin-bottom: 20px;
  border: 1px solid #ebeef5;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .ranking-actions {
      display: flex;
      gap: 10px;
    }
  }

  .ranking-table {
    border: 1px solid #ebeef5;
    border-radius: 4px;
    overflow: hidden;
  }

  .ranking-header {
    display: flex;
    background: #f5f7fa;
    border-bottom: 1px solid #ebeef5;
    
    .header-cell {
      padding: 12px;
      font-weight: 500;
      color: #606266;
      text-align: center;
      
      &.rank { width: 80px; }
      &.coach { flex: 2; text-align: left; }
      &.sessions { width: 120px; }
      &.revenue { width: 140px; }
      &.rating { width: 160px; }
      &.completion { width: 140px; }
      &.commission { width: 120px; }
      &.action { width: 120px; }
    }
  }

  .ranking-row {
    display: flex;
    align-items: center;
    border-bottom: 1px solid #ebeef5;
    transition: all 0.3s;
    
    &:hover {
      background: #f0f9ff;
    }
    
    &.top-3 {
      background: linear-gradient(90deg, rgba(255, 251, 235, 0.3), transparent);
    }
    
    .row-cell {
      padding: 16px 12px;
      text-align: center;
      
      &.rank {
        width: 80px;
        .rank-number {
          display: inline-block;
          width: 32px;
          height: 32px;
          line-height: 32px;
          border-radius: 50%;
          font-weight: 600;
          font-size: 14px;
          
          &.rank-gold {
            background: linear-gradient(135deg, #ffd700, #ffed4e);
            color: #b38700;
          }
          
          &.rank-silver {
            background: linear-gradient(135deg, #c0c0c0, #e0e0e0);
            color: #606060;
          }
          
          &.rank-bronze {
            background: linear-gradient(135deg, #cd7f32, #e8b074);
            color: #8b4513;
          }
          
          &.rank-normal {
            background: #f5f7fa;
            color: #909399;
          }
        }
      }
      
      &.coach {
        flex: 2;
        text-align: left;
        
        .coach-info {
          display: flex;
          align-items: center;
          
          .coach-details {
            margin-left: 12px;
            
            .coach-name {
              font-weight: 500;
              color: #303133;
              margin-bottom: 4px;
            }
            
            .coach-level {
              display: inline-block;
            }
          }
        }
      }
      
      &.sessions,
      &.revenue,
      &.commission {
        .metric-value {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }
        
        .metric-label {
          font-size: 12px;
          color: #909399;
        }
      }
      
      &.rating {
        .rating-display {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          
          .rating-value {
            font-size: 14px;
            font-weight: 500;
            color: #e6a23c;
          }
        }
      }
      
      &.completion {
        .progress-container {
          .progress-text {
            font-size: 14px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 6px;
          }
          
          :deep(.el-progress) {
            width: 100%;
          }
        }
      }
      
      &.action {
        width: 120px;
        display: flex;
        justify-content: center;
        gap: 8px;
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

.analysis-card {
  margin-bottom: 20px;
  border: 1px solid #ebeef5;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .analysis-tabs {
      margin-bottom: -15px;
    }
  }

  .analysis-content {
    padding: 20px 0;
  }

  .monthly-comparison {
    display: flex;
    gap: 40px;
    
    .comparison-chart {
      flex: 2;
    }
    
    .comparison-summary {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 20px;
      
      .summary-item {
        padding: 20px;
        background: #f9fafc;
        border-radius: 8px;
        border-left: 4px solid #409eff;
        
        .summary-label {
          font-size: 14px;
          color: #606266;
          margin-bottom: 8px;
        }
        
        .summary-value {
          font-size: 18px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }
        
        .summary-desc {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }

  .yearly-report {
    .report-summary {
      margin-bottom: 30px;
      
      h4 {
        margin: 0 0 20px 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
      
      .report-stats {
        display: flex;
        gap: 20px;
        
        .report-stat {
          flex: 1;
          text-align: center;
          padding: 20px;
          background: #f9fafc;
          border-radius: 8px;
          
          .stat-value {
            font-size: 24px;
            font-weight: 600;
            color: #409eff;
            margin-bottom: 8px;
          }
          
          .stat-label {
            font-size: 14px;
            color: #606266;
          }
        }
      }
    }
  }
}

.rewards-card {
  border: 1px solid #ebeef5;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .rewards-list {
    .reward-item {
      display: flex;
      align-items: center;
      padding: 16px;
      border-bottom: 1px solid #ebeef5;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:hover {
        background: #f9fafc;
      }
      
      .reward-rank {
        width: 60px;
        text-align: center;
        
        .rank-badge {
          display: inline-block;
          width: 32px;
          height: 32px;
          line-height: 32px;
          border-radius: 50%;
          font-weight: 600;
          font-size: 14px;
          
          &.rank-gold {
            background: linear-gradient(135deg, #ffd700, #ffed4e);
            color: #b38700;
          }
          
          &.rank-silver {
            background: linear-gradient(135deg, #c0c0c0, #e0e0e0);
            color: #606060;
          }
          
          &.rank-bronze {
            background: linear-gradient(135deg, #cd7f32, #e8b074);
            color: #8b4513;
          }
          
          &.rank-normal {
            background: #f5f7fa;
            color: #909399;
          }
        }
      }
      
      .reward-coach {
        flex: 2;
        
        .coach-name {
          font-weight: 500;
          color: #303133;
          margin-bottom: 6px;
        }
        
        .coach-performance {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .performance-score {
            font-size: 12px;
            color: #909399;
          }
        }
      }
      
      .reward-amount,
      .reward-commission {
        flex: 1;
        text-align: center;
        
        .amount-value,
        .commission-value {
          font-size: 18px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }
        
        .amount-label,
        .commission-label {
          font-size: 12px;
          color: #909399;
        }
      }
      
      .reward-actions {
        width: 120px;
        display: flex;
        justify-content: center;
        gap: 8px;
      }
    }
  }
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 20px;
}

@media (max-width: 992px) {
  .monthly-comparison {
    flex-direction: column !important;
  }
  
  .report-stats {
    flex-wrap: wrap;
  }
  
  .reward-item {
    flex-wrap: wrap;
    gap: 10px;
    
    .reward-rank {
      width: 100% !important;
      text-align: left;
    }
    
    .reward-actions {
      width: 100% !important;
      justify-content: flex-start !important;
    }
  }
}

@media (max-width: 768px) {
  .filter-row .filter-group {
    flex-direction: column;
    align-items: flex-start !important;
    
    .filter-label {
      margin-bottom: 8px;
    }
  }
  
  .ranking-header,
  .ranking-row {
    flex-wrap: wrap;
    
    .header-cell,
    .row-cell {
      width: 100% !important;
      text-align: left !important;
      padding: 8px 12px !important;
      
      &.rank {
        text-align: center !important;
      }
      
      &.action {
        text-align: center !important;
      }
    }
  }
  
  .coach-info {
    flex-direction: column;
    align-items: flex-start !important;
    
    .coach-details {
      margin-left: 0 !important;
      margin-top: 8px;
    }
  }
}
</style>