<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <main class="language-page" aria-labelledby="language-title">
    <section class="language-page__content">
      <p class="language-page__identity">{{ t('shell.siteName') }}</p>
      <h1 id="language-title" class="tm-page-title">{{ t('language.title') }}</h1>
      <p class="tm-page-copy">{{ t('language.introduction') }}</p>
      <div class="language-page__choices" aria-label="Language selection">
        <router-link class="language-page__choice tm-interactive" to="/fa" lang="fa" dir="rtl">
          {{ t('language.fa') }}
        </router-link>
        <router-link class="language-page__choice tm-interactive" to="/en" lang="en" dir="ltr">
          {{ t('language.en') }}
        </router-link>
      </div>
    </section>
  </main>
</template>

<style scoped lang="scss">
.language-page {
  display: grid;
  min-block-size: 100dvh;
  padding: var(--tm-space-6) var(--tm-gutter);
  background: var(--tm-canvas);
  place-items: center;
}

.language-page__content {
  inline-size: min(100%, 560px);
  padding-block: var(--tm-space-12);
}

.language-page__identity {
  margin: 0 0 var(--tm-space-6);
  color: var(--tm-action-primary);
  font-size: 1rem;
  font-weight: 700;
}

.language-page__choices {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--tm-space-3);
  margin-block-start: var(--tm-space-8);
}

.language-page__choice {
  display: flex;
  align-items: center;
  justify-content: center;
  min-block-size: 56px;
  padding-inline: var(--tm-space-4);
  border: 1px solid var(--tm-border-subtle);
  border-radius: var(--tm-radius-control);
  color: var(--tm-text-primary);
  font-size: 1.0625rem;
  font-weight: 700;
  text-decoration: none;
}

.language-page__choice:hover {
  border-color: var(--tm-action-primary);
  color: var(--tm-action-primary);
}

@media (max-width: 374px) {
  .language-page__choices {
    grid-template-columns: 1fr;
  }
}
</style>
