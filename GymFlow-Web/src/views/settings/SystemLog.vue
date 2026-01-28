<template>
  <div class="system-log">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h3 class="card-title">系统日志</h3>
          <div class="card-subtitle">查看系统操作和事件记录</div>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <div class="log-filters">
        <el-form :model="filterForm" :inline="true">
          <el-form-item label="日志类型">
            <el-select
              v-model="filterForm.logType"
              placeholder="全部类型"
              clearable
              style="width: 150px;"
            >
              <el-option label="操作日志" value="OPERATION" />
              <el-option label="登录日志" value="LOGIN" />
              <el-option label="错误日志" value="ERROR" />
              <el-option label="系统日志" value="SYSTEM" />
              <el-option label="安全日志" value="SECURITY" />
            </el-select>
          </el-form-item>

          <el-form-item label="操作模块">
            <el-select
              v-model="filterForm.module"
              placeholder="全部模块"
              clearable
              filterable
              style="width: 150px;"
            >
              <el-option
                v-for="module in moduleOptions"
                :key="module"
                :label="module"
                :value="module"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="操作人">
            <el-select
              v-model="filterForm.operator"
              placeholder="全部操作人"
              clearable
              filterable
              remote
              :remote-method="searchOperators"
              :loading="searchingOperators"
              style="width: 180px;"
            >
              <el-option
                v-for="operator in operatorOptions"
                :key="operator.id"
                :label="operator.name"
                :value="operator.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="操作时间">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px;"
            />
          </el-form-item>

          <el-form-item label="操作结果">
            <el-select
              v-model="filterForm.result"
              placeholder="全部结果"
              clearable
              style="width: 120px;"
            >
              <el-option label="成功" value="SUCCESS" />
              <el-option label="失败" value="FAILED" />
              <el-option label="异常" value="ERROR" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="searchLogs" :loading="searching">
              查询
            </el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 日志统计 -->
      <div class="log-stats">
        <el-row :gutter="20">
          <el-col :span="6" v-for="stat in logStats" :key="stat.type">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
                <div class="stat-trend" :class="getTrendClass(stat.trend)">
                  {{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%
                </div>
              </div>
              <div class="stat-icon">
                <el-icon :size="24" :color="stat.color">
                  <component :is="stat.icon" />
                </el-icon>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 日志表格 -->
      <div class="log-table">
        <div class="table-header">
          <div class="table-title">日志记录</div>
          <div class="table-actions">
            <el-button
              type="primary"
              :loading="exporting"
              @click="exportLogs"
              :disabled="logList.length === 0"
            >
              导出日志
            </el-button>
            <el-button
              type="danger"
              @click="clearLogs"
              :disabled="logList.length === 0"
            >
              清空日志
            </el-button>
          </div>
        </div>

        <el-table
          ref="logTableRef"
          :data="logList"
          style="width: 100%"
          stripe
          border
          :default-sort="{ prop: 'createTime', order: 'descending' }"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
        >
          <el-table-column type="selection" width="55" />
          
          <el-table-column label="时间" prop="createTime" width="160" sortable>
            <template #default="{ row }">
              <div class="log-time">
                <div class="time-date">{{ formatDate(row.createTime) }}</div>
                <div class="time-time">{{ formatTime(row.createTime) }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="类型" prop="logType" width="100">
            <template #default="{ row }">
              <el-tag :type="getLogTypeTag(row.logType)" size="small">
                {{ getLogTypeText(row.logType) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="模块" prop="module" width="120" show-overflow-tooltip />

          <el-table-column label="操作" prop="operation" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="log-operation">
                <div class="operation-title">{{ row.operation }}</div>
                <div class="operation-desc" v-if="row.description">{{ row.description }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作人" prop="operatorName" width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="operator-info">
                <div class="operator-name">{{ row.operatorName }}</div>
                <div class="operator-role">{{ row.operatorRole }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="IP地址" prop="ip" width="140" />

          <el-table-column label="结果" prop="result" width="80">
            <template #default="{ row }">
              <el-tag :type="getResultTag(row.result)" size="small">
                {{ getResultText(row.result) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="耗时" prop="duration" width="80" align="right">
            <template #default="{ row }">
              <span v-if="row.duration">{{ row.duration }}ms</span>
              <span v-else>-</span>
            </template>
          </el-table-column>

          <el-table-column label="详情" width="80" align="center">
            <template #default="{ row }">
              <el-button type="text" size="small" @click="viewLogDetail(row)">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <!-- 批量操作 -->
      <div class="batch-actions" v-if="selectedLogs.length > 0">
        <el-space>
          <span>已选择 {{ selectedLogs.length }} 条日志</span>
          <el-button type="danger" size="small" @click="deleteSelectedLogs">
            批量删除
          </el-button>
          <el-button type="primary" size="small" @click="exportSelectedLogs">
            批量导出
          </el-button>
        </el-space>
      </div>
    </el-card>

    <!-- 日志详情弹窗 -->
    <el-dialog
      v-model="showDetailDialog"
      title="日志详情"
      width="600px"
      destroy-on-close
    >
      <div class="log-detail" v-if="selectedLog">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="日志ID">
            {{ selectedLog.id }}
          </el-descriptions-item>
          <el-descriptions-item label="时间">
            {{ formatDateTime(selectedLog.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getLogTypeTag(selectedLog.logType)" size="small">
              {{ getLogTypeText(selectedLog.logType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="模块">
            {{ selectedLog.module }}
          </el-descriptions-item>
          <el-descriptions-item label="操作">
            {{ selectedLog.operation }}
          </el-descriptions-item>
          <el-descriptions-item label="操作描述" v-if="selectedLog.description">
            {{ selectedLog.description }}
          </el-descriptions-item>
          <el-descriptions-item label="操作人">
            {{ selectedLog.operatorName }} ({{ selectedLog.operatorRole }})
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ selectedLog.ip }}
          </el-descriptions-item>
          <el-descriptions-item label="用户代理" v-if="selectedLog.userAgent">
            {{ selectedLog.userAgent }}
          </el-descriptions-item>
          <el-descriptions-item label="请求方法" v-if="selectedLog.method">
            {{ selectedLog.method }}
          </el-descriptions-item>
          <el-descriptions-item label="请求地址" v-if="selectedLog.url">
            {{ selectedLog.url }}
          </el-descriptions-item>
          <el-descriptions-item label="请求参数" v-if="selectedLog.params">
            <pre class="params-content">{{ formatParams(selectedLog.params) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="操作结果">
            <el-tag :type="getResultTag(selectedLog.result)" size="small">
              {{ getResultText(selectedLog.result) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="错误信息" v-if="selectedLog.errorMessage">
            <div class="error-content">{{ selectedLog.errorMessage }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="耗时">
            {{ selectedLog.duration || 0 }}ms
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-actions">
          <el-button type="primary" @click="showDetailDialog = false">
            关闭
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 日志图表 -->
    <el-card shadow="never" class="log-chart" style="margin-top: 20px;">
      <template #header>
        <div class="chart-header">
          <h3>日志统计图表</h3>
          <div class="chart-controls">
            <el-select
              v-model="chartType"
              placeholder="选择图表类型"
              size="small"
              style="width: 120px;"
            >
              <el-option label="按类型统计" value="type" />
              <el-option label="按模块统计" value="module" />
              <el-option label="按时段统计" value="time" />
              <el-option label="按结果统计" value="result" />
            </el-select>
            
            <el-date-picker
              v-model="chartDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              size="small"
              style="width: 240px; margin-left: 10px;"
              @change="loadChartData"
            />
          </div>
        </div>
      </template>

      <div class="chart-container" ref="logChartRef"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { useChart } from '@/composables/useChart'
import type { LogRecord } from '@/types'

// 图表相关
const logChartRef = ref<HTMLElement>()
const logChart = useChart({ autoResize: true })

// 响应式数据
const filterForm = ref({
  logType: '',
  module: '',
  operator: '',
  dateRange: [] as string[],
  result: ''
})

const logList = ref<LogRecord[]>([])
const selectedLogs = ref<LogRecord[]>([])
const selectedLog = ref<LogRecord | null>(null)
const showDetailDialog = ref(false)
const moduleOptions = ref<string[]>([])
const operatorOptions = ref<any[]>([])
const chartType = ref('type')
const chartDateRange = ref<string[]>([])

// 加载状态
const searching = ref(false)
const searchingOperators = ref(false)
const exporting = ref(false)

// 分页配置
const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 统计卡片数据
const logStats = ref([
  {
    type: 'total',
    label: '总日志数',
    value: '1,245',
    trend: 12.5,
    color: '#409eff',
    icon: Document
  },
  {
    type: 'operation',
    label: '操作日志',
    value: '856',
    trend: 8.3,
    color: '#67c23a',
    icon: Document
  },
  {
    type: 'login',
    label: '登录日志',
    value: '289',
    trend: 15.2,
    color: '#e6a23c',
    icon: User
  },
  {
    type: 'error',
    label: '错误日志',
    value: '100',
    trend: -5.7,
    color: '#f56c6c',
    icon: Warning
  }
])

// 计算属性
const getTrendClass = (trend: number) => {
  if (trend > 0) return 'positive'
  if (trend < 0) return 'negative'
  return 'neutral'
}

const getLogTypeTag = (type: string) => {
  const typeMap: Record<string, any> = {
    'OPERATION': 'primary',
    'LOGIN': 'success',
    'ERROR': 'danger',
    'SYSTEM': 'info',
    'SECURITY': 'warning'
  }
  return typeMap[type] || 'info'
}

const getLogTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    'OPERATION': '操作',
    'LOGIN': '登录',
    'ERROR': '错误',
    'SYSTEM': '系统',
    'SECURITY': '安全'
  }
  return textMap[type] || type
}

const getResultTag = (result: string) => {
  if (result === 'SUCCESS') return 'success'
  if (result === 'FAILED') return 'warning'
  if (result === 'ERROR') return 'danger'
  return 'info'
}

const getResultText = (result: string) => {
  const textMap: Record<string, string> = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'ERROR': '异常'
  }
  return textMap[result] || result
}

// 方法
const formatDate = (dateTime: string) => {
  if (!dateTime) return ''
  return dateTime.split(' ')[0]
}

const formatTime = (dateTime: string) => {
  if (!dateTime) return ''
  return dateTime.split(' ')[1]?.substring(0, 8) || ''
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return dateTime.replace('T', ' ')
}

const formatParams = (params: any) => {
  if (typeof params === 'string') {
    try {
      return JSON.stringify(JSON.parse(params), null, 2)
    } catch {
      return params
    }
  } else if (typeof params === 'object') {
    return JSON.stringify(params, null, 2)
  }
  return params
}

// 搜索操作人
const searchOperators = async (query: string) => {
  if (!query) {
    operatorOptions.value = []
    return
  }
  
  searchingOperators.value = true
  try {
    // 这里应该调用API搜索操作人
    // 暂时使用模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    
    operatorOptions.value = [
      { id: '1', name: '管理员' },
      { id: '2', name: '张教练' },
      { id: '3', name: '李前台' },
      { id: '4', name: '王会员' }
    ].filter(operator => operator.name.includes(query))
  } catch (error) {
    console.error('搜索操作人失败:', error)
  } finally {
    searchingOperators.value = false
  }
}

// 搜索日志
const searchLogs = async () => {
  searching.value = true
  
  try {
    // 这里应该调用API搜索日志
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 模拟数据
    const mockLogs: LogRecord[] = []
    const now = new Date()
    
    for (let i = 0; i < 100; i++) {
      const logTime = new Date(now.getTime() - i * 60000) // 每分钟一条
      const types: any[] = ['OPERATION', 'LOGIN', 'ERROR', 'SYSTEM', 'SECURITY']
      const modules = ['会员管理', '教练管理', '课程管理', '订单管理', '系统设置']
      const results: any[] = ['SUCCESS', 'FAILED', 'ERROR']
      const operators = [
        { name: '管理员', role: '管理员' },
        { name: '张教练', role: '教练' },
        { name: '李前台', role: '前台' }
      ]
      
      const type = types[i % types.length]
      const result = results[i % results.length]
      const operator = operators[i % operators.length]
      
      mockLogs.push({
        id: `log-${i}`,
        createTime: logTime.toISOString().replace('T', ' ').substring(0, 19),
        logType: type,
        module: modules[i % modules.length],
        operation: i % 3 === 0 ? '新增数据' : i % 3 === 1 ? '修改数据' : '删除数据',
        description: i % 5 === 0 ? `操作了ID为${i}的数据` : '',
        operatorId: (i % 3 + 1).toString(),
        operatorName: operator.name,
        operatorRole: operator.role,
        ip: `192.168.1.${100 + (i % 50)}`,
        userAgent: i % 2 === 0 ? 'Mozilla/5.0 (Windows NT 10.0)' : 'Mozilla/5.0 (iPhone)',
        method: i % 2 === 0 ? 'POST' : 'GET',
        url: `/api/${modules[i % modules.length].toLowerCase()}/${i}`,
        params: JSON.stringify({ id: i, name: `数据${i}` }),
        result: result,
        errorMessage: result === 'ERROR' ? '数据库连接失败' : '',
        duration: Math.floor(Math.random() * 500) + 50
      })
    }
    
    // 应用筛选条件
    let filteredLogs = mockLogs
    
    if (filterForm.value.logType) {
      filteredLogs = filteredLogs.filter(log => log.logType === filterForm.value.logType)
    }
    
    if (filterForm.value.module) {
      filteredLogs = filteredLogs.filter(log => log.module === filterForm.value.module)
    }
    
    if (filterForm.value.result) {
      filteredLogs = filteredLogs.filter(log => log.result === filterForm.value.result)
    }
    
    if (filterForm.value.dateRange?.length === 2) {
      const [start, end] = filterForm.value.dateRange
      filteredLogs = filteredLogs.filter(log => {
        const logDate = log.createTime.split(' ')[0]
        return logDate >= start && logDate <= end
      })
    }
    
    // 更新分页
    const startIndex = (pagination.value.currentPage - 1) * pagination.value.pageSize
    const endIndex = startIndex + pagination.value.pageSize
    
    logList.value = filteredLogs.slice(startIndex, endIndex)
    pagination.value.total = filteredLogs.length
    
  } catch (error) {
    console.error('搜索日志失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    searching.value = false
  }
}

// 重置筛选条件
const resetFilters = () => {
  filterForm.value = {
    logType: '',
    module: '',
    operator: '',
    dateRange: [],
    result: ''
  }
  searchLogs()
}

// 选择日志变化
const handleSelectionChange = (selection: LogRecord[]) => {
  selectedLogs.value = selection
}

// 排序变化
const handleSortChange = (sort: any) => {
  console.log('排序变化:', sort)
  // 这里应该根据排序重新加载数据
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.currentPage = 1
  searchLogs()
}

// 页码变化
const handlePageChange = (page: number) => {
  pagination.value.currentPage = page
  searchLogs()
}

// 查看日志详情
const viewLogDetail = (log: LogRecord) => {
  selectedLog.value = log
  showDetailDialog.value = true
}

// 批量删除日志
const deleteSelectedLogs = async () => {
  if (selectedLogs.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedLogs.value.length} 条日志吗？此操作不可恢复。`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    // 这里应该调用API批量删除
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 从列表中移除已删除的日志
    const selectedIds = selectedLogs.value.map(log => log.id)
    logList.value = logList.value.filter(log => !selectedIds.includes(log.id))
    
    selectedLogs.value = []
    
    ElMessage.success('删除成功')
  } catch (error) {
    // 用户取消
  }
}

// 导出日志
const exportLogs = async () => {
  exporting.value = true
  
  try {
    // 这里应该调用API导出日志
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 模拟下载
    const blob = new Blob(['日志数据'], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `系统日志_${new Date().toISOString().split('T')[0]}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 导出选中日志
const exportSelectedLogs = async () => {
  if (selectedLogs.value.length === 0) return
  
  exporting.value = true
  
  try {
    // 这里应该调用API导出选中日志
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 模拟下载
    const blob = new Blob(['选中的日志数据'], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `选中日志_${new Date().toISOString().split('T')[0]}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 清空日志
const clearLogs = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有日志吗？此操作不可恢复。',
      '确认清空',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    // 这里应该调用API清空日志
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    logList.value = []
    pagination.value.total = 0
    
    ElMessage.success('清空成功')
  } catch (error) {
    // 用户取消
  }
}

// 加载图表数据
const loadChartData = async () => {
  logChart.chartContainer = logChartRef
  
  // 根据chartType显示不同的图表
  let option = {}
  
  if (chartType.value === 'type') {
    option = {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '日志类型分布',
          type: 'pie',
          radius: '50%',
          data: [
            { value: 856, name: '操作日志' },
            { value: 289, name: '登录日志' },
            { value: 100, name: '错误日志' },
            { value: 50, name: '系统日志' },
            { value: 30, name: '安全日志' }
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
  } else if (chartType.value === 'module') {
    option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: ['会员管理', '教练管理', '课程管理', '订单管理', '系统设置', '签到管理', '报表统计']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '日志数量',
          type: 'bar',
          data: [120, 200, 150, 80, 70, 110, 130],
          itemStyle: {
            color: '#409eff'
          }
        }
      ]
    }
  } else if (chartType.value === 'time') {
    option = {
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '日志数量',
          type: 'line',
          data: [30, 15, 120, 180, 150, 80, 40],
          smooth: true,
          itemStyle: {
            color: '#67c23a'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(103, 194, 58, 0.5)' },
              { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
            ])
          }
        }
      ]
    }
  } else {
    option = {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '操作结果分布',
          type: 'pie',
          radius: ['40%', '70%'],
          data: [
            { value: 1000, name: '成功' },
            { value: 200, name: '失败' },
            { value: 45, name: '异常' }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          itemStyle: {
            color: (params: any) => {
              const colors = ['#67c23a', '#e6a23c', '#f56c6c']
              return colors[params.dataIndex] || '#409eff'
            }
          }
        }
      ]
    }
  }
  
  logChart.setChartOption(option)
}

// 初始化模块选项
const loadModuleOptions = () => {
  moduleOptions.value = [
    '会员管理',
    '教练管理',
    '课程管理',
    '订单管理',
    '签到管理',
    '报表统计',
    '系统设置',
    '用户管理',
    '权限管理',
    '日志管理'
  ]
}

// 初始化
onMounted(() => {
  loadModuleOptions()
  searchLogs()
  loadChartData()
  
  // 设置默认图表日期范围（最近7天）
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(endDate.getDate() - 7)
  
  chartDateRange.value = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
})

onUnmounted(() => {
  logChart.disposeChart()
})
</script>

<style scoped lang="scss">
.system-log {
  .card-header {
    .card-title {
      margin: 0 0 8px 0;
      font-size: 18px;
      color: #303133;
    }
    
    .card-subtitle {
      margin: 0;
      font-size: 14px;
      color: #909399;
    }
  }
  
  .log-filters {
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #e4e7ed;
  }
  
  .log-stats {
    margin-bottom: 20px;
    
    .stat-card {
      .stat-content {
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #303133;
          margin-bottom: 4px;
        }
        
        .stat-label {
          font-size: 14px;
          color: #606266;
          margin-bottom: 4px;
        }
        
        .stat-trend {
          font-size: 12px;
          
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
      
      .stat-icon {
        position: absolute;
        top: 20px;
        right: 20px;
      }
    }
  }
  
  .log-table {
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      
      .table-title {
        font-size: 16px;
        color: #303133;
        font-weight: 500;
      }
    }
    
    .log-time {
      .time-date {
        color: #303133;
      }
      
      .time-time {
        color: #909399;
        font-size: 12px;
      }
    }
    
    .log-operation {
      .operation-title {
        color: #303133;
      }
      
      .operation-desc {
        color: #909399;
        font-size: 12px;
        margin-top: 2px;
      }
    }
    
    .operator-info {
      .operator-name {
        color: #303133;
      }
      
      .operator-role {
        color: #909399;
        font-size: 12px;
      }
    }
    
    .pagination-container {
      margin-top: 20px;
      text-align: right;
    }
  }
  
  .batch-actions {
    margin-top: 20px;
    padding: 15px;
    background-color: #f0f9ff;
    border-radius: 4px;
    border: 1px solid #c6e2ff;
  }
  
  .log-detail {
    .params-content,
    .error-content {
      background-color: #f8f9fa;
      padding: 10px;
      border-radius: 4px;
      font-family: monospace;
      font-size: 12px;
      white-space: pre-wrap;
      word-break: break-all;
      max-height: 200px;
      overflow-y: auto;
    }
    
    .detail-actions {
      margin-top: 20px;
      text-align: center;
    }
  }
  
  .log-chart {
    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
        color: #303133;
      }
    }
    
    .chart-container {
      height: 400px;
    }
  }
}
</style>