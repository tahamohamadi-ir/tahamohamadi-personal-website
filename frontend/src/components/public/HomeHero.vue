<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  page: {
    type: Object,
    default: null
  },
  locale: {
    type: String,
    required: true
  }
})

const { t } = useI18n()

const greeting = computed(() => t('public.homeHero.greeting'))
const statement = computed(() => t('public.homeHero.statement'))
const eyebrow = computed(() => t('public.homeHero.eyebrow'))

const statusItems = computed(() => {
  return [
    { label: t('public.homeHero.status.currently.label'), value: t('public.homeHero.status.currently.value') },
    { label: t('public.homeHero.status.researching.label'), value: t('public.homeHero.status.researching.value') },
    { label: t('public.homeHero.status.basedIn.label'), value: t('public.homeHero.status.basedIn.value') },
    { label: t('public.homeHero.status.availableFor.label'), value: t('public.homeHero.status.availableFor.value') }
  ]
})
</script>

<template>
  <section class="tm-container home-hero">
    <div class="home-hero__content">
      <span class="tm-eyebrow">OPEN TO PhD RESEARCH · HUMAN-CENTERED AI</span>
      <h1 class="tm-display home-hero__title">
        <template v-if="page?.title">
          {{ page.title }}
        </template>
        <template v-else>
          <span class="home-hero__greeting">{{ t('public.homeHero.greeting') }}</span>
          <br /> {{ t('public.homeHero.title') }}
        </template>
      </h1>
      <p class="home-hero__summary">
        {{ page?.summary || t('public.homeHero.summary') }}
      </p>
    </div>
    
    <aside class="home-hero__status">
      <div class="status-card">
        <div v-for="(item, index) in statusItems" :key="index" class="status-card__row">
          <dt class="status-card__label tm-mono">{{ item.label }}</dt>
          <dd class="status-card__value">{{ item.value }}</dd>
        </div>
      </div>
    </aside>
  </section>
</template>

<style scoped>
.home-hero {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--tm-space-8);
  padding-block: clamp(var(--tm-space-10), 10vw, var(--tm-space-16));
}

@media (min-width: 1024px) {
  .home-hero {
    grid-template-columns: 7fr 4fr;
    gap: var(--tm-space-12);
    align-items: start;
  }
}

.home-hero__title {
  margin-block: var(--tm-space-4);
  max-inline-size: 15ch;
}

[lang='fa'] .home-hero__title {
  max-inline-size: 20ch;
}

.home-hero__greeting {
  color: var(--tm-text-secondary);
}

.home-hero__summary {
  color: var(--tm-text-secondary);
  font-size: clamp(1.125rem, 2vw, 1.25rem);
  line-height: 1.7;
  max-inline-size: 50ch;
  margin-top: var(--tm-space-6);
}

.status-card {
  background: var(--tm-surface);
  border: 1px solid var(--tm-border-subtle);
  border-radius: var(--tm-radius-card);
  padding: var(--tm-space-6);
  display: flex;
  flex-direction: column;
  gap: var(--tm-space-4);
  box-shadow: var(--tm-editorial-shadow);
}

.status-card__row {
  display: flex;
  flex-direction: column;
  gap: var(--tm-space-1);
  padding-bottom: var(--tm-space-3);
  border-bottom: 1px solid var(--tm-border-subtle);
}

.status-card__row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.status-card__label {
  font-size: 0.75rem;
  color: var(--tm-text-secondary);
  letter-spacing: 0.05em;
}

.status-card__value {
  margin: 0;
  font-weight: 500;
  color: var(--tm-text-primary);
  line-height: 1.4;
}
</style>
