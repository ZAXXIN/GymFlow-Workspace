<template>
  <div class="role-permission-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/settings' }">系统设置</el-breadcrumb-item>
          <el-breadcrumb-item>角色权限</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">角色权限管理</h1>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleSave" :loading="saving">
          保存权限配置
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左侧角色列表 -->
      <el-col :span="8">
        <el-card class="role-card" v-loading="store.loading">
          <template #header>
            <div class="card-header">
              <span class="card-title">角色列表</span>
              <el-button 
                type="primary" 
                size="small" 
                @click="handleAddRole" 
                v-permission="'settings:role:add'"
              >
                <el-icon><Plus /></el-icon>
                新增角色
              </el-button>
            </div>
          </template>

          <div class="role-list">
            <div
              v-for="role in store.roles"
              :key="role.id"
              class="role-item"
              :class="{ active: currentRole?.id === role.id }"
              @click="selectRole(role)"
            >
              <div class="role-info">
                <span class="role-name">{{ role.roleName }}</span>
                <span class="role-code">{{ role.roleCode }}</span>
                <el-tag 
                  :type="role.status === 1 ? 'success' : 'info'" 
                  size="small"
                  effect="plain"
                  class="role-status"
                >
                  {{ role.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </div>
              <div class="role-actions">
                <el-button 
                  type="text" 
                  size="small" 
                  @click.stop="handleEditRole(role)" 
                  v-permission="'settings:role:edit'"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-popconfirm
                  v-if="role.roleCode !== 'BOSS'"
                  title="确定要删除这个角色吗？"
                  @confirm="handleDeleteRole(role.id)"
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                  v-permission="'settings:role:delete'"
                >
                  <template #reference>
                    <el-button type="text" size="small" @click.stop>
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧权限树 -->
      <el-col :span="16">
        <el-card v-if="currentRole" class="permission-card" v-loading="loadingPermissions">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="card-title">{{ currentRole.roleName }} - 权限配置</span>
                <el-tag 
                  :type="currentRole.status === 1 ? 'success' : 'info'" 
                  size="small"
                  style="margin-left: 10px;"
                >
                  {{ currentRole.status === 1 ? '已启用' : '已禁用' }}
                </el-tag>
              </div>
              <div class="header-actions">
                <el-button size="small" @click="expandAll">展开所有</el-button>
                <el-button size="small" @click="collapseAll">收起所有</el-button>
              </div>
            </div>
          </template>

          <div class="permission-tree-container">
            <el-tree
              ref="treeRef"
              :data="store.permissionTree"
              show-checkbox
              node-key="id"
              default-expand-all
              :props="{
                label: 'permissionName',
                children: 'children'
              }"
              @check="handleCheck"
            />
          </div>
        </el-card>

        <el-card v-else class="empty-card">
          <el-empty description="请先选择一个角色" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑角色对话框 -->
    <el-dialog
      v-model="roleDialog.visible"
      :title="roleDialog.title"
      width="400px"
      destroy-on-close
    >
      <el-form 
        :model="roleForm" 
        :rules="roleRules" 
        ref="roleFormRef" 
        label-width="80px"
        v-loading="roleDialog.loading"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input 
            v-model="roleForm.roleName" 
            placeholder="请输入角色名称" 
            maxlength="50" 
            clearable
          />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input 
            v-model="roleForm.roleCode" 
            placeholder="请输入角色编码，如：MANAGER" 
            :disabled="roleDialog.mode === 'edit'"
            maxlength="50"
            clearable
          />
          <div class="form-tip" v-if="roleDialog.mode === 'add'">
            创建后不可修改
          </div>
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input 
            v-model="roleForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入角色描述" 
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="roleDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveRole" :loading="roleDialog.loading">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { usePermission } from '@/composables/usePermission'
import { useRolePermissionStore } from '@/stores/settings/rolePermission'

const { hasPermission } = usePermission()
const store = useRolePermissionStore()

// 状态
const currentRole = ref<any>(null)
const treeRef = ref<any>()
const saving = ref(false)
const loadingPermissions = ref(false)

// 角色列表
const roles = computed(() => store.roles)

// 角色表单
const roleFormRef = ref<FormInstance>()
const roleDialog = reactive({
  visible: false,
  title: '',
  mode: 'add',
  loading: false
})

const roleForm = reactive({
  id: null as number | null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

const roleRules: FormRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { max: 50, message: '角色名称不能超过50个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' },
    { max: 50, message: '角色编码不能超过50个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 加载数据
const loadData = async () => {
  await store.fetchRoles()
  await store.fetchPermissionTree()
}

// 选择角色
const selectRole = async (role: any) => {
  currentRole.value = role
  
  loadingPermissions.value = true
  try {
    await store.fetchRolePermissionDetails(role.id)
    await nextTick()
    treeRef.value?.setCheckedKeys(store.rolePermissions)
  } catch (error) {
    console.error('加载角色权限失败:', error)
    ElMessage.error('加载角色权限失败')
  } finally {
    loadingPermissions.value = false
  }
}

// 展开所有
const expandAll = () => {
  const nodes = treeRef.value?.store?.nodesMap
  nodes?.forEach((node: any) => {
    if (node.childNodes && node.childNodes.length > 0) {
      node.expanded = true
    }
  })
}

// 收起所有
const collapseAll = () => {
  const nodes = treeRef.value?.store?.nodesMap
  nodes?.forEach((node: any) => {
    if (node.childNodes && node.childNodes.length > 0) {
      node.expanded = false
    }
  })
}

// 处理权限勾选
const handleCheck = () => {
  // 可以在这里添加实时验证或其他逻辑
}

// 保存权限
const handleSave = async () => {
  if (!currentRole.value) {
    ElMessage.warning('请先选择一个角色')
    return
  }

  const checkedKeys = treeRef.value?.getCheckedKeys(true) || []
  
  saving.value = true
  try {
    await store.updateRolePermissions(currentRole.value.id, checkedKeys)
  } catch (error) {
    console.error('保存权限失败:', error)
  } finally {
    saving.value = false
  }
}

// 添加角色
const handleAddRole = () => {
  roleDialog.mode = 'add'
  roleDialog.title = '新增角色'
  roleForm.id = null
  roleForm.roleName = ''
  roleForm.roleCode = ''
  roleForm.description = ''
  roleForm.status = 1
  roleDialog.visible = true
}

// 编辑角色
const handleEditRole = (role: any) => {
  roleDialog.mode = 'edit'
  roleDialog.title = '编辑角色'
  roleForm.id = role.id
  roleForm.roleName = role.roleName
  roleForm.roleCode = role.roleCode
  roleForm.description = role.description || ''
  roleForm.status = role.status
  roleDialog.visible = true
}

// 保存角色
const saveRole = async () => {
  await roleFormRef.value?.validate()
  
  roleDialog.loading = true
  try {
    if (roleDialog.mode === 'add') {
      await store.addRole({
        roleName: roleForm.roleName,
        roleCode: roleForm.roleCode,
        description: roleForm.description,
        status: roleForm.status
      })
    } else if (roleForm.id) {
      await store.updateRole(roleForm.id, {
        id: roleForm.id,
        roleName: roleForm.roleName,
        roleCode: roleForm.roleCode,
        description: roleForm.description,
        status: roleForm.status
      })
    }
    roleDialog.visible = false
  } catch (error) {
    console.error('保存角色失败:', error)
  } finally {
    roleDialog.loading = false
  }
}

// 删除角色
const handleDeleteRole = async (roleId: number) => {
  try {
    await store.deleteRole(roleId)
    if (currentRole.value?.id === roleId) {
      currentRole.value = null
    }
  } catch (error) {
    console.error('删除角色失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.role-permission-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding:20px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    .header-left {
      .page-title {
        margin: 10px 0 0 0;
        font-size: 20px;
        font-weight: 600;
        color: #303133;
      }
    }
  }

  .role-card {
    height: 600px;
    display: flex;
    flex-direction: column;

    :deep(.el-card__body) {
      flex: 1;
      overflow-y: auto;
      padding: 0;
    }

    .role-list {
      .role-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 12px 20px;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background-color: #f5f7fa;
        }

        &.active {
          background-color: #ecf5ff;
          border-left: 3px solid #409eff;
        }

        .role-info {
          flex: 1;
          display: flex;
          align-items: center;
          gap: 8px;
          min-width: 0;

          .role-name {
            font-weight: 500;
            color: #303133;
          }

          .role-code {
            font-size: 12px;
            color: #909399;
            background-color: #f0f0f0;
            padding: 2px 6px;
            border-radius: 4px;
          }

          .role-status {
            font-size: 11px;
          }
        }

        .role-actions {
          display: flex;
          align-items: center;
          gap: 8px;
          opacity: 0.3;
          transition: opacity 0.3s;

          .el-button {
            font-size: 16px;
          }
        }

        &:hover .role-actions {
          opacity: 1;
        }
      }
    }
  }

  .permission-card {
    height: 600px;
    display: flex;
    flex-direction: column;

    :deep(.el-card__header) {
      flex-shrink: 0;
    }

    :deep(.el-card__body) {
      flex: 1;
      overflow-y: auto;
      padding: 20px;
    }

    .permission-tree-container {
      height: 100%;
      overflow-y: auto;
    }
  }

  .empty-card {
    height: 600px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
    }

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}

:deep(.el-tree) {
  background-color: transparent;

  .el-tree-node__content {
    height: 36px;
    margin: 2px 0;
  }

  .el-tree-node.is-current > .el-tree-node__content {
    background-color: #ecf5ff;
  }
}
</style>