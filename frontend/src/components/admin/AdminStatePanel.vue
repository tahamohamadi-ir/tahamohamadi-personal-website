<script setup>
import { computed } from 'vue'

const props = defineProps({
  state: {
    type: String,
    required: true,
    validator: (value) => ['loading', 'empty', 'error'].includes(value)
  }
})

const emit = defineEmits(['retry'])

const content = computed(() => ({
  loading: { role: 'status', message: 'Loading content…' },
  empty: { role: 'status', message: 'No content has been added yet.' },
  error: { role: 'alert', message: 'This content could not be loaded.' }
}[props.state]))
</script>

<template>
  <section
    class="admin-state-panel"
    :role="content.role"
    :aria-live="content.role === 'alert' ? 'assertive' : 'polite'"
  >
    <span v-if="state === 'loading'" class="admin-state-panel__spinner" aria-hidden="true" />
    <p class="q-ma-none">{{ content.message }}</p>
    <button
      v-if="state === 'error'"
      type="button"
      class="admin-state-panel__retry"
      @click="emit('retry')"
    >
      Try again
    </button>
  </section>
</template>

<style scoped>
.admin-state-panel {
  display: grid;
  gap: 1rem;
  justify-items: start;
  padding: 1.5rem;
  border: 1px solid var(--q-primary);
  border-radius: 0.5rem;
}

.admin-state-panel__spinner {
  inline-size: 2rem;
  block-size: 2rem;
  border: 0.25rem solid var(--q-primary);
  border-inline-end-color: transparent;
  border-radius: 50%;
}

.admin-state-panel__retry {
  min-block-size: 2.75rem;
  padding-inline: 1rem;
  border: 1px solid var(--q-primary);
  border-radius: 0.25rem;
  background: transparent;
  color: var(--q-primary);
  font: inherit;
  cursor: pointer;
}
</style>
