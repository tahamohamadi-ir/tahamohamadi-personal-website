<script setup>
import { useI18n } from 'vue-i18n'

defineOptions({
  name: 'HomePractice'
})

const { t } = useI18n()

const practices = [
  {
    number: '01',
    titleKey: 'public.home.practice.understand.title',
    bodyKey: 'public.home.practice.understand.body'
  },
  {
    number: '02',
    titleKey: 'public.home.practice.design.title',
    bodyKey: 'public.home.practice.design.body'
  },
  {
    number: '03',
    titleKey: 'public.home.practice.build.title',
    bodyKey: 'public.home.practice.build.body'
  }
]
</script>

<template>
  <section class="home-section home-practice" aria-labelledby="practice-title">
    <div class="tm-container">
      <header class="home-section__header">
        <p class="home-section__label">
          {{ t('public.home.practice.sectionLabel') }}
        </p>
        <h2 id="practice-title">
          {{ t('public.home.practice.title') }}
        </h2>
        <p>
          {{ t('public.home.practice.introduction') }}
        </p>
      </header>

      <ol class="home-practice__list">
        <li
          v-for="practice in practices"
          :key="practice.number"
          class="home-practice__item"
        >
          <span class="home-practice__number" aria-hidden="true">
            {{ practice.number }}
          </span>
          <h3>{{ t(practice.titleKey) }}</h3>
          <p>{{ t(practice.bodyKey) }}</p>
        </li>
      </ol>
    </div>
  </section>
</template>

<style scoped lang="scss">
.home-section {
  padding-block: clamp(var(--tm-space-8), 7vw, var(--tm-space-14));
  border-block-end: 1px solid var(--tm-border-subtle);
}

.home-section__header {
  max-inline-size: 46rem;
  margin-block-end: var(--tm-space-8);
}

.home-section__label {
  margin: 0 0 var(--tm-space-3);
  color: var(--tm-action-primary);
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.home-section__header h2 {
  margin: 0;
  font-size: clamp(2rem, 4vw, 3.5rem);
  letter-spacing: -0.04em;
  line-height: 1.05;
}

.home-section__header > p:last-child {
  margin: var(--tm-space-4) 0 0;
  color: var(--tm-text-secondary);
  font-size: 1.0625rem;
  line-height: 1.7;
}

.home-practice__list {
  display: grid;
  margin: 0;
  padding: 0;
  list-style: none;
}

.home-practice__item {
  display: grid;
  grid-template-columns: 3.5rem minmax(0, 1fr);
  gap: var(--tm-space-3) var(--tm-space-5);
  padding-block: var(--tm-space-6);
  border-block-start: 1px solid var(--tm-border-subtle);
}

.home-practice__number {
  grid-row: 1 / 3;
  color: var(--tm-action-secondary);
  font-size: 0.8125rem;
  font-weight: 800;
}

.home-practice__item h3 {
  margin: 0;
  font-size: 1.375rem;
  letter-spacing: -0.025em;
}

.home-practice__item p {
  margin: 0;
  color: var(--tm-text-secondary);
  line-height: 1.65;
}

@media (min-width: 800px) {
  .home-practice__list {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: var(--tm-space-6);
  }

  .home-practice__item {
    grid-template-columns: 1fr;
    align-content: start;
    padding-inline-end: var(--tm-space-5);
  }

  .home-practice__number {
    grid-row: auto;
  }
}
</style>
