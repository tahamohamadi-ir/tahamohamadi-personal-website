<script setup>
import { useI18n } from 'vue-i18n'
import CollectionMedia from 'src/components/public/CollectionMedia.vue'
import { formatLocalizedDate } from 'src/utils/formatDate'

defineProps({
  publications: {
    type: Array,
    required: true
  }
})

const { locale, t } = useI18n()
</script>

<template>
  <ol class="publication-list">
    <li
      v-for="publication in publications"
      :key="publication.slug"
      class="publication-list__item"
    >
      <article class="publication-list__article">
        <router-link
          class="publication-list__card tm-interactive"
          :to="publication.canonicalPath"
        >
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
                  {{ formatLocalizedDate(publication.publishedOn, locale) }}
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
        </router-link>

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
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 19rem), 1fr));
  gap: var(--tm-space-5);
  max-inline-size: 76rem;
  margin: 0;
  padding: 0;
  list-style: none;
}

.publication-list__item {
  min-inline-size: 0;
}

.publication-list__article {
  display: grid;
  gap: var(--tm-space-3);
}

.publication-list__card {
  display: grid;
  gap: var(--tm-space-3);
  min-block-size: 18rem;
  padding: var(--tm-space-5);
  border: 1px solid var(--tm-border-subtle);
  border-radius: var(--tm-radius-card);
  background: var(--tm-surface);
  color: inherit;
  text-decoration: none;
  transition: border-color 180ms ease, box-shadow 180ms ease, transform 180ms ease;
}

.publication-list__card:hover {
  border-color: var(--tm-link);
  box-shadow: var(--tm-editorial-shadow);
  transform: translateY(-2px);
}

.publication-list__card:focus-visible {
  outline: 3px solid var(--tm-focus-ring);
  outline-offset: 3px;
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

@media (prefers-reduced-motion: reduce) {
  .publication-list__card {
    transition: none;
  }

  .publication-list__card:hover {
    transform: none;
  }
}
</style>
