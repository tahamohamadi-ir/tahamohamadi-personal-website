<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <section
    class="tm-editorial-page tm-editorial-page--introduction tm-container"
    aria-labelledby="home-title"
  >
    <header class="tm-editorial-page__content">
      <h1 id="home-title" class="tm-page-title">{{ t('shell.siteName') }}</h1>
      <p class="tm-page-copy">{{ t('public.home.lead') }}</p>
    </header>
  </section>
</template>
