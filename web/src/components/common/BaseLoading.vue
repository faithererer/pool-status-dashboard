<template>
  <div :class="loadingClasses">
    <div class="loading__spinner">
      <div v-if="type === 'spinner'" class="spinner">
        <div class="spinner__circle"></div>
      </div>
      
      <div v-else-if="type === 'dots'" class="dots">
        <div class="dots__dot"></div>
        <div class="dots__dot"></div>
        <div class="dots__dot"></div>
      </div>
      
      <div v-else-if="type === 'pulse'" class="pulse">
        <div class="pulse__circle"></div>
      </div>
      
      <div v-else-if="type === 'bars'" class="bars">
        <div class="bars__bar"></div>
        <div class="bars__bar"></div>
        <div class="bars__bar"></div>
        <div class="bars__bar"></div>
      </div>
    </div>
    
    <div v-if="text" class="loading__text">
      {{ text }}
    </div>
  </div>
</template>

<script>
import { defineComponent, computed } from 'vue'

export default defineComponent({
  name: 'BaseLoading',
  props: {
    type: {
      type: String,
      default: 'spinner',
      validator: (value) => ['spinner', 'dots', 'pulse', 'bars'].includes(value)
    },
    size: {
      type: String,
      default: 'medium',
      validator: (value) => ['small', 'medium', 'large'].includes(value)
    },
    text: {
      type: String,
      default: ''
    },
    overlay: {
      type: Boolean,
      default: false
    },
    color: {
      type: String,
      default: 'primary'
    }
  },
  setup(props) {
    const loadingClasses = computed(() => [
      'loading',
      `loading--${props.size}`,
      `loading--${props.color}`,
      {
        'loading--overlay': props.overlay
      }
    ])

    return {
      loadingClasses
    }
  }
})
</script>

<style lang="scss" scoped>
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-3;
  
  &--overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(2px);
    z-index: 9999;
  }
  
  // 尺寸变体
  &--small {
    .loading__spinner {
      width: 20px;
      height: 20px;
    }
    
    .loading__text {
      font-size: $font-size-sm;
    }
  }
  
  &--medium {
    .loading__spinner {
      width: 32px;
      height: 32px;
    }
    
    .loading__text {
      font-size: $font-size-base;
    }
  }
  
  &--large {
    .loading__spinner {
      width: 48px;
      height: 48px;
    }
    
    .loading__text {
      font-size: $font-size-lg;
    }
  }
  
  // 颜色变体
  &--primary {
    --loading-color: #{$primary-500};
  }
  
  &--white {
    --loading-color: #{$white};
  }
  
  &--gray {
    --loading-color: #{$gray-400};
  }
  
  &__spinner {
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  &__text {
    color: var(--loading-color, #{$primary-500});
    font-weight: $font-weight-medium;
    text-align: center;
  }
}

// 旋转器样式
.spinner {
  width: 100%;
  height: 100%;
  
  &__circle {
    width: 100%;
    height: 100%;
    border: 2px solid transparent;
    border-top: 2px solid var(--loading-color, #{$primary-500});
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
}

// 点状加载器
.dots {
  display: flex;
  gap: 4px;
  
  &__dot {
    width: 6px;
    height: 6px;
    background-color: var(--loading-color, #{$primary-500});
    border-radius: 50%;
    animation: dots 1.4s ease-in-out infinite both;
    
    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
    &:nth-child(3) { animation-delay: 0s; }
  }
}

// 脉冲加载器
.pulse {
  width: 100%;
  height: 100%;
  
  &__circle {
    width: 100%;
    height: 100%;
    background-color: var(--loading-color, #{$primary-500});
    border-radius: 50%;
    animation: pulse 1.5s ease-in-out infinite;
  }
}

// 条状加载器
.bars {
  display: flex;
  gap: 2px;
  align-items: end;
  height: 100%;
  
  &__bar {
    width: 4px;
    background-color: var(--loading-color, #{$primary-500});
    animation: bars 1.2s ease-in-out infinite;
    
    &:nth-child(1) { animation-delay: -0.45s; }
    &:nth-child(2) { animation-delay: -0.3s; }
    &:nth-child(3) { animation-delay: -0.15s; }
    &:nth-child(4) { animation-delay: 0s; }
  }
}

// 动画定义
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes dots {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
  }
}

@keyframes bars {
  0%, 40%, 100% {
    height: 20%;
  }
  20% {
    height: 100%;
  }
}

// 响应式调整
@media (max-width: 768px) {
  .loading {
    &--small .loading__spinner {
      width: 16px;
      height: 16px;
    }
    
    &--medium .loading__spinner {
      width: 24px;
      height: 24px;
    }
    
    &--large .loading__spinner {
      width: 32px;
      height: 32px;
    }
  }
}
</style>