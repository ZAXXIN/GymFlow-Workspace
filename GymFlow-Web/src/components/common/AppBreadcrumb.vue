<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="item.path">
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
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const breadcrumbs = ref<any[]>([])

// 获取面包屑数据
const getBreadcrumb = () => {
  // 只显示带有标题的路由
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  
  const first = matched[0]
  
  // 如果不是首页，添加首页
  if (first && first.path !== '/dashboard') {
    matched.unshift({
      path: '/dashboard',
      meta: { title: '首页' }
    } as any)
  }
  
  breadcrumbs.value = matched
}

// 处理链接点击
const handleLink = (item: any) => {
  const { redirect, path } = item
  if (redirect) {
    router.push(redirect)
    return
  }
  router.push(path)
}

// 监听路由变化
watch(
  () => route.path,
  () => {
    getBreadcrumb()
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.app-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;
  margin-left: 8px;
  
  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
  
  a {
    color: var(--el-text-color-regular);
    text-decoration: none;
    transition: color 0.3s;
    
    &:hover {
      color: var(--el-color-primary);
    }
  }
}
</style>