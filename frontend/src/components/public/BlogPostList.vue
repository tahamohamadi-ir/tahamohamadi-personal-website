<script setup>
import CollectionMedia from 'src/components/public/CollectionMedia.vue'

defineProps({
  posts: {
    type: Array,
    required: true
  }
})
</script>

<template>
  <ol class="blog-post-list">
    <li
      v-for="post in posts"
      :key="post.slug"
      class="blog-post-list__item"
    >
      <article class="blog-post-list__article">
        <CollectionMedia
          v-if="post.ogMedia"
          :media="post.ogMedia"
        />
        <header class="blog-post-list__header">
          <h2 class="blog-post-list__title">{{ post.title }}</h2>
          <time
            v-if="post.publishedAt"
            class="blog-post-list__date"
            :datetime="post.publishedAt"
          >
            {{ post.publishedAt }}
          </time>
        </header>

        <p
          v-if="post.excerpt"
          class="blog-post-list__excerpt"
        >
          {{ post.excerpt }}
        </p>
      </article>
    </li>
  </ol>
</template>

<style scoped lang="scss">
.blog-post-list {
  display: grid;
  gap: var(--tm-space-6);
  max-inline-size: var(--tm-prose-max-width);
  margin: 0;
  padding: 0;
  list-style: none;
}

.blog-post-list__item {
  padding-block-end: var(--tm-space-6);
  border-block-end: 1px solid var(--tm-border-subtle);
}

.blog-post-list__item:last-child {
  padding-block-end: 0;
  border-block-end: 0;
}

.blog-post-list__article {
  display: grid;
  gap: var(--tm-space-3);
}

.blog-post-list__header {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-2);
  align-items: baseline;
  justify-content: space-between;
}

.blog-post-list__title,
.blog-post-list__excerpt,
.blog-post-list__date {
  margin: 0;
}

.blog-post-list__title {
  color: var(--tm-text-primary);
  font-size: 1.375rem;
  line-height: 1.3;
}

.blog-post-list__date {
  color: var(--tm-text-secondary);
  font-size: 0.9375rem;
}

.blog-post-list__excerpt {
  margin-block-start: var(--tm-space-3);
  color: var(--tm-text-secondary);
}
</style>
