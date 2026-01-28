<template>
  <div class="system-settings">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h3 class="card-title">系统设置</h3>
          <div class="card-subtitle">管理系统基本配置和参数</div>
        </div>
      </template>

      <div class="settings-content">
        <!-- 基本设置 -->
        <div class="settings-section">
          <h4 class="section-title">基本设置</h4>
          <div class="section-content">
            <el-form ref="basicForm" :model="basicForm" :rules="basicRules" label-width="150px">
              <el-form-item label="系统名称" prop="systemName">
                <el-input
                  v-model="basicForm.systemName"
                  placeholder="请输入系统名称"
                  style="width: 300px;"
                />
              </el-form-item>

              <el-form-item label="系统Logo">
                <div class="logo-upload">
                  <el-image
                    v-if="basicForm.logo"
                    :src="basicForm.logo"
                    class="logo-preview"
                    fit="contain"
                  />
                  <div v-else class="logo-placeholder">
                    <el-icon :size="48"><Picture /></el-icon>
                  </div>
                  <div class="logo-actions">
                    <el-upload
                      class="logo-uploader"
                      :action="uploadUrl"
                      :show-file-list="false"
                      :on-success="handleLogoSuccess"
                      :before-upload="beforeLogoUpload"
                    >
                      <el-button type="primary" size="small">
                        上传Logo
                      </el-button>
                    </el-upload>
                    <el-button
                      v-if="basicForm.logo"
                      type="text"
                      size="small"
                      @click="removeLogo"
                    >
                      移除
                    </el-button>
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="系统版本" prop="version">
                <el-input
                  v-model="basicForm.version"
                  placeholder="请输入系统版本"
                  style="width: 200px;"
                />
              </el-form-item>

              <el-form-item label="版权信息" prop="copyright">
                <el-input
                  v-model="basicForm.copyright"
                  type="textarea"
                  placeholder="请输入版权信息"
                  :rows="2"
                  style="width: 400px;"
                />
              </el-form-item>

              <el-form-item label="备案号" prop="recordNumber">
                <el-input
                  v-model="basicForm.recordNumber"
                  placeholder="请输入备案号"
                  style="width: 300px;"
                />
              </el-form-item>

              <el-form-item label="技术支持电话" prop="supportPhone">
                <el-input
                  v-model="basicForm.supportPhone"
                  placeholder="请输入技术支持电话"
                  style="width: 300px;"
                />
              </el-form-item>

              <el-form-item label="技术支持邮箱" prop="supportEmail">
                <el-input
                  v-model="basicForm.supportEmail"
                  placeholder="请输入技术支持邮箱"
                  style="width: 300px;"
                />
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 业务设置 -->
        <div class="settings-section">
          <h4 class="section-title">业务设置</h4>
          <div class="section-content">
            <el-form ref="businessForm" :model="businessForm" label-width="150px">
              <el-form-item label="营业时间">
                <div class="business-hours">
                  <el-time-select
                    v-model="businessForm.openTime"
                    placeholder="开始时间"
                    start="08:00"
                    step="00:30"
                    end="23:30"
                    style="width: 120px; margin-right: 10px;"
                  />
                  <span>至</span>
                  <el-time-select
                    v-model="businessForm.closeTime"
                    placeholder="结束时间"
                    start="08:00"
                    step="00:30"
                    end="23:30"
                    style="width: 120px; margin-left: 10px;"
                  />
                </div>
              </el-form-item>

              <el-form-item label="课程提前预约时间" prop="advanceBookingDays">
                <el-input-number
                  v-model="businessForm.advanceBookingDays"
                  :min="1"
                  :max="30"
                  controls-position="right"
                  style="width: 120px;"
                >
                  <template #suffix>天</template>
                </el-input-number>
              </el-form-item>

              <el-form-item label="课程取消时间限制" prop="cancelBeforeHours">
                <el-input-number
                  v-model="businessForm.cancelBeforeHours"
                  :min="1"
                  :max="24"
                  controls-position="right"
                  style="width: 120px;"
                >
                  <template #suffix>小时</template>
                </el-input-number>
              </el-form-item>

              <el-form-item label="自动签到时间" prop="autoCheckinMinutes">
                <el-input-number
                  v-model="businessForm.autoCheckinMinutes"
                  :min="5"
                  :max="60"
                  controls-position="right"
                  style="width: 120px;"
                >
                  <template #suffix>分钟</template>
                </el-input-number>
              </el-form-item>

              <el-form-item label="默认会员有效期" prop="defaultMemberValidity">
                <el-input-number
                  v-model="businessForm.defaultMemberValidity"
                  :min="1"
                  :max="36"
                  controls-position="right"
                  style="width: 120px;"
                >
                  <template #suffix>月</template>
                </el-input-number>
              </el-form-item>

              <el-form-item label="最低开课人数" prop="minClassSize">
                <el-input-number
                  v-model="businessForm.minClassSize"
                  :min="1"
                  :max="20"
                  controls-position="right"
                  style="width: 120px;"
                />
              </el-form-item>

              <el-form-item label="最大课程容量" prop="maxClassSize">
                <el-input-number
                  v-model="businessForm.maxClassSize"
                  :min="1"
                  :max="50"
                  controls-position="right"
                  style="width: 120px;"
                />
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 通知设置 -->
        <div class="settings-section">
          <h4 class="section-title">通知设置</h4>
          <div class="section-content">
            <el-form ref="notificationForm" :model="notificationForm" label-width="200px">
              <el-form-item label="启用短信通知">
                <el-switch v-model="notificationForm.smsEnabled" />
              </el-form-item>

              <el-form-item label="启用邮件通知">
                <el-switch v-model="notificationForm.emailEnabled" />
              </el-form-item>

              <el-form-item label="启用微信通知">
                <el-switch v-model="notificationForm.wechatEnabled" />
              </el-form-item>

              <el-form-item label="课程开始前通知">
                <el-input-number
                  v-model="notificationForm.courseReminderMinutes"
                  :min="10"
                  :max="1440"
                  :step="10"
                  controls-position="right"
                  style="width: 120px;"
                >
                  <template #suffix>分钟</template>
                </el-input-number>
              </el-form-item>

              <el-form-item label="会员到期前通知">
                <el-input-number
                  v-model="notificationForm.memberExpireDays"
                  :min="1"
                  :max="30"
                  controls-position="right"
                  style="width: 120px;"
                >
                  <template #suffix>天</template>
                </el-input-number>
              </el-form-item>

              <el-form-item label="会员卡余额不足通知">
                <el-input-number
                  v-model="notificationForm.lowBalanceSessions"
                  :min="1"
                  :max="20"
                  controls-position="right"
                  style="width: 120px;"
                >
                  <template #suffix>次</template>
                </el-input-number>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 支付设置 -->
        <div class="settings-section">
          <h4 class="section-title">支付设置</h4>
          <div class="section-content">
            <el-form ref="paymentForm" :model="paymentForm" label-width="150px">
              <el-form-item label="启用微信支付">
                <el-switch v-model="paymentForm.wechatPayEnabled" />
              </el-form-item>

              <el-form-item label="微信支付商户号" prop="wechatMerchantId">
                <el-input
                  v-model="paymentForm.wechatMerchantId"
                  placeholder="请输入微信支付商户号"
                  style="width: 300px;"
                  :disabled="!paymentForm.wechatPayEnabled"
                />
              </el-form-item>

              <el-form-item label="启用支付宝">
                <el-switch v-model="paymentForm.alipayEnabled" />
              </el-form-item>

              <el-form-item label="支付宝商户号" prop="alipayMerchantId">
                <el-input
                  v-model="paymentForm.alipayMerchantId"
                  placeholder="请输入支付宝商户号"
                  style="width: 300px;"
                  :disabled="!paymentForm.alipayEnabled"
                />
              </el-form-item>

              <el-form-item label="启用现金支付">
                <el-switch v-model="paymentForm.cashEnabled" />
              </el-form-item>

              <el-form-item label="启用银行卡支付">
                <el-switch v-model="paymentForm.bankCardEnabled" />
              </el-form-item>

              <el-form-item label="自动取消未支付订单">
                <el-input-number
                  v-model="paymentForm.autoCancelMinutes"
                  :min="5"
                  :max="1440"
                  controls-position="right"
                  style="width: 120px;"
                >
                  <template #suffix>分钟</template>
                </el-input-number>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 保存按钮 -->
        <div class="settings-actions">
          <el-button @click="resetSettings">重置</el-button>
          <el-button type="primary" @click="saveSettings" :loading="saving">
            保存设置
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 表单引用
const basicFormRef = ref<FormInstance>()
const businessFormRef = ref<FormInstance>()
const notificationFormRef = ref<FormInstance>()
const paymentFormRef = ref<FormInstance>()

// 响应式数据
const basicForm = ref({
  systemName: 'GymFlow 健身工作室管理系统',
  logo: '',
  version: '1.0.0',
  copyright: 'Copyright © 2024 GymFlow 健身工作室 版权所有',
  recordNumber: '京ICP备2024000000号',
  supportPhone: '400-123-4567',
  supportEmail: 'support@gymflow.com'
})

const businessForm = ref({
  openTime: '08:00',
  closeTime: '22:00',
  advanceBookingDays: 7,
  cancelBeforeHours: 2,
  autoCheckinMinutes: 15,
  defaultMemberValidity: 12,
  minClassSize: 3,
  maxClassSize: 20
})

const notificationForm = ref({
  smsEnabled: true,
  emailEnabled: true,
  wechatEnabled: true,
  courseReminderMinutes: 30,
  memberExpireDays: 7,
  lowBalanceSessions: 5
})

const paymentForm = ref({
  wechatPayEnabled: true,
  wechatMerchantId: '',
  alipayEnabled: true,
  alipayMerchantId: '',
  cashEnabled: true,
  bankCardEnabled: true,
  autoCancelMinutes: 30
})

const saving = ref(false)
const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL}/upload`)

// 表单验证规则
const basicRules: FormRules = {
  systemName: [
    { required: true, message: '请输入系统名称', trigger: 'blur' }
  ],
  version: [
    { required: true, message: '请输入系统版本', trigger: 'blur' }
  ],
  supportPhone: [
    { pattern: /^[\d-]+$/, message: '请输入正确的电话号码', trigger: 'blur' }
  ],
  supportEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 方法
const beforeLogoUpload = (file: File) => {
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

const handleLogoSuccess = (response: any) => {
  if (response.code === 200 && response.data?.url) {
    basicForm.value.logo = response.data.url
    ElMessage.success('Logo上传成功')
  }
}

const removeLogo = () => {
  basicForm.value.logo = ''
  ElMessage.success('Logo已移除')
}

const saveSettings = async () => {
  const forms = [basicFormRef, businessFormRef, notificationFormRef, paymentFormRef]
  
  for (const formRef of forms) {
    if (formRef.value) {
      try {
        await formRef.value.validate()
      } catch (error) {
        ElMessage.error('请检查表单填写是否正确')
        return
      }
    }
  }
  
  saving.value = true
  
  try {
    // 这里应该调用API保存设置
    const allSettings = {
      basic: basicForm.value,
      business: businessForm.value,
      notification: notificationForm.value,
      payment: paymentForm.value
    }
    
    console.log('保存设置:', allSettings)
    
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('设置保存成功')
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const resetSettings = () => {
  basicForm.value = {
    systemName: 'GymFlow 健身工作室管理系统',
    logo: '',
    version: '1.0.0',
    copyright: 'Copyright © 2024 GymFlow 健身工作室 版权所有',
    recordNumber: '京ICP备2024000000号',
    supportPhone: '400-123-4567',
    supportEmail: 'support@gymflow.com'
  }
  
  businessForm.value = {
    openTime: '08:00',
    closeTime: '22:00',
    advanceBookingDays: 7,
    cancelBeforeHours: 2,
    autoCheckinMinutes: 15,
    defaultMemberValidity: 12,
    minClassSize: 3,
    maxClassSize: 20
  }
  
  notificationForm.value = {
    smsEnabled: true,
    emailEnabled: true,
    wechatEnabled: true,
    courseReminderMinutes: 30,
    memberExpireDays: 7,
    lowBalanceSessions: 5
  }
  
  paymentForm.value = {
    wechatPayEnabled: true,
    wechatMerchantId: '',
    alipayEnabled: true,
    alipayMerchantId: '',
    cashEnabled: true,
    bankCardEnabled: true,
    autoCancelMinutes: 30
  }
  
  ElMessage.info('设置已重置为默认值')
}
</script>

<style scoped lang="scss">
.system-settings {
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
  
  .settings-content {
    .settings-section {
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
        .logo-upload {
          display: flex;
          align-items: center;
          gap: 20px;
          
          .logo-preview {
            width: 120px;
            height: 120px;
            border: 1px solid #dcdfe6;
            border-radius: 4px;
            padding: 8px;
            background-color: #f8f9fa;
          }
          
          .logo-placeholder {
            width: 120px;
            height: 120px;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 1px dashed #dcdfe6;
            border-radius: 4px;
            background-color: #f8f9fa;
            color: #909399;
          }
          
          .logo-actions {
            display: flex;
            flex-direction: column;
            gap: 8px;
          }
        }
        
        .business-hours {
          display: flex;
          align-items: center;
        }
      }
    }
    
    .settings-actions {
      margin-top: 20px;
      text-align: center;
    }
  }
}
</style>