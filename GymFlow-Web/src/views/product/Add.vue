<template>
  <div class="product-add-edit-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">{{ isEdit ? '编辑商品' : '添加商品' }}</span>
          <div class="header-actions">
            <el-button @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
          </div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        class="form-container"
        :validate-on-rule-change="false"
      >
        <!-- 基础信息 -->
        <div class="form-section">
          <div class="section-title">基础信息</div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品名称" prop="productName">
                <el-input
                  v-model="formData.productName"
                  placeholder="请输入商品名称"
                  clearable
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="商品类型" prop="productType">
                <el-select
                  v-model="formData.productType"
                  placeholder="请选择商品类型"
                  clearable
                  @change="handleProductTypeChange"
                >
                  <el-option label="会籍卡" :value="0" />
                  <el-option label="私教课" :value="1" />
                  <el-option label="团课" :value="2" />
                  <el-option label="相关产品" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品分类" prop="categoryId">
                <el-select
                  v-model="formData.categoryId"
                  placeholder="请选择商品分类"
                  clearable
                  filterable
                >
                  <el-option
                    v-for="category in flatCategories"
                    :key="category.id"
                    :label="category.fullName"
                    :value="category.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="商品状态" prop="status">
                <el-select v-model="formData.status" placeholder="请选择状态">
                  <el-option label="在售" :value="1" />
                  <el-option label="下架" :value="0" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="商品描述">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="4"
              placeholder="请输入商品描述"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="商品图片">
            <el-upload
              v-model:file-list="imageList"
              :action="uploadUrl"
              list-type="picture-card"
              :before-upload="beforeImageUpload"
              :on-success="handleImageSuccess"
              :on-remove="handleImageRemove"
              :on-error="handleUploadError"
              :headers="uploadHeaders"
              accept=".jpg,.jpeg,.png,.gif"
              multiple
            >
              <el-icon><Plus /></el-icon>
              <template #file="{ file }">
                <div>
                  <img
                    class="el-upload-list__item-thumbnail"
                    :src="file.url"
                    alt=""
                  />
                  <span class="el-upload-list__item-actions">
                    <span
                      class="el-upload-list__item-preview"
                      @click="handlePictureCardPreview(file)"
                    >
                      <el-icon><zoom-in /></el-icon>
                    </span>
                    <span
                      class="el-upload-list__item-delete"
                      @click="handleImageRemove(file)"
                    >
                      <el-icon><Delete /></el-icon>
                    </span>
                  </span>
                </div>
              </template>
            </el-upload>
            <div class="upload-tip">
              支持上传jpg、jpeg、png、gif格式图片，建议尺寸800x800px，大小不超过2MB
            </div>
          </el-form-item>
        </div>

        <!-- 价格与库存 -->
        <div class="form-section">
          <div class="section-title">价格与库存</div>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="原价(¥)" prop="originalPrice">
                <el-input-number
                  v-model="formData.originalPrice"
                  :min="0"
                  :precision="2"
                  :step="0.01"
                  placeholder="请输入原价"
                  style="width: 100%"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="现价(¥)" prop="currentPrice">
                <el-input-number
                  v-model="formData.currentPrice"
                  :min="0"
                  :precision="2"
                  :step="0.01"
                  placeholder="请输入现价"
                  style="width: 100%"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="成本价(¥)">
                <el-input-number
                  v-model="formData.costPrice"
                  :min="0"
                  :precision="2"
                  :step="0.01"
                  placeholder="请输入成本价"
                  style="width: 100%"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="库存数量" prop="stockQuantity">
                <el-input-number
                  v-model="formData.stockQuantity"
                  :min="0"
                  :step="1"
                  placeholder="请输入库存数量"
                  style="width: 100%"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="单位">
                <el-input
                  v-model="formData.unit"
                  placeholder="请输入单位，如：张、套、件"
                  maxlength="10"
                  clearable
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="规格信息">
            <el-input
              v-model="formData.specifications"
              type="textarea"
              :rows="3"
              placeholder="请输入规格信息，如：颜色、尺寸等"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </div>

        <!-- 商品详情 -->
        <div v-if="showDetailSection" class="form-section">
          <div class="section-title">商品详情</div>
          
          <template v-if="formData.productType === 0">
            <!-- 会籍卡详情 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="会籍类型">
                  <el-select
                    v-model="formData.detailDTO.membershipType"
                    placeholder="请选择会籍类型"
                    clearable
                  >
                    <el-option label="私教课" :value="0" />
                    <el-option label="团课" :value="1" />
                    <el-option label="月卡" :value="2" />
                    <el-option label="年卡" :value="3" />
                    <el-option label="其他" :value="4" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="有效期(天)">
                  <el-input-number
                    v-model="formData.detailDTO.validityDays"
                    :min="1"
                    placeholder="请输入有效期"
                    style="width: 100%"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="会籍权益">
              <el-input
                v-model="benefitsText"
                type="textarea"
                :rows="4"
                placeholder="请输入会籍权益，每行一个权益"
                @change="handleBenefitsChange"
              />
              <div class="form-tip">每行一个权益项，按回车分隔</div>
            </el-form-item>
          </template>

          <template v-if="formData.productType === 1 || formData.productType === 2">
            <!-- 课程详情 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="课时长(分钟)">
                  <el-input-number
                    v-model="formData.detailDTO.courseDuration"
                    :min="1"
                    placeholder="请输入课时长"
                    style="width: 100%"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="总节数">
                  <el-input-number
                    v-model="formData.detailDTO.totalSessions"
                    :min="1"
                    placeholder="请输入总节数"
                    style="width: 100%"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="课程级别">
                  <el-input
                    v-model="formData.detailDTO.courseLevel"
                    placeholder="请输入课程级别，如：初级、中级、高级"
                    maxlength="20"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="有效期(天)">
                  <el-input-number
                    v-model="formData.detailDTO.validityDays"
                    :min="1"
                    placeholder="请输入有效期"
                    style="width: 100%"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item v-if="formData.productType === 1" label="适用教练">
              <el-select
                v-model="coachIds"
                multiple
                placeholder="请选择适用教练"
                style="width: 100%"
                clearable
                filterable
              >
                <el-option
                  v-for="coach in coachList"
                  :key="coach.id"
                  :label="`${coach.realName} (${coach.phone})`"
                  :value="coach.id"
                />
              </el-select>
            </el-form-item>
          </template>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="最大购买数量">
                <el-input-number
                  v-model="formData.detailDTO.maxPurchaseQuantity"
                  :min="1"
                  placeholder="请输入最大购买数量"
                  style="width: 100%"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="使用规则">
            <el-input
              v-model="formData.detailDTO.usageRules"
              type="textarea"
              :rows="4"
              placeholder="请输入使用规则"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="退款政策">
            <el-input
              v-model="formData.detailDTO.refundPolicy"
              type="textarea"
              :rows="4"
              placeholder="请输入退款政策"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </div>

        <!-- 表单操作 -->
        <div class="form-actions">
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            {{ isEdit ? '更新' : '保存' }}
          </el-button>
          <el-button @click="goBack" :disabled="loading">取消</el-button>
        </div>
      </el-form>

      <!-- 图片预览 -->
      <el-dialog v-model="previewVisible" title="图片预览" width="600px">
        <img :src="previewImage" alt="预览图片" style="width: 100%" />
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules, type UploadProps } from 'element-plus'
import { useProductStore } from '@/stores/product'
import { useCoachStore } from '@/stores/coach'
import type { ProductBasicDTO, ProductDetailDTO, ProductFullDTO } from '@/types/product'
import type { UploadFile, UploadUserFile } from 'element-plus'

const router = useRouter()
const route = useRoute()
const productStore = useProductStore()
const coachStore = useCoachStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const previewVisible = ref(false)
const previewImage = ref('')

// 是否是编辑模式
const isEdit = computed(() => route.name === 'ProductEdit')

// 商品ID（编辑模式下使用）
const productId = computed(() => isEdit.value ? Number(route.params.id) : null)

// 表单数据
const formData = reactive<ProductBasicDTO>({
  productName: '',
  productType: 0,
  categoryId: undefined,
  description: '',
  images: [],
  originalPrice: 0,
  currentPrice: 0,
  costPrice: undefined,
  stockQuantity: 0,
  unit: '',
  specifications: '',
  status: 1,
  detailDTO: {
    membershipType: undefined,
    coachId: undefined,
    courseDuration: undefined,
    totalSessions: undefined,
    availableSessions: undefined,
    courseLevel: '',
    membershipBenefits: [],
    validityDays: undefined,
    defaultTotalSessions: undefined,
    maxPurchaseQuantity: undefined,
    refundPolicy: '',
    usageRules: '',
    coachIds: []
  }
})

// 图片列表
const imageList = ref<UploadUserFile[]>([])

// 教练列表
const coachList = ref<any[]>([])
const coachIds = ref<number[]>([])

// 会籍权益文本
const benefitsText = ref('')

// 分类列表（扁平化）
const flatCategories = computed(() => {
  const flatten = (categories: any[], prefix = ''): any[] => {
    return categories.reduce((acc, category) => {
      const fullName = prefix ? `${prefix} / ${category.categoryName}` : category.categoryName
      acc.push({
        id: category.id,
        fullName,
        categoryName: category.categoryName,
        parentId: category.parentId
      })
      
      if (category.children && category.children.length > 0) {
        acc.push(...flatten(category.children, fullName))
      }
      
      return acc
    }, [])
  }
  
  return flatten(productStore.categories)
})

// 是否显示详情部分
const showDetailSection = computed(() => {
  return formData.productType !== undefined && formData.productType !== 3
})

// 上传配置
const uploadUrl = ref(`${import.meta.env.VITE_API_BASE_URL}/upload/image`)
const uploadHeaders = ref({
  Authorization: `Bearer ${localStorage.getItem('token')}`
})

// 表单验证规则
const rules: FormRules = {
  productName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { max: 200, message: '商品名称长度不能超过200个字符', trigger: 'blur' }
  ],
  productType: [
    { required: true, message: '请选择商品类型', trigger: 'change' }
  ],
  originalPrice: [
    { required: true, message: '请输入原价', trigger: 'blur' },
    { 
      type: 'number', 
      validator: (_, value, callback) => {
        if (value < 0) {
          callback(new Error('原价不能小于0'))
        } else if (value >= 1000000) {
          callback(new Error('原价不能超过1000000'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  currentPrice: [
    { required: true, message: '请输入现价', trigger: 'blur' },
    { 
      type: 'number', 
      validator: (_, value, callback) => {
        if (value < 0) {
          callback(new Error('现价不能小于0'))
        } else if (value >= 1000000) {
          callback(new Error('现价不能超过1000000'))
        } else if (value > formData.originalPrice) {
          callback(new Error('现价不能高于原价'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  stockQuantity: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { 
      type: 'number', 
      validator: (_, value, callback) => {
        if (value < 0) {
          callback(new Error('库存数量不能小于0'))
        } else if (value >= 1000000) {
          callback(new Error('库存数量不能超过1000000'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 初始化数据
onMounted(async () => {
  try {
    // 加载分类数据
    await productStore.fetchCategories()
    
    // 如果是编辑模式，加载商品数据
    if (isEdit.value && productId.value) {
      await loadProductData(productId.value)
    } else {
      // 添加模式，设置默认值
      setDefaultValues()
    }
    
    // 加载教练数据
    await loadCoachData()
  } catch (error) {
    console.error('初始化失败:', error)
    ElMessage.error('初始化失败')
  }
})

// 加载商品数据
const loadProductData = async (id: number) => {
  try {
    loading.value = true
    const product = await productStore.fetchProductDetail(id)
    
    if (product) {
      // 复制基础数据
      Object.assign(formData, {
        productName: product.productName,
        productType: product.productType,
        categoryId: product.categoryId,
        description: product.description || '',
        images: product.images || [],
        originalPrice: product.originalPrice,
        currentPrice: product.currentPrice,
        costPrice: product.costPrice,
        stockQuantity: product.stockQuantity,
        unit: product.unit || '',
        specifications: product.specifications || '',
        status: product.status,
        detailDTO: product.detailDTO || {}
      })
      
      // 设置图片列表
      if (product.images && product.images.length > 0) {
        imageList.value = product.images.map((url, index) => ({
          name: `image-${index}.jpg`,
          url: url,
          status: 'success'
        }))
      }
      
      // 设置教练ID列表
      if (product.detailDTO?.coachIds) {
        coachIds.value = product.detailDTO.coachIds
      }
      
      // 设置会籍权益
      if (product.detailDTO?.membershipBenefits) {
        benefitsText.value = product.detailDTO.membershipBenefits.join('\n')
      }
    }
  } catch (error) {
    console.error('加载商品数据失败:', error)
    ElMessage.error('加载商品数据失败')
  } finally {
    loading.value = false
  }
}

// 设置默认值
const setDefaultValues = () => {
  formData.detailDTO = {
    membershipType: undefined,
    coachId: undefined,
    courseDuration: undefined,
    totalSessions: undefined,
    availableSessions: undefined,
    courseLevel: '',
    membershipBenefits: [],
    validityDays: undefined,
    defaultTotalSessions: undefined,
    maxPurchaseQuantity: undefined,
    refundPolicy: '',
    usageRules: '',
    coachIds: []
  }
}

// 加载教练数据
const loadCoachData = async () => {
  try {
    await coachStore.fetchCoachList({ pageNum: 1, pageSize: 100 })
    coachList.value = coachStore.coachList
  } catch (error) {
    console.error('加载教练数据失败:', error)
  }
}

// 商品类型变化
const handleProductTypeChange = (type: number) => {
  // 重置详情数据
  formData.detailDTO = {
    membershipType: undefined,
    coachId: undefined,
    courseDuration: undefined,
    totalSessions: undefined,
    availableSessions: undefined,
    courseLevel: '',
    membershipBenefits: [],
    validityDays: undefined,
    defaultTotalSessions: undefined,
    maxPurchaseQuantity: undefined,
    refundPolicy: '',
    usageRules: '',
    coachIds: []
  }
  
  // 根据类型设置默认值
  switch (type) {
    case 0: // 会籍卡
      formData.detailDTO.validityDays = 30
      break
    case 1: // 私教课
      formData.detailDTO.courseDuration = 60
      formData.detailDTO.totalSessions = 10
      formData.detailDTO.availableSessions = 10
      formData.detailDTO.validityDays = 180
      break
    case 2: // 团课
      formData.detailDTO.courseDuration = 60
      formData.detailDTO.totalSessions = 10
      formData.detailDTO.availableSessions = 10
      formData.detailDTO.validityDays = 90
      break
  }
  
  // 重置教练选择
  coachIds.value = []
}

// 图片上传前的验证
const beforeImageUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = /\.(jpg|jpeg|png|gif)$/i.test(file.name)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB！')
    return false
  }
  
  return true
}

// 图片上传成功
const handleImageSuccess = (response: any, file: UploadFile) => {
  if (response.code === 200 && response.data) {
    file.url = response.data
    formData.images = imageList.value.map(item => item.url || '').filter(url => url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 图片移除
const handleImageRemove = (file: UploadFile) => {
  const index = imageList.value.findIndex(item => item.uid === file.uid)
  if (index !== -1) {
    imageList.value.splice(index, 1)
    formData.images = imageList.value.map(item => item.url || '').filter(url => url)
  }
}

// 上传错误
const handleUploadError = (error: Error) => {
  console.error('上传失败:', error)
  ElMessage.error('上传失败')
}

// 图片预览
const handlePictureCardPreview = (file: UploadFile) => {
  previewImage.value = file.url || ''
  previewVisible.value = true
}

// 会籍权益变化
const handleBenefitsChange = (value: string) => {
  if (formData.detailDTO) {
    formData.detailDTO.membershipBenefits = value
      .split('\n')
      .filter(item => item.trim())
      .map(item => item.trim())
  }
}

// 监听教练ID变化
watch(coachIds, (newVal) => {
  if (formData.detailDTO) {
    formData.detailDTO.coachIds = newVal
  }
})

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 表单验证
    await formRef.value.validate()
    
    // 价格验证
    if (formData.currentPrice > formData.originalPrice) {
      ElMessage.error('现价不能高于原价')
      return
    }
    
    // 如果是课程类型，验证必要字段
    if (formData.productType === 1 || formData.productType === 2) {
      if (!formData.detailDTO?.courseDuration) {
        ElMessage.error('请填写课时长')
        return
      }
      if (!formData.detailDTO?.totalSessions) {
        ElMessage.error('请填写总节数')
        return
      }
    }
    
    loading.value = true
    
    if (isEdit.value && productId.value) {
      // 编辑模式
      await productStore.updateProduct(productId.value, formData)
      ElMessage.success('更新成功')
    } else {
      // 添加模式
      await productStore.addProduct(formData)
      ElMessage.success('添加成功')
    }
    
    goBack()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  } finally {
    loading.value = false
  }
}

// 返回
const goBack = () => {
  router.push('/product/list')
}
</script>

<style scoped lang="scss">
.product-add-edit-container {
  padding: 20px;
  
  .box-card {
    min-height: calc(100vh - 120px);
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .title {
        font-size: 18px;
        font-weight: bold;
        color: #333;
      }
      
      .header-actions {
        display: flex;
        gap: 10px;
      }
    }
    
    .form-container {
      padding: 20px 0;
      
      .form-section {
        margin-bottom: 30px;
        padding: 20px;
        background-color: #fff;
        border-radius: 8px;
        border: 1px solid #ebeef5;
        
        .section-title {
          font-size: 16px;
          font-weight: bold;
          color: #333;
          margin-bottom: 20px;
          padding-bottom: 10px;
          border-bottom: 2px solid #409eff;
        }
        
        .form-tip {
          font-size: 12px;
          color: #909399;
          margin-top: 5px;
        }
        
        .upload-tip {
          font-size: 12px;
          color: #909399;
          margin-top: 8px;
        }
        
        :deep(.el-upload-list__item-thumbnail) {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        :deep(.el-upload--picture-card) {
          width: 100px;
          height: 100px;
          line-height: 100px;
        }
        
        :deep(.el-upload-list__item) {
          width: 100px;
          height: 100px;
        }
      }
      
      .form-actions {
        margin-top: 30px;
        padding-top: 20px;
        border-top: 1px solid #ebeef5;
        text-align: center;
        
        .el-button {
          min-width: 100px;
        }
      }
    }
  }
}
</style>