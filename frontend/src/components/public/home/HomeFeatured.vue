<script setup>
import { useI18n } from 'vue-i18n'

defineOptions({
  name: 'HomeFeatured'
})

defineProps({
  entries: {
    type: Array,
    default: () => []
  }
})

const { t } = useI18n()

function typeLabel(targetType) {
  return targetType === 'PUBLICATION'
    ? t('public.home.featuredTypes.publication')
    : t('public.home.featuredTypes.project')
}
</script>

<template>
  <section
    v-if="entries.length"
    class="home-featured"
    aria-labelledby="featured-title"
  >
    <div class="tm-container">
      <header class="home-featured__header">
        <p>{{ t('public.home.featuredLabel') }}</p>
        <h2 id="featured-title">
          {{ t('public.home.featured') }}
        </h2>
      </header>

      <div class="home-featured__list">
        <router-link
          v-for="(entry, index) in entries"
          :key="`${entry.targetType}:${entry.slug}`"
          :to="entry.path"
          class="home-featured__entry tm-interactive"
        >
          <span class="home-featured__index" aria-hidden="true">
            {{ String(index + 1).padStart(2, '0') }}
          </span>

          <span class="home-featured__copy">
            <small>{{ typeLabel(entry.targetType) }}</small>
            <strong>{{ entry.title }}</strong>
          </span>

          <span class="home-featured__action" aria-hidden="true">
            â†’
          </span>
        </router-link>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.home-featured {
  padding-block: clamp(var(--tm-space-8), 7vw, var(--tm-space-14));
  border-block-end: 1px solid var(--tm-border-subtle);
  background: var(--tm-surface);
}

.home-featured__header {
  display: grid;
  gap: var(--tm-space-2);
  margin-block-end: var(--tm-space-7);
}

.home-featured__header p {
  margin: 0;
  color: var(--tm-action-primary);
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.home-featured__header h2 {
  margin: 0;
  font-size: clamp(2rem, 4vw, 3.25rem);
  letter-spacing: -0.04em;
}

.home-featured__list {
  border-block-end: 1px solid var(--tm-border-subtle);
}

.home-featured__entry {
  display: grid;
  grid-template-columns: 3rem minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--tm-space-4);
  min-block-size: 7rem;
  padding-block: var(--tm-space-5);
  border-block-start: 1px solid var(--tm-border-subtle);
  color: var(--tm-text-primary);
  text-decoration: none;
}

.home-featured__entry:hover {
  background: var(--tm-interactive-surface-hover);
  color: var(--tm-interactive-hover);
}

.home-featured__index,
.home-featured__copy small {
  color: var(--tm-text-secondary);
  font-size: 0.75rem;
  font-weight: 700;
}

.home-featured__copy {
  display: grid;
  gap: var(--tm-space-2);
}

.home-featured__copy strong {
  font-size: clamp(1.125rem, 2vw, 1.5rem);
  letter-spacing: -0.02em;
}

.home-featured__action {
  font-size: 1.5rem;
}
</style>
