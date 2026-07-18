<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  locale: { type: String, required: true },
  alternatePath: { type: String, default: null }
})

const { t } = useI18n()
const targetLocale = computed(() => (props.locale === 'fa' ? 'en' : 'fa'))
const targetPath = computed(() => props.alternatePath ?? `/${targetLocale.value}`)
</script>

<template>
  <router-link
    :to="targetPath"
    class="language-switch tm-interactive"
    :aria-label="t('shell.switchLanguage')"
    :lang="targetLocale"
  >
    {{ targetLocale === 'fa' ? t('language.fa') : t('language.en') }}
  </router-link>
</template>

<style scoped lang="scss">
.language-switch {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-inline-size: var(--tm-control-min-size);
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-3);
  border: 1px solid var(--tm-border-subtle);
  border-radius: var(--tm-radius-control);
  color: var(--tm-text-secondary);
  font-size: 0.9375rem;
  font-weight: 600;
  text-decoration: none;
}

.language-switch:hover {
  border-color: var(--tm-interactive-hover);
  background: var(--tm-interactive-surface-hover);
  color: var(--tm-interactive-hover);
}

.language-switch:active {
  background: var(--tm-interactive-surface-hover);
  color: var(--tm-interactive-active);
}
</style>
