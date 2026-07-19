<script setup>
import { useI18n } from 'vue-i18n'

defineProps({
  entries: {
    type: Array,
    required: true
  }
})

const { t } = useI18n()

function entryTypeLabel(entryType) {
  return t(`resume.entryTypes.${entryType}`)
}
</script>

<template>
  <ol class="resume-timeline">
    <li
      v-for="entry in entries"
      :key="`${entry.title}-${entry.organization}-${entry.startedOn}`"
      class="resume-timeline__entry"
    >
      <header class="resume-timeline__heading">
        <h2 class="resume-timeline__title">{{ entry.title }}</h2>
        <p class="resume-timeline__organization">{{ entry.organization }}</p>
      </header>

      <p
        v-if="entry.location"
        class="resume-timeline__location"
      >
        {{ entry.location }}
      </p>

      <p class="resume-timeline__meta">
        <span class="resume-timeline__type">
          {{ entryTypeLabel(entry.entryType) }}
        </span>
        <time :datetime="entry.startedOn">{{ entry.startedOn }}</time>
        <template v-if="entry.current">
          <span class="resume-timeline__current">
            {{ t('resume.current') }}
          </span>
        </template>
        <template v-else-if="entry.endedOn">
          <time :datetime="entry.endedOn">{{ entry.endedOn }}</time>
        </template>
      </p>

      <p
        v-if="entry.summary"
        class="resume-timeline__summary"
      >
        {{ entry.summary }}
      </p>
    </li>
  </ol>
</template>

<style scoped lang="scss">
.resume-timeline {
  display: grid;
  gap: var(--tm-space-6);
  max-inline-size: var(--tm-prose-max-width);
  margin: 0;
  padding: 0;
  list-style-position: inside;
}

.resume-timeline__entry {
  padding-block-end: var(--tm-space-6);
  border-block-end: 1px solid var(--tm-border-subtle);
}

.resume-timeline__entry:last-child {
  padding-block-end: 0;
  border-block-end: 0;
}

.resume-timeline__heading,
.resume-timeline__meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-2);
  align-items: baseline;
}

.resume-timeline__title,
.resume-timeline__organization,
.resume-timeline__location,
.resume-timeline__meta,
.resume-timeline__summary {
  margin: 0;
}

.resume-timeline__title {
  color: var(--tm-text-primary);
  font-size: 1.25rem;
  line-height: 1.3;
}

.resume-timeline__organization {
  color: var(--tm-text-secondary);
  font-weight: 700;
}

.resume-timeline__location,
.resume-timeline__meta {
  margin-block-start: var(--tm-space-2);
  color: var(--tm-text-secondary);
}

.resume-timeline__type,
.resume-timeline__current {
  color: var(--tm-action-primary);
  font-weight: 700;
}

.resume-timeline__summary {
  margin-block-start: var(--tm-space-3);
  color: var(--tm-text-primary);
}
</style>
