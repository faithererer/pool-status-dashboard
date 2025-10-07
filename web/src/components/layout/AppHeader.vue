<template>
  <header class="header fade-in">
    <div class="container">
      <div class="header-content">
        <div class="header-left">
          <h1 class="title glow">号池监控面板</h1>
          <p class="subtitle">实时监控您的服务号池状态和压力情况</p>
        </div>
        <div class="header-right">
          <nav class="nav-buttons" v-if="isAuthenticated">
            <router-link
              to="/dashboard"
              class="nav-btn"
              :class="{ active: $route.path === '/dashboard' }"
            >
              监控面板
            </router-link>
            <router-link
              to="/management"
              class="nav-btn"
              :class="{ active: $route.path.startsWith('/management') }"
            >
              管理面板
            </router-link>
          </nav>
          
          <div class="header-actions">
            <!-- 主题切换按钮 -->
            <BaseButton
              variant="ghost"
              size="small"
              @click="toggleTheme"
              class="theme-toggle"
              :title="isDark ? '切换到亮色主题' : '切换到暗色主题'"
            >
              <svg v-if="isDark" width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4 8a4 4 0 11-8 0 4 4 0 018 0zm-.464 4.95l.707.707a1 1 0 001.414-1.414l-.707-.707a1 1 0 00-1.414 1.414zm2.12-10.607a1 1 0 010 1.414l-.706.707a1 1 0 11-1.414-1.414l.707-.707a1 1 0 011.414 0zM17 11a1 1 0 100-2h-1a1 1 0 100 2h1zm-7 4a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zM5.05 6.464A1 1 0 106.465 5.05l-.708-.707a1 1 0 00-1.414 1.414l.707.707zm1.414 8.486l-.707.707a1 1 0 01-1.414-1.414l.707-.707a1 1 0 011.414 1.414zM4 11a1 1 0 100-2H3a1 1 0 000 2h1z" clip-rule="evenodd" />
              </svg>
              <svg v-else width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M17.293 13.293A8 8 0 0 1 6.707 2.707a8.001 8.001 0 1 0 10.586 10.586z" />
              </svg>
            </BaseButton>
            
            <!-- 用户菜单 -->
            <div class="user-menu" v-if="isAuthenticated">
              <BaseButton
                variant="ghost"
                size="small"
                @click="toggleUserDropdown"
                class="user-button"
                ref="userButton"
              >
                <div class="user-avatar">
                  <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
                  </svg>
                </div>
                <span class="user-name">{{ user?.username || '用户' }}</span>
                <svg class="dropdown-icon" :class="{ 'rotate': showUserDropdown }" width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
                </svg>
              </BaseButton>
              
              <!-- 下拉菜单 -->
              <div v-if="showUserDropdown" class="user-dropdown" ref="userDropdown">
                <div class="dropdown-header">
                  <div class="user-info">
                    <div class="user-name">{{ user?.username }}</div>
                    <div class="user-role">管理员</div>
                  </div>
                </div>
                <div class="dropdown-divider"></div>
                <div class="dropdown-menu">
                  <button @click="handleSettings" class="dropdown-item">
                    <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clip-rule="evenodd" />
                    </svg>
                    系统设置
                  </button>
                  <div class="dropdown-divider"></div>
                  <button @click="handleLogout" class="dropdown-item logout">
                    <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" clip-rule="evenodd" />
                    </svg>
                    退出登录
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script>
import { defineComponent, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import { useAuth } from '@/composables/useAuth'

export default defineComponent({
  name: 'AppHeader',
  setup() {
    const router = useRouter()
    const { isDark, toggleTheme } = useTheme()
    const { isAuthenticated, user, logout } = useAuth()

    // 用户下拉菜单状态
    const showUserDropdown = ref(false)
    const userButton = ref(null)
    const userDropdown = ref(null)

    // 切换用户下拉菜单
    const toggleUserDropdown = () => {
      showUserDropdown.value = !showUserDropdown.value
    }


    // 处理系统设置
    const handleSettings = () => {
      showUserDropdown.value = false
      router.push('/management/config')
    }

    // 处理退出登录
    const handleLogout = async () => {
      showUserDropdown.value = false
      try {
        await logout()
        router.push('/login')
      } catch (error) {
        console.error('退出登录失败:', error)
      }
    }

    // 点击外部关闭下拉菜单
    const handleClickOutside = (event) => {
      if (showUserDropdown.value &&
          userButton.value &&
          userDropdown.value &&
          !userButton.value.$el.contains(event.target) &&
          !userDropdown.value.contains(event.target)) {
        showUserDropdown.value = false
      }
    }

    onMounted(() => {
      document.addEventListener('click', handleClickOutside)
    })

    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
    })
    
    return {
      isDark,
      toggleTheme,
      isAuthenticated,
      user,
      showUserDropdown,
      userButton,
      userDropdown,
      toggleUserDropdown,
      handleSettings,
      handleLogout
    }
  }
})
</script>

<style lang="scss" scoped>
.header {
  position: relative;
  z-index: 100;
  flex-shrink: 0;
  border-bottom: 1px solid $border-color;
  backdrop-filter: blur(10px);
  background: $bg-card;
}

.container {
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 $spacing-5;
  height: auto; // 覆盖全局的 height: 100vh
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-4 0;
}

.header-left {
  .title {
    font-size: $font-size-3xl;
    font-weight: $font-weight-bold;
    background: $primary-gradient;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: $spacing-2;
    
    &.glow {
      animation: glow 2s ease-in-out infinite alternate;
    }
  }
  
  .subtitle {
    color: $gray-400;
    font-size: $font-size-base;
    margin: 0;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: $spacing-4;
}

.nav-buttons {
  display: flex;
  gap: $spacing-4;
}

.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: $radius-lg;
  transition: all $transition-normal;
  
  &:hover {
    background: $bg-card-hover;
    transform: scale(1.05);
  }
  
  svg {
    transition: transform $transition-normal;
  }
  
  &:hover svg {
    transform: rotate(15deg);
  }
}

.nav-btn {
  padding: $spacing-3 $spacing-5;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $radius-lg;
  color: $gray-50;
  text-decoration: none;
  font-weight: $font-weight-medium;
  transition: all $transition-normal;
  backdrop-filter: blur(10px);
  
  &:hover {
    background: $bg-card-hover;
    border-color: $border-active;
    transform: translateY(-2px);
  }
  
  &.active {
    background: $primary-gradient;
    border-color: transparent;
    color: $white;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.user-menu {
  position: relative;
}

.user-button {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-2 $spacing-3;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $radius-lg;
  color: $gray-50;
  font-weight: $font-weight-medium;
  transition: all $transition-normal;
  backdrop-filter: blur(10px);
  
  &:hover {
    background: $bg-card-hover;
    border-color: $border-active;
  }
  
  .user-avatar {
    width: 24px;
    height: 24px;
    background: $primary-gradient;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    svg {
      width: 14px;
      height: 14px;
      color: $white;
    }
  }
  
  .user-name {
    font-size: $font-size-sm;
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .dropdown-icon {
    width: 16px;
    height: 16px;
    transition: transform $transition-normal;
    
    &.rotate {
      transform: rotate(180deg);
    }
  }
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 200px;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $radius-lg;
  box-shadow: $shadow-lg;
  backdrop-filter: blur(20px);
  z-index: 1000;
  animation: fadeInDown 0.2s ease-out;
}

.dropdown-header {
  padding: $spacing-4;
  border-bottom: 1px solid $border-color;
  
  .user-info {
    .user-name {
      font-weight: $font-weight-semibold;
      color: $gray-50;
      margin-bottom: $spacing-1;
    }
    
    .user-role {
      font-size: $font-size-sm;
      color: $gray-400;
    }
  }
}

.dropdown-divider {
  height: 1px;
  background: $border-color;
  margin: $spacing-2 0;
}

.dropdown-menu {
  padding: $spacing-2;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  width: 100%;
  padding: $spacing-3;
  background: transparent;
  border: none;
  border-radius: $radius-md;
  color: $gray-300;
  font-size: $font-size-sm;
  text-align: left;
  cursor: pointer;
  transition: all $transition-normal;
  
  &:hover {
    background: $bg-card-hover;
    color: $gray-50;
  }
  
  &.logout {
    color: $red-400;
    
    &:hover {
      background: rgba($red-500, 0.1);
      color: $red-300;
    }
  }
  
  svg {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes glow {
  from {
    filter: drop-shadow(0 0 5px rgba(59, 130, 246, 0.5));
  }
  to {
    filter: drop-shadow(0 0 20px rgba(59, 130, 246, 0.8));
  }
}

// 响应式设计
@media (max-width: $breakpoint-md) {
  .header-content {
    flex-direction: column;
    gap: $spacing-4;
    text-align: center;
  }
  
  .header-left .title {
    font-size: $font-size-2xl;
  }
  
  .header-right {
    flex-direction: column;
    gap: $spacing-3;
  }
  
  .nav-buttons {
    justify-content: center;
  }
}

@media (max-width: $breakpoint-sm) {
  .container {
    padding: 0 $spacing-4;
  }
  
  .nav-btn {
    padding: $spacing-2 $spacing-4;
    font-size: $font-size-sm;
  }
}
</style>