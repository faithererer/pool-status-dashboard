import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { poolApi, poolStatusApi } from '@/services/api'

export const useDashboardStore = defineStore('dashboard', () => {
  // 状态
  const publicPools = ref([])
  const overviewStats = ref({})
  const selectedPoolId = ref(null)
  const chartData = ref({})
  const poolHistory = ref({})
  const selectedPoolStatus = ref(null) // 新增：存储选中号池的最新状态
  const loading = ref({
    pools: false,
    overview: false,
    chart: false,
    history: false,
    latestStatus: false // 新增
  })
  const error = ref({
    pools: null,
    overview: null,
    chart: null,
    history: null,
    latestStatus: null // 新增
  })
  const autoRefreshTimer = ref(null)
  const refreshInterval = ref(30000) // 30秒自动刷新

  // 计算属性
  const isLoading = computed(() => {
    return Object.values(loading.value).some(l => l)
  })

  const hasError = computed(() => {
    return Object.values(error.value).some(e => e !== null)
  })

  const currentChartData = computed(() => {
    if (!selectedPoolId.value) return null
    return chartData.value[selectedPoolId.value] || null
  })

  const selectedPool = computed(() => {
    if (!selectedPoolId.value) return null
    const pool = publicPools.value.find(p => p.id === selectedPoolId.value)
    if (!pool) return null
    // 将最新的状态合并到号池信息中
    return {
      ...pool,
      latestStatus: selectedPoolStatus.value || pool.latestStatus
    }
  })

  // 获取公共号池列表
  const fetchPublicPools = async () => {
    loading.value.pools = true
    error.value.pools = null
    try {
      // 并行获取号池列表和所有号池的最新状态
      const [poolsResponse, statusesResponse] = await Promise.all([
        poolApi.getPublicPools(),
        poolStatusApi.getAllLatestPoolStatus()
      ])

      if (poolsResponse.success && statusesResponse.success) {
        const pools = Array.isArray(poolsResponse.data) ? poolsResponse.data : []
        const statuses = Array.isArray(statusesResponse.data) ? statusesResponse.data : []

        // 创建一个 status 的查找表，以 poolId 为键，提高合并效率
        const statusMap = statuses.reduce((map, status) => {
          map[status.poolId] = status
          return map
        }, {})

        // 将最新状态合并到号池列表中
        const enrichedPools = pools.map(pool => ({
          ...pool,
          latestStatus: statusMap[pool.id] || pool.latestStatus || null
        }))

        publicPools.value = enrichedPools

        // 如果当前没有选中的号池，并且列表不为空，则默认选中第一个
        if (!selectedPoolId.value && publicPools.value.length > 0) {
          await setSelectedPool(publicPools.value[0].id)
        }
      } else {
        throw new Error(poolsResponse.message || statusesResponse.message || '获取号池数据失败')
      }
    } catch (err) {
      console.error('获取公共号池列表失败:', err)
      error.value.pools = err.message || '获取公共号池列表失败'
      publicPools.value = []
    } finally {
      loading.value.pools = false
    }
  }

  // 获取总览统计数据
  const fetchOverviewStats = async () => {
    loading.value.overview = true
    error.value.overview = null
    
    try {
      const response = await poolStatusApi.getOverviewStats()
      
      if (response.success && response.data) {
        overviewStats.value = response.data
      } else {
        throw new Error(response.message || '获取总览统计失败')
      }
    } catch (err) {
      console.error('获取总览统计失败:', err)
      error.value.overview = err.message || '获取总览统计失败'
      
      // 移除模拟数据，清空状态
      overviewStats.value = {}
    } finally {
      loading.value.overview = false
    }
  }

  // 获取选中号池的最新状态
  const fetchLatestPoolStatus = async (poolId) => {
    if (!poolId) {
      selectedPoolStatus.value = null
      return
    }
    loading.value.latestStatus = true
    error.value.latestStatus = null
    try {
      const response = await poolStatusApi.getPoolStatus(poolId)
      if (response.success) {
        selectedPoolStatus.value = response.data
      } else {
        throw new Error(response.message || '获取号池最新状态失败')
      }
    } catch (err) {
      console.error('获取号池最新状态失败:', err)
      error.value.latestStatus = err.message || '获取号池最新状态失败'
      selectedPoolStatus.value = null
    } finally {
      loading.value.latestStatus = false
    }
  }

  // 获取号池历史数据
  const fetchPoolHistory = async (poolId, timeRange = '24h') => {
    if (!poolId) return
    
    loading.value.history = true
    error.value.history = null
    
    try {
      // 根据时间范围计算参数
      const now = Date.now()
      let startTime, endTime = now
      
      switch (timeRange) {
        case '1h':
          startTime = now - 60 * 60 * 1000
          break
        case '24h':
          startTime = now - 24 * 60 * 60 * 1000
          break
        case '7d':
          startTime = now - 7 * 24 * 60 * 60 * 1000
          break
        case '30d':
          startTime = now - 30 * 24 * 60 * 60 * 1000
          break
        default:
          startTime = now - 24 * 60 * 60 * 1000
      }
      
      const response = await poolStatusApi.getPoolHistory(poolId, { timeRange, startTime, endTime })
      
      if (response.success && response.data) {
        if (!poolHistory.value[poolId]) {
          poolHistory.value[poolId] = {}
        }
        poolHistory.value[poolId][timeRange] = response.data
        
        // 转换为图表数据格式
        chartData.value[poolId] = transformToChartData(response.data, timeRange)
      } else {
        throw new Error(response.message || '获取历史数据失败')
      }
    } catch (err) {
      console.error('获取历史数据失败:', err)
      error.value.history = err.message || '获取历史数据失败'
      
      // 移除模拟数据，清空状态
      chartData.value[poolId] = null
    } finally {
      loading.value.history = false
    }
  }

  // 转换历史数据为图表格式
  const transformToChartData = (records, timeRange) => {
    if (!records || !Array.isArray(records) || records.length === 0) {
      return null
    }
    const labels = records.map(record => formatTimeLabel(record.timestamp, timeRange))
    
    return {
      labels,
      datasets: [
        {
          label: '有效数量',
          data: records.map(r => r.validCount || 0),
          borderColor: '#22c55e',
          backgroundColor: 'rgba(34, 197, 94, 0.1)',
          tension: 0.4,
          fill: true
        },
        {
          label: '无效数量',
          data: records.map(r => r.invalidCount || 0),
          borderColor: '#ef4444',
          backgroundColor: 'rgba(239, 68, 68, 0.1)',
          tension: 0.4,
          fill: true
        },
        {
          label: '冷却中数量',
          data: records.map(r => r.coolingCount || 0),
          borderColor: '#fbbf24',
          backgroundColor: 'rgba(251, 191, 36, 0.1)',
          tension: 0.4,
          fill: true
        },
        {
          label: '压力(%)',
          data: records.map(r => r.pressure || 0),
          borderColor: '#8b5cf6',
          backgroundColor: 'rgba(139, 92, 246, 0.1)',
          tension: 0.4,
          fill: false,
          yAxisID: 'y1'
        }
      ]
    }
  }

  // 生成模拟图表数据
  const generateMockChartData = (poolId, timeRange) => {
    const labelMaps = {
      '1h': ['00:00', '00:15', '00:30', '00:45', '01:00'],
      '24h': ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
      '7d': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      '30d': ['第1周', '第2周', '第3周', '第4周']
    }
    
    const labels = labelMaps[timeRange] || labelMaps['24h']
    
    // 基础数据（根据不同号池生成不同的基础值）
    const baseData = {
      1: { valid: 2847, invalid: 156, cooling: 423, pressure: 83 },
      2: { valid: 1234, invalid: 89, cooling: 267, pressure: 78 },
      3: { valid: 892, invalid: 23, cooling: 145, pressure: 84 },
      4: { valid: 456, invalid: 234, cooling: 123, pressure: 56 }
    }
    
    const base = baseData[poolId] || baseData[1]
    
    const generateValues = (baseValue, variance = 0.1) => {
      return labels.map(() => {
        const variation = (Math.random() - 0.5) * variance * baseValue
        return Math.max(0, Math.round(baseValue + variation))
      })
    }
    
    return {
      labels,
      datasets: [
        {
          label: '有效数量',
          data: generateValues(base.valid),
          borderColor: '#22c55e',
          backgroundColor: 'rgba(34, 197, 94, 0.1)',
          tension: 0.4,
          fill: true
        },
        {
          label: '无效数量',
          data: generateValues(base.invalid),
          borderColor: '#ef4444',
          backgroundColor: 'rgba(239, 68, 68, 0.1)',
          tension: 0.4,
          fill: true
        },
        {
          label: '冷却中数量',
          data: generateValues(base.cooling),
          borderColor: '#fbbf24',
          backgroundColor: 'rgba(251, 191, 36, 0.1)',
          tension: 0.4,
          fill: true
        },
        {
          label: '压力(%)',
          data: generateValues(base.pressure, 0.2),
          borderColor: '#8b5cf6',
          backgroundColor: 'rgba(139, 92, 246, 0.1)',
          tension: 0.4,
          fill: false,
          yAxisID: 'y1'
        }
      ]
    }
  }

  // 格式化时间标签
  const formatTimeLabel = (timestamp, timeRange) => {
    const date = new Date(timestamp)
    
    switch (timeRange) {
      case '1h':
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      case '24h':
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      case '7d':
        return date.toLocaleDateString('zh-CN', { weekday: 'short' })
      case '30d':
        return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
      default:
        return date.toLocaleString('zh-CN')
    }
  }

  // 设置选中的号池，并获取其历史数据
  const setSelectedPool = async (poolId) => {
    // 如果选择的还是当前号池，则不执行任何操作
    if (selectedPoolId.value === poolId) {
      return
    }
    
    selectedPoolId.value = poolId
    
    // 如果设置了新的号池ID，则并行获取其历史数据和最新状态
    if (poolId) {
      await Promise.all([
        fetchPoolHistory(poolId),
        fetchLatestPoolStatus(poolId)
      ])
    } else {
      // 如果没有选中号池，则清空相关数据
      chartData.value = {}
      selectedPoolStatus.value = null
    }
  }

  // 获取图表数据
  const getChartData = (poolId) => {
    return chartData.value[poolId] || null
  }

  // 启动自动刷新
  const startAutoRefresh = () => {
    if (autoRefreshTimer.value) {
      clearInterval(autoRefreshTimer.value)
    }
    
    autoRefreshTimer.value = setInterval(async () => {
      try {
        // 并行获取总览数据和公共号池列表（及其最新状态）
        await Promise.all([
          fetchOverviewStats(),
          fetchPublicPools()
        ])

        if (selectedPoolId.value) {
          // 如果有选中的号池，也并行刷新其历史数据和最新状态
          // 注意：此简化版实现未存储每个池子的时间范围，将使用默认值
          await Promise.all([
            fetchPoolHistory(selectedPoolId.value),
            fetchLatestPoolStatus(selectedPoolId.value)
          ])
        }
      } catch (error) {
        console.error('自动刷新失败:', error)
      }
    }, refreshInterval.value)
  }

  // 停止自动刷新
  const stopAutoRefresh = () => {
    if (autoRefreshTimer.value) {
      clearInterval(autoRefreshTimer.value)
      autoRefreshTimer.value = null
    }
  }

  // 清除错误
  const clearError = (type) => {
    if (type) {
      error.value[type] = null
    } else {
      Object.keys(error.value).forEach(key => {
        error.value[key] = null
      })
    }
  }

  // 重置状态
  const reset = () => {
    publicPools.value = []
    overviewStats.value = {}
    selectedPoolId.value = null
    chartData.value = {}
    poolHistory.value = {}
    Object.keys(loading.value).forEach(key => {
      loading.value[key] = false
    })
    clearError()
    stopAutoRefresh()
  }

  return {
    // 状态
    publicPools,
    overviewStats,
    selectedPoolId,
    selectedPoolStatus, // 新增
    chartData,
    poolHistory,
    loading,
    error,
    refreshInterval,
    
    // 计算属性
    isLoading,
    hasError,
    currentChartData,
    selectedPool, // 新增
    
    // 方法
    fetchPublicPools,
    fetchOverviewStats,
    fetchPoolHistory,
    fetchLatestPoolStatus, // 新增
    setSelectedPool,
    getChartData,
    startAutoRefresh,
    stopAutoRefresh,
    clearError,
    reset
  }
})