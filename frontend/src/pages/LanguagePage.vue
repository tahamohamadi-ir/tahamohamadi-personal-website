<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()
</script>

<template>
  <q-page class="column flex-center q-gutter-md q-pa-lg">
    <h1 class="text-h5 q-ma-none">{{ t('language.title') }}</h1>
    <div class="row q-gutter-sm">
      <q-btn color="primary" :label="t('language.fa')" @click="router.push('/fa')" />
      <q-btn color="secondary" :label="t('language.en')" @click="router.push('/en')" />
    </div>
  </q-page>
</template>
