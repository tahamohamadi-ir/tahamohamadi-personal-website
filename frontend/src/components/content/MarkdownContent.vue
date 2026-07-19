<script setup>
import { computed } from 'vue'
import { renderSafeMarkdown } from './safeMarkdown'

const props = defineProps({
  markdown: {
    type: String,
    default: ''
  }
})

const result = computed(() => renderSafeMarkdown(props.markdown))
</script>

<template>
  <div
    v-if="result.status === 'ready' && result.html"
    class="tm-rich-content"
    v-html="result.html"
  />
  <slot
    v-else-if="result.status === 'error'"
    name="error"
  />
</template>
