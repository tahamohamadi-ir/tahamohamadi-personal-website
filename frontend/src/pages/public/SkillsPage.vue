<script setup>
import { computed, inject, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import PageState from 'src/components/public/PageState.vue'
import { useAsyncPage } from 'src/composables/useAsyncPage'
import { usePublicSeoMeta } from 'src/composables/usePublicSeoMeta'
import { PUBLIC_API_KEY } from 'src/services/apiContext'

defineOptions({ name: 'SkillsPage' })

const props = defineProps({
  initialData: {
    type: Object,
    default: undefined
  }
})

const api = inject(PUBLIC_API_KEY)
const route = useRoute()
const { t } = useI18n()
const locale = computed(() => route.meta.locale)
const ssrKey = computed(() => `public:${locale.value}:skills`)

function isEmptySkills(value) {
  return Array.isArray(value?.items) && value.items.length === 0
}

const {
  data,
  state,
  load,
  refresh,
  hasInitialState
} = useAsyncPage({
  api,
  load: (currentApi) => currentApi.getSkills(locale.value),
  isEmpty: isEmptySkills,
  initialData: props.initialData,
  ssrKey: () => ssrKey.value
})

const skills = computed(() => data.value?.items ?? [])
usePublicSeoMeta({ data, state })
const showsContent = computed(() => (
  data.value !== null && state.value !== 'empty'
))

function retry() {
  return state.value === 'stale' ? refresh() : load()
}

onMounted(() => {
  if (!hasInitialState) {
    void load()
  }
})
</script>

<template>
  <section class="skills-page tm-container" aria-labelledby="skills-title">
    <h1 id="skills-title" class="tm-page-title">
      {{ t('shell.navigation.skills') }}
    </h1>

    <PageState
      v-if="state"
      :state="state"
      @retry="retry"
    />

    <ul v-if="showsContent" class="skills-page__list">
      <li v-for="skill in skills" :key="skill.key" class="skills-page__item">
        <h2 class="skills-page__name">{{ skill.name }}</h2>
        <p v-if="skill.description" class="skills-page__description">
          {{ skill.description }}
        </p>
      </li>
    </ul>
  </section>
</template>

<style scoped lang="scss">
.skills-page {
  display: grid;
  gap: var(--tm-space-8);
  padding-block: var(--tm-content-block-start) var(--tm-content-block-end);
}

.skills-page__list {
  display: grid;
  gap: var(--tm-space-6);
  max-inline-size: var(--tm-prose-max-width);
  margin: 0;
  padding-inline-start: var(--tm-space-6);
}

.skills-page__item {
  display: grid;
  gap: var(--tm-space-2);
}

.skills-page__name,
.skills-page__description {
  margin: 0;
}

.skills-page__name {
  font-size: 1.125rem;
}

.skills-page__description {
  color: var(--tm-text-secondary);
}
</style>
