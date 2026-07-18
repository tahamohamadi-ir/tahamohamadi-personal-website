<script setup>
import { computed, inject, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import CollectionPagination from 'src/components/public/CollectionPagination.vue'
import PageState from 'src/components/public/PageState.vue'
import PortfolioProjectList from 'src/components/public/PortfolioProjectList.vue'
import { useAsyncPage } from 'src/composables/useAsyncPage'
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
const page = computed(() => {
  const value = Number(route.query.page ?? 0)
  return Number.isInteger(value) && value >= 0 ? value : 0
})

function isEmptyPortfolio(value) {
  return Array.isArray(value?.items) && value.items.length === 0
}

const {
  data,
  state,
  load,
  refresh
} = useAsyncPage({
  api,
  load: (currentApi) => currentApi.listPortfolio(locale.value, {
    page: page.value,
    size: 20
  }),
  isEmpty: isEmptyPortfolio,
  initialData: props.initialData
})

const projects = computed(() => data.value?.items ?? [])
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

watch(
  () => route.query.page,
  () => {
    void refresh()
  }
)

onMounted(() => {
  if (props.initialData === undefined) {
    void load()
  }
})
</script>

<template>
  <section class="collection-page tm-container">
    <h1 class="tm-page-title">{{ t('shell.navigation.portfolio') }}</h1>
    <PageState
      v-if="state"
      :state="state"
      @retry="retry"
    />

    <div
      v-if="showsContent"
      class="collection-page__content"
    >
      <PortfolioProjectList :projects="projects" />
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
</style>
