<template>
  <div class="app-table">
    <!-- 表格操作栏 -->
    <div v-if="showToolbar && (showOperations || $slots.toolbar)" class="table-toolbar">
      <div class="toolbar-left">
        <slot name="toolbar-left" />
      </div>
      <div class="toolbar-right">
        <slot name="toolbar" />
        <div v-if="showOperations" class="default-operations">
          <el-button
            v-if="showSearch"
            type="primary"
            text
            :icon="Search"
            @click="$emit('search')"
          >
            搜索
          </el-button>
          <el-button
            v-if="showAdd"
            type="primary"
            :icon="Plus"
            @click="$emit('add')"
          >
            新增
          </el-button>
          <el-button
            v-if="showRefresh"
            :icon="Refresh"
            @click="$emit('refresh')"
          >
            刷新
          </el-button>
          <el-button
            v-if="showExport"
            :icon="Download"
            @click="$emit('export')"
          >
            导出
          </el-button>
        </div>
      </div>
    </div>

    <!-- 表格主体 -->
    <el-table
      ref="tableRef"
      v-bind="$attrs"
      :data="tableData"
      :height="height"
      :max-height="maxHeight"
      :stripe="stripe"
      :border="border"
      :size="size"
      :fit="fit"
      :show-header="showHeader"
      :highlight-current-row="highlightCurrentRow"
      :row-key="rowKey"
      :tree-props="treeProps"
      :default-sort="defaultSort"
      :cell-class-name="cellClassName"
      :row-class-name="rowClassName"
      :header-cell-class-name="headerCellClassName"
      :row-style="rowStyle"
      :cell-style="cellStyle"
      :header-row-style="headerRowStyle"
      :header-cell-style="headerCellStyle"
      :show-summary="showSummary"
      :sum-text="sumText"
      :summary-method="summaryMethod"
      :span-method="spanMethod"
      :select-on-indeterminate="selectOnIndeterminate"
      :indent="indent"
      :lazy="lazy"
      :load="load"
      @select="handleSelect"
      @select-all="handleSelectAll"
      @selection-change="handleSelectionChange"
      @cell-mouse-enter="handleCellMouseEnter"
      @cell-mouse-leave="handleCellMouseLeave"
      @cell-click="handleCellClick"
      @cell-dblclick="handleCellDblclick"
      @row-click="handleRowClick"
      @row-contextmenu="handleRowContextmenu"
      @row-dblclick="handleRowDblclick"
      @header-click="handleHeaderClick"
      @header-contextmenu="handleHeaderContextmenu"
      @sort-change="handleSortChange"
      @filter-change="handleFilterChange"
      @current-change="handleCurrentChange"
      @header-dragend="handleHeaderDragend"
      @expand-change="handleExpandChange"
    >
      <!-- 选择列 -->
      <el-table-column
        v-if="showSelection"
        type="selection"
        width="55"
        align="center"
        :selectable="selectable"
      />

      <!-- 序号列 -->
      <el-table-column
        v-if="showIndex"
        type="index"
        width="80"
        align="center"
        label="序号"
        :index="indexMethod"
      />

      <!-- 自定义列 -->
      <template v-for="column in processedColumns" :key="column.prop || column.type">
        <el-table-column
          v-bind="getColumnProps(column)"
          :show-overflow-tooltip="column.showOverflowTooltip ?? true"
        >
          <!-- 表头插槽 -->
          <template v-if="column.headerSlot" #header>
            <slot :name="column.headerSlot" :column="column" />
          </template>

          <!-- 默认内容插槽 -->
          <template v-if="column.slot" #default="scope">
            <slot :name="column.slot" :row="scope.row" :column="column" :$index="scope.$index" />
          </template>
          
          <!-- 表头自定义渲染 -->
          <template v-else-if="column.renderHeader" #header="scope">
            {{ column.renderHeader(scope) }}
          </template>
          
          <!-- 内容自定义渲染 -->
          <template v-else-if="column.formatter" #default="scope">
            {{ column.formatter(scope.row, scope.column, scope.$index) }}
          </template>
          
          <!-- 内容自定义渲染函数 -->
          <template v-else-if="column.render" #default="scope">
            <component
              :is="column.render"
              :row="scope.row"
              :column="scope.column"
              :index="scope.$index"
            />
          </template>
        </el-table-column>
      </template>

      <!-- 操作列 -->
      <el-table-column
        v-if="showActions"
        label="操作"
        :width="actionsWidth"
        align="center"
        fixed="right"
      >
        <template #default="scope">
          <div class="table-actions">
            <slot name="actions" :row="scope.row" :index="scope.$index" />
            <div v-if="!$slots.actions" class="default-actions">
              <el-button
                v-if="showView"
                type="primary"
                text
                size="small"
                @click="$emit('view', scope.row, scope.$index)"
              >
                查看
              </el-button>
              <el-button
                v-if="showEdit"
                type="primary"
                text
                size="small"
                @click="$emit('edit', scope.row, scope.$index)"
              >
                编辑
              </el-button>
              <el-button
                v-if="showDelete"
                type="danger"
                text
                size="small"
                @click="$emit('delete', scope.row, scope.$index)"
              >
                删除
              </el-button>
            </div>
          </div>
        </template>
      </el-table-column>

      <!-- 空状态 -->
      <template #empty>
        <div class="empty-state">
          <el-empty :description="emptyText" :image-size="emptyImageSize">
            <slot name="empty">
              <el-button v-if="showAdd" type="primary" @click="$emit('add')">
                添加数据
              </el-button>
            </slot>
          </el-empty>
        </div>
      </template>

      <!-- 加载状态 -->
      <template #append>
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>
        <slot name="append" />
      </template>
    </el-table>

    <!-- 分页 -->
    <div v-if="showPagination && pagination" class="table-pagination">
      <el-pagination
        v-bind="paginationProps"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'

interface ColumnProps {
  prop?: string
  label?: string
  width?: string | number
  minWidth?: string | number
  fixed?: boolean | 'left' | 'right'
  sortable?: boolean | 'custom'
  sortOrders?: ('ascending' | 'descending' | null)[]
  resizable?: boolean
  formatter?: (row: any, column: TableColumnCtx<any>, cellValue: any, index: number) => any
  showOverflowTooltip?: boolean
  align?: 'left' | 'center' | 'right'
  headerAlign?: 'left' | 'center' | 'right'
  className?: string
  labelClassName?: string
  filters?: Array<{ text: string; value: any }>
  filterPlacement?: string
  filterMultiple?: boolean
  filterMethod?: (value: any, row: any, column: TableColumnCtx<any>) => boolean
  filteredValue?: any[]
  slot?: string
  headerSlot?: string
  render?: any
  renderHeader?: (scope: any) => string
  type?: string
  index?: number | ((index: number) => number)
  selectable?: (row: any, index: number) => boolean
  reserveSelection?: boolean
  [key: string]: any
}

interface PaginationProps {
  page?: number
  pageSize?: number
  total?: number
  pageSizes?: number[]
  layout?: string
  background?: boolean
  small?: boolean
  disabled?: boolean
  hideOnSinglePage?: boolean
}

const props = withDefaults(defineProps<{
  // 数据相关
  data?: any[]
  columns?: ColumnProps[]
  loading?: boolean
  rowKey?: string
  // 表格配置
  height?: string | number
  maxHeight?: string | number
  stripe?: boolean
  border?: boolean
  size?: 'large' | 'default' | 'small'
  fit?: boolean
  showHeader?: boolean
  highlightCurrentRow?: boolean
  treeProps?: { children: string; hasChildren: string }
  defaultSort?: { prop: string; order: 'ascending' | 'descending' }
  // 样式回调
  cellClassName?: string | (({ row, column, rowIndex, columnIndex }: any) => string)
  rowClassName?: string | (({ row, rowIndex }: any) => string)
  headerCellClassName?: string | (({ row, column, rowIndex, columnIndex }: any) => string)
  rowStyle?: any
  cellStyle?: any
  headerRowStyle?: any
  headerCellStyle?: any
  // 汇总
  showSummary?: boolean
  sumText?: string
  summaryMethod?: (param: any) => string[]
  spanMethod?: (param: any) => number[] | { rowspan: number; colspan: number }
  // 其他配置
  selectOnIndeterminate?: boolean
  indent?: number
  lazy?: boolean
  load?: (row: any, treeNode: any, resolve: (data: any[]) => void) => void
  // 功能控制
  showToolbar?: boolean
  showOperations?: boolean
  showSearch?: boolean
  showAdd?: boolean
  showRefresh?: boolean
  showExport?: boolean
  showSelection?: boolean
  showIndex?: boolean
  showActions?: boolean
  showView?: boolean
  showEdit?: boolean
  showDelete?: boolean
  actionsWidth?: string | number
  showPagination?: boolean
  pagination?: PaginationProps | boolean
  emptyText?: string
  emptyImageSize?: number
  indexMethod?: (index: number) => number
  selectable?: (row: any, index: number) => boolean
}>(), {
  data: () => [],
  columns: () => [],
  loading: false,
  rowKey: 'id',
  stripe: true,
  border: true,
  size: 'default',
  fit: true,
  showHeader: true,
  highlightCurrentRow: false,
  selectOnIndeterminate: false,
  indent: 16,
  lazy: false,
  showToolbar: true,
  showOperations: true,
  showSearch: true,
  showAdd: true,
  showRefresh: true,
  showExport: true,
  showSelection: false,
  showIndex: false,
  showActions: true,
  showView: true,
  showEdit: true,
  showDelete: true,
  actionsWidth: 180,
  showPagination: true,
  emptyText: '暂无数据',
  emptyImageSize: 120,
  indexMethod: (index: number) => index + 1,
  selectable: () => true
})

const emit = defineEmits<{
  // 表格事件
  'select': [selection: any[], row: any]
  'select-all': [selection: any[]]
  'selection-change': [selection: any[]]
  'cell-mouse-enter': [row: any, column: any, cell: any, event: Event]
  'cell-mouse-leave': [row: any, column: any, cell: any, event: Event]
  'cell-click': [row: any, column: any, cell: any, event: Event]
  'cell-dblclick': [row: any, column: any, cell: any, event: Event]
  'row-click': [row: any, column: any, event: Event]
  'row-contextmenu': [row: any, column: any, event: Event]
  'row-dblclick': [row: any, column: any, event: Event]
  'header-click': [column: any, event: Event]
  'header-contextmenu': [column: any, event: Event]
  'sort-change': [sort: { prop: string; order: 'ascending' | 'descending' }]
  'filter-change': [filters: any]
  'current-change': [currentRow: any, oldCurrentRow: any]
  'header-dragend': [newWidth: number, oldWidth: number, column: any, event: Event]
  'expand-change': [row: any, expandedRows: any[]]
  // 自定义事件
  'search': []
  'add': []
  'refresh': []
  'export': []
  'view': [row: any, index: number]
  'edit': [row: any, index: number]
  'delete': [row: any, index: number]
  'size-change': [size: number]
  'current-change': [page: number]
  'page-change': [page: number]
}>()

const tableRef = ref<InstanceType<typeof ElTable>>()

// 表格数据
const tableData = computed(() => props.data)

// 处理后的列配置
const processedColumns = computed(() => {
  return props.columns.filter(column => column.type !== 'selection' && column.type !== 'index')
})

// 分页配置
const paginationProps = computed(() => {
  if (typeof props.pagination === 'boolean') {
    return props.pagination ? {
      page: 1,
      pageSize: 10,
      total: 0,
      pageSizes: [10, 20, 50, 100],
      layout: 'total, sizes, prev, pager, next, jumper',
      background: true,
      small: false,
      disabled: false,
      hideOnSinglePage: false
    } : {}
  }
  return {
    page: 1,
    pageSize: 10,
    total: 0,
    pageSizes: [10, 20, 50, 100],
    layout: 'total, sizes, prev, pager, next, jumper',
    background: true,
    small: false,
    disabled: false,
    hideOnSinglePage: false,
    ...props.pagination
  }
})

// 获取列属性
const getColumnProps = (column: ColumnProps) => {
  const { slot, headerSlot, render, renderHeader, ...rest } = column
  return rest
}

// 表格事件处理
const handleSelect = (selection: any[], row: any) => {
  emit('select', selection, row)
}

const handleSelectAll = (selection: any[]) => {
  emit('select-all', selection)
}

const handleSelectionChange = (selection: any[]) => {
  emit('selection-change', selection)
}

const handleCellMouseEnter = (row: any, column: any, cell: any, event: Event) => {
  emit('cell-mouse-enter', row, column, cell, event)
}

const handleCellMouseLeave = (row: any, column: any, cell: any, event: Event) => {
  emit('cell-mouse-leave', row, column, cell, event)
}

const handleCellClick = (row: any, column: any, cell: any, event: Event) => {
  emit('cell-click', row, column, cell, event)
}

const handleCellDblclick = (row: any, column: any, cell: any, event: Event) => {
  emit('cell-dblclick', row, column, cell, event)
}

const handleRowClick = (row: any, column: any, event: Event) => {
  emit('row-click', row, column, event)
}

const handleRowContextmenu = (row: any, column: any, event: Event) => {
  emit('row-contextmenu', row, column, event)
}

const handleRowDblclick = (row: any, column: any, event: Event) => {
  emit('row-dblclick', row, column, event)
}

const handleHeaderClick = (column: any, event: Event) => {
  emit('header-click', column, event)
}

const handleHeaderContextmenu = (column: any, event: Event) => {
  emit('header-contextmenu', column, event)
}

const handleSortChange = (sort: { prop: string; order: 'ascending' | 'descending' }) => {
  emit('sort-change', sort)
}

const handleFilterChange = (filters: any) => {
  emit('filter-change', filters)
}

const handleCurrentChange = (currentRow: any, oldCurrentRow: any) => {
  emit('current-change', currentRow, oldCurrentRow)
}

const handleHeaderDragend = (newWidth: number, oldWidth: number, column: any, event: Event) => {
  emit('header-dragend', newWidth, oldWidth, column, event)
}

const handleExpandChange = (row: any, expandedRows: any[]) => {
  emit('expand-change', row, expandedRows)
}

// 分页事件处理
const handleSizeChange = (size: number) => {
  emit('size-change', size)
  if (typeof props.pagination === 'object') {
    emit('page-change', 1) // 切换分页大小时回到第一页
  }
}

const handlePageChange = (page: number) => {
  emit('current-change', page)
  emit('page-change', page)
}

// 暴露的方法
defineExpose({
  // 获取表格实例
  getTableRef: () => tableRef.value,
  // 清空选择
  clearSelection: () => {
    if (tableRef.value) {
      tableRef.value.clearSelection()
    }
  },
  // 切换行选择
  toggleRowSelection: (row: any, selected?: boolean) => {
    if (tableRef.value) {
      tableRef.value.toggleRowSelection(row, selected)
    }
  },
  // 切换所有行选择
  toggleAllSelection: () => {
    if (tableRef.value) {
      tableRef.value.toggleAllSelection()
    }
  },
  // 设置当前行
  setCurrentRow: (row?: any) => {
    if (tableRef.value) {
      tableRef.value.setCurrentRow(row)
    }
  },
  // 清除当前行
  clearCurrentRow: () => {
    if (tableRef.value) {
      tableRef.value.setCurrentRow()
    }
  },
  // 排序
  sort: (prop: string, order: 'ascending' | 'descending') => {
    if (tableRef.value) {
      tableRef.value.sort(prop, order)
    }
  },
  // 清除排序
  clearSort: () => {
    if (tableRef.value) {
      tableRef.value.clearSort()
    }
  },
  // 清除筛选
  clearFilter: (columnKey?: string) => {
    if (tableRef.value) {
      tableRef.value.clearFilter(columnKey)
    }
  },
  // 重新计算布局
  doLayout: () => {
    if (tableRef.value) {
      tableRef.value.doLayout()
    }
  },
  // 滚动到指定位置
  scrollTo: (options: ScrollToOptions) => {
    nextTick(() => {
      const scrollBody = tableRef.value?.$el.querySelector('.el-table__body-wrapper')
      if (scrollBody) {
        scrollBody.scrollTo(options)
      }
    })
  },
  // 设置滚动位置
  setScrollTop: (top: number) => {
    nextTick(() => {
      const scrollBody = tableRef.value?.$el.querySelector('.el-table__body-wrapper')
      if (scrollBody) {
        scrollBody.scrollTop = top
      }
    })
  },
  // 设置滚动位置
  setScrollLeft: (left: number) => {
    nextTick(() => {
      const scrollBody = tableRef.value?.$el.querySelector('.el-table__body-wrapper')
      if (scrollBody) {
        scrollBody.scrollLeft = left
      }
    })
  }
})
</script>

<style scoped lang="scss">
.app-table {
  width: 100%;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;

  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #ebeef5;

    .toolbar-left,
    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .default-operations {
      display: flex;
      gap: 8px;
    }
  }

  .table-actions {
    display: flex;
    justify-content: center;
    gap: 8px;

    .default-actions {
      display: flex;
      gap: 4px;
    }
  }

  .empty-state {
    padding: 40px 0;
  }

  .loading-state {
    padding: 20px;
  }

  .table-pagination {
    display: flex;
    justify-content: flex-end;
    padding: 16px 20px;
    border-top: 1px solid #ebeef5;
    background: #fff;
  }
}

// 表格样式优化
:deep(.el-table) {
  .el-table__header-wrapper {
    th {
      background: #f5f7fa;
      color: #303133;
      font-weight: 600;
    }
  }

  .el-table__body-wrapper {
    &::-webkit-scrollbar {
      width: 6px;
      height: 6px;
    }

    &::-webkit-scrollbar-track {
      background: #f5f5f5;
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: #c0c4cc;
      border-radius: 3px;

      &:hover {
        background: #909399;
      }
    }
  }

  .el-table__row {
    &:hover {
      background-color: #f5f7fa;
    }

    &.current-row {
      background-color: #f0f9ff !important;
    }
  }

  .cell {
    line-height: 1.5;
    padding: 8px 0;
  }
}
</style>