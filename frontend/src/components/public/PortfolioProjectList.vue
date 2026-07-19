<script setup>
import CollectionMedia from 'src/components/public/CollectionMedia.vue'

defineProps({
  projects: {
    type: Array,
    required: true
  }
})
</script>

<template>
  <ol class="portfolio-project-list">
    <li
      v-for="project in projects"
      :key="project.slug"
      class="portfolio-project-list__item"
    >
      <article class="portfolio-project-list__article">
        <CollectionMedia
          v-if="project.ogMedia"
          :media="project.ogMedia"
        />
        <h2 class="portfolio-project-list__title">{{ project.title }}</h2>
        <p
          v-if="project.summary"
          class="portfolio-project-list__summary"
        >
          {{ project.summary }}
        </p>
      </article>
    </li>
  </ol>
</template>

<style scoped lang="scss">
.portfolio-project-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 17rem), 1fr));
  gap: var(--tm-space-4);
  margin: 0;
  padding: 0;
  list-style: none;
}

.portfolio-project-list__item {
  min-inline-size: 0;
  padding: var(--tm-space-6);
  border: 1px solid var(--tm-border-subtle);
  border-radius: var(--tm-radius-card);
  background: var(--tm-surface);
}

.portfolio-project-list__article {
  display: grid;
  gap: var(--tm-space-3);
}

.portfolio-project-list__title,
.portfolio-project-list__summary {
  margin: 0;
}

.portfolio-project-list__title {
  color: var(--tm-text-primary);
  font-size: 1.25rem;
  line-height: 1.3;
}

.portfolio-project-list__summary {
  color: var(--tm-text-secondary);
}
</style>
