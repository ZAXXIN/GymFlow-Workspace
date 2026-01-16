<template>
  <div class="health-record-container">
    <!-- 头部 -->
    <div class="section-header">
      <h3 class="section-title">健康档案</h3>
      <el-button type="primary" @click="handleAddRecord">
        <el-icon><Plus /></el-icon>
        新增记录
      </el-button>
    </div>
    
    <!-- 健康趋势图 -->
    <el-card class="chart-card" v-if="records.length > 0">
      <div class="chart-container">
        <div ref="healthChartRef" class="chart" style="height: 300px;"></div>
      </div>
    </el-card>
    
    <!-- 健康记录列表 -->
    <el-card class="records-card">
      <el-table :data="records" v-loading="loading">
        <el-table-column prop="recordDate" label="记录日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.recordDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="height" label="身高(cm)" width="100" align="center" />
        <el-table-column prop="weight" label="体重(kg)" width="100" align="center" />
        <el-table-column prop="bmi" label="BMI" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getBMIType(row.bmi)" size="small">
              {{ row.bmi.toFixed(1) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bodyFatPercentage" label="体脂率(%)" width="120" align="center">
          <template #default="{ row }">
            {{ row.bodyFatPercentage || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="muscleMass" label="肌肉量(kg)" width="120" align="center">
          <template #default="{ row }">
            {{ row.muscleMass || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="bloodPressure" label="血压" width="120" align="center">
          <template #default="{ row }">
            {{ row.bloodPressure || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="heartRate" label="心率" width="100" align="center">
          <template #default="{ row }">
            {{ row.heartRate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="notes" label="备注" min-width="200">
          <template #default="{ row }">
            <span class="notes-text">{{ row.notes || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="text" size="small" class="delete-btn" @click="handleDelete(row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空状态 -->
      <div v-if="records.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无健康记录">
          <el-button type="primary" @click="handleAddRecord">
            添加第一条记录
          </el-button>
        </el-empty>
      </div>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="recordDialog.visible"
      :title="recordDialog.title"
      width="500px"
    >
      <el-form
        ref="recordFormRef"
        :model="recordForm"
        :rules="recordRules"
        label-width="100px"
      >
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker
            v-model="recordForm.recordDate"
            type="date"
            placeholder="选择记录日期"
            style="width: 100%;"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身高(cm)" prop="height">
              <el-input-number
                v-model="recordForm.height"
                :min="100"
                :max="250"
                :step="1"
                controls-position="right"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)" prop="weight">
              <el-input-number
                v-model="recordForm.weight"
                :min="30"
                :max="200"
                :step="0.5"
                controls-position="right"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="体脂率(%)" prop="bodyFatPercentage">
              <el-input-number
                v-model="recordForm.bodyFatPercentage"
                :min="5"
                :max="50"
                :step="0.1"
                controls-position="right"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="肌肉量(kg)" prop="muscleMass">
              <el-input-number
                v-model="recordForm.muscleMass"
                :min="20"
                :max="100"
                :step="0.1"
                controls-position="right"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="血压" prop="bloodPressure">
              <el-input
                v-model="recordForm.bloodPressure"
                placeholder="如：120/80"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="心率" prop="heartRate">
              <el-input-number
                v-model="recordForm.heartRate"
                :min="40"
                :max="200"
                :step="1"
                controls-position="right"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="备注" prop="notes">
          <el-input
            v-model="recordForm.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="recordDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleRecordSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { memberApi } from '@/api/member'
import { formatDate } from '@/utils'
import type { HealthRecord } from '@/types'

// Route
const route = useRoute()

// Refs
const loading = ref(false)
const healthChartRef = ref<HTMLElement>()
let healthChart: echarts.ECharts | null = null

// 状态
const records = ref<HealthRecord[]>([])

const recordDialog = reactive({
  visible: false,
  title: '新增健康记录',
  mode: 'create' as 'create' | 'edit',
  recordId: 0
})

const recordForm = reactive({
  recordDate: '',
  height: 170,
  weight: 65,
  bodyFatPercentage: undefined as number | undefined,
  muscleMass: undefined as number | undefined,
  bloodPressure: '',
  heartRate: undefined as number | undefined,
  notes: ''
})

const recordRules: FormRules = {
  recordDate: [
    { required: true, message: '请选择记录日期', trigger: 'change' }
  ],
  height: [
    { required: true, message: '请输入身高', trigger: 'blur' }
  ],
  weight: [
    { required: true, message: '请输入体重', trigger: 'blur' }
  ]
}

const recordFormRef = ref<FormInstance>()

// Computed
const memberId = computed(() => parseInt(route.params.id as string))

// Methods
const loadHealthRecords = async () => {
  try {
    loading.value = true
    const data = await memberApi.getHealthRecords(memberId.value)
    records.value = data.sort((a, b) => 
      new Date(b.recordDate).getTime() - new Date(a.recordDate).getTime()
    )
    
    if (records.value.length > 0) {
      nextTick(() => {
        initHealthChart()
      })
    }
  } catch (error) {
    console.error('加载健康记录失败:', error)
    ElMessage.error('加载健康记录失败')
  } finally {
    loading.value = false
  }
}

const initHealthChart = () => {
  if (!healthChartRef.value || records.value.length === 0) return
  
  if (healthChart) {
    healthChart.dispose()
  }
  
  healthChart = echarts.init(healthChartRef.value)
  
  // 准备数据
  const sortedRecords = [...records.value].sort((a, b) => 
    new Date(a.recordDate).getTime() - new Date(b.recordDate).getTime()
  )
  
  const dates = sortedRecords.map(record => formatDate(record.recordDate, 'MM-dd'))
  const weights = sortedRecords.map(record => record.weight)
  const bmis = sortedRecords.map(record => record.bmi)
  const bodyFats = sortedRecords.map(record => record.bodyFatPercentage || null)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['体重', 'BMI', '体脂率']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '体重(kg)',
        position: 'left'
      },
      {
        type: 'value',
        name: 'BMI/体脂率',
        position: 'right'
      }
    ],
    series: [
      {
        name: '体重',
        type: 'line',
        smooth: true,
        data: weights,
        itemStyle: {
          color: '#5470c6'
        },
        lineStyle: {
          width: 3
        }
      },
      {
        name: 'BMI',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: bmis,
        itemStyle: {
          color: '#91cc75'
        },
        lineStyle: {
          width: 3
        }
      },
      {
        name: '体脂率',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: bodyFats,
        itemStyle: {
          color: '#fac858'
        },
        lineStyle: {
          width: 3,
          type: 'dashed'
        }
      }
    ]
  }
  
  healthChart.setOption(option)
}

const getBMIType = (bmi: number) => {
  if (bmi < 18.5) return 'warning'
  if (bmi < 24) return 'success'
  if (bmi < 28) return 'warning'
  return 'danger'
}

const handleAddRecord = () => {
  recordDialog.mode = 'create'
  recordDialog.title = '新增健康记录'
  recordDialog.recordId = 0
  resetRecordForm()
  recordDialog.visible = true
}

const handleEdit = (record: HealthRecord) => {
  recordDialog.mode = 'edit'
  recordDialog.title = '编辑健康记录'
  recordDialog.recordId = record.id
  
  // 填充表单数据
  Object.assign(recordForm, {
    recordDate: record.recordDate,
    height: record.height,
    weight: record.weight,
    bodyFatPercentage: record.bodyFatPercentage,
    muscleMass: record.muscleMass,
    bloodPressure: record.bloodPressure || '',
    heartRate: record.heartRate,
    notes: record.notes || ''
  })
  
  recordDialog.visible = true
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该条记录吗？', '删除记录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await memberApi.deleteHealthRecord(memberId.value, id)
    ElMessage.success('删除成功')
    loadHealthRecords()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const handleRecordSubmit = async () => {
  if (!recordFormRef.value) return
  
  try {
    await recordFormRef.value.validate()
    
    const data = {
      ...recordForm,
      // 计算BMI
      bmi: calculateBMI(recordForm.height, recordForm.weight)
    }
    
    if (recordDialog.mode === 'create') {
      await memberApi.addHealthRecord(memberId.value, data)
      ElMessage.success('添加成功')
    } else {
      await memberApi.updateHealthRecord(memberId.value, recordDialog.recordId, data)
      ElMessage.success('更新成功')
    }
    
    recordDialog.visible = false
    loadHealthRecords()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  }
}

const resetRecordForm = () => {
  Object.assign(recordForm, {
    recordDate: new Date().toISOString().split('T')[0],
    height: 170,
    weight: 65,
    bodyFatPercentage: undefined,
    muscleMass: undefined,
    bloodPressure: '',
    heartRate: undefined,
    notes: ''
  })
}

const handleResize = () => {
  if (healthChart) {
    healthChart.resize()
  }
}

// 生命周期
onMounted(() => {
  loadHealthRecords()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (healthChart) {
    healthChart.dispose()
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.health-record-container {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .section-title {
      font-size: 18px;
      font-weight: 600;
      color: var(--gymflow-text-primary);
      margin: 0;
    }
  }
  
  .chart-card {
    margin-bottom: 20px;
    border-radius: 12px;
    
    :deep(.el-card__body) {
      padding: 20px;
    }
    
    .chart-container {
      .chart {
        width: 100%;
      }
    }
  }
  
  .records-card {
    border-radius: 12px;
    
    :deep(.el-card__body) {
      padding: 0;
    }
    
    :deep(.el-table) {
      th {
        background: var(--gymflow-bg);
        font-weight: 600;
      }
      
      .notes-text {
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .delete-btn {
        color: var(--el-color-danger);
        
        &:hover {
          background: rgba(245, 108, 108, 0.1);
        }
      }
    }
    
    .empty-state {
      padding: 60px 0;
    }
  }
}

// 对话框样式
:deep(.el-dialog) {
  border-radius: 12px;
  
  .el-dialog__header {
    padding: 20px;
    border-bottom: 1px solid var(--gymflow-border);
  }
  
  .el-dialog__body {
    padding: 20px;
    
    .el-form-item {
      margin-bottom: 20px;
    }
  }
  
  .el-dialog__footer {
    padding: 20px;
    border-top: 1px solid var(--gymflow-border);
  }
}
</style>