<script setup>
import { useI18n } from 'vue-i18n'
import CollectionMedia from 'src/components/public/CollectionMedia.vue'

defineProps({
  publications: {
    type: Array,
    required: true
  }
})

const { t } = useI18n()
</script>

<template>
  <ol class="publication-list">
    <li
      v-for="publication in publications"
      :key="publication.slug"
      class="publication-list__item"
    >
      <article class="publication-list__article">
        <CollectionMedia
          v-if="publication.ogMedia"
          :media="publication.ogMedia"
        />
        <h2 class="publication-list__title">{{ publication.title }}</h2>

        <dl class="publication-list__metadata">
          <div v-if="publication.authorsDisplay">
            <dt>{{ t('collections.publications.authors') }}</dt>
            <dd>{{ publication.authorsDisplay }}</dd>
          </div>
          <div v-if="publication.venueDisplay">
            <dt>{{ t('collections.publications.venue') }}</dt>
            <dd>{{ publication.venueDisplay }}</dd>
          </div>
          <div v-if="publication.publishedOn">
            <dt>{{ t('collections.publications.publishedOn') }}</dt>
            <dd>
              <time :datetime="publication.publishedOn">
                {{ publication.publishedOn }}
              </time>
            </dd>
          </div>
          <div v-else-if="publication.year">
            <dt>{{ t('collections.publications.year') }}</dt>
            <dd>{{ publication.year }}</dd>
          </div>
          <div v-if="publication.stage">
            <dt>{{ t('collections.publications.stage') }}</dt>
            <dd>{{ publication.stage }}</dd>
          </div>
          <div v-if="publication.doi">
            <dt>{{ t('collections.publications.doi') }}</dt>
            <dd><bdi>{{ publication.doi }}</bdi></dd>
          </div>
        </dl>

        <p
          v-if="publication.abstractText"
          class="publication-list__abstract"
        >
          {{ publication.abstractText }}
        </p>

        <a
          v-if="publication.externalUrl"
          class="publication-list__link tm-interactive"
          :href="publication.externalUrl"
          rel="noopener noreferrer"
          target="_blank"
        >
          {{ t('collections.publications.externalLink') }}
        </a>
      </article>
    </li>
  </ol>
</template>

<style scoped lang="scss">
.publication-list {
  display: grid;
  gap: var(--tm-space-6);
  max-inline-size: var(--tm-prose-max-width);
  margin: 0;
  padding: 0;
  list-style: none;
}

.publication-list__item {
  padding-block-end: var(--tm-space-6);
  border-block-end: 1px solid var(--tm-border-subtle);
}

.publication-list__item:last-child {
  padding-block-end: 0;
  border-block-end: 0;
}

.publication-list__article {
  display: grid;
  gap: var(--tm-space-3);
}

.publication-list__title,
.publication-list__abstract {
  margin: 0;
}

.publication-list__title {
  color: var(--tm-text-primary);
  font-size: 1.25rem;
  line-height: 1.35;
}

.publication-list__metadata {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-2) var(--tm-space-4);
  margin: 0;
  color: var(--tm-text-secondary);
}

.publication-list__metadata div {
  display: flex;
  gap: var(--tm-space-1);
}

.publication-list__metadata dt {
  font-weight: 700;
}

.publication-list__metadata dd {
  margin: 0;
}

.publication-list__abstract {
  color: var(--tm-text-secondary);
}

.publication-list__link {
  display: inline-flex;
  align-items: center;
  min-block-size: var(--tm-control-min-size);
  max-inline-size: 100%;
  margin-block-start: 0;
  color: var(--tm-link);
  overflow-wrap: anywhere;
  text-decoration: none;
}

.publication-list__link + .publication-list__link {
  margin-inline-start: var(--tm-space-3);
}

.publication-list__link:hover {
  color: var(--tm-text-primary);
}

.publication-list__link:focus-visible {
  outline: 2px solid var(--tm-focus-ring);
  outline-offset: 2px;
}
</style>
