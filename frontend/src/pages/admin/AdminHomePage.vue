<script setup>
import { computed, inject, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const { t } = useI18n()
const state = ref('loading')
const error = ref(null)
const summary = ref(null)

const metrics = computed(() => [
  { label: t('admin.dashboard.pages'), value: summary.value?.pages, icon: 'article', to: '/admin/pages' },
  { label: t('admin.dashboard.posts'), value: summary.value?.posts, icon: 'article', to: '/admin/blog/posts' },
  { label: t('admin.dashboard.media'), value: summary.value?.media, icon: 'perm_media', to: '/admin/media' },
  { label: t('admin.dashboard.contactMessages'), value: summary.value?.newContactMessages, icon: 'mail', to: '/admin/contact-messages' }
])

async function load() {
  state.value = 'loading'
  error.value = null

  try {
    const response = await httpClient.get('/api/v1/admin/analytics/summary')
    summary.value = response.data
    state.value = 'ready'
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
    state.value = 'error'
  }
}

onMounted(() => { void load() })
</script>

<template>
  <q-page class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1 class="text-h4 q-my-none">{{ t('admin.dashboard.title') }}</h1>
        <p class="admin-page__description">{{ t('admin.dashboard.description') }}</p>
      </div>
      <q-btn color="primary" no-caps icon="add" :label="t('admin.dashboard.createPage')" to="/admin/pages" />
    </header>

    <q-banner v-if="error" class="bg-red-1 text-negative" role="alert">
      {{ error.message }}
    </q-banner>
    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />
    <template v-else>
      <section class="admin-dashboard-grid" :aria-label="t('admin.dashboard.summary')">
        <q-card v-for="metric in metrics" :key="metric.label" flat class="admin-metric">
          <div class="row items-center justify-between no-wrap">
            <span class="admin-metric__label">{{ metric.label }}</span>
            <q-icon :name="metric.icon" size="24px" color="primary" aria-hidden="true" />
          </div>
          <strong class="admin-metric__value">{{ metric.value }}</strong>
          <q-btn flat no-caps align="left" color="primary" :to="metric.to" :label="t('admin.dashboard.manage')" />
        </q-card>
      </section>

      <section class="admin-dashboard-next" :aria-label="t('admin.dashboard.nextActions')">
        <q-card flat class="admin-panel">
          <q-card-section>
            <h2 class="text-h6 q-my-none">{{ t('admin.dashboard.continueEditing') }}</h2>
          </q-card-section>
          <q-list separator>
            <q-item clickable to="/admin/pages"><q-item-section>{{ t('admin.dashboard.managePages') }}</q-item-section><q-item-section side><q-icon name="chevron_right" /></q-item-section></q-item>
            <q-item clickable to="/admin/blog/posts"><q-item-section>{{ t('admin.dashboard.managePosts') }}</q-item-section><q-item-section side><q-icon name="chevron_right" /></q-item-section></q-item>
            <q-item clickable to="/admin/media"><q-item-section>{{ t('admin.dashboard.manageMedia') }}</q-item-section><q-item-section side><q-icon name="chevron_right" /></q-item-section></q-item>
          </q-list>
        </q-card>
        <q-card flat class="admin-panel">
          <q-card-section>
            <h2 class="text-h6 q-my-none">{{ t('admin.dashboard.translationWorkflow') }}</h2>
            <p class="text-body2 q-mt-sm q-mb-none">{{ t('admin.dashboard.translationWorkflowDescription') }}</p>
          </q-card-section>
        </q-card>
      </section>
    </template>
  </q-page>
</template>
