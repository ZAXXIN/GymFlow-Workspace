<template>
  <div class="product-list-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">商品管理</span>
          <div class="header-actions">
            <el-button v-permission="'product:add'" type="primary" @click="handleAdd">
              <el-icon>
                <Plus />
              </el-icon>
              添加商品
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="queryParams" :inline="true" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="queryParams.productName" placeholder="请输入商品名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>

        <el-form-item label="商品类型">
          <el-select v-model="queryParams.productType" placeholder="请选择商品类型" clearable>
            <el-option label="会籍卡" :value="0" />
            <el-option label="私教课" :value="1" />
            <el-option label="团课" :value="2" />
            <el-option label="相关产品" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="在售" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon>
              <Search />
            </el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon>
              <Refresh />
            </el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="store.loading" :data="formattedList" border style="width: 100%" @sort-change="handleSortChange">
        <el-table-column type="index" label="序号" width="60" align="center" />

        <el-table-column label="商品图片" width="100" align="center">
          <template #default="{ row }">
            <el-image v-if="row.images && row.images.length > 0" :src="row.images[0]" :preview-src-list="row.images" preview-teleported fit="cover" style="width: 60px; height: 60px; border-radius: 4px;">
              <template #error>
                <div class="image-error">
                  <el-icon>
                    <Picture />
                  </el-icon>
                </div>
              </template>
            </el-image>
            <div v-else class="no-image">
              <el-icon>
                <Picture />
              </el-icon>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="productName" label="商品名称" min-width="180">
          <template #default="{ row }">
            <div class="product-name">
              <span class="name">{{ row.productName }}</span>
              <div class="type">{{ row.productTypeDesc }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="currentPrice" label="价格" width="120" align="center">
          <template #default="{ row }">
            <div class="price">
              <div class="current">{{ row.priceFormatted }}</div>
              <div v-if="row.discount" class="discount">{{ row.discountFormatted }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="stockQuantity" label="库存/销量" width="120" align="center">
          <template #default="{ row }">
            <div>
              <div :class="row.stockStatusClass">{{ row.stockQuantity }}</div>
              <div class="sales">销量: {{ row.salesVolume }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="140" align="center">
          <template #default="{ row }">
            {{ row.createTimeFormatted }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row.id)">
              详情
            </el-button>
            <el-button v-permission="'product:edit'" type="primary" size="small" link @click="handleEdit(row.id)">
              编辑
            </el-button>
            <el-button v-permission="'product:status'" v-if="row.status === 1" type="warning" size="small" link @click="handleStatusChange(row.id, 0)">
              下架
            </el-button>
            <el-button v-permission="'product:status'" v-else type="success" size="small" link @click="handleStatusChange(row.id, 1)">
              上架
            </el-button>
            <el-popconfirm title="确认删除该商品吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button v-permission="'product:delete'" type="danger" size="small" link>
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination v-model:current-page="store.pageInfo.pageNum" v-model:page-size="store.pageInfo.pageSize" :page-sizes="[10, 20, 50, 100]" :total="store.total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useProductStore } from '@/stores/product'
import type { ProductQueryDTO } from '@/types/product'
import { usePermission } from '@/composables/usePermission'

const { hasPermission } = usePermission()

const router = useRouter()
const store = useProductStore()

// 查询参数
const queryParams = ref<ProductQueryDTO>({
  // pageNum: 1,
  // pageSize: 10,
  ProductName: '',
  ProductType: '',
  cardStatus: '',
})

// 格式化后的列表
const formattedList = computed(() => store.formattedProductList())

// 初始化数据
onMounted(() => {
  fetchData()
})

// 获取数据
const fetchData = async () => {
  try {
    await store.fetchProductList(queryParams.value)
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  queryParams.value.pageNum = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: store.pageInfo.pageSize,
  }
  fetchData()
}

// 排序
const handleSortChange = (sort: any) => {
  console.log('排序变化:', sort)
  // 这里可以添加排序逻辑
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  queryParams.value.pageSize = size
  store.setPageInfo(1, size)
  fetchData()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  queryParams.value.pageNum = page
  store.setPageInfo(page, store.pageInfo.pageSize)
  fetchData()
}

// 添加商品
const handleAdd = () => {
  router.push('/product/add')
}

// 查看详情
const handleView = (id: number) => {
  router.push(`/product/detail/${id}`)
}

// 编辑商品
const handleEdit = (id: number) => {
  router.push(`/product/edit/${id}`)
}

// 状态变更
const handleStatusChange = async (id: number, status: number) => {
  try {
    const actionText = status === 1 ? '上架' : '下架'
    await store.updateProductStatus(id, status)
    ElMessage.success(`${actionText}成功`)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除商品
const handleDelete = async (id: number) => {
  try {
    await store.deleteProduct(id)
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped lang="scss">
.product-list-container {
  padding: 20px;

  .box-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .title {
        font-size: 18px;
        font-weight: bold;
      }

      .header-actions {
        display: flex;
        gap: 10px;
      }
    }

    .search-form {
      margin-bottom: 20px;
    }

    .product-name {
      .name {
        font-weight: 500;
        margin-bottom: 4px;
      }

      .type {
        font-size: 12px;
        color: #666;
      }
    }

    .price {
      .current {
        font-weight: bold;
        color: #f56c6c;
      }

      .discount {
        font-size: 12px;
        color: #999;
        text-decoration: line-through;
      }
    }

    .stock-status {
      font-weight: 500;

      &.text-success {
        color: #67c23a;
      }

      &.text-danger {
        color: #f56c6c;
      }
    }

    .no-image {
      width: 60px;
      height: 60px;
      background-color: #f5f5f5;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #ccc;
    }

    .image-error {
      width: 60px;
      height: 60px;
      background-color: #f5f5f5;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #ccc;
    }

    .pagination-container {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }
}
</style>