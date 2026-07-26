<script setup>
import { computed } from 'vue'

import MarkdownContent from 'src/components/content/MarkdownContent.vue'

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i
const props = defineProps({ document: { type: Object, default: null } })
const blocks = computed(() => Array.isArray(props.document?.blocks) ? props.document.blocks : [])
const headingTag = (level) => `h${Math.max(2, Math.min(6, Number(level) || 2))}`
const headingId = (index) => `article-heading-${index}`
const mediaUrl = (mediaId) => typeof mediaId === 'string' && UUID.test(mediaId) ? `/api/v1/public/media/${mediaId}` : null
</script>

<template>
  <section class="article-document-content">
    <template v-for="(block, index) in blocks" :key="index">
      <p v-if="block?.type === 'paragraph'">{{ block.value }}</p>
      <component :is="headingTag(block.level)" v-else-if="block?.type === 'heading'" :id="headingId(index)">{{ block.value }}</component>
      <blockquote v-else-if="block?.type === 'quote'"><p>{{ block.value }}</p></blockquote>
      <pre v-else-if="block?.type === 'code'"><code>{{ block.value }}</code></pre>
      <figure v-else-if="block?.type === 'image' && mediaUrl(block.mediaId)">
        <img :src="mediaUrl(block.mediaId)" :alt="block.alt || ''" loading="lazy">
        <figcaption v-if="block.caption">{{ block.caption }}</figcaption>
      </figure>
      <hr v-else-if="block?.type === 'divider'">
      <MarkdownContent v-else-if="block?.type === 'markdown'" :markdown="block.value || ''" />
    </template>
  </section>
</template>

<style scoped>
.article-document-content { display: grid; gap: var(--tm-space-4); }
.article-document-content > :is(p, h2, h3, h4, h5, h6, blockquote, pre, figure, hr) { margin: 0; }
.article-document-content blockquote { border-inline-start: 3px solid var(--tm-color-border-strong); padding-inline-start: var(--tm-space-4); }
.article-document-content pre { overflow-x: auto; padding: var(--tm-space-4); }
.article-document-content figure { display: grid; gap: var(--tm-space-2); }
.article-document-content img { block-size: auto; inline-size: 100%; }
</style>
