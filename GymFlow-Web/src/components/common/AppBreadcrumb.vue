<template>
  <el-breadcrumb separator="/" class="app-breadcrumb">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item
        v-for="(item, index) in breadcrumbs"
        :key="item.path"
      >
        <span
          v-if="item.redirect === 'noRedirect' || index === breadcrumbs.length - 1"
          class="no-redirect"
        >
          {{ item.meta?.title }}
        </span>
        <a v-else @click.prevent="handleLink(item)">
          {{ item.meta?.title }}
        </a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter, type RouteLocationMatched } from 'vue-router'
import { compile } from 'path-to-regexp'

const route = useRoute()
const router = useRouter()

// 计算面包屑数据
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  const first = matched[0]
  
  // 如果不是首页，添加首页面包屑
  if (first && first.path !== '/dashboard') {
    return [
      {
        path: '/dashboard',
        meta: { title: '首页' }
      } as RouteLocationMatched
    ].concat(matched)
  }
  
  return matched
})

// 处理链接点击
const handleLink = (item: RouteLocationMatched) => {
  const { path, redirect } = item
  if (redirect) {
    router.push(redirect as string)
    return
  }
  
  // 处理动态路由参数
  const { params } = route
  let toPath = ''
  
  try {
    toPath = compile(path)(params)
  } catch (err) {
    console.error('路径编译错误:', err)
    return
  }
  
  router.push(toPath)
}
</script>

<style scoped lang="scss">
.app-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;
  margin-left: 8px;

  :deep(.el-breadcrumb__separator) {
    font-weight: normal;
    color: #c0c4cc;
  }

  :deep(.el-breadcrumb__inner) {
    font-weight: normal;
    
    &.is-link {
      color: #606266;
      transition: color 0.3s;
      
      &:hover {
        color: #409eff;
        cursor: pointer;
      }
    }
    
    .no-redirect {
      color: #97a8be;
      cursor: text;
    }
  }
}

.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.5s;
}

.breadcrumb-enter-from,
.breadcrumb-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.breadcrumb-leave-active {
  position: absolute;
}
</style>