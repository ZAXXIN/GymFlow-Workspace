<template>
  <footer class="app-footer" v-if="showFooter">
    <div class="footer-content">
      <!-- 版权信息 -->
      <div class="copyright">
        Copyright &copy; {{ currentYear }} GymFlow 健身工作室管理系统
      </div>
      
      <!-- 备案信息 -->
      <div class="record" v-if="recordInfo">
        <a :href="recordInfo.url" target="_blank" class="record-link">
          {{ recordInfo.text }}
        </a>
      </div>
      
      <!-- 系统信息 -->
      <div class="system-info">
        <span class="version">v{{ version }}</span>
        <span class="separator">|</span>
        <span class="build-time">{{ buildTime }}</span>
      </div>
      
      <!-- 快捷链接 -->
      <div class="quick-links">
        <a href="#" class="link-item">帮助中心</a>
        <span class="separator">|</span>
        <a href="#" class="link-item">使用协议</a>
        <span class="separator">|</span>
        <a href="#" class="link-item">隐私政策</a>
        <span class="separator">|</span>
        <a href="#" class="link-item">联系我们</a>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useSettingsStore } from '@/stores/settings'

const settingsStore = useSettingsStore()

// 响应式数据
const currentYear = computed(() => new Date().getFullYear())
const version = computed(() => settingsStore.systemInfo.version)
const buildTime = computed(() => settingsStore.systemInfo.buildTime)

// 备案信息
const recordInfo = ref({
  text: '京ICP备2021000000号',
  url: 'https://beian.miit.gov.cn/'
})

// 是否显示页脚
const showFooter = computed(() => settingsStore.showFooter)
</script>

<style scoped lang="scss">
.app-footer {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: rgba(255, 255, 255, 0.8);
  padding: 16px 20px;
  font-size: 14px;
  
  .footer-content {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    
    .copyright {
      font-size: 14px;
    }
    
    .record {
      .record-link {
        color: rgba(255, 255, 255, 0.8);
        text-decoration: none;
        transition: color 0.3s;
        
        &:hover {
          color: #ffffff;
          text-decoration: underline;
        }
      }
    }
    
    .system-info {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .separator {
        color: rgba(255, 255, 255, 0.5);
      }
      
      .version,
      .build-time {
        font-family: monospace;
      }
    }
    
    .quick-links {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-top: 4px;
      
      .link-item {
        color: rgba(255, 255, 255, 0.8);
        text-decoration: none;
        transition: color 0.3s;
        
        &:hover {
          color: #ffffff;
          text-decoration: underline;
        }
      }
      
      .separator {
        color: rgba(255, 255, 255, 0.5);
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .app-footer {
    padding: 12px 10px;
    font-size: 12px;
    
    .footer-content {
      .quick-links {
        flex-wrap: wrap;
        justify-content: center;
        gap: 8px;
      }
    }
  }
}
</style>