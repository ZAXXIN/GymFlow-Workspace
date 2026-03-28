<template>
  <div class="system-settings">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h3 class="card-title">系统设置</h3>
          <div class="card-subtitle">管理系统基本配置和业务参数</div>
        </div>
      </template>

      <div class="settings-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-wrapper">
          <el-skeleton :rows="10" animated />
        </div>

        <template v-else>
          <!-- 基本设置 -->
          <div class="settings-section">
            <h4 class="section-title">基本设置</h4>
            <div class="section-content">
              <el-form ref="basicFormRef" :model="basicForm" :rules="basicRules" label-width="120px" status-icon>
                <el-form-item label="系统名称" prop="systemName">
                  <el-input v-model="basicForm.systemName" placeholder="请输入系统名称" maxlength="100" clearable style="width: 400px;" />
                  <div class="form-tip">显示在浏览器标签和页面标题上</div>
                </el-form-item>

                <el-form-item label="系统Logo" prop="systemLogo">
                  <div class="logo-upload">
                    <el-image v-if="basicForm.systemLogo" :src="basicForm.systemLogo" class="logo-preview" fit="contain">
                      <template #error>
                        <div class="image-error">
                          <el-icon>
                            <Picture />
                          </el-icon>
                          <span>加载失败</span>
                        </div>
                      </template>
                    </el-image>
                    <div v-else class="logo-placeholder">
                      <el-icon :size="48">
                        <Picture />
                      </el-icon>
                      <span>暂无Logo</span>
                    </div>
                    <div class="logo-actions">
                      <el-upload class="logo-uploader" :show-file-list="false" :before-upload="beforeLogoUpload" :http-request="handleUploadRequest" :disabled="uploading">
                        <el-button type="primary" size="small" :loading="uploading">
                          {{ uploading ? '上传中...' : '上传Logo' }}
                        </el-button>
                      </el-upload>
                      <el-button v-if="basicForm.systemLogo" type="danger" size="small" plain @click="removeLogo">
                        移除
                      </el-button>
                    </div>
                  </div>
                  <div class="form-tip">建议尺寸：200x200像素，支持PNG、JPG格式，大小不超过2MB</div>
                </el-form-item>
              </el-form>
            </div>
          </div>

          <!-- 业务设置 -->
          <div class="settings-section">
            <h4 class="section-title">业务设置</h4>
            <div class="section-content">
              <el-form ref="businessFormRef" :model="businessForm" :rules="businessRules" label-width="150px" status-icon>
                <el-form-item label="营业时间" prop="businessHours">
                  <div class="business-hours">
                    <el-time-picker v-model="businessForm.businessStartTime" format="HH:mm" value-format="HH:mm:ss" placeholder="开始时间" :disabled-hours="disabledStartHours" style="width: 120px;" />
                    <span class="time-separator">至</span>
                    <el-time-picker v-model="businessForm.businessEndTime" format="HH:mm" value-format="HH:mm:ss" placeholder="结束时间" :disabled-hours="disabledEndHours" style="width: 120px;" />
                  </div>
                  <div class="form-tip">课程排课、签到只能在营业时间内进行</div>
                </el-form-item>

                <el-form-item label="课程提前预约时间" prop="courseAdvanceBookingHours">
                  <el-input-number v-model="businessForm.courseAdvanceBookingHours" :min="0" :max="12" controls-position="right" style="width: 150px;">
                    <template #suffix>小时</template>
                  </el-input-number>
                  <div class="form-tip">课程需要提前多少小时预约</div>
                </el-form-item>

                <el-form-item label="课程取消时间限制" prop="courseCancelHours">
                  <el-input-number v-model="businessForm.courseCancelHours" :min="0" :max="2" controls-position="right" style="width: 150px;">
                    <template #suffix>小时</template>
                  </el-input-number>
                  <div class="form-tip">课程开始前多少小时内不能取消</div>
                </el-form-item>

                <el-form-item label="签到开始时间" prop="checkinStartMinutes">
                  <el-input-number v-model="businessForm.checkinStartMinutes" :min="0" :max="60" controls-position="right" style="width: 150px;">
                    <template #suffix>分钟</template>
                  </el-input-number>
                  <div class="form-tip">课程开始前多少分钟可签到</div>
                </el-form-item>

                <el-form-item label="签到截止时间" prop="checkinEndMinutes">
                  <el-input-number v-model="businessForm.checkinEndMinutes" :min="0" :max="60" controls-position="right" style="width: 150px;">
                    <template #suffix>分钟</template>
                  </el-input-number>
                  <div class="form-tip">课程开始后多少分钟截止签到</div>
                </el-form-item>

                <el-form-item label="最低开课人数" prop="minClassSize">
                  <el-input-number v-model="businessForm.minClassSize" :min="1" :max="20" controls-position="right" style="width: 150px;">
                    <template #suffix>人</template>
                  </el-input-number>
                  <div class="form-tip">团课报名人数达到此值才能开课</div>
                </el-form-item>

                <el-form-item label="最大课程容量" prop="maxClassCapacity">
                  <el-input-number v-model="businessForm.maxClassCapacity" :min="1" :max="100" controls-position="right" style="width: 150px;">
                    <template #suffix>人</template>
                  </el-input-number>
                  <div class="form-tip">团课最多允许报名人数</div>
                </el-form-item>

                <el-form-item label="自动完成时间" prop="autoCompleteHours">
                  <el-input-number v-model="businessForm.autoCompleteHours" :min="0" :max="24" controls-position="right" style="width: 150px;">
                    <template #suffix>小时</template>
                  </el-input-number>
                  <div class="form-tip">课程结束后多少小时自动变更为已完成</div>
                </el-form-item>
              </el-form>
            </div>
          </div>

          <!-- 最后更新时间 -->
          <div v-if="updateTime" class="update-time">
            最后更新：{{ updateTime }}
          </div>

          <!-- 保存按钮 -->
          <div class="settings-actions">
            <el-button @click="handleReset" :disabled="saving">重置</el-button>
            <el-button type="primary" @click="handleSave" :loading="saving">
              保存设置
            </el-button>
          </div>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { useSystemConfigStore } from '@/stores/settings/systemConfig'
import { useUploadStore } from '@/stores/common/upload'

const systemConfigStore = useSystemConfigStore()
const uploadStore = useUploadStore()

// 表单引用
const basicFormRef = ref<FormInstance>()
const businessFormRef = ref<FormInstance>()

// 状态
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const updateTime = ref('')

// 基本设置表单
const basicForm = reactive({
  systemName: '',
  systemLogo: '',
})

// 业务设置表单
const businessForm = reactive({
  businessStartTime: '08:00:00',
  businessEndTime: '22:00:00',
  courseAdvanceBookingHours: 2,
  courseCancelHours: 2,
  checkinStartMinutes: 2,
  checkinEndMinutes: 2,
  minClassSize: 5,
  maxClassCapacity: 20,
  autoCompleteHours:1
})

// 表单验证规则
const basicRules: FormRules = {
  systemName: [
    { required: true, message: '请输入系统名称', trigger: 'blur' },
    { min: 2, max: 100, message: '系统名称长度在2-100个字符', trigger: 'blur' },
  ],
}

const businessRules: FormRules = {
  businessStartTime: [{ required: true, message: '请选择营业开始时间', trigger: 'change' }],
  businessEndTime: [{ required: true, message: '请选择营业结束时间', trigger: 'change' }],
  courseAdvanceBookingHours: [
    { required: true, message: '请输入课程提前预约时间', trigger: 'blur' },
  ],
  courseCancelHours: [{ required: true, message: '请输入课程取消时间限制', trigger: 'blur' }],
  checkinStartMinutes: [{ required: true, message: '请输入课程开始前可签到时间', trigger: 'blur' }],
  checkinEndMinutes: [{ required: true, message: '请输入课程开始后可签到时间', trigger: 'blur' }],
  minClassSize: [{ required: true, message: '请输入最低开课人数', trigger: 'blur' }],
  maxClassCapacity: [{ required: true, message: '请输入最大课程容量', trigger: 'blur' }],
  autoCompleteHours:[{ required: true, message: '请输入自动完成时间', trigger: 'blur' }]
}

// 禁用的小时选项
const disabledStartHours = computed(() => {
  if (!businessForm.businessEndTime) return []
  const endHour = parseInt(businessForm.businessEndTime.split(':')[0])
  return Array.from({ length: 24 }, (_, i) => i).filter((h) => h > endHour)
})

const disabledEndHours = computed(() => {
  if (!businessForm.businessStartTime) return []
  const startHour = parseInt(businessForm.businessStartTime.split(':')[0])
  return Array.from({ length: 24 }, (_, i) => i).filter((h) => h < startHour)
})

// 验证营业时间
watch(
  [() => businessForm.businessStartTime, () => businessForm.businessEndTime],
  ([start, end]) => {
    if (start && end && start >= end) {
      ElMessage.warning('营业结束时间不能早于或等于开始时间')
    }
  }
)

// 加载配置
const loadConfig = async () => {
  loading.value = true
  try {
    const config = await systemConfigStore.fetchConfig()
    if (config) {
      // 填充基本设置
      basicForm.systemName = config.basic.systemName
      basicForm.systemLogo = config.basic.systemLogo || ''

      // 填充业务设置
      businessForm.businessStartTime = config.business.businessStartTime
      businessForm.businessEndTime = config.business.businessEndTime
      businessForm.courseAdvanceBookingHours = config.business.courseAdvanceBookingHours
      businessForm.courseCancelHours = config.business.courseCancelHours
      businessForm.checkinStartMinutes = config.business.checkinStartMinutes
      businessForm.checkinEndMinutes = config.business.checkinEndMinutes
      businessForm.minClassSize = config.business.minClassSize
      businessForm.maxClassCapacity = config.business.maxClassCapacity
      businessForm.autoCompleteHours = config.business.autoCompleteHours

      updateTime.value = config.updateTime || ''
    }
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.error('加载系统配置失败')
  } finally {
    loading.value = false
  }
}

// 上传Logo前的验证
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

const handleUploadRequest = async (options: any) => {
  const { file } = options

  const url = await uploadStore.uploadImage(file, {
    onSuccess: (url) => {
      basicForm.systemLogo = url
    },
    onError: (error) => {
      console.error('Logo上传失败:', error)
    },
  })
}

// 移除Logo
const removeLogo = () => {
  ElMessageBox.confirm('确定要移除Logo吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      basicForm.systemLogo = ''
      ElMessage.success('Logo已移除')
    })
    .catch(() => {})
}

// 保存设置
const handleSave = async () => {
  // 验证表单
  try {
    await basicFormRef.value?.validate()
    await businessFormRef.value?.validate()
  } catch (error) {
    ElMessage.error('请检查表单填写是否正确')
    return
  }

  // 验证营业时间
  if (businessForm.businessStartTime >= businessForm.businessEndTime) {
    ElMessage.error('营业结束时间不能早于或等于开始时间')
    return
  }

  // 验证课程容量
  if (businessForm.minClassSize > businessForm.maxClassCapacity) {
    ElMessage.error('最低开课人数不能大于最大课程容量')
    return
  }

  saving.value = true

  try {
    await systemConfigStore.updateConfig(
      {
        systemName: basicForm.systemName,
        systemLogo: basicForm.systemLogo || undefined,
      },
      {
        businessStartTime: businessForm.businessStartTime,
        businessEndTime: businessForm.businessEndTime,
        courseAdvanceBookingHours: businessForm.courseAdvanceBookingHours,
        courseCancelHours: businessForm.courseCancelHours,
        checkinStartMinutes: businessForm.checkinStartMinutes,
        checkinEndMinutes: businessForm.checkinEndMinutes,
        minClassSize: businessForm.minClassSize,
        maxClassCapacity: businessForm.maxClassCapacity,
        autoCompleteHours:businessForm.autoCompleteHours
      }
    )
    // 重新加载配置获取更新时间
    await loadConfig()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

// 重置设置
const handleReset = () => {
  ElMessageBox.confirm('确定要重置系统配置吗？所有未保存的更改将丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await systemConfigStore.resetConfig()
        await loadConfig()
        ElMessage.success('配置已重置为默认值')
      } catch (error) {
        console.error('重置失败:', error)
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadConfig()
})
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
    .loading-wrapper {
      padding: 40px 20px;
    }

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
        .form-tip {
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
          line-height: 1.5;
        }

        .logo-upload {
          display: flex;
          align-items: flex-start;
          gap: 20px;

          .logo-preview,
          .logo-placeholder {
            width: 120px;
            height: 120px;
            border: 1px solid #dcdfe6;
            border-radius: 4px;
            overflow: hidden;
            flex-shrink: 0;
          }

          .logo-preview {
            :deep(.el-image__inner) {
              width: 100%;
              height: 100%;
              object-fit: contain;
            }
          }

          .image-error {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background-color: #f5f7fa;
            color: #909399;
            font-size: 12px;

            .el-icon {
              margin-bottom: 4px;
            }
          }

          .logo-placeholder {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background-color: #f8f9fa;
            color: #909399;

            span {
              margin-top: 8px;
              font-size: 12px;
            }
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
          gap: 10px;

          .time-separator {
            color: #606266;
          }
        }
      }
    }

    .update-time {
      text-align: right;
      font-size: 12px;
      color: #909399;
      margin-bottom: 20px;
      padding-top: 10px;
      border-top: 1px dashed #e4e7ed;
    }

    .settings-actions {
      margin-top: 20px;
      text-align: center;
    }
  }
}

:deep(.el-input-number .el-input__wrapper) {
  width: 100%;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-form-item__content) {
  line-height: 1;
  display: block;
}
</style>