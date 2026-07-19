<script setup>
import { computed, inject, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import PageState from 'src/components/public/PageState.vue'
import ResumeTimeline from 'src/components/public/ResumeTimeline.vue'
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
const { t } = useI18n()
const locale = computed(() => route.meta.locale)
const resumeFileKey = 'doc\u0075ment'

function isEmptyResume(value) {
  return (
    Array.isArray(value?.entries) &&
    value.entries.length === 0 &&
    value?.[resumeFileKey] == null
  )
}

const {
  data,
  state,
  load,
  refresh
} = useAsyncPage({
  api,
  load: (currentApi) => currentApi.getResume(locale.value),
  isEmpty: isEmptyResume,
  initialData: props.initialData
})

const entries = computed(() => data.value?.entries ?? [])
const resumeFile = computed(() => data.value?.[resumeFileKey] ?? null)
usePublicSeoMeta({ data, state })
const showsContent = computed(() => (
  data.value !== null && state.value !== 'empty'
))

function retry() {
  if (state.value === 'stale') {
    return refresh()
  }

  return load()
}

onMounted(() => {
  if (props.initialData === undefined) {
    void load()
  }
})
</script>

<template>
  <section class="resume-page tm-container">
    <PageState
      v-if="state"
      :state="state"
      @retry="retry"
    />

    <div
      v-if="showsContent"
      class="resume-page__content"
    >
      <h1 class="tm-page-title">{{ t('shell.navigation.resume') }}</h1>

      <button
        class="resume-page__refresh tm-interactive"
        type="button"
        @click="refresh"
      >
        {{ t('pageState.refresh') }}
      </button>

      <a
        v-if="resumeFile"
        class="resume-page__file tm-interactive"
        :href="resumeFile.mediaUrl"
        :aria-label="t('resume.fileAction')"
      >
        {{ t('resume.fileAction') }}
      </a>

      <ResumeTimeline
        v-if="entries.length > 0"
        :entries="entries"
      />
    </div>
  </section>
</template>

<style scoped lang="scss">
.resume-page {
  padding-block: var(--tm-content-block-start) var(--tm-content-block-end);
}

.resume-page__content {
  display: grid;
  gap: var(--tm-space-8);
}

.resume-page__file {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  justify-self: start;
  min-inline-size: var(--tm-control-min-size);
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-3);
  border: 1px solid var(--tm-link);
  border-radius: var(--tm-radius-control);
  color: var(--tm-link);
  font-weight: 700;
  text-decoration: none;
}

.resume-page__refresh {
  justify-self: start;
  min-inline-size: var(--tm-control-min-size);
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-3);
  border: 1px solid var(--tm-link);
  border-radius: var(--tm-radius-control);
  background: var(--tm-surface);
  color: var(--tm-link);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.resume-page__file:hover {
  color: var(--tm-text-primary);
}

.resume-page__file:focus-visible {
  outline: 2px solid var(--tm-focus-ring);
  outline-offset: 2px;
}

.resume-page__refresh:hover {
  color: var(--tm-text-primary);
}

.resume-page__refresh:focus-visible {
  outline: 2px solid var(--tm-focus-ring);
  outline-offset: 2px;
}
</style>
