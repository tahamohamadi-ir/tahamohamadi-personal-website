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
      <header class="resume-page__header">
        <h1 class="tm-page-title">{{ t('shell.navigation.resume') }}</h1>
        <div class="resume-page__actions">
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
        </div>
      </header>

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

.resume-page__header {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-4);
  align-items: center;
  justify-content: space-between;
}

.resume-page__actions {
  display: flex;
  gap: var(--tm-space-2);
}

.resume-page__file,
.resume-page__refresh {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-inline-size: var(--tm-control-min-size);
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-3);
  border: 1px solid var(--tm-border-subtle);
  border-radius: var(--tm-radius-control);
  background: var(--tm-surface);
  color: var(--tm-text-primary);
  font-family: inherit;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  transition: transform var(--tm-motion-press) var(--tm-ease-out),
              background-color var(--tm-motion-state) var(--tm-ease-out),
              border-color var(--tm-motion-state) var(--tm-ease-out);
}

.resume-page__file:hover,
.resume-page__refresh:hover {
  background-color: var(--tm-editorial-muted-surface);
  border-color: var(--tm-text-secondary);
}

.resume-page__file:active,
.resume-page__refresh:active {
  transform: scale(0.97);
}

.resume-page__file:focus-visible,
.resume-page__refresh:focus-visible {
  outline: 2px solid var(--tm-focus-ring);
  outline-offset: 2px;
}
</style>
