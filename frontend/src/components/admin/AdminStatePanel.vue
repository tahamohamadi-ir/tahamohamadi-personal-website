<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps({
  state: {
    type: String,
    required: true,
    validator: (value) => ['loading', 'empty', 'error'].includes(value)
  }
})

const emit = defineEmits(['retry'])

const content = computed(() => ({
  loading: { role: 'status', message: t('admin.state.loading') },
  empty: { role: 'status', message: t('admin.state.empty') },
  error: { role: 'alert', message: t('admin.state.error') }
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
      {{ t('admin.state.retry') }}
    </button>
  </section>
</template>

<style scoped>
.admin-state-panel {
  display: grid;
  gap: var(--tm-space-4);
  justify-items: start;
  padding: var(--tm-space-6);
  border: 1px solid var(--tm-admin-border);
  border-radius: var(--tm-admin-panel-radius);
  background: var(--tm-admin-surface);
}

.admin-state-panel__spinner {
  inline-size: var(--tm-space-8);
  block-size: var(--tm-space-8);
  border: var(--tm-space-1) solid var(--tm-action-primary);
  border-inline-end-color: transparent;
  border-radius: 50%;
  animation: admin-state-spin var(--tm-motion-loading) linear infinite;
}

.admin-state-panel__retry {
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-4);
  border: 1px solid var(--tm-action-primary);
  border-radius: var(--tm-admin-control-radius);
  background: transparent;
  color: var(--tm-action-primary);
  font: inherit;
  cursor: pointer;
}

.admin-state-panel__retry:focus-visible { outline: 3px solid var(--tm-focus-ring); outline-offset: 2px; }
@keyframes admin-state-spin { to { transform: rotate(1turn); } }
@media (prefers-reduced-motion: reduce) { .admin-state-panel__spinner { animation: none; } }
</style>
