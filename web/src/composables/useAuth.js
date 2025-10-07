import { ref, computed } from 'vue'
import { authApi } from '@/services/api'

// 全局认证状态
const token = ref(localStorage.getItem('token'))
const user = ref(null)

try {
  const storedUser = localStorage.getItem('user')
  if (storedUser && storedUser !== 'undefined') {
    user.value = JSON.parse(storedUser)
  }
} catch (error) {
  console.error('Failed to parse user from localStorage:', error)
  localStorage.removeItem('user')
}
const isLoading = ref(false)

export function useAuth() {
  
  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'admin')
  
  // 登录
  const login = async (credentials) => {
    isLoading.value = true
    try {
      const response = await authApi.login(credentials)
      
      if (response.success) {
        token.value = response.data.token
        user.value = response.data.user
        
        // 存储到本地存储
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('user', JSON.stringify(response.data.user))
        
        return { success: true }
      } else {
        return { success: false, message: response.message || '登录失败' }
      }
    } catch (error) {
      console.error('登录错误:', error)
      return { 
        success: false, 
        message: error.response?.data?.message || '网络错误，请稍后重试' 
      }
    } finally {
      isLoading.value = false
    }
  }
  
  // 登出
  const logout = async () => {
    try {
      // 调用后端登出接口
      await authApi.logout()
    } catch (error) {
      console.error('登出错误:', error)
    } finally {
      // 清除本地状态
      token.value = null
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      
      // 跳转的操作交给调用方处理
    }
  }
  
  // 刷新token
  const refreshToken = async () => {
    try {
      const response = await authApi.refreshToken()
      
      if (response.success) {
        token.value = response.data.token
        localStorage.setItem('token', response.data.token)
        return true
      } else {
        // token刷新失败，需要重新登录
        await logout()
        return false
      }
    } catch (error) {
      console.error('刷新token错误:', error)
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
      // 调用后端 /validate 接口来验证 token 的有效性
      const response = await authApi.validateToken()
      
      // 如果后端返回成功，说明 token 有效
      if (response.success) {
        // 如果本地没有用户信息，可以尝试从 localStorage 恢复
        if (!user.value) {
          try {
            const storedUser = localStorage.getItem('user');
            if (storedUser && storedUser !== 'undefined') {
              user.value = JSON.parse(storedUser);
            }
          } catch (e) {
            console.error('恢复用户信息失败:', e);
          }
        }
        return true
      } else {
        // 如果后端验证失败，则登出
        await logout()
        return false
      }
    } catch (error) {
      console.error('检查认证状态错误:', error)
      // 发生任何网络错误或服务器错误，都执行登出
      await logout()
      return false
    }
  }
  
  // 更新用户信息
  const updateUser = (userData) => {
    user.value = { ...user.value, ...userData }
    localStorage.setItem('user', JSON.stringify(user.value))
  }
  
  return {
    // 状态
    token: computed(() => token.value),
    user: computed(() => user.value),
    isLoading: computed(() => isLoading.value),
    isAuthenticated,
    isAdmin,
    
    // 方法
    login,
    logout,
    refreshToken,
    checkAuth,
    updateUser
  }
}

// 导出全局状态供其他地方使用
export const authState = {
  token,
  user,
  isLoading
}