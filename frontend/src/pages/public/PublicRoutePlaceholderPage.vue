<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const locale = computed(() =>
  String(route.meta.locale ?? 'en')
)

const direction = computed(() =>
  String(route.meta.direction ?? 'ltr')
)

const pageKey = computed(() =>
  String(route.meta.pageKey ?? 'public-page')
)
</script>

<template>
  <main
    class="q-pa-lg"
    :lang="locale"
    :dir="direction"
  >
    <h1>{{ pageKey }}</h1>

    <p>
      This temporary route shell will be replaced by
      CMS-backed public content.
    </p>
  </main>
</template>