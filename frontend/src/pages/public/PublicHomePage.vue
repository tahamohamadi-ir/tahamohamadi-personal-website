<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <q-page class="q-pa-lg">
    <h1 class="text-h5 q-mt-none">{{ t('public.placeholder') }}</h1>
  </q-page>
</template>
