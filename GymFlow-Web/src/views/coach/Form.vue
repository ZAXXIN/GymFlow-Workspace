<template>
  <div class="coach-form-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/coach' }">教练管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ formData.id ? '编辑教练' : '新增教练' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">{{ formData.id ? '编辑教练信息' : '新增教练' }}</h1>
      </div>
      <div class="header-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="loading">
          {{ formData.id ? '更新' : '创建' }}
        </el-button>
      </div>
    </div>

    <!-- 主表单区域 -->
    <div class="form-content">
      <el-form 
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="coach-form"
      >
        <!-- 基本信息 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
            </div>
          </template>
          
          <div class="form-row">
            <el-form-item label="教练姓名" prop="name" class="form-item">
              <el-input 
                v-model="formData.name"
                placeholder="请输入教练姓名"
                maxlength="20"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="英文名" prop="englishName" class="form-item">
              <el-input 
                v-model="formData.englishName"
                placeholder="请输入英文名"
                maxlength="50"
                clearable
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="手机号" prop="phone" class="form-item">
              <el-input 
                v-model="formData.phone"
                placeholder="请输入手机号"
                maxlength="11"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="邮箱" prop="email" class="form-item">
              <el-input 
                v-model="formData.email"
                placeholder="请输入邮箱"
                clearable
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="性别" prop="gender" class="form-item">
              <el-radio-group v-model="formData.gender">
                <el-radio label="MALE">男</el-radio>
                <el-radio label="FEMALE">女</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="出生日期" prop="birthday" class="form-item">
              <el-date-picker
                v-model="formData.birthday"
                type="date"
                placeholder="选择出生日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          
          <el-form-item label="个人简介" prop="introduction" class="full-width">
            <el-input 
              v-model="formData.introduction"
              type="textarea"
              :rows="3"
              placeholder="请输入个人简介"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <div class="form-row">
            <el-form-item label="头像" prop="avatar" class="form-item">
              <el-upload
                class="avatar-uploader"
                action="/api/upload"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <img v-if="formData.avatar" :src="formData.avatar" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon">
                  <Plus />
                </el-icon>
              </el-upload>
            </el-form-item>
            
            <el-form-item label="教练状态" prop="status" class="form-item">
              <el-select v-model="formData.status" placeholder="请选择教练状态" style="width: 100%">
                <el-option 
                  v-for="item in coachStatusOptions" 
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
        </el-card>

        <!-- 专业信息 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">专业信息</span>
            </div>
          </template>
          
          <div class="form-row">
            <el-form-item label="教练等级" prop="level" class="form-item">
              <el-select v-model="formData.level" placeholder="请选择教练等级" style="width: 100%">
                <el-option label="初级教练" value="JUNIOR" />
                <el-option label="中级教练" value="INTERMEDIATE" />
                <el-option label="高级教练" value="SENIOR" />
                <el-option label="明星教练" value="STAR" />
                <el-option label="总监级教练" value="DIRECTOR" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="从业年限" prop="experienceYears" class="form-item">
              <el-input-number
                v-model="formData.experienceYears"
                :min="0"
                :max="50"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          
          <el-form-item label="专业资质" prop="certificates" class="full-width">
            <el-select
              v-model="formData.certificates"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入专业资质"
              style="width: 100%"
            >
              <el-option
                v-for="item in certificateOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="专长领域" prop="specialties" class="full-width">
            <el-checkbox-group v-model="formData.specialties" class="specialty-checkbox-group">
              <el-checkbox label="减脂塑形">减脂塑形</el-checkbox>
              <el-checkbox label="增肌增力">增肌增力</el-checkbox>
              <el-checkbox label="康复训练">康复训练</el-checkbox>
              <el-checkbox label="体能训练">体能训练</el-checkbox>
              <el-checkbox label="瑜伽普拉提">瑜伽普拉提</el-checkbox>
              <el-checkbox label="运动营养">运动营养</el-checkbox>
              <el-checkbox label="产后恢复">产后恢复</el-checkbox>
              <el-checkbox label="青少年体能">青少年体能</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          
          <el-form-item label="教学理念" prop="teachingPhilosophy" class="full-width">
            <el-input 
              v-model="formData.teachingPhilosophy"
              type="textarea"
              :rows="3"
              placeholder="请输入教学理念"
              maxlength="300"
              show-word-limit
            />
          </el-form-item>
        </el-card>

        <!-- 薪酬与排班 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">薪酬与排班</span>
            </div>
          </template>
          
          <div class="form-row">
            <el-form-item label="时薪(元)" prop="hourlyRate" class="form-item">
              <el-input-number
                v-model="formData.hourlyRate"
                :min="50"
                :max="1000"
                :step="10"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="佣金比例(%)" prop="commissionRate" class="form-item">
              <el-input-number
                v-model="formData.commissionRate"
                :min="0"
                :max="50"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          
          <el-form-item label="可排班时间" prop="availableSchedule" class="full-width">
            <div class="schedule-table">
              <div class="schedule-header">
                <div class="time-slot">时间</div>
                <div class="weekdays">
                  <span v-for="day in weekDays" :key="day" class="weekday">{{ day }}</span>
                </div>
              </div>
              <div class="schedule-body">
                <div v-for="timeSlot in timeSlots" :key="timeSlot.value" class="schedule-row">
                  <div class="time-label">{{ timeSlot.label }}</div>
                  <div class="day-cells">
                    <el-checkbox 
                      v-for="day in 7" 
                      :key="day" 
                      v-model="formData.schedule[timeSlot.value][day-1]"
                      class="schedule-checkbox"
                    />
                  </div>
                </div>
              </div>
            </div>
          </el-form-item>
          
          <el-form-item label="最大学员数" prop="maxStudents" class="full-width">
            <el-input-number
              v-model="formData.maxStudents"
              :min="1"
              :max="50"
              :step="1"
              controls-position="right"
            />
            <span class="form-tip">同一时间段最多可带学员数</span>
          </el-form-item>
        </el-card>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { CoachFormData } from '@/types'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const loading = ref(false)

// 表单数据
const formData = reactive<CoachFormData>({
  id: undefined,
  name: '',
  englishName: '',
  phone: '',
  email: '',
  gender: 'MALE',
  birthday: '',
  introduction: '',
  avatar: '',
  status: 'ACTIVE',
  level: 'INTERMEDIATE',
  experienceYears: 3,
  certificates: [],
  specialties: ['减脂塑形', '增肌增力'],
  teachingPhilosophy: '',
  hourlyRate: 200,
  commissionRate: 15,
  maxStudents: 10,
  schedule: {
    morning: [true, true, true, true, true, false, false],
    afternoon: [true, true, true, true, true, false, false],
    evening: [true, true, true, true, true, true, false]
  }
})

// 教练状态选项
const coachStatusOptions = [
  { label: '在职', value: 'ACTIVE' },
  { label: '休假', value: 'ON_LEAVE' },
  { label: '离职', value: 'RESIGNED' }
]

// 资质证书选项
const certificateOptions = [
  { label: '国家健身教练职业资格证', value: 'NATIONAL_CERT' },
  { label: 'ACE美国运动委员会认证', value: 'ACE' },
  { label: 'NASM美国国家运动医学学院', value: 'NASM' },
  { label: 'NSCA美国体能协会认证', value: 'NSCA' },
  { label: 'CrossFit Level 1认证', value: 'CROSSFIT' },
  { label: '瑜伽教练认证', value: 'YOGA' },
  { label: '普拉提认证教练', value: 'PILATES' },
  { label: '运动营养师认证', value: 'NUTRITION' }
]

// 排班相关
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const timeSlots = [
  { label: '上午 (9:00-12:00)', value: 'morning' },
  { label: '下午 (14:00-18:00)', value: 'afternoon' },
  { label: '晚上 (19:00-22:00)', value: 'evening' }
]

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入教练姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入正确的邮箱', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择教练状态', trigger: 'change' }
  ],
  level: [
    { required: true, message: '请选择教练等级', trigger: 'change' }
  ],
  hourlyRate: [
    { required: true, message: '请输入时薪', trigger: 'blur' }
  ]
}

// 头像上传处理
const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
  formData.avatar = response.data.url
  ElMessage.success('头像上传成功')
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('头像图片必须是 JPG 或 PNG 格式!')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 保存表单
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    // 这里调用API保存数据
    // await saveCoach(formData)
    
    ElMessage.success(formData.id ? '教练信息更新成功' : '教练创建成功')
    router.push('/coach')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请检查表单')
  } finally {
    loading.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  router.push('/coach')
}

// 初始化数据（编辑模式）
const initFormData = () => {
  const id = route.params.id
  if (id) {
    // 编辑模式：从API获取数据
    // const data = await getCoachDetail(id)
    // Object.assign(formData, data)
    
    // 模拟数据
    Object.assign(formData, {
      id: id,
      name: '李教练',
      englishName: 'Coach Li',
      phone: '13900139000',
      email: 'coachli@example.com',
      gender: 'MALE',
      birthday: '1985-05-15',
      introduction: '拥有10年健身教练经验，擅长减脂塑形和力量训练',
      avatar: 'https://via.placeholder.com/150',
      status: 'ACTIVE',
      level: 'SENIOR',
      experienceYears: 10,
      certificates: ['NATIONAL_CERT', 'ACE', 'NUTRITION'],
      specialties: ['减脂塑形', '增肌增力', '运动营养'],
      teachingPhilosophy: '科学训练，健康生活',
      hourlyRate: 300,
      commissionRate: 20,
      maxStudents: 15
    })
  }
}

onMounted(() => {
  initFormData()
})
</script>

<style scoped lang="scss">
.coach-form-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  .header-left {
    .page-title {
      margin: 10px 0 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
  }

  .header-actions {
    display: flex;
    gap: 10px;
  }
}

.form-content {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.form-section {
  margin-bottom: 20px;
  border: 1px solid #ebeef5;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;

  .form-item {
    flex: 1;
    margin-bottom: 0;
  }
}

.full-width {
  width: 100%;
  margin-bottom: 20px;
}

.specialty-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;

  :deep(.el-checkbox) {
    margin-right: 0;
    width: 120px;
  }
}

.schedule-table {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.schedule-header {
  display: flex;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;

  .time-slot {
    width: 120px;
    padding: 12px;
    text-align: center;
    font-weight: 500;
    color: #606266;
  }

  .weekdays {
    flex: 1;
    display: flex;

    .weekday {
      flex: 1;
      padding: 12px;
      text-align: center;
      font-weight: 500;
      color: #606266;
      border-left: 1px solid #ebeef5;
    }
  }
}

.schedule-body {
  .schedule-row {
    display: flex;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }

    .time-label {
      width: 120px;
      padding: 12px;
      text-align: center;
      color: #606266;
      background: #fafafa;
    }

    .day-cells {
      flex: 1;
      display: flex;

      .schedule-checkbox {
        flex: 1;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 12px;
        border-left: 1px solid #ebeef5;

        :deep(.el-checkbox__label) {
          display: none;
        }
      }
    }
  }
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }

  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 100px;
    text-align: center;
    line-height: 100px;
  }

  .avatar {
    width: 100px;
    height: 100px;
    display: block;
    object-fit: cover;
  }
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 20px;
}
</style>