<script setup>
import { computed } from 'vue'

const props = defineProps({
  media: {
    type: Object,
    required: true
  }
})

const renderable = computed(() => (
  typeof props.media?.url === 'string'
  && props.media.url.trim().length > 0
  && typeof props.media?.altText === 'string'
  && props.media.altText.trim().length > 0
))
</script>

<template>
  <div v-if="renderable" class="collection-media">
    <img
      class="collection-media__image"
      :src="media.url"
      :alt="media.altText"
    >
  </div>
</template>

<style scoped lang="scss">
.collection-media {
  overflow: hidden;
  aspect-ratio: 16 / 9;
  border: 1px solid var(--tm-border-subtle);
  border-radius: var(--tm-radius-card);
  background: var(--tm-surface);
}

.collection-media__image {
  display: block;
  inline-size: 100%;
  block-size: 100%;
  object-fit: cover;
}
</style>
