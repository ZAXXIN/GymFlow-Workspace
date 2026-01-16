<template>
  <div class="attendance-report-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/report' }">报表分析</el-breadcrumb-item>
          <el-breadcrumb-item>考勤报表</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">考勤签到分析</h1>
      </div>
      <div class="header-actions">
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出数据
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
            <el-date-picker
              v-model="filterParams.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 300px; margin-right: 20px"
            />
            
            <span class="filter-label">考勤类型：</span>
            <el-select
              v-model="filterParams.attendanceType"
              placeholder="全部类型"
              style="width: 120px; margin-right: 20px"
              clearable
            >
              <el-option label="课程签到" value="COURSE" />
              <el-option label="健身房入场" value="GYM_ACCESS" />
              <el-option label="私教课程" value="PERSONAL_TRAINING" />
            </el-select>
            
            <span class="filter-label">签到状态：</span>
            <el-select
              v-model="filterParams.status"
              placeholder="全部状态"
              style="width: 120px"
              clearable
            >
              <el-option label="已签到" value="CHECKED_IN" />
              <el-option label="已签出" value="CHECKED_OUT" />
            </el-select>
          </div>
        </div>
        
        <div class="filter-row">
          <div class="filter-group">
            <span class="filter-label">会员姓名：</span>
            <el-input
              v-model="filterParams.memberName"
              placeholder="请输入会员姓名"
              style="width: 200px; margin-right: 20px"
              clearable
            />
            
            <span class="filter-label">教练姓名：</span>
            <el-input
              v-model="filterParams.coachName"
              placeholder="请输入教练姓名"
              style="width: 200px; margin-right: 20px"
              clearable
            />
            
            <span class="filter-label">课程名称：</span>
            <el-input
              v-model="filterParams.courseName"
              placeholder="请输入课程名称"
              style="width: 200px"
              clearable
            />
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
            <div class="stat-icon total-checkins">
              <el-icon><UserChecked /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCheckins }}</div>
              <div class="stat-label">总签到人次</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon unique-members">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.uniqueMembers }}</div>
              <div class="stat-label">参与会员数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon avg-frequency">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.avgFrequency.toFixed(1) }}</div>
              <div class="stat-label">人均签到频率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon attendance-rate">
              <el-icon><Finished /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ (stats.attendanceRate * 100).toFixed(1) }}%</div>
              <div class="stat-label">出勤率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表分析 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>每日签到趋势</span>
              <div class="chart-actions">
                <el-select v-model="trendChartType" size="small" style="width: 100px">
                  <el-option label="折线图" value="line" />
                  <el-option label="柱状图" value="bar" />
                </el-select>
              </div>
            </div>
          </template>
          <div ref="dailyTrendChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>时段分布分析</span>
            </div>
          </template>
          <div ref="timeDistributionChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>课程出勤统计</span>
            </div>
          </template>
          <div ref="courseAttendanceChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>会员活跃度分布</span>
            </div>
          </template>
          <div ref="memberActivityChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>考勤明细数据</span>
          <div class="table-actions">
            <el-button size="small" @click="handleBatchCheckOut">
              批量签出
            </el-button>
          </div>
        </div>
      </template>
      
      <AppTable
        :data="attendanceData"
        :loading="loading"
        :columns="columns"
        :pagination="pagination"
        @page-change="handlePageChange"
        @selection-change="handleSelectionChange"
      >
        <template #type="{ row }">
          <el-tag :type="getAttendanceTypeTag(row.type)" size="small">
            {{ getAttendanceTypeText(row.type) }}
          </el-tag>
        </template>

        <template #status="{ row }">
          <el-tag :type="getStatusTag(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>

        <template #checkInTime="{ row }">
          {{ formatDateTime(row.checkInTime) }}
        </template>

        <template #checkOutTime="{ row }">
          {{ row.checkOutTime ? formatDateTime(row.checkOutTime) : '--' }}
        </template>

        <template #duration="{ row }">
          {{ row.duration ? `${row.duration}分钟` : '--' }}
        </template>

        <template #actions="{ row }">
          <div class="action-buttons">
            <el-button 
              v-if="row.status === 'CHECKED_IN'"
              type="success" 
              text 
              size="small"
              @click="handleCheckOut(row)"
            >
              签出
            </el-button>
            <el-button type="primary" text size="small" @click="handleView(row)">
              查看
            </el-button>
          </div>
        </template>
      </AppTable>
      
      <!-- 批量操作 -->
      <div v-if="selectedRows.length > 0" class="batch-actions">
        <div class="batch-info">
          已选择 {{ selectedRows.length }} 条记录
        </div>
        <div class="batch-buttons">
          <el-button type="primary" size="small" @click="handleBatchCheckOut">
            批量签出
          </el-button>
          <el-button size="small" @click="clearSelection">
            取消选择
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 签出弹窗 -->
    <el-dialog
      v-model="checkOutDialog.visible"
      title="会员签出"
      width="400px"
      destroy-on-close
    >
      <div v-if="checkOutDialog.attendance" class="checkout-dialog">
        <el-form :model="checkOutForm" label-width="100px">
          <el-form-item label="会员姓名：">
            <span>{{ checkOutDialog.attendance.memberName }}</span>
          </el-form-item>
          <el-form-item label="签到类型：">
            <el-tag :type="getAttendanceTypeTag(checkOutDialog.attendance.type)" size="small">
              {{ getAttendanceTypeText(checkOutDialog.attendance.type) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="签到时间：">
            <span>{{ formatDateTime(checkOutDialog.attendance.checkInTime) }}</span>
          </el-form-item>
          <el-form-item label="签出时间：">
            <el-time-picker
              v-model="checkOutForm.checkOutTime"
              placeholder="选择签出时间"
              value-format="HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="备注：">
            <el-input
              v-model="checkOutForm.notes"
              type="textarea"
              :rows="2"
              placeholder="请输入签出备注"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="checkOutDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmCheckOut">确认签出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import AppTable from '@/components/common/AppTable.vue'
import {
  Download,
  Refresh,
  Search,
  RefreshRight,
  UserChecked,
  User,
  TrendCharts,
  Finished
} from '@element-plus/icons-vue'

// 筛选参数
const filterParams = reactive({
  dateRange: [] as string[],
  attendanceType: '',
  status: '',
  memberName: '',
  coachName: '',
  courseName: ''
})

// 统计数据
const stats = reactive({
  totalCheckins: 0,
  uniqueMembers: 0,
  avgFrequency: 0,
  attendanceRate: 0
})

// 图表引用
const dailyTrendChart = ref<HTMLElement>()
const timeDistributionChart = ref<HTMLElement>()
const courseAttendanceChart = ref<HTMLElement>()
const memberActivityChart = ref<HTMLElement>()

// 图表类型
const trendChartType = ref('line')

// 表格数据
const attendanceData = ref<any[]>([])
const loading = ref(false)
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 表格列定义
const columns = [
  { type: 'selection', width: 50 },
  { prop: 'checkInNo', label: '签到编号', width: 140 },
  { prop: 'memberName', label: '会员姓名', width: 100 },
  { prop: 'type', label: '签到类型', slot: true, width: 100 },
  { prop: 'courseName', label: '课程名称', width: 150 },
  { prop: 'coachName', label: '教练', width: 100 },
  { prop: 'location', label: '地点', width: 120 },
  { prop: 'checkInTime', label: '签到时间', slot: true, width: 180 },
  { prop: 'checkOutTime', label: '签出时间', slot: true, width: 180 },
  { prop: 'duration', label: '停留时长', slot: true, width: 120 },
  { prop: 'status', label: '状态', slot: true, width: 100 },
  { label: '操作', slot: true, width: 120 }
]

// 选择的行数据
const selectedRows = ref<any[]>([])

// 签出弹窗
const checkOutDialog = reactive({
  visible: false,
  attendance: null as any
})

const checkOutForm = reactive({
  checkOutTime: '',
  notes: ''
})

// 格式化日期时间
const formatDateTime = (dateString: string) => {
  if (!dateString) return '--'
  return new Date(dateString).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取签到类型标签样式
const getAttendanceTypeTag = (type: string) => {
  const map: Record<string, string> = {
    COURSE: 'primary',
    GYM_ACCESS: 'success',
    PERSONAL_TRAINING: 'warning'
  }
  return map[type] || 'info'
}

// 获取签到类型文本
const getAttendanceTypeText = (type: string) => {
  const map: Record<string, string> = {
    COURSE: '课程签到',
    GYM_ACCESS: '健身房入场',
    PERSONAL_TRAINING: '私教课程'
  }
  return map[type] || '其他'
}

// 获取状态标签样式
const getStatusTag = (status: string) => {
  return status === 'CHECKED_IN' ? 'success' : 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  return status === 'CHECKED_IN' ? '已签到' : '已签出'
}

// 查询数据
const handleSearch = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 更新统计数据
    stats.totalCheckins = 856
    stats.uniqueMembers = 142
    stats.avgFrequency = 6.0
    stats.attendanceRate = 0.78
    
    // 更新表格数据
    attendanceData.value = [
      {
        id: 1,
        checkInNo: 'CHK20230615001',
        memberName: '张三',
        type: 'COURSE',
        courseName: '晨间瑜伽',
        coachName: '张教练',
        location: 'A区瑜伽室',
        checkInTime: '2023-06-15 08:25:00',
        checkOutTime: '2023-06-15 09:30:00',
        duration: 65,
        status: 'CHECKED_OUT'
      },
      {
        id: 2,
        checkInNo: 'CHK20230615002',
        memberName: '李四',
        type: 'GYM_ACCESS',
        courseName: '',
        coachName: '',
        location: '健身房入口',
        checkInTime: '2023-06-15 10:15:00',
        checkOutTime: '',
        duration: null,
        status: 'CHECKED_IN'
      },
      {
        id: 3,
        checkInNo: 'CHK20230615003',
        memberName: '王五',
        type: 'PERSONAL_TRAINING',
        courseName: '私教课程',
        coachName: '李教练',
        location: '私教区',
        checkInTime: '2023-06-15 14:30:00',
        checkOutTime: '2023-06-15 15:30:00',
        duration: 60,
        status: 'CHECKED_OUT'
      }
    ]
    
    pagination.total = attendanceData.value.length
    
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
  filterParams.dateRange = []
  filterParams.attendanceType = ''
  filterParams.status = ''
  filterParams.memberName = ''
  filterParams.coachName = ''
  filterParams.courseName = ''
  handleSearch()
}

// 刷新数据
const handleRefresh = () => {
  handleSearch()
}

// 导出数据
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.page = page
  handleSearch()
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

// 清除选择
const clearSelection = () => {
  selectedRows.value = []
}

// 查看详情
const handleView = (row: any) => {
  // 跳转到签到详情页或打开详情弹窗
  ElMessage.info(`查看签到记录 ${row.checkInNo}`)
}

// 单个签出
const handleCheckOut = (row: any) => {
  checkOutDialog.attendance = row
  checkOutDialog.visible = true
  checkOutForm.checkOutTime = new Date().toTimeString().split(' ')[0]
  checkOutForm.notes = ''
}

// 批量签出
const handleBatchCheckOut = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要签出的记录')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要批量签出 ${selectedRows.value.length} 条记录吗？`,
      '批量签出确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用批量签出API
    // await batchCheckOut(selectedRows.value.map(row => row.id))
    
    ElMessage.success('批量签出成功')
    clearSelection()
    handleSearch()
  } catch {
    // 用户取消操作
  }
}

// 确认签出
const confirmCheckOut = async () => {
  if (!checkOutDialog.attendance) return
  
  try {
    // 调用签出API
    // await checkOut(checkOutDialog.attendance.id, checkOutForm)
    
    ElMessage.success('签出成功')
    checkOutDialog.visible = false
    handleSearch()
  } catch (error) {
    console.error('签出失败:', error)
    ElMessage.error('签出失败')
  }
}

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    // 每日签到趋势图表
    if (dailyTrendChart.value) {
      const trendChart = echarts.init(dailyTrendChart.value)
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: (params: any) => {
            return `${params[0].axisValue}<br/>签到人数: ${params[0].value}人`
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['6/8', '6/9', '6/10', '6/11', '6/12', '6/13', '6/14', '6/15']
        },
        yAxis: {
          type: 'value',
          name: '人次'
        },
        series: [
          {
            name: '签到人数',
            type: trendChartType.value,
            data: [85, 73, 92, 88, 106, 95, 112, 98],
            smooth: true,
            lineStyle: { width: 3 },
            itemStyle: { color: '#409eff' },
            areaStyle: trendChartType.value === 'line' ? {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
              ])
            } : undefined
          }
        ]
      }
      trendChart.setOption(option)
      
      window.addEventListener('resize', () => {
        trendChart.resize()
      })
    }
    
    // 时段分布图表
    if (timeDistributionChart.value) {
      const timeChart = echarts.init(timeDistributionChart.value)
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
          data: ['6-8点', '8-10点', '10-12点', '12-14点', '14-16点', '16-18点', '18-20点', '20-22点']
        },
        yAxis: {
          type: 'value',
          name: '人次'
        },
        series: [
          {
            name: '签到人数',
            type: 'bar',
            data: [12, 58, 42, 25, 38, 65, 89, 47],
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
          }
        ]
      }
      timeChart.setOption(option)
      
      window.addEventListener('resize', () => {
        timeChart.resize()
      })
    }
    
    // 课程出勤统计图表
    if (courseAttendanceChart.value) {
      const courseChart = echarts.init(courseAttendanceChart.value)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: (params: any) => {
            return `${params.name}<br/>出勤率: ${params.percent}%<br/>参与人数: ${params.value}人`
          }
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'center'
        },
        series: [
          {
            name: '课程出勤',
            type: 'pie',
            radius: '70%',
            data: [
              { value: 156, name: '晨间瑜伽', itemStyle: { color: '#67c23a' } },
              { value: 128, name: '力量训练', itemStyle: { color: '#409eff' } },
              { value: 95, name: '动感单车', itemStyle: { color: '#e6a23c' } },
              { value: 78, name: '普拉提', itemStyle: { color: '#f56c6c' } },
              { value: 62, name: '私教课程', itemStyle: { color: '#909399' } }
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
      courseChart.setOption(option)
      
      window.addEventListener('resize', () => {
        courseChart.resize()
      })
    }
    
    // 会员活跃度分布图表
    if (memberActivityChart.value) {
      const activityChart = echarts.init(memberActivityChart.value)
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
          data: ['0-2次', '3-5次', '6-8次', '9-11次', '12次以上']
        },
        yAxis: {
          type: 'value',
          name: '会员数'
        },
        series: [
          {
            name: '会员数',
            type: 'bar',
            data: [35, 48, 32, 18, 9],
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#f56c6c' },
                { offset: 0.5, color: '#e6a23c' },
                { offset: 1, color: '#67c23a' }
              ])
            }
          }
        ]
      }
      activityChart.setOption(option)
      
      window.addEventListener('resize', () => {
        activityChart.resize()
      })
    }
  })
}

// 监听图表类型变化
watch(trendChartType, () => {
  initCharts()
})

// 初始化
onMounted(() => {
  // 设置默认日期范围（最近一周）
  const today = new Date()
  const lastWeek = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
  filterParams.dateRange = [
    lastWeek.toISOString().split('T')[0],
    today.toISOString().split('T')[0]
  ]
  
  handleSearch()
})
</script>

<style scoped lang="scss">
.attendance-report-container {
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

      &.total-checkins {
        background: rgba(64, 158, 255, 0.1);
        color: #409eff;
      }

      &.unique-members {
        background: rgba(103, 194, 58, 0.1);
        color: #67c23a;
      }

      &.avg-frequency {
        background: rgba(230, 162, 60, 0.1);
        color: #e6a23c;
      }

      &.attendance-rate {
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

  .batch-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    background: #f0f9ff;
    border-top: 1px solid #ebeef5;

    .batch-info {
      color: #409eff;
      font-size: 14px;
    }

    .batch-buttons {
      display: flex;
      gap: 10px;
    }
  }

  .action-buttons {
    display: flex;
    gap: 8px;
  }
}

.checkout-dialog {
  .el-form-item {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 20px;
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
  }
  
  .batch-actions {
    flex-direction: column;
    gap: 10px;
    
    .batch-buttons {
      width: 100%;
      justify-content: flex-end;
    }
  }
}
</style>