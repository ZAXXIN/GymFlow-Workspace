<template>
  <div class="app-pagination" :class="[{ 'is-hidden': hideOnSinglePage && total <= pageSize }, position]">
    <el-pagination
      v-bind="paginationProps"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      :page-sizes="pageSizes"
      :layout="layout"
      :background="background"
      :small="small"
      :disabled="disabled"
      :hide-on-single-page="hideOnSinglePage"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    >
      <!-- 自定义内容 -->
      <template v-if="$slots.default" #default>
        <slot />
      </template>
      
      <!-- 自定义页数选择器 -->
      <template v-if="$slots.sizer" #sizer>
        <slot name="sizer" />
      </template>
      
      <!-- 自定义页码 -->
      <template v-if="$slots.pager" #pager>
        <slot name="pager" />
      </template>
      
      <!-- 自定义跳页 -->
      <template v-if="$slots.jumper" #jumper>
        <slot name="jumper" />
      </template>
    </el-pagination>
    
    <!-- 额外内容 -->
    <div v-if="$slots.extra" class="pagination-extra">
      <slot name="extra" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface PaginationProps {
  total?: number
  page?: number
  pageSize?: number
  pageSizes?: number[]
  layout?: string
  background?: boolean
  small?: boolean
  disabled?: boolean
  hideOnSinglePage?: boolean
  position?: 'left' | 'center' | 'right'
  showTotal?: boolean
  showSizeChanger?: boolean
  showQuickJumper?: boolean
  showSizeOptions?: boolean
}

const props = withDefaults(defineProps<PaginationProps>(), {
  total: 0,
  page: 1,
  pageSize: 10,
  pageSizes: () => [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true,
  small: false,
  disabled: false,
  hideOnSinglePage: false,
  position: 'right',
  showTotal: true,
  showSizeChanger: true,
  showQuickJumper: true,
  showSizeOptions: true
})

const emit = defineEmits<{
  'update:page': [page: number]
  'update:pageSize': [pageSize: number]
  'size-change': [size: number]
  'current-change': [page: number]
  'page-change': [page: number]
}>()

// 当前页码
const currentPage = computed({
  get: () => props.page,
  set: (value) => emit('update:page', value)
})

// 分页配置
const paginationProps = computed(() => {
  const layout = computedLayout()
  return {
    layout,
    background: props.background,
    small: props.small,
    disabled: props.disabled,
    hideOnSinglePage: props.hideOnSinglePage
  }
})

// 计算布局
const computedLayout = () => {
  let layout = props.layout
  
  if (!props.showTotal && layout.includes('total')) {
    layout = layout.replace('total, ', '').replace(', total', '').replace('total', '')
  }
  
  if (!props.showSizeChanger && layout.includes('sizes')) {
    layout = layout.replace('sizes, ', '').replace(', sizes', '').replace('sizes', '')
  }
  
  if (!props.showQuickJumper && layout.includes('jumper')) {
    layout = layout.replace('jumper, ', '').replace(', jumper', '').replace('jumper', '')
  }
  
  // 清理多余的逗号
  layout = layout.replace(/,\s*,/g, ',').replace(/^,\s*/, '').replace(/\s*,*$/, '')
  
  return layout
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  emit('update:pageSize', size)
  emit('size-change', size)
  // 当每页条数变化时，重置到第一页
  currentPage.value = 1
  emit('current-change', 1)
  emit('page-change', 1)
}

// 处理当前页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  emit('current-change', page)
  emit('page-change', page)
}

// 快捷跳转方法
const jumpToPage = (page: number) => {
  if (page < 1 || page > Math.ceil(props.total / props.pageSize)) {
    return
  }
  handleCurrentChange(page)
}

// 暴露的方法
defineExpose({
  jumpToPage,
  prevPage: () => {
    if (currentPage.value > 1) {
      jumpToPage(currentPage.value - 1)
    }
  },
  nextPage: () => {
    const totalPages = Math.ceil(props.total / props.pageSize)
    if (currentPage.value < totalPages) {
      jumpToPage(currentPage.value + 1)
    }
  },
  firstPage: () => jumpToPage(1),
  lastPage: () => jumpToPage(Math.ceil(props.total / props.pageSize)),
  refresh: () => {
    emit('current-change', currentPage.value)
    emit('page-change', currentPage.value)
  }
})
</script>

<style scoped lang="scss">
.app-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  transition: all 0.3s;

  &.left {
    justify-content: flex-start;
  }

  &.center {
    justify-content: center;
  }

  &.right {
    justify-content: flex-end;
  }

  &.is-hidden {
    display: none;
  }

  .pagination-extra {
    margin-left: 20px;
    display: flex;
    align-items: center;
    gap: 10px;
  }
}

// 分页样式优化
:deep(.el-pagination) {
  .btn-prev,
  .btn-next,
  .el-pager li {
    min-width: 32px;
    height: 32px;
    line-height: 32px;
    border-radius: 4px;
    transition: all 0.3s;

    &:not(.disabled):hover {
      color: #409eff;
      background: #f5f7fa;
    }

    &.active {
      background: #409eff;
      color: white;

      &:hover {
        background: #66b1ff;
      }
    }
  }

  .el-pagination__sizes {
    .el-input {
      width: 100px;
    }

    .el-input__wrapper {
      border-radius: 4px;
    }
  }

  .el-pagination__jump {
    .el-input {
      width: 50px;
    }

    .el-input__wrapper {
      border-radius: 4px;
    }
  }

  &.is-background {
    .btn-prev,
    .btn-next,
    .el-pager li {
      background: white;
      border: 1px solid #dcdfe6;

      &:not(.disabled):hover {
        border-color: #409eff;
      }

      &.active {
        border-color: #409eff;
      }
    }
  }

  &.is-small {
    .btn-prev,
    .btn-next,
    .el-pager li {
      min-width: 28px;
      height: 28px;
      line-height: 28px;
      font-size: 12px;
    }
  }
}
</style>