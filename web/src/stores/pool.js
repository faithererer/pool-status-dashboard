import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { poolApi, poolStatusApi, virtualPoolApi, dataSourceApi } from '@/services/api'

export const usePoolStore = defineStore('pool', () => {
  // 状态
  const pools = ref([])
  const poolStatuses = ref({})
  const virtualPools = ref([])
  const dataSourceTypes = ref([])
  const pagination = ref({
    current: 1,
    size: 10,
    total: 0,
    totalPages: 1,
  })
  const loading = ref({
    pools: false,
    status: false,
    virtual: false,
    create: false,
    update: false,
    delete: false
  })
  const error = ref({
    pools: null,
    status: null,
    virtual: null,
    create: null,
    update: null,
    delete: null
  })

  // 计算属性
  const isLoading = computed(() => {
    return Object.values(loading.value).some(l => l)
  })

  const hasError = computed(() => {
    return Object.values(error.value).some(e => e !== null)
  })

  const activePools = computed(() => {
    return pools.value.filter(pool => pool.status !== 'offline')
  })

  const poolsWithStatus = computed(() => {
    return pools.value.map(pool => ({
      ...pool,
      currentStatus: poolStatuses.value[pool.id] || null
    }))
  })

  // 获取号池列表
  const fetchPools = async (params = {}) => {
    loading.value.pools = true
    error.value.pools = null
    try {
      const queryParams = {
        current: pagination.value.current,
        size: pagination.value.size,
        ...params,
      }
      const response = await poolApi.getPools(queryParams)
      // API返回的数据结构是 { code, message, data: { records, total, size, current, pages } }
      if (response.data && Array.isArray(response.data.records)) {
        const { records, total, size, current, pages } = response.data
        pools.value = records
        pagination.value = { total, size, current, totalPages: pages }
      } else {
        throw new Error(response.message || '获取号池列表失败或数据格式不正确')
      }
    } catch (err) {
      console.error('获取号池列表失败:', err)
      error.value.pools = err.message || '获取号池列表失败'
      pools.value = []
      throw err
    } finally {
      loading.value.pools = false
    }
  }

  const setCurrentPage = (page) => {
    if (page > 0 && page <= pagination.value.totalPages) {
      pagination.value.current = page
    }
  }

  // 获取号池状态
  const fetchPoolStatus = async (poolId) => {
    if (!poolId) return
    
    loading.value.status = true
    error.value.status = null
    
    try {
      const response = await poolStatusApi.getPoolStatus(poolId)

      if (response.success) {
        poolStatuses.value[poolId] = {
          validCount: response.data.validCount || 0,
          invalidCount: response.data.invalidCount || 0,
          coolingCount: response.data.coolingCount || 0,
          totalCount: response.data.totalCount || 0,
          pressure: response.data.pressure || 0,
          lastUpdated: response.data.lastUpdated
        }
        // console.warn("压力是"+poolStatuses.value[poolId].pressure)
      } else {
        throw new Error(response.message || '获取号池状态失败')
      }
    } catch (err) {
      console.error('获取号池状态失败:', err)
      error.value.status = err.message || '获取号池状态失败'
      
      // 使用模拟数据作为后备
      const mockData = {
        1: { validCount: 2847, invalidCount: 156, coolingCount: 423, totalCount: 3426, pressure: 83 },
        2: { validCount: 1234, invalidCount: 89, coolingCount: 267, totalCount: 1590, pressure: 78 },
        3: { validCount: 892, invalidCount: 23, coolingCount: 145, totalCount: 1060, pressure: 84 },
        4: { validCount: 456, invalidCount: 234, coolingCount: 123, totalCount: 813, pressure: 56 }
      }
      
      poolStatuses.value[poolId] = mockData[poolId] || mockData[1]
    } finally {
      loading.value.status = false
    }
  }

  // 批量获取号池状态
  const fetchAllPoolStatuses = async () => {
    const promises = pools.value.map(pool => fetchPoolStatus(pool.id))
    await Promise.allSettled(promises)
  }

  // 获取虚拟号池列表
  const fetchVirtualPools = async () => {
    loading.value.virtual = true
    error.value.virtual = null
    
    try {
      const response = await virtualPoolApi.getVirtualPools()
      
      if (response.success) {
        virtualPools.value = response.data.map(vpool => ({
          id: vpool.id,
          name: vpool.name,
          description: vpool.description,
          poolIds: vpool.poolIds || [],
          strategy: vpool.strategy || 'round_robin',
          status: vpool.status,
          createdAt: vpool.createdAt,
          updatedAt: vpool.updatedAt
        }))
      } else {
        throw new Error(response.message || '获取虚拟号池列表失败')
      }
    } catch (err) {
      console.error('获取虚拟号池列表失败:', err)
      error.value.virtual = err.message || '获取虚拟号池列表失败'
      
      // 使用模拟数据作为后备
      virtualPools.value = [
        {
          id: 1,
          name: '主要业务虚拟池',
          description: '主要业务的虚拟号池',
          poolIds: [1, 2],
          strategy: 'priority',
          status: 'active'
        },
        {
          id: 2,
          name: '测试虚拟池',
          description: '测试环境虚拟号池',
          poolIds: [3],
          strategy: 'round_robin',
          status: 'active'
        }
      ]
    } finally {
      loading.value.virtual = false
    }
  }

  // 创建号池
  const createPool = async (poolData) => {
    loading.value.create = true
    error.value.create = null
    
    try {
      const response = await poolApi.createPool(poolData)
      
      if (response.success) {
        const newPool = {
          id: response.data.id,
          name: poolData.name,
          description: poolData.description,
          status: 'healthy',
          pressure: 0,
          type: poolData.type || 'normal',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }
        
        pools.value.push(newPool)
        return newPool
      } else {
        throw new Error(response.message || '创建号池失败')
      }
    } catch (err) {
      console.error('创建号池失败:', err)
      error.value.create = err.message || '创建号池失败'
      throw err
    } finally {
      loading.value.create = false
    }
  }

  // 更新号池
  const updatePool = async (poolId, poolData) => {
    loading.value.update = true
    error.value.update = null
    try {
      const response = await poolApi.updatePool(poolId, poolData)
      if (response.success) {
        // 不再手动更新本地状态，依赖组件重新fetch
        return response.data
      } else {
        throw new Error(response.message || '更新号池失败')
      }
    } catch (err) {
      console.error('更新号池失败:', err)
      error.value.update = err.message || '更新号池失败'
      throw err
    } finally {
      loading.value.update = false
    }
  }

  // 删除号池
  const deletePool = async (poolId) => {
    loading.value.delete = true
    error.value.delete = null
    
    try {
      const response = await poolApi.deletePool(poolId)
      
      if (response.success) {
        const index = pools.value.findIndex(p => p.id === poolId)
        if (index !== -1) {
          pools.value.splice(index, 1)
        }
        
        // 清除相关状态数据
        delete poolStatuses.value[poolId]
        
        return true
      } else {
        throw new Error(response.message || '删除号池失败')
      }
    } catch (err) {
      console.error('删除号池失败:', err)
      error.value.delete = err.message || '删除号池失败'
      throw err
    } finally {
      loading.value.delete = false
    }
  }

  // 批量删除号池
  const deletePools = async (poolIds) => {
    loading.value.delete = true
    error.value.delete = null
    try {
      const response = await poolApi.deletePools(poolIds)
      if (!response.success) {
        throw new Error(response.message || '批量删除号池失败')
      }
      return true
    } catch (err) {
      console.error('批量删除号池失败:', err)
      error.value.delete = err.message || '批量删除号池失败'
      throw err
    } finally {
      loading.value.delete = false
    }
  }
  
  // 切换号池启用状态
  const togglePoolEnabled = async (poolId, enabled) => {
    // 这是一个乐观的更新，可以先在UI上反馈，但为了数据一致性，最好还是等待API调用成功
    try {
      const response = await poolApi.togglePoolEnabled(poolId, enabled)
      if (!response.success) {
        throw new Error(response.message || '切换状态失败')
      }
      // 成功后，让调用者去刷新列表
    } catch (err) {
      console.error(`切换号池 ${poolId} 状态失败:`, err)
      // 向上抛出错误，以便UI可以显示通知
      throw err
    }
  }

  // 创建虚拟号池
  const createVirtualPool = async (virtualPoolData) => {
    loading.value.create = true
    error.value.create = null
    
    try {
      const response = await virtualPoolApi.createVirtualPool(virtualPoolData)
      
      if (response.success) {
        const newVirtualPool = {
          id: response.data.id,
          name: virtualPoolData.name,
          description: virtualPoolData.description,
          poolIds: virtualPoolData.poolIds,
          strategy: virtualPoolData.strategy,
          status: 'active',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }
        
        virtualPools.value.push(newVirtualPool)
        return newVirtualPool
      } else {
        throw new Error(response.message || '创建虚拟号池失败')
      }
    } catch (err) {
      console.error('创建虚拟号池失败:', err)
      error.value.create = err.message || '创建虚拟号池失败'
      throw err
    } finally {
      loading.value.create = false
    }
  }

  // 获取所有可用的数据源类型
  const fetchDataSourceTypes = async () => {
    // 如果已经获取过，则不再重复获取
    if (dataSourceTypes.value.length > 0) {
      return
    }
    
    // 这个加载过程通常很快，并且只执行一次，可以不设置独立的 loading 状态
    // 如果需要，可以设置 loading.value.pools = true
    error.value.pools = null
    try {
      const response = await dataSourceApi.getDataSourceTypes()
      if (response.success) {
        if (Array.isArray(response.data)) {
          dataSourceTypes.value = response.data
        } else {
          console.error('Expected an array for data source types, but got:', response.data)
          throw new Error('获取数据源类型失败：返回的数据格式不正确')
        }
      } else {
        throw new Error(response.message || '获取数据源类型失败')
      }
    } catch (err) {
      console.error('获取数据源类型失败:', err)
      error.value.pools = err.message || '加载数据源类型时出错'
    }
  }

  // 获取号池详情
  const getPool = (poolId) => {
    return pools.value.find(pool => pool.id === poolId) || null
  }

  // 获取号池状态
  const getPoolStatus = (poolId) => {
    return poolStatuses.value[poolId] || null
  }

  // 获取虚拟号池详情
  const getVirtualPool = (virtualPoolId) => {
    return virtualPools.value.find(vpool => vpool.id === virtualPoolId) || null
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
    pools.value = []
    poolStatuses.value = {}
    virtualPools.value = []
    Object.keys(loading.value).forEach(key => {
      loading.value[key] = false
    })
    clearError()
  }

  return {
    // 状态
    pools,
    poolStatuses,
    virtualPools,
    pagination,
    dataSourceTypes,
    loading,
    error,
    
    // 计算属性
    isLoading,
    hasError,
    activePools,
    poolsWithStatus,
    
    // 方法
    fetchPools,
    fetchPoolStatus,
    fetchAllPoolStatuses,
    fetchVirtualPools,
    setCurrentPage,
    fetchDataSourceTypes,
    createPool,
    updatePool,
    deletePool,
    deletePools,
    togglePoolEnabled,
    createVirtualPool,
    getPool,
    getPoolStatus,
    getVirtualPool,
    clearError,
    reset
  }
})