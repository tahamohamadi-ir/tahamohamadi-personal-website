<script setup>
import { computed } from 'vue'

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
  <nav v-if="totalPages > 1" aria-label="Admin table pages" class="admin-pagination">
    <button
      type="button"
      :disabled="!canGoBack"
      @click="requestPage(page - 1)"
    >
      Previous
    </button>
    <span aria-live="polite">Page {{ page + 1 }} of {{ totalPages }}</span>
    <button
      data-next
      type="button"
      :disabled="!canGoForward"
      @click="requestPage(page + 1)"
    >
      Next
    </button>
  </nav>
</template>

<style scoped>
.admin-pagination {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  align-items: center;
}

button {
  min-block-size: 2.75rem;
  padding-inline: 1rem;
  border: 1px solid currentColor;
  border-radius: 0.25rem;
  background: transparent;
  color: inherit;
  font: inherit;
}
</style>
