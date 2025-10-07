<template>
  <BaseModal
    :model-value="show"
    :title="isEdit ? '编辑号池' : '新增号池'"
    size="large"
    @close="handleClose"
    :show-footer="true"
  >
    <form @submit.prevent="handleSubmit" class="pool-form">
      <div class="form-grid">
        <!-- 基本信息 -->
        <div class="form-section">
          <h4 class="section-title">基本信息</h4>
          <div class="form-group">
            <label for="name" class="form-label required">号池名称</label>
            <input id="name" v-model="form.name" type="text" class="form-input" :class="{ error: errors.name }" placeholder="请输入号池名称" required />
            <span v-if="errors.name" class="error-message">{{ errors.name }}</span>
          </div>
          <div class="form-group">
            <label for="description" class="form-label">描述</label>
            <textarea id="description" v-model="form.description" class="form-textarea" placeholder="请输入号池描述" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label for="enabled" class="form-label">状态</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input v-model="form.enabled" type="checkbox" class="form-checkbox" />
                <span class="checkbox-label">启用号池</span>
              </label>
            </div>
          </div>
        </div>

        <!-- 配置信息 -->
        <div class="form-section">
          <h4 class="section-title">数据源配置</h4>
          <div class="form-group">
            <label for="dataSourceClass" class="form-label required">数据源类</label>
            <select id="dataSourceClass" v-model="form.dataSourceClass" class="form-select" :class="{ error: errors.dataSourceClass }" required>
              <option value="" disabled>请选择一个数据源</option>
              <option v-for="type in dataSourceTypes" :key="type.classFullName" :value="type.classFullName">
                {{ type.name }} - {{ type.description }}
              </option>
            </select>
            <span v-if="errors.dataSourceClass" class="error-message">{{ errors.dataSourceClass }}</span>
          </div>
          <div class="form-group">
            <label for="updateFrequency" class="form-label">更新频率(秒)</label>
            <input id="updateFrequency" v-model.number="form.updateFrequency" type="number" class="form-input" :class="{ error: errors.updateFrequency }" placeholder="例如: 60" min="1" />
            <span v-if="errors.updateFrequency" class="error-message">{{ errors.updateFrequency }}</span>
          </div>
           <div class="form-group">
            <label for="displayStrategy" class="form-label">显示策略</label>
            <select id="displayStrategy" v-model="form.displayStrategy" class="form-select">
              <option value="public">公开</option>
              <option value="private">私有</option>
              <option value="protected">保护</option>
            </select>
          </div>
        </div>
      </div>

      <!-- 高级JSON配置 -->
      <div class="form-section full-width advanced-config">
        <div @click="toggleAdvancedConfig" class="section-header">
          <h4 class="section-title">高级配置 (JSON)</h4>
          <ChevronDownIcon class="expand-icon" :class="{ rotated: isAdvancedConfigOpen }" />
        </div>
        
        <div v-if="isAdvancedConfigOpen" class="form-grid">
          <div class="form-section">
            <h5 class="section-subtitle">数据源配置</h5>
            <div class="form-group">
              <label for="dataSourceConfig" class="form-label">DataSource Config</label>
              <textarea id="dataSourceConfig" v-model="form.dataSourceConfig" class="form-textarea json-input" :class="{ error: errors.dataSourceConfig }" rows="8"></textarea>
              <p class="form-hint">请输入合法的JSON格式</p>
              <span v-if="errors.dataSourceConfig" class="error-message">{{ errors.dataSourceConfig }}</span>
            </div>
          </div>
          <div class="form-section">
            <h5 class="section-subtitle">显示字段配置</h5>
            <div class="form-group">
              <label for="displayFields" class="form-label">Display Fields Config</label>
              <textarea id="displayFields" v-model="form.displayFields" class="form-textarea json-input" :class="{ error: errors.displayFields }" rows="8"></textarea>
               <p class="form-hint">请输入合法的JSON格式</p>
              <span v-if="errors.displayFields" class="error-message">{{ errors.displayFields }}</span>
            </div>
          </div>
        </div>
      </div>
    </form>
    
    <template #footer>
      <div class="modal-actions">
        <BaseButton
          variant="ghost"
          @click="handleClose"
        >
          取消
        </BaseButton>
        
        <BaseButton
          variant="primary"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ isEdit ? '更新' : '创建' }}
        </BaseButton>
      </div>
    </template>
  </BaseModal>
</template>

<script>
import { defineComponent, ref, reactive, computed, watch, onMounted } from 'vue'
import { ChevronDownIcon } from '@heroicons/vue/24/solid'
import { usePoolStore } from '@/stores/pool'

export default defineComponent({
  name: 'PoolFormModal',
  props: {
    show: {
      type: Boolean,
      default: false
    },
    pool: {
      type: Object,
      default: null
    }
  },
  emits: ['close', 'submit'],
  setup(props, { emit }) {
    const poolStore = usePoolStore()
    const dataSourceTypes = computed(() => poolStore.dataSourceTypes)

    onMounted(() => {
      poolStore.fetchDataSourceTypes()
    })
    
    const loading = ref(false)
    const isEdit = computed(() => !!props.pool?.id)

    const isAdvancedConfigOpen = ref(false)

    const toggleAdvancedConfig = () => {
      isAdvancedConfigOpen.value = !isAdvancedConfigOpen.value
    }

    const defaultForm = {
      name: '',
      description: '',
      dataSourceClass: '',
      dataSourceConfig: JSON.stringify({ url: "", timeout: 5000 }, null, 2),
      updateFrequency: 60,
      displayStrategy: 'public',
      displayFields: JSON.stringify({ valid: true, invalid: true, cooling: true, total: true, pressure: true }, null, 2),
      enabled: true,
    }

    const form = reactive({ ...defaultForm })
    const errors = reactive({})

    watch(() => props.pool, (newPool) => {
      let formData
      if (newPool) {
        formData = { ...defaultForm, ...newPool }
        formData.dataSourceConfig = JSON.stringify(newPool.dataSourceConfig || {}, null, 2)
        formData.displayFields = JSON.stringify(newPool.displayFields || {}, null, 2)
      } else {
        formData = { ...defaultForm }
      }
      Object.assign(form, formData)
      
      Object.keys(errors).forEach(key => { delete errors[key] })
    }, { immediate: true, deep: true })

    const validateForm = () => {
      const newErrors = {}
      if (!form.name?.trim()) newErrors.name = '请输入号池名称'
      if (!form.dataSourceClass) newErrors.dataSourceClass = '请选择一个数据源'
      if (form.updateFrequency && form.updateFrequency < 1) newErrors.updateFrequency = '更新频率必须是正数'

      try {
        JSON.parse(form.dataSourceConfig)
      } catch (e) {
        newErrors.dataSourceConfig = '数据源配置不是有效的JSON格式'
      }

      try {
        JSON.parse(form.displayFields)
      } catch (e) {
        newErrors.displayFields = '显示字段配置不是有效的JSON格式'
      }

      Object.keys(errors).forEach(key => { delete errors[key] })
      Object.assign(errors, newErrors)
      return Object.keys(newErrors).length === 0
    }

    const handleSubmit = async () => {
      if (!validateForm()) return

      loading.value = true
      try {
        const submissionData = {
          ...form,
          dataSourceConfig: JSON.parse(form.dataSourceConfig),
          displayFields: JSON.parse(form.displayFields),
        }
        emit('submit', submissionData)
      } catch (error) {
        console.error('提交表单失败:', error)
        // 可以添加用户反馈，例如 toast
      } finally {
        loading.value = false
      }
    }

    const handleClose = () => {
      emit('close')
    }

    return {
      loading,
      isEdit,
      form,
      errors,
      dataSourceTypes,
      isAdvancedConfigOpen,
      toggleAdvancedConfig,
      handleSubmit,
      handleClose
    }
  }
})
</script>

<style lang="scss" scoped>
.pool-form {
  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: $spacing-6;
    margin-bottom: $spacing-6;
  }
  
  .form-section {
    &.full-width {
      grid-column: 1 / -1;
    }
  }
  
  .section-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $gray-100;
    margin: 0 0 $spacing-4 0;
    padding-bottom: $spacing-2;
    border-bottom: 1px solid $border-color;
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    cursor: pointer;
    margin-bottom: $spacing-4;
    
    .expand-icon {
      width: 20px;
      height: 20px;
      color: $gray-400;
      margin-left: $spacing-2;
      transition: transform $transition-normal;
      
      &.rotated {
        transform: rotate(180deg);
      }
    }
  }
  
  .section-subtitle {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $gray-200;
    margin: 0 0 $spacing-3 0;
  }
  
  .form-group {
    margin-bottom: $spacing-4;
  }
  
  .form-label {
    display: block;
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $gray-300;
    margin-bottom: $spacing-2;
    
    &.required::after {
      content: ' *';
      color: $red-400;
    }
  }
  
  .form-input,
  .form-select,
  .form-textarea {
    width: 100%;
    padding: $spacing-3;
    background: $bg-overlay;
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
    
    &.error {
      border-color: $red-400;
      
      &:focus {
        box-shadow: 0 0 0 3px rgba($red-400, 0.1);
      }
    }
    
    &::placeholder {
      color: $gray-500;
    }
  }
  
  .form-textarea {
    resize: vertical;
    min-height: 80px;
  }
  
  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: $spacing-4;
  }
  
  .checkbox-group {
    display: flex;
    gap: $spacing-4;
  }
  
  .checkbox-item {
    display: flex;
    align-items: center;
    cursor: pointer;
  }
  
  .form-checkbox {
    margin-right: $spacing-2;
    accent-color: $primary-500;
  }
  
  .checkbox-label {
    font-size: $font-size-sm;
    color: $gray-300;
  }
  
  .error-message {
    display: block;
    font-size: $font-size-xs;
    color: $red-400;
    margin-top: $spacing-1;
  }
  
  .form-hint {
    font-size: $font-size-xs;
    color: $gray-500;
    margin-top: $spacing-1;
  }

  .json-input {
    font-family: $font-mono;
    font-size: $font-size-sm;
    line-height: 1.6;
  }

  .advanced-config {
    padding-top: $spacing-4;
    border-top: 1px solid $border-color;
  }
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-3;
}

// 响应式设计
@media (max-width: $breakpoint-lg) {
  .form-grid {
    grid-template-columns: 1fr;
    gap: $spacing-4;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .checkbox-group {
    flex-direction: column;
    gap: $spacing-2;
  }
}

@media (max-width: $breakpoint-md) {
  .modal-actions {
    flex-direction: column-reverse;
    
    .base-button {
      width: 100%;
    }
  }
}
</style>