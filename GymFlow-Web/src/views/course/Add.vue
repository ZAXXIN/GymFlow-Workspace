<template>
  <div class="course-form-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/course' }">课程管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ isEditMode ? '编辑课程' : '新增课程' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">{{ isEditMode ? '编辑课程信息' : '新增课程' }}</h1>
      </div>
      <div class="header-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEditMode ? '更新' : '创建' }}
        </el-button>
      </div>
    </div>

    <!-- 主表单区域 -->
    <div class="form-content">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" class="course-form">
        <!-- 基本信息 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
            </div>
          </template>

          <div class="form-row">
            <el-form-item label="课程名称" prop="courseName" class="form-item">
              <el-input v-model="formData.courseName" placeholder="请输入课程名称" maxlength="100" clearable />
            </el-form-item>

            <el-form-item label="课程类型" prop="courseType" class="form-item">
              <el-select v-model="formData.courseType" placeholder="请选择课程类型" style="width: 100%">
                <el-option label="私教课" :value="0" />
                <el-option label="团课" :value="1" />
              </el-select>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="绑定教练" prop="coachIds" class="full-width">
              <el-select v-model="formData.coachIds" multiple filterable placeholder="请选择绑定的教练" style="width: 100%">
                <el-option v-for="coach in coachOptions" :key="coach.id" :label="coach.realName" :value="coach.id" />
              </el-select>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="课时长" prop="duration" class="form-item">
              <el-input-number v-model="formData.duration" :min="1" :max="240" :step="5" controls-position="right" style="width: 100%" placeholder="请输入课时长">
                <template #append>分钟</template>
              </el-input-number>
            </el-form-item>

            <el-form-item label="课时消耗" prop="sessionCost" class="form-item">
              <el-input-number v-model="formData.sessionCost" :min="1" :max="10" :step="1" controls-position="right" style="width: 100%" placeholder="请输入课时消耗">
                <template #append>课时</template>
              </el-input-number>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="课程描述" prop="description" class="full-width">
              <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入课程描述" maxlength="500" show-word-limit />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="课程须知" prop="notice" class="full-width">
              <el-input v-model="formData.notice" type="textarea" :rows="4" placeholder="请输入课程须知" maxlength="1000" show-word-limit />
            </el-form-item>
          </div>
        </el-card>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useCourseStore } from '@/stores/course'
import type { CourseBasicDTO } from '@/types/course'
import { coachApi } from '@/api/coach'

const router = useRouter()
const route = useRoute()
const courseStore = useCourseStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEditMode = computed(() => !!route.params.id)

// 表单数据
const formData = reactive<CourseBasicDTO>({
  courseType: 1,
  courseName: '',
  description: '',
  coachIds: [],
  duration: 60,
  sessionCost: 1,
  notice: '',
})

// 教练选项
const coachOptions = ref<any[]>([])

// 表单验证规则
const formRules: FormRules = {
  courseType: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 100, message: '课程名称长度在2-100个字符', trigger: 'blur' },
  ],
  coachIds: [
    { required: true, message: '请至少选择一个教练', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && value.length === 0) {
          callback(new Error('请至少选择一个教练'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  duration: [
    { required: true, message: '请输入课时长', trigger: 'blur' },
    { type: 'number', min: 1, message: '课时长不能小于1分钟', trigger: 'blur' },
  ],
  sessionCost: [
    { required: true, message: '请输入课时消耗', trigger: 'blur' },
    { type: 'number', min: 1, message: '课时消耗不能小于1', trigger: 'blur' }
  ]
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 表单验证
    await formRef.value.validate()

    loading.value = true

    if (isEditMode.value) {
      // 更新课程
      await courseStore.updateCourse(Number(route.params.id), formData)
      ElMessage.success('课程信息更新成功')
    } else {
      // 新增课程
      await courseStore.addCourse(formData)
      ElMessage.success('课程创建成功')
    }

    router.push('/course/list')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请检查表单')
  } finally {
    loading.value = false
  }
}

// 取消
const handleCancel = () => {
  router.push('/course/list')
}

// 加载教练列表
const loadCoachOptions = async () => {
  const response = await coachApi.getCoachList({})
  try {
    if (response.code === 200) {
      coachOptions.value = response.data.list.map((item) => ({
        id: item.id,
        realName: item.realName,
      }))
    }
  } catch (error) {
    console.error('加载教练列表失败:', error)
  }
}

// 初始化表单数据（编辑模式）
const initFormData = async () => {
  if (!isEditMode.value) return
  try {
    loading.value = true
    const courseId = Number(route.params.id)
    await courseStore.fetchCourseDetail(courseId)

    if (courseStore.currentCourse) {
      const course = courseStore.currentCourse
      formData.courseType = course.courseType
      formData.courseName = course.courseName
      formData.description = course.description || ''
      formData.coachIds = course.coaches.map((coach) => coach.id)
      formData.duration = course.duration
      formData.sessionCost = course.sessionCost
      formData.notice = course.notice || ''
    }
  } catch (error) {
    console.error('加载课程详情失败:', error)
    ElMessage.error('加载课程信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCoachOptions()
  initFormData()
})
</script>

<style scoped>
.course-form-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left .page-title {
  margin: 10px 0 0 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  margin-top: 10px;
}

.form-content {
  background-color: white;
  border-radius: 4px;
  padding: 20px;
}

.form-section {
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
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

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.form-item {
  flex: 1;
  margin-bottom: 0;
}

.full-width {
  width: 100%;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  background-color: #fafafa;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}
</style>