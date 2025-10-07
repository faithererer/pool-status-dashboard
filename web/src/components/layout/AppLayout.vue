<template>
  <div class="app-layout">
    <AppHeader />
    <main class="main-content">
      <slot />
    </main>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import AppHeader from './AppHeader.vue'

export default defineComponent({
  name: 'AppLayout',
  components: {
    AppHeader
  }
})
</script>

<style lang="scss" scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}
</style>