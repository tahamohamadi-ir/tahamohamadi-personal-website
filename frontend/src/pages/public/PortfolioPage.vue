<script setup>
import { computed, inject, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import CollectionPagination from 'src/components/public/CollectionPagination.vue'
import PageState from 'src/components/public/PageState.vue'
import PortfolioProjectList from 'src/components/public/PortfolioProjectList.vue'
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
const page = computed(() => {
  const value = Number(route.query.page ?? 0)
  return Number.isInteger(value) && value >= 0 ? value : 0
})
const selectedSkill = computed(() => (
  typeof route.query.skill === 'string' && route.query.skill.trim()
    ? route.query.skill
    : null
))

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
    ...(selectedSkill.value ? { skill: selectedSkill.value } : {}),
    page: page.value,
    size: 20
  }),
  isEmpty: isEmptyPortfolio,
  initialData: props.initialData
})

const {
  data: skillsData,
  load: loadSkills
} = useAsyncPage({
  api,
  load: (currentApi) => currentApi.getSkills(locale.value),
  initialData: undefined
})

const projects = computed(() => data.value?.items ?? [])
const skills = computed(() => skillsData.value?.items ?? [])
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

async function selectSkill(skillKey) {
  const query = { ...route.query }
  delete query.page

  if (!skillKey || skillKey === selectedSkill.value) {
    delete query.skill
  }
  else {
    query.skill = skillKey
  }

  await router.push({ query })
}

watch(
  () => [route.query.page, route.query.skill],
  () => {
    void refresh()
  }
)

onMounted(() => {
  if (props.initialData === undefined) {
    void load()
  }
  void loadSkills()
})
</script>

<template>
  <section class="collection-page tm-container">
    <h1 class="tm-page-title">{{ t('shell.navigation.portfolio') }}</h1>

    <nav
      v-if="skills.length || selectedSkill"
      class="portfolio-filter"
      :aria-label="t('collections.portfolio.filterLabel')"
    >
      <button
        class="portfolio-filter__control tm-interactive"
        :aria-pressed="!selectedSkill"
        :data-active="!selectedSkill"
        type="button"
        @click="selectSkill(null)"
      >
        {{ t('collections.portfolio.all') }}
      </button>
      <button
        v-for="skill in skills"
        :key="skill.key"
        class="portfolio-filter__control tm-interactive"
        :aria-pressed="selectedSkill === skill.key"
        :data-active="selectedSkill === skill.key"
        :data-skill-filter="skill.key"
        type="button"
        @click="selectSkill(skill.key)"
      >
        {{ skill.name }}
      </button>
    </nav>

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

.portfolio-filter {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-2);
  max-inline-size: 76rem;
  padding-block: var(--tm-space-4);
  border-block: 1px solid var(--tm-editorial-rule);
}

.portfolio-filter__control {
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-3);
  border: 1px solid var(--tm-editorial-card-border);
  border-radius: var(--tm-radius-control);
  background: var(--tm-editorial-card-surface);
  color: var(--tm-text-primary);
  cursor: pointer;
  font: inherit;
  font-weight: 700;
  transition: background-color 180ms ease, border-color 180ms ease, color 180ms ease;
}

.portfolio-filter__control[data-active='true'] {
  border-color: var(--tm-action-primary);
  background: var(--tm-interactive-surface-hover);
  color: var(--tm-text-primary);
}

.portfolio-filter__control:hover:not([data-active='true']) {
  border-color: var(--tm-link);
  color: var(--tm-link);
}

.portfolio-filter__control:focus-visible {
  outline: 3px solid var(--tm-focus-ring);
  outline-offset: 3px;
}

@media (prefers-reduced-motion: reduce) {
  .portfolio-filter__control { transition: none; }
}
</style>
