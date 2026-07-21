<script setup>
import { computed } from 'vue'

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
const locales = [
  { value: 'fa', label: 'فارسی' },
  { value: 'en', label: 'English' }
]

const missingLocale = computed(() => locales.find(
  ({ value }) => isMissingTranslation(props.translations, value)
)?.value)
</script>

<template>
  <section aria-label="Content translations">
    <div class="admin-locale-tabs" role="tablist" aria-label="Content locale">
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
          (Missing translation)
        </span>
      </button>
    </div>
    <p v-if="missingLocale" class="text-caption q-mt-sm q-mb-none" role="status">
      Missing translation: {{ missingLocale === 'fa' ? 'فارسی' : 'English' }}.
    </p>
  </section>
</template>

<style scoped>
.admin-locale-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.admin-locale-tabs__tab {
  min-block-size: 2.75rem;
  padding-inline: 1rem;
  border: 1px solid currentColor;
  border-radius: 0.25rem;
  background: transparent;
  color: inherit;
  cursor: pointer;
}

.admin-locale-tabs__tab[aria-selected='true'] {
  background: var(--q-primary);
  color: white;
}
</style>
