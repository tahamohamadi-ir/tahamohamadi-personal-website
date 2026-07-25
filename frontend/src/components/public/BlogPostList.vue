<script setup>
import { useI18n } from 'vue-i18n'
import CollectionMedia from 'src/components/public/CollectionMedia.vue'
import { formatLocalizedDate } from 'src/utils/formatDate'

defineProps({
  posts: {
    type: Array,
    required: true
  }
})

const { locale } = useI18n()
</script>

<template>
  <ol class="blog-post-list">
    <li
      v-for="post in posts"
      :key="post.slug"
      class="blog-post-list__item"
    >
      <router-link
        class="blog-post-list__card tm-interactive"
        :to="post.canonicalPath"
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
              {{ formatLocalizedDate(post.publishedAt, locale) }}
            </time>
          </header>

          <p
            v-if="post.excerpt"
            class="blog-post-list__excerpt"
          >
            {{ post.excerpt }}
          </p>
        </article>
      </router-link>
    </li>
  </ol>
</template>

<style scoped lang="scss">
.blog-post-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 18rem), 1fr));
  gap: var(--tm-space-5);
  max-inline-size: 76rem;
  margin: 0;
  padding: 0;
  list-style: none;
}

.blog-post-list__item {
  min-inline-size: 0;
}

.blog-post-list__card {
  display: block;
  block-size: 100%;
  min-block-size: 13rem;
  padding: var(--tm-space-5);
  border: 1px solid var(--tm-border-subtle);
  border-radius: var(--tm-radius-card);
  background: var(--tm-surface);
  color: inherit;
  text-decoration: none;
  transition: border-color 180ms ease, box-shadow 180ms ease, transform 180ms ease;
}

.blog-post-list__card:hover {
  border-color: var(--tm-link);
  box-shadow: var(--tm-editorial-shadow);
  transform: translateY(-2px);
}

.blog-post-list__card:focus-visible {
  outline: 3px solid var(--tm-focus-ring);
  outline-offset: 3px;
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
  color: var(--tm-text-secondary);
}

@media (prefers-reduced-motion: reduce) {
  .blog-post-list__card {
    transition: none;
  }

  .blog-post-list__card:hover {
    transform: none;
  }
}
</style>
