<script setup>
import { useI18n } from 'vue-i18n'

import TmButton from 'src/components/shared/TmButton.vue'

defineOptions({
  name: 'HomeResearchWriting'
})

defineProps({
  locale: {
    type: String,
    required: true
  }
})

const { t } = useI18n()

const researchTopics = [
  'public.home.research.topics.humanAi',
  'public.home.research.topics.explainability',
  'public.home.research.topics.wearables'
]
</script>

<template>
  <section class="home-research-writing">
    <div class="tm-container home-research-writing__grid">
      <article aria-labelledby="research-index-title">
        <p class="home-section-label">
          {{ t('public.home.research.label') }}
        </p>
        <h2 id="research-index-title">
          {{ t('public.home.research.title') }}
        </h2>

        <ol class="home-research-writing__index">
          <li
            v-for="(topic, index) in researchTopics"
            :key="topic"
          >
            <span aria-hidden="true">
              {{ String(index + 1).padStart(2, '0') }}
            </span>
            <strong>{{ t(topic) }}</strong>
          </li>
        </ol>

        <div class="home-research-writing__actions">
          <TmButton
            variant="secondary"
            :to="`/${locale}/research`"
          >
            {{ t('public.home.research.cta') }}
          </TmButton>

          <TmButton
            variant="text"
            :to="`/${locale}/publications`"
          >
            {{ t('public.home.research.publicationsCta') }}
          </TmButton>
        </div>
      </article>

      <article
        class="home-research-writing__writing"
        aria-labelledby="writing-title"
      >
        <p class="home-section-label">
          {{ t('public.home.writing.label') }}
        </p>
        <h2 id="writing-title">
          {{ t('public.home.writing.title') }}
        </h2>
        <p>{{ t('public.home.writing.description') }}</p>

        <TmButton
          variant="secondary"
          :to="`/${locale}/blog`"
        >
          {{ t('public.home.writing.cta') }}
        </TmButton>
      </article>
    </div>
  </section>
</template>

<style scoped lang="scss">
.home-research-writing {
  padding-block: clamp(var(--tm-space-8), 7vw, var(--tm-space-14));
  border-block-end: 1px solid var(--tm-border-subtle);
  background: var(--tm-canvas);
}

.home-research-writing__grid {
  display: grid;
  gap: var(--tm-space-10);
}

.home-section-label {
  margin: 0 0 var(--tm-space-3);
  color: var(--tm-action-primary);
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.home-research-writing h2 {
  max-inline-size: 16ch;
  margin: 0;
  font-size: clamp(2rem, 4vw, 3.25rem);
  letter-spacing: -0.04em;
  line-height: 1.08;
}

.home-research-writing__index {
  margin: var(--tm-space-7) 0 0;
  padding: 0;
  list-style: none;
}

.home-research-writing__index li {
  display: grid;
  grid-template-columns: 3rem minmax(0, 1fr);
  gap: var(--tm-space-4);
  padding-block: var(--tm-space-4);
  border-block-start: 1px solid var(--tm-border-subtle);
}

.home-research-writing__index span {
  color: var(--tm-action-secondary);
  font-size: 0.75rem;
  font-weight: 800;
}

.home-research-writing__index strong {
  font-size: 1.0625rem;
}

.home-research-writing__actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-3);
  margin-block-start: var(--tm-space-6);
}

.home-research-writing__writing {
  padding-block-start: var(--tm-space-7);
  border-block-start: 1px solid var(--tm-border-subtle);
}

.home-research-writing__writing > p:not(.home-section-label) {
  max-inline-size: 38rem;
  margin: var(--tm-space-5) 0 var(--tm-space-6);
  color: var(--tm-text-secondary);
  font-size: 1.0625rem;
  line-height: 1.7;
}

@media (min-width: 900px) {
  .home-research-writing__grid {
    grid-template-columns: minmax(0, 1.15fr) minmax(18rem, 0.85fr);
  }

  .home-research-writing__writing {
    padding-block-start: 0;
    padding-inline-start: var(--tm-space-8);
    border-block-start: 0;
    border-inline-start: 1px solid var(--tm-border-subtle);
  }
}
</style>
