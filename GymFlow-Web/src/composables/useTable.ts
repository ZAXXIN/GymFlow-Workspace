import { ref, reactive, computed, onMounted, watch } from 'vue'
import type { Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Action } from '@/types'

interface TableOptions<T = any> {
  // API函数
  fetchData: (params: any) => Promise<any>
  deleteData?: (id: string | number) => Promise<any>
  exportData?: (params: any) => Promise<any>
  
  // 表格配置
  pageSize?: number
  showSelection?: boolean
  showIndex?: boolean
  showOperation?: boolean
  operationWidth?: number
  operationActions?: Action[]
  customRowClass?: (row: T, rowIndex: number) => string
  
  // 事件回调
  onSelectionChange?: (selection: T[]) => void
  onRowClick?: (row: T, column: any, event: Event) => void
  onCellClick?: (row: T, column: any, cell: any, event: Event) => void
  onDeleteSuccess?: () => void
}

interface Pagination {
  page: number
  size: number
  total: number
}

interface Sort {
  prop?: string
  order?: 'ascending' | 'descending' | null
}

/**
 * 通用表格操作组合式函数
 * @param options 表格配置选项
 * @returns 表格相关的状态和方法
 */
export function useTable<T = any>(options: TableOptions<T>) {
  // 表格数据
  const tableData = ref<T[]>([]) as Ref<T[]>
  const loading = ref(false)
  const selectedRows = ref<T[]>([]) as Ref<T[]>
  
  // 分页
  const pagination = reactive<Pagination>({
    page: 1,
    size: options.pageSize || 10,
    total: 0
  })
  
  // 排序
  const sort = reactive<Sort>({
    prop: undefined,
    order: null
  })
  
  // 搜索参数
  const searchParams = ref<Record<string, any>>({})
  
  // 操作列配置
  const operationActions = computed(() => {
    const defaultActions: Action[] = [
      {
        label: '查看',
        type: 'primary',
        icon: 'View',
        action: 'view',
        show: true
      },
      {
        label: '编辑',
        type: 'primary',
        icon: 'Edit',
        action: 'edit',
        show: true
      }
    ]
    
    if (options.deleteData) {
      defaultActions.push({
        label: '删除',
        type: 'danger',
        icon: 'Delete',
        action: 'delete',
        show: true,
        confirm: true,
        confirmMessage: '确定要删除这条数据吗？'
      })
    }
    
    return options.operationActions || defaultActions
  })
  
  // 获取表格数据
  const fetchTableData = async () => {
    try {
      loading.value = true
      
      const params = {
        page: pagination.page,
        size: pagination.size,
        ...searchParams.value
      }
      
      // 添加排序参数
      if (sort.prop && sort.order) {
        params.sortField = sort.prop
        params.sortOrder = sort.order === 'ascending' ? 'asc' : 'desc'
      }
      
      const response = await options.fetchData(params)
      
      if (response.code === 200) {
        tableData.value = response.data?.records || response.data?.list || []
        pagination.total = response.data?.total || 0
        
        // 如果有自定义行类名函数，应用它
        if (options.customRowClass) {
          tableData.value = tableData.value.map((row, index) => {
            const rowWithClass = { ...row }
            Object.defineProperty(rowWithClass, '_rowClassName', {
              value: options.customRowClass!(row, index),
              enumerable: false
            })
            return rowWithClass
          })
        }
      } else {
        ElMessage.error(response.message || '获取数据失败')
      }
    } catch (error) {
      console.error('获取表格数据失败:', error)
      ElMessage.error('获取数据失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }
  
  // 处理页码变化
  const handlePageChange = (page: number) => {
    pagination.page = page
    fetchTableData()
  }
  
  // 处理每页条数变化
  const handleSizeChange = (size: number) => {
    pagination.size = size
    pagination.page = 1
    fetchTableData()
  }
  
  // 处理排序变化
  const handleSortChange = ({ prop, order }: { prop?: string; order?: 'ascending' | 'descending' }) => {
    sort.prop = prop
    sort.order = order || null
    fetchTableData()
  }
  
  // 处理选择变化
  const handleSelectionChange = (selection: T[]) => {
    selectedRows.value = selection
    options.onSelectionChange?.(selection)
  }
  
  // 处理行点击
  const handleRowClick = (row: T, column: any, event: Event) => {
    options.onRowClick?.(row, column, event)
  }
  
  // 处理单元格点击
  const handleCellClick = (row: T, column: any, cell: any, event: Event) => {
    options.onCellClick?.(row, column, cell, event)
  }
  
  // 处理操作按钮点击
  const handleActionClick = async (action: string, row: T, index: number) => {
    switch (action) {
      case 'view':
        // 查看操作，由父组件处理
        break
      case 'edit':
        // 编辑操作，由父组件处理
        break
      case 'delete':
        await handleDelete(row, index)
        break
      default:
        // 自定义操作，由父组件处理
        break
    }
  }
  
  // 删除数据
  const handleDelete = async (row: T, index: number) => {
    try {
      if (!options.deleteData) return
      
      await ElMessageBox.confirm(
        '确定要删除这条数据吗？',
        '删除确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      loading.value = true
      const id = (row as any).id || (row as any)._id
      await options.deleteData(id)
      
      ElMessage.success('删除成功')
      
      // 重新获取数据
      await fetchTableData()
      
      // 回调函数
      options.onDeleteSuccess?.()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
        ElMessage.error('删除失败')
      }
    } finally {
      loading.value = false
    }
  }
  
  // 批量删除
  const handleBatchDelete = async () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的数据')
      return
    }
    
    try {
      if (!options.deleteData) return
      
      await ElMessageBox.confirm(
        `确定要删除选中的 ${selectedRows.value.length} 条数据吗？`,
        '批量删除确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      loading.value = true
      
      // 批量删除
      const deletePromises = selectedRows.value.map(row => {
        const id = (row as any).id || (row as any)._id
        return options.deleteData!(id)
      })
      
      await Promise.all(deletePromises)
      
      ElMessage.success('批量删除成功')
      
      // 重新获取数据
      await fetchTableData()
      selectedRows.value = []
      
      // 回调函数
      options.onDeleteSuccess?.()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除失败:', error)
        ElMessage.error('批量删除失败')
      }
    } finally {
      loading.value = false
    }
  }
  
  // 导出数据
  const handleExport = async () => {
    if (!options.exportData) {
      ElMessage.warning('导出功能未配置')
      return
    }
    
    try {
      loading.value = true
      
      const params = {
        ...searchParams.value,
        page: pagination.page,
        size: pagination.size
      }
      
      const response = await options.exportData(params)
      
      if (response.code === 200) {
        // 创建下载链接
        const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `导出数据_${new Date().getTime()}.xlsx`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        ElMessage.success('导出成功')
      } else {
        ElMessage.error(response.message || '导出失败')
      }
    } catch (error) {
      console.error('导出失败:', error)
      ElMessage.error('导出失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }
  
  // 搜索
  const handleSearch = (params: Record<string, any>) => {
    searchParams.value = { ...params }
    pagination.page = 1
    fetchTableData()
  }
  
  // 重置搜索
  const handleReset = () => {
    searchParams.value = {}
    pagination.page = 1
    fetchTableData()
  }
  
  // 刷新表格
  const refreshTable = () => {
    fetchTableData()
  }
  
  // 监听分页或排序变化
  watch(
    () => [pagination.page, pagination.size, sort.prop, sort.order],
    () => {
      fetchTableData()
    }
  )
  
  // 初始化
  onMounted(() => {
    fetchTableData()
  })
  
  return {
    // 状态
    tableData,
    loading,
    selectedRows,
    pagination: reactive({
      currentPage: pagination.page,
      pageSize: pagination.size,
      total: pagination.total
    }),
    sort,
    
    // 配置
    operationActions,
    
    // 方法
    fetchTableData,
    handlePageChange,
    handleSizeChange,
    handleSortChange,
    handleSelectionChange,
    handleRowClick,
    handleCellClick,
    handleActionClick,
    handleDelete,
    handleBatchDelete,
    handleExport,
    handleSearch,
    handleReset,
    refreshTable
  }
}