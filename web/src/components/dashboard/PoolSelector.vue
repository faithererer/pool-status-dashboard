<template>
  <div class="pool-selector-section card">
    <h3 class="section-title">号池列表</h3>
    <div class="pool-list">
      <!-- 全局概览选项 -->
      <div
        class="pool-item overview-item"
        :class="{ active: !selectedPool }"
        @click="selectPool(null)"
      >
        <div class="pool-info">
          <h4 class="overview-title">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-globe"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>
            全局概览
          </h4>
          <p>查看所有号池的汇总数据</p>
        </div>
      </div>

      <div
        v-for="pool in pools"
        :key="pool.id"
        class="pool-item"
        :class="{ active: selectedPool && selectedPool.id === pool.id }"
        @click="selectPool(pool.id)"
      >
        <div class="pool-info">
          <h4>{{ pool.name }}</h4>
          <p>{{ pool.description || '暂无描述' }}</p>
        </div>
        <div class="pool-pressure">
          <div
            class="pressure-value"
            :style="{ color: getPressureColor(pool.pressure) }"
          >
            {{ pool.latestStatus ? pool.latestStatus.pressure : '-' }}%
          </div>
          <div class="pressure-status">
            <span
              class="badge"
              :class="getPressureStatusClass(pool.latestStatus ? pool.latestStatus.pressure : 0)"
            >
              {{ getPressureStatusText(pool.latestStatus ? pool.latestStatus.pressure : 0) }}
            </span>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="pools.length === 0" class="empty-state">
        <div class="empty-icon">📊</div>
        <p>暂无号池数据</p>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'PoolSelector',
  props: {
    pools: {
      type: Array,
      default: () => []
    },
    selectedPool: {
      type: Object,
      default: null
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['pool-selected'],
  setup(props, { emit }) {
    // 选择号池
    const selectPool = (poolId) => {
      emit('pool-selected', poolId)
    }
    
    // 获取压力颜色
    const getPressureColor = (pressure) => {
      if (pressure >= 80) return 'var(--red-500)'
      if (pressure >= 60) return 'var(--amber-400)'
      if (pressure >= 40) return 'var(--cyan-500)'
      return 'var(--green-500)'
    }
    
    // 获取压力状态样式类
    const getPressureStatusClass = (pressure) => {
      if (pressure >= 80) return 'badge--danger'
      if (pressure >= 60) return 'badge--warning'
      if (pressure >= 40) return 'badge--info'
      return 'badge--success'
    }
    
    // 获取压力状态文本
    const getPressureStatusText = (pressure) => {

      if (pressure >= 80) return '危险'
      if (pressure >= 60) return '警告'
      if (pressure >= 40) return '正常'
      return '健康'
    }
    
    return {
      selectPool,
      getPressureColor,
      getPressureStatusClass,
      getPressureStatusText
    }
  }
})
</script>

<style lang="scss" scoped>

.pool-selector-section {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.overview-item {
  border-style: dashed;
  border-color: rgba(148, 163, 184, 0.3);
}

.overview-title {
  display: flex;
  align-items: center;
  gap: 0.5rem; /* $spacing-2 */
}

.section-title {
  font-size: 1.25rem; /* $font-size-xl */
  font-weight: 600; /* $font-weight-semibold */
  margin-bottom: 1rem; /* $spacing-4 */
  color: $gray-100;
}

.pool-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem; /* $spacing-3 */
  overflow-y: auto;
  flex: 1;
  padding-right: 0.25rem; /* $spacing-1 - 为滚动条留空间 */
}

.pool-item {
  padding: 1rem; /* $spacing-4 */
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: 0.75rem; /* $radius-lg */
  cursor: pointer;
  transition: all 0.3s ease; /* $transition-normal */
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  &:hover {
    background: $bg-card-hover;
    border-color: $border-hover;
    transform: translateY(-1px);
  }
  
  &.active {
    background: rgba($primary-color, 0.2);
    border-color: $border-active;
    box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
  }
}

.pool-info {
  flex: 1;
  min-width: 0; // 防止文本溢出
  
  h4 {
    font-size: 1rem; /* $font-size-base */
    font-weight: 600; /* $font-weight-semibold */
    margin-bottom: 0.25rem; /* $spacing-1 */
    color: $gray-100;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  p {
    font-size: 0.875rem; /* $font-size-sm */
    color: $gray-400;
    margin: 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.pool-pressure {
  text-align: right;
  flex-shrink: 0;
  margin-left: 0.75rem; /* $spacing-3 */
}

.pressure-value {
  font-size: 1.125rem; /* $font-size-lg */
  font-weight: 700; /* $font-weight-bold */
  line-height: 1.2;
  margin-bottom: 0.25rem; /* $spacing-1 */
}

.pressure-status {
  .badge {
    font-size: 0.75rem; /* $font-size-xs */
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem; /* $spacing-8 $spacing-4 */
  color: $gray-400;
  text-align: center;
  
  .empty-icon {
    font-size: 3rem;
    margin-bottom: 1rem; /* $spacing-4 */
    opacity: 0.5;
  }
  
  p {
    margin: 0;
    font-size: 0.875rem; /* $font-size-sm */
  }
}

// 加载状态
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.5rem 1rem; /* $spacing-6 $spacing-4 */
  color: $gray-400;
  text-align: center;
  
  .loading-spinner {
    margin-bottom: 0.75rem; /* $spacing-3 */
  }
  
  p {
    margin: 0;
    font-size: 0.875rem; /* $font-size-sm */
  }
}

// 响应式设计
@media (max-width: 1400px) {
  .pool-selector-section {
    max-height: 200px;
  }
  
  .pool-list {
    flex-direction: row;
    overflow-x: auto;
    overflow-y: hidden;
    gap: 1rem; /* $spacing-4 */
  }
  
  .pool-item {
    min-width: 200px;
    flex-shrink: 0;
  }
}

@media (max-width: 768px) { /* $breakpoint-md */
  .pool-item {
    padding: 0.75rem; /* $spacing-3 */
    
    .pool-info h4 {
      font-size: 0.875rem; /* $font-size-sm */
    }
    
    .pool-info p {
      font-size: 0.75rem; /* $font-size-xs */
    }
    
    .pressure-value {
      font-size: 1rem; /* $font-size-base */
    }
  }
}
</style>