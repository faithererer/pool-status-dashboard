import { ref, createApp } from 'vue'
import BaseToast from '@/components/common/BaseToast.vue'

// 全局Toast实例管理
const toasts = ref([])
let toastId = 0

// Toast配置默认值
const defaultOptions = {
  type: 'info',
  title: '',
  message: '',
  duration: 4000,
  position: 'top-right',
  closable: true,
  showProgress: true,
  onClick: null
}

/**
 * 创建Toast实例
 * @param {Object} options Toast配置选项
 * @returns {Object} Toast实例控制对象
 */
function createToast(options = {}) {
  const config = { ...defaultOptions, ...options }
  const id = ++toastId
  
  // 创建Toast容器
  const container = document.createElement('div')
  container.id = `toast-${id}`
  document.body.appendChild(container)
  
  // 创建Vue应用实例
  const app = createApp(BaseToast, {
    ...config,
    onClose: () => {
      // 清理DOM和Vue实例
      app.unmount()
      if (container.parentNode) {
        container.parentNode.removeChild(container)
      }
      
      // 从toasts数组中移除
      const index = toasts.value.findIndex(toast => toast.id === id)
      if (index > -1) {
        toasts.value.splice(index, 1)
      }
      
      // 调用用户定义的关闭回调
      if (config.onClose) {
        config.onClose()
      }
    }
  })
  
  // 挂载Toast组件
  app.mount(container)
  
  // 创建Toast控制对象
  const toastInstance = {
    id,
    app,
    container,
    close: () => {
      app.unmount()
      if (container.parentNode) {
        container.parentNode.removeChild(container)
      }
      
      const index = toasts.value.findIndex(toast => toast.id === id)
      if (index > -1) {
        toasts.value.splice(index, 1)
      }
    }
  }
  
  // 添加到全局toasts数组
  toasts.value.push(toastInstance)
  
  return toastInstance
}

/**
 * useToast composable
 * @returns {Object} Toast方法集合
 */
export function useToast() {
  /**
   * 显示成功消息
   * @param {string|Object} message 消息内容或配置对象
   * @param {Object} options 额外配置选项
   */
  const success = (message, options = {}) => {
    const config = typeof message === 'string' 
      ? { message, ...options } 
      : { ...message, ...options }
    
    return createToast({
      type: 'success',
      ...config
    })
  }
  
  /**
   * 显示错误消息
   * @param {string|Object} message 消息内容或配置对象
   * @param {Object} options 额外配置选项
   */
  const error = (message, options = {}) => {
    const config = typeof message === 'string' 
      ? { message, ...options } 
      : { ...message, ...options }
    
    return createToast({
      type: 'error',
      duration: 6000, // 错误消息显示时间更长
      ...config
    })
  }
  
  /**
   * 显示警告消息
   * @param {string|Object} message 消息内容或配置对象
   * @param {Object} options 额外配置选项
   */
  const warning = (message, options = {}) => {
    const config = typeof message === 'string' 
      ? { message, ...options } 
      : { ...message, ...options }
    
    return createToast({
      type: 'warning',
      duration: 5000,
      ...config
    })
  }
  
  /**
   * 显示信息消息
   * @param {string|Object} message 消息内容或配置对象
   * @param {Object} options 额外配置选项
   */
  const info = (message, options = {}) => {
    const config = typeof message === 'string' 
      ? { message, ...options } 
      : { ...message, ...options }
    
    return createToast({
      type: 'info',
      ...config
    })
  }
  
  /**
   * 显示自定义Toast
   * @param {Object} options Toast配置选项
   */
  const show = (options = {}) => {
    return createToast(options)
  }
  
  /**
   * 关闭指定Toast
   * @param {Object} toastInstance Toast实例
   */
  const close = (toastInstance) => {
    if (toastInstance && typeof toastInstance.close === 'function') {
      toastInstance.close()
    }
  }
  
  /**
   * 关闭所有Toast
   */
  const closeAll = () => {
    toasts.value.forEach(toast => {
      if (toast && typeof toast.close === 'function') {
        toast.close()
      }
    })
    toasts.value = []
  }
  
  /**
   * 获取当前所有Toast实例
   */
  const getToasts = () => {
    return toasts.value
  }
  
  return {
    success,
    error,
    warning,
    info,
    show,
    close,
    closeAll,
    getToasts,
    toasts: toasts.value
  }
}

// 全局Toast方法（可选）
export const toast = {
  success: (message, options) => useToast().success(message, options),
  error: (message, options) => useToast().error(message, options),
  warning: (message, options) => useToast().warning(message, options),
  info: (message, options) => useToast().info(message, options),
  show: (options) => useToast().show(options),
  closeAll: () => useToast().closeAll()
}

export default useToast