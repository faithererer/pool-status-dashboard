<template>
  <span class="sort-icon" :class="{ active: direction }">
    <svg width="12" height="12" viewBox="0 0 12 12" fill="currentColor">
      <path
        v-if="!direction || direction === 'asc'"
        class="sort-arrow up"
        :class="{ active: direction === 'asc' }"
        d="M6 2L3 5h6z"
      />
      <path
        v-if="!direction || direction === 'desc'"
        class="sort-arrow down"
        :class="{ active: direction === 'desc' }"
        d="M6 10L3 7h6z"
      />
    </svg>
  </span>
</template>

<script>
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'SortIcon',
  props: {
    direction: {
      type: String,
      default: null,
      validator: (value) => [null, 'asc', 'desc'].includes(value)
    }
  }
})
</script>

<style lang="scss" scoped>
.sort-icon {
  display: inline-flex;
  align-items: center;
  margin-left: $spacing-2;
  opacity: 0.5;
  transition: opacity $transition-normal;
  
  &.active {
    opacity: 1;
  }
  
  svg {
    display: block;
  }
  
  .sort-arrow {
    opacity: 0.3;
    transition: opacity $transition-normal;
    
    &.active {
      opacity: 1;
    }
  }
  
  .up {
    fill: currentColor;
  }
  
  .down {
    fill: currentColor;
  }
}
</style>