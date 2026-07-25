<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { nextPage } from 'src/composables/adminContentInteractions'

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
const canGoForward = computed(() => props.page + 1 < props.totalPages)

function requestPage(requestedPage) {
  const resolved = nextPage(props.page, props.totalPages, requestedPage)
  if (resolved !== props.page) {
    emit('change-page', resolved)
  }
}
</script>

<template>
  <nav v-if="totalPages > 1" :aria-label="t('admin.pagination.label')" class="admin-pagination">
    <button
      type="button"
      :disabled="!canGoBack"
      @click="requestPage(page - 1)"
    >
      {{ t('admin.pagination.previous') }}
    </button>
    <span aria-live="polite">{{ t('admin.pagination.status', { page: page + 1, total: totalPages }) }}</span>
    <button
      data-next
      type="button"
      :disabled="!canGoForward"
      @click="requestPage(page + 1)"
    >
      {{ t('admin.pagination.next') }}
    </button>
  </nav>
</template>

<style scoped>
.admin-pagination {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-3);
  align-items: center;
}

button {
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-4);
  border: 1px solid var(--tm-admin-border);
  border-radius: var(--tm-admin-control-radius);
  background: transparent;
  color: inherit;
  font: inherit;
}

button:focus-visible {
  outline: 3px solid var(--tm-focus-ring);
  outline-offset: 2px;
}
</style>
