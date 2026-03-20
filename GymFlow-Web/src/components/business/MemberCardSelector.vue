<template>
  <div class="member-card-selector">
    <!-- 卡类型选择（商品级联） -->
    <el-form-item label="卡类型" prop="cardType" required>
      <el-cascader
        v-model="selectedProduct"
        :options="productOptions"
        :props="cascaderProps"
        placeholder="请选择卡类型"
        style="width: 100%"
        clearable
        filterable
        :show-all-levels="false"
        @change="handleProductChange"
      />
    </el-form-item>

    <!-- 课程卡：显示总课时数（私教课、团课） -->
    <el-row :gutter="20" v-if="isCourseCard">
      <el-col :span="24">
        <el-form-item label="总课时" prop="totalSessions" required>
          <el-input-number
            v-model="localCard.totalSessions"
            :min="1"
            :max="999"
            :step="1"
            controls-position="right"
            style="width: 100%"
            placeholder="请输入总课时"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <!-- 金额 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-form-item label="金额" prop="amount" required>
          <el-input-number
            v-model="localCard.amount"
            :min="0"
            :step="100"
            :precision="2"
            controls-position="right"
            style="width: 100%"
            placeholder="请先选择商品"
            disabled
          >
            <template #prepend>¥</template>
          </el-input-number>
        </el-form-item>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { productApi } from '@/api/product'
import type { MemberCardDTO, ProductOption } from '@/types/member'

interface Props {
  modelValue: MemberCardDTO
  isEditMode?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: MemberCardDTO): void
  (e: 'change', value: MemberCardDTO): void
}

const props = withDefaults(defineProps<Props>(), {
  isEditMode: false
})

const emit = defineEmits<Emits>()

// 本地卡片数据
const localCard = ref<MemberCardDTO>({ ...props.modelValue })

// 商品选项列表
const productOptions = ref<any[]>([])
const productMap = ref<Map<number, ProductOption>>(new Map())
const selectedProductInfo = ref<ProductOption | null>(null)
const selectedProduct = ref<(string | number)[]>([])

// 级联选择器配置
const cascaderProps = {
  expandTrigger: 'hover' as const,
  value: 'id',
  label: 'productName',
  children: 'children',
  checkStrictly: false, // 不允许选择非叶子节点
  emitPath: false
}

// 判断是否为课程卡（私教课、团课）
const isCourseCard = computed(() => {
  const type = localCard.value.cardType
  return type === 0 || type === 1
})

// 商品描述
const productDescription = computed(() => {
  if (!selectedProductInfo.value) return ''
  const info = selectedProductInfo.value
  const desc = []
  if (info.validityDays) desc.push(`有效期：${info.validityDays}天`)
  if (info.totalSessions) desc.push(`总课时：${info.totalSessions}节`)
  if (info.description) desc.push(info.description)
  return desc.join(' | ')
})

// 加载商品列表
const loadProducts = async () => {
  try {
    // 按商品类型分类加载
    const productTypes = [0, 1, 2, 3] // 0-会籍卡，1-私教课，2-团课，3-相关产品
    const typeLabels = ['会籍卡', '私教课', '团课', '相关产品']
    
    const options = []
    
    for (let i = 0; i < productTypes.length; i++) {
      const type = productTypes[i]
      const response = await productApi.getProductList({
        productType: type,
        status: 1, // 只在售商品
        pageSize: 100
      })
      
      if (response.code === 200 && response.data.list.length > 0) {
        const children = response.data.list.map((product: any) => {
          productMap.value.set(product.id, {
            id: product.id,
            productName: product.productName,
            productType: type,
            productTypeDesc: product.productTypeDesc,
            currentPrice: product.currentPrice,
            originalPrice: product.originalPrice,
            validityDays: product.validityDays,
            totalSessions: product.totalSessions,
            description: product.description
          })
          return {
            id: product.id,
            productName: product.productName,
            productType: type,
            productTypeDesc: product.productTypeDesc,
            currentPrice: product.currentPrice
          }
        })
        
        options.push({
          id: `type-${type}`,
          productName: typeLabels[i],
          children
        })
      }
    }
    
    productOptions.value = options
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  }
}

// 商品选择变化
const handleProductChange = (value: number) => {
  if (!value) {
    // 清空选择
    selectedProductInfo.value = null
    localCard.value.productId = undefined
    localCard.value.productName = undefined
    localCard.value.cardType = undefined
    localCard.value.totalSessions = undefined
    localCard.value.amount = 0
    emit('update:modelValue', localCard.value)
    emit('change', localCard.value)
    return
  }
  
  const product = productMap.value.get(value)
  if (product) {
    selectedProductInfo.value = product
    localCard.value.productId = product.id
    localCard.value.productName = product.productName
    localCard.value.cardType = product.productType
    localCard.value.amount = product.currentPrice
    
    // 根据商品类型设置默认总课时
    if (product.productType === 0 || product.productType === 1) {
      localCard.value.totalSessions = product.totalSessions || 10
    } else {
      localCard.value.totalSessions = undefined
    }
    
    emit('update:modelValue', localCard.value)
    emit('change', localCard.value)
  }
}

// 监听外部数据变化
watch(() => props.modelValue, (newVal) => {
  localCard.value = { ...newVal }
  // 如果外部传入的 productId 有值，需要回显选中
  if (newVal.productId) {
    const product = productMap.value.get(newVal.productId)
    if (product) {
      selectedProductInfo.value = product
      selectedProduct.value = [newVal.productId] // 注意级联的值可能是数组，我们使用 emitPath:false 所以直接传id
    }
  }
}, { deep: true, immediate: true })

// 监听本地数据变化
watch(localCard, (newVal) => {
  emit('update:modelValue', newVal)
  emit('change', newVal)
}, { deep: true })

onMounted(() => {
  loadProducts()
})
</script>

<style scoped lang="scss">
.member-card-selector {
  .el-row {
    margin-bottom: 18px;
  }
  
  .el-alert {
    margin-top: 10px;
  }
}
</style>