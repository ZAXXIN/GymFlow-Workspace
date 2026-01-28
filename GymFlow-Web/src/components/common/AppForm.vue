<template>
  <div class="app-form" :class="{ 'is-dialog': isDialog }">
    <!-- 表单头部 -->
    <div v-if="showHeader" class="form-header">
      <div class="header-left">
        <h3 class="form-title">{{ title }}</h3>
        <div v-if="description" class="form-description">{{ description }}</div>
      </div>
      <div v-if="$slots.header" class="header-right">
        <slot name="header" />
      </div>
    </div>

    <!-- 表单主体 -->
    <el-form
      ref="formRef"
      v-bind="$attrs"
      :model="formModel"
      :rules="formRules"
      :label-width="labelWidth"
      :label-position="labelPosition"
      :size="size"
      :disabled="disabled"
      :inline="inline"
      :hide-required-asterisk="hideRequiredAsterisk"
      :show-message="showMessage"
      :inline-message="inlineMessage"
      :status-icon="statusIcon"
      :validate-on-rule-change="validateOnRuleChange"
      :scroll-to-error="scrollToError"
      :scroll-into-view-options="scrollIntoViewOptions"
      @validate="handleValidate"
    >
      <el-row :gutter="rowGutter">
        <!-- 表单字段 -->
        <template v-for="(item, index) in formItems" :key="item.prop || index">
          <!-- 自定义插槽 -->
          <slot v-if="item.slot" :name="item.slot" :form="formModel" :item="item" />
          
          <!-- 自定义渲染 -->
          <template v-else-if="item.render">
            <component
              :is="item.render"
              :form="formModel"
              :item="item"
              :index="index"
            />
          </template>
          
          <!-- 默认表单字段 -->
          <template v-else-if="!item.hidden">
            <el-col
              v-bind="getColProps(item)"
              :class="{ 'full-width': item.span === 24 }"
            >
              <el-form-item
                :label="item.label"
                :prop="item.prop"
                :rules="item.rules"
                :required="item.required"
                :label-width="item.labelWidth"
                :error="item.error"
                :show-message="item.showMessage"
                :inline-message="item.inlineMessage"
                :size="item.size || size"
                :class="item.className"
                :style="item.style"
              >
                <!-- 字段前缀 -->
                <template v-if="item.prefix" #prefix>
                  <span class="field-prefix">{{ item.prefix }}</span>
                </template>
                
                <!-- 字段内容 -->
                <component
                  :is="getFieldComponent(item)"
                  v-model="formModel[item.prop]"
                  v-bind="getFieldProps(item)"
                  :placeholder="getPlaceholder(item)"
                  :disabled="getDisabled(item)"
                  :clearable="getClearable(item)"
                  :size="item.size || size"
                  @change="handleFieldChange(item, $event)"
                  @input="handleFieldInput(item, $event)"
                  @blur="handleFieldBlur(item, $event)"
                  @focus="handleFieldFocus(item, $event)"
                >
                  <!-- 选择器选项 -->
                  <template v-if="item.type === 'select' || item.type === 'radio-group' || item.type === 'checkbox-group'">
                    <template v-if="item.options && item.options.length > 0">
                      <component
                        :is="getOptionComponent(item.type)"
                        v-for="option in item.options"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value"
                        :disabled="option.disabled"
                      >
                        {{ option.label }}
                      </component>
                    </template>
                    <slot v-else :name="`${item.prop}-options`" />
                  </template>
                  
                  <!-- 文本域 -->
                  <template v-if="item.type === 'textarea'">
                    <slot v-if="$slots[`${item.prop}-textarea`]" :name="`${item.prop}-textarea`" />
                  </template>
                  
                  <!-- 上传组件 -->
                  <template v-if="item.type === 'upload'">
                    <slot v-if="$slots[`${item.prop}-upload`]" :name="`${item.prop}-upload`" />
                  </template>
                  
                  <!-- 自定义插槽 -->
                  <slot v-if="item.childrenSlot" :name="item.childrenSlot" :item="item" />
                </component>
                
                <!-- 字段后缀 -->
                <template v-if="item.suffix" #suffix>
                  <span class="field-suffix">{{ item.suffix }}</span>
                </template>
                
                <!-- 字段说明 -->
                <template v-if="item.tip" #label>
                  <span class="field-label">
                    {{ item.label }}
                    <el-tooltip :content="item.tip" placement="top">
                      <el-icon class="tip-icon"><QuestionFilled /></el-icon>
                    </el-tooltip>
                  </span>
                </template>
                
                <!-- 字段额外内容 -->
                <template v-if="item.extra" #extra>
                  <div class="field-extra">{{ item.extra }}</div>
                </template>
              </el-form-item>
            </el-col>
          </template>
        </template>
      </el-row>
    </el-form>

    <!-- 表单底部 -->
    <div v-if="showFooter" class="form-footer">
      <div class="footer-left">
        <slot name="footer-left" />
      </div>
      <div class="footer-right">
        <slot name="footer">
          <div class="default-footer">
            <el-button
              v-if="showCancel"
              :size="size"
              :disabled="disabled"
              @click="handleCancel"
            >
              {{ cancelText }}
            </el-button>
            <el-button
              v-if="showReset"
              :size="size"
              :disabled="disabled"
              @click="handleReset"
            >
              {{ resetText }}
            </el-button>
            <el-button
              v-if="showSubmit"
              type="primary"
              :size="size"
              :loading="submitting"
              :disabled="disabled"
              @click="handleSubmit"
            >
              {{ submitText }}
            </el-button>
          </div>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'

interface FormItem {
  // 基础属性
  prop: string
  label: string
  type?: string
  required?: boolean
  hidden?: boolean
  // 布局属性
  span?: number
  offset?: number
  pull?: number
  push?: number
  xs?: number | object
  sm?: number | object
  md?: number | object
  lg?: number | object
  xl?: number | object
  // 字段配置
  defaultValue?: any
  placeholder?: string
  disabled?: boolean
  clearable?: boolean
  options?: Array<{ label: string; value: any; disabled?: boolean }>
  // 验证规则
  rules?: any[] | any
  // 样式配置
  labelWidth?: string
  className?: string
  style?: object
  // 提示信息
  tip?: string
  extra?: string
  prefix?: string
  suffix?: string
  // 自定义
  slot?: string
  render?: any
  childrenSlot?: string
  // 组件特定属性
  [key: string]: any
}

interface FormProps {
  modelValue?: Record<string, any>
  items?: FormItem[]
  rules?: FormRules
  title?: string
  description?: string
  // 布局配置
  labelWidth?: string
  labelPosition?: 'left' | 'right' | 'top'
  rowGutter?: number
  size?: 'large' | 'default' | 'small'
  // 功能控制
  showHeader?: boolean
  showFooter?: boolean
  showSubmit?: boolean
  showReset?: boolean
  showCancel?: boolean
  submitText?: string
  resetText?: string
  cancelText?: string
  // 表单配置
  disabled?: boolean
  inline?: boolean
  hideRequiredAsterisk?: boolean
  showMessage?: boolean
  inlineMessage?: boolean
  statusIcon?: boolean
  validateOnRuleChange?: boolean
  scrollToError?: boolean
  scrollIntoViewOptions?: ScrollIntoViewOptions
  // 状态
  submitting?: boolean
  isDialog?: boolean
}

const props = withDefaults(defineProps<FormProps>(), {
  modelValue: () => ({}),
  items: () => [],
  rules: () => ({}),
  title: '',
  description: '',
  labelWidth: '120px',
  labelPosition: 'right',
  rowGutter: 24,
  size: 'default',
  showHeader: true,
  showFooter: true,
  showSubmit: true,
  showReset: true,
  showCancel: true,
  submitText: '提交',
  resetText: '重置',
  cancelText: '取消',
  disabled: false,
  inline: false,
  hideRequiredAsterisk: false,
  showMessage: true,
  inlineMessage: false,
  statusIcon: false,
  validateOnRuleChange: true,
  scrollToError: true,
  submitting: false,
  isDialog: false
})

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any>]
  'submit': [formData: Record<string, any>]
  'reset': []
  'cancel': []
  'validate': [prop: string, isValid: boolean, message: string]
  'field-change': [prop: string, value: any, item: FormItem]
  'field-input': [prop: string, value: any, item: FormItem]
  'field-blur': [prop: string, value: any, item: FormItem]
  'field-focus': [prop: string, value: any, item: FormItem]
}>()

const formRef = ref<InstanceType<typeof ElForm>>()

// 表单数据
const formModel = reactive({ ...props.modelValue })

// 表单验证规则
const formRules = computed(() => {
  const rules: FormRules = { ...props.rules }
  
  // 自动添加required规则
  props.items.forEach(item => {
    if (item.required && item.prop) {
      if (!rules[item.prop]) {
        rules[item.prop] = []
      }
      
      if (Array.isArray(rules[item.prop])) {
        const hasRequired = rules[item.prop].some((rule: any) => rule.required)
        if (!hasRequired) {
          rules[item.prop].unshift({
            required: true,
            message: `${item.label}不能为空`,
            trigger: item.type === 'select' ? 'change' : 'blur'
          })
        }
      }
    }
  })
  
  return rules
})

// 表单字段配置
const formItems = computed(() => {
  return props.items.filter(item => !item.hidden)
})

// 获取列布局属性
const getColProps = (item: FormItem) => {
  const { span, offset, pull, push, xs, sm, md, lg, xl } = item
  return {
    span: span || 24,
    offset,
    pull,
    push,
    xs,
    sm,
    md,
    lg,
    xl
  }
}

// 获取字段组件
const getFieldComponent = (item: FormItem) => {
  const componentMap: Record<string, string> = {
    'input': 'ElInput',
    'textarea': 'ElInput',
    'number': 'ElInputNumber',
    'select': 'ElSelect',
    'radio': 'ElRadioGroup',
    'radio-group': 'ElRadioGroup',
    'checkbox': 'ElCheckboxGroup',
    'checkbox-group': 'ElCheckboxGroup',
    'switch': 'ElSwitch',
    'slider': 'ElSlider',
    'time': 'ElTimePicker',
    'time-picker': 'ElTimePicker',
    'date': 'ElDatePicker',
    'date-picker': 'ElDatePicker',
    'daterange': 'ElDatePicker',
    'datetime': 'ElDatePicker',
    'datetime-picker': 'ElDatePicker',
    'cascader': 'ElCascader',
    'transfer': 'ElTransfer',
    'rate': 'ElRate',
    'color': 'ElColorPicker',
    'upload': 'ElUpload',
    'editor': 'Editor',
    'custom': 'div'
  }
  
  if (item.component) {
    return item.component
  }
  
  return componentMap[item.type || 'input'] || 'ElInput'
}

// 获取字段属性
const getFieldProps = (item: FormItem) => {
  const baseProps: Record<string, any> = {}
  
  // 根据类型设置默认属性
  switch (item.type) {
    case 'textarea':
      baseProps.type = 'textarea'
      baseProps.rows = item.rows || 3
      baseProps.resize = item.resize || 'none'
      break
    case 'number':
      baseProps.type = 'number'
      baseProps.min = item.min
      baseProps.max = item.max
      baseProps.step = item.step
      baseProps.precision = item.precision
      break
    case 'daterange':
      baseProps.type = 'daterange'
      baseProps['range-separator'] = item.rangeSeparator || '至'
      baseProps['start-placeholder'] = item.startPlaceholder || '开始日期'
      baseProps['end-placeholder'] = item.endPlaceholder || '结束日期'
      baseProps['value-format'] = item.valueFormat || 'YYYY-MM-DD'
      break
    case 'datetime':
      baseProps.type = 'datetime'
      baseProps['value-format'] = item.valueFormat || 'YYYY-MM-DD HH:mm:ss'
      break
    case 'select':
      baseProps.filterable = item.filterable ?? true
      baseProps.allowCreate = item.allowCreate
      baseProps.multiple = item.multiple
      baseProps.collapseTags = item.collapseTags
      break
    case 'upload':
      baseProps.action = item.action
      baseProps.listType = item.listType || 'text'
      baseProps.multiple = item.multiple
      baseProps.accept = item.accept
      baseProps['on-success'] = item.onSuccess
      baseProps['on-error'] = item.onError
      baseProps['on-remove'] = item.onRemove
      baseProps['on-exceed'] = item.onExceed
      break
  }
  
  // 合并自定义属性
  return { ...baseProps, ...item.props }
}

// 获取占位符
const getPlaceholder = (item: FormItem) => {
  if (item.placeholder) return item.placeholder
  
  const placeholders: Record<string, string> = {
    'input': `请输入${item.label}`,
    'textarea': `请输入${item.label}`,
    'select': `请选择${item.label}`,
    'date': `请选择${item.label}`,
    'time': `请选择${item.label}`,
    'daterange': `请选择${item.label}范围`
  }
  
  return placeholders[item.type || 'input'] || `请输入${item.label}`
}

// 获取禁用状态
const getDisabled = (item: FormItem) => {
  return item.disabled ?? props.disabled
}

// 获取可清除状态
const getClearable = (item: FormItem) => {
  return item.clearable ?? true
}

// 获取选项组件
const getOptionComponent = (type: string) => {
  if (type === 'select') return 'ElOption'
  if (type === 'radio-group') return 'ElRadio'
  if (type === 'checkbox-group') return 'ElCheckbox'
  return 'ElOption'
}

// 处理字段变化
const handleFieldChange = (item: FormItem, value: any) => {
  emit('field-change', item.prop, value, item)
}

// 处理字段输入
const handleFieldInput = (item: FormItem, value: any) => {
  emit('field-input', item.prop, value, item)
}

// 处理字段失去焦点
const handleFieldBlur = (item: FormItem, event: Event) => {
  emit('field-blur', item.prop, formModel[item.prop], item)
}

// 处理字段获取焦点
const handleFieldFocus = (item: FormItem, event: Event) => {
  emit('field-focus', item.prop, formModel[item.prop], item)
}

// 处理验证
const handleValidate = (prop: string, isValid: boolean, message: string) => {
  emit('validate', prop, isValid, message)
}

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (valid) {
      emit('submit', { ...formModel })
    }
  } catch (error) {
    console.error('表单验证失败:', error)
    ElMessage.error('请检查表单填写是否正确')
  }
}

// 处理重置
const handleReset = () => {
  if (!formRef.value) return
  
  formRef.value.resetFields()
  
  // 重置为默认值
  props.items.forEach(item => {
    if (item.defaultValue !== undefined && item.prop) {
      formModel[item.prop] = item.defaultValue
    }
  })
  
  emit('reset')
}

// 处理取消
const handleCancel = () => {
  emit('cancel')
}

// 设置字段值
const setFieldValue = (prop: string, value: any) => {
  if (prop in formModel) {
    formModel[prop] = value
  }
}

// 获取字段值
const getFieldValue = (prop: string) => {
  return formModel[prop]
}

// 获取所有字段值
const getFormData = () => {
  return { ...formModel }
}

// 验证字段
const validateField = (props?: string | string[]) => {
  if (!formRef.value) return Promise.reject()
  return formRef.value.validateField(props)
}

// 重置字段
const resetField = (props?: string | string[]) => {
  if (!formRef.value) return
  formRef.value.resetFields(props)
}

// 清除验证
const clearValidate = (props?: string | string[]) => {
  if (!formRef.value) return
  formRef.value.clearValidate(props)
}

// 滚动到字段
const scrollToField = (prop: string) => {
  nextTick(() => {
    const field = document.querySelector(`[data-prop="${prop}"]`)
    if (field) {
      field.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  })
}

// 暴露的方法
defineExpose({
  getFormRef: () => formRef.value,
  validate: () => formRef.value?.validate(),
  resetFields: () => formRef.value?.resetFields(),
  clearValidate: () => formRef.value?.clearValidate(),
  setFieldValue,
  getFieldValue,
  getFormData,
  validateField,
  resetField,
  scrollToField,
  submit: handleSubmit,
  reset: handleReset
})

// 监听外部值变化
watch(() => props.modelValue, (value) => {
  Object.assign(formModel, value)
}, { deep: true })

// 监听内部值变化
watch(formModel, (value) => {
  emit('update:modelValue', { ...value })
}, { deep: true })

// 设置默认值
onMounted(() => {
  props.items.forEach(item => {
    if (item.defaultValue !== undefined && item.prop && formModel[item.prop] === undefined) {
      formModel[item.prop] = item.defaultValue
    }
  })
})
</script>

<style scoped lang="scss">
.app-form {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;

  &.is-dialog {
    padding: 0;
  }

  .form-header {
    padding: 20px 24px;
    border-bottom: 1px solid #ebeef5;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      .form-title {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }

      .form-description {
        margin-top: 8px;
        font-size: 14px;
        color: #606266;
        line-height: 1.5;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 10px;
    }
  }

  :deep(.el-form) {
    padding: 24px;
    
    .el-row {
      margin-bottom: -16px;
      
      .el-col {
        margin-bottom: 16px;
        
        &.full-width {
          width: 100%;
        }
      }
    }

    .el-form-item {
      margin-bottom: 0;

      .field-label {
        display: inline-flex;
        align-items: center;
        gap: 4px;

        .tip-icon {
          color: #c0c4cc;
          cursor: help;
          font-size: 14px;
        }
      }

      .field-prefix,
      .field-suffix {
        color: #606266;
        font-size: 14px;
      }

      .field-extra {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }

  .form-footer {
    padding: 16px 24px;
    border-top: 1px solid #ebeef5;
    background: #fafafa;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .footer-left,
    .footer-right {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .default-footer {
      display: flex;
      gap: 10px;
    }
  }
}

// 对话框表单样式
:deep(.el-dialog) .app-form {
  .form-header {
    padding: 16px 20px;
  }
  
  :deep(.el-form) {
    padding: 20px;
    
    .el-row {
      margin-bottom: -12px;
      
      .el-col {
        margin-bottom: 12px;
      }
    }
  }
  
  .form-footer {
    padding: 12px 20px;
  }
}

// 响应式适配
@media (max-width: 768px) {
  .app-form {
    .form-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
      
      .header-right {
        width: 100%;
        justify-content: flex-end;
      }
    }
    
    :deep(.el-form) {
      .el-row {
        .el-col {
          span: 24 !important;
        }
      }
    }
    
    .form-footer {
      flex-direction: column;
      gap: 12px;
      
      .footer-left,
      .footer-right {
        width: 100%;
        justify-content: center;
      }
    }
  }
}
</style>