<template>
  <div class="user-detail-container">
    <!-- 页面头部 -->
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <div class="header-content">
          <span class="page-title">用户详情</span>
        </div>
      </template>
    </el-page-header>

    <!-- 基本信息卡片 -->
    <el-card class="info-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">基本信息</span>
          <div class="card-actions">
            <el-tag :type="currentUser?.status === 1 ? 'success' : 'info'" size="large">
              {{ currentUser?.statusDesc }}
            </el-tag>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ currentUser?.id || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser?.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ currentUser?.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="currentUser?.role === 0 ? 'danger' : 'primary'" size="small">
            {{ currentUser?.roleDesc || '-' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentUser?.status === 1 ? 'success' : 'info'" size="small">
            {{ currentUser?.statusDesc || '-' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentUser?.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(currentUser?.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          无
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 登录记录卡片 -->
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">最近登录记录</span>
        </div>
      </template>

      <div v-if="loginRecords && loginRecords.length > 0">
        <el-table :data="loginRecords" style="width: 100%" border stripe>
          <el-table-column prop="loginTime" label="登录时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.loginTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="ipAddress" label="IP地址" width="150" />
          <el-table-column prop="loginLocation" label="登录地点" width="200" />
          <el-table-column prop="device" label="登录设备" min-width="200" />
          <el-table-column prop="loginStatus" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.loginStatus === 'SUCCESS' ? 'success' : 'danger'" size="small">
                {{ row.loginStatus === 'SUCCESS' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination v-model:current-page="loginPageInfo.pageNum" v-model:page-size="loginPageInfo.pageSize" :total="loginTotal" layout="total, prev, pager, next" background small />
        </div>
      </div>

      <div v-else class="empty-data">
        <el-empty description="暂无登录记录" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useWebUserStore } from '@/stores/settings/webUser'

const router = useRouter()
const route = useRoute()
const userStore = useWebUserStore()

const loading = ref(false)

const userId = computed(() => Number(route.params.id))
const currentUser = computed(() => userStore.currentUser)

// 登录记录（模拟数据）
const loginRecords = ref<any[]>([
  {
    loginTime: '2024-01-15 09:30:45',
    ipAddress: '192.168.1.100',
    loginLocation: '北京市 北京市',
    device: 'Chrome 120.0.0.0 / Windows 10',
    loginStatus: 'SUCCESS',
  },
  {
    loginTime: '2024-01-14 18:22:30',
    ipAddress: '192.168.1.101',
    loginLocation: '上海市 上海市',
    device: 'Safari 17.2 / macOS 14.2',
    loginStatus: 'SUCCESS',
  },
  {
    loginTime: '2024-01-13 09:15:10',
    ipAddress: '192.168.1.102',
    loginLocation: '广东省 深圳市',
    device: 'WeChat 8.0.0 / iOS 17.2',
    loginStatus: 'SUCCESS',
  },
  {
    loginTime: '2024-01-12 20:05:00',
    ipAddress: '192.168.1.103',
    loginLocation: '浙江省 杭州市',
    device: 'Chrome 119.0.0.0 / Android 14',
    loginStatus: 'FAILED',
  },
])

const loginPageInfo = ref({
  pageNum: 1,
  pageSize: 5,
  totalPages: 0,
})

const loginTotal = ref(8)

// 格式化函数
const formatDateTime = (datetime: string | null | undefined) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ')
}

// 加载用户详情
const loadUserDetail = async () => {
  try {
    loading.value = true
    await userStore.fetchUserDetail(userId.value)
  } catch (error) {
    console.error('加载用户详情失败:', error)
    ElMessage.error('加载用户详情失败')
  } finally {
    loading.value = false
  }
}

// 编辑用户
const goEdit = () => {
  router.push(`/settings/webUser/edit/${userId.value}`)
}

// 启用用户
const handleEnable = async () => {
  try {
    await ElMessageBox.confirm('确定要启用该用户吗？', '提示', {
      type: 'warning',
    })

    await userStore.updateUserStatus(userId.value, 1)
    ElMessage.success('启用成功')
    await loadUserDetail()
  } catch (error) {
    // 用户取消
  }
}

// 禁用用户
const handleDisable = async () => {
  try {
    await ElMessageBox.confirm('确定要禁用该用户吗？', '提示', {
      type: 'warning',
    })

    await userStore.updateUserStatus(userId.value, 0)
    ElMessage.success('禁用成功')
    await loadUserDetail()
  } catch (error) {
    // 用户取消
  }
}

// 重置密码
const handleResetPassword = async () => {
  try {
    await ElMessageBox.confirm('确定要重置该用户的密码为123456吗？', '提示', {
      type: 'warning',
    })

    await userStore.resetPassword(userId.value)
    ElMessage.success('密码已重置为123456')
  } catch (error) {
    // 用户取消
  }
}

// 删除用户
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '警告', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })

    await userStore.deleteUser(userId.value)
    ElMessage.success('用户已删除')
    goBack()
  } catch (error) {
    // 用户取消
  }
}

// 返回
const goBack = () => {
  router.push('/settings/webUser')
}

onMounted(() => {
  loadUserDetail()
})
</script>

<style scoped lang="scss">
.user-detail-container {
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

.header-actions {
  display: flex;
  gap: 8px;
}

.info-card,
.login-card {
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

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.empty-data {
  padding: 40px 0;
  text-align: center;
}

:deep(.el-card__header) {
  padding: 16px 20px;
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