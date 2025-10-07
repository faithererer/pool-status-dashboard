<template>
  <div class="management-view">
    <div class="management-header">
      <div class="header-content">
        <h1 class="page-title">号池管理</h1>
        <p class="page-description">管理和配置您的号池资源</p>
      </div>
      
      <div class="header-actions">
        <BaseButton
          variant="primary"
          @click="showCreateModal = true"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor" class="mr-2">
            <path d="M8 2a.75.75 0 01.75.75v4.5h4.5a.75.75 0 010 1.5h-4.5v4.5a.75.75 0 01-1.5 0v-4.5h-4.5a.75.75 0 010-1.5h4.5v-4.5A.75.75 0 018 2z"/>
          </svg>
          新增号池
        </BaseButton>
        
        <BaseButton
          variant="danger"
          @click="handleBatchDelete"
          :disabled="selectedPools.length === 0"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor" class="mr-2">
            <path fill-rule="evenodd" d="M6.5 1.75a.25.25 0 01.25-.25h2.5a.25.25 0 01.25.25V3h-3V1.75zm4.5 0V3h2.25a.75.75 0 010 1.5H2.75a.75.75 0 010-1.5H5V1.75C5 .784 5.784 0 6.75 0h2.5C10.216 0 11 .784 11 1.75zM4.496 6.675a.75.75 0 10-1.492.15l.66 6.6A1.75 1.75 0 005.405 15h5.19c.9 0 1.652-.681 1.741-1.576l.66-6.6a.75.75 0 00-1.492-.149l-.66 6.6a.25.25 0 01-.249.225h-5.19a.25.25 0 01-.249-.225l-.66-6.6z" clip-rule="evenodd"/>
          </svg>
          批量删除 ({{ selectedPools.length }})
        </BaseButton>
        <BaseButton
          variant="ghost"
          @click="refreshData"
          :loading="loading.pools"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor" class="mr-2">
            <path fill-rule="evenodd" d="M1.679 7.932c.412-.621 1.242-.421 1.242.317 0 .317.158.59.397.792A5.5 5.5 0 1013.5 8a.75.75 0 011.5 0 7 7 0 11-9.015-6.7c.75-.43 1.592.25 1.264 1.018l-.328.767c-.25.586-.88.918-1.514.647l-1.328-.567c-.634-.271-.934-1.009-.467-1.632l.467-1.098z" clip-rule="evenodd"/>
          </svg>
          刷新
        </BaseButton>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <SearchFilter
      @search="handleSearch"
      @filter-change="handleFilterChange"
    />

    <!-- 数据加载状态 -->
    <div v-if="loading.pools" class="loading-container">
      <BaseLoading size="large" />
      <p class="loading-text">正在加载号池数据...</p>
    </div>

    <!-- 数据表格 -->
    <div v-else class="table-container">
      <PoolTable
        :pools="pools"
        :selected-pools="selectedPools"
        @update:selectedPools="selectedPools = $event"
        @edit="handleEdit"
        @delete="handleDelete"
        @view-details="handleViewDetails"
        @toggle-enabled="handleToggleEnabled"
      />
    </div>

    <!-- 空状态 -->
    <div v-if="!loading.pools && pools.length === 0" class="empty-state">
      <svg width="64" height="64" viewBox="0 0 64 64" fill="currentColor" class="empty-icon">
        <path d="M32 2C15.432 2 2 15.432 2 32s13.432 30 30 30 30-13.432 30-30S48.568 2 32 2zm0 54C17.664 56 6 44.336 6 32S17.664 8 32 8s26 11.664 26 24-11.664 24-26 24z"/>
        <path d="M32 16c-8.837 0-16 7.163-16 16s7.163 16 16 16 16-7.163 16-16-7.163-16-16-16zm0 28c-6.627 0-12-5.373-12-12s5.373-12 12-12 12 5.373 12 12-5.373 12-12 12z"/>
      </svg>
      <h3 class="empty-title">暂无号池数据</h3>
      <p class="empty-description">
        {{ hasFilters ? '没有找到符合条件的号池' : '还没有创建任何号池，点击上方按钮开始创建' }}
      </p>
      <BaseButton
        v-if="!hasFilters"
        variant="primary"
        @click="showCreateModal = true"
      >
        创建第一个号池
      </BaseButton>
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="pagination.totalPages > 1">
      <BasePagination
        :current-page="pagination.current"
        :total-pages="pagination.totalPages"
        @page-change="handlePageChange"
      />
    </div>

    <!-- 创建/编辑模态框 -->
    <PoolFormModal
      :show="showCreateModal || showEditModal"
      :pool="editingPool"
      @close="handleModalClose"
      @submit="handleModalSubmit"
    />

    <!-- 删除确认模态框 -->
    <BaseModal
      v-model="showDeleteModal"
      title="确认删除"
      size="small"
    >
      <p class="delete-message">
        确定要删除号池 <strong>{{ deletingPool?.name }}</strong> 吗？
        <br>
        <span class="warning-text">此操作不可撤销，请谨慎操作。</span>
      </p>
      
      <template #footer>
        <div class="modal-actions">
          <BaseButton
            variant="ghost"
            @click="showDeleteModal = false"
          >
            取消
          </BaseButton>
          
          <BaseButton
            variant="danger"
            :loading="deleteLoading"
            @click="confirmDelete"
          >
            确认删除
          </BaseButton>
        </div>
      </template>
    </BaseModal>

    <!-- 批量删除确认模态框 -->
    <BaseModal
      v-model="showBatchDeleteModal"
      title="确认批量删除"
      size="small"
    >
      <p class="delete-message">
        确定要删除选中的 <strong>{{ selectedPools.length }}</strong> 个号池吗？
        <br>
        <span class="warning-text">此操作不可撤销，请谨慎操作。</span>
      </p>
      
      <template #footer>
        <div class="modal-actions">
          <BaseButton
            variant="ghost"
            @click="showBatchDeleteModal = false"
          >
            取消
          </BaseButton>
          
          <BaseButton
            variant="danger"
            :loading="deleteLoading"
            @click="confirmBatchDelete"
          >
            确认删除
          </BaseButton>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<script>
import { defineComponent, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { usePoolStore } from '@/stores/pool'
import SearchFilter from '@/components/management/SearchFilter.vue'
import PoolTable from '@/components/management/PoolTable.vue'
import PoolFormModal from '@/components/management/PoolFormModal.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {BaseModal} from "@/components/common/index.js";

export default defineComponent({
  name: 'ManagementView',
  components: {
    BaseModal,
    SearchFilter,
    PoolTable,
    PoolFormModal,
    BasePagination
  },
  setup() {
    const router = useRouter()
    const poolStore = usePoolStore()
    const { pools, pagination, loading: storeLoading } = storeToRefs(poolStore)
    
    const deleteLoading = ref(false)
    const searchQuery = ref('')
    const filters = ref({})
    
    const showCreateModal = ref(false)
    const showEditModal = ref(false)
    const showDeleteModal = ref(false)
    const showBatchDeleteModal = ref(false)
    const editingPool = ref(null)
    const deletingPool = ref(null)
    const selectedPools = ref([])
    
    const hasFilters = computed(() => {
      return searchQuery.value || Object.values(filters.value).some(v => v)
    })
    
    const refreshData = async () => {
      try {
        const queryParams = {
          name: searchQuery.value,
          ...filters.value
        }
        await poolStore.fetchPools(queryParams)
        // 刷新后清空选择
        selectedPools.value = []
      } catch (error) {
        console.error('在组件中捕获到错误:', error)
      }
    }
    
    const handleSearch = (query) => {
      searchQuery.value = query
      refreshData()
    }
    
    const handleFilterChange = (newFilters) => {
      filters.value = newFilters
      refreshData()
    }

    const handleToggleEnabled = async (pool) => {
      try {
        await poolStore.togglePoolEnabled(pool.id, !pool.enabled)
        await refreshData()
      } catch (error) {
        console.error('切换启用状态失败:', error)
      }
    }

    const handlePageChange = (page) => {
      poolStore.setCurrentPage(page)
      refreshData()
    }
    
    const handleEdit = (pool) => {
      editingPool.value = { ...pool }
      showEditModal.value = true
    }
    
    const handleDelete = (pool) => {
      deletingPool.value = pool
      showDeleteModal.value = true
    }
    
    const handleViewDetails = (pool) => {
      router.push({ path: '/dashboard', query: { poolId: pool.id } })
    }

    const handleBatchDelete = () => {
      if (selectedPools.value.length > 0) {
        showBatchDeleteModal.value = true
      }
    }
    
    const handleModalClose = () => {
      showCreateModal.value = false
      showEditModal.value = false
      editingPool.value = null
    }
    
    const handleModalSubmit = async (formData) => {
      try {
        if (editingPool.value) {
          await poolStore.updatePool(editingPool.value.id, formData)
        } else {
          await poolStore.createPool(formData)
        }
        handleModalClose()
        await refreshData()
      } catch (error) {
        console.error('保存失败:', error)
      }
    }
    
    const confirmDelete = async () => {
      if (!deletingPool.value) return
      
      deleteLoading.value = true
      try {
        await poolStore.deletePool(deletingPool.value.id)
        showDeleteModal.value = false
        deletingPool.value = null
        await refreshData()
      } catch (error) {
        console.error('删除失败:', error)
      } finally {
        deleteLoading.value = false
      }
    }

    const confirmBatchDelete = async () => {
      if (selectedPools.value.length === 0) return
      
      deleteLoading.value = true
      try {
        await poolStore.deletePools(selectedPools.value)
        showBatchDeleteModal.value = false
        await refreshData() // refreshData会清空selectedPools
      } catch (error) {
        console.error('批量删除失败:', error)
      } finally {
        deleteLoading.value = false
      }
    }
    
    onMounted(() => {
      refreshData()
    })
    
    return {
      loading: storeLoading,
      pools,
      pagination,
      deleteLoading,
      hasFilters,
      showCreateModal,
      showEditModal,
      showDeleteModal,
      showBatchDeleteModal,
      editingPool,
      deletingPool,
      selectedPools,
      refreshData,
      handleSearch,
      handleFilterChange,
      handleEdit,
      handleDelete,
      handleViewDetails,
      handleModalClose,
      handleModalSubmit,
      confirmDelete,
      handleToggleEnabled,
      handlePageChange,
      handleBatchDelete,
      confirmBatchDelete
    }
  }
})
</script>

<style lang="scss" scoped>
.management-view {
  padding: $spacing-6;
  max-width: 1600px;
  margin: 0 auto;
}

.management-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-6;
  
  .header-content {
    .page-title {
      font-size: $font-size-3xl;
      font-weight: $font-weight-bold;
      color: $gray-50;
      margin: 0 0 $spacing-2 0;
    }
    
    .page-description {
      font-size: $font-size-base;
      color: $gray-400;
      margin: 0;
    }
  }
  
  .header-actions {
    display: flex;
    gap: $spacing-3;
    
    .mr-2 {
      margin-right: $spacing-2;
    }
  }
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-12 $spacing-6;
  
  .loading-text {
    margin-top: $spacing-4;
    color: $gray-400;
    font-size: $font-size-sm;
  }
}

.table-container {
  background: $bg-card;
  border-radius: $radius-lg;
  border: 1px solid $border-color;
  overflow: hidden;
}

.grid-container {
  .pool-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: $spacing-4;
  }
  
  .pool-card {
    background: $bg-card;
    border: 1px solid $border-color;
    border-radius: $radius-lg;
    padding: $spacing-4;
    cursor: pointer;
    transition: all $transition-normal;
    
    &:hover {
      border-color: $primary-500;
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: $spacing-3;
      
      .pool-name {
        font-size: $font-size-lg;
        font-weight: $font-weight-semibold;
        color: $gray-50;
        margin: 0;
      }
    }
    
    .pool-description {
      color: $gray-400;
      font-size: $font-size-sm;
      margin: 0 0 $spacing-4 0;
      line-height: 1.5;
    }
    
    .pool-stats {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: $spacing-3;
      margin-bottom: $spacing-4;
      
      .stat-item {
        text-align: center;
        
        .stat-label {
          display: block;
          font-size: $font-size-xs;
          color: $gray-500;
          margin-bottom: $spacing-1;
        }
        
        .stat-value {
          display: block;
          font-size: $font-size-sm;
          font-weight: $font-weight-semibold;
          color: $gray-200;
          
          &.valid {
            color: $green-400;
          }
        }
      }
    }
    
    .card-actions {
      display: flex;
      justify-content: flex-end;
      gap: $spacing-2;
      
      .delete-btn:hover {
        color: $red-400;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-12 $spacing-6;
  text-align: center;
  
  .empty-icon {
    color: $gray-600;
    margin-bottom: $spacing-4;
  }
  
  .empty-title {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    color: $gray-300;
    margin: 0 0 $spacing-2 0;
  }
  
  .empty-description {
    color: $gray-500;
    margin: 0 0 $spacing-6 0;
    max-width: 400px;
    line-height: 1.6;
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: $spacing-4 0;
  border-top: 1px solid $border-color;
  background-color: $bg-card;
}

.delete-message {
  color: $gray-300;
  line-height: 1.6;
  margin: 0;
  
  .warning-text {
    color: $red-400;
    font-size: $font-size-sm;
  }
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-3;
}

// 响应式设计
@media (max-width: $breakpoint-lg) {
  .management-view {
    padding: $spacing-4;
  }
  
  .management-header {
    flex-direction: column;
    gap: $spacing-4;
    
    .header-actions {
      align-self: stretch;
      justify-content: flex-end;
    }
  }
  
  .pool-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: $breakpoint-md) {
  .management-view {
    padding: $spacing-3;
  }
  
  .header-actions {
    flex-direction: column;
    gap: $spacing-2;
  }
  
  .pool-grid {
    grid-template-columns: 1fr;
  }
  
  .pool-stats {
    grid-template-columns: 1fr;
    gap: $spacing-2;
    
    .stat-item {
      display: flex;
      justify-content: space-between;
      text-align: left;
    }
  }
  
  .modal-actions {
    flex-direction: column-reverse;
    gap: $spacing-2;
  }
}
</style>