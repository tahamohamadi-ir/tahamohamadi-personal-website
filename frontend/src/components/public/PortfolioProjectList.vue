<script setup>
import { useI18n } from 'vue-i18n'
import CollectionMedia from 'src/components/public/CollectionMedia.vue'

defineProps({
  projects: {
    type: Array,
    required: true
  }
})

const { t } = useI18n()
</script>

<template>
  <ol class="portfolio-project-list">
    <li
      v-for="project in projects"
      :key="project.slug"
      class="portfolio-project-list__item"
    >
      <router-link
        class="portfolio-project-list__card tm-interactive"
        :to="project.canonicalPath"
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
          <span class="portfolio-project-list__action">
            {{ t('public.caseStudy.viewProject') }}
          </span>
        </article>
      </router-link>
    </li>
  </ol>
</template>

<style scoped lang="scss">
.portfolio-project-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 18rem), 1fr));
  gap: var(--tm-space-5);
  max-inline-size: 76rem;
  margin: 0;
  padding: 0;
  list-style: none;
}

.portfolio-project-list__item {
  min-inline-size: 0;
}

.portfolio-project-list__card {
  display: block;
  block-size: 100%;
  min-block-size: 17rem;
  padding: var(--tm-space-5);
  border: 1px solid var(--tm-editorial-card-border);
  border-radius: var(--tm-radius-card);
  background: var(--tm-editorial-card-surface);
  color: inherit;
  text-decoration: none;
  transition: border-color 180ms ease, box-shadow 180ms ease, transform 180ms ease;
}

.portfolio-project-list__card:hover {
  border-color: var(--tm-link);
  box-shadow: var(--tm-editorial-shadow);
  transform: translateY(-2px);
}

.portfolio-project-list__card:focus-visible {
  outline: 3px solid var(--tm-focus-ring);
  outline-offset: 3px;
}

.portfolio-project-list__article {
  display: grid;
  gap: var(--tm-space-3);
  block-size: 100%;
}

.portfolio-project-list__title,
.portfolio-project-list__summary {
  margin: 0;
}

.portfolio-project-list__title {
  color: var(--tm-text-primary);
  font-size: 1.375rem;
  line-height: 1.25;
}

.portfolio-project-list__summary {
  color: var(--tm-text-secondary);
}

.portfolio-project-list__action {
  align-self: end;
  min-block-size: var(--tm-control-min-size);
  margin-block-start: auto;
  padding-block-start: var(--tm-space-3);
  border-block-start: 1px solid var(--tm-editorial-rule);
  color: var(--tm-link);
  font-size: .9375rem;
  font-weight: 700;
}

.portfolio-project-list__card:hover .portfolio-project-list__action {
  color: var(--tm-text-primary);
}

@media (prefers-reduced-motion: reduce) {
  .portfolio-project-list__card {
    transition: none;
  }

  .portfolio-project-list__card:hover {
    transform: none;
  }
}
</style>
