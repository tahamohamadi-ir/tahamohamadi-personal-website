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
</script>

<template>
  <section :aria-label="t('admin.localeTabs.sectionLabel')">
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
        {{ locale.label }}
        <span v-if="isMissingTranslation(translations, locale.value)">
          ({{ t('admin.localeTabs.missing') }})
        </span>
      </button>
    </div>
    <p v-if="missingLocale" class="text-caption q-mt-sm q-mb-none" role="status">
      {{ t('admin.localeTabs.missingStatus', { locale: missingLocale === 'fa' ? t('admin.localeTabs.persian') : t('admin.localeTabs.english') }) }}
    </p>
  </section>
</template>

<style scoped>
.admin-locale-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-2);
}

.admin-locale-tabs__tab {
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-4);
  border: 1px solid var(--tm-admin-border);
  border-radius: var(--tm-admin-control-radius);
  background: transparent;
  color: var(--tm-text-primary);
  cursor: pointer;
}

.admin-locale-tabs__tab[aria-selected='true'] {
  border-color: var(--tm-action-primary);
  background: var(--tm-action-primary);
  color: var(--tm-white);
}

.admin-locale-tabs__tab:focus-visible {
  outline: 3px solid var(--tm-focus-ring);
  outline-offset: 2px;
}
</style>
