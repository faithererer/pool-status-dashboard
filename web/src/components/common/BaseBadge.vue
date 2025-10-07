<template>
  <span :class="badgeClasses">
    <slot></slot>
  </span>
</template>

<script>
import { defineComponent, computed } from 'vue'

export default defineComponent({
  name: 'BaseBadge',
  props: {
    variant: {
      type: String,
      default: 'default',
      validator: (value) => ['default', 'primary', 'success', 'warning', 'danger', 'info'].includes(value)
    },
    size: {
      type: String,
      default: 'medium',
      validator: (value) => ['small', 'medium', 'large'].includes(value)
    },
    dot: {
      type: Boolean,
      default: false
    },
    outline: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const badgeClasses = computed(() => [
      'badge',
      `badge--${props.variant}`,
      `badge--${props.size}`,
      {
        'badge--dot': props.dot,
        'badge--outline': props.outline
      }
    ])

    return {
      badgeClasses
    }
  }
})
</script>

<style lang="scss" scoped>
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-1 $spacing-2;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  line-height: 1;
  text-align: center;
  white-space: nowrap;
  transition: all $transition-normal;
  
  // 尺寸变体
  &--small {
    padding: 2px $spacing-1;
    font-size: 10px;
  }
  
  &--medium {
    padding: $spacing-1 $spacing-2;
    font-size: $font-size-xs;
  }
  
  &--large {
    padding: $spacing-2 $spacing-3;
    font-size: $font-size-sm;
  }
  
  // 颜色变体
  &--default {
    background-color: $gray-600;
    color: $gray-100;
    
    &.badge--outline {
      background-color: transparent;
      border: 1px solid $gray-600;
      color: $gray-400;
    }
  }
  
  &--primary {
    background-color: $primary-500;
    color: white;
    
    &.badge--outline {
      background-color: transparent;
      border: 1px solid $primary-500;
      color: $primary-400;
    }
  }
  
  &--success {
    background-color: $green-500;
    color: white;
    
    &.badge--outline {
      background-color: transparent;
      border: 1px solid $green-500;
      color: $green-400;
    }
  }
  
  &--warning {
    background-color: $yellow-500;
    color: $gray-900;
    
    &.badge--outline {
      background-color: transparent;
      border: 1px solid $yellow-500;
      color: $yellow-400;
    }
  }
  
  &--danger {
    background-color: $red-500;
    color: white;
    
    &.badge--outline {
      background-color: transparent;
      border: 1px solid $red-500;
      color: $red-400;
    }
  }
  
  &--info {
    background-color: $blue-500;
    color: white;
    
    &.badge--outline {
      background-color: transparent;
      border: 1px solid $blue-500;
      color: $blue-400;
    }
  }
  
  // 点状徽章
  &--dot {
    padding: 0;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    
    &.badge--small {
      width: 6px;
      height: 6px;
    }
    
    &.badge--large {
      width: 10px;
      height: 10px;
    }
  }
}

// 状态映射的便捷类
.badge {
  &--healthy {
    @extend .badge--success;
  }
  
  &--warning {
    @extend .badge--warning;
  }
  
  &--critical {
    @extend .badge--danger;
  }
  
  &--offline {
    @extend .badge--default;
  }
}
</style>