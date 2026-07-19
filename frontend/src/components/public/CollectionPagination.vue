<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  page: {
    type: Number,
    required: true
  },
  totalPages: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['change-page'])
const { t } = useI18n()

const canGoBack = computed(() => props.page > 0)
const canGoForward = computed(() => (
  props.page + 1 < props.totalPages
))

function changePage(nextPage) {
  if (nextPage < 0 || nextPage >= props.totalPages) {
    return
  }

  emit('change-page', nextPage)
}
</script>

<template>
  <nav
    v-if="totalPages > 1"
    class="collection-pagination"
    :aria-label="t('collections.pagination.navigationLabel')"
  >
    <button
      class="collection-pagination__control tm-interactive"
      type="button"
      :disabled="!canGoBack"
      :aria-label="t('collections.pagination.previous')"
      @click="changePage(page - 1)"
    >
      {{ t('collections.pagination.previous') }}
    </button>

    <p class="collection-pagination__status" aria-live="polite">
      {{ t('collections.pagination.position', {
        current: page + 1,
        total: totalPages
      }) }}
    </p>

    <button
      class="collection-pagination__control tm-interactive"
      type="button"
      :disabled="!canGoForward"
      :aria-label="t('collections.pagination.next')"
      @click="changePage(page + 1)"
    >
      {{ t('collections.pagination.next') }}
    </button>
  </nav>
</template>

<style scoped lang="scss">
.collection-pagination {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-3);
  align-items: center;
  max-inline-size: var(--tm-prose-max-width);
  padding-block-start: var(--tm-space-6);
  border-block-start: 1px solid var(--tm-border-subtle);
}

.collection-pagination__control {
  min-inline-size: var(--tm-control-min-size);
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-3);
  border: 1px solid var(--tm-link);
  border-radius: var(--tm-radius-control);
  background: var(--tm-surface);
  color: var(--tm-link);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.collection-pagination__control:hover:not(:disabled) {
  color: var(--tm-text-primary);
}

.collection-pagination__control:disabled {
  border-color: var(--tm-border-subtle);
  color: var(--tm-text-secondary);
  cursor: not-allowed;
}

.collection-pagination__control:focus-visible {
  outline: 2px solid var(--tm-focus-ring);
  outline-offset: 2px;
}

.collection-pagination__status {
  margin: 0;
  color: var(--tm-text-secondary);
}
</style>
