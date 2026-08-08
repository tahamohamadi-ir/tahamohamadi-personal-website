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
      <div class="resume-timeline__main">
        <header class="resume-timeline__heading">
          <h2 class="resume-timeline__title">{{ entry.title }}</h2>
          <p class="resume-timeline__organization">{{ entry.organization }}</p>
        </header>

        <p
          v-if="entry.summary"
          class="resume-timeline__summary"
        >
          {{ entry.summary }}
        </p>
      </div>
      
      <div class="resume-timeline__aside">
        <div class="resume-timeline__meta tm-mono-meta">
          <span class="resume-timeline__type">
            {{ entryTypeLabel(entry.entryType) }}
          </span>
          <span class="resume-timeline__date">
            <time :datetime="entry.startedOn">{{ entry.startedOn }}</time>
            <span class="resume-timeline__separator"> &mdash; </span>
            <template v-if="entry.current">
              <span class="resume-timeline__current">
                {{ t('resume.current') }}
              </span>
            </template>
            <template v-else-if="entry.endedOn">
              <time :datetime="entry.endedOn">{{ entry.endedOn }}</time>
            </template>
          </span>
        </div>
        <div
          v-if="entry.location"
          class="resume-timeline__location tm-mono-meta"
        >
          {{ entry.location }}
        </div>
      </div>
    </li>
  </ol>
</template>

<style scoped lang="scss">
.resume-timeline {
  display: flex;
  flex-direction: column;
  margin: 0;
  padding: 0;
  list-style: none;
}

.resume-timeline__entry {
  display: grid;
  gap: var(--tm-space-2);
  padding-block: var(--tm-space-5);
  border-block-end: 1px solid var(--tm-border-subtle);
  transition: background-color var(--tm-motion-state) var(--tm-ease-out), padding-inline var(--tm-motion-state) var(--tm-ease-out);
  border-radius: var(--tm-radius-card);
}

.resume-timeline__entry:hover {
  background-color: var(--tm-editorial-muted-surface);
  padding-inline: var(--tm-space-4);
  /* To keep border spanning correctly we can use negative margins, or simply padding adjustments */
  margin-inline: calc(var(--tm-space-4) * -1);
}

.resume-timeline__entry:last-child {
  border-block-end: 0;
}

.resume-timeline__main {
  display: grid;
  gap: var(--tm-space-3);
}

.resume-timeline__aside {
  display: flex;
  flex-direction: column;
  gap: var(--tm-space-1);
  margin-block-start: var(--tm-space-2);
}

.resume-timeline__heading {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-2);
  align-items: baseline;
}

.resume-timeline__title,
.resume-timeline__organization,
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

.resume-timeline__meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-3);
  align-items: center;
}

.resume-timeline__type {
  color: var(--tm-action-primary);
}

.resume-timeline__current {
  color: var(--tm-action-primary);
}

.resume-timeline__summary {
  color: var(--tm-text-primary);
}

.resume-timeline__separator {
  color: var(--tm-border-subtle);
}

@media (min-width: 768px) {
  .resume-timeline__entry {
    grid-template-columns: 1fr 220px;
    align-items: start;
    gap: var(--tm-space-6);
  }

  .resume-timeline__aside {
    margin-block-start: 0;
    align-items: flex-end;
    text-align: end;
  }

  .resume-timeline__meta {
    justify-content: flex-end;
  }
}
</style>
