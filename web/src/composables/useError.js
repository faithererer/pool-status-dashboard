import { ref, reactive } from 'vue'
import { useToast } from './useToast'

// 全局错误状态
const errors = reactive({
  global: null,
  network: null,
  validation: {},
  api: {}
})

const isLoading = ref(false)

export function useError() {
  const toast = useToast()
  
  // 清除所有错误
  const clearErrors = () => {
    errors.global = null
    errors.network = null
    errors.validation = {}
    errors.api = {}
  }
  
  // 清除特定类型的错误
  const clearError = (type, key = null) => {
    if (key) {
      if (errors[type] && typeof errors[type] === 'object') {
        delete errors[type][key]
      }
    } else {
      if (type === 'validation' || type === 'api') {
        errors[type] = {}
      } else {
        errors[type] = null
      }
    }
  }
  
  // 设置全局错误
  const setGlobalError = (message) => {
    errors.global = message
  }
  
  // 设置网络错误
  const setNetworkError = (message) => {
    errors.network = message || '网络连接失败，请检查网络设置'
  }
  
  // 设置验证错误
  const setValidationError = (field, message) => {
    errors.validation[field] = message
  }
  
  // 设置验证错误（批量）
  const setValidationErrors = (validationErrors) => {
    errors.validation = { ...errors.validation, ...validationErrors }
  }
  
  // 设置API错误
  const setApiError = (endpoint, message) => {
    errors.api[endpoint] = message
  }
  
  // 处理HTTP错误
  const handleHttpError = (error, context = '') => {
    console.error(`HTTP错误 ${context}:`, error)
    
    let errorMessage = ''
    
    if (!error.response) {
      // 网络错误
      errorMessage = '网络连接失败，请检查网络设置'
      setNetworkError(errorMessage)
      toast.error(errorMessage)
      return
    }
    
    const { status, data } = error.response
    
    switch (status) {
      case 400:
        // 请求参数错误
        if (data.errors && typeof data.errors === 'object') {
          setValidationErrors(data.errors)
          // 显示第一个验证错误
          const firstError = Object.values(data.errors)[0]
          if (firstError) {
            toast.error(firstError)
          }
        } else {
          errorMessage = data.message || '请求参数错误'
          setGlobalError(errorMessage)
          toast.error(errorMessage)
        }
        break
        
      case 401:
        // 未授权
        errorMessage = '登录已过期，请重新登录'
        setGlobalError(errorMessage)
        toast.error(errorMessage)
        // 这里可以触发重新登录逻辑
        break
        
      case 403:
        // 权限不足
        errorMessage = '权限不足，无法执行此操作'
        setGlobalError(errorMessage)
        toast.error(errorMessage)
        break
        
      case 404:
        // 资源不存在
        errorMessage = '请求的资源不存在'
        setGlobalError(errorMessage)
        toast.error(errorMessage)
        break
        
      case 422:
        // 验证错误
        if (data.errors && typeof data.errors === 'object') {
          setValidationErrors(data.errors)
          // 显示第一个验证错误
          const firstError = Object.values(data.errors)[0]
          if (firstError) {
            toast.error(firstError)
          }
        } else {
          errorMessage = data.message || '数据验证失败'
          setGlobalError(errorMessage)
          toast.error(errorMessage)
        }
        break
        
      case 429:
        // 请求过于频繁
        errorMessage = '请求过于频繁，请稍后再试'
        setGlobalError(errorMessage)
        toast.warning(errorMessage)
        break
        
      case 500:
        // 服务器内部错误
        errorMessage = '服务器内部错误，请稍后重试'
        setGlobalError(errorMessage)
        toast.error(errorMessage)
        break
        
      case 502:
      case 503:
      case 504:
        // 服务不可用
        errorMessage = '服务暂时不可用，请稍后重试'
        setGlobalError(errorMessage)
        toast.error(errorMessage)
        break
        
      default:
        errorMessage = data.message || `请求失败 (${status})`
        setGlobalError(errorMessage)
        toast.error(errorMessage)
    }
  }
  
  // 处理异步操作的错误
  const handleAsyncError = async (asyncFn, context = '') => {
    isLoading.value = true
    clearErrors()
    
    try {
      const result = await asyncFn()
      return { success: true, data: result }
    } catch (error) {
      handleHttpError(error, context)
      return { success: false, error }
    } finally {
      isLoading.value = false
    }
  }
  
  // 显示成功消息的辅助函数
  const showSuccess = (message, options = {}) => {
    toast.success(message, options)
  }
  
  // 显示警告消息的辅助函数
  const showWarning = (message, options = {}) => {
    toast.warning(message, options)
  }
  
  // 显示信息消息的辅助函数
  const showInfo = (message, options = {}) => {
    toast.info(message, options)
  }
  
  // 显示错误Toast通知
  const showErrorToast = (message, options = {}) => {
    toast.error(message, options)
  }
  
  // 获取错误消息的辅助函数
  const getErrorMessage = (type, key = null) => {
    if (key) {
      return errors[type]?.[key] || null
    }
    return errors[type] || null
  }
  
  // 检查是否有错误
  const hasError = (type = null, key = null) => {
    if (!type) {
      return !!(errors.global || errors.network || 
               Object.keys(errors.validation).length > 0 ||
               Object.keys(errors.api).length > 0)
    }
    
    if (key) {
      return !!(errors[type]?.[key])
    }
    
    if (type === 'validation' || type === 'api') {
      return Object.keys(errors[type]).length > 0
    }
    
    return !!errors[type]
  }
  
  // 格式化错误消息用于显示
  const formatErrorMessage = (error) => {
    if (typeof error === 'string') {
      return error
    }
    
    if (error?.message) {
      return error.message
    }
    
    if (error?.response?.data?.message) {
      return error.response.data.message
    }
    
    return '发生未知错误'
  }
  
  return {
    // 状态
    errors,
    isLoading,
    
    // 方法
    clearErrors,
    clearError,
    setGlobalError,
    setNetworkError,
    setValidationError,
    setValidationErrors,
    setApiError,
    handleHttpError,
    handleAsyncError,
    showSuccess,
    showWarning,
    showInfo,
    showErrorToast,
    getErrorMessage,
    hasError,
    formatErrorMessage
  }
}

// 导出全局错误状态
export const errorState = {
  errors,
  isLoading
}