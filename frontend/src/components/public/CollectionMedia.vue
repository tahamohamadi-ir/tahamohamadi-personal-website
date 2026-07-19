<script setup>
defineProps({
  media: {
    type: Object,
    required: true
  }
})
</script>

<template>
  <div class="collection-media">
    <img
      class="collection-media__image"
      :src="media.url"
      :alt="media.altText ?? ''"
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
