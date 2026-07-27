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
        class="portfolio-project-list__link tm-interactive"
        :to="project.canonicalPath"
      >
        <article class="portfolio-project-list__article">
          <div class="portfolio-project-list__content">
            <h2 class="portfolio-project-list__title">{{ project.title }}</h2>
            <p
              v-if="project.summary"
              class="portfolio-project-list__summary"
            >
              {{ project.summary }}
            </p>
          </div>
          <span class="portfolio-project-list__action tm-mono">
            {{ t('public.caseStudy.viewProject') }}
          </span>
        </article>
      </router-link>
    </li>
  </ol>
</template>

<style scoped lang="scss">
.portfolio-project-list {
  display: flex;
  flex-direction: column;
  margin: 0;
  padding: 0;
  list-style: none;
  max-inline-size: 64rem;
}

.portfolio-project-list__item {
  border-bottom: 1px solid var(--tm-editorial-rule);
}

.portfolio-project-list__item:first-child {
  border-top: 1px solid var(--tm-editorial-rule);
}

.portfolio-project-list__link {
  display: block;
  padding-block: var(--tm-space-6);
  padding-inline: var(--tm-space-4);
  color: inherit;
  text-decoration: none;
  transition: background-color 180ms ease, transform 180ms ease;
  margin-inline: calc(var(--tm-space-4) * -1); /* Negative margin to let hover bleed */
  border-radius: var(--tm-radius-card);
}

.portfolio-project-list__link:hover {
  background-color: var(--tm-editorial-muted-surface);
}

.portfolio-project-list__link:focus-visible {
  outline: 3px solid var(--tm-focus-ring);
  outline-offset: -3px;
  background-color: var(--tm-editorial-muted-surface);
}

.portfolio-project-list__article {
  display: grid;
  gap: var(--tm-space-4);
  align-items: center;
}

@media (min-width: 768px) {
  .portfolio-project-list__article {
    grid-template-columns: 1fr auto;
    gap: var(--tm-space-8);
  }
}

.portfolio-project-list__content {
  display: flex;
  flex-direction: column;
  gap: var(--tm-space-2);
}

.portfolio-project-list__title {
  margin: 0;
  font-size: clamp(1.25rem, 3vw, 1.75rem);
  font-weight: 500;
  color: var(--tm-text-primary);
  letter-spacing: -0.02em;
}

.portfolio-project-list__summary {
  margin: 0;
  color: var(--tm-text-secondary);
  font-size: 1.0625rem;
  line-height: 1.6;
  max-inline-size: 65ch;
}

.portfolio-project-list__action {
  font-size: 0.8125rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--tm-link);
  display: inline-flex;
  align-items: center;
}

.portfolio-project-list__link:hover .portfolio-project-list__action {
  color: var(--tm-text-primary);
}

@media (prefers-reduced-motion: reduce) {
  .portfolio-project-list__link {
    transition: none;
  }
}
</style>
