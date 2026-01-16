<template>
  <div class="security-settings">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h3 class="card-title">安全设置</h3>
          <div class="card-subtitle">管理账户安全和访问权限</div>
        </div>
      </template>

      <div class="security-content">
        <!-- 密码安全 -->
        <div class="security-section">
          <h4 class="section-title">密码安全</h4>
          <div class="section-content">
            <el-form ref="passwordForm" :model="passwordForm" :rules="passwordRules" label-width="150px">
              <el-form-item label="当前密码" prop="currentPassword">
                <el-input
                  v-model="passwordForm.currentPassword"
                  type="password"
                  placeholder="请输入当前密码"
                  show-password
                  style="width: 300px;"
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  show-password
                  style="width: 300px;"
                />
                <div class="password-tips">
                  <el-progress
                    :percentage="passwordStrength"
                    :status="getPasswordStrengthStatus"
                    :show-text="false"
                    style="width: 300px; margin-top: 5px;"
                  />
                  <div class="password-requirements">
                    <span :class="{ 'met': passwordRequirements.length }">至少8个字符</span>
                    <span :class="{ 'met': passwordRequirements.uppercase }">包含大写字母</span>
                    <span :class="{ 'met': passwordRequirements.lowercase }">包含小写字母</span>
                    <span :class="{ 'met': passwordRequirements.number }">包含数字</span>
                    <span :class="{ 'met': passwordRequirements.special }">包含特殊字符</span>
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                  style="width: 300px;"
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="updatePassword" :loading="updatingPassword">
                  更新密码
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 双重认证 -->
        <div class="security-section">
          <h4 class="section-title">双重认证</h4>
          <div class="section-content">
            <div class="two-factor-status">
              <el-tag :type="twoFactorEnabled ? 'success' : 'info'" size="large">
                {{ twoFactorEnabled ? '已启用' : '未启用' }}
              </el-tag>
              <p class="status-description">
                {{ twoFactorEnabled ? 
                  '双重认证已启用，登录时需要输入验证码' : 
                  '启用双重认证可以增加账户安全性' }}
              </p>
            </div>

            <div class="two-factor-actions">
              <el-button
                v-if="!twoFactorEnabled"
                type="primary"
                @click="enableTwoFactor"
                :loading="enablingTwoFactor"
              >
                启用双重认证
              </el-button>
              <el-button
                v-else
                type="danger"
                @click="disableTwoFactor"
                :loading="disablingTwoFactor"
              >
                禁用双重认证
              </el-button>
            </div>

            <div class="two-factor-info" v-if="showTwoFactorSetup">
              <div class="qr-code-container">
                <h5>扫描二维码</h5>
                <div class="qr-code" ref="qrCodeRef"></div>
                <p class="qr-instructions">使用身份验证应用（如 Google Authenticator）扫描二维码</p>
              </div>

              <div class="backup-codes">
                <h5>备份验证码</h5>
                <div class="codes-list">
                  <div v-for="(code, index) in backupCodes" :key="index" class="backup-code">
                    {{ code }}
                  </div>
                </div>
                <p class="codes-warning">请妥善保管这些验证码，每个验证码只能使用一次</p>
                <el-button @click="generateBackupCodes" :loading="generatingCodes">
                  重新生成备份验证码
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 登录历史 -->
        <div class="security-section">
          <h4 class="section-title">登录历史</h4>
          <div class="section-content">
            <el-table
              :data="loginHistory"
              style="width: 100%"
              stripe
              border
            >
              <el-table-column label="登录时间" prop="loginTime" width="180" />
              <el-table-column label="登录IP" prop="ip" width="150" />
              <el-table-column label="登录地点" prop="location" />
              <el-table-column label="设备信息" prop="device" />
              <el-table-column label="登录状态" prop="status" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === '成功' ? 'success' : 'danger'" size="small">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button
                    v-if="row.status === '成功'"
                    type="text"
                    size="small"
                    @click="logoutDevice(row.id)"
                  >
                    退出登录
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="login-history-actions">
              <el-button @click="loadMoreHistory" :loading="loadingHistory" :disabled="!hasMoreHistory">
                加载更多
              </el-button>
              <el-button type="danger" @click="logoutAllDevices" :loading="loggingOutAll">
                退出所有设备
              </el-button>
            </div>
          </div>
        </div>

        <!-- 会话管理 -->
        <div class="security-section">
          <h4 class="section-title">会话管理</h4>
          <div class="section-content">
            <div class="session-info">
              <p>当前登录会话将在以下时间后过期：<strong>{{ sessionExpiresIn }}</strong></p>
              <p>最大会话时长：<strong>24小时</strong></p>
            </div>

            <div class="session-actions">
              <el-button @click="refreshSession">
                刷新会话
              </el-button>
              <el-button type="warning" @click="logoutCurrentSession">
                退出当前会话
              </el-button>
            </div>
          </div>
        </div>

        <!-- 安全策略 -->
        <div class="security-section">
          <h4 class="section-title">安全策略</h4>
          <div class="section-content">
            <el-form ref="policyForm" :model="policyForm" label-width="200px">
              <el-form-item label="密码有效期">
                <el-select v-model="policyForm.passwordExpiryDays" style="width: 150px;">
                  <el-option label="30天" :value="30" />
                  <el-option label="60天" :value="60" />
                  <el-option label="90天" :value="90" />
                  <el-option label="180天" :value="180" />
                  <el-option label="永不过期" :value="0" />
                </el-select>
              </el-form-item>

              <el-form-item label="密码历史记录">
                <el-select v-model="policyForm.passwordHistoryCount" style="width: 150px;">
                  <el-option label="不保存" :value="0" />
                  <el-option label="保存3个" :value="3" />
                  <el-option label="保存5个" :value="5" />
                  <el-option label="保存10个" :value="10" />
                </el-select>
              </el-form-item>

              <el-form-item label="登录失败锁定">
                <el-switch v-model="policyForm.lockoutEnabled" />
                <div class="policy-details" v-if="policyForm.lockoutEnabled">
                  <el-input-number
                    v-model="policyForm.maxFailedAttempts"
                    :min="1"
                    :max="10"
                    size="small"
                    style="width: 80px; margin: 0 10px;"
                  />
                  次失败后锁定
                  <el-input-number
                    v-model="policyForm.lockoutMinutes"
                    :min="1"
                    :max="60"
                    size="small"
                    style="width: 80px; margin: 0 10px;"
                  />
                  分钟
                </div>
              </el-form-item>

              <el-form-item label="会话超时">
                <el-select v-model="policyForm.sessionTimeoutMinutes" style="width: 150px;">
                  <el-option label="15分钟" :value="15" />
                  <el-option label="30分钟" :value="30" />
                  <el-option label="1小时" :value="60" />
                  <el-option label="2小时" :value="120" />
                  <el-option label="4小时" :value="240" />
                </el-select>
              </el-form-item>

              <el-form-item label="异地登录提醒">
                <el-switch v-model="policyForm.geoLocationAlert" />
              </el-form-item>

              <el-form-item label="新设备登录验证">
                <el-switch v-model="policyForm.newDeviceVerification" />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveSecurityPolicy" :loading="savingPolicy">
                  保存安全策略
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import QRCode from 'qrcode'

interface PasswordForm {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

interface SecurityPolicy {
  passwordExpiryDays: number
  passwordHistoryCount: number
  lockoutEnabled: boolean
  maxFailedAttempts: number
  lockoutMinutes: number
  sessionTimeoutMinutes: number
  geoLocationAlert: boolean
  newDeviceVerification: boolean
}

interface LoginRecord {
  id: string
  loginTime: string
  ip: string
  location: string
  device: string
  status: string
}

// 表单引用
const passwordForm = ref<FormInstance>()
const policyForm = ref<FormInstance>()

// 响应式数据
const passwordFormData = ref<PasswordForm>({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const policyFormData = ref<SecurityPolicy>({
  passwordExpiryDays: 90,
  passwordHistoryCount: 5,
  lockoutEnabled: true,
  maxFailedAttempts: 5,
  lockoutMinutes: 30,
  sessionTimeoutMinutes: 60,
  geoLocationAlert: true,
  newDeviceVerification: true
})

const twoFactorEnabled = ref(false)
const showTwoFactorSetup = ref(false)
const backupCodes = ref<string[]>([])
const loginHistory = ref<LoginRecord[]>([])
const qrCodeRef = ref<HTMLElement>()

// 加载状态
const updatingPassword = ref(false)
const enablingTwoFactor = ref(false)
const disablingTwoFactor = ref(false)
const generatingCodes = ref(false)
const loadingHistory = ref(false)
const loggingOutAll = ref(false)
const savingPolicy = ref(false)

// 计算属性
const passwordRequirements = computed(() => {
  const password = passwordFormData.value.newPassword
  return {
    length: password.length >= 8,
    uppercase: /[A-Z]/.test(password),
    lowercase: /[a-z]/.test(password),
    number: /\d/.test(password),
    special: /[!@#$%^&*(),.?":{}|<>]/.test(password)
  }
})

const passwordStrength = computed(() => {
  const requirements = passwordRequirements.value
  const metCount = Object.values(requirements).filter(Boolean).length
  return (metCount / 5) * 100
})

const getPasswordStrengthStatus = computed(() => {
  const strength = passwordStrength.value
  if (strength < 40) return 'exception'
  if (strength < 80) return 'warning'
  return 'success'
})

const sessionExpiresIn = computed(() => {
  // 这里应该计算会话剩余时间
  return '23小时45分钟'
})

const hasMoreHistory = computed(() => {
  return loginHistory.value.length < 50 // 假设有50条记录
})

// 密码验证规则
const passwordRules = ref<FormRules>({
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度至少8位', trigger: 'blur' },
    { validator: validatePasswordStrength, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validatePasswordMatch, trigger: 'blur' }
  ]
})

// 密码强度验证
const validatePasswordStrength = (_: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入新密码'))
    return
  }

  const requirements = passwordRequirements.value
  const metRequirements = Object.values(requirements).filter(Boolean).length

  if (metRequirements < 3) {
    callback(new Error('密码强度不足，请包含大写字母、小写字母、数字和特殊字符'))
  } else {
    callback()
  }
}

// 密码匹配验证
const validatePasswordMatch = (_: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordFormData.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 更新密码
const updatePassword = async () => {
  if (!passwordForm.value) return

  try {
    await passwordForm.value.validate()
    
    updatingPassword.value = true
    
    // 这里应该调用API更新密码
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('密码更新成功')
    
    // 重置表单
    passwordFormData.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
    passwordForm.value.clearValidate()
    
  } catch (error) {
    console.error('密码更新失败:', error)
  } finally {
    updatingPassword.value = false
  }
}

// 启用双重认证
const enableTwoFactor = async () => {
  enablingTwoFactor.value = true
  
  try {
    // 这里应该调用API启用双重认证并获取二维码
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟生成二维码
    if (qrCodeRef.value) {
      const qrCodeUrl = 'otpauth://totp/GymFlow:admin@example.com?secret=JBSWY3DPEHPK3PXP&issuer=GymFlow'
      await QRCode.toCanvas(qrCodeRef.value, qrCodeUrl, { width: 200 })
    }
    
    // 生成备份验证码
    generateBackupCodes()
    
    twoFactorEnabled.value = true
    showTwoFactorSetup.value = true
    
    ElMessage.success('双重认证已启用，请扫描二维码配置身份验证应用')
  } catch (error) {
    console.error('启用双重认证失败:', error)
    ElMessage.error('启用双重认证失败')
  } finally {
    enablingTwoFactor.value = false
  }
}

// 禁用双重认证
const disableTwoFactor = async () => {
  try {
    await ElMessageBox.confirm(
      '禁用双重认证会降低账户安全性，确定要继续吗？',
      '确认禁用双重认证',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    disablingTwoFactor.value = true
    
    // 这里应该调用API禁用双重认证
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    twoFactorEnabled.value = false
    showTwoFactorSetup.value = false
    
    ElMessage.success('双重认证已禁用')
  } catch (error) {
    // 用户取消
  } finally {
    disablingTwoFactor.value = false
  }
}

// 生成备份验证码
const generateBackupCodes = async () => {
  generatingCodes.value = true
  
  try {
    // 这里应该调用API生成备份验证码
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟生成8个备份验证码
    const codes: string[] = []
    for (let i = 0; i < 8; i++) {
      const code = Array.from({ length: 8 }, () => 
        Math.floor(Math.random() * 10)
      ).join('')
      codes.push(code)
    }
    
    backupCodes.value = codes
    
    ElMessage.success('备份验证码已生成')
  } catch (error) {
    console.error('生成备份验证码失败:', error)
    ElMessage.error('生成备份验证码失败')
  } finally {
    generatingCodes.value = false
  }
}

// 加载登录历史
const loadLoginHistory = async () => {
  loadingHistory.value = true
  
  try {
    // 这里应该调用API获取登录历史
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 模拟数据
    const mockHistory: LoginRecord[] = []
    for (let i = 0; i < 10; i++) {
      const date = new Date()
      date.setDate(date.getDate() - i)
      
      mockHistory.push({
        id: `${i}`,
        loginTime: date.toISOString().replace('T', ' ').substring(0, 19),
        ip: `192.168.1.${100 + i}`,
        location: i === 0 ? '本地登录' : `北京朝阳区`,
        device: i === 0 ? 'Chrome/Windows' : 'Safari/iOS',
        status: i < 9 ? '成功' : '失败'
      })
    }
    
    loginHistory.value = mockHistory
  } catch (error) {
    console.error('加载登录历史失败:', error)
  } finally {
    loadingHistory.value = false
  }
}

// 加载更多历史
const loadMoreHistory = async () => {
  if (loadingHistory.value) return
  
  loadingHistory.value = true
  
  try {
    // 这里应该调用API加载更多历史记录
    await new Promise(resolve => setTimeout(resolve, 800))
    
    const currentLength = loginHistory.value.length
    const moreHistory: LoginRecord[] = []
    
    for (let i = 0; i < 5; i++) {
      const date = new Date()
      date.setDate(date.getDate() - currentLength - i)
      
      moreHistory.push({
        id: `${currentLength + i}`,
        loginTime: date.toISOString().replace('T', ' ').substring(0, 19),
        ip: `192.168.1.${150 + i}`,
        location: `北京朝阳区`,
        device: i % 2 === 0 ? 'Chrome/Windows' : 'Safari/iOS',
        status: '成功'
      })
    }
    
    loginHistory.value.push(...moreHistory)
  } catch (error) {
    console.error('加载更多历史失败:', error)
  } finally {
    loadingHistory.value = false
  }
}

// 退出设备
const logoutDevice = async (deviceId: string) => {
  try {
    await ElMessageBox.confirm(
      '确定要退出该设备的登录吗？',
      '确认退出登录',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    // 这里应该调用API退出设备
    await new Promise(resolve => setTimeout(resolve, 500))
    
    loginHistory.value = loginHistory.value.filter(record => record.id !== deviceId)
    
    ElMessage.success('设备已退出登录')
  } catch (error) {
    // 用户取消
  }
}

// 退出所有设备
const logoutAllDevices = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出所有设备的登录吗？当前会话也会被退出。',
      '确认退出所有设备',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    loggingOutAll.value = true
    
    // 这里应该调用API退出所有设备
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    loginHistory.value = []
    
    ElMessage.success('所有设备已退出登录')
  } catch (error) {
    // 用户取消
  } finally {
    loggingOutAll.value = false
  }
}

// 刷新会话
const refreshSession = async () => {
  try {
    // 这里应该调用API刷新会话
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('会话已刷新')
  } catch (error) {
    console.error('刷新会话失败:', error)
  }
}

// 退出当前会话
const logoutCurrentSession = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出当前会话吗？',
      '确认退出登录',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    // 这里应该调用API退出当前会话
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('当前会话已退出')
    
    // 重新登录
    // window.location.href = '/login'
  } catch (error) {
    // 用户取消
  }
}

// 保存安全策略
const saveSecurityPolicy = async () => {
  savingPolicy.value = true
  
  try {
    // 这里应该调用API保存安全策略
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('安全策略已保存')
  } catch (error) {
    console.error('保存安全策略失败:', error)
    ElMessage.error('保存失败')
  } finally {
    savingPolicy.value = false
  }
}

// 监听密码变化
watch(
  () => passwordFormData.value.newPassword,
  () => {
    // 当密码变化时触发验证
    if (passwordForm.value) {
      passwordForm.value.validateField('newPassword')
    }
  }
)

// 初始化
onMounted(() => {
  loadLoginHistory()
  generateBackupCodes()
})
</script>

<style scoped lang="scss">
.security-settings {
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
  
  .security-content {
    .security-section {
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
        .password-tips {
          .password-requirements {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-top: 8px;
            font-size: 12px;
            
            span {
              color: #c0c4cc;
              
              &.met {
                color: #67c23a;
              }
            }
          }
        }
        
        .two-factor-status {
          margin-bottom: 20px;
          
          .status-description {
            margin: 10px 0 0 0;
            color: #909399;
            font-size: 14px;
          }
        }
        
        .two-factor-info {
          margin-top: 20px;
          padding: 20px;
          background-color: #f8f9fa;
          border-radius: 4px;
          border: 1px solid #e4e7ed;
          
          .qr-code-container {
            margin-bottom: 20px;
            
            h5 {
              margin: 0 0 10px 0;
              font-size: 14px;
              color: #606266;
            }
            
            .qr-code {
              text-align: center;
              margin-bottom: 10px;
            }
            
            .qr-instructions {
              margin: 0;
              font-size: 12px;
              color: #909399;
              text-align: center;
            }
          }
          
          .backup-codes {
            h5 {
              margin: 0 0 10px 0;
              font-size: 14px;
              color: #606266;
            }
            
            .codes-list {
              display: grid;
              grid-template-columns: repeat(2, 1fr);
              gap: 10px;
              margin-bottom: 10px;
              
              @media (max-width: 768px) {
                grid-template-columns: 1fr;
              }
              
              .backup-code {
                padding: 8px 12px;
                background-color: #ffffff;
                border: 1px solid #dcdfe6;
                border-radius: 4px;
                font-family: monospace;
                text-align: center;
                font-size: 14px;
              }
            }
            
            .codes-warning {
              margin: 0 0 15px 0;
              font-size: 12px;
              color: #e6a23c;
            }
          }
        }
        
        .login-history-actions {
          margin-top: 20px;
          display: flex;
          gap: 10px;
        }
        
        .session-info {
          margin-bottom: 20px;
          
          p {
            margin: 8px 0;
            color: #606266;
            
            strong {
              color: #303133;
            }
          }
        }
        
        .session-actions {
          display: flex;
          gap: 10px;
        }
        
        .policy-details {
          margin-top: 10px;
          display: flex;
          align-items: center;
          color: #606266;
          font-size: 14px;
        }
      }
    }
  }
}
</style>