<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <a
    class="skip-link tm-interactive"
    href="#main-content"
  >
    {{ t('shell.skipToContent') }}
  </a>
</template>

<style scoped lang="scss">
.skip-link {
  position: fixed;
  inset-block-start: var(--tm-space-2);
  inset-inline-start: var(--tm-space-2);
  z-index: 1000;
  display: inline-flex;
  align-items: center;
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-4);
  border: 1px solid var(--tm-action-primary);
  border-radius: var(--tm-radius-control);
  background: var(--tm-surface);
  color: var(--tm-text-primary);
  font-weight: 700;
  text-decoration: none;
  transform: translateY(-160%);
  transition: transform var(--tm-motion-state) ease-out;
}

.skip-link:focus,
.skip-link:focus-visible {
  outline: 3px solid var(--tm-focus-ring);
  outline-offset: 3px;
  transform: translateY(0);
}

@media (prefers-reduced-motion: reduce) {
  .skip-link {
    transition: none;
  }
}
</style>
