<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :before-close="handleClose"
    destroy-on-close
  >
    <MemberForm
      ref="formRef"
      :form-data="formData"
      :is-edit="isEdit"
      @submit="handleSubmit"
      @cancel="handleClose"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import MemberForm from './MemberForm.vue'
import type { MemberFormData } from '@/types'

interface Props {
  modelValue?: boolean
  formData?: MemberFormData
  isEdit?: boolean
  title?: string
  width?: string
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit', data: MemberFormData): void
  (e: 'close'): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  formData: () => ({
    name: '',
    gender: 'MALE',
    phone: '',
    email: '',
    birthDate: '',
    address: '',
    height: 170,
    weight: 60,
    healthStatus: 'NORMAL',
    memberLevel: 'SILVER',
    status: 'ACTIVE',
    joinDate: '',
    expireDate: '',
    remainingSessions: 10,
    emergencyContact: {
      name: '',
      relationship: '',
      phone: ''
    }
  }),
  isEdit: false,
  title: '会员信息',
  width: '800px'
})

const emit = defineEmits<Emits>()

const visible = ref(props.modelValue)
const formRef = ref<FormInstance>()

// 计算标题
const title = computed(() => {
  return props.isEdit ? '编辑会员' : '新增会员'
})

// 处理关闭
const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
  emit('close')
}

// 处理提交
const handleSubmit = (data: MemberFormData) => {
  emit('submit', data)
  handleClose()
}

// 打开对话框
const open = (data?: MemberFormData) => {
  if (data) {
    props.formData = { ...props.formData, ...data }
  }
  visible.value = true
  emit('update:modelValue', true)
  
  nextTick(() => {
    formRef.value?.clearValidate?.()
  })
}

// 关闭对话框
const close = () => {
  handleClose()
}

// 暴露方法
defineExpose({
  open,
  close
})

// 监听外部变化
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
})

watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})
</script>