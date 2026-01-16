<template>
  <div class="app-search" :class="{ expanded: isExpanded }">
    <!-- 基础搜索框 -->
    <div class="search-input-wrapper">
      <el-input
        ref="searchInputRef"
        v-model="keyword"
        :placeholder="placeholder"
        :size="size"
        :clearable="clearable"
        :disabled="disabled"
        :maxlength="maxlength"
        @input="handleInput"
        @clear="handleClear"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon class="search-icon">
            <Search />
          </el-icon>
        </template>
        
        <template #suffix>
          <el-icon
            v-if="showAdvanced && !isExpanded"
            class="advanced-icon"
            @click="toggleAdvanced"
          >
            <Setting />
          </el-icon>
          <el-icon
            v-else-if="showAdvanced && isExpanded"
            class="advanced-icon active"
            @click="toggleAdvanced"
          >
            <Close />
          </el-icon>
        </template>
      </el-input>
      
      <el-button
        v-if="showSearchButton"
        type="primary"
        :size="size"
        :loading="loading"
        :disabled="disabled"
        @click="handleSearch"
        class="search-button"
      >
        <template #icon>
          <el-icon><Search /></el-icon>
        </template>
        {{ searchButtonText }}
      </el-button>
    </div>

    <!-- 高级搜索面板 -->
    <transition name="slide-down">
      <div v-if="showAdvanced && isExpanded" class="advanced-panel">
        <div class="panel-header">
          <span class="panel-title">高级筛选</span>
          <div class="panel-actions">
            <el-button
              v-if="showReset"
              size="small"
              :icon="Refresh"
              @click="handleReset"
            >
              重置
            </el-button>
            <el-button
              v-if="showClose"
              size="small"
              :icon="Close"
              @click="toggleAdvanced"
            >
              收起
            </el-button>
          </div>
        </div>
        
        <div class="panel-content">
          <slot name="advanced">
            <!-- 默认高级搜索表单 -->
            <el-form
              ref="advancedFormRef"
              :model="advancedForm"
              :label-width="advancedLabelWidth"
              :size="size"
              :inline="true"
              class="advanced-form"
            >
              <slot name="advanced-fields">
                <!-- 默认字段 -->
                <el-form-item
                  v-for="field in advancedFields"
                  :key="field.prop"
                  :label="field.label"
                  :prop="field.prop"
                  class="advanced-field"
                >
                  <component
                    :is="getFieldComponent(field)"
                    v-bind="getFieldProps(field)"
                    v-model="advancedForm[field.prop]"
                    :placeholder="field.placeholder || `请输入${field.label}`"
                  />
                </el-form-item>
              </slot>
            </el-form>
          </slot>
        </div>
        
        <div class="panel-footer">
          <el-button
            type="primary"
            :size="size"
            :loading="loading"
            @click="handleAdvancedSearch"
            class="advanced-search-button"
          >
            <el-icon><Search /></el-icon>
            高级搜索
          </el-button>
        </div>
      </div>
    </transition>

    <!-- 搜索历史 -->
    <transition name="slide-down">
      <div
        v-if="showHistory && historyVisible && history.length > 0"
        class="history-panel"
        v-click-outside="closeHistory"
      >
        <div class="history-header">
          <span class="history-title">搜索历史</span>
          <el-button
            type="text"
            size="small"
            @click="clearHistory"
            class="clear-history-btn"
          >
            清空
          </el-button>
        </div>
        <div class="history-list">
          <div
            v-for="item in history"
            :key="item.id || item"
            class="history-item"
            @click="selectHistory(item)"
          >
            <span class="history-text">{{ getHistoryText(item) }}</span>
            <el-icon class="history-delete" @click.stop="deleteHistory(item)">
              <Close />
            </el-icon>
          </div>
        </div>
      </div>
    </transition>

    <!-- 搜索结果 -->
    <slot name="results" :results="searchResults" :loading="loading" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElInput, ElForm, ElMessage } from 'element-plus'
import { Search, Setting, Close, Refresh } from '@element-plus/icons-vue'
import vClickOutside from 'click-outside-vue3'

interface SearchField {
  prop: string
  label: string
  type?: 'input' | 'select' | 'date' | 'daterange' | 'number' | 'radio' | 'checkbox'
  placeholder?: string
  options?: Array<{ label: string; value: any }>
  component?: any
  props?: Record<string, any>
  [key: string]: any
}

interface HistoryItem {
  id?: string | number
  keyword: string
  timestamp?: number
  [key: string]: any
}

const props = withDefaults(defineProps<{
  // 基础配置
  modelValue?: string
  placeholder?: string
  size?: 'large' | 'default' | 'small'
  clearable?: boolean
  disabled?: boolean
  maxlength?: number
  // 按钮配置
  showSearchButton?: boolean
  searchButtonText?: string
  // 高级搜索配置
  showAdvanced?: boolean
  advancedFields?: SearchField[]
  advancedModelValue?: Record<string, any>
  advancedLabelWidth?: string
  showReset?: boolean
  showClose?: boolean
  // 历史记录配置
  showHistory?: boolean
  historyKey?: string
  maxHistory?: number
  // 状态
  loading?: boolean
}>(), {
  modelValue: '',
  placeholder: '请输入搜索关键词',
  size: 'default',
  clearable: true,
  disabled: false,
  maxlength: 100,
  showSearchButton: true,
  searchButtonText: '搜索',
  showAdvanced: true,
  advancedFields: () => [],
  advancedModelValue: () => ({}),
  advancedLabelWidth: '80px',
  showReset: true,
  showClose: true,
  showHistory: true,
  historyKey: 'app_search_history',
  maxHistory: 10,
  loading: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'update:advancedModelValue': [value: Record<string, any>]
  'search': [keyword: string, advancedParams?: Record<string, any>]
  'clear': []
  'input': [value: string]
  'advanced-change': [value: Record<string, any>]
  'history-select': [item: HistoryItem]
}>()

const searchInputRef = ref<InstanceType<typeof ElInput>>()
const advancedFormRef = ref<InstanceType<typeof ElForm>>()

// 搜索关键词
const keyword = ref(props.modelValue)

// 高级搜索表单
const advancedForm = reactive({ ...props.advancedModelValue })

// 状态控制
const isExpanded = ref(false)
const historyVisible = ref(false)
const searchResults = ref<any[]>([])

// 搜索历史
const history = ref<HistoryItem[]>([])

// 初始化历史记录
const initHistory = () => {
  if (!props.showHistory) return
  
  try {
    const saved = localStorage.getItem(props.historyKey)
    if (saved) {
      history.value = JSON.parse(saved).slice(0, props.maxHistory)
    }
  } catch (error) {
    console.error('读取搜索历史失败:', error)
  }
}

// 保存历史记录
const saveHistory = () => {
  if (!props.showHistory || !keyword.value.trim()) return
  
  const newItem: HistoryItem = {
    keyword: keyword.value.trim(),
    timestamp: Date.now()
  }
  
  // 移除重复项
  const filtered = history.value.filter(item => item.keyword !== newItem.keyword)
  filtered.unshift(newItem)
  
  // 限制数量
  if (filtered.length > props.maxHistory) {
    filtered.splice(props.maxHistory)
  }
  
  history.value = filtered
  
  try {
    localStorage.setItem(props.historyKey, JSON.stringify(filtered))
  } catch (error) {
    console.error('保存搜索历史失败:', error)
  }
}

// 获取字段组件
const getFieldComponent = (field: SearchField) => {
  if (field.component) return field.component
  
  const componentMap: Record<string, string> = {
    input: 'ElInput',
    select: 'ElSelect',
    date: 'ElDatePicker',
    daterange: 'ElDatePicker',
    number: 'ElInputNumber',
    radio: 'ElRadioGroup',
    checkbox: 'ElCheckboxGroup'
  }
  
  return componentMap[field.type || 'input'] || 'ElInput'
}

// 获取字段属性
const getFieldProps = (field: SearchField) => {
  const baseProps: Record<string, any> = {
    clearable: true
  }
  
  if (field.type === 'select' && field.options) {
    baseProps.options = field.options
  }
  
  if (field.type === 'daterange') {
    baseProps.type = 'daterange'
    baseProps['range-separator'] = '至'
    baseProps['start-placeholder'] = '开始日期'
    baseProps['end-placeholder'] = '结束日期'
    baseProps['value-format'] = 'YYYY-MM-DD'
  }
  
  if (field.type === 'date') {
    baseProps['value-format'] = 'YYYY-MM-DD'
  }
  
  return { ...baseProps, ...field.props }
}

// 获取历史记录文本
const getHistoryText = (item: HistoryItem) => {
  if (typeof item === 'string') return item
  return item.keyword
}

// 处理输入
const handleInput = (value: string) => {
  emit('update:modelValue', value)
  emit('input', value)
  
  if (props.showHistory && value.trim()) {
    historyVisible.value = true
  } else {
    historyVisible.value = false
  }
}

// 处理清空
const handleClear = () => {
  emit('clear')
  historyVisible.value = false
}

// 处理搜索
const handleSearch = () => {
  const searchKeyword = keyword.value.trim()
  if (!searchKeyword && !props.showAdvanced) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  saveHistory()
  emit('search', searchKeyword)
  historyVisible.value = false
}

// 切换高级搜索
const toggleAdvanced = () => {
  isExpanded.value = !isExpanded.value
  if (isExpanded.value) {
    nextTick(() => {
      // 高级面板展开后，可以执行一些初始化操作
    })
  }
}

// 处理重置
const handleReset = () => {
  Object.keys(advancedForm).forEach(key => {
    advancedForm[key] = ''
  })
  emit('update:advancedModelValue', { ...advancedForm })
  emit('advanced-change', { ...advancedForm })
}

// 处理高级搜索
const handleAdvancedSearch = () => {
  saveHistory()
  emit('search', keyword.value.trim(), { ...advancedForm })
  historyVisible.value = false
  isExpanded.value = false
}

// 选择历史记录
const selectHistory = (item: HistoryItem) => {
  const text = getHistoryText(item)
  keyword.value = text
  emit('update:modelValue', text)
  emit('history-select', item)
  handleSearch()
}

// 删除历史记录
const deleteHistory = (item: HistoryItem) => {
  const index = history.value.findIndex(h => 
    typeof h === 'string' ? h === item : h.keyword === (item as any).keyword
  )
  
  if (index > -1) {
    history.value.splice(index, 1)
    try {
      localStorage.setItem(props.historyKey, JSON.stringify(history.value))
    } catch (error) {
      console.error('更新搜索历史失败:', error)
    }
  }
}

// 清空历史记录
const clearHistory = () => {
  history.value = []
  try {
    localStorage.removeItem(props.historyKey)
  } catch (error) {
    console.error('清空搜索历史失败:', error)
  }
}

// 关闭历史面板
const closeHistory = () => {
  historyVisible.value = false
}

// 聚焦搜索框
const focus = () => {
  if (searchInputRef.value) {
    searchInputRef.value.focus()
  }
}

// 暴露的方法
defineExpose({
  focus,
  clear: handleClear,
  reset: handleReset,
  search: handleSearch,
  toggleAdvanced
})

// 监听关键词变化
watch(() => props.modelValue, (value) => {
  keyword.value = value
})

// 监听高级表单变化
watch(() => props.advancedModelValue, (value) => {
  Object.assign(advancedForm, value)
})

// 监听高级表单内部变化
watch(advancedForm, (value) => {
  emit('update:advancedModelValue', { ...value })
  emit('advanced-change', { ...value })
}, { deep: true })

// 初始化
onMounted(() => {
  initHistory()
})

// 监听输入框焦点
watch(() => keyword.value, (value) => {
  if (props.showHistory && value.trim()) {
    historyVisible.value = true
  }
})
</script>

<style scoped lang="scss">
.app-search {
  position: relative;
  width: 100%;
  transition: all 0.3s;

  &.expanded {
    .search-input-wrapper {
      margin-bottom: 16px;
    }
  }

  .search-input-wrapper {
    display: flex;
    gap: 10px;
    align-items: center;

    :deep(.el-input) {
      flex: 1;

      .el-input__wrapper {
        transition: all 0.3s;
        
        &:hover {
          box-shadow: 0 0 0 1px #409eff inset;
        }
        
        &.is-focus {
          box-shadow: 0 0 0 1px #409eff inset;
        }
      }
    }

    .search-icon {
      color: #c0c4cc;
    }

    .advanced-icon {
      color: #c0c4cc;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        color: #409eff;
      }

      &.active {
        color: #409eff;
      }
    }

    .search-button {
      white-space: nowrap;
    }
  }

  .advanced-panel {
    background: white;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    margin-top: 8px;
    overflow: hidden;

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: #f5f7fa;
      border-bottom: 1px solid #ebeef5;

      .panel-title {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
      }

      .panel-actions {
        display: flex;
        gap: 8px;
      }
    }

    .panel-content {
      padding: 16px;

      .advanced-form {
        display: flex;
        flex-wrap: wrap;
        gap: 16px;

        .advanced-field {
          margin-bottom: 0;
          min-width: 200px;

          :deep(.el-form-item__label) {
            padding-right: 8px;
          }

          :deep(.el-input),
          :deep(.el-select),
          :deep(.el-date-editor) {
            width: 100%;
          }
        }
      }
    }

    .panel-footer {
      padding: 12px 16px;
      border-top: 1px solid #ebeef5;
      text-align: right;
    }
  }

  .history-panel {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: white;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    margin-top: 8px;
    z-index: 1000;
    overflow: hidden;

    .history-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: #f5f7fa;
      border-bottom: 1px solid #ebeef5;

      .history-title {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
      }

      .clear-history-btn {
        padding: 0;
        height: auto;
      }
    }

    .history-list {
      max-height: 300px;
      overflow-y: auto;

      .history-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background: #f5f7fa;
        }

        .history-text {
          flex: 1;
          color: #606266;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .history-delete {
          color: #c0c4cc;
          font-size: 12px;
          margin-left: 8px;
          transition: all 0.3s;

          &:hover {
            color: #f56c6c;
          }
        }
      }
    }
  }
}

// 动画效果
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
  max-height: 500px;
  opacity: 1;
}

.slide-down-enter-from,
.slide-down-leave-to {
  max-height: 0;
  opacity: 0;
  margin-top: 0;
  overflow: hidden;
}

@media (max-width: 768px) {
  .app-search {
    .search-input-wrapper {
      flex-direction: column;
      align-items: stretch;
    }

    .advanced-form {
      flex-direction: column;

      .advanced-field {
        min-width: 100% !important;
      }
    }
  }
}
</style>