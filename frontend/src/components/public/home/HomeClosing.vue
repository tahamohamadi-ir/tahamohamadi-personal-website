<script setup>
import { useI18n } from 'vue-i18n'

import TmButton from 'src/components/shared/TmButton.vue'

defineOptions({
  name: 'HomeClosing'
})

defineProps({
  locale: {
    type: String,
    required: true
  },
  socialLinks: {
    type: Array,
    default: () => []
  }
})

const { t } = useI18n()
</script>

<template>
  <div class="home-closing">
    <section class="home-visual" aria-labelledby="visual-title">
      <div class="tm-container home-visual__grid">
        <div class="home-visual__copy">
          <p class="home-section-label">
            {{ t('public.home.visual.label') }}
          </p>
          <h2 id="visual-title">
            {{ t('public.home.visual.title') }}
          </h2>
          <p>{{ t('public.home.visual.description') }}</p>

          <TmButton
            variant="secondary"
            :to="`/${locale}/portfolio`"
          >
            {{ t('public.home.visual.cta') }}
          </TmButton>
        </div>

        <div class="home-visual__frame" aria-hidden="true">
          <span class="home-visual__tile home-visual__tile--wide" />
          <span class="home-visual__tile" />
          <span class="home-visual__tile" />
          <small>{{ t('public.home.visual.archiveLabel') }}</small>
        </div>
      </div>
    </section>

    <section class="home-about" aria-labelledby="about-snapshot-title">
      <div class="tm-container home-about__grid">
        <p class="home-section-label">
          {{ t('public.home.about.label') }}
        </p>

        <div>
          <h2 id="about-snapshot-title">
            {{ t('public.home.about.title') }}
          </h2>
          <p>{{ t('public.home.about.description') }}</p>

          <TmButton
            variant="text"
            :to="`/${locale}/about`"
          >
            {{ t('public.home.about.cta') }}
          </TmButton>
        </div>
      </div>
    </section>

    <section class="home-contact" aria-labelledby="contact-cta-title">
      <div class="tm-container home-contact__grid">
        <div>
          <p class="home-section-label">
            {{ t('public.home.contact.label') }}
          </p>
          <h2 id="contact-cta-title">
            {{ t('public.home.contact.title') }}
          </h2>
          <p>{{ t('public.home.contact.description') }}</p>
        </div>

        <TmButton :to="`/${locale}/contact`">
          {{ t('public.home.contact.cta') }}
        </TmButton>
      </div>

      <nav
        v-if="socialLinks.length"
        class="tm-container home-social"
        :aria-label="t('public.home.socialLinks')"
      >
        <TmButton
          v-for="link in socialLinks"
          :key="`${link.platformCode}:${link.url}`"
          variant="text"
          :href="link.url"
          target="_blank"
        >
          {{ link.platformCode }}
        </TmButton>
      </nav>
    </section>
  </div>
</template>

<style scoped lang="scss">
.home-section-label {
  margin: 0 0 var(--tm-space-3);
  color: var(--tm-action-primary);
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.home-visual,
.home-about,
.home-contact {
  padding-block: clamp(var(--tm-space-8), 7vw, var(--tm-space-14));
  border-block-end: 1px solid var(--tm-border-subtle);
}

.home-visual__grid,
.home-about__grid,
.home-contact__grid {
  display: grid;
  gap: var(--tm-space-8);
}

.home-visual h2,
.home-about h2,
.home-contact h2 {
  max-inline-size: 17ch;
  margin: 0;
  font-size: clamp(2rem, 4vw, 3.25rem);
  letter-spacing: -0.04em;
  line-height: 1.08;
}

.home-visual__copy > p:not(.home-section-label),
.home-about__grid > div > p,
.home-contact__grid > div > p:not(.home-section-label) {
  max-inline-size: 40rem;
  margin: var(--tm-space-5) 0 var(--tm-space-6);
  color: var(--tm-text-secondary);
  font-size: 1.0625rem;
  line-height: 1.7;
}

.home-visual__frame {
  display: grid;
  grid-template-columns: 1.4fr 0.8fr;
  grid-template-rows: repeat(2, minmax(7rem, 1fr));
  gap: var(--tm-space-3);
  min-block-size: 24rem;
  padding: var(--tm-space-4);
  border: 1px solid var(--tm-border-subtle);
  background: var(--tm-surface);
}

.home-visual__tile {
  border: 1px solid var(--tm-border-subtle);
  background: var(--tm-interactive-surface-hover);
}

.home-visual__tile--wide {
  grid-row: 1 / 3;
  background: var(--tm-bg-accent-soft);
}

.home-visual__frame small {
  align-self: end;
  color: var(--tm-text-secondary);
  font-size: 0.6875rem;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.home-about {
  background: var(--tm-canvas);
}

.home-contact {
  border-block-end: 0;
}

.home-contact__grid {
  align-items: end;
}

.home-social {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-2);
  padding-block-start: var(--tm-space-7);
}

@media (min-width: 900px) {
  .home-visual__grid {
    grid-template-columns: minmax(0, 0.8fr) minmax(24rem, 1.2fr);
    align-items: center;
  }

  .home-about__grid {
    grid-template-columns: minmax(10rem, 0.4fr) minmax(0, 1fr);
  }

  .home-contact__grid {
    grid-template-columns: minmax(0, 1fr) auto;
  }
}
</style>
