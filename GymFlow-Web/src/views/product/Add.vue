<template>
  <div class="product-form-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/product' }">商品管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ isEdit ? '编辑商品' : '新增商品' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">{{ isEdit ? '编辑商品信息' : '新增商品' }}</h1>
      </div>
      <div class="header-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </div>

    <!-- 主表单区域 -->
    <div class="form-content">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="product-form">
        <!-- 基础信息 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">基础信息</span>
            </div>
          </template>

          <div class="form-row">
            <el-form-item label="商品名称" prop="productName" class="form-item">
              <el-input v-model="formData.productName" placeholder="请输入商品名称" maxlength="200" clearable />
            </el-form-item>

            <el-form-item label="商品类型" prop="productType" class="form-item">
              <el-select v-model="formData.productType" placeholder="请选择商品类型" style="width: 100%" @change="handleProductTypeChange">
                <el-option label="会籍卡" :value="0" />
                <el-option label="私教课" :value="1" />
                <el-option label="团课" :value="2" />
                <el-option label="相关产品" :value="3" />
              </el-select>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="商品状态" prop="status" class="form-item">
              <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="在售" :value="1" />
                <el-option label="下架" :value="0" />
              </el-select>
            </el-form-item>
          </div>

          <div class="form-row">
            <!-- 会籍卡 -->
            <el-form-item v-if="formData.productType == 0" label="有效期(天)" prop="validityDays" class="form-item">
              <el-input-number v-model="formData.validityDays" :min="1" :max="3650" placeholder="请输入有效期" style="width: 100%" controls-position="right" />
              <!-- <div class="form-tip">会员购买后，会籍到期时间 = 购买日期 + 有效期天数</div> -->
            </el-form-item>

            <!-- 私教课/团课 -->
            <el-form-item v-if="formData.productType == 1 || formData.productType == 2" label="总课时数" prop="totalSessions" class="form-item">
              <el-input-number v-model="formData.totalSessions" :min="1" placeholder="请输入总课时数" style="width: 100%" controls-position="right" />
            </el-form-item>

            <el-form-item label="最大购买数量" class="form-item">
              <el-input-number v-model="formData.maxPurchaseQuantity" :min="1" placeholder="请输入最大购买数量" style="width: 100%" controls-position="right" />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="商品描述" class="full-width">
              <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="请输入商品描述" maxlength="1000" show-word-limit />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="商品图片" class="full-width">
              <el-upload v-model:file-list="imageList" :action="uploadUrl" list-type="picture-card" :before-upload="beforeImageUpload" :on-success="handleImageSuccess" :on-remove="handleImageRemove" :on-error="handleUploadError" :headers="uploadHeaders" accept=".jpg,.jpeg,.png,.gif" multiple>
                <el-icon>
                  <Plus />
                </el-icon>
                <template #file="{ file }">
                  <div>
                    <img class="el-upload-list__item-thumbnail" :src="file.url" alt="" />
                    <span class="el-upload-list__item-actions">
                      <span class="el-upload-list__item-preview" @click="handlePictureCardPreview(file)">
                        <el-icon><zoom-in /></el-icon>
                      </span>
                      <span class="el-upload-list__item-delete" @click="handleImageRemove(file)">
                        <el-icon>
                          <Delete />
                        </el-icon>
                      </span>
                    </span>
                  </div>
                </template>
              </el-upload>
              <div class="upload-tip">支持上传jpg、jpeg、png、gif格式图片，建议尺寸800x800px，大小不超过2MB</div>
            </el-form-item>
          </div>
        </el-card>

        <!-- 价格与库存 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">价格与库存</span>
            </div>
          </template>

          <div class="form-row">
            <el-form-item label="原价(¥)" prop="originalPrice" class="form-item">
              <el-input-number v-model="formData.originalPrice" :min="0" :precision="2" :step="0.01" placeholder="请输入原价" style="width: 100%" controls-position="right" />
            </el-form-item>

            <el-form-item label="现价(¥)" prop="currentPrice" class="form-item">
              <el-input-number v-model="formData.currentPrice" :min="0" :precision="2" :step="0.01" placeholder="请输入现价" style="width: 100%" controls-position="right" />
            </el-form-item>

            <el-form-item label="库存数量" prop="stockQuantity" class="form-item">
              <el-input-number v-model="formData.stockQuantity" :min="0" :step="1" placeholder="请输入库存数量" style="width: 100%" controls-position="right" />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="规格信息" class="form-item">
              <el-input v-model="formData.specifications" type="textarea" :rows="3" placeholder="请输入规格信息" maxlength="500" show-word-limit />
            </el-form-item>
          </div>
        </el-card>

        <!-- 商品详情 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">商品详情</span>
            </div>
          </template>

          <div class="form-row" v-if="formData.productType == 0">
            <el-form-item label="会籍权益" class="full-width">
              <el-input v-model="benefitsText" type="textarea" :rows="4" placeholder="请输入会籍权益，每行一个权益" @change="handleBenefitsChange" />
              <div class="form-tip">每行一个权益项，按回车分隔</div>
            </el-form-item>
          </div>

          <!-- 通用字段（所有类型） -->
          <div class="form-row">
            <el-form-item label="使用规则" class="full-width">
              <el-input v-model="formData.usageRules" type="textarea" :rows="4" placeholder="请输入使用规则" maxlength="500" show-word-limit />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="退款政策" class="full-width">
              <el-input v-model="formData.refundPolicy" type="textarea" :rows="4" placeholder="请输入退款政策" maxlength="500" show-word-limit />
            </el-form-item>
          </div>
        </el-card>
      </el-form>
    </div>

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="600px">
      <img :src="previewImage" alt="预览图片" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules, type UploadProps } from 'element-plus'
import { useProductStore } from '@/stores/product'
import type { ProductBasicDTO } from '@/types/product'
import type { UploadFile, UploadUserFile } from 'element-plus'

const router = useRouter()
const route = useRoute()
const productStore = useProductStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const previewVisible = ref(false)
const previewImage = ref('')

const isEdit = computed(() => route.name === 'ProductEdit')
const productId = computed(() => (isEdit.value ? Number(route.params.id) : null))

const formData = reactive<ProductBasicDTO>({
  productName: '',
  productType: 0,
  description: '',
  images: [],
  originalPrice: 0,
  currentPrice: 0,
  stockQuantity: 0,
  specifications: '',
  status: 1,
  validityDays: undefined,
  totalSessions: undefined,
  membershipBenefits: [],
  maxPurchaseQuantity: 10,
  refundPolicy: '',
  usageRules: '',
})

const imageList = ref<UploadUserFile[]>([])
const benefitsText = ref('')

const uploadUrl = ref(`${import.meta.env.VITE_API_BASE_URL}/common/upload/image`)
const uploadHeaders = ref({
  Authorization: `Bearer ${localStorage.getItem('token')}`,
})

const rules: FormRules = {
  productName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { max: 200, message: '商品名称长度不能超过200个字符', trigger: 'blur' },
  ],
  productType: [{ required: true, message: '请选择商品类型', trigger: 'change' }],
  originalPrice: [
    { required: true, message: '请输入原价', trigger: 'blur' },
    { type: 'number', min: 0, message: '原价不能小于0', trigger: 'blur' },
  ],
  currentPrice: [
    { required: true, message: '请输入现价', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value < 0) callback(new Error('现价不能小于0'))
        else if (value > formData.originalPrice) callback(new Error('现价不能高于原价'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
  stockQuantity: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存数量不能小于0', trigger: 'blur' },
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  validityDays: [
    {
      validator: (_, value, callback) => {
        if (formData.productType === 0 && !value) {
          callback(new Error('会籍卡需要填写有效期天数'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  totalSessions: [
    {
      validator: (_, value, callback) => {
        if ((formData.productType === 1 || formData.productType === 2) && !value) {
          callback(new Error('课程类商品需要填写总课时数'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

onMounted(async () => {
  if (isEdit.value && productId.value) {
    await loadProductData(productId.value)
  } else {
    setDefaultValues()
  }
})

const loadProductData = async (id: number) => {
  try {
    loading.value = true
    const product = await productStore.fetchProductDetail(id)
    if (product) {
      Object.assign(formData, {
        productName: product.productName,
        productType: product.productType,
        description: product.description || '',
        images: product.images || [],
        originalPrice: product.originalPrice,
        currentPrice: product.currentPrice,
        stockQuantity: product.stockQuantity,
        specifications: product.specifications || '',
        status: product.status,
        validityDays: product.validityDays,
        totalSessions: product.totalSessions,
        maxPurchaseQuantity: product.maxPurchaseQuantity,
        refundPolicy: product.refundPolicy || '',
        usageRules: product.usageRules || '',
      })

      if (product.images && product.images.length > 0) {
        imageList.value = product.images.map((url, index) => ({
          name: `image-${index}.jpg`,
          url: url,
          status: 'success',
        }))
      }

      if (product.membershipBenefits) {
        benefitsText.value = product.membershipBenefits.join('\n')
      }
    }
  } catch (error) {
    console.error('加载商品数据失败:', error)
    ElMessage.error('加载商品数据失败')
  } finally {
    loading.value = false
  }
}

const setDefaultValues = () => {
  formData.maxPurchaseQuantity = 10
  formData.validityDays = undefined
  formData.totalSessions = undefined
  benefitsText.value = ''
}

const handleProductTypeChange = (type: number) => {
  formData.validityDays = undefined
  formData.totalSessions = undefined
  formData.membershipBenefits = []
  benefitsText.value = ''

  switch (type) {
    case 0:
      formData.validityDays = 30
      break
    case 1:
    case 2:
      formData.totalSessions = 10
      break
  }
}

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

const handleImageSuccess = (response: any, file: UploadFile) => {
  if (response.code === 200 && response.data) {
    file.url = response.data.url || response.data
    formData.images = imageList.value.map((item) => item.url || '').filter((url) => url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleImageRemove = (file: UploadFile) => {
  const index = imageList.value.findIndex((item) => item.uid === file.uid)
  if (index !== -1) {
    imageList.value.splice(index, 1)
    formData.images = imageList.value.map((item) => item.url || '').filter((url) => url)
  }
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const handlePictureCardPreview = (file: UploadFile) => {
  previewImage.value = file.url || ''
  previewVisible.value = true
}

const handleBenefitsChange = (value: string) => {
  formData.membershipBenefits = value
    .split('\n')
    .filter((item) => item.trim())
    .map((item) => item.trim())
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (formData.currentPrice > formData.originalPrice) {
      ElMessage.error('现价不能高于原价')
      return
    }
    loading.value = true
    if (isEdit.value && productId.value) {
      await productStore.updateProduct(productId.value, formData)
      ElMessage.success('更新成功')
    } else {
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

const goBack = () => {
  router.push('/product/list')
}
</script>

<style scoped>
/* 样式与之前相同，保持不变 */
.product-form-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}
.header-left .page-title {
  margin: 10px 0 0 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}
.header-actions {
  margin-top: 10px;
}
.form-content {
  background-color: white;
  border-radius: 4px;
  padding: 20px;
}
.form-section {
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}
.form-item {
  flex: 1;
  margin-bottom: 0;
}
.full-width {
  width: 100%;
}
.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
:deep(.el-card__header) {
  padding: 16px 20px;
  background-color: #fafafa;
}
:deep(.el-input-number) {
  width: 100%;
}
:deep(.el-select) {
  width: 100%;
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
</style>