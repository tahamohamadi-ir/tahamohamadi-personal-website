<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const CANONICAL_STATES = [
  'loading',
  'empty',
  'recoverable-failure',
  'offline',
  'stale',
  'translation-unavailable'
]

const props = defineProps({
  state: {
    type: String,
    required: true,
    validator: (value) => [
  'loading',
  'empty',
  'recoverable-failure',
  'offline',
  'stale',
  'translation-unavailable'
].includes(value)
  }
})

const emit = defineEmits(['retry'])
const { t } = useI18n()

const stateContent = computed(() => ({
  loading: {
    role: 'status',
    message: t('pageState.loading')
  },
  empty: {
    role: 'status',
    message: t('pageState.empty')
  },
  'recoverable-failure': {
    role: 'alert',
    message: t('pageState.recoverableFailure'),
    action: 'retry'
  },
  offline: {
    role: 'status',
    message: t('pageState.offline'),
    action: 'retry'
  },
  stale: {
    role: 'status',
    message: t('pageState.stale'),
    action: 'refresh'
  },
  'translation-unavailable': {
    role: 'alert',
    message: t('pageState.translationUnavailable')
  }
}[props.state]))

const actionLabel = computed(() => {
  if (stateContent.value.action === 'refresh') {
    return t('pageState.refresh')
  }

  return t('pageState.retry')
})
</script>

<template>
  <div
    class="page-state"
    :class="`page-state--${state}`"
    :role="stateContent.role"
    :aria-live="stateContent.role === 'alert' ? 'assertive' : 'polite'"
  >
    <p class="page-state__message">{{ stateContent.message }}</p>
    <button
      v-if="stateContent.action"
      class="page-state__action tm-interactive"
      type="button"
      @click="emit('retry')"
    >
      {{ actionLabel }}
    </button>
  </div>
</template>

<style scoped lang="scss">
.page-state {
  display: grid;
  gap: var(--tm-space-3);
  max-inline-size: var(--tm-prose-max-width);
  padding: var(--tm-space-4);
  border: 1px solid var(--tm-border-subtle);
  border-radius: var(--tm-radius-card);
  background: var(--tm-surface);
  color: var(--tm-text-primary);
}

.page-state__message {
  margin: 0;
  color: var(--tm-text-secondary);
}

.page-state__action {
  justify-self: start;
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

.page-state__action:hover {
  color: var(--tm-text-primary);
}

.page-state__action:focus-visible {
  outline: 2px solid var(--tm-focus-ring);
  outline-offset: 2px;
}
</style>
