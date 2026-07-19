<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  alternatePath: { type: String, default: null },
  targetLocale: { type: String, required: true }
})

const { t } = useI18n()
const hasAlternatePath = computed(() => props.alternatePath !== null)
const recoveryPath = computed(() => (
  props.alternatePath ?? `/${props.targetLocale}`
))
const recoveryLabel = computed(() => (
  hasAlternatePath.value
    ? t('translationUnavailable.viewAvailableTranslation')
    : t('translationUnavailable.returnToLocaleHome')
))
</script>

<template>
  <div
    class="translation-unavailable"
    role="alert"
    aria-live="assertive"
  >
    <p class="translation-unavailable__message">
      {{ t('public.translationUnavailable') }}
    </p>
    <p class="translation-unavailable__description">
      {{ t('public.translationUnavailableDescription') }}
    </p>
    <a
      class="translation-unavailable__recovery tm-interactive"
      :href="recoveryPath"
      :aria-label="recoveryLabel"
    >
      {{ recoveryLabel }}
    </a>
  </div>
</template>

<style scoped lang="scss">
.translation-unavailable {
  display: grid;
  gap: var(--tm-space-3);
  max-inline-size: var(--tm-prose-max-width);
  padding: var(--tm-space-4);
  border: 1px solid var(--tm-border-subtle);
  border-radius: var(--tm-radius-card);
  background: var(--tm-surface);
  color: var(--tm-text-primary);
}

.translation-unavailable__message,
.translation-unavailable__description {
  margin: 0;
}

.translation-unavailable__message {
  font-weight: 700;
}

.translation-unavailable__description {
  color: var(--tm-text-secondary);
}

.translation-unavailable__recovery {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  justify-self: start;
  min-inline-size: var(--tm-control-min-size);
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-3);
  border: 1px solid var(--tm-link);
  border-radius: var(--tm-radius-control);
  color: var(--tm-link);
  font-weight: 700;
  text-decoration: none;
}

.translation-unavailable__recovery:hover {
  color: var(--tm-text-primary);
}

.translation-unavailable__recovery:focus-visible {
  outline: 2px solid var(--tm-focus-ring);
  outline-offset: 2px;
}
</style>
