<template>
  <div id="app">
    <!-- 根据路由元信息决定是否显示布局 -->
    <AppLayout v-if="!$route.meta.hideLayout">

      <router-view />
    </AppLayout>
    <router-view v-else />
    
    <!-- Toast容器 -->
    <div id="toast-container"></div>
  </div>
</template>

<script>
import { defineComponent, onMounted } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import { useTheme } from '@/composables/useTheme'

export default defineComponent({
  name: 'App',
  components: {
    AppLayout
  },
  setup() {
    const { loadTheme, applyTheme } = useTheme()
    
    onMounted(() => {
      // 初始化主题
      loadTheme()
      applyTheme()
    })
    
    return {}
  }
})
</script>

<style lang="scss">
// 全局样式已在 main.scss 中定义
#app {
  min-height: 100vh;
  width: 100%;
}
</style>