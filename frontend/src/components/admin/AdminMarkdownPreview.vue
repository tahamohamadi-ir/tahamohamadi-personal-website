<script setup>
import { useI18n } from 'vue-i18n'

import MarkdownContent from 'src/components/content/MarkdownContent.vue'

defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])
const { t } = useI18n()
</script>

<template>
  <section class="admin-markdown-preview" :aria-label="t('admin.markdown.editorAndPreview')">
    <label>
      {{ t('admin.markdown.source') }}
      <textarea
        :value="modelValue"
        rows="12"
        @input="emit('update:modelValue', $event.target.value)"
      />
    </label>
    <section :aria-label="t('admin.markdown.safePreview')">
      <h2 class="text-subtitle1">{{ t('admin.markdown.preview') }}</h2>
      <MarkdownContent :markdown="modelValue">
        <template #error>
          <p role="alert">{{ t('admin.markdown.previewError') }}</p>
        </template>
      </MarkdownContent>
    </section>
  </section>
</template>

<style scoped>
.admin-markdown-preview {
  display: grid;
  gap: var(--tm-space-6);
}

textarea {
  display: block;
  inline-size: 100%;
  min-block-size: 12rem;
  margin-block-start: var(--tm-space-2);
  padding: var(--tm-space-3);
  border: 1px solid var(--tm-admin-border);
  border-radius: var(--tm-admin-control-radius);
  background: var(--tm-admin-surface);
  color: var(--tm-text-primary);
  font: inherit;
}

textarea:focus-visible { outline: 3px solid var(--tm-focus-ring); outline-offset: 2px; }
</style>
