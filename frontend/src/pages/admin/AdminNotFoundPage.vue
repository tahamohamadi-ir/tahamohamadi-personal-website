<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <q-page class="q-pa-lg">
    <section class="admin-not-found" role="alert">
      <h1 class="text-h5 q-mt-none">{{ t('admin.notFound.title') }}</h1>
      <p>{{ t('admin.notFound.description') }}</p>
      <q-btn color="primary" :label="t('admin.notFound.backToDashboard')" to="/admin" />
    </section>
  </q-page>
</template>
