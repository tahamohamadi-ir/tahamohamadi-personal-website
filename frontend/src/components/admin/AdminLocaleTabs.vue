<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { isMissingTranslation } from 'src/composables/adminContentInteractions'

const props = defineProps({
  modelValue: {
    type: String,
    required: true
  },
  translations: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue'])
const { t } = useI18n()
const locales = computed(() => [
  { value: 'fa', label: t('admin.localeTabs.persian') },
  { value: 'en', label: t('admin.localeTabs.english') }
])

const missingLocale = computed(() => locales.value.find(
  ({ value }) => isMissingTranslation(props.translations, value)
)?.value)

const completedCount = computed(() => {
  return locales.value.filter(({ value }) => !isMissingTranslation(props.translations, value)).length
})
</script>

<template>
  <section :aria-label="t('admin.localeTabs.sectionLabel')" class="admin-locale-tabs-wrapper">
    <div class="admin-locale-tabs" role="tablist" :aria-label="t('admin.localeTabs.tabListLabel')">
      <button
        v-for="locale in locales"
        :key="locale.value"
        :data-locale="locale.value"
        class="admin-locale-tabs__tab"
        type="button"
        role="tab"
        :aria-selected="modelValue === locale.value"
        @click="emit('update:modelValue', locale.value)"
      >
        <span>{{ locale.label }}</span>
        <span
          class="admin-locale-tabs__badge"
          :class="isMissingTranslation(translations, locale.value) ? 'admin-locale-tabs__badge--missing' : 'admin-locale-tabs__badge--complete'"
        >
          <template v-if="isMissingTranslation(translations, locale.value)">
            ● <span class="visually-hidden">({{ t('admin.localeTabs.missing') }})</span>
          </template>
          <template v-else>
            ✓
          </template>
        </span>
      </button>
      <span class="admin-locale-tabs__summary text-caption text-grey-7">
        {{ completedCount }}/{{ locales.length }}
      </span>
    </div>
    <p v-if="missingLocale" class="text-caption q-mt-xs q-mb-none text-grey-7" role="status">
      {{ t('admin.localeTabs.missingStatus', { locale: missingLocale === 'fa' ? t('admin.localeTabs.persian') : t('admin.localeTabs.english') }) }}
    </p>
  </section>
</template>

<style scoped>
.admin-locale-tabs-wrapper {
  display: flex;
  flex-direction: column;
}
.admin-locale-tabs {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-2);
}
.admin-locale-tabs__tab {
  align-items: center;
  background: var(--tm-admin-surface, #ffffff);
  border: 1px solid var(--tm-admin-border, #cbd5e1);
  border-radius: var(--tm-admin-control-radius, 6px);
  color: var(--tm-text-primary);
  cursor: pointer;
  display: inline-flex;
  gap: 6px;
  min-block-size: var(--tm-control-min-size, 36px);
  padding-inline: var(--tm-space-3);
  transition: all 0.2s ease;
}
.admin-locale-tabs__tab:focus-visible {
  outline: 3px solid var(--tm-focus-ring);
  outline-offset: 2px;
}
.admin-locale-tabs__tab[aria-selected='true'] {
  background: var(--tm-action-primary, #4f46e5);
  border-color: var(--tm-action-primary, #4f46e5);
  color: var(--tm-white, #ffffff);
}
.admin-locale-tabs__badge {
  border-radius: 999px;
  font-size: 11px;
  font-weight: bold;
  padding: 1px 6px;
}
.admin-locale-tabs__badge--missing {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}
.admin-locale-tabs__tab[aria-selected='true'] .admin-locale-tabs__badge--missing {
  background: rgba(255, 255, 255, 0.25);
  color: #ffffff;
}
.admin-locale-tabs__badge--complete {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
}
.admin-locale-tabs__tab[aria-selected='true'] .admin-locale-tabs__badge--complete {
  background: rgba(255, 255, 255, 0.25);
  color: #ffffff;
}
.admin-locale-tabs__summary {
  font-size: 12px;
  margin-inline-start: 4px;
}
.visually-hidden {
  border: 0;
  clip: rect(0 0 0 0);
  height: 1px;
  margin: -1px;
  overflow: hidden;
  padding: 0;
  position: absolute;
  width: 1px;
}
</style>
