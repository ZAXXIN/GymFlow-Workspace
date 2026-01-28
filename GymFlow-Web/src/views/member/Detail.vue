<template>
  <div class="member-detail">
    <el-page-header @back="goBack">
      <template #content>
        <span class="text-large font-600 mr-3"> 会员详情 </span>
      </template>
    </el-page-header>
    
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <el-button type="primary" @click="goEdit">编辑</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="会员ID">{{ memberId }}</el-descriptions-item>
        <el-descriptions-item label="会员编号">M001234</el-descriptions-item>
        <el-descriptions-item label="姓名">张三</el-descriptions-item>
        <el-descriptions-item label="手机号">13800138000</el-descriptions-item>
        <el-descriptions-item label="性别">男</el-descriptions-item>
        <el-descriptions-item label="年龄">28</el-descriptions-item>
        <el-descriptions-item label="会籍类型">年卡会员</el-descriptions-item>
        <el-descriptions-item label="专属教练">李教练</el-descriptions-item>
        <el-descriptions-item label="会籍开始">2024-01-01</el-descriptions-item>
        <el-descriptions-item label="会籍结束">2024-12-31</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag type="success">有效</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="累计消费">¥5,800.00</el-descriptions-item>
      </el-descriptions>
      
      <el-divider />
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="健康档案" name="health">
          <div class="health-records">
            <el-table :data="healthData" style="width: 100%">
              <el-table-column prop="date" label="记录日期" width="180" />
              <el-table-column prop="weight" label="体重(kg)" width="100" />
              <el-table-column prop="bodyFat" label="体脂率(%)" width="100" />
              <el-table-column prop="bmi" label="BMI" width="100" />
              <el-table-column prop="muscle" label="肌肉量(kg)" width="100" />
              <el-table-column prop="recordedBy" label="记录人" />
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="课程记录" name="courses">
          <div class="course-records">
            <el-table :data="courseData" style="width: 100%">
              <el-table-column prop="date" label="上课日期" width="180" />
              <el-table-column prop="courseName" label="课程名称" />
              <el-table-column prop="coach" label="教练" width="120" />
              <el-table-column prop="duration" label="时长(分钟)" width="100" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === '已完成' ? 'success' : 'warning'">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="签到记录" name="checkins">
          <div class="checkin-records">
            <el-table :data="checkinData" style="width: 100%">
              <el-table-column prop="date" label="签到日期" width="180" />
              <el-table-column prop="time" label="签到时间" width="120" />
              <el-table-column prop="course" label="关联课程" />
              <el-table-column prop="method" label="签到方式" width="100">
                <template #default="scope">
                  {{ scope.row.method === 0 ? '教练签到' : '前台签到' }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const memberId = computed(() => route.params.id)
const activeTab = ref('health')

// 模拟数据
const healthData = ref([
  { date: '2024-01-15', weight: 75.2, bodyFat: 18.5, bmi: 24.5, muscle: 58.5, recordedBy: '张教练' },
  { date: '2024-01-01', weight: 76.5, bodyFat: 19.2, bmi: 24.9, muscle: 57.8, recordedBy: '张教练' },
  { date: '2023-12-15', weight: 77.8, bodyFat: 20.1, bmi: 25.3, muscle: 57.2, recordedBy: '张教练' }
])

const courseData = ref([
  { date: '2024-01-20', courseName: '私教课-增肌训练', coach: '张教练', duration: 60, status: '已完成' },
  { date: '2024-01-18', courseName: '团课-瑜伽', coach: '王教练', duration: 45, status: '已完成' },
  { date: '2024-01-15', courseName: '私教课-体能训练', coach: '张教练', duration: 60, status: '已完成' }
])

const checkinData = ref([
  { date: '2024-01-20', time: '14:30', course: '私教课-增肌训练', method: 0 },
  { date: '2024-01-18', time: '19:00', course: '团课-瑜伽', method: 1 },
  { date: '2024-01-15', time: '16:00', course: '私教课-体能训练', method: 0 }
])

const goBack = () => {
  router.back()
}

const goEdit = () => {
  router.push(`/member/edit/${memberId.value}`)
}
</script>

<style scoped>
.member-detail {
  padding: 20px;
}

.detail-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.health-records,
.course-records,
.checkin-records {
  margin-top: 20px;
}
</style>