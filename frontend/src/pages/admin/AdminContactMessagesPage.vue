<script setup>
import { computed, inject, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const { t } = useI18n()
const statuses = ['NEW', 'READ', 'ARCHIVED']
const status = ref('NEW')
const items = ref([])
const selected = ref(null)
const page = ref(0)
const totalPages = ref(0)
const state = ref('loading')
const error = ref(null)
const updating = ref(false)
const archiveConfirmationOpen = ref(false)
const detailDirection = computed(() => selected.value?.sourceLanguage === 'FA' ? 'rtl' : 'ltr')

async function load(requestedPage = 0) {
  state.value = 'loading'
  error.value = null
  try {
    const response = await httpClient.get('/api/v1/admin/contact-messages', {
      params: { status: status.value, page: requestedPage, size: 20 }
    })
    items.value = response.data.items ?? []
    page.value = response.data.page ?? requestedPage
    totalPages.value = response.data.totalPages ?? 0
    state.value = items.value.length === 0 ? 'empty' : 'ready'
  }
  catch (cause) { error.value = normalizeApiError(cause); state.value = 'error' }
}

async function select(item) {
  error.value = null
  try {
    const response = await httpClient.get(`/api/v1/admin/contact-messages/${item.id}`)
    selected.value = response.data
  }
  catch (cause) { error.value = normalizeApiError(cause) }
}

async function transition(action) {
  if (!selected.value) return
  updating.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    await httpClient.post(`/api/v1/admin/contact-messages/${selected.value.id}/${action}`)
    selected.value = null
    await load(page.value)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { updating.value = false }
}

function requestArchive() { archiveConfirmationOpen.value = true }
function confirmArchive() {
  archiveConfirmationOpen.value = false
  void transition('archive')
}

function changeStatus(nextStatus) {
  status.value = nextStatus
  selected.value = null
  void load(0)
}

onMounted(() => { void load() })
</script>

<template>
  <q-page class="admin-page">
    <header class="admin-page__header">
      <div><h1 class="text-h4 q-my-none">{{ t('admin.contact.title') }}</h1><p class="admin-page__description">{{ t('admin.contact.description') }}</p></div>
    </header>
    <q-banner v-if="error" class="bg-red-1 text-negative" role="alert">{{ error.message }}</q-banner>
    <q-tabs :model-value="status" dense align="left" active-color="primary" indicator-color="primary" @update:model-value="changeStatus">
      <q-tab v-for="itemStatus in statuses" :key="itemStatus" :name="itemStatus" :label="itemStatus" />
    </q-tabs>
    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />
    <template v-else>
      <q-list bordered separator>
        <q-item v-for="item in items" :key="item.id" clickable @click="select(item)">
          <q-item-section><q-item-label>{{ item.senderName }}</q-item-label><q-item-label caption>{{ item.senderEmail }} · {{ new Date(item.submittedAt).toLocaleString() }}</q-item-label></q-item-section>
          <q-item-section side><q-badge :label="item.status" :color="item.status === 'NEW' ? 'warning' : 'grey-7'" /></q-item-section>
        </q-item>
      </q-list>
      <AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" />
    </template>
    <section v-if="selected" class="admin-panel q-pa-md" :aria-label="t('admin.contact.selected')">
      <div class="admin-page__header"><div><h2 class="text-h5 q-my-none">{{ selected.senderName }}</h2><p class="admin-page__description">{{ selected.senderEmail }} · {{ new Date(selected.submittedAt).toLocaleString() }}</p></div><q-badge :label="selected.status" /></div>
      <p class="q-mt-lg text-body1" :dir="detailDirection">{{ selected.message }}</p>
      <div class="admin-form-actions q-mt-lg"><q-btn v-if="selected.status === 'NEW'" outline no-caps :label="t('admin.contact.markRead')" :loading="updating" @click="transition('read')" /><q-btn v-if="selected.status !== 'ARCHIVED'" outline color="negative" no-caps :label="t('admin.contact.archive')" :loading="updating" @click="requestArchive" /></div>
    </section>
    <q-dialog v-model="archiveConfirmationOpen" persistent>
      <q-card>
        <q-card-section class="text-h6">{{ t('admin.contact.archiveTitle') }}</q-card-section>
        <q-card-section>{{ t('admin.contact.archiveDescription') }}</q-card-section>
        <q-card-actions align="right">
          <q-btn flat :label="t('admin.unsaved.cancel')" @click="archiveConfirmationOpen = false" />
          <q-btn color="negative" :label="t('admin.contact.archive')" :loading="updating" @click="confirmArchive" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>
