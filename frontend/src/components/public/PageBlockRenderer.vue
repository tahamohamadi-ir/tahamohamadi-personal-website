<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import MarkdownContent from 'src/components/content/MarkdownContent.vue'
import TmButton from 'src/components/shared/TmButton.vue'

const props = defineProps({
  blocks: { type: Array, default: () => [] },
  locale: { type: String, required: true },
  collectionItems: { type: Object, default: () => ({}) },
  skills: { type: Array, default: () => [] },
  socialLinks: { type: Array, default: () => [] },
  heroHeadingLevel: { type: Number, default: 2 }
})
const { t } = useI18n()

const supportedBlocks = new Set([
  'hero', 'rich-text', 'media', 'media-text', 'call-to-action', 'collection',
  'skills', 'resume', 'social-links', 'contact'
])

const visibleBlocks = computed(() => {
  let primaryHeroAvailable = props.heroHeadingLevel === 1

  return props.blocks
    .map((block) => ({
      ...(block?.settings ?? {}),
      ...block,
      type: typeof block?.type === 'string' ? block.type.toLowerCase().replaceAll('_', '-') : ''
    }))
    .filter((block) => (
      block
      && block.enabled !== false
      && supportedBlocks.has(block.type)
      && (block.type !== 'collection' || collectionEntries(block).length > 0)
    ))
    .map((block) => {
      const headingLevel = block.type === 'hero' && primaryHeroAvailable && block.title
        ? 1
        : 2

      if (block.type === 'hero' && block.title) {
        primaryHeroAvailable = false
      }

      return { ...block, headingLevel }
    })
})

function isSafePath(value) {
  return typeof value === 'string' && (/^\/(fa|en)(?:\/|$)/.test(value) || /^https:\/\//.test(value))
}

function isInternalPath(value) {
  return typeof value === 'string' && /^\/(fa|en)(?:\/|$)/.test(value)
}

function isCurrentLocalePath(value) {
  return isInternalPath(value) && new RegExp(`^/${props.locale}(?:/|$)`).test(value)
}

function mediaUrl(block) {
  return block.mediaUrl || (block.mediaId ? `/api/v1/public/media/${block.mediaId}` : null)
}

function mediaAlt(block) {
  return typeof block.alt === 'string' ? block.alt.trim() : ''
}

function heroMediaUrl(block) {
  return mediaAlt(block) ? mediaUrl(block) : null
}

function blockClasses(block) {
  return [
    `page-block--${block.type}`,
    {
      'page-block--has-hero-media': block.type === 'hero' && Boolean(heroMediaUrl(block))
    }
  ]
}

function collectionEntries(block) {
  const source = block.source
  const items = Array.isArray(props.collectionItems[source])
    ? props.collectionItems[source]
    : []
  const limit = Number.isInteger(block.limit) ? block.limit : items.length
  return items.slice(0, limit).map((item) => ({
    ...item,
    path: collectionPath(source, item)
  })).filter((item) => item.path)
}

function collectionPath(source, item) {
  if (isCurrentLocalePath(item?.canonicalPath)) return item.canonicalPath
  const slug = item?.slug
  if (typeof slug !== 'string' || !slug.trim()) return null
  const route = {
    BLOG: 'blog',
    PORTFOLIO: 'portfolio',
    PUBLICATIONS: 'publications'
  }[source]
  return route ? `/${props.locale}/${route}/${slug}` : null
}

function entrySummary(entry) {
  return entry.excerpt || entry.summary || entry.abstractText || ''
}
</script>

<template>
  <div class="page-block-renderer">
    <section
      v-for="(block, index) in visibleBlocks"
      :key="block.id ?? `${block.type}-${index}`"
      class="page-block"
      :class="blockClasses(block)"
    >
      <div class="tm-container page-block__content">
        <template v-if="block.type === 'hero'">
          <div class="page-block__hero-copy">
            <p v-if="block.eyebrow" class="page-block__kicker">{{ block.eyebrow }}</p>
            <component :is="block.headingLevel === 1 ? 'h1' : 'h2'" v-if="block.title" class="page-block__title">{{ block.title }}</component>
            <p v-if="block.lead" class="page-block__lead">{{ block.lead }}</p>
            <TmButton v-if="block.actionLabel && isSafePath(block.actionPath)" :to="isInternalPath(block.actionPath) ? block.actionPath : undefined" :href="isInternalPath(block.actionPath) ? undefined : block.actionPath" :target="isInternalPath(block.actionPath) ? undefined : '_blank'" :rel="isInternalPath(block.actionPath) ? undefined : 'noopener noreferrer'">
              {{ block.actionLabel }}
            </TmButton>
          </div>
          <figure v-if="heroMediaUrl(block)" class="page-block__hero-media">
            <img
              :src="heroMediaUrl(block)"
              :alt="mediaAlt(block)"
              width="1600"
              height="900"
              fetchpriority="high"
            >
          </figure>
        </template>

        <template v-else-if="block.type === 'rich-text'">
          <h2 v-if="block.title" class="page-block__title">{{ block.title }}</h2>
          <MarkdownContent :markdown="block.bodyMarkdown ?? ''">
            <template #error><p role="alert">{{ t('public.richContent.renderingFailure') }}</p></template>
          </MarkdownContent>
        </template>

        <template v-else-if="block.type === 'media' || block.type === 'media-text'">
          <div class="page-block__media-layout">
            <img
              v-if="mediaUrl(block)"
              class="page-block__media"
              :src="mediaUrl(block)"
              :alt="block.alt ?? ''"
              width="1200"
              height="800"
              loading="lazy"
            >
            <div>
              <h2 v-if="block.title" class="page-block__title">{{ block.title }}</h2>
              <p v-if="block.lead" class="page-block__lead">{{ block.lead }}</p>
              <MarkdownContent v-if="block.bodyMarkdown" :markdown="block.bodyMarkdown">
                <template #error><p role="alert">{{ t('public.richContent.renderingFailure') }}</p></template>
              </MarkdownContent>
            </div>
          </div>
        </template>

        <template v-else-if="block.type === 'call-to-action'">
          <p v-if="block.eyebrow" class="page-block__kicker">{{ block.eyebrow }}</p>
          <h2 v-if="block.title" class="page-block__title">{{ block.title }}</h2>
          <p v-if="block.lead" class="page-block__lead">{{ block.lead }}</p>
          <TmButton v-if="block.actionLabel && isSafePath(block.actionPath)" :to="isInternalPath(block.actionPath) ? block.actionPath : undefined" :href="isInternalPath(block.actionPath) ? undefined : block.actionPath" :target="isInternalPath(block.actionPath) ? undefined : '_blank'" :rel="isInternalPath(block.actionPath) ? undefined : 'noopener noreferrer'">{{ block.actionLabel }}</TmButton>
        </template>

        <template v-else-if="block.type === 'collection'">
          <p v-if="block.eyebrow" class="page-block__kicker">{{ block.eyebrow }}</p>
          <h2 v-if="block.title" class="page-block__title">{{ block.title }}</h2>
          <p v-if="block.lead" class="page-block__lead">{{ block.lead }}</p>
          <ol v-if="collectionEntries(block).length" class="page-block__collection">
            <li v-for="(entry, entryIndex) in collectionEntries(block)" :key="entry.path">
              <router-link :to="entry.path" class="page-block__collection-link tm-interactive">
                <article class="page-block__collection-card">
                  <span class="page-block__collection-index" aria-hidden="true">{{ String(entryIndex + 1).padStart(2, '0') }}</span>
                  <div>
                    <component :is="block.title ? 'h3' : 'h2'">{{ entry.title }}</component>
                    <p v-if="entrySummary(entry)">{{ entrySummary(entry) }}</p>
                  </div>
                </article>
              </router-link>
            </li>
          </ol>
          <TmButton v-if="block.actionLabel && isSafePath(block.actionPath)" variant="secondary" :to="isInternalPath(block.actionPath) ? block.actionPath : undefined" :href="isInternalPath(block.actionPath) ? undefined : block.actionPath" :target="isInternalPath(block.actionPath) ? undefined : '_blank'" :rel="isInternalPath(block.actionPath) ? undefined : 'noopener noreferrer'">{{ block.actionLabel }}</TmButton>
        </template>

        <template v-else-if="block.type === 'skills'">
          <p v-if="block.eyebrow" class="page-block__kicker">{{ block.eyebrow }}</p>
          <h2 v-if="block.title" class="page-block__title">{{ block.title }}</h2>
          <p v-if="block.lead" class="page-block__lead">{{ block.lead }}</p>
          <ul v-if="skills.length" class="page-block__skills">
            <li v-for="skill in skills" :key="skill.key"><strong>{{ skill.name }}</strong><span v-if="skill.description">{{ skill.description }}</span></li>
          </ul>
          <TmButton v-if="block.actionLabel && isSafePath(block.actionPath)" variant="secondary" :to="isInternalPath(block.actionPath) ? block.actionPath : undefined" :href="isInternalPath(block.actionPath) ? undefined : block.actionPath" :target="isInternalPath(block.actionPath) ? undefined : '_blank'" :rel="isInternalPath(block.actionPath) ? undefined : 'noopener noreferrer'">{{ block.actionLabel }}</TmButton>
        </template>

        <template v-else-if="block.type === 'social-links'">
          <p v-if="block.eyebrow" class="page-block__kicker">{{ block.eyebrow }}</p>
          <h2 v-if="block.title" class="page-block__title">{{ block.title }}</h2>
          <p v-if="block.lead" class="page-block__lead">{{ block.lead }}</p>
          <nav v-if="socialLinks.length" class="page-block__social-links" :aria-label="block.title || block.eyebrow">
            <a v-for="link in socialLinks" :key="`${link.platformCode}:${link.url}`" :href="link.url" class="tm-interactive" target="_blank" rel="noopener noreferrer">{{ link.platformCode }}</a>
          </nav>
          <TmButton v-if="block.actionLabel && isSafePath(block.actionPath)" variant="secondary" :to="isInternalPath(block.actionPath) ? block.actionPath : undefined" :href="isInternalPath(block.actionPath) ? undefined : block.actionPath" :target="isInternalPath(block.actionPath) ? undefined : '_blank'" :rel="isInternalPath(block.actionPath) ? undefined : 'noopener noreferrer'">{{ block.actionLabel }}</TmButton>
        </template>

        <template v-else>
          <slot :name="block.type" :block="block">
            <h2 v-if="block.title" class="page-block__title">{{ block.title }}</h2>
            <p v-if="block.lead" class="page-block__lead">{{ block.lead }}</p>
            <TmButton v-if="block.actionLabel && isSafePath(block.actionPath)" variant="secondary" :to="isInternalPath(block.actionPath) ? block.actionPath : undefined" :href="isInternalPath(block.actionPath) ? undefined : block.actionPath">{{ block.actionLabel }}</TmButton>
          </slot>
        </template>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.page-block { border-block-end: 1px solid var(--tm-editorial-rule); padding-block: clamp(var(--tm-space-8), 8vw, var(--tm-space-16)); }
.page-block__content { display: grid; gap: var(--tm-space-5); }
.page-block--hero { padding-block: clamp(var(--tm-space-10), 10vw, var(--tm-space-16)); }
.page-block--has-hero-media .page-block__content { gap: var(--tm-space-8); }
.page-block__hero-copy { display: grid; gap: var(--tm-space-5); max-inline-size: 62rem; }
.page-block__hero-media { margin: 0; }
.page-block__hero-media img { aspect-ratio: 16 / 9; background: var(--tm-editorial-muted-surface); display: block; inline-size: 100%; object-fit: cover; }
.page-block__kicker { color: var(--tm-editorial-kicker); font-size: .75rem; font-weight: 800; letter-spacing: .12em; margin: 0; text-transform: uppercase; }
.page-block__title { font-size: clamp(2rem, 5vw, 4rem); letter-spacing: -.045em; line-height: 1.05; margin: 0; max-inline-size: 19ch; }
.page-block__lead { color: var(--tm-text-secondary); font-size: clamp(1.0625rem, 2vw, 1.25rem); line-height: 1.7; margin: 0; max-inline-size: 62ch; }
.page-block--call-to-action { background: var(--tm-editorial-muted-surface); }
.page-block__media-layout { display: grid; gap: var(--tm-space-6); align-items: center; }
.page-block__media { aspect-ratio: 3 / 2; background: var(--tm-editorial-muted-surface); display: block; inline-size: 100%; object-fit: cover; }
.page-block__collection { display: grid; grid-template-columns: repeat(auto-fit, minmax(min(100%, 17rem), 1fr)); gap: var(--tm-space-4); margin: var(--tm-space-3) 0 0; padding: 0; list-style: none; }
.page-block__collection-link { display: block; block-size: 100%; min-block-size: 11rem; padding: var(--tm-space-5); border: 1px solid var(--tm-editorial-card-border); border-radius: var(--tm-radius-card); background: var(--tm-editorial-card-surface); color: var(--tm-text-primary); text-decoration: none; transition: border-color 180ms ease, box-shadow 180ms ease, transform 180ms ease; }
.page-block__collection-link:hover { border-color: var(--tm-link); box-shadow: var(--tm-editorial-shadow); transform: translateY(-2px); }
.page-block__collection-link:focus-visible { outline: 3px solid var(--tm-focus-ring); outline-offset: 3px; }
.page-block__collection-card { display: grid; grid-template-columns: 2rem minmax(0, 1fr); gap: var(--tm-space-3); }
.page-block__collection-index { color: var(--tm-text-secondary); font-size: .75rem; font-weight: 800; }
.page-block__collection-card > div, .page-block__skills li { display: grid; gap: var(--tm-space-2); }
.page-block__collection-card :is(h2, h3), .page-block__collection-card p { margin: 0; }
.page-block__collection-card :is(h2, h3), .page-block__skills strong { font-size: clamp(1.125rem, 2vw, 1.5rem); }
.page-block__collection-card p, .page-block__skills span { color: var(--tm-text-secondary); font-size: 1rem; line-height: 1.6; }
.page-block__skills li { padding-block: var(--tm-space-5); border-block-start: 1px solid var(--tm-editorial-rule); }
.page-block__social-links { display: flex; flex-wrap: wrap; gap: var(--tm-space-3); }
.page-block__social-links a { display: inline-flex; align-items: center; min-block-size: var(--tm-control-min-size); color: var(--tm-action-primary); font-weight: 800; }
@media (prefers-reduced-motion: reduce) { .page-block__collection-link { transition: none; } .page-block__collection-link:hover { transform: none; } }
@media (min-width: 900px) { .page-block--has-hero-media .page-block__content { grid-template-columns: minmax(0, .88fr) minmax(24rem, 1.12fr); align-items: end; } .page-block__media-layout { grid-template-columns: minmax(0, 1fr) minmax(18rem, .8fr); } }
</style>
