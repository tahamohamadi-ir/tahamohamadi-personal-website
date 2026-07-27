<script setup>
import { computed, inject, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import BlogPostList from 'src/components/public/BlogPostList.vue'
import CollectionPagination from 'src/components/public/CollectionPagination.vue'
import PageState from 'src/components/public/PageState.vue'
import { useAsyncPage } from 'src/composables/useAsyncPage'
import { usePublicSeoMeta } from 'src/composables/usePublicSeoMeta'
import { PUBLIC_API_KEY } from 'src/services/apiContext'

const props = defineProps({
  initialData: {
    type: Object,
    default: undefined
  }
})

const api = inject(PUBLIC_API_KEY)
const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const locale = computed(() => route.meta.locale)
const searchInput = ref(String(route.query.q ?? ''))
const page = computed(() => {
  const value = Number(route.query.page ?? 0)
  return Number.isInteger(value) && value >= 0 ? value : 0
})
const searchQuery = computed(() => String(route.query.q ?? '').trim())
const category = computed(() => String(route.query.category ?? '').trim())
const tag = computed(() => String(route.query.tag ?? '').trim())
const categories = ref([])
const tags = ref([])
const taxonomyLoading = ref(false)

const postParams = computed(() => ({
  page: page.value,
  size: 20,
  ...(searchQuery.value ? { q: searchQuery.value } : {}),
  ...(category.value ? { category: category.value } : {}),
  ...(tag.value ? { tag: tag.value } : {})
}))
const tagOptions = computed(() => tags.value
  .filter((item) => item?.slug && item?.name)
  .map((item) => ({ label: item.name, value: item.slug })))
const hasActiveFilters = computed(() => (
  Boolean(searchQuery.value || category.value || tag.value)
))

function isEmptyBlog(value) {
  return Array.isArray(value?.items) && value.items.length === 0
}

const {
  data,
  state,
  load,
  refresh
} = useAsyncPage({
  api,
  load: (currentApi) => currentApi.listPosts(locale.value, postParams.value),
  isEmpty: isEmptyBlog,
  initialData: props.initialData
})

const posts = computed(() => data.value?.items ?? [])
usePublicSeoMeta({ data, state })
const showsContent = computed(() => (
  data.value !== null && state.value !== 'empty'
))

function retry() {
  return state.value === 'stale' ? refresh() : load()
}

async function changePage(nextPage) {
  if (nextPage < 0 || nextPage >= (data.value?.totalPages ?? 0)) {
    return
  }

  const query = { ...route.query }
  if (nextPage === 0) {
    delete query.page
  }
  else {
    query.page = String(nextPage)
  }

  await router.push({ query })
}

async function updateFilters(nextFilters = {}) {
  const query = { ...route.query }
  const filters = {
    q: searchQuery.value,
    category: category.value,
    tag: tag.value,
    ...nextFilters
  }

  for (const [key, value] of Object.entries(filters)) {
    if (typeof value === 'string' && value.trim()) query[key] = value.trim()
    else delete query[key]
  }
  delete query.page
  await router.push({ query })
}

function applySearch() {
  return updateFilters({ q: searchInput.value })
}

function selectCategory(value) {
  return updateFilters({ category: value })
}

function selectTag(value) {
  return updateFilters({ tag: value })
}

function clearFilters() {
  searchInput.value = ''
  return updateFilters({ q: '', category: '', tag: '' })
}

async function loadTaxonomy() {
  if (typeof api?.listCategories !== 'function' || typeof api?.listTags !== 'function') return
  taxonomyLoading.value = true
  try {
    const [categoryResponse, tagResponse] = await Promise.all([
      api.listCategories(locale.value),
      api.listTags(locale.value)
    ])
    categories.value = categoryResponse?.items ?? []
    tags.value = tagResponse?.items ?? []
  }
  finally {
    taxonomyLoading.value = false
  }
}

watch(
  () => [route.query.page, route.query.q, route.query.category, route.query.tag],
  () => {
    searchInput.value = searchQuery.value
    void refresh()
  }
)

onMounted(() => {
  if (props.initialData === undefined) {
    void load()
  }
  void loadTaxonomy()
})
</script>

<template>
  <section class="collection-page tm-container">
    <h1 class="tm-page-title">{{ t('shell.navigation.blog') }}</h1>
    <form class="blog-discovery" :aria-label="t('collections.blog.discoveryLabel')" @submit.prevent="applySearch">
      <q-input
        v-model="searchInput"
        outlined
        clearable
        :label="t('collections.blog.searchLabel')"
        :disable="state === 'loading'"
      />
      <q-btn type="submit" color="primary" no-caps :disable="state === 'loading'" :label="t('collections.blog.searchAction')" />
    </form>
    <section v-if="categories.length || tagOptions.length || taxonomyLoading" class="blog-topics" :aria-label="t('collections.blog.topicsLabel')">
      <div v-if="categories.length" class="blog-topics__categories">
        <span class="blog-topics__label">{{ t('collections.blog.categoriesLabel') }}</span>
        <div class="blog-topics__choices" role="group" :aria-label="t('collections.blog.categoriesLabel')">
          <q-btn outline no-caps class="blog-topics__choice" :aria-pressed="!category" :label="t('collections.blog.allTopics')" @click="selectCategory('')" />
          <q-btn v-for="item in categories" :key="item.slug" outline no-caps class="blog-topics__choice" :aria-pressed="category === item.slug" :color="category === item.slug ? 'primary' : undefined" :label="item.name" @click="selectCategory(item.slug)" />
        </div>
      </div>
      <q-select
        v-if="tagOptions.length"
        :model-value="tag || null"
        outlined
        clearable
        emit-value
        map-options
        :options="tagOptions"
        :label="t('collections.blog.tagsLabel')"
        :disable="taxonomyLoading || state === 'loading'"
        @update:model-value="selectTag($event ?? '')"
      />
      <q-btn v-if="hasActiveFilters" flat no-caps class="blog-topics__reset" :label="t('collections.blog.clearFilters')" @click="clearFilters" />
    </section>
    <PageState
      v-if="state"
      :state="state"
      @retry="retry"
    />

    <div
      v-if="showsContent"
      class="collection-page__content"
    >
      <BlogPostList :posts="posts" />
      <CollectionPagination
        :page="data.page"
        :total-pages="data.totalPages"
        @change-page="changePage"
      />
    </div>
  </section>
</template>

<style scoped lang="scss">
.collection-page {
  display: grid;
  gap: var(--tm-space-8);
  padding-block: var(--tm-content-block-start) var(--tm-content-block-end);
}

.collection-page__content {
  display: grid;
  gap: var(--tm-space-8);
}

.blog-discovery { align-items: start; display: grid; gap: var(--tm-space-3); grid-template-columns: minmax(0, 1fr) auto; max-inline-size: 46rem; }
.blog-topics { border-block: 1px solid var(--tm-editorial-rule); display: grid; gap: var(--tm-space-4); padding-block: var(--tm-space-4); }
.blog-topics__categories { display: grid; gap: var(--tm-space-2); }
.blog-topics__label { color: var(--tm-text-secondary); font-size: 0.875rem; font-weight: 700; }
.blog-topics__choices { display: flex; flex-wrap: wrap; gap: var(--tm-space-2); }
.blog-topics__choice { min-block-size: var(--tm-control-min-size); }
.blog-topics__reset { justify-self: start; min-block-size: var(--tm-control-min-size); }

@media (max-width: 599px) {
  .blog-discovery { grid-template-columns: 1fr; }
}

@media (prefers-reduced-motion: reduce) {
  .blog-topics__choice { transition: none; }
}
</style>
