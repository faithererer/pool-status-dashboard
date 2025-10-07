<template>
  <nav class="pagination" v-if="totalPages > 1">
    <ul class="pagination-list">
      <li>
        <button
          class="pagination-button"
          :disabled="currentPage === 1"
          @click="changePage(currentPage - 1)"
          aria-label="Go to previous page"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path fill-rule="evenodd" d="M9.78 4.22a.75.75 0 010 1.06L7.06 8l2.72 2.72a.75.75 0 11-1.06 1.06L5.47 8.53a.75.75 0 010-1.06l3.25-3.25a.75.75 0 011.06 0z" clip-rule="evenodd" />
          </svg>
        </button>
      </li>
      <li v-for="page in pages" :key="page.number">
        <button
          v-if="page.type === 'page'"
          class="pagination-button"
          :class="{ 'is-current': page.number === currentPage }"
          @click="changePage(page.number)"
          :aria-label="`Go to page ${page.number}`"
          :aria-current="page.number === currentPage ? 'page' : undefined"
        >
          {{ page.number }}
        </button>
        <span v-else class="pagination-ellipsis">&hellip;</span>
      </li>
      <li>
        <button
          class="pagination-button"
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
          aria-label="Go to next page"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path fill-rule="evenodd" d="M6.22 4.22a.75.75 0 000 1.06L8.94 8l-2.72 2.72a.75.75 0 101.06 1.06L10.53 8.53a.75.75 0 000-1.06L7.28 4.22a.75.75 0 00-1.06 0z" clip-rule="evenodd" />
          </svg>
        </button>
      </li>
    </ul>
  </nav>
</template>

<script>
import { defineComponent, computed } from 'vue'

export default defineComponent({
  name: 'BasePagination',
  props: {
    currentPage: {
      type: Number,
      required: true
    },
    totalPages: {
      type: Number,
      required: true
    },
    maxVisibleButtons: {
      type: Number,
      default: 7
    }
  },
  emits: ['page-change'],
  setup(props, { emit }) {
    const pages = computed(() => {
      const range = []
      const { currentPage, totalPages, maxVisibleButtons } = props

      if (totalPages <= maxVisibleButtons) {
        for (let i = 1; i <= totalPages; i++) {
          range.push({ type: 'page', number: i })
        }
        return range
      }

      const half = Math.floor(maxVisibleButtons / 2)
      let start = currentPage - half
      let end = currentPage + half

      if (start <= 1) {
        start = 1
        end = maxVisibleButtons - 1
        range.push({ type: 'page', number: 1 })
      } else {
        range.push({ type: 'page', number: 1 })
        range.push({ type: 'ellipsis' })
      }
      
      if (end >= totalPages) {
        end = totalPages
        start = totalPages - maxVisibleButtons + 2
      }

      for (let i = start; i <= end; i++) {
        if (i > 1 && i < totalPages) {
          range.push({ type: 'page', number: i })
        }
      }

      if (end < totalPages) {
        range.push({ type: 'ellipsis' })
        range.push({ type: 'page', number: totalPages })
      } else {
         range.push({ type: 'page', number: totalPages })
      }
      
      // Remove duplicates that might be introduced by edge cases
      const uniquePages = [];
      const seen = new Set();
      for (const page of range) {
        const key = page.type === 'page' ? `p-${page.number}` : 'ellipsis';
        if (!seen.has(key)) {
          uniquePages.push(page);
          seen.add(key);
        }
      }
      
      // Ensure ellipsis are not next to each other
      return uniquePages.filter((page, index) => {
        return !(page.type === 'ellipsis' && uniquePages[index + 1]?.type === 'ellipsis');
      });
    })

    const changePage = (page) => {
      if (page > 0 && page <= props.totalPages && page !== props.currentPage) {
        emit('page-change', page)
      }
    }

    return {
      pages,
      changePage
    }
  }
})
</script>

<style lang="scss" scoped>
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: $spacing-4 0;
}

.pagination-list {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: $spacing-2;
}

.pagination-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid $border-color;
  background-color: $bg-card;
  color: $gray-300;
  border-radius: $radius-md;
  cursor: pointer;
  transition: all $transition-normal;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;

  &:hover:not(:disabled) {
    background-color: $bg-card-hover;
    border-color: $primary-500;
    color: $primary-300;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  &.is-current {
    background-color: $primary-500;
    border-color: $primary-500;
    color: white;
    font-weight: $font-weight-bold;
  }
}

.pagination-ellipsis {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  color: $gray-500;
}
</style>