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

const heading = computed(() =>
  locale.value === 'fa'
    ? '\u062a\u0631\u062c\u0645\u0647\u0020\u062f\u0631\u0020\u062f\u0633\u062a\u0631\u0633\u0020\u0646\u06cc\u0633\u062a'
    : 'Translation unavailable'
)

const description = computed(() =>
  locale.value === 'fa'
    ? '\u0646\u0633\u062e\u0647\u0020\u0627\u06cc\u0646\u0020\u0632\u0628\u0627\u0646\u0020\u0628\u0631\u0627\u06cc\u0020\u0627\u06cc\u0646\u0020\u0645\u062d\u062a\u0648\u0627\u0020\u0645\u0648\u062c\u0648\u062f\u0020\u0646\u06cc\u0633\u062a\u002e'
    : 'This content is not available in the requested language.'
)
</script>

<template>
  <main
    class="q-pa-lg"
    :lang="locale"
    :dir="direction"
  >
    <h1>{{ heading }}</h1>

    <p>{{ description }}</p>
  </main>
</template>