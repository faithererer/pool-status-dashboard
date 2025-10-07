<template>
  <Teleport to="body">
    <Transition name="modal" appear>
      <div v-if="modelValue" class="modal-overlay" @click="handleOverlayClick">
        <div 
          ref="modalRef"
          :class="modalClasses"
          @click.stop
          role="dialog"
          :aria-labelledby="titleId"
          aria-modal="true"
        >
          <!-- 头部 -->
          <div v-if="showHeader" class="modal__header">
            <h3 v-if="title" :id="titleId" class="modal__title">
              {{ title }}
            </h3>
            <slot name="header" v-else></slot>
            <button
              v-if="closable"
              class="modal__close"
              @click="handleClose"
              aria-label="关闭"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>
          </div>

          <!-- 内容 -->
          <div class="modal__body">
            <slot></slot>
          </div>

          <!-- 底部 -->
          <div v-if="showFooter" class="modal__footer">
            <slot name="footer">
              <BaseButton
                v-if="showCancel"
                variant="secondary"
                @click="handleCancel"
              >
                {{ cancelText }}
              </BaseButton>
              <BaseButton
                v-if="showConfirm"
                :variant="confirmVariant"
                :loading="confirmLoading"
                @click="handleConfirm"
              >
                {{ confirmText }}
              </BaseButton>
            </slot>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script>
import { defineComponent, computed, ref, watch, nextTick } from 'vue'
import BaseButton from './BaseButton.vue'

let modalIdCounter = 0

export default defineComponent({
  name: 'BaseModal',
  components: {
    BaseButton
  },
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: ''
    },
    size: {
      type: String,
      default: 'medium',
      validator: (value) => ['small', 'medium', 'large', 'full'].includes(value)
    },
    closable: {
      type: Boolean,
      default: true
    },
    maskClosable: {
      type: Boolean,
      default: true
    },
    showHeader: {
      type: Boolean,
      default: true
    },
    showFooter: {
      type: Boolean,
      default: false
    },
    showCancel: {
      type: Boolean,
      default: true
    },
    showConfirm: {
      type: Boolean,
      default: true
    },
    cancelText: {
      type: String,
      default: '取消'
    },
    confirmText: {
      type: String,
      default: '确定'
    },
    confirmVariant: {
      type: String,
      default: 'primary'
    },
    confirmLoading: {
      type: Boolean,
      default: false
    },
    destroyOnClose: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue', 'close', 'cancel', 'confirm', 'opened', 'closed'],
  setup(props, { emit }) {
    const modalRef = ref(null)
    const titleId = `modal-title-${++modalIdCounter}`

    const modalClasses = computed(() => [
      'modal',
      `modal--${props.size}`
    ])

    const handleClose = () => {
      emit('update:modelValue', false)
      emit('close')
    }

    const handleCancel = () => {
      emit('cancel')
      if (!props.confirmLoading) {
        handleClose()
      }
    }

    const handleConfirm = () => {
      emit('confirm')
    }

    const handleOverlayClick = () => {
      if (props.maskClosable && !props.confirmLoading) {
        handleClose()
      }
    }

    const handleEscapeKey = (event) => {
      if (event.key === 'Escape' && props.closable && !props.confirmLoading) {
        handleClose()
      }
    }

    // 监听模态框状态变化
    watch(() => props.modelValue, async (newValue) => {
      if (newValue) {
        document.addEventListener('keydown', handleEscapeKey)
        document.body.style.overflow = 'hidden'
        await nextTick()
        modalRef.value?.focus()
        emit('opened')
      } else {
        document.removeEventListener('keydown', handleEscapeKey)
        document.body.style.overflow = ''
        emit('closed')
      }
    })

    return {
      modalRef,
      titleId,
      modalClasses,
      handleClose,
      handleCancel,
      handleConfirm,
      handleOverlayClick
    }
  }
})
</script>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: $spacing-4;
}

.modal {
  background: $gray-800;
  border: 1px solid $gray-700;
  border-radius: $radius-lg;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  outline: none;
  
  // 尺寸变体
  &--small {
    width: 100%;
    max-width: 400px;
  }
  
  &--medium {
    width: 100%;
    max-width: 600px;
  }
  
  &--large {
    width: 100%;
    max-width: 800px;
  }
  
  &--full {
    width: 95vw;
    height: 95vh;
    max-width: none;
    max-height: none;
  }
  
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-6 $spacing-6 0;
    flex-shrink: 0;
  }
  
  &__title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $gray-100;
    margin: 0;
  }
  
  &__close {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border: none;
    background: none;
    color: $gray-400;
    cursor: pointer;
    border-radius: $radius-md;
    transition: all $transition-normal;
    
    &:hover {
      background-color: $gray-700;
      color: $gray-200;
    }
    
    &:focus {
      outline: 2px solid $primary-500;
      outline-offset: 2px;
    }
  }
  
  &__body {
    padding: $spacing-6;
    overflow-y: auto;
    flex: 1;
    min-height: 0;
  }
  
  &__footer {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: $spacing-3;
    padding: 0 $spacing-6 $spacing-6;
    flex-shrink: 0;
  }
}

// 动画
.modal-enter-active,
.modal-leave-active {
  transition: opacity $transition-normal;
  
  .modal {
    transition: transform $transition-normal;
  }
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
  
  .modal {
    transform: scale(0.95) translateY(-20px);
  }
}

// 响应式
@media (max-width: 768px) {
  .modal-overlay {
    padding: $spacing-2;
  }
  
  .modal {
    &--small,
    &--medium,
    &--large {
      width: 100%;
      max-width: none;
    }
    
    &__header {
      padding: $spacing-4 $spacing-4 0;
    }
    
    &__body {
      padding: $spacing-4;
    }
    
    &__footer {
      padding: 0 $spacing-4 $spacing-4;
      flex-direction: column-reverse;
      
      :deep(.btn) {
        width: 100%;
      }
    }
  }
}
</style>