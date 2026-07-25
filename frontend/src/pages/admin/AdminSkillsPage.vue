<script setup>
import { computed, inject, nextTick, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { useI18n } from 'vue-i18n'

import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import {
  createUnsavedChangesGuard,
  isVersionConflict,
  mapValidationErrors
} from 'src/composables/adminContentInteractions'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const { t } = useI18n()
const categories = ref([])
const skills = ref([])
const page = ref(0)
const totalPages = ref(0)
const state = ref('loading')
const error = ref(null)
const saving = ref(false)
const selectedLocale = ref('fa')
const replacing = ref(false)
const deactivationConfirmationOpen = ref(false)
const pendingDeactivation = ref(null)
const changes = createUnsavedChangesGuard(() => Promise.resolve(
  window.confirm(t('admin.skills.discard'))
))
const categoryForm = ref(createCategoryForm())
const skillForm = ref(createSkillForm())

function translation() {
  return { name: '', description: '' }
}

function createCategoryForm(value = {}) {
  return {
    id: value.id ?? null,
    categoryKey: value.categoryKey ?? '',
    sortOrder: value.sortOrder ?? 0,
    active: value.active ?? true,
    version: value.version ?? null,
    fa: { ...translation(), ...(value.fa ?? {}) },
    en: { ...translation(), ...(value.en ?? {}) }
  }
}

function createSkillForm(value = {}) {
  return {
    id: value.id ?? null,
    categoryId: value.categoryId ?? null,
    skillKey: value.skillKey ?? '',
    sortOrder: value.sortOrder ?? 0,
    active: value.active ?? true,
    version: value.version ?? null,
    fa: { ...translation(), ...(value.fa ?? {}) },
    en: { ...translation(), ...(value.en ?? {}) }
  }
}

function replaceCategory(value = {}) {
  replacing.value = true
  categoryForm.value = createCategoryForm(value)
  changes.markSaved()
  nextTick(() => { replacing.value = false })
}

function replaceSkill(value = {}) {
  replacing.value = true
  skillForm.value = createSkillForm(value)
  changes.markSaved()
  nextTick(() => { replacing.value = false })
}

const categoryTranslations = computed(() => ({
  fa: Boolean(categoryForm.value.fa.name),
  en: Boolean(categoryForm.value.en.name)
}))
const skillTranslations = computed(() => ({
  fa: Boolean(skillForm.value.fa.name),
  en: Boolean(skillForm.value.en.name)
}))
const activeCategoryTranslation = computed(() => categoryForm.value[selectedLocale.value])
const activeSkillTranslation = computed(() => skillForm.value[selectedLocale.value])
const categoryOptions = computed(() => categories.value
  .filter((category) => category.active)
  .map((category) => ({
    label: `${category.categoryKey} · ${category[selectedLocale.value]?.name ?? ''}`,
    value: category.id
  })))
const fieldErrors = computed(() => mapValidationErrors(error.value))

watch([categoryForm, skillForm], () => {
  if (!replacing.value) changes.markDirty()
}, { deep: true, flush: 'sync' })
onBeforeRouteLeave(async () => changes.confirmLeave())

async function load(requestedPage = page.value) {
  state.value = 'loading'
  error.value = null
  try {
    const [categoryResponse, skillResponse] = await Promise.all([
      httpClient.get('/api/v1/admin/skills/categories', { params: { page: 0, size: 100, sort: 'sortOrder,asc' } }),
      httpClient.get('/api/v1/admin/skills', { params: { page: requestedPage, size: 20, sort: 'sortOrder,asc' } })
    ])
    categories.value = categoryResponse.data.items ?? []
    skills.value = skillResponse.data.items ?? []
    page.value = skillResponse.data.page ?? requestedPage
    totalPages.value = skillResponse.data.totalPages ?? 0
    state.value = categories.value.length === 0 && skills.value.length === 0 ? 'empty' : 'ready'
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
    state.value = 'error'
  }
}

async function selectCategory(item) {
  if (!(await changes.confirmLeave())) return
  try {
    const response = await httpClient.get(`/api/v1/admin/skills/categories/${item.id}`)
    replaceCategory(response.data)
    selectedLocale.value = 'fa'
  }
  catch (cause) { error.value = normalizeApiError(cause) }
}

async function selectSkill(item) {
  if (!(await changes.confirmLeave())) return
  try {
    const response = await httpClient.get(`/api/v1/admin/skills/${item.id}`)
    replaceSkill(response.data)
    selectedLocale.value = 'fa'
  }
  catch (cause) { error.value = normalizeApiError(cause) }
}

async function createCategory() {
  if (await changes.confirmLeave()) {
    replaceCategory()
    error.value = null
  }
}

async function createSkill() {
  if (await changes.confirmLeave()) {
    replaceSkill()
    error.value = null
  }
}

async function saveCategory() {
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = categoryForm.value.id
      ? await httpClient.put(`/api/v1/admin/skills/categories/${categoryForm.value.id}`, categoryForm.value)
      : await httpClient.post('/api/v1/admin/skills/categories', categoryForm.value)
    replaceCategory(response.data)
    await load(page.value)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}

async function saveSkill() {
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = skillForm.value.id
      ? await httpClient.put(`/api/v1/admin/skills/${skillForm.value.id}`, skillForm.value)
      : await httpClient.post('/api/v1/admin/skills', skillForm.value)
    replaceSkill(response.data)
    await load(page.value)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}

function requestDeactivation(resource, value) {
  pendingDeactivation.value = { resource, value }
  deactivationConfirmationOpen.value = true
}

async function confirmDeactivation() {
  const pending = pendingDeactivation.value
  deactivationConfirmationOpen.value = false
  pendingDeactivation.value = null
  if (!pending) return
  await deactivate(pending.resource, pending.value)
}

async function deactivate(resource, value) {
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    await httpClient.delete(`/api/v1/admin/skills${resource === 'category' ? '/categories' : ''}/${value.id}`, {
      params: { version: value.version }
    })
    if (resource === 'category') replaceCategory({ ...value, active: false })
    else replaceSkill({ ...value, active: false })
    await load(page.value)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}

onMounted(() => { void load() })
</script>

<template>
  <q-page class="q-pa-md q-pa-lg-md">
    <header class="admin-page__header q-mb-lg">
      <h1 class="text-h5 q-my-none">{{ t('admin.skills.title') }}</h1>
      <p class="text-body2 text-grey-8 q-mb-none">{{ t('admin.skills.description') }}</p>
    </header>

    <q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">
      {{ isVersionConflict(error) ? t('admin.skills.conflict') : error.message }}
    </q-banner>
    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />

    <template v-else>
      <section :aria-labelledby="'skill-categories-heading'">
        <h2 id="skill-categories-heading" class="text-h6">{{ t('admin.skills.categories') }}</h2>
        <q-list bordered separator>
          <q-item v-for="item in categories" :key="item.id" clickable @click="selectCategory(item)">
            <q-item-section>
              <q-item-label>{{ item.categoryKey }}</q-item-label>
              <q-item-label caption>{{ item.fa?.name || t('admin.localeTabs.missing') }} · {{ item.en?.name || t('admin.localeTabs.missing') }}</q-item-label>
            </q-item-section>
            <q-item-section side>
              <q-badge :label="item.active ? t('admin.skills.active') : t('admin.skills.inactive')" :color="item.active ? 'positive' : 'grey-7'" />
            </q-item-section>
          </q-item>
        </q-list>
      </section>

      <section :aria-labelledby="'skills-heading'" class="q-mt-xl">
        <h2 id="skills-heading" class="text-h6">{{ t('admin.skills.skills') }}</h2>
        <q-list bordered separator>
          <q-item v-for="item in skills" :key="item.id" clickable @click="selectSkill(item)">
            <q-item-section>
              <q-item-label>{{ item.skillKey }}</q-item-label>
              <q-item-label caption>{{ item.fa?.name || t('admin.localeTabs.missing') }} · {{ item.en?.name || t('admin.localeTabs.missing') }}</q-item-label>
            </q-item-section>
            <q-item-section side>
              <q-badge :label="item.active ? t('admin.skills.active') : t('admin.skills.inactive')" :color="item.active ? 'positive' : 'grey-7'" />
            </q-item-section>
          </q-item>
        </q-list>
      </section>
      <AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" />
    </template>

    <q-form class="q-mt-xl q-gutter-md" @submit.prevent="saveCategory">
      <div class="row items-center justify-between">
        <h2 class="text-h6 q-my-none">{{ categoryForm.id ? t('admin.skills.editCategory') : t('admin.skills.createCategory') }}</h2>
        <q-btn flat :label="t('admin.skills.newCategory')" @click="createCategory" />
      </div>
      <q-input v-model="categoryForm.categoryKey" :label="t('admin.skills.categoryKey')" :disable="saving" :error="Boolean(fieldErrors.categoryKey)" :error-message="fieldErrors.categoryKey" />
      <q-input v-model.number="categoryForm.sortOrder" type="number" min="0" :label="t('admin.skills.sortOrder')" :disable="saving" />
      <AdminLocaleTabs v-model="selectedLocale" :translations="categoryTranslations" />
      <q-input v-model="activeCategoryTranslation.name" :label="t('admin.skills.name')" :disable="saving" />
      <q-input v-model="activeCategoryTranslation.description" type="textarea" :label="t('admin.skills.itemDescription')" :disable="saving" />
      <div class="row q-gutter-sm">
        <q-btn type="submit" color="primary" :loading="saving" :label="t('admin.skills.saveCategory')" />
        <q-btn v-if="categoryForm.id && categoryForm.active" color="negative" flat :disable="saving" :label="t('admin.skills.deactivate')" @click="requestDeactivation('category', categoryForm)" />
        <q-btn tag="a" flat :href="`/${selectedLocale}/skills`" target="_blank" rel="noopener noreferrer" :label="t('admin.skills.preview')" />
      </div>
    </q-form>

    <q-form class="q-mt-xl q-gutter-md" @submit.prevent="saveSkill">
      <div class="row items-center justify-between">
        <h2 class="text-h6 q-my-none">{{ skillForm.id ? t('admin.skills.editSkill') : t('admin.skills.createSkill') }}</h2>
        <q-btn flat :label="t('admin.skills.newSkill')" @click="createSkill" />
      </div>
      <q-select v-model="skillForm.categoryId" :options="categoryOptions" option-label="label" option-value="value" emit-value map-options :label="t('admin.skills.category')" :disable="saving" :error="Boolean(fieldErrors.categoryId)" :error-message="fieldErrors.categoryId" />
      <q-input v-model="skillForm.skillKey" :label="t('admin.skills.skillKey')" :disable="saving" :error="Boolean(fieldErrors.skillKey)" :error-message="fieldErrors.skillKey" />
      <q-input v-model.number="skillForm.sortOrder" type="number" min="0" :label="t('admin.skills.sortOrder')" :disable="saving" />
      <AdminLocaleTabs v-model="selectedLocale" :translations="skillTranslations" />
      <q-input v-model="activeSkillTranslation.name" :label="t('admin.skills.name')" :disable="saving" />
      <q-input v-model="activeSkillTranslation.description" type="textarea" :label="t('admin.skills.itemDescription')" :disable="saving" />
      <div class="row q-gutter-sm">
        <q-btn type="submit" color="primary" :loading="saving" :label="t('admin.skills.saveSkill')" />
        <q-btn v-if="skillForm.id && skillForm.active" color="negative" flat :disable="saving" :label="t('admin.skills.deactivate')" @click="requestDeactivation('skill', skillForm)" />
        <q-btn tag="a" flat :href="`/${selectedLocale}/skills`" target="_blank" rel="noopener noreferrer" :label="t('admin.skills.preview')" />
      </div>
    </q-form>

    <q-dialog v-model="deactivationConfirmationOpen">
      <q-card class="admin-confirmation-dialog">
        <q-card-section>
          <h2 class="text-h6 q-my-none">{{ t('admin.skills.deactivateTitle') }}</h2>
        </q-card-section>
        <q-card-section class="q-pt-none">
          {{ t('admin.skills.deactivateDescription') }}
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat :label="t('admin.actions.cancel')" @click="deactivationConfirmationOpen = false" />
          <q-btn color="negative" :loading="saving" :label="t('admin.skills.deactivate')" @click="confirmDeactivation" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>
