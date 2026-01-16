<template>
  <div class="app-card" :class="cardClasses" :style="cardStyles">
    <!-- 卡片头部 -->
    <div v-if="showHeader" class="card-header" :class="{ 'with-border': headerBorder }">
      <div class="header-left">
        <slot name="header-left">
          <div v-if="icon || $slots.icon" class="header-icon">
            <slot name="icon">
              <el-icon v-if="icon" :size="iconSize" :color="iconColor">
                <component :is="icon" />
              </el-icon>
            </slot>
          </div>
          <div class="header-content">
            <div v-if="title" class="card-title">
              <h3 class="title-text">{{ title }}</h3>
              <el-tag v-if="tag" :type="tagType" size="small" class="title-tag">
                {{ tag }}
              </el-tag>
              <el-badge v-if="badge" :value="badge" :type="badgeType" class="title-badge" />
            </div>
            <div v-if="description" class="card-description">{{ description }}</div>
            <div v-if="extra" class="card-extra">{{ extra }}</div>
          </div>
        </slot>
      </div>
      <div v-if="$slots.header || showActions" class="header-right">
        <slot name="header">
          <div v-if="showActions" class="header-actions">
            <el-dropdown v-if="actionMenu?.length > 0" @command="handleActionMenu">
              <el-button size="small" text>
                更多
                <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="action in actionMenu"
                    :key="action.key"
                    :command="action.key"
                    :divided="action.divided"
                    :disabled="action.disabled"
                  >
                    <div class="dropdown-item">
                      <el-icon v-if="action.icon"><component :is="action.icon" /></el-icon>
                      <span>{{ action.label }}</span>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button
              v-if="showRefresh"
              size="small"
              text
              @click="handleRefresh"
            >
              <el-icon><Refresh /></el-icon>
            </el-button>
            <el-button
              v-if="showFullscreen"
              size="small"
              text
              @click="toggleFullscreen"
            >
              <el-icon><FullScreen /></el-icon>
            </el-button>
            <el-button
              v-if="showClose"
              size="small"
              text
              @click="handleClose"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </slot>
      </div>
    </div>

    <!-- 卡片主体 -->
    <div class="card-body" :class="{ 'loading': loading, 'with-padding': bodyPadding }">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="skeletonRows" animated />
      </div>
      <div v-else-if="empty" class="empty-state">
        <slot name="empty">
          <el-empty :description="emptyText" :image-size="emptySize" />
        </slot>
      </div>
      <slot v-else />
    </div>

    <!-- 卡片底部 -->
    <div v-if="showFooter" class="card-footer" :class="{ 'with-border': footerBorder }">
      <slot name="footer">
        <div v-if="footerLeft || $slots['footer-left']" class="footer-left">
          <slot name="footer-left">
            <div v-if="footerLeft" class="footer-text">{{ footerLeft }}</div>
          </slot>
        </div>
        <div v-if="footerRight || $slots['footer-right']" class="footer-right">
          <slot name="footer-right">
            <div v-if="footerRight" class="footer-text">{{ footerRight }}</div>
          </slot>
        </div>
      </slot>
    </div>

    <!-- 全屏模式 -->
    <div v-if="fullscreen" class="card-fullscreen">
      <div class="fullscreen-header">
        <h3 class="fullscreen-title">{{ fullscreenTitle || title }}</h3>
        <el-button size="small" text @click="toggleFullscreen">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      <div class="fullscreen-body">
        <slot v-if="!loading && !empty" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ArrowDown,
  Refresh,
  FullScreen,
  Close
} from '@element-plus/icons-vue'

interface ActionMenuItem {
  key: string
  label: string
  icon?: any
  divided?: boolean
  disabled?: boolean
}

interface CardProps {
  // 基础配置
  title?: string
  description?: string
  extra?: string
  tag?: string
  tagType?: string
  badge?: string | number
  badgeType?: string
  // 图标配置
  icon?: any
  iconSize?: number | string
  iconColor?: string
  // 布局配置
  width?: string
  height?: string
  minHeight?: string
  maxHeight?: string
  shadow?: 'always' | 'hover' | 'never'
  border?: boolean
  radius?: string
  // 显示控制
  showHeader?: boolean
  showFooter?: boolean
  showActions?: boolean
  showRefresh?: boolean
  showFullscreen?: boolean
  showClose?: boolean
  headerBorder?: boolean
  footerBorder?: boolean
  bodyPadding?: boolean
  // 状态控制
  loading?: boolean
  skeletonRows?: number
  empty?: boolean
  emptyText?: string
  emptySize?: number
  fullscreen?: boolean
  fullscreenTitle?: string
  // 内容配置
  footerLeft?: string
  footerRight?: string
  actionMenu?: ActionMenuItem[]
  // 样式配置
  backgroundColor?: string
  headerBackground?: string
  bodyBackground?: string
  footerBackground?: string
}

const props = withDefaults(defineProps<CardProps>(), {
  title: '',
  description: '',
  extra: '',
  tag: '',
  tagType: 'info',
  badge: '',
  badgeType: 'danger',
  icon: undefined,
  iconSize: 20,
  iconColor: '#409eff',
  width: '100%',
  height: 'auto',
  minHeight: 'auto',
  maxHeight: 'none',
  shadow: 'hover',
  border: true,
  radius: '8px',
  showHeader: true,
  showFooter: false,
  showActions: true,
  showRefresh: true,
  showFullscreen: true,
  showClose: false,
  headerBorder: true,
  footerBorder: true,
  bodyPadding: true,
  loading: false,
  skeletonRows: 4,
  empty: false,
  emptyText: '暂无数据',
  emptySize: 120,
  fullscreen: false,
  fullscreenTitle: '',
  footerLeft: '',
  footerRight: '',
  actionMenu: () => [],
  backgroundColor: '#fff',
  headerBackground: '#fff',
  bodyBackground: '#fff',
  footerBackground: '#fafafa'
})

const emit = defineEmits<{
  'refresh': []
  'close': []
  'fullscreen-change': [fullscreen: boolean]
  'action-menu': [action: string]
}>()

const fullscreen = ref(props.fullscreen)

// 计算卡片类名
const cardClasses = computed(() => {
  return {
    'has-shadow': props.shadow === 'always',
    'has-hover-shadow': props.shadow === 'hover',
    'has-border': props.border,
    'is-fullscreen': fullscreen.value
  }
})

// 计算卡片样式
const cardStyles = computed(() => {
  return {
    width: props.width,
    height: fullscreen.value ? '100%' : props.height,
    minHeight: props.minHeight,
    maxHeight: props.maxHeight,
    borderRadius: props.radius,
    backgroundColor: props.backgroundColor
  }
})

// 处理刷新
const handleRefresh = () => {
  emit('refresh')
}

// 处理关闭
const handleClose = () => {
  emit('close')
}

// 切换全屏
const toggleFullscreen = () => {
  fullscreen.value = !fullscreen.value
  emit('fullscreen-change', fullscreen.value)
}

// 处理动作菜单
const handleActionMenu = (action: string) => {
  emit('action-menu', action)
}

// 暴露的方法
defineExpose({
  toggleFullscreen,
  setLoading: (loading: boolean) => {
    props.loading = loading
  },
  setEmpty: (empty: boolean, text?: string) => {
    props.empty = empty
    if (text) {
      props.emptyText = text
    }
  }
})
</script>

<style scoped lang="scss">
.app-card {
  position: relative;
  transition: all 0.3s;
  overflow: hidden;
  box-sizing: border-box;

  // 阴影样式
  &.has-shadow {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

  &.has-hover-shadow {
    &:hover {
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    }
  }

  // 边框样式
  &.has-border {
    border: 1px solid #ebeef5;
  }

  // 全屏样式
  &.is-fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
    border-radius: 0;
    border: none;
    background: #fff;

    .card-header,
    .card-body,
    .card-footer {
      display: none;
    }

    .card-fullscreen {
      display: flex;
      flex-direction: column;
      height: 100%;
    }
  }

  // 卡片头部
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: v-bind('props.headerBackground');
    transition: all 0.3s;

    &.with-border {
      border-bottom: 1px solid #ebeef5;
    }

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
      flex: 1;
      min-width: 0;

      .header-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        height: 40px;
        border-radius: 8px;
        background: #f0f9ff;
        flex-shrink: 0;
      }

      .header-content {
        flex: 1;
        min-width: 0;

        .card-title {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 4px;

          .title-text {
            margin: 0;
            font-size: 16px;
            font-weight: 600;
            color: #303133;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .title-tag {
            flex-shrink: 0;
          }

          .title-badge {
            flex-shrink: 0;
          }
        }

        .card-description {
          font-size: 14px;
          color: #606266;
          margin-bottom: 2px;
          line-height: 1.4;
        }

        .card-extra {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .header-right {
      flex-shrink: 0;

      .header-actions {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  // 卡片主体
  .card-body {
    background: v-bind('props.bodyBackground');
    transition: all 0.3s;
    position: relative;

    &.with-padding {
      padding: 20px;
    }

    &.loading,
    &.empty {
      min-height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .loading-state {
      width: 100%;
      padding: 20px;
    }

    .empty-state {
      width: 100%;
    }
  }

  // 卡片底部
  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    background: v-bind('props.footerBackground');
    transition: all 0.3s;

    &.with-border {
      border-top: 1px solid #ebeef5;
    }

    .footer-left,
    .footer-right {
      .footer-text {
        font-size: 14px;
        color: #606266;
      }
    }

    .footer-left {
      flex: 1;
    }

    .footer-right {
      flex-shrink: 0;
    }
  }

  // 全屏模式
  .card-fullscreen {
    display: none;

    .fullscreen-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 24px;
      border-bottom: 1px solid #ebeef5;
      background: #fff;

      .fullscreen-title {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }

    .fullscreen-body {
      flex: 1;
      padding: 24px;
      overflow: auto;
    }
  }
}

// 下拉菜单项样式
.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    font-size: 16px;
  }
}

// 响应式适配
@media (max-width: 768px) {
  .app-card {
    .card-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;

      .header-right {
        width: 100%;
        justify-content: flex-end;
      }
    }

    .card-footer {
      flex-direction: column;
      gap: 8px;
      align-items: flex-start;

      .footer-left,
      .footer-right {
        width: 100%;
      }
    }
  }
}
</style>