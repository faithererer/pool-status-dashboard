import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || null)
  const user = ref(JSON.parse(localStorage.getItem('username') || 'null'))
  const isLoading = ref(false)
  const error = ref(null)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const userInfo = computed(() => user.value)

  // 登录
  const login = async (credentials) => {
    try {
      isLoading.value = true
      error.value = null
      
      const response = await api.post('/auth/login', credentials)
      
      if (response.data.success) {
        const { token: authToken, user: userInfo } = response.data.data
        
        // 保存到状态
        token.value = authToken
        user.value = userInfo
        
        // 保存到本地存储
        localStorage.setItem('token', authToken)
        localStorage.setItem('user', JSON.stringify(userInfo))
        
        return { success: true }
      } else {
        throw new Error(response.data.message || '登录失败')
      }
    } catch (err) {
      error.value = err.response?.data?.message || err.message || '登录失败'
      return { success: false, error: error.value }
    } finally {
      isLoading.value = false
    }
  }

  // 登出
  const logout = async () => {
    try {
      // 调用后端登出接口
      if (token.value) {
        await api.post('/auth/logout')
      }
    } catch (err) {
      console.warn('登出请求失败:', err)
    } finally {
      // 清除状态
      token.value = null
      user.value = null
      
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      
      error.value = null
    }
  }

  // 刷新令牌
  const refreshToken = async () => {
    try {
      const response = await api.post('/auth/refresh')
      
      if (response.data.success) {
        const { token: newToken } = response.data.data
        token.value = newToken
        localStorage.setItem('token', newToken)
        return true
      }
      
      return false
    } catch (err) {
      console.error('刷新令牌失败:', err)
      await logout()
      return false
    }
  }

  // 检查认证状态
  const checkAuth = async () => {
    if (!token.value) {
      return false
    }

    try {
      const response = await api.get('auth/validate')
      
      if (response.data.success) {
        user.value = response.data.data
        localStorage.setItem('username', JSON.stringify(response.data.data))
        return true
      }
      
      return false
    } catch (err) {
      console.error('检查认证状态失败:', err)
      await logout()
      return false
    }
  }

  // 清除错误
  const clearError = () => {
    error.value = null
  }

  // 初始化时检查认证状态
  const initialize = async () => {
    if (token.value) {
      await checkAuth()
    }
  }

  return {
    // 状态
    token,
    user,
    isLoading,
    error,
    
    // 计算属性
    isAuthenticated,
    userInfo,
    
    // 方法
    login,
    logout,
    refreshToken,
    checkAuth,
    clearError,
    initialize
  }
})