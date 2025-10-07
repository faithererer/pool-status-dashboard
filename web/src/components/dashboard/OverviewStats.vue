<template>
  <section class="overview-section">
    <h3 class="overview-title">总览统计</h3>
    <div class="overview-stats">
      <div
        v-for="stat in stats"
        :key="stat.key"
        class="overview-card"
        :style="{ '--accent-color': stat.color }"
        :class="{ 'pressure-card': stat.key === 'averagePressure' || stat.key === 'pressure' }"
      >
        <div v-if="stat.key === 'averagePressure' || stat.key === 'pressure'" class="gauge-container">
          <PressureGauge :pressure="stat.value" :size="120" />
        </div>
        <div v-else class="overview-value">{{ formatValue(stat.value, stat) }}</div>
        <div v-if="stat.key !== 'averagePressure' && stat.key !== 'pressure'" class="overview-label">
          {{ stat.label }}
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import { defineComponent, computed } from 'vue'
import PressureGauge from '@/components/charts/PressureGauge.vue'

export default defineComponent({
  name: 'OverviewStats',
  components: {
    PressureGauge
  },
  props: {
    statsData: {
      type: Array,
      default: () => []
    }
  },
  setup(props) {
    // 统计数据现在直接来自 prop
    const stats = computed(() => {
      if (props.statsData && props.statsData.length > 0) {
        return props.statsData
      }
      // 提供一个默认的骨架屏结构，防止在数据加载时出现空白
      return [
        { key: 'stat1', label: '...', value: 0, color: '#94a3b8' },
        { key: 'stat2', label: '...', value: 0, color: '#94a3b8' },
        { key: 'stat3', label: '...', value: 0, color: '#94a3b8' },
        { key: 'stat4', label: '...', value: 0, color: '#94a3b8' },
        { key: 'stat5', label: '...', value: 0, color: '#94a3b8' },
        { key: 'stat6', label: '...', value: 0, color: '#94a3b8' },
      ]
    })

    // 格式化数值显示
    const formatValue = (value, stat) => {
      // 如果值已经是字符串且包含%，直接返回
      if (typeof value === 'string' && value.includes('%')) {
        return value
      }

      
      // 如果是百分比类型，添加%符号
      if (stat && stat.isPercentage) {
        const numValue = typeof value === 'number' ? value : parseFloat(value) || 0
        return `${Math.round(numValue)}%`
      }
      
      // 如果是数字类型
      if (typeof value === 'number') {
        if (value >= 10000) {
          return (value / 10000).toFixed(1) + '万'
        } else if (value >= 1000) {
          return (value / 1000).toFixed(1) + 'k'
        }
        return value.toLocaleString()
      }
      
      // 其他情况直接返回
      return value || 0
    }
    
    return {
      stats,
      formatValue
    }
  }
})
</script>

<style lang="scss" scoped>
.overview-section {
  margin-bottom: 1.25rem; /* $spacing-5 */
  flex-shrink: 0;
}

.overview-title {
  font-size: 1.25rem; /* $font-size-xl */
  font-weight: 600; /* $font-weight-semibold */
  color: $gray-100;
  margin-bottom: 1rem; /* $spacing-4 */
}

.overview-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem; /* $spacing-4 */
}

.overview-card {
  background: $bg-card;
  backdrop-filter: blur(10px);
  border: 1px solid $border-color;
  border-radius: 0.75rem; /* $radius-lg */
  padding: 1.25rem; /* $spacing-5 */
  text-align: center;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease; /* $transition-normal */
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05); /* $shadow-lg */
    border-color: var(--accent-color);
  }
  
  // 顶部装饰条
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: var(--accent-color);
  }
}

.overview-card.pressure-card {
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.gauge-container {
  margin-bottom: -0.5rem; /* 调整仪表盘和标签的间距 */
}

.overview-value {
  font-size: 1.875rem; /* $font-size-3xl */
  font-weight: 700; /* $font-weight-bold */
  margin-bottom: 0.5rem; /* $spacing-2 */
  color: var(--accent-color);
  line-height: 1.2;
}

.overview-label {
  font-size: 0.875rem; /* $font-size-sm */
  color: $gray-400;
  font-weight: 500; /* $font-weight-medium */
}

// 响应式设计
@media (max-width: 768px) { /* $breakpoint-md */
  .overview-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) { /* $breakpoint-sm */
  .overview-stats {
    grid-template-columns: 1fr;
  }
  
  .overview-card {
    padding: 1rem; /* $spacing-4 */
  }
  
  .overview-value {
    font-size: 1.5rem; /* $font-size-2xl */
  }
}
</style>