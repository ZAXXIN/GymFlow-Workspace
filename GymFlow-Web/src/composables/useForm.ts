import { ref, reactive, computed, watch, nextTick } from 'vue'
import type { Ref } from 'vue'
import { cloneDeep, isEqual } from 'lodash-es'

interface FormOptions<T = any> {
  // 表单初始数据
  initialData?: T
  
  // API函数
  fetchData?: (id: string | number) => Promise<any>
  createData?: (data: T) => Promise<any>
  updateData?: (id: string | number, data: T) => Promise<any>
  
  // 表单配置
  formRules?: FormRules
  immediate?: boolean
  autoReset?: boolean
  
  // 事件回调
  onFetchSuccess?: (data: T) => void
  onCreateSuccess?: (data: T) => void
  onUpdateSuccess?: (data: T) => void
  onCancel?: () => void
  onValidateError?: (errors: any) => void
}

/**
 * 通用表单处理组合式函数
 * @param options 表单配置选项
 * @returns 表单相关的状态和方法
 */
export function useForm<T extends Record<string, any> = any>(options: FormOptions<T> = {}) {
  // 表单引用
  const formRef = ref<FormInstance>()
  
  // 表单数据
  const formData = ref<T>({} as T) as Ref<T>
  const initialFormData = ref<T>({} as T) as Ref<T>
  
  // 表单状态
  const loading = ref(false)
  const submitting = ref(false)
  const isEditing = ref(false)
  const formChanged = ref(false)
  
  // 表单规则
  const formRules = ref<FormRules>(options.formRules || {})
  
  // 表单是否已改变
  watch(
    () => formData.value,
    (newVal) => {
      formChanged.value = !isEqual(newVal, initialFormData.value)
    },
    { deep: true }
  )
  
  // 重置表单
  const resetForm = () => {
    formData.value = cloneDeep(initialFormData.value)
    formRef.value?.clearValidate()
  }
  
  // 获取表单数据
  const fetchFormData = async (id?: string | number) => {
    if (!options.fetchData || !id) {
      // 如果没有获取数据的API，使用初始数据
      if (options.initialData) {
        formData.value = cloneDeep(options.initialData) as T
        initialFormData.value = cloneDeep(options.initialData) as T
      }
      return
    }
    
    try {
      loading.value = true
      const response = await options.fetchData(id)
      
      if (response.code === 200) {
        formData.value = response.data
        initialFormData.value = cloneDeep(response.data)
        isEditing.value = true
        
        // 回调函数
        options.onFetchSuccess?.(response.data)
      } else {
        ElMessage.error(response.message || '获取数据失败')
      }
    } catch (error) {
      console.error('获取表单数据失败:', error)
      ElMessage.error('获取数据失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }
  
  // 验证表单
  const validateForm = async (): Promise<boolean> => {
    if (!formRef.value) return true
    
    try {
      await formRef.value.validate()
      return true
    } catch (errors) {
      options.onValidateError?.(errors)
      return false
    }
  }
  
  // 提交表单
  const submitForm = async (): Promise<boolean> => {
    // 验证表单
    const isValid = await validateForm()
    if (!isValid) return false
    
    try {
      submitting.value = true
      
      if (isEditing.value && options.updateData) {
        // 更新数据
        const id = formData.value.id || (formData.value as any)._id
        const response = await options.updateData(id, formData.value)
        
        if (response.code === 200) {
          ElMessage.success('更新成功')
          initialFormData.value = cloneDeep(formData.value)
          formChanged.value = false
          
          // 回调函数
          options.onUpdateSuccess?.(response.data)
          return true
        } else {
          ElMessage.error(response.message || '更新失败')
          return false
        }
      } else if (!isEditing.value && options.createData) {
        // 创建数据
        const response = await options.createData(formData.value)
        
        if (response.code === 200) {
          ElMessage.success('创建成功')
          
          // 如果配置了自动重置，重置表单
          if (options.autoReset) {
            resetForm()
          } else {
            initialFormData.value = cloneDeep(formData.value)
            formChanged.value = false
          }
          
          // 回调函数
          options.onCreateSuccess?.(response.data)
          return true
        } else {
          ElMessage.error(response.message || '创建失败')
          return false
        }
      }
      
      ElMessage.warning('未配置创建或更新接口')
      return false
    } catch (error) {
      console.error('提交表单失败:', error)
      ElMessage.error('提交失败，请稍后重试')
      return false
    } finally {
      submitting.value = false
    }
  }
  
  // 取消表单
  const cancelForm = () => {
    if (formChanged.value) {
      ElMessageBox.confirm(
        '表单内容已修改，是否放弃修改？',
        '确认取消',
        {
          confirmButtonText: '放弃',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(() => {
          resetForm()
          options.onCancel?.()
        })
        .catch(() => {
          // 取消操作
        })
    } else {
      options.onCancel?.()
    }
  }
  
  // 设置表单字段值
  const setFieldValue = (field: keyof T, value: any) => {
    formData.value[field] = value
  }
  
  // 获取表单字段值
  const getFieldValue = (field: keyof T) => {
    return formData.value[field]
  }
  
  // 设置表单规则
  const setFormRules = (rules: FormRules) => {
    formRules.value = rules
  }
  
  // 添加表单规则
  const addFormRules = (field: string, rules: FormRules[string]) => {
    formRules.value[field] = rules
  }
  
  // 清除表单验证
  const clearValidate = (fields?: string | string[]) => {
    formRef.value?.clearValidate(fields)
  }
  
  // 重置表单验证
  const resetValidate = () => {
    formRef.value?.clearValidate()
  }
  
  // 初始化表单数据
  const initFormData = (data: T) => {
    formData.value = cloneDeep(data)
    initialFormData.value = cloneDeep(data)
    isEditing.value = false
  }
  
  // 初始化
  if (options.immediate !== false) {
    if (options.initialData) {
      formData.value = cloneDeep(options.initialData) as T
      initialFormData.value = cloneDeep(options.initialData) as T
    }
  }
  
  return {
    // 引用
    formRef,
    
    // 状态
    formData,
    initialFormData,
    loading,
    submitting,
    isEditing,
    formChanged,
    formRules,
    
    // 方法
    fetchFormData,
    validateForm,
    submitForm,
    cancelForm,
    resetForm,
    setFieldValue,
    getFieldValue,
    setFormRules,
    addFormRules,
    clearValidate,
    resetValidate,
    initFormData
  }
}