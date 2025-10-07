<template>
  <div class="dashboard-view">
    <div class="container">
      <!-- 总览统计区域 -->
      <OverviewStats :stats-data="overviewDisplayData" class="fade-in" />
      
      <!-- 主要内容区域 - 两栏布局 -->
      <div class="main-layout fade-in">
        <!-- 左侧号池选择器 -->
        <PoolSelector
          :pools="pools"
          :selected-pool="selectedPool"
          @pool-selected="handlePoolSelected"
        />
        
        <!-- 右侧图表区域 -->
        <div class="chart-area card" v-if="selectedPool">
          <TrendChart
            :pool-id="selectedPool.id"
          :data="chartData"
          @time-range-change="handleTimeRangeUpdate"
        />
        </div>
         <div class="chart-area-placeholder card" v-else>
          <div class="placeholder-content">
            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><path d="M20.2 7.8l-7.7 7.7-4-4-5.7 5.7"/><path d="M15 7h6v6"/></svg>
            <h3>请从左侧选择一个号池</h3>
            <p>选择后将在此处查看其详细的历史趋势数据</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted, computed } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'
import OverviewStats from '@/components/dashboard/OverviewStats.vue'
import PoolSelector from '@/components/dashboard/PoolSelector.vue'
import TrendChart from '@/components/charts/TrendChart.vue'

export default defineComponent({
  name: 'DashboardView',
  components: {
    OverviewStats,
    PoolSelector,
    TrendChart
  },
  setup() {
    const dashboardStore = useDashboardStore()
    // 直接从 store 获取计算好的 selectedPool，它包含了最新的状态
    const selectedPool = computed(() => dashboardStore.selectedPool)
    
    const pools = computed(() => dashboardStore.publicPools)

    const chartData = computed(() => {
      if (!selectedPool.value) return null
      return dashboardStore.getChartData(selectedPool.value.id)
    })

    const overviewDisplayData = computed(() => {
      if (selectedPool.value && selectedPool.value.latestStatus) {
        // 显示选中号池的数据
        const status = selectedPool.value.latestStatus
        return [
          { key: 'valid', label: '有效数量', value: status.validCount, color: '#22c55e' },
          { key: 'invalid', label: '无效数量', value: status.invalidCount, color: '#ef4444' },
          { key: 'cooling', label: '冷却中', value: status.coolingCount, color: '#fbbf24' },
          { key: 'total', label: '总数量', value: status.totalCount, color: '#94a3b8' },
          { key: 'pressure', label: '当前压力', value: status.pressure, color: '#8b5cf6', isPercentage: true },
        ]
      } else if (selectedPool.value) {
        // 如果选中了号池但没有状态数据，显示占位符
        return [
          { key: 'valid', label: '有效数量', value: '-', color: '#22c55e' },
          { key: 'invalid', label: '无效数量', value: '-', color: '#ef4444' },
          { key: 'cooling', label: '冷却中', value: '-', color: '#fbbf24' },
          { key: 'total', label: '总数量', value: '-', color: '#94a3b8' },
          { key: 'pressure', label: '当前压力', value: '-', color: '#8b5cf6', isPercentage: true },
        ]
      }
      else {
        // 显示全局总览数据
        const overview = dashboardStore.overviewStats
        return [
          { key: 'activePools', label: '活跃号池', value: overview.activePools || 0, color: '#22c55e' },
          { key: 'totalValid', label: '总有效数量', value: overview.totalValidCount || 0, color: '#3b82f6' },
          { key: 'totalInvalid', label: '总无效数量', value: overview.totalInvalidCount || 0, color: '#ef4444' },
          { key: 'totalCooling', label: '冷却中数量', value: overview.totalCoolingCount || 0, color: '#fbbf24' },
          { key: 'totalCount', label: '总数量', value: overview.totalCount || 0, color: '#94a3b8' },
          { key: 'averagePressure', label: '平均压力', value: overview.avgPressure || 0, color: '#8b5cf6', isPercentage: true }
        ]
      }
    })
    
    // 简化事件处理，只调用 store 的 action，由 store 负责后续的数据获取
    const handlePoolSelected = (poolId) => {
      dashboardStore.setSelectedPool(poolId)
    }
    
    const handleTimeRangeUpdate = async (timeRange) => {
      if (selectedPool.value) {
        await dashboardStore.fetchPoolHistory(selectedPool.value.id, timeRange)
      }
    }
    
    onMounted(async () => {
      // store 的 fetchPublicPools 会自动处理首次加载时选中第一个号池并获取其数据
      await dashboardStore.fetchOverviewStats()
      await dashboardStore.fetchPublicPools()
      
      dashboardStore.startAutoRefresh()
    })
    
    return {
      pools,
      selectedPool,
      chartData,
      overviewDisplayData,
      handlePoolSelected,
      handleTimeRangeUpdate
    }
  }
})
</script>

<style lang="scss" scoped>

.dashboard-view {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.container {
  max-width: 1600px;
  margin: 0 auto;
  padding: 1.25rem; /* $spacing-5 */
  width: 100%;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.main-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 1.25rem; /* $spacing-5 */
  flex: 1;
  min-height: 0;
  margin-top: 1.25rem; /* $spacing-5 */
}

.chart-area {
  padding: 1.5rem; /* $spacing-6 */
  display: flex;
  flex-direction: column;
}

.chart-area-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b; /* $gray-500 */
  text-align: center;
  
  .placeholder-content {
    max-width: 300px;
    h3 {
      font-size: 1.25rem; /* $font-size-xl */
      color: #cbd5e1; /* $gray-300 */
      margin-top: 1rem; /* $spacing-4 */
    }
    p {
      font-size: 0.875rem; /* $font-size-sm */
    }
  }
}


// 响应式设计
@media (max-width: 1200px) {
  .main-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) { /* $breakpoint-md */
  .container {
    padding: 1rem; /* $spacing-4 */
  }
  
  .main-layout {
    gap: 1rem; /* $spacing-4 */
  }
}
</style>