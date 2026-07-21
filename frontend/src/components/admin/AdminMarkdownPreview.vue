<script setup>
import MarkdownContent from 'src/components/content/MarkdownContent.vue'

defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])
</script>

<template>
  <section class="admin-markdown-preview" aria-label="Markdown editor and preview">
    <label>
      Markdown
      <textarea
        :value="modelValue"
        rows="12"
        @input="emit('update:modelValue', $event.target.value)"
      />
    </label>
    <section aria-label="Safe Markdown preview">
      <h2 class="text-subtitle1">Preview</h2>
      <MarkdownContent :markdown="modelValue">
        <template #error>
          <p role="alert">The Markdown preview could not be rendered safely.</p>
        </template>
      </MarkdownContent>
    </section>
  </section>
</template>

<style scoped>
.admin-markdown-preview {
  display: grid;
  gap: 1.5rem;
}

textarea {
  display: block;
  inline-size: 100%;
  margin-block-start: 0.5rem;
  font: inherit;
}
</style>
