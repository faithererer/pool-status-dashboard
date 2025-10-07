import axios from 'axios'

// 创建 axios 实例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  },
  // 在 JSON 解析前，对响应数据进行预处理
  transformResponse: [
    (data) => {
      // 检查 data 是否为字符串，如果不是（例如，已经是对象），则直接返回
      if (typeof data !== 'string') {
        return data
      }
      // 使用正则表达式查找所有以 "Id" (不区分大小写) 结尾的键，
      // 并且其值是一个长度超过15位的数字，然后将其值转换为字符串。
      // 例如: "poolId": 123... -> "poolId": "123..."
      // 正则表达式解释:
      // "(\w*[iI]d)"  - 捕获组1: 匹配一个以 "id" 或 "Id" 结尾的键名
      // :\s*          - 匹配冒号和任意空格
      // (\d{16,})     - 捕获组2: 匹配一个长度至少为16位的数字序列
      const fixedData = data.replace(/"(\w*[iI]d)":\s*(\d{16,})/g, '"$1":"$2"')
      
      try {
        // 解析修复后的 JSON 字符串
        return JSON.parse(fixedData)
      } catch (e) {
        // 如果解析失败，返回原始数据，让后续的拦截器处理错误
        console.error('JSON parsing error after transform:', e)
        return data
      }
    }
  ]
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 添加认证 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加请求时间戳
    config.metadata = { startTime: new Date() }
    
    console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, config.data)
    return config
  },
  (error) => {
    console.error('[API Request Error]', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 计算请求耗时
    const endTime = new Date()
    const duration = endTime - response.config.metadata.startTime
    
    console.log(`[API Response] ${response.config.method?.toUpperCase()} ${response.config.url} - ${duration}ms`, response.data)
    
    // 统一处理后端响应格式 { code, data, message }
    const res = response.data
    if (res && typeof res === 'object') {
      // 如果是标准成功响应
      if (res.code === 200) {
        // 返回实际的业务数据，并附加一个 success 标志以便 store 中判断
        return { ...res, success: true, data: res.data }
      }
      // 如果是业务逻辑错误（code 非 200）
      if (res.code && res.code !== 200) {
        console.error(`[API Business Error] ${response.config.url}:`, res.message)
        // 抛出错误，让业务代码的 catch 块处理
        return Promise.reject({
          success: false,
          message: res.message || '业务处理失败',
          code: res.code
        })
      }
    }
    
    // 对于非标准格式的响应，直接返回 data
    return { success: true, data: res }
  },
  (error) => {
    console.error('[API Response Error]', error)
    
    // 处理网络错误
    if (!error.response) {
      return Promise.reject({
        success: false,
        message: '网络连接失败，请检查网络设置',
        code: 'NETWORK_ERROR'
      })
    }
    
    // 处理 HTTP 错误状态码
    const { status, data } = error.response
    
    switch (status) {
      case 401:
        // 未授权，清除 token 并跳转到登录页
        localStorage.removeItem('auth_token')
        window.location.href = '/login'
        return Promise.reject({
          success: false,
          message: '登录已过期，请重新登录',
          code: 'UNAUTHORIZED'
        })
      
      case 403:
        return Promise.reject({
          success: false,
          message: '没有权限访问此资源',
          code: 'FORBIDDEN'
        })
      
      case 404:
        return Promise.reject({
          success: false,
          message: '请求的资源不存在',
          code: 'NOT_FOUND'
        })
      
      case 500:
        return Promise.reject({
          success: false,
          message: '服务器内部错误',
          code: 'INTERNAL_ERROR'
        })
      
      default:
        return Promise.reject({
          success: false,
          message: data?.message || `请求失败 (${status})`,
          code: data?.code || 'REQUEST_ERROR'
        })
    }
  }
)

// 号池相关 API
export const poolApi = {
  // 获取号池列表
  getPools: (params = {}) => api.get('/pools', { params }),
  
  // 获取号池详情
  getPool: (poolId) => api.get(`/pools/${poolId}`),
  
  // 创建号池
  createPool: (poolData) => api.post('/pools', poolData),
  
  // 更新号池
  updatePool: (poolId, poolData) => api.put(`/pools/${poolId}`, poolData),
  
  // 删除号池
  deletePool: (poolId) => api.delete(`/pools/${poolId}`),
  
  // 批量删除号池
  deletePoolBatch: (ids) => api.delete('/pools/batch', { data: ids }),
  
  // 启用/禁用号池
  togglePoolEnabled: (poolId, enabled) => api.put(`/pools/${poolId}/toggle`, null, { params: { enabled } }),
  
  // 获取启用的号池列表
  getEnabledPools: () => api.get('/pools/enabled'),
  
  // 获取公开显示的号池列表
  getPublicPools: () => api.get('/pools/public'),
  
  // 根据显示策略获取号池列表
  getPoolsByDisplayStrategy: (displayStrategy) => api.get(`/pools/strategy/${displayStrategy}`),
  
  // 检查号池名称是否存在
  checkPoolNameExists: (name, excludeId) => api.get('/pools/check-name', { params: { name, excludeId } })
}

// 号池状态相关 API
export const poolStatusApi = {
  // 获取号池最新状态
  getPoolStatus: (poolId) => api.get(`/pool-status/latest/${poolId}`),
  
  // 获取所有号池的最新状态
  getAllLatestPoolStatus: () => api.get('/pool-status/latest'),
  
  // 获取指定号池列表的最新状态
  getLatestPoolStatusByIds: (poolIds) => api.post('/pool-status/latest/batch', poolIds),
  
  // 获取号池历史趋势数据
  getPoolHistory: (poolId, params = {}) => api.get(`/pool-status/trend/${poolId}`, { params }),
  
  // 获取号池状态统计信息
  getPoolStatistics: (poolId, params = {}) => api.get(`/pool-status/statistics/${poolId}`, { params }),
  
  // 获取总览统计数据
  getOverviewStats: () => api.get('/pool-status/overview'),
  
  // 获取高压力号池列表
  getHighPressurePools: (pressureThreshold = 80) => api.get('/pool-status/high-pressure', { params: { pressureThreshold } }),
  
  // 获取号池压力分布统计
  getPressureDistribution: () => api.get('/pool-status/pressure-distribution'),
  
  // 检查号池状态是否异常
  checkPoolStatusAbnormal: (poolId) => api.get(`/pool-status/check-abnormal/${poolId}`),
  
  // 保存号池状态
  savePoolStatus: (poolStatusData) => api.post('/pool-status', poolStatusData),
  
  // 批量保存号池状态
  batchSavePoolStatus: (poolStatusList) => api.post('/pool-status/batch', poolStatusList),
  
  // 删除过期的历史数据
  deleteExpiredData: (retentionDays = 30) => api.delete('/pool-status/expired', { params: { retentionDays } })
}

// 虚拟号池相关 API
export const virtualPoolApi = {
  // 获取虚拟号池列表
  getVirtualPools: () => api.get('/virtual-pools'),
  
  // 获取虚拟号池详情
  getVirtualPool: (virtualPoolId) => api.get(`/virtual-pools/${virtualPoolId}`),
  
  // 创建虚拟号池
  createVirtualPool: (virtualPoolData) => api.post('/virtual-pools', virtualPoolData),
  
  // 更新虚拟号池
  updateVirtualPool: (virtualPoolId, virtualPoolData) => 
    api.put(`/virtual-pools/${virtualPoolId}`, virtualPoolData),
  
  // 删除虚拟号池
  deleteVirtualPool: (virtualPoolId) => api.delete(`/virtual-pools/${virtualPoolId}`),
  
  // 获取虚拟号池状态
  getVirtualPoolStatus: (virtualPoolId) => api.get(`/virtual-pools/${virtualPoolId}/status`)
}

// 数据源相关 API
export const dataSourceApi = {
  // 获取所有可用的数据源类型
  getDataSourceTypes: () => api.get('/datasource/types'),
  
  // 测试数据源连接
  testConnection: (config) => api.post('/datasource/test-connection', config),
}

// 认证相关 API
export const authApi = {
  // 登录
  login: (credentials) => api.post('/auth/login', credentials),
  
  // 登出
  logout: () => api.post('/auth/logout'),
  
  // 刷新 token
  refreshToken: () => api.post('/auth/refresh'),
  
  // 验证 token
  validateToken: () => api.get('/auth/validate')
}

// 配置相关 API
export const configApi = {
  // 获取系统配置
  getConfig: () => api.get('/config'),
  
  // 更新系统配置
  updateConfig: (configData) => api.put('/config', configData),
  
  // 获取数据源配置
  getDataSources: () => api.get('/config/datasources'),
  
  // 更新数据源配置
  updateDataSource: (dataSourceId, dataSourceData) => 
    api.put(`/config/datasources/${dataSourceId}`, dataSourceData),
  
  // 测试数据源连接
  testDataSource: (dataSourceData) => api.post('/config/datasources/test', dataSourceData)
}

// 任务相关 API
export const taskApi = {
  // 获取任务列表
  getTasks: () => api.get('/tasks'),
  
  // 获取任务详情
  getTask: (taskId) => api.get(`/tasks/${taskId}`),
  
  // 启动任务
  startTask: (taskId) => api.post(`/tasks/${taskId}/start`),
  
  // 停止任务
  stopTask: (taskId) => api.post(`/tasks/${taskId}/stop`),
  
  // 获取任务日志
  getTaskLogs: (taskId, params = {}) => api.get(`/tasks/${taskId}/logs`, { params })
}

// 健康检查相关 API
export const healthApi = {
  // 获取系统健康状态
  getHealth: () => api.get('/health'),
  
  // 获取数据库健康状态
  getDatabaseHealth: () => api.get('/health/database'),
  
  // 获取数据源健康状态
  getDataSourceHealth: () => api.get('/health/datasources')
}

// 工具函数
export const apiUtils = {
  // 处理文件上传
  uploadFile: (file, onProgress) => {
    const formData = new FormData()
    formData.append('file', file)
    
    return api.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress) {
          const percentCompleted = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total
          )
          onProgress(percentCompleted)
        }
      }
    })
  },
  
  // 下载文件
  downloadFile: (url, filename) => {
    return api.get(url, {
      responseType: 'blob'
    }).then(response => {
      const blob = new Blob([response.data])
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = filename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
    })
  },
  
  // 取消请求
  createCancelToken: () => axios.CancelToken.source(),
  
  // 判断是否为取消请求的错误
  isCancel: (error) => axios.isCancel(error)
}

// 导出默认 API 实例
export default api