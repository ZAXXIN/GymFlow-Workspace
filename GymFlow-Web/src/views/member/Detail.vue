<template>
  <div class="member-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button icon="i-ep-arrow-left" @click="goBack">
          返回
        </el-button>
        <h1 class="page-title">会员详情</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleEdit">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-dropdown @command="handleMoreAction">
          <el-button>
            <el-icon><MoreFilled /></el-icon>
            更多操作
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="renew">
                <el-icon><RefreshRight /></el-icon>
                续费
              </el-dropdown-item>
              <el-dropdown-item command="health">
                <el-icon><Document /></el-icon>
                健康档案
              </el-dropdown-item>
              <el-dropdown-item command="training">
                <el-icon><List /></el-icon>
                训练计划
              </el-dropdown-item>
              <el-dropdown-divider />
              <el-dropdown-item command="checkin">
                <el-icon><Check /></el-icon>
                快速签到
              </el-dropdown-item>
              <el-dropdown-item command="booking">
                <el-icon><Calendar /></el-icon>
                课程预约
              </el-dropdown-item>
              <el-dropdown-divider />
              <el-dropdown-item command="delete" class="delete-item">
                <el-icon><Delete /></el-icon>
                删除
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 会员信息卡片 -->
    <div class="member-info-cards">
      <el-row :gutter="20">
        <!-- 基本信息 -->
        <el-col :xs="24" :md="8">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <h3>基本信息</h3>
              </div>
            </template>
            <div class="info-content">
              <div class="avatar-section">
                <el-avatar :size="80" :src="memberData?.user?.avatar" class="member-avatar">
                  {{ memberData?.realName?.charAt(0) || 'M' }}
                </el-avatar>
                <div class="avatar-info">
                  <h4 class="member-name">{{ memberData?.realName }}</h4>
                  <p class="member-no">会员编号: {{ memberData?.memberNo }}</p>
                  <el-tag :type="getStatusType(memberData?.status)" size="small">
                    {{ getStatusText(memberData?.status) }}
                  </el-tag>
                </div>
              </div>
              
              <el-divider />
              
              <div class="info-list">
                <div class="info-item">
                  <span class="label">性别:</span>
                  <span class="value">{{ memberData?.gender === 1 ? '男' : '女' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">出生日期:</span>
                  <span class="value">{{ formatDate(memberData?.birthDate) || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">身高/体重:</span>
                  <span class="value">
                    {{ memberData?.height || '-' }}cm / {{ memberData?.weight || '-' }}kg
                  </span>
                </div>
                <div class="info-item">
                  <span class="label">BMI:</span>
                  <span class="value">
                    {{ memberData?.bmi ? memberData.bmi.toFixed(1) : '-' }}
                    <el-tag v-if="memberData?.bmi" :type="getBMIType(memberData.bmi)" size="small">
                      {{ getBMIRating(memberData.bmi) }}
                    </el-tag>
                  </span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 联系信息 -->
        <el-col :xs="24" :md="8">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <h3>联系信息</h3>
              </div>
            </template>
            <div class="info-content">
              <div class="info-list">
                <div class="info-item">
                  <el-icon class="icon"><User /></el-icon>
                  <div class="item-content">
                    <span class="label">用户名</span>
                    <span class="value">{{ memberData?.user?.username }}</span>
                  </div>
                </div>
                <div class="info-item">
                  <el-icon class="icon"><Message /></el-icon>
                  <div class="item-content">
                    <span class="label">邮箱</span>
                    <span class="value">{{ memberData?.user?.email || '-' }}</span>
                  </div>
                </div>
                <div class="info-item">
                  <el-icon class="icon"><Phone /></el-icon>
                  <div class="item-content">
                    <span class="label">手机号</span>
                    <span class="value">{{ memberData?.user?.phone }}</span>
                  </div>
                </div>
                <div class="info-item">
                  <el-icon class="icon"><Location /></el-icon>
                  <div class="item-content">
                    <span class="label">注册时间</span>
                    <span class="value">{{ formatDateTime(memberData?.createdAt) }}</span>
                  </div>
                </div>
              </div>
              
              <el-divider />
              
              <h4 class="section-title">紧急联系人</h4>
              <div class="info-list">
                <div class="info-item">
                  <span class="label">联系人:</span>
                  <span class="value">{{ memberData?.emergencyContact || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">联系电话:</span>
                  <span class="value">{{ memberData?.emergencyPhone || '-' }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 会籍信息 -->
        <el-col :xs="24" :md="8">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <h3>会籍信息</h3>
              </div>
            </template>
            <div class="info-content">
              <div class="stats-grid">
                <div class="stat-item">
                  <div class="stat-value">{{ memberData?.remainingSessions || 0 }}</div>
                  <div class="stat-label">剩余课时</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ memberData?.totalSessions || 0 }}</div>
                  <div class="stat-label">总课时</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ getAttendanceRate() }}%</div>
                  <div class="stat-label">出勤率</div>
                </div>
              </div>
              
              <el-divider />
              
              <div class="info-list">
                <div class="info-item">
                  <span class="label">会籍开始:</span>
                  <span class="value">{{ formatDate(memberData?.membershipStartDate) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">会籍结束:</span>
                  <span class="value">{{ formatDate(memberData?.membershipEndDate) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">剩余天数:</span>
                  <span class="value">
                    <el-tag :type="getDaysLeftType()" size="small">
                      {{ getDaysLeft() }}
                    </el-tag>
                  </span>
                </div>
              </div>
              
              <el-divider />
              
              <h4 class="section-title">健身目标</h4>
              <p class="goal-text">{{ memberData?.fitnessGoal || '暂无' }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- Tab页 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <!-- 健康记录 -->
      <el-tab-pane label="健康记录" name="health">
        <health-record :member-id="memberId" />
      </el-tab-pane>
      
      <!-- 课程记录 -->
      <el-tab-pane label="课程记录" name="courses">
        <course-record :member-id="memberId" />
      </el-tab-pane>
      
      <!-- 签到记录 -->
      <el-tab-pane label="签到记录" name="checkins">
        <checkin-record :member-id="memberId" />
      </el-tab-pane>
      
      <!-- 订单记录 -->
      <el-tab-pane label="订单记录" name="orders">
        <order-record :member-id="memberId" />
      </el-tab-pane>
      
      <!-- 训练计划 -->
      <el-tab-pane label="训练计划" name="training">
        <training-plan :member-id="memberId" />
      </el-tab-pane>
    </el-tabs>
    
    <!-- 编辑对话框 -->
    <member-form-dialog
      v-model="editDialog.visible"
      mode="edit"
      :member-id="memberId"
      @success="handleEditSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit,
  MoreFilled,
  RefreshRight,
  Document,
  List,
  Check,
  Calendar,
  Delete,
  User,
  Message,
  Phone,
  Location
} from '@element-plus/icons-vue'
import { useMemberStore } from '@/stores/member'
import { formatDate, formatDateTime, calculateBMI, getBMIRating } from '@/utils'
import type { Member, MemberStatus } from '@/types'
import MemberFormDialog from '@/components/business/MemberFormDialog.vue'
import HealthRecord from './HealthRecord.vue'
import CourseRecord from './CourseRecord.vue'
import CheckinRecord from './CheckinRecord.vue'
import OrderRecord from './OrderRecord.vue'
import TrainingPlan from './TrainingPlan.vue'

// Store
const memberStore = useMemberStore()

// Router
const route = useRoute()
const router = useRouter()

// 状态
const loading = ref(false)
const activeTab = ref('health')

const editDialog = reactive({
  visible: false
})

// Computed
const memberId = computed(() => parseInt(route.params.id as string))
const memberData = computed(() => memberStore.currentMember)

// Methods
const goBack = () => {
  router.push('/members')
}

const loadMemberData = async () => {
  try {
    loading.value = true
    await memberStore.fetchMemberById(memberId.value)
  } catch (error) {
    console.error('加载会员数据失败:', error)
    ElMessage.error('加载会员数据失败')
    goBack()
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  editDialog.visible = true
}

const handleMoreAction = async (command: string) => {
  switch (command) {
    case 'renew':
      handleRenew()
      break
    case 'health':
      router.push(`/members/${memberId.value}/health`)
      break
    case 'training':
      activeTab.value = 'training'
      break
    case 'checkin':
      handleQuickCheckin()
      break
    case 'booking':
      handleQuickBooking()
      break
    case 'delete':
      handleDelete()
      break
  }
}

const handleRenew = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入续费月数', '会员续费', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^[1-9]\d*$/,
      inputErrorMessage: '请输入有效的月数'
    })
    
    const months = parseInt(value)
    // 调用续费API
    ElMessage.success('续费成功')
    loadMemberData()
  } catch (error) {
    console.error('续费失败:', error)
  }
}

const handleQuickCheckin = async () => {
  try {
    await ElMessageBox.confirm('确认为该会员进行签到吗？', '快速签到', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    // 调用快速签到API
    ElMessage.success('签到成功')
  } catch (error) {
    console.error('签到失败:', error)
  }
}

const handleQuickBooking = () => {
  // 跳转到课程预约页面
  router.push('/courses/schedule')
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确认删除该会员吗？此操作不可恢复。', '删除会员', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    
    await memberStore.deleteMember(memberId.value)
    ElMessage.success('删除成功')
    goBack()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

const handleEditSuccess = () => {
  loadMemberData()
}

const getStatusType = (status?: MemberStatus) => {
  if (!status) return 'info'
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'INACTIVE':
      return 'info'
    case 'SUSPENDED':
      return 'warning'
    case 'EXPIRED':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status?: MemberStatus) => {
  if (!status) return '未知'
  switch (status) {
    case 'ACTIVE':
      return '活跃'
    case 'INACTIVE':
      return '不活跃'
    case 'SUSPENDED':
      return '暂停'
    case 'EXPIRED':
      return '已过期'
    default:
      return '未知'
  }
}

const getBMIType = (bmi: number) => {
  if (bmi < 18.5) return 'warning'
  if (bmi < 24) return 'success'
  if (bmi < 28) return 'warning'
  return 'danger'
}

const getAttendanceRate = () => {
  if (!memberData.value?.totalSessions) return 0
  const attended = memberData.value.totalSessions - memberData.value.remainingSessions
  return Math.round((attended / memberData.value.totalSessions) * 100)
}

const getDaysLeft = () => {
  if (!memberData.value?.membershipEndDate) return '0天'
  const endDate = new Date(memberData.value.membershipEndDate)
  const today = new Date()
  const diffTime = endDate.getTime() - today.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays > 0 ? `${diffDays}天` : '已过期'
}

const getDaysLeftType = () => {
  if (!memberData.value?.membershipEndDate) return 'info'
  const endDate = new Date(memberData.value.membershipEndDate)
  const today = new Date()
  const diffTime = endDate.getTime() - today.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffDays > 30) return 'success'
  if (diffDays > 7) return 'warning'
  return 'danger'
}

// 生命周期
onMounted(() => {
  loadMemberData()
})
</script>

<style lang="scss" scoped>
.member-detail-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .page-title {
        font-size: 24px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
        margin: 0;
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }
  
  .member-info-cards {
    margin-bottom: 24px;
    
    .info-card {
      border-radius: 12px;
      height: 100%;
      
      :deep(.el-card__header) {
        padding: 16px 20px;
        border-bottom: 1px solid var(--gymflow-border);
        
        h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
          color: var(--gymflow-text-primary);
        }
      }
      
      .info-content {
        padding: 16px 0;
        
        .avatar-section {
          display: flex;
          align-items: center;
          gap: 16px;
          margin-bottom: 16px;
          
          .member-avatar {
            background: var(--gymflow-primary);
            color: white;
            font-size: 24px;
            font-weight: bold;
          }
          
          .avatar-info {
            .member-name {
              font-size: 20px;
              font-weight: 600;
              color: var(--gymflow-text-primary);
              margin: 0 0 4px;
            }
            
            .member-no {
              font-size: 14px;
              color: var(--gymflow-text-secondary);
              margin: 0 0 8px;
            }
          }
        }
        
        .info-list {
          .info-item {
            display: flex;
            align-items: center;
            margin-bottom: 12px;
            
            &:last-child {
              margin-bottom: 0;
            }
            
            &.with-icon {
              gap: 12px;
            }
            
            .icon {
              font-size: 18px;
              color: var(--gymflow-primary);
            }
            
            .item-content {
              display: flex;
              flex-direction: column;
            }
            
            .label {
              font-size: 12px;
              color: var(--gymflow-text-secondary);
              margin-bottom: 2px;
            }
            
            .value {
              font-size: 14px;
              font-weight: 500;
              color: var(--gymflow-text-primary);
              
              .el-tag {
                margin-left: 8px;
              }
            }
          }
        }
        
        .stats-grid {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 16px;
          margin-bottom: 16px;
          
          .stat-item {
            text-align: center;
            
            .stat-value {
              font-size: 24px;
              font-weight: 700;
              color: var(--gymflow-primary);
              margin-bottom: 4px;
            }
            
            .stat-label {
              font-size: 12px;
              color: var(--gymflow-text-secondary);
            }
          }
        }
        
        .section-title {
          font-size: 14px;
          font-weight: 600;
          color: var(--gymflow-text-primary);
          margin: 16px 0 12px;
        }
        
        .goal-text {
          font-size: 14px;
          color: var(--gymflow-text-secondary);
          line-height: 1.6;
          margin: 0;
        }
      }
    }
  }
  
  .detail-tabs {
    :deep(.el-tabs__header) {
      margin: 0;
      background: var(--gymflow-card-bg);
      border-radius: 8px 8px 0 0;
      padding: 0 20px;
    }
    
    :deep(.el-tabs__content) {
      background: var(--gymflow-card-bg);
      border-radius: 0 0 8px 8px;
      padding: 20px;
    }
  }
}

:deep(.delete-item) {
  color: var(--el-color-danger);
  
  &:hover {
    background: rgba(245, 108, 108, 0.1);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .member-detail-container {
    padding: 16px;
    
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    
    .member-info-cards {
      .info-card {
        margin-bottom: 16px;
        
        .avatar-section {
          flex-direction: column;
          text-align: center;
        }
      }
    }
  }
}
</style>