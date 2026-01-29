<template>
  <div class="member-detail-container">
    <!-- 页面头部 -->
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <div class="header-content">
          <span class="page-title">会员详情</span>
          <!-- <div class="header-actions">
            <el-button type="primary" @click="goEdit">编辑</el-button>
            <el-button @click="goBack">返回</el-button>
          </div> -->
        </div>
      </template>
    </el-page-header>
    
    <!-- 基本信息卡片 -->
    <el-card class="info-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">基本信息</span>
          <div class="card-actions">
            <span class="member-no">{{ memberDetail?.memberNo }}</span>
          </div>
        </div>
      </template>
      
      <el-descriptions :column="3" border>
        <el-descriptions-item label="会员编号">{{ memberDetail?.memberNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ memberDetail?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ memberDetail?.realName || '-' }}</el-descriptions-item>
        
        <el-descriptions-item label="性别">{{ getGenderText(memberDetail?.gender) }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ memberDetail?.age || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ formatDate(memberDetail?.birthday) }}</el-descriptions-item>
        
        <el-descriptions-item label="身份证号">{{ memberDetail?.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身高">{{ memberDetail?.height ? memberDetail.height + 'cm' : '-' }}</el-descriptions-item>
        <!-- <el-descriptions-item label="体重">{{ memberDetail?.weight ? memberDetail.weight + 'kg' : '-' }}</el-descriptions-item> -->
        
        <!-- <el-descriptions-item label="专属教练">{{ memberDetail?.personalCoachName || '未分配' }}</el-descriptions-item> -->
        
        <el-descriptions-item label="注册时间">{{ formatDateTime(memberDetail?.createTime) }}</el-descriptions-item>

        <el-descriptions-item label="地址" span="2">{{ memberDetail?.address || '-' }}</el-descriptions-item>
      </el-descriptions>
      
      <!-- 统计信息 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="4">
            <div class="stat-item">
              <div class="stat-label">总签到次数</div>
              <div class="stat-value">{{ memberDetail?.totalCheckins || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-item">
              <div class="stat-label">总课时数</div>
              <div class="stat-value">{{ memberDetail?.totalCourseHours || 0 }}</div>
              <div class="stat-unit">小时</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-item">
              <div class="stat-label">累计消费</div>
              <div class="stat-value amount">¥{{ formatAmount(memberDetail?.totalSpent) }}</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-item">
              <div class="stat-label">会员卡总数</div>
              <div class="stat-value">{{ memberDetail?.memberCards?.length || 0 }}</div>
              <div class="stat-unit">张</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-item">
              <div class="stat-label">健康档案数</div>
              <div class="stat-value">{{ memberDetail?.healthRecords?.length || 0 }}</div>
              <div class="stat-unit">份</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-item">
              <div class="stat-label">课程记录数</div>
              <div class="stat-value">{{ memberDetail?.courseRecords?.length || 0 }}</div>
              <div class="stat-unit">次</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <!-- 会员卡信息 -->
      <el-tab-pane label="会员卡" name="cards">
        <el-card shadow="never" class="tab-content">
          <template #header>
            <div class="tab-header">
              <span class="tab-title">会员卡信息</span>
              <el-button type="primary" size="small" @click="handleAddCard">
                <el-icon><Plus /></el-icon>
                添加会员卡
              </el-button>
            </div>
          </template>
          
          <div v-if="memberDetail?.memberCards && memberDetail.memberCards.length > 0">
            <div class="cards-grid">
              <div v-for="card in memberDetail.memberCards" :key="card.productId" class="card-item">
                <el-card shadow="hover" class="card-content">
                  <template #header>
                    <div class="card-item-header">
                      <div class="card-header-left">
                        <!-- <span class="card-name">{{ card.cardName || card.productName }}</span> -->
                        <span class="card-name">{{ card.productName }}</span>
                        <el-tag :type="getCardStatusType(card.status)" size="small">
                          {{ card.statusDesc || '未知' }}
                        </el-tag>
                      </div>
                      <div class="card-header-right">
                        <span class="card-type">{{ card.cardTypeDesc || '未知类型' }}</span>
                      </div>
                    </div>
                  </template>
                  
                  <div class="card-details">
                    <!-- 会籍卡显示时间 -->
                    <template v-if="isMembershipCard(card.cardType)">
                      <div class="card-field">
                        <span class="label">有效期：</span>
                        <span class="value">
                          {{ formatDate(card.startDate) }} 至 {{ formatDate(card.endDate) }}
                        </span>
                      </div>
                      <div class="card-field">
                        <span class="label">剩余天数：</span>
                        <span class="value">
                          <el-tag :type="getRemainingDaysType(card.endDate)" size="small">
                            {{ calculateRemainingDays(card.endDate) }} 天
                          </el-tag>
                        </span>
                      </div>
                    </template>
                    
                    <!-- 课程卡显示课时数 -->
                    <template v-else-if="isCourseCard(card.cardType)">
                      <div class="card-field">
                        <span class="label">总课时：</span>
                        <span class="value">
                          <el-tag type="info" size="small">{{ card.totalSessions || 0 }}</el-tag>
                        </span>
                      </div>
                      <div class="card-field">
                        <span class="label">已用课时：</span>
                        <span class="value">
                          <el-tag type="warning" size="small">{{ card.usedSessions || 0 }}</el-tag>
                        </span>
                      </div>
                      <div class="card-field">
                        <span class="label">剩余课时：</span>
                        <span class="value">
                          <el-tag :type="card.remainingSessions > 0 ? 'success' : 'danger'" size="small">
                            {{ card.remainingSessions || 0 }}
                          </el-tag>
                        </span>
                      </div>
                    </template>
                    
                    <!-- 其他卡类型 -->
                    <template v-else>
                      <div class="card-field">
                        <span class="label">开始日期：</span>
                        <span class="value">{{ formatDate(card.startDate) }}</span>
                      </div>
                      <div class="card-field">
                        <span class="label">结束日期：</span>
                        <span class="value">{{ formatDate(card.endDate) }}</span>
                      </div>
                    </template>
                    
                    <div class="card-field">
                      <span class="label">金额：</span>
                      <span class="value amount">¥{{ formatAmount(card.amount) }}</span>
                    </div>
                    
                    <div class="card-field" v-if="card.productId">
                      <span class="label">商品ID：</span>
                      <span class="value">{{ card.productId }}</span>
                    </div>
                    
                    <div class="card-actions">
                      <el-button type="text" size="small" @click="handleRenewCard(card)">
                        续费
                      </el-button>
                      <el-button type="text" size="small" @click="handleViewCardDetail(card)">
                        详情
                      </el-button>
                    </div>
                  </div>
                </el-card>
              </div>
            </div>
          </div>
          
          <div v-else class="empty-data">
            <el-empty description="暂无会员卡信息" />
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 健康档案 -->
      <el-tab-pane label="健康档案" name="health">
        <el-card shadow="never" class="tab-content">
          <template #header>
            <div class="tab-header">
              <span class="tab-title">健康档案记录</span>
              <el-button type="primary" size="small" @click="handleAddHealthRecord">
                <el-icon><Plus /></el-icon>
                添加记录
              </el-button>
            </div>
          </template>
          
          <div v-if="memberDetail?.healthRecords && memberDetail.healthRecords.length > 0">
            <el-table :data="memberDetail.healthRecords" style="width: 100%">
              <el-table-column prop="recordDate" label="记录日期" width="120" sortable>
                <template #default="{ row }">
                  {{ formatDate(row.recordDate) }}
                </template>
              </el-table-column>
              <el-table-column prop="weight" label="体重(kg)" width="100" align="center">
                <template #default="{ row }">
                  <span class="value-highlight">{{ row.weight }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="bodyFatPercentage" label="体脂率(%)" width="100" align="center">
                <template #default="{ row }">
                  {{ row.bodyFatPercentage || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="muscleMass" label="肌肉量(kg)" width="100" align="center">
                <template #default="{ row }">
                  {{ row.muscleMass || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="bmi" label="BMI指数" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getBmiType(row.bmi)" size="small">
                    {{ row.bmi || '-' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="chestCircumference" label="胸围(cm)" width="100" align="center">
                <template #default="{ row }">
                  {{ row.chestCircumference || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="waistCircumference" label="腰围(cm)" width="100" align="center">
                <template #default="{ row }">
                  {{ row.waistCircumference || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="hipCircumference" label="臀围(cm)" width="100" align="center">
                <template #default="{ row }">
                  {{ row.hipCircumference || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="bloodPressure" label="血压" width="100" align="center">
                <template #default="{ row }">
                  {{ row.bloodPressure || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="heartRate" label="心率" width="100" align="center">
                <template #default="{ row }">
                  <span class="value-highlight">{{ row.heartRate || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="notes" label="备注" min-width="150">
                <template #default="{ row }">
                  <span class="notes-text">{{ row.notes || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row, $index }">
                  <el-button type="text" size="small" @click="handleEditHealthRecord(row, $index)">
                    编辑
                  </el-button>
                  <el-button type="text" size="small" @click="handleDeleteHealthRecord($index)" style="color: #f56c6c;">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div v-else class="empty-data">
            <el-empty description="暂无健康档案记录" />
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 课程记录 -->
      <el-tab-pane label="课程记录" name="courses">
        <el-card shadow="never" class="tab-content">
          <template #header>
            <div class="tab-header">
              <span class="tab-title">课程记录</span>
            </div>
          </template>
          
          <div v-if="memberDetail?.courseRecords && memberDetail.courseRecords.length > 0">
            <el-table :data="memberDetail.courseRecords" style="width: 100%">
              <el-table-column prop="courseId" label="课程ID" width="100" />
              <el-table-column prop="courseName" label="课程名称" min-width="150">
                <template #default="{ row }">
                  <span class="course-name">{{ row.courseName || '未命名课程' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="coachName" label="教练" width="120">
                <template #default="{ row }">
                  {{ row.coachName || '未分配教练' }}
                </template>
              </el-table-column>
              <el-table-column prop="courseDate" label="上课日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.courseDate) }}
                </template>
              </el-table-column>
              <el-table-column prop="startTime" label="开始时间" width="100" />
              <el-table-column prop="endTime" label="结束时间" width="100" />
              <el-table-column prop="location" label="上课地点" width="150">
                <template #default="{ row }">
                  {{ row.location || '默认场地' }}
                </template>
              </el-table-column>
              <el-table-column prop="bookingStatus" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getBookingStatusType(row.bookingStatus)" size="small">
                    {{ getBookingStatusText(row.bookingStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="checkinTime" label="签到时间" width="160">
                <template #default="{ row }">
                  {{ formatDateTime(row.checkinTime) || '-' }}
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div v-else class="empty-data">
            <el-empty description="暂无课程记录" />
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 签到记录 -->
      <el-tab-pane label="签到记录" name="checkins">
        <el-card shadow="never" class="tab-content">
          <template #header>
            <div class="tab-header">
              <span class="tab-title">签到记录</span>
            </div>
          </template>
          
          <div v-if="memberDetail?.checkinRecords && memberDetail.checkinRecords.length > 0">
            <el-table :data="memberDetail.checkinRecords" style="width: 100%">
              <el-table-column prop="checkinTime" label="签到时间" width="160" sortable>
                <template #default="{ row }">
                  {{ formatDateTime(row.checkinTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="courseName" label="关联课程" min-width="150">
                <template #default="{ row }">
                  {{ row.courseName || '无关联课程' }}
                </template>
              </el-table-column>
              <el-table-column prop="coachName" label="教练" width="120">
                <template #default="{ row }">
                  {{ row.coachName || '系统记录' }}
                </template>
              </el-table-column>
              <el-table-column prop="checkinMethod" label="签到方式" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.checkinMethod === 0 ? 'primary' : 'success'" size="small">
                    {{ row.checkinMethod === 0 ? '教练签到' : '前台签到' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="notes" label="备注" min-width="150">
                <template #default="{ row }">
                  <span class="notes-text">{{ row.notes || '-' }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div v-else class="empty-data">
            <el-empty description="暂无签到记录" />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useMemberStore } from '@/stores/member'
import type { MemberFullDTO, MemberCardDTO, HealthRecordDTO } from '@/types/member'

const router = useRouter()
const route = useRoute()
const memberStore = useMemberStore()

const loading = ref(false)
const activeTab = ref('cards')

const memberId = computed(() => Number(route.params.id))
const memberDetail = computed(() => memberStore.currentMember)

// 格式化函数
const formatDate = (date: string | null | undefined) => {
  if (!date) return '-'
  return date
}

const formatDateTime = (datetime: string | null | undefined) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ')
}

const formatAmount = (amount: number | null | undefined) => {
  if (!amount) return '0.00'
  return amount.toFixed(2)
}

const getGenderText = (gender: number | null | undefined) => {
  if (gender === 1) return '男'
  if (gender === 0) return '女'
  return '未知'
}

// 判断是否为会籍卡
const isMembershipCard = (cardType: number | undefined) => {
  return cardType !== undefined && (cardType === 2 || cardType === 3 || cardType === 4)
}

// 判断是否为课程卡
const isCourseCard = (cardType: number | undefined) => {
  return cardType !== undefined && (cardType === 0 || cardType === 1)
}

// 获取卡状态标签类型
const getCardStatusType = (status: string | undefined) => {
  switch (status) {
    case 'ACTIVE': return 'success'
    case 'EXPIRED': return 'danger'
    case 'USED_UP': return 'warning'
    case 'INACTIVE': return 'info'
    default: return 'info'
  }
}

// 计算剩余天数
const calculateRemainingDays = (endDate: string | null | undefined) => {
  if (!endDate) return 0
  
  const end = new Date(endDate)
  const today = new Date()
  const diffTime = end.getTime() - today.getTime()
  const days = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return days > 0 ? days : 0
}

// 获取剩余天数标签类型
const getRemainingDaysType = (endDate: string | null | undefined) => {
  const days = calculateRemainingDays(endDate)
  if (days <= 0) return 'danger'
  if (days <= 7) return 'warning'
  if (days <= 30) return 'info'
  return 'success'
}

// BMI类型
const getBmiType = (bmi: number | null | undefined) => {
  if (!bmi) return 'info'
  if (bmi < 18.5) return 'warning'
  if (bmi < 24) return 'success'
  if (bmi < 28) return 'warning'
  return 'danger'
}

// 预约状态
const getBookingStatusType = (status: number | undefined) => {
  switch (status) {
    case 0: return 'info'      // 待上课
    case 1: return 'success'   // 已签到
    case 2: return 'success'   // 已完成
    case 3: return 'danger'    // 已取消
    default: return 'info'
  }
}

const getBookingStatusText = (status: number | undefined) => {
  switch (status) {
    case 0: return '待上课'
    case 1: return '已签到'
    case 2: return '已完成'
    case 3: return '已取消'
    default: return '未知'
  }
}

// 加载会员详情
const loadMemberDetail = async () => {
  try {
    loading.value = true
    await memberStore.fetchMemberDetail(memberId.value)
  } catch (error) {
    console.error('加载会员详情失败:', error)
    ElMessage.error('加载会员详情失败')
  } finally {
    loading.value = false
  }
}

// 添加健康记录
const handleAddHealthRecord = () => {
  router.push(`/member/${memberId.value}/health-record/add`)
}

// 编辑健康记录
const handleEditHealthRecord = (record: HealthRecordDTO, index: number) => {
  ElMessage.info('编辑健康记录功能开发中...')
}

// 删除健康记录
const handleDeleteHealthRecord = async (index: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条健康记录吗？',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('删除成功')
    // 这里需要调用API删除健康记录
    // await memberStore.deleteHealthRecord(memberId.value, index)
  } catch (error) {
    // 用户取消
  }
}

// 添加会员卡
const handleAddCard = () => {
  router.push(`/member/${memberId.value}/card/add`)
}

// 续费会员卡
const handleRenewCard = (card: MemberCardDTO) => {
  ElMessageBox.confirm(
    `确定要为【${card.cardName}】续费吗？`,
    '续费确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    router.push(`/member/${memberId.value}/card/renew?cardId=${card.productId}`)
  }).catch(() => {
    // 用户取消
  })
}

// 查看会员卡详情
const handleViewCardDetail = (card: MemberCardDTO) => {
  ElMessage.info(`查看会员卡详情：${card.cardName}`)
  // 这里可以跳转到会员卡详情页或打开详情弹窗
}

// 导航
const goBack = () => {
  router.push('/member/list')
}

const goEdit = () => {
  router.push(`/member/edit/${memberId.value}`)
}

onMounted(() => {
  loadMemberDetail()
})
</script>

<style scoped>
.member-detail-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.member-no {
  font-size: 16px;
  font-weight: 600;
  color: #409EFF;
  padding: 4px 12px;
  background-color: #ecf5ff;
  border-radius: 4px;
}

.stats-section {
  margin-top: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.stat-item {
  text-align: center;
  padding: 10px;
  border-radius: 4px;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-value.amount {
  color: #67C23A;
}

.stat-unit {
  font-size: 12px;
  color: #909399;
}

.detail-tabs {
  margin-top: 20px;
}

.tab-content {
  margin-top: 0;
  border: none;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.empty-data {
  padding: 40px 0;
  text-align: center;
}

/* 会员卡网格布局 */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 10px;
}

.card-item {
  transition: transform 0.3s;
}

.card-item:hover {
  transform: translateY(-2px);
}

.card-content {
  height: 100%;
}

.card-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-header-right .card-type {
  font-size: 12px;
  color: #409EFF;
  background-color: #ecf5ff;
  padding: 2px 8px;
  border-radius: 12px;
}

.card-details {
  padding: 10px 0;
}

.card-field {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  line-height: 1.5;
}

.card-field .label {
  color: #606266;
  flex-shrink: 0;
}

.card-field .value {
  color: #303133;
  font-weight: 500;
  text-align: right;
  flex-grow: 1;
  margin-left: 10px;
}

.card-field .value.amount {
  color: #67C23A;
  font-weight: 600;
}

.card-actions {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}

/* 表格样式 */
.value-highlight {
  font-weight: 600;
  color: #409EFF;
}

.notes-text {
  color: #606266;
  font-size: 13px;
  line-height: 1.4;
}

.course-name {
  font-weight: 500;
  color: #303133;
}

:deep(.el-card__header) {
  padding: 16px 20px;
}

:deep(.el-tabs__header) {
  background-color: white;
  padding: 0 20px;
  margin: 0;
}

:deep(.el-descriptions__body) {
  background-color: white;
}

:deep(.el-descriptions__cell) {
  padding: 12px 16px;
}

:deep(.el-table__header) {
  background-color: #f8f9fa;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>