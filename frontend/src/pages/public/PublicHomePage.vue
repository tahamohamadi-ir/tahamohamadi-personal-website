<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <section class="tm-editorial-page tm-container" aria-labelledby="home-title">
    <div class="tm-editorial-page__content">
      <h1 id="home-title" class="tm-page-title">{{ t('public.placeholder') }}</h1>
      <p class="tm-page-copy">{{ t('public.homeDescription') }}</p>
    </div>
  </section>
</template>
