<template>
  <div class="pool-detail-section card">
    <div class="pool-detail-header">
      <h3 class="pool-detail-title">{{ pool.name }}</h3>
      <div class="pool-status">
        <span 
          class="badge"
          :class="getStatusClass(pool.status)"
        >
          {{ getStatusText(pool.status) }}
        </span>
      </div>
    </div>
    
    <!-- 统计数据网格 -->
    <div class="pool-stats-grid">
      <div 
        v-for="stat in stats" 
        :key="stat.key"
        class="stat-item"
      >
        <div 
          class="stat-value"
          :style="{ color: stat.color }"
        >
          {{ formatNumber(stat.value) }}
        </div>
        <div class="stat-label">{{ stat.label }}</div>
      </div>
    </div>

    <!-- 压力仪表盘 -->
    <div class="pressure-gauge">
      <PressureGauge 
        :value="pool.pressure || 0"
        :size="280"
      />
      <div 
        class="pressure-text"
        :style="{ color: getPressureColor(pool.pressure) }"
      >
        {{ pool.pressure || 0 }}%
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, computed } from 'vue'
import PressureGauge from '@/components/charts/PressureGauge.vue'

export default defineComponent({
  name: 'PoolDetail',
  components: {
    PressureGauge
  },
  props: {
    pool: {
      type: Object,
      required: true
    },
    poolStatus: {
      type: Object,
      default: null
    }
  },
  setup(props) {
    // 计算统计数据
    const stats = computed(() => {
      const status = props.poolStatus || {}

      return [
        {
          key: 'valid',
          label: '有效数量',
          value: status.validCount || 0,
          color: '#22c55e'
        },
        {
          key: 'invalid',
          label: '无效数量',
          value: status.invalidCount || 0,
          color: '#ef4444'
        },
        {
          key: 'cooling',
          label: '冷却中',
          value: status.coolingCount || 0,
          color: '#fbbf24'
        },
        {
          key: 'total',
          label: '总数量',
          value: status.totalCount || 0,
          color: '#94a3b8'
        }
      ]
    })
    
    // 格式化数字
    const formatNumber = (value) => {
      if (value >= 10000) {
        return (value / 10000).toFixed(1) + '万'
      } else if (value >= 1000) {
        return (value / 1000).toFixed(1) + 'k'
      }
      return value.toLocaleString()
    }
    
    // 获取压力颜色
    const getPressureColor = (pressure) => {
      if (pressure >= 80) return '#ef4444'
      if (pressure >= 60) return '#fbbf24'
      if (pressure >= 40) return '#06b6d4'
      return '#22c55e'
    }
    
    // 获取状态样式类
    const getStatusClass = (status) => {
      switch (status) {
        case 'healthy': return 'badge--success'
        case 'warning': return 'badge--warning'
        case 'critical': return 'badge--danger'
        case 'offline': return 'badge--info'
        default: return 'badge--info'
      }
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      switch (status) {
        case 'healthy': return '健康'
        case 'warning': return '警告'
        case 'critical': return '危险'
        case 'offline': return '离线'
        default: return '未知'
      }
    }
    
    return {
      stats,
      formatNumber,
      getPressureColor,
      getStatusClass,
      getStatusText
    }
  }
})
</script>

<style lang="scss" scoped>
.pool-detail-section {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.pool-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem; /* $spacing-5 */
  padding-bottom: 0.75rem; /* $spacing-3 */
  border-bottom: 1px solid $border-color;
}

.pool-detail-title {
  font-size: 1.25rem; /* $font-size-xl */
  font-weight: 600; /* $font-weight-semibold */
  color: $gray-100;
  margin: 0;
}

.pool-status {
  flex-shrink: 0;
}

// 统计数据网格
.pool-stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem; /* $spacing-4 */
  margin-bottom: 1.25rem; /* $spacing-5 */
}

.stat-item {
  text-align: center;
  padding: 1rem; /* $spacing-4 */
  background: $bg-overlay;
  border-radius: 0.75rem; /* $radius-lg */
  border: 1px solid $border-color;
  transition: all 0.3s ease; /* $transition-normal */
  
  &:hover {
    border-color: $border-hover;
    transform: translateY(-1px);
  }
}

.stat-value {
  font-size: 1.5rem; /* $font-size-2xl */
  font-weight: 700; /* $font-weight-bold */
  margin-bottom: 0.5rem; /* $spacing-2 */
  line-height: 1.2;
}

.stat-label {
  font-size: 0.875rem; /* $font-size-sm */
  color: $gray-400;
  font-weight: 500; /* $font-weight-medium */
}

// 压力仪表盘
.pressure-gauge {
  text-align: center;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.pressure-text {
  font-size: 1.875rem; /* $font-size-3xl */
  font-weight: 700; /* $font-weight-bold */
  margin-top: 1rem; /* $spacing-4 */
  text-shadow: 0 0 10px currentColor;
}

// 响应式设计
@media (max-width: 1024px) { /* $breakpoint-lg */
  .pool-detail-header {
    flex-direction: column;
    gap: 0.75rem; /* $spacing-3 */
    text-align: center;
  }
}

@media (max-width: 768px) { /* $breakpoint-md */
  .pool-stats-grid {
    grid-template-columns: 1fr;
    gap: 0.75rem; /* $spacing-3 */
  }
  
  .stat-item {
    padding: 0.75rem; /* $spacing-3 */
  }
  
  .stat-value {
    font-size: 1.25rem; /* $font-size-xl */
  }
  
  .pressure-text {
    font-size: 1.5rem; /* $font-size-2xl */
  }
}

@media (max-width: 640px) { /* $breakpoint-sm */
  .pool-detail-section {
    padding: 1rem; /* $spacing-4 */
  }
  
  .pool-detail-title {
    font-size: 1.125rem; /* $font-size-lg */
  }
  
  .pressure-gauge {
    min-height: 150px;
  }
}
</style>