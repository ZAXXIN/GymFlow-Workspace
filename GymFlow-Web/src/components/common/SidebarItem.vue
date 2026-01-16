<template>
  <div v-if="!item.meta?.hidden">
    <!-- 没有子菜单的情况 -->
    <el-menu-item
      v-if="!hasChildren"
      :index="resolvePath(item.path)"
      @click="handleClick"
    >
      <el-icon v-if="item.meta?.icon">
        <component :is="item.meta.icon" />
      </el-icon>
      <span>{{ item.meta?.title }}</span>
    </el-menu-item>

    <!-- 有子菜单的情况 -->
    <el-sub-menu
      v-else
      :index="resolvePath(item.path)"
      popper-append-to-body
    >
      <template #title>
        <el-icon v-if="item.meta?.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta?.title }}</span>
      </template>
      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(item.path)"
        :level="level + 1"
      />
    </el-sub-menu>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

interface Props {
  item: RouteRecordRaw
  basePath?: string
  level?: number
}

const props = withDefaults(defineProps<Props>(), {
  basePath: '',
  level: 0
})

const router = useRouter()

// 计算是否有子菜单
const hasChildren = computed(() => {
  const children = props.item.children
  return children && children.length > 0
})

// 解析完整路径
const resolvePath = (routePath: string) => {
  if (routePath.startsWith('/')) {
    return routePath
  }
  if (props.basePath.endsWith('/')) {
    return `${props.basePath}${routePath}`
  }
  return `${props.basePath}/${routePath}`
}

// 处理菜单点击
const handleClick = () => {
  if (props.item.meta?.fullPath) {
    router.push(props.item.meta.fullPath)
  } else {
    router.push(resolvePath(props.item.path))
  }
}
</script>

<style scoped lang="scss">
.el-menu-item {
  display: flex;
  align-items: center;
  
  .el-icon {
    margin-right: 8px;
  }
}

.el-sub-menu {
  .el-menu-item {
    padding-left: 48px !important;
  }
  
  .el-sub-menu__title {
    display: flex;
    align-items: center;
    
    .el-icon {
      margin-right: 8px;
    }
  }
}

// 二级菜单缩进
:deep(.el-menu--vertical) {
  .el-menu-item {
    padding-left: 48px !important;
  }
}

// 选中状态
.el-menu-item.is-active {
  color: var(--el-color-primary);
  background-color: rgba(var(--el-color-primary-rgb), 0.1);
  
  &::after {
    content: '';
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background-color: var(--el-color-primary);
  }
}
</style>