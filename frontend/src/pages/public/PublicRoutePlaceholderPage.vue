<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

const route = useRoute()
const { t } = useI18n()

const pageKey = computed(() => String(route.meta.pageKey ?? ''))
const PAGE_INTRODUCTION_KEYS = {
  about: {
    title: 'shell.navigation.about',
    message: 'public.pageIntroduction.about.pending'
  },
  research: {
    title: 'shell.navigation.research',
    message: 'public.pageIntroduction.research.pending'
  },
  skills: {
    title: 'shell.navigation.skills',
    message: 'public.pageIntroduction.skills.pending'
  },
  contact: {
    title: 'shell.navigation.contact',
    message: 'public.pageIntroduction.contact.pending'
  }
}

const pageIntroduction = computed(() => PAGE_INTRODUCTION_KEYS[pageKey.value])

const pageTitle = computed(() => (
  pageIntroduction.value
    ? t(pageIntroduction.value.title)
    : t('public.temporaryPage')
))

const pageMessage = computed(() => (
  pageIntroduction.value
    ? t(pageIntroduction.value.message)
    : t('public.pageIntroduction.unavailable')
))
</script>

<template>
  <section
    class="tm-editorial-page tm-editorial-page--introduction tm-container"
    aria-labelledby="placeholder-title"
  >
    <header class="tm-editorial-page__content">
      <h1 id="placeholder-title" class="tm-page-title">{{ pageTitle }}</h1>
      <p class="tm-page-copy">{{ pageMessage }}</p>
    </header>
  </section>
</template>
