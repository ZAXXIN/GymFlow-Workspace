<template>
  <div class="user-profile">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h3 class="card-title">个人资料</h3>
          <div class="card-subtitle">管理您的个人信息和账户设置</div>
        </div>
      </template>

      <div class="profile-content">
        <!-- 基本信息 -->
        <div class="profile-section">
          <h4 class="section-title">基本信息</h4>
          <div class="section-content">
            <el-form ref="basicInfoForm" :model="basicInfoForm" :rules="basicInfoRules" label-width="120px">
              <div class="form-row">
                <div class="form-column">
                  <el-form-item label="头像">
                    <div class="avatar-upload">
                      <el-avatar :size="120" :src="avatarUrl" class="avatar-preview">
                        {{ basicInfoForm.name?.charAt(0) || 'U' }}
                      </el-avatar>
                      <div class="avatar-actions">
                        <el-upload
                          class="avatar-uploader"
                          :action="uploadUrl"
                          :show-file-list="false"
                          :on-success="handleAvatarSuccess"
                          :before-upload="beforeAvatarUpload"
                        >
                          <el-button type="primary" size="small">
                            <el-icon><Upload /></el-icon>更换头像
                          </el-button>
                        </el-upload>
                        <el-button 
                          type="text" 
                          size="small" 
                          @click="removeAvatar"
                          :disabled="!avatarUrl"
                        >
                          移除头像
                        </el-button>
                      </div>
                    </div>
                  </el-form-item>

                  <el-form-item label="用户名" prop="username">
                    <el-input
                      v-model="basicInfoForm.username"
                      placeholder="请输入用户名"
                      style="width: 300px;"
                      :disabled="!editingBasicInfo"
                    />
                  </el-form-item>

                  <el-form-item label="姓名" prop="name">
                    <el-input
                      v-model="basicInfoForm.name"
                      placeholder="请输入真实姓名"
                      style="width: 300px;"
                      :disabled="!editingBasicInfo"
                    />
                  </el-form-item>

                  <el-form-item label="性别" prop="gender">
                    <el-radio-group v-model="basicInfoForm.gender" :disabled="!editingBasicInfo">
                      <el-radio label="MALE">男</el-radio>
                      <el-radio label="FEMALE">女</el-radio>
                      <el-radio label="UNKNOWN">保密</el-radio>
                    </el-radio-group>
                  </el-form-item>

                  <el-form-item label="出生日期" prop="birthDate">
                    <el-date-picker
                      v-model="basicInfoForm.birthDate"
                      type="date"
                      placeholder="选择出生日期"
                      value-format="YYYY-MM-DD"
                      style="width: 300px;"
                      :disabled="!editingBasicInfo"
                    />
                  </el-form-item>
                </div>

                <div class="form-column">
                  <el-form-item label="手机号码" prop="phone">
                    <el-input
                      v-model="basicInfoForm.phone"
                      placeholder="请输入手机号码"
                      style="width: 300px;"
                      :disabled="!editingBasicInfo"
                    />
                  </el-form-item>

                  <el-form-item label="邮箱" prop="email">
                    <el-input
                      v-model="basicInfoForm.email"
                      placeholder="请输入邮箱地址"
                      style="width: 300px;"
                      :disabled="!editingBasicInfo"
                    />
                  </el-form-item>

                  <el-form-item label="部门" prop="department">
                    <el-select
                      v-model="basicInfoForm.department"
                      placeholder="请选择部门"
                      style="width: 300px;"
                      :disabled="!editingBasicInfo"
                    >
                      <el-option label="管理部" value="MANAGEMENT" />
                      <el-option label{"value"="FRONT_DESK"} label="前台部" />
                      <el-option label="教练部" value="COACH" />
                      <el-option label="销售部" value="SALES" />
                      <el-option label{"value"="FINANCE"} label="财务部" />
                    </el-select>
                  </el-form-item>

                  <el-form-item label="职位" prop="position">
                    <el-select
                      v-model="basicInfoForm.position"
                      placeholder="请选择职位"
                      style="width: 300px;"
                      :disabled="!editingBasicInfo"
                    >
                      <el-option label="管理员" value="ADMIN" />
                      <el-option label="前台" value="RECEPTIONIST" />
                      <el-option label="教练" value="COACH" />
                      <el-option label="销售顾问" value="SALES_CONSULTANT" />
                      <el-option label="财务" value="ACCOUNTANT" />
                    </el-select>
                  </el-form-item>

                  <el-form-item label="入职日期" prop="hireDate">
                    <el-date-picker
                      v-model="basicInfoForm.hireDate"
                      type="date"
                      placeholder="选择入职日期"
                      value-format="YYYY-MM-DD"
                      style="width: 300px;"
                      :disabled="!editingBasicInfo"
                    />
                  </el-form-item>
                </div>
              </div>

              <el-form-item label="个人简介" prop="bio">
                <el-input
                  v-model="basicInfoForm.bio"
                  type="textarea"
                  placeholder="请输入个人简介"
                  :rows="4"
                  style="width: 620px;"
                  :disabled="!editingBasicInfo"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="联系地址" prop="address">
                <el-input
                  v-model="basicInfoForm.address"
                  type="textarea"
                  placeholder="请输入联系地址"
                  :rows="2"
                  style="width: 620px;"
                  :disabled="!editingBasicInfo"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item>
                <el-button
                  v-if="!editingBasicInfo"
                  type="primary"
                  @click="startEditingBasicInfo"
                >
                  编辑基本信息
                </el-button>
                <div v-else class="edit-actions">
                  <el-button @click="cancelEditingBasicInfo">取消</el-button>
                  <el-button type="primary" @click="saveBasicInfo" :loading="savingBasicInfo">
                    保存
                  </el-button>
                </div>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 账户信息 -->
        <div class="profile-section">
          <h4 class="section-title">账户信息</h4>
          <div class="section-content">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="账户ID">{{ accountInfo.id }}</el-descriptions-item>
              <el-descriptions-item label="用户角色">
                <el-tag :type="getRoleTagType(accountInfo.role)" size="small">
                  {{ getRoleText(accountInfo.role) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ accountInfo.registerTime }}</el-descriptions-item>
              <el-descriptions-item label="最后登录时间">{{ accountInfo.lastLoginTime }}</el-descriptions-item>
              <el-descriptions-item label="账户状态">
                <el-tag :type="accountInfo.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
                  {{ accountInfo.status === 'ACTIVE' ? '正常' : '禁用' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="邮箱验证">
                <el-tag :type="accountInfo.emailVerified ? 'success' : 'warning'" size="small">
                  {{ accountInfo.emailVerified ? '已验证' : '未验证' }}
                </el-tag>
                <el-button
                  v-if="!accountInfo.emailVerified"
                  type="text"
                  size="small"
                  @click="verifyEmail"
                >
                  验证邮箱
                </el-button>
              </el-descriptions-item>
              <el-descriptions-item label="手机验证">
                <el-tag :type="accountInfo.phoneVerified ? 'success' : 'warning'" size="small">
                  {{ accountInfo.phoneVerified ? '已验证' : '未验证' }}
                </el-tag>
                <el-button
                  v-if="!accountInfo.phoneVerified"
                  type="text"
                  size="small"
                  @click="verifyPhone"
                >
                  验证手机
                </el-button>
              </el-descriptions-item>
            </el-descriptions>

            <div class="account-actions">
              <el-button @click="viewLoginHistory">查看登录历史</el-button>
              <el-button @click="viewOperationLogs">查看操作日志</el-button>
              <el-button type="primary" @click="viewAccountSecurity">账户安全设置</el-button>
            </div>
          </div>
        </div>

        <!-- 通知设置 -->
        <div class="profile-section">
          <h4 class="section-title">通知设置</h4>
          <div class="section-content">
            <el-form ref="notificationForm" :model="notificationForm" label-width="200px">
              <el-form-item label="系统通知">
                <el-switch v-model="notificationForm.systemNotifications" />
                <div class="notification-desc">接收系统更新、维护等通知</div>
              </el-form-item>

              <el-form-item label="邮件通知">
                <el-switch v-model="notificationForm.emailNotifications" />
                <div class="notification-desc">通过邮件接收重要通知</div>
              </el-form-item>

              <el-form-item label="短信通知">
                <el-switch v-model="notificationForm.smsNotifications" />
                <div class="notification-desc">通过短信接收紧急通知</div>
              </el-form-item>

              <el-form-item label="订单提醒">
                <el-switch v-model="notificationForm.orderAlerts" />
                <div class="notification-desc">接收新订单、订单状态变更提醒</div>
              </el-form-item>

              <el-form-item label="课程提醒">
                <el-switch v-model="notificationForm.courseAlerts" />
                <div class="notification-desc">接收课程安排、变更提醒</div>
              </el-form-item>

              <el-form-item label="会员提醒">
                <el-switch v-model="notificationForm.memberAlerts" />
                <div class="notification-desc">接收会员相关通知</div>
              </el-form-item>

              <el-form-item label="安全提醒">
                <el-switch v-model="notificationForm.securityAlerts" />
                <div class="notification-desc">接收账户安全相关通知</div>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveNotificationSettings" :loading="savingNotifications">
                  保存通知设置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 偏好设置 -->
        <div class="profile-section">
          <h4 class="section-title">偏好设置</h4>
          <div class="section-content">
            <el-form ref="preferenceForm" :model="preferenceForm" label-width="200px">
              <el-form-item label="界面主题">
                <el-select v-model="preferenceForm.theme" style="width: 200px;">
                  <el-option label="浅色主题" value="light" />
                  <el-option label="深色主题" value="dark" />
                  <el-option label="自动跟随系统" value="auto" />
                </el-select>
              </el-form-item>

              <el-form-item label="语言">
                <el-select v-model="preferenceForm.language" style="width: 200px;">
                  <el-option label="简体中文" value="zh-CN" />
                  <el-option label="English" value="en-US" />
                </el-select>
              </el-form-item>

              <el-form-item label="默认时区">
                <el-select v-model="preferenceForm.timezone" filterable style="width: 200px;">
                  <el-option label="Asia/Shanghai (UTC+8)" value="Asia/Shanghai" />
                  <el-option label="Asia/Tokyo (UTC+9)" value="Asia/Tokyo" />
                  <el-option label="America/New_York (UTC-5)" value="America/New_York" />
                  <el-option label="Europe/London (UTC+0)" value="Europe/London" />
                </el-select>
              </el-form-item>

              <el-form-item label="日期格式">
                <el-select v-model="preferenceForm.dateFormat" style="width: 200px;">
                  <el-option label="YYYY-MM-DD" value="YYYY-MM-DD" />
                  <el-option label="MM/DD/YYYY" value="MM/DD/YYYY" />
                  <el-option label="DD/MM/YYYY" value="DD/MM/YYYY" />
                </el-select>
              </el-form-item>

              <el-form-item label="时间格式">
                <el-select v-model="preferenceForm.timeFormat" style="width: 200px;">
                  <el-option label="24小时制" value="24" />
                  <el-option label="12小时制" value="12" />
                </el-select>
              </el-form-item>

              <el-form-item label="列表默认每页显示">
                <el-select v-model="preferenceForm.pageSize" style="width: 120px;">
                  <el-option label="10条" :value="10" />
                  <el-option label="20条" :value="20" />
                  <el-option label="50条" :value="50" />
                  <el-option label="100条" :value="100" />
                </el-select>
              </el-form-item>

              <el-form-item label="侧边栏默认状态">
                <el-select v-model="preferenceForm.sidebarCollapsed" style="width: 120px;">
                  <el-option label="展开" :value="false" />
                  <el-option label="折叠" :value="true" />
                </el-select>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="savePreferences" :loading="savingPreferences">
                  保存偏好设置
                </el-button>
                <el-button @click="resetPreferences">恢复默认</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 高级设置 -->
        <div class="profile-section">
          <h4 class="section-title">高级设置</h4>
          <div class="section-content">
            <div class="advanced-actions">
              <el-button @click="exportPersonalData">导出个人数据</el-button>
              <el-button @click="viewDataUsage">查看数据使用情况</el-button>
              <el-button type="danger" @click="deleteAccount">删除账户</el-button>
            </div>

            <div class="data-privacy">
              <h5>数据隐私设置</h5>
              <el-form>
                <el-form-item label="公开个人信息">
                  <el-switch v-model="privacySettings.publicProfile" />
                  <div class="privacy-desc">是否允许其他用户查看您的个人资料</div>
                </el-form-item>

                <el-form-item label="显示在线状态">
                  <el-switch v-model="privacySettings.showOnlineStatus" />
                  <div class="privacy-desc">是否显示您的在线状态</div>
                </el-form-item>

                <el-form-item label="允许搜索">
                  <el-switch v-model="privacySettings.allowSearch" />
                  <div class="privacy-desc">是否允许其他用户通过搜索找到您</div>
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="savePrivacySettings" :loading="savingPrivacy">
                    保存隐私设置
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

// 表单引用
const basicInfoFormRef = ref<FormInstance>()
const notificationFormRef = ref<FormInstance>()
const preferenceFormRef = ref<FormInstance>()

// 响应式数据
const editingBasicInfo = ref(false)
const avatarUrl = ref('')
const originalBasicInfo = ref<any>(null)

const basicInfoForm = ref({
  username: 'admin',
  name: '管理员',
  gender: 'MALE' as 'MALE' | 'FEMALE' | 'UNKNOWN',
  birthDate: '1990-01-01',
  phone: '13800138000',
  email: 'admin@gymflow.com',
  department: 'MANAGEMENT',
  position: 'ADMIN',
  hireDate: '2023-01-01',
  bio: '系统管理员，负责管理系统日常运维',
  address: '北京市朝阳区'
})

const accountInfo = ref({
  id: '100001',
  role: 'ADMIN',
  registerTime: '2023-01-01 10:00:00',
  lastLoginTime: '2024-01-15 09:30:25',
  status: 'ACTIVE',
  emailVerified: true,
  phoneVerified: true
})

const notificationForm = ref({
  systemNotifications: true,
  emailNotifications: true,
  smsNotifications: false,
  orderAlerts: true,
  courseAlerts: true,
  memberAlerts: true,
  securityAlerts: true
})

const preferenceForm = ref({
  theme: 'light',
  language: 'zh-CN',
  timezone: 'Asia/Shanghai',
  dateFormat: 'YYYY-MM-DD',
  timeFormat: '24',
  pageSize: 20,
  sidebarCollapsed: false
})

const privacySettings = ref({
  publicProfile: false,
  showOnlineStatus: true,
  allowSearch: false
})

// 加载状态
const savingBasicInfo = ref(false)
const savingNotifications = ref(false)
const savingPreferences = ref(false)
const savingPrivacy = ref(false)

// 计算属性
const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL}/upload`)

const basicInfoRules = ref<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2到20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  position: [
    { required: true, message: '请选择职位', trigger: 'change' }
  ]
})

// 方法
const getRoleTagType = (role: string) => {
  const roleMap: Record<string, any> = {
    'ADMIN': 'danger',
    'COACH': 'success',
    'MEMBER': 'primary',
    'RECEPTIONIST': 'info',
    'SALES_CONSULTANT': 'warning'
  }
  return roleMap[role] || 'info'
}

const getRoleText = (role: string) => {
  const textMap: Record<string, string> = {
    'ADMIN': '管理员',
    'COACH': '教练',
    'MEMBER': '会员',
    'RECEPTIONIST': '前台',
    'SALES_CONSULTANT': '销售顾问'
  }
  return textMap[role] || role
}

// 头像上传处理
const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  
  return true
}

const handleAvatarSuccess = (response: any) => {
  if (response.code === 200 && response.data?.url) {
    avatarUrl.value = response.data.url
    ElMessage.success('头像上传成功')
  }
}

const removeAvatar = () => {
  avatarUrl.value = ''
  ElMessage.success('头像已移除')
}

// 开始编辑基本信息
const startEditingBasicInfo = () => {
  originalBasicInfo.value = JSON.parse(JSON.stringify(basicInfoForm.value))
  editingBasicInfo.value = true
}

// 取消编辑基本信息
const cancelEditingBasicInfo = () => {
  if (originalBasicInfo.value) {
    basicInfoForm.value = originalBasicInfo.value
  }
  editingBasicInfo.value = false
  basicInfoFormRef.value?.clearValidate()
}

// 保存基本信息
const saveBasicInfo = async () => {
  if (!basicInfoFormRef.value) return

  try {
    await basicInfoFormRef.value.validate()
    
    savingBasicInfo.value = true
    
    // 这里应该调用API保存基本信息
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    editingBasicInfo.value = false
    originalBasicInfo.value = null
    
    ElMessage.success('基本信息保存成功')
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    savingBasicInfo.value = false
  }
}

// 验证邮箱
const verifyEmail = async () => {
  try {
    // 这里应该调用API发送验证邮件
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('验证邮件已发送，请查收邮箱')
  } catch (error) {
    console.error('发送验证邮件失败:', error)
    ElMessage.error('发送失败')
  }
}

// 验证手机
const verifyPhone = async () => {
  try {
    // 这里应该调用API发送验证短信
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('验证短信已发送，请查收')
  } catch (error) {
    console.error('发送验证短信失败:', error)
    ElMessage.error('发送失败')
  }
}

// 查看登录历史
const viewLoginHistory = () => {
  router.push('/settings/security')
  // 滚动到登录历史部分
  setTimeout(() => {
    const element = document.querySelector('.login-history')
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' })
    }
  }, 100)
}

// 查看操作日志
const viewOperationLogs = () => {
  router.push('/settings/system-log')
}

// 查看账户安全设置
const viewAccountSecurity = () => {
  router.push('/settings/security')
}

// 保存通知设置
const saveNotificationSettings = async () => {
  savingNotifications.value = true
  
  try {
    // 这里应该调用API保存通知设置
    await new Promise(resolve => setTimeout(resolve, 800))
    
    ElMessage.success('通知设置保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    savingNotifications.value = false
  }
}

// 保存偏好设置
const savePreferences = async () => {
  savingPreferences.value = true
  
  try {
    // 这里应该调用API保存偏好设置
    await new Promise(resolve => setTimeout(resolve, 800))
    
    ElMessage.success('偏好设置保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    savingPreferences.value = false
  }
}

// 重置偏好设置
const resetPreferences = () => {
  preferenceForm.value = {
    theme: 'light',
    language: 'zh-CN',
    timezone: 'Asia/Shanghai',
    dateFormat: 'YYYY-MM-DD',
    timeFormat: '24',
    pageSize: 20,
    sidebarCollapsed: false
  }
  
  ElMessage.info('偏好设置已重置为默认值')
}

// 导出个人数据
const exportPersonalData = async () => {
  try {
    await ElMessageBox.confirm(
      '系统将准备您的个人数据文件，包含所有个人信息和操作记录。确认要导出吗？',
      '确认导出个人数据',
      {
        type: 'info',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    // 这里应该调用API导出个人数据
    ElMessage.success('个人数据导出请求已提交，将在完成后发送到您的邮箱')
  } catch (error) {
    // 用户取消
  }
}

// 查看数据使用情况
const viewDataUsage = () => {
  ElMessage.info('数据使用情况功能开发中')
}

// 删除账户
const deleteAccount = async () => {
  try {
    await ElMessageBox.confirm(
      '删除账户将永久删除您的所有数据，此操作不可恢复。确定要删除账户吗？',
      '确认删除账户',
      {
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    // 这里应该调用API删除账户
    ElMessage.warning('账户删除请求已提交，需要管理员审核')
  } catch (error) {
    // 用户取消
  }
}

// 保存隐私设置
const savePrivacySettings = async () => {
  savingPrivacy.value = true
  
  try {
    // 这里应该调用API保存隐私设置
    await new Promise(resolve => setTimeout(resolve, 800))
    
    ElMessage.success('隐私设置保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    savingPrivacy.value = false
  }
}

// 初始化
onMounted(() => {
  // 这里应该加载用户数据
  // 暂时使用模拟数据
  avatarUrl.value = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
})
</script>

<style scoped lang="scss">
.user-profile {
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
  
  .profile-content {
    .profile-section {
      margin-bottom: 30px;
      padding-bottom: 30px;
      border-bottom: 1px solid #e4e7ed;
      
      &:last-child {
        margin-bottom: 0;
        padding-bottom: 0;
        border-bottom: none;
      }
      
      .section-title {
        margin: 0 0 20px 0;
        font-size: 16px;
        color: #606266;
        font-weight: 500;
      }
      
      .section-content {
        .form-row {
          display: flex;
          gap: 40px;
          
          @media (max-width: 768px) {
            flex-direction: column;
            gap: 20px;
          }
          
          .form-column {
            flex: 1;
          }
        }
        
        .avatar-upload {
          display: flex;
          align-items: center;
          gap: 20px;
          
          .avatar-preview {
            border: 2px solid #e4e7ed;
          }
          
          .avatar-actions {
            display: flex;
            flex-direction: column;
            gap: 8px;
          }
        }
        
        .edit-actions {
          display: flex;
          gap: 10px;
        }
        
        .account-actions {
          margin-top: 20px;
          display: flex;
          gap: 10px;
        }
        
        .notification-desc,
        .privacy-desc {
          margin-top: 4px;
          font-size: 12px;
          color: #909399;
        }
        
        .advanced-actions {
          margin-bottom: 20px;
          display: flex;
          gap: 10px;
        }
        
        .data-privacy {
          padding: 20px;
          background-color: #f8f9fa;
          border-radius: 4px;
          border: 1px solid #e4e7ed;
          
          h5 {
            margin: 0 0 15px 0;
            font-size: 14px;
            color: #606266;
          }
        }
      }
    }
  }
}
</style>