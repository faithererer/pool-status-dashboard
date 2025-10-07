<template>
  <div class="search-filter">
    <div class="search-section">
      <div class="search-input-wrapper">
        <svg class="search-icon" width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          class="search-input"
          placeholder="搜索号池名称或描述..."
          @input="handleSearch"
        />
        <BaseButton
          v-if="searchQuery"
          variant="ghost"
          size="small"
          class="clear-btn"
          @click="clearSearch"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path d="M3.72 3.72a.75.75 0 011.06 0L8 6.94l3.22-3.22a.75.75 0 111.06 1.06L9.06 8l3.22 3.22a.75.75 0 11-1.06 1.06L8 9.06l-3.22 3.22a.75.75 0 01-1.06-1.06L6.94 8 3.72 4.78a.75.75 0 010-1.06z"/>
          </svg>
        </BaseButton>
      </div>
    </div>
    
    <div class="filter-section">
      <div class="filter-group">
        <label class="filter-label">状态</label>
        <select
          v-model="filters.status"
          class="filter-select"
          @change="handleFilterChange"
        >
          <option value="">全部状态</option>
          <option value="healthy">健康</option>
          <option value="warning">警告</option>
          <option value="critical">严重</option>
          <option value="offline">离线</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label class="filter-label">类型</label>
        <select
          v-model="filters.type"
          class="filter-select"
          @change="handleFilterChange"
        >
          <option value="">全部类型</option>
          <option value="main">主要业务池</option>
          <option value="backup">备用池</option>
          <option value="test">测试池</option>
          <option value="emergency">紧急备用池</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label class="filter-label">数据源</label>
        <select
          v-model="filters.dataSource"
          class="filter-select"
          @change="handleFilterChange"
        >
          <option value="">全部数据源</option>
          <option value="database">数据库</option>
          <option value="api">API接口</option>
          <option value="file">文件导入</option>
          <option value="mock">模拟数据</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label class="filter-label">压力范围</label>
        <select
          v-model="filters.pressureRange"
          class="filter-select"
          @change="handleFilterChange"
        >
          <option value="">全部范围</option>
          <option value="low">低压力 (0-30%)</option>
          <option value="medium">中压力 (31-60%)</option>
          <option value="high">高压力 (61-80%)</option>
          <option value="critical">严重压力 (81-100%)</option>
        </select>
      </div>
    </div>
    
    <div class="action-section">
      <BaseButton
        variant="ghost"
        size="small"
        @click="clearAllFilters"
        :disabled="!hasActiveFilters"
      >
        清空筛选
      </BaseButton>
      
      <div class="view-toggle">
        <BaseButton
          :variant="viewMode === 'table' ? 'primary' : 'ghost'"
          size="small"
          @click="$emit('view-mode-change', 'table')"
          title="表格视图"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path d="M0 1.75C0 .784.784 0 1.75 0h12.5C15.216 0 16 .784 16 1.75v3.5a.75.75 0 01-.75.75H.75a.75.75 0 01-.75-.75v-3.5zM0 7.75C0 6.784.784 6 1.75 6h12.5c.966 0 1.75.784 1.75 1.75v6.5A1.75 1.75 0 0114.25 16H1.75A1.75 1.75 0 010 14.25v-6.5z"/>
          </svg>
        </BaseButton>
        
        <BaseButton
          :variant="viewMode === 'grid' ? 'primary' : 'ghost'"
          size="small"
          @click="$emit('view-mode-change', 'grid')"
          title="网格视图"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path d="M1.75 0A1.75 1.75 0 000 1.75v3.5C0 6.216.784 7 1.75 7h3.5A1.75 1.75 0 007 5.25v-3.5A1.75 1.75 0 005.25 0h-3.5zM1.75 9A1.75 1.75 0 000 10.75v3.5C0 15.216.784 16 1.75 16h3.5A1.75 1.75 0 007 14.25v-3.5A1.75 1.75 0 005.25 9h-3.5zM9 1.75A1.75 1.75 0 0110.75 0h3.5A1.75 1.75 0 0116 1.75v3.5A1.75 1.75 0 0114.25 7h-3.5A1.75 1.75 0 019 5.25v-3.5zM10.75 9A1.75 1.75 0 009 10.75v3.5c0 .966.784 1.75 1.75 1.75h3.5A1.75 1.75 0 0016 14.25v-3.5A1.75 1.75 0 0014.25 9h-3.5z"/>
          </svg>
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref, reactive, computed, watch } from 'vue'
import { debounce } from 'lodash-es'

export default defineComponent({
  name: 'SearchFilter',
  props: {
    viewMode: {
      type: String,
      default: 'table',
      validator: (value) => ['table', 'grid'].includes(value)
    }
  },
  emits: ['search', 'filter-change', 'view-mode-change'],
  setup(props, { emit }) {
    const searchQuery = ref('')
    const filters = reactive({
      status: '',
      type: '',
      dataSource: '',
      pressureRange: ''
    })
    
    const hasActiveFilters = computed(() => {
      return searchQuery.value || 
             Object.values(filters).some(value => value !== '')
    })
    
    // 防抖搜索
    const debouncedSearch = debounce((query) => {
      emit('search', query)
    }, 300)
    
    const handleSearch = () => {
      debouncedSearch(searchQuery.value)
    }
    
    const handleFilterChange = () => {
      emit('filter-change', { ...filters })
    }
    
    const clearSearch = () => {
      searchQuery.value = ''
      emit('search', '')
    }
    
    const clearAllFilters = () => {
      searchQuery.value = ''
      Object.keys(filters).forEach(key => {
        filters[key] = ''
      })
      emit('search', '')
      emit('filter-change', { ...filters })
    }
    
    // 监听filters变化
    watch(filters, () => {
      handleFilterChange()
    }, { deep: true })
    
    return {
      searchQuery,
      filters,
      hasActiveFilters,
      handleSearch,
      handleFilterChange,
      clearSearch,
      clearAllFilters
    }
  }
})
</script>

<style lang="scss" scoped>
.search-filter {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-4;
  align-items: flex-end;
  padding: $spacing-4;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $radius-lg;
  margin-bottom: $spacing-4;
}

.search-section {
  flex: 1;
  min-width: 280px;
  
  .search-input-wrapper {
    position: relative;
    display: flex;
    align-items: center;
  }
  
  .search-icon {
    position: absolute;
    left: $spacing-3;
    color: $gray-400;
    pointer-events: none;
    z-index: 1;
  }
  
  .search-input {
    width: 100%;
    padding: $spacing-3 $spacing-12 $spacing-3 $spacing-10;
    background: $bg-card;
    border: 1px solid $border-color;
    border-radius: $radius-md;
    color: $gray-100;
    font-size: $font-size-sm;
    transition: all $transition-normal;
    
    &:focus {
      outline: none;
      border-color: $primary-500;
      box-shadow: 0 0 0 3px rgba($primary-500, 0.1);
    }
    
    &::placeholder {
      color: $gray-500;
    }
  }
  
  .clear-btn {
    position: absolute;
    right: $spacing-2;
    padding: $spacing-1;
    color: $gray-400;
    
    &:hover {
      color: $gray-200;
    }
  }
}

.filter-section {
  display: flex;
  gap: $spacing-3;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
  min-width: 120px;
  
  .filter-label {
    font-size: $font-size-xs;
    font-weight: $font-weight-medium;
    color: $gray-400;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }
  
  .filter-select {
    padding: $spacing-2 $spacing-3;
    background: $bg-card;
    border: 1px solid $border-color;
    border-radius: $radius-md;
    color: $gray-100;
    font-size: $font-size-sm;
    transition: all $transition-normal;
    
    &:focus {
      outline: none;
      border-color: $primary-500;
      box-shadow: 0 0 0 3px rgba($primary-500, 0.1);
    }
    
    option {
      background: $bg-card;
      color: $gray-100;
    }
  }
}

.action-section {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  
  .view-toggle {
    display: flex;
    gap: $spacing-1;
    padding: $spacing-1;
    background: $bg-card;
    border-radius: $radius-md;
    border: 1px solid $border-color;
  }
}

// 响应式设计
@media (max-width: $breakpoint-lg) {
  .search-filter {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-section {
    min-width: auto;
  }
  
  .filter-section {
    justify-content: space-between;
  }
  
  .filter-group {
    flex: 1;
    min-width: 100px;
  }
  
  .action-section {
    justify-content: space-between;
  }
}

@media (max-width: $breakpoint-md) {
  .filter-section {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: $spacing-3;
  }
  
  .action-section {
    flex-direction: column;
    gap: $spacing-2;
    
    .view-toggle {
      align-self: stretch;
      justify-content: center;
    }
  }
}

@media (max-width: $breakpoint-sm) {
  .search-filter {
    padding: $spacing-3;
  }
  
  .filter-section {
    grid-template-columns: 1fr;
  }
  
  .search-input {
    padding: $spacing-2 $spacing-10 $spacing-2 $spacing-8;
  }
  
  .search-icon {
    left: $spacing-2;
    width: 16px;
    height: 16px;
  }
}
</style>