<template>
  <button
    :class="buttonClasses"
    :disabled="disabled || loading"
    :type="type"
    @click="handleClick"
  >
    <span v-if="loading" class="loading-spinner"></span>
    <slot v-if="!loading"></slot>
    <span v-if="loading && loadingText">{{ loadingText }}</span>
  </button>
</template>

<script>
import { defineComponent, computed } from 'vue'

export default defineComponent({
  name: 'BaseButton',
  props: {
    variant: {
      type: String,
      default: 'primary',
      validator: (value) => ['primary', 'secondary', 'success', 'warning', 'danger', 'info', 'ghost'].includes(value)
    },
    size: {
      type: String,
      default: 'medium',
      validator: (value) => ['small', 'medium', 'large'].includes(value)
    },
    disabled: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    },
    loadingText: {
      type: String,
      default: ''
    },
    type: {
      type: String,
      default: 'button',
      validator: (value) => ['button', 'submit', 'reset'].includes(value)
    },
    block: {
      type: Boolean,
      default: false
    },
    round: {
      type: Boolean,
      default: false
    }
  },
  emits: ['click'],
  setup(props, { emit }) {
    const buttonClasses = computed(() => [
      'base-button',
      `base-button--${props.variant}`,
      `base-button--${props.size}`,
      {
        'base-button--disabled': props.disabled,
        'base-button--loading': props.loading,
        'base-button--block': props.block,
        'base-button--round': props.round
      }
    ])

    const handleClick = (event) => {
      if (!props.disabled && !props.loading) {
        emit('click', event)
      }
    }

    return {
      buttonClasses,
      handleClick
    }
  }
})
</script>

<style lang="scss" scoped>
.base-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-2;
  padding: $spacing-2 $spacing-4;
  border: 1px solid transparent;
  border-radius: $radius-md;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  line-height: 1.5;
  text-align: center;
  text-decoration: none;
  cursor: pointer;
  transition: all $transition-normal;
  user-select: none;
  white-space: nowrap;
  
  &:focus {
    outline: none;
    box-shadow: 0 0 0 2px rgba($primary-500, 0.2);
  }
  
  // 尺寸变体
  &--small {
    padding: $spacing-1 $spacing-3;
    font-size: $font-size-xs;
  }
  
  &--medium {
    padding: $spacing-2 $spacing-4;
    font-size: $font-size-sm;
  }
  
  &--large {
    padding: $spacing-3 $spacing-6;
    font-size: $font-size-base;
  }
  
  // 颜色变体
  &--primary {
    background: linear-gradient(45deg, $primary-600, $primary-500);
    border-color: $primary-500;
    color: white;
    
    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background: linear-gradient(45deg, $primary-700, $primary-600);
      border-color: $primary-600;
      transform: translateY(-1px);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
  
  &--secondary {
    background: $bg-overlay;
    border-color: $border-color;
    color: $gray-100;
    
    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background: rgba($primary-500, 0.1);
      border-color: $primary-500;
      color: $primary-400;
      transform: translateY(-1px);
    }
  }
  
  &--success {
    background: linear-gradient(45deg, $green-600, $green-500);
    border-color: $green-500;
    color: white;
    
    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background: linear-gradient(45deg, $green-700, $green-600);
      border-color: $green-600;
      transform: translateY(-1px);
    }
  }
  
  &--warning {
    background: linear-gradient(45deg, $yellow-600, $yellow-500);
    border-color: $yellow-500;
    color: white;
    
    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background: linear-gradient(45deg, $yellow-700, $yellow-600);
      border-color: $yellow-600;
      transform: translateY(-1px);
    }
  }
  
  &--danger {
    background: linear-gradient(45deg, $red-600, $red-500);
    border-color: $red-500;
    color: white;
    
    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background: linear-gradient(45deg, $red-700, $red-600);
      border-color: $red-600;
      transform: translateY(-1px);
    }
  }
  
  &--info {
    background: linear-gradient(45deg, $blue-600, $blue-500);
    border-color: $blue-500;
    color: white;
    
    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background: linear-gradient(45deg, $blue-700, $blue-600);
      border-color: $blue-600;
      transform: translateY(-1px);
    }
  }
  
  &--ghost {
    background: transparent;
    border-color: $border-color;
    color: $gray-300;
    
    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background: rgba($primary-500, 0.1);
      border-color: $primary-500;
      color: $primary-400;
    }
  }
  
  // 状态变体
  &--disabled {
    opacity: 0.5;
    cursor: not-allowed;
    transform: none !important;
  }
  
  &--loading {
    cursor: wait;
    
    .loading-spinner {
      width: 16px;
      height: 16px;
      border: 2px solid transparent;
      border-top: 2px solid currentColor;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
  }
  
  // 布局变体
  &--block {
    display: flex;
    width: 100%;
  }
  
  &--round {
    border-radius: 9999px;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>