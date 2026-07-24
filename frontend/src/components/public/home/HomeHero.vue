<script setup>
import { useI18n } from 'vue-i18n'

import TmButton from 'src/components/shared/TmButton.vue'

defineOptions({
  name: 'HomeHero'
})

defineProps({
  locale: {
    type: String,
    required: true
  }
})

const { t } = useI18n()
</script>

<template>
  <section class="home-hero" aria-labelledby="home-title">
    <div class="tm-container home-hero__layout">
      <div class="home-hero__content">
        <p class="home-kicker">
          {{ t('public.home.eyebrow') }}
        </p>

        <slot name="identity" />

        <p class="home-hero__statement">
          {{ t('public.home.headline') }}
        </p>

        <p class="home-hero__lead">
          {{ t('public.home.lead') }}
        </p>

        <div class="home-hero__actions">
          <TmButton :to="`/${locale}/portfolio`">
            {{ t('public.home.viewWork') }}
          </TmButton>

          <TmButton
            variant="secondary"
            :to="`/${locale}/research`"
          >
            {{ t('public.home.exploreResearch') }}
          </TmButton>
        </div>
      </div>

      <aside
        class="home-hero__context"
        :aria-label="t('public.home.contextLabel')"
      >
        <dl class="home-hero__facts">
          <div>
            <dt>{{ t('public.home.currentFocusLabel') }}</dt>
            <dd>{{ t('public.home.currentFocus') }}</dd>
          </div>

          <div>
            <dt>{{ t('public.home.practiceLabel') }}</dt>
            <dd>{{ t('public.home.practiceValue') }}</dd>
          </div>

          <div>
            <dt>{{ t('public.home.availabilityLabel') }}</dt>
            <dd>{{ t('public.home.availability') }}</dd>
          </div>
        </dl>
      </aside>
    </div>
  </section>
</template>

<style scoped lang="scss">
.home-hero {
  padding-block: clamp(var(--tm-space-8), 9vw, var(--tm-space-16));
  border-block-end: 1px solid var(--tm-border-subtle);
  background: var(--tm-canvas);
}

.home-hero__layout {
  display: grid;
  gap: var(--tm-space-8);
}

.home-hero__content {
  max-inline-size: 48rem;
}

.home-kicker {
  margin: 0 0 var(--tm-space-4);
  color: var(--tm-action-primary);
  font-size: 0.8125rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  line-height: 1.5;
  text-transform: uppercase;
}

:slotted(.home-hero__identity) {
  margin: 0 0 var(--tm-space-4);
  color: var(--tm-text-secondary);
  font-size: 1rem;
  font-weight: 700;
  letter-spacing: 0.03em;
}

.home-hero__statement {
  max-inline-size: 13ch;
  margin: 0;
  color: var(--tm-text-primary);
  font-size: clamp(2.75rem, 7vw, 5.75rem);
  font-weight: 780;
  letter-spacing: -0.055em;
  line-height: 0.98;
}

.home-hero__lead {
  max-inline-size: 42rem;
  margin: var(--tm-space-6) 0 0;
  color: var(--tm-text-secondary);
  font-size: clamp(1.0625rem, 2vw, 1.3125rem);
  line-height: 1.7;
}

.home-hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-3);
  margin-block-start: var(--tm-space-7);
}

.home-hero__context {
  padding-block-start: var(--tm-space-6);
  border-block-start: 1px solid var(--tm-border-subtle);
}

.home-hero__facts {
  display: grid;
  gap: 0;
  margin: 0;
}

.home-hero__facts div {
  padding-block: var(--tm-space-4);
  border-block-start: 1px solid var(--tm-border-subtle);
}

.home-hero__facts div:first-child {
  padding-block-start: 0;
  border-block-start: 0;
}

.home-hero__facts dt {
  margin-block-end: var(--tm-space-2);
  color: var(--tm-action-primary);
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.home-hero__facts dd {
  margin: 0;
  color: var(--tm-text-primary);
  font-size: 1rem;
  line-height: 1.6;
}

@media (min-width: 960px) {
  .home-hero__layout {
    grid-template-columns: minmax(0, 1fr) minmax(16rem, 0.42fr);
    align-items: end;
    gap: clamp(var(--tm-space-8), 7vw, var(--tm-space-16));
  }

  .home-hero__context {
    padding-block-start: 0;
    padding-inline-start: var(--tm-space-7);
    border-block-start: 0;
    border-inline-start: 1px solid var(--tm-border-subtle);
  }
}

@media (min-width: 600px) and (max-width: 959px) {
  .home-hero {
    padding-block: var(--tm-space-12);
  }

  .home-hero__statement {
    max-inline-size: 17ch;
    font-size: clamp(3rem, 5.3vw, 3.75rem);
    line-height: 1.02;
  }

  :global([lang='fa'] .home-hero__statement) {
    max-inline-size: 15ch;
    font-size: clamp(2.875rem, 5vw, 3.5rem);
    letter-spacing: normal;
    line-height: 1.28;
  }
}
</style>
