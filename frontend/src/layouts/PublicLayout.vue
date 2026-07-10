<script setup>
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

const route = useRoute()
const { locale } = useI18n()

const language = computed(() => route.meta.locale || 'en')
const direction = computed(() => route.meta.direction || 'ltr')

watch(
  language,
  (value) => {
    locale.value = value
  },
  { immediate: true }
)

watch(
  direction,
  (value) => {
    if (typeof document !== 'undefined') {
      document.documentElement.dir = value
      document.documentElement.lang = language.value
    }
  },
  { immediate: true }
)
</script>

<template>
  <q-layout view="hHh lpR fFf">
    <q-page-container>
      <main :dir="direction" :lang="language">
        <router-view />
      </main>
    </q-page-container>
  </q-layout>
</template>
