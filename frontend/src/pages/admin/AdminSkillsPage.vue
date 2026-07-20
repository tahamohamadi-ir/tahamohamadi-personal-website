<script setup>
import { computed, inject, nextTick, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'

import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import { createUnsavedChangesGuard, isVersionConflict, mapValidationErrors } from 'src/composables/adminContentInteractions'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const categories = ref([])
const skills = ref([])
const page = ref(0)
const totalPages = ref(0)
const state = ref('loading')
const error = ref(null)
const saving = ref(false)
const selectedLocale = ref('fa')
const replacing = ref(false)
const changes = createUnsavedChangesGuard(() => Promise.resolve(window.confirm('Discard unsaved skill changes?')))
const categoryForm = ref(createCategoryForm())
const skillForm = ref(createSkillForm())

function translation() { return { name: '', description: '' } }
function createCategoryForm(value = {}) { return { id: value.id ?? null, categoryKey: value.categoryKey ?? '', sortOrder: value.sortOrder ?? 0, active: value.active ?? true, version: value.version ?? null, fa: { ...translation(), ...(value.fa ?? {}) }, en: { ...translation(), ...(value.en ?? {}) } } }
function createSkillForm(value = {}) { return { id: value.id ?? null, categoryId: value.categoryId ?? null, skillKey: value.skillKey ?? '', sortOrder: value.sortOrder ?? 0, active: value.active ?? true, version: value.version ?? null, fa: { ...translation(), ...(value.fa ?? {}) }, en: { ...translation(), ...(value.en ?? {}) } } }
function replaceCategory(value = {}) { replacing.value = true; categoryForm.value = createCategoryForm(value); changes.markSaved(); nextTick(() => { replacing.value = false }) }
function replaceSkill(value = {}) { replacing.value = true; skillForm.value = createSkillForm(value); changes.markSaved(); nextTick(() => { replacing.value = false }) }

const categoryTranslations = computed(() => ({ fa: Boolean(categoryForm.value.fa.name), en: Boolean(categoryForm.value.en.name) }))
const skillTranslations = computed(() => ({ fa: Boolean(skillForm.value.fa.name), en: Boolean(skillForm.value.en.name) }))
const activeCategoryTranslation = computed(() => categoryForm.value[selectedLocale.value])
const activeSkillTranslation = computed(() => skillForm.value[selectedLocale.value])
const categoryOptions = computed(() => categories.value.filter((category) => category.active).map((category) => ({ label: `${category.categoryKey} · ${category[selectedLocale.value]?.name ?? ''}`, value: category.id })))
const fieldErrors = computed(() => mapValidationErrors(error.value))

watch([categoryForm, skillForm], () => { if (!replacing.value) changes.markDirty() }, { deep: true, flush: 'sync' })
onBeforeRouteLeave(async () => changes.confirmLeave())

async function load(requestedPage = page.value) {
  state.value = 'loading'; error.value = null
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
  catch (cause) { error.value = normalizeApiError(cause); state.value = 'error' }
}

async function selectCategory(item) { if (!(await changes.confirmLeave())) return; try { const response = await httpClient.get(`/api/v1/admin/skills/categories/${item.id}`); replaceCategory(response.data); selectedLocale.value = 'fa' } catch (cause) { error.value = normalizeApiError(cause) } }
async function selectSkill(item) { if (!(await changes.confirmLeave())) return; try { const response = await httpClient.get(`/api/v1/admin/skills/${item.id}`); replaceSkill(response.data); selectedLocale.value = 'fa' } catch (cause) { error.value = normalizeApiError(cause) } }
async function createCategory() { if (await changes.confirmLeave()) { replaceCategory(); error.value = null } }
async function createSkill() { if (await changes.confirmLeave()) { replaceSkill(); error.value = null } }

async function saveCategory() {
  saving.value = true; error.value = null
  try { await primeCsrfToken(httpClient); const response = categoryForm.value.id ? await httpClient.put(`/api/v1/admin/skills/categories/${categoryForm.value.id}`, categoryForm.value) : await httpClient.post('/api/v1/admin/skills/categories', categoryForm.value); replaceCategory(response.data); await load(page.value) }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}
async function saveSkill() {
  saving.value = true; error.value = null
  try { await primeCsrfToken(httpClient); const response = skillForm.value.id ? await httpClient.put(`/api/v1/admin/skills/${skillForm.value.id}`, skillForm.value) : await httpClient.post('/api/v1/admin/skills', skillForm.value); replaceSkill(response.data); await load(page.value) }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}
async function deactivate(resource, value) {
  saving.value = true; error.value = null
  try { await primeCsrfToken(httpClient); await httpClient.delete(`/api/v1/admin/skills${resource === 'category' ? '/categories' : ''}/${value.id}`, { params: { version: value.version } }); resource === 'category' ? replaceCategory({ ...value, active: false }) : replaceSkill({ ...value, active: false }); await load(page.value) }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}
onMounted(() => { void load() })
</script>

<template>
  <q-page class="q-pa-md q-pa-lg-md">
    <h1 class="text-h5 q-mt-none">Skills</h1><p class="text-body2 text-grey-8">Manage categories and skills with independent Persian and English labels.</p>
    <q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">{{ isVersionConflict(error) ? 'This item changed elsewhere. Reload it before saving.' : error.message }}</q-banner>
    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />
    <template v-else>
      <h2 class="text-h6">Categories</h2><q-list bordered separator><q-item v-for="item in categories" :key="item.id" clickable @click="selectCategory(item)"><q-item-section><q-item-label>{{ item.categoryKey }}</q-item-label><q-item-label caption>{{ item.fa?.name || 'Missing translation' }} · {{ item.en?.name || 'Missing translation' }}</q-item-label></q-item-section><q-item-section side><q-badge :label="item.active ? 'ACTIVE' : 'INACTIVE'" :color="item.active ? 'positive' : 'grey-7'" /></q-item-section></q-item></q-list>
      <h2 class="text-h6 q-mt-xl">Skills</h2><q-list bordered separator><q-item v-for="item in skills" :key="item.id" clickable @click="selectSkill(item)"><q-item-section><q-item-label>{{ item.skillKey }}</q-item-label><q-item-label caption>{{ item.fa?.name || 'Missing translation' }} · {{ item.en?.name || 'Missing translation' }}</q-item-label></q-item-section><q-item-section side><q-badge :label="item.active ? 'ACTIVE' : 'INACTIVE'" :color="item.active ? 'positive' : 'grey-7'" /></q-item-section></q-item></q-list>
      <AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" />
    </template>
    <q-form class="q-mt-xl q-gutter-md" @submit.prevent="saveCategory"><div class="row items-center justify-between"><h2 class="text-h6 q-my-none">{{ categoryForm.id ? 'Edit category' : 'Create category' }}</h2><q-btn flat label="New category" @click="createCategory" /></div><q-input v-model="categoryForm.categoryKey" label="Category key" :disable="saving" :error="Boolean(fieldErrors.categoryKey)" :error-message="fieldErrors.categoryKey" /><q-input v-model.number="categoryForm.sortOrder" type="number" min="0" label="Sort order" :disable="saving" /><AdminLocaleTabs v-model="selectedLocale" :translations="categoryTranslations" /><q-input v-model="activeCategoryTranslation.name" label="Name" :disable="saving" /><q-input v-model="activeCategoryTranslation.description" type="textarea" label="Description" :disable="saving" /><div class="row q-gutter-sm"><q-btn type="submit" color="primary" :loading="saving" label="Save category" /><q-btn v-if="categoryForm.id && categoryForm.active" color="negative" flat :disable="saving" label="Deactivate" @click="deactivate('category', categoryForm)" /><q-btn tag="a" flat :href="`/${selectedLocale}/skills`" target="_blank" rel="noopener noreferrer" label="Preview public skills" /></div></q-form>
    <q-form class="q-mt-xl q-gutter-md" @submit.prevent="saveSkill"><div class="row items-center justify-between"><h2 class="text-h6 q-my-none">{{ skillForm.id ? 'Edit skill' : 'Create skill' }}</h2><q-btn flat label="New skill" @click="createSkill" /></div><q-select v-model="skillForm.categoryId" :options="categoryOptions" option-label="label" option-value="value" emit-value map-options label="Category" :disable="saving" :error="Boolean(fieldErrors.categoryId)" :error-message="fieldErrors.categoryId" /><q-input v-model="skillForm.skillKey" label="Skill key" :disable="saving" :error="Boolean(fieldErrors.skillKey)" :error-message="fieldErrors.skillKey" /><q-input v-model.number="skillForm.sortOrder" type="number" min="0" label="Sort order" :disable="saving" /><AdminLocaleTabs v-model="selectedLocale" :translations="skillTranslations" /><q-input v-model="activeSkillTranslation.name" label="Name" :disable="saving" /><q-input v-model="activeSkillTranslation.description" type="textarea" label="Description" :disable="saving" /><div class="row q-gutter-sm"><q-btn type="submit" color="primary" :loading="saving" label="Save skill" /><q-btn v-if="skillForm.id && skillForm.active" color="negative" flat :disable="saving" label="Deactivate" @click="deactivate('skill', skillForm)" /><q-btn tag="a" flat :href="`/${selectedLocale}/skills`" target="_blank" rel="noopener noreferrer" label="Preview public skills" /></div></q-form>
  </q-page>
</template>
