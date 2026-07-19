<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <section
    class="tm-editorial-page tm-editorial-page--introduction tm-container"
    aria-labelledby="placeholder-title"
  >
    <header class="tm-editorial-page__content">
      <h1 id="placeholder-title" class="tm-page-title">
        {{ t('public.temporaryPage') }}
      </h1>
      <p class="tm-page-copy">{{ t('public.pageIntroduction.unavailable') }}</p>
    </header>
  </section>
</template>
