<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

const route = useRoute()
const { t } = useI18n()

const pageKey = computed(() => String(route.meta.pageKey ?? ''))
const navigationKeys = new Set([
  'about', 'research', 'skills', 'resume', 'blog', 'portfolio', 'publications', 'contact'
])

const pageTitle = computed(() => (
  navigationKeys.has(pageKey.value)
    ? t(`shell.navigation.${pageKey.value}`)
    : t('public.temporaryPage')
))
</script>

<template>
  <section class="tm-editorial-page tm-container" aria-labelledby="placeholder-title">
    <div class="tm-editorial-page__content">
      <h1 id="placeholder-title" class="tm-page-title">{{ pageTitle }}</h1>
      <p class="tm-page-copy">{{ t('public.temporaryDescription') }}</p>
    </div>
  </section>
</template>
