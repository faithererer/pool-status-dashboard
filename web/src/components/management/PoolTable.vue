<template>
  <div class="pool-table">
    <div class="table-container">
      <table class="table">
        <thead>
          <tr>
            <th class="checkbox-col">
              <input type="checkbox" @change="toggleSelectAll" :checked="isAllSelected" :indeterminate="isIndeterminate" />
            </th>
            <th class="sortable" @click="sort('name')">
              号池名称 <SortIcon :direction="getSortDirection('name')" />
            </th>
            <th>启用状态</th>
            <th class="sortable" @click="sort('dataSourceClass')">
              数据源类 <SortIcon :direction="getSortDirection('dataSourceClass')" />
            </th>
            <th>最后更新状态</th>
            <th class="sortable" @click="sort('lastUpdateTime')">
              最后更新时间 <SortIcon :direction="getSortDirection('lastUpdateTime')" />
            </th>
            <th class="actions-col">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="pool in sortedPools" :key="pool.id" class="table-row" :class="{ 'is-selected': isSelected(pool.id) }">
            <td class="checkbox-col" data-label="选择">
              <input type="checkbox" :checked="isSelected(pool.id)" @change="togglePoolSelection(pool.id)" />
            </td>
            <td class="pool-name-cell" data-label="号池名称">
              <div class="pool-info">
                <h4 class="pool-name">{{ pool.name }}</h4>
                <p v-if="pool.description" class="pool-description">{{ pool.description }}</p>
              </div>
            </td>
            <td class="status-cell" data-label="启用状态">
              <label class="toggle-switch" @click.stop>
                <input type="checkbox" :checked="pool.enabled" @change="$emit('toggle-enabled', pool)">
                <span class="slider"></span>
              </label>
            </td>
            <td class="datasource-cell" data-label="数据源类">
              <span class="code-font">{{ pool.dataSourceClass }}</span>
            </td>
            <td class="status-cell" data-label="最后更新状态">
              <BaseBadge :variant="getLastUpdateStatusVariant(pool.lastUpdateStatus)">
                {{ pool.lastUpdateStatus || '-' }}
              </BaseBadge>
            </td>
            <td class="time-cell" data-label="最后更新时间">
              <span class="time">{{ formatTime(pool.lastUpdateTime) || '-' }}</span>
            </td>
            <td class="actions-cell" data-label="操作">
              <div class="actions">
                <BaseButton variant="ghost" size="small" @click="$emit('view-details', pool)" title="查看详情">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor"><path d="M8 9.5a1.5 1.5 0 100-3 1.5 1.5 0 000 3z"/><path fill-rule="evenodd" d="M1.38 8.28a.87.87 0 010-.566 7.003 7.003 0 0113.24.006.87.87 0 010 .566A7.003 7.003 0 011.38 8.28zM3 8a5 5 0 1010 0A5 5 0 003 8z" clip-rule="evenodd"/></svg>
                </BaseButton>
                <BaseButton variant="ghost" size="small" @click="$emit('edit', pool)" title="编辑">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor"><path d="M11.013 1.427a1.75 1.75 0 012.474 0l1.086 1.086a1.75 1.75 0 010 2.474l-8.61 8.61c-.21.21-.47.364-.756.445l-3.251.93a.75.75 0 01-.927-.928l.929-3.25a1.75 1.75 0 01.445-.758l8.61-8.61zm1.414 1.06a.25.25 0 00-.354 0L10.811 3.75l1.439 1.44 1.263-1.263a.25.25 0 000-.354l-1.086-1.086zM11.189 6.25L9.75 4.81l-6.286 6.287a.25.25 0 00-.064.108l-.558 1.953 1.953-.558a.249.249 0 00.108-.064l6.286-6.286z"/></svg>
                </BaseButton>
                <BaseButton variant="ghost" size="small" @click="$emit('delete', pool)" title="删除" class="delete-btn">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor"><path fill-rule="evenodd" d="M6.5 1.75a.25.25 0 01.25-.25h2.5a.25.25 0 01.25.25V3h-3V1.75zm4.5 0V3h2.25a.75.75 0 010 1.5H2.75a.75.75 0 010-1.5H5V1.75C5 .784 5.784 0 6.75 0h2.5C10.216 0 11 .784 11 1.75zM4.496 6.675a.75.75 0 10-1.492.15l.66 6.6A1.75 1.75 0 005.405 15h5.19c.9 0 1.652-.681 1.741-1.576l.66-6.6a.75.75 0 00-1.492-.149l-.66 6.6a.25.25 0 01-.249.225h-5.19a.25.25 0 01-.249-.225l-.66-6.6z" clip-rule="evenodd"/></svg>
                </BaseButton>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref, computed } from 'vue'
import SortIcon from './SortIcon.vue'

export default defineComponent({
  name: 'PoolTable',
  components: {
    SortIcon
  },
  props: {
    pools: {
      type: Array,
      required: true
    },
    selectedPools: {
      type: Array,
      required: true,
    }
  },
  emits: ['edit', 'delete', 'view-details', 'toggle-enabled', 'update:selectedPools'],
  setup(props, { emit }) {
    const sortField = ref('name')
    const sortDirection = ref('asc')

    const isSelected = (poolId) => props.selectedPools.includes(poolId)

    const isAllSelected = computed(() => {
      if (props.pools.length === 0) return false
      return props.pools.every(p => props.selectedPools.includes(p.id))
    })

    const isIndeterminate = computed(() => {
      return !isAllSelected.value && props.selectedPools.length > 0 && props.pools.some(p => props.selectedPools.includes(p.id))
    })

    const togglePoolSelection = (poolId) => {
      const newSelection = [...props.selectedPools]
      const index = newSelection.indexOf(poolId)
      if (index > -1) {
        newSelection.splice(index, 1)
      } else {
        newSelection.push(poolId)
      }
      emit('update:selectedPools', newSelection)
    }

    const toggleSelectAll = (event) => {
      const isChecked = event.target.checked
      let newSelection
      if (isChecked) {
        newSelection = props.pools.map(p => p.id)
      } else {
        newSelection = []
      }
      emit('update:selectedPools', newSelection)
    }

    const sortedPools = computed(() => {
      const pools = [...props.pools]
      return pools.sort((a, b) => {
        let aVal = a[sortField.value]
        let bVal = b[sortField.value]
        
        if (sortField.value === 'lastUpdateTime') {
          aVal = aVal ? new Date(aVal).getTime() : 0
          bVal = bVal ? new Date(bVal).getTime() : 0
        } else if (typeof aVal === 'string') {
          aVal = aVal.toLowerCase()
          bVal = bVal.toLowerCase()
        }
        
        if (aVal < bVal) return sortDirection.value === 'asc' ? -1 : 1
        if (aVal > bVal) return sortDirection.value === 'asc' ? 1 : -1
        return 0
      })
    })

    const sort = (field) => {
      if (sortField.value === field) {
        sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
      } else {
        sortField.value = field
        sortDirection.value = 'asc'
      }
    }

    const getSortDirection = (field) => {
      if (sortField.value === field) return sortDirection.value
      return null
    }

    const getLastUpdateStatusVariant = (status) => {
      if (status === 'success') return 'success'
      if (status === 'failure') return 'danger'
      return 'default'
    }

    const formatTime = (time) => {
      if (!time) return '-'
      const date = new Date(time)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    return {
      sortedPools,
      sort,
      getSortDirection,
      getLastUpdateStatusVariant,
      formatTime,
      isSelected,
      isAllSelected,
      isIndeterminate,
      togglePoolSelection,
      toggleSelectAll
    }
  }
})
</script>

<style lang="scss" scoped>
.pool-table {
  .table-container {
    overflow-x: auto;
  }
  
  .table {
    width: 100%;
    border-collapse: collapse;
    
    th, td {
      padding: $spacing-4;
      text-align: left;
      border-bottom: 1px solid $border-color;
    }
    
    th {
      background: $bg-card;
      color: $gray-300;
      font-weight: $font-weight-semibold;
      font-size: $font-size-sm;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      position: sticky;
      top: 0;
      z-index: 1;
      
      &.sortable {
        cursor: pointer;
        user-select: none;
        transition: background-color $transition-normal;
        
        &:hover {
          background: $bg-card-hover;
        }
      }
    }
    
    .table-row {
      transition: background-color $transition-normal;
      
      &:hover {
        background: $bg-card-hover;
      }
      
      &.is-selected {
        background-color: rgba($primary-500, 0.1);
        
        &:hover {
          background-color: rgba($primary-500, 0.2);
        }
      }
    }
  }
}

.checkbox-col {
  width: 48px;
  text-align: center;
}

.pool-name-cell {
  min-width: 200px;
  
  .pool-info {
    .pool-name {
      font-size: $font-size-base;
      font-weight: $font-weight-semibold;
      color: $gray-50;
      margin: 0 0 $spacing-1 0;
    }
    
    .pool-description {
      font-size: $font-size-sm;
      color: $gray-400;
      margin: 0;
      line-height: 1.4;
    }
  }
}

.status-cell {
  min-width: 100px;
}

.datasource-cell {
  min-width: 250px;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  
  .code-font {
    font-family: $font-mono;
    font-size: $font-size-xs;
    color: $gray-400;
  }
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 22px;

  input {
    opacity: 0;
    width: 0;
    height: 0;
  }

  .slider {
    position: absolute;
    cursor: pointer;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: $gray-600;
    transition: .4s;
    border-radius: 22px;

    &:before {
      position: absolute;
      content: "";
      height: 16px;
      width: 16px;
      left: 3px;
      bottom: 3px;
      background-color: white;
      transition: .4s;
      border-radius: 50%;
    }
  }

  input:checked + .slider {
    background-color: $primary-500;
  }

  input:checked + .slider:before {
    transform: translateX(18px);
  }
}

.time-cell {
  min-width: 120px;
  
  .time {
    font-size: $font-size-sm;
    color: $gray-400;
  }
}

.actions-cell {
  min-width: 120px;
  
  .actions {
    display: flex;
    gap: $spacing-1;
  }
  
  .delete-btn {
    &:hover {
      color: $red-400;
    }
  }
}

.actions-col {
  width: 120px;
}

// 响应式设计
@media (max-width: $breakpoint-lg) {
  .table {
    font-size: $font-size-sm;
    
    th, td {
      padding: $spacing-3;
    }
  }
  
  .pool-name-cell {
    min-width: 150px;
  }
  
  .actions {
    flex-direction: column;
    gap: $spacing-1;
  }
}

@media (max-width: $breakpoint-md) {
  .table-container {
    border: none;
  }

  .table {
    thead {
      display: none;
    }

    tr.table-row {
      display: block;
      margin-bottom: $spacing-4;
      border: 1px solid $border-color;
      border-radius: $radius-lg;
      padding: $spacing-4;
      background: $bg-card;
      box-shadow: $shadow-md;
    }

    td {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: $spacing-3 0;
      border-bottom: 1px solid $border-color;
      text-align: right;

      &:before {
        content: attr(data-label);
        font-weight: $font-weight-semibold;
        color: $gray-300;
        text-align: left;
        padding-right: $spacing-4;
      }

      &:last-child {
        border-bottom: none;
      }
    }
    
    .checkbox-col {
      justify-content: flex-end;
    }
    
    .pool-name-cell {
      flex-direction: column;
      align-items: flex-start;
      gap: $spacing-2;
      
      &:before {
        margin-bottom: $spacing-1;
      }
      
      .pool-info {
        text-align: left;
        width: 100%;
      }
    }

    .actions-cell {
      .actions {
        justify-content: flex-end;
        width: 100%;
      }
    }
  }
}
</style>