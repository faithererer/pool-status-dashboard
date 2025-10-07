<template>
  <Teleport to="body">
    <Transition name="toast" appear>
      <div
        v-if="visible"
        class="toast-container"
        :class="[`toast-${type}`, positionClass]"
        @click="handleClick"
      >
        <div class="toast-content">
          <div class="toast-icon">
            <!-- 成功图标 -->
            <svg v-if="type === 'success'" width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
            
            <!-- 错误图标 -->
            <svg v-else-if="type === 'error'" width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
            
            <!-- 警告图标 -->
            <svg v-else-if="type === 'warning'" width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
            </svg>
            
            <!-- 信息图标 -->
            <svg v-else width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
            </svg>
          </div>
          
          <div class="toast-body">
            <div v-if="title" class="toast-title">{{ title }}</div>
            <div class="toast-message">{{ message }}</div>
          </div>
          
          <button
            v-if="closable"
            class="toast-close"
            @click.stop="close"
            :title="'关闭'"
          >
            <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>
        </div>
        
        <!-- 进度条 -->
        <div v-if="showProgress && duration > 0" class="toast-progress">
          <div class="toast-progress-bar" :style="{ animationDuration: `${duration}ms` }"></div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script>
import { defineComponent, ref, onMounted, onUnmounted } from 'vue'

export default defineComponent({
  name: 'BaseToast',
  props: {
    type: {
      type: String,
      default: 'info',
      validator: (value) => ['success', 'error', 'warning', 'info'].includes(value)
    },
    title: {
      type: String,
      default: ''
    },
    message: {
      type: String,
      required: true
    },
    duration: {
      type: Number,
      default: 4000
    },
    position: {
      type: String,
      default: 'top-right',
      validator: (value) => [
        'top-left', 'top-center', 'top-right',
        'bottom-left', 'bottom-center', 'bottom-right'
      ].includes(value)
    },
    closable: {
      type: Boolean,
      default: true
    },
    showProgress: {
      type: Boolean,
      default: true
    },
    onClick: {
      type: Function,
      default: null
    }
  },
  emits: ['close'],
  setup(props, { emit }) {
    const visible = ref(true)
    let timer = null

    const positionClass = `toast-${props.position}`

    const close = () => {
      visible.value = false
      if (timer) {
        clearTimeout(timer)
        timer = null
      }
      emit('close')
    }

    const handleClick = () => {
      if (props.onClick) {
        props.onClick()
      }
    }

    onMounted(() => {
      if (props.duration > 0) {
        timer = setTimeout(() => {
          close()
        }, props.duration)
      }
    })

    onUnmounted(() => {
      if (timer) {
        clearTimeout(timer)
      }
    })

    return {
      visible,
      positionClass,
      close,
      handleClick
    }
  }
})
</script>

<style lang="scss" scoped>
.toast-container {
  position: fixed;
  z-index: 9999;
  max-width: 400px;
  min-width: 300px;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $radius-lg;
  box-shadow: $shadow-xl;
  backdrop-filter: blur(20px);
  overflow: hidden;
  cursor: pointer;
  
  // 位置定位
  &.toast-top-left {
    top: $spacing-6;
    left: $spacing-6;
  }
  
  &.toast-top-center {
    top: $spacing-6;
    left: 50%;
    transform: translateX(-50%);
  }
  
  &.toast-top-right {
    top: $spacing-6;
    right: $spacing-6;
  }
  
  &.toast-bottom-left {
    bottom: $spacing-6;
    left: $spacing-6;
  }
  
  &.toast-bottom-center {
    bottom: $spacing-6;
    left: 50%;
    transform: translateX(-50%);
  }
  
  &.toast-bottom-right {
    bottom: $spacing-6;
    right: $spacing-6;
  }
  
  // 类型样式
  &.toast-success {
    border-left: 4px solid $green-500;
    
    .toast-icon {
      color: $green-500;
    }
  }
  
  &.toast-error {
    border-left: 4px solid $red-500;
    
    .toast-icon {
      color: $red-500;
    }
  }
  
  &.toast-warning {
    border-left: 4px solid $yellow-500;
    
    .toast-icon {
      color: $yellow-500;
    }
  }
  
  &.toast-info {
    border-left: 4px solid $blue-500;
    
    .toast-icon {
      color: $blue-500;
    }
  }
}

.toast-content {
  display: flex;
  align-items: flex-start;
  gap: $spacing-3;
  padding: $spacing-4;
}

.toast-icon {
  flex-shrink: 0;
  margin-top: 2px;
}

.toast-body {
  flex: 1;
  min-width: 0;
}

.toast-title {
  font-weight: $font-weight-semibold;
  color: $gray-50;
  margin-bottom: $spacing-1;
  font-size: $font-size-sm;
}

.toast-message {
  color: $gray-300;
  font-size: $font-size-sm;
  line-height: 1.5;
  word-break: break-word;
}

.toast-close {
  flex-shrink: 0;
  background: transparent;
  border: none;
  color: $gray-400;
  cursor: pointer;
  padding: $spacing-1;
  border-radius: $radius-md;
  transition: all $transition-normal;
  
  &:hover {
    background: $bg-card-hover;
    color: $gray-300;
  }
}

.toast-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: rgba($gray-600, 0.3);
  overflow: hidden;
}

.toast-progress-bar {
  height: 100%;
  background: currentColor;
  width: 100%;
  transform: translateX(-100%);
  animation: toast-progress linear forwards;
}

// 动画
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
  
  .toast-container.toast-top-left &,
  .toast-container.toast-bottom-left & {
    transform: translateX(-100%);
  }
  
  .toast-container.toast-top-center &,
  .toast-container.toast-bottom-center & {
    transform: translateX(-50%) translateY(-20px);
  }
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
  
  .toast-container.toast-top-left &,
  .toast-container.toast-bottom-left & {
    transform: translateX(-100%);
  }
  
  .toast-container.toast-top-center &,
  .toast-container.toast-bottom-center & {
    transform: translateX(-50%) translateY(-20px);
  }
}

@keyframes toast-progress {
  from {
    transform: translateX(-100%);
  }
  to {
    transform: translateX(0);
  }
}

// 响应式设计
@media (max-width: $breakpoint-sm) {
  .toast-container {
    max-width: calc(100vw - #{$spacing-4 * 2});
    min-width: auto;
    
    &.toast-top-left,
    &.toast-top-right,
    &.toast-bottom-left,
    &.toast-bottom-right {
      left: $spacing-4;
      right: $spacing-4;
      transform: none;
    }
    
    &.toast-top-center,
    &.toast-bottom-center {
      left: $spacing-4;
      right: $spacing-4;
      transform: none;
    }
  }
}
</style>