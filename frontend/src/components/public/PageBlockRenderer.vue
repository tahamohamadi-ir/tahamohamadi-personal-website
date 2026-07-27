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
  heroHeadingLevel: { type: Number, default: 2 },
  sections: { type: Array, default: () => [] }
})
const { t } = useI18n()

const supportedBlocks = new Set([
  'hero', 'rich-text', 'media', 'media-text', 'call-to-action', 'collection',
  'skills', 'resume', 'social-links', 'contact', 'divider', 'spacer',
  'gallery', 'stats', 'quote'
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

/* Section layout helpers */
const sectionMap = computed(() => {
  const map = {}
  props.sections.forEach((section, index) => {
    let settings = {}
    try { settings = section.settingsJson ? JSON.parse(section.settingsJson) : {} }
    catch { settings = {} }
    map[index] = {
      layout: section.layout || 'SINGLE_COLUMN',
      ratio: settings.ratio || 'EQUAL',
      padding: settings.padding || 'STANDARD',
      background: settings.background || 'TRANSPARENT',
      fullWidth: Boolean(settings.fullWidth),
      enabled: section.enabled !== false
    }
  })
  return map
})

function sectionStyle(sectionIndex) {
  const section = sectionMap.value[sectionIndex]
  if (!section) return {}
  const style = {}
  const paddingVar = `var(--tm-section-padding-${(section.padding || 'standard').toLowerCase()})`
  style.paddingBlock = paddingVar
  const bgVar = `var(--tm-section-bg-${(section.background || 'transparent').toLowerCase()})`
  style.background = bgVar
  if (['DARK', 'ACCENT'].includes(section.background)) {
    style.color = 'var(--tm-section-dark-text)'
  }
  return style
}

function sectionContentClass(sectionIndex) {
  const section = sectionMap.value[sectionIndex]
  if (!section) return ['tm-container']
  const classes = []
  if (!section.fullWidth) classes.push('tm-container')
  const layout = section.layout || 'SINGLE_COLUMN'
  if (layout !== 'SINGLE_COLUMN') {
    classes.push('page-section__grid')
    classes.push(`page-section__grid--${layout.toLowerCase().replace(/_/g, '-')}`)
    const ratio = section.ratio || 'EQUAL'
    if (ratio !== 'EQUAL') {
      classes.push(`page-section__ratio--${ratio.toLowerCase().replace(/_/g, '-')}`)
    }
  }
  return classes
}

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
      :style="block.sectionIndex !== undefined ? sectionStyle(block.sectionIndex) : {}"
    >
      <div :class="block.sectionIndex !== undefined ? sectionContentClass(block.sectionIndex) : ['tm-container']" class="page-block__content">
        <template v-if="block.type === 'hero'">
          <div class="page-block__hero-copy">
            <div v-if="block.eyebrow" class="page-block__hero-badge">
              <span class="page-block__hero-badge-dot" aria-hidden="true" />
              <p class="page-block__kicker">{{ block.eyebrow }}</p>
            </div>
            <component :is="block.headingLevel === 1 ? 'h1' : 'h2'" v-if="block.title" class="page-block__title">{{ block.title }}</component>
            <p v-if="block.lead" class="page-block__lead">{{ block.lead }}</p>
            <div v-if="block.actionLabel && isSafePath(block.actionPath)" class="page-block__hero-actions">
              <TmButton :to="isInternalPath(block.actionPath) ? block.actionPath : undefined" :href="isInternalPath(block.actionPath) ? undefined : block.actionPath" :target="isInternalPath(block.actionPath) ? undefined : '_blank'" :rel="isInternalPath(block.actionPath) ? undefined : 'noopener noreferrer'">
                {{ block.actionLabel }}
              </TmButton>
            </div>
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
            <figure class="page-block__media-figure">
              <img
                v-if="mediaUrl(block)"
                class="page-block__media"
                :src="mediaUrl(block)"
                :alt="block.alt ?? ''"
                width="1200"
                height="800"
                loading="lazy"
              >
            </figure>
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
          <div class="page-block__cta-card">
            <p v-if="block.eyebrow" class="page-block__kicker">{{ block.eyebrow }}</p>
            <h2 v-if="block.title" class="page-block__title">{{ block.title }}</h2>
            <p v-if="block.lead" class="page-block__lead">{{ block.lead }}</p>
            <TmButton v-if="block.actionLabel && isSafePath(block.actionPath)" :to="isInternalPath(block.actionPath) ? block.actionPath : undefined" :href="isInternalPath(block.actionPath) ? undefined : block.actionPath" :target="isInternalPath(block.actionPath) ? undefined : '_blank'" :rel="isInternalPath(block.actionPath) ? undefined : 'noopener noreferrer'">{{ block.actionLabel }}</TmButton>
          </div>
        </template>

        <template v-else-if="block.type === 'collection'">
          <div class="page-block__header">
            <p v-if="block.eyebrow" class="page-block__kicker">{{ block.eyebrow }}</p>
            <h2 v-if="block.title" class="page-block__title">{{ block.title }}</h2>
            <p v-if="block.lead" class="page-block__lead">{{ block.lead }}</p>
          </div>
          <ol v-if="collectionEntries(block).length" class="page-block__collection">
            <li v-for="(entry, entryIndex) in collectionEntries(block)" :key="entry.path">
              <router-link :to="entry.path" class="page-block__collection-link tm-interactive">
                <article class="page-block__collection-card">
                  <span class="page-block__collection-index" aria-hidden="true">{{ String(entryIndex + 1).padStart(2, '0') }}</span>
                  <div class="page-block__collection-body">
                    <component :is="block.title ? 'h3' : 'h2'" class="page-block__collection-heading">{{ entry.title }}</component>
                    <p v-if="entrySummary(entry)" class="page-block__collection-summary">{{ entrySummary(entry) }}</p>
                  </div>
                </article>
              </router-link>
            </li>
          </ol>
          <div v-if="block.actionLabel && isSafePath(block.actionPath)" class="page-block__actions">
            <TmButton variant="secondary" :to="isInternalPath(block.actionPath) ? block.actionPath : undefined" :href="isInternalPath(block.actionPath) ? undefined : block.actionPath" :target="isInternalPath(block.actionPath) ? undefined : '_blank'" :rel="isInternalPath(block.actionPath) ? undefined : 'noopener noreferrer'">{{ block.actionLabel }}</TmButton>
          </div>
        </template>

        <template v-else-if="block.type === 'skills'">
          <div class="page-block__header">
            <p v-if="block.eyebrow" class="page-block__kicker">{{ block.eyebrow }}</p>
            <h2 v-if="block.title" class="page-block__title">{{ block.title }}</h2>
            <p v-if="block.lead" class="page-block__lead">{{ block.lead }}</p>
          </div>
          <ul v-if="skills.length" class="page-block__skills">
            <li v-for="skill in skills" :key="skill.key" class="page-block__skill-item">
              <strong>{{ skill.name }}</strong>
              <span v-if="skill.description">{{ skill.description }}</span>
            </li>
          </ul>
          <div v-if="block.actionLabel && isSafePath(block.actionPath)" class="page-block__actions">
            <TmButton variant="secondary" :to="isInternalPath(block.actionPath) ? block.actionPath : undefined" :href="isInternalPath(block.actionPath) ? undefined : block.actionPath" :target="isInternalPath(block.actionPath) ? undefined : '_blank'" :rel="isInternalPath(block.actionPath) ? undefined : 'noopener noreferrer'">{{ block.actionLabel }}</TmButton>
          </div>
        </template>

        <template v-else-if="block.type === 'social-links'">
          <div class="page-block__header">
            <p v-if="block.eyebrow" class="page-block__kicker">{{ block.eyebrow }}</p>
            <h2 v-if="block.title" class="page-block__title">{{ block.title }}</h2>
            <p v-if="block.lead" class="page-block__lead">{{ block.lead }}</p>
          </div>
          <nav v-if="socialLinks.length" class="page-block__social-links" :aria-label="block.title || block.eyebrow">
            <a v-for="link in socialLinks" :key="`${link.platformCode}:${link.url}`" :href="link.url" class="page-block__social-item tm-interactive" target="_blank" rel="noopener noreferrer">{{ link.platformCode }}</a>
          </nav>
          <div v-if="block.actionLabel && isSafePath(block.actionPath)" class="page-block__actions">
            <TmButton variant="secondary" :to="isInternalPath(block.actionPath) ? block.actionPath : undefined" :href="isInternalPath(block.actionPath) ? undefined : block.actionPath" :target="isInternalPath(block.actionPath) ? undefined : '_blank'" :rel="isInternalPath(block.actionPath) ? undefined : 'noopener noreferrer'">{{ block.actionLabel }}</TmButton>
          </div>
        </template>

        <!-- Decorative: Divider -->
        <template v-else-if="block.type === 'divider'">
          <hr class="page-block__divider" aria-hidden="true">
        </template>

        <!-- Decorative: Spacer -->
        <template v-else-if="block.type === 'spacer'">
          <div class="page-block__spacer" :style="{ blockSize: `${block.height ?? 48}px` }" aria-hidden="true" />
        </template>

        <!-- Quote block -->
        <template v-else-if="block.type === 'quote'">
          <blockquote class="page-block__quote">
            <p v-if="block.lead" class="page-block__quote-text">{{ block.lead }}</p>
            <footer v-if="block.title" class="page-block__quote-attribution">— {{ block.title }}</footer>
          </blockquote>
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
.page-block__content { display: grid; gap: var(--tm-space-6); }
.page-block--hero { padding-block: clamp(var(--tm-space-10), 10vw, var(--tm-space-18)); }
.page-block--has-hero-media .page-block__content { gap: var(--tm-space-8); }
.page-block__hero-copy { display: grid; gap: var(--tm-space-5); max-inline-size: 62rem; }
.page-block__hero-badge { display: inline-flex; align-items: center; gap: var(--tm-space-2); }
.page-block__hero-badge-dot { inline-size: 8px; block-size: 8px; border-radius: 50%; background: var(--tm-success); display: inline-block; }
.page-block__hero-actions, .page-block__actions { margin-block-start: var(--tm-space-2); }
.page-block__hero-media { margin: 0; overflow: hidden; border-radius: var(--tm-radius-card); border: 1px solid var(--tm-border-subtle); }
.page-block__hero-media img { aspect-ratio: 16 / 9; background: var(--tm-editorial-muted-surface); display: block; inline-size: 100%; object-fit: cover; transition: transform var(--tm-motion-enter) ease; }
.page-block__hero-media:hover img { transform: scale(1.02); }
.page-block__kicker { color: var(--tm-editorial-kicker); font-size: .75rem; font-weight: 800; letter-spacing: .12em; margin: 0; text-transform: uppercase; }
.page-block__title { font-size: clamp(2rem, 5vw, 4.25rem); letter-spacing: -.045em; line-height: 1.08; margin: 0; max-inline-size: 20ch; }
.page-block__lead { color: var(--tm-text-secondary); font-size: clamp(1.0625rem, 2vw, 1.25rem); line-height: 1.7; margin: 0; max-inline-size: 62ch; }
.page-block--call-to-action { background: var(--tm-editorial-muted-surface); }
.page-block__cta-card { display: grid; gap: var(--tm-space-4); max-inline-size: 54rem; padding: var(--tm-space-6); border-radius: var(--tm-radius-card); }
.page-block__media-layout { display: grid; gap: var(--tm-space-6); align-items: center; }
.page-block__media-figure { margin: 0; border-radius: var(--tm-radius-card); overflow: hidden; border: 1px solid var(--tm-border-subtle); }
.page-block__media { aspect-ratio: 3 / 2; background: var(--tm-editorial-muted-surface); display: block; inline-size: 100%; object-fit: cover; }
.page-block__header { display: grid; gap: var(--tm-space-3); }
.page-block__collection { display: grid; grid-template-columns: repeat(auto-fit, minmax(min(100%, 17rem), 1fr)); gap: var(--tm-space-5); margin: var(--tm-space-2) 0 0; padding: 0; list-style: none; }
.page-block__collection-link { display: block; block-size: 100%; min-block-size: 11rem; padding: var(--tm-space-5); border: 1px solid var(--tm-editorial-card-border); border-radius: var(--tm-radius-card); background: var(--tm-editorial-card-surface); color: var(--tm-text-primary); text-decoration: none; transition: border-color var(--tm-motion-state) ease, box-shadow var(--tm-motion-state) ease, transform var(--tm-motion-state) ease; }
.page-block__collection-link:hover { border-color: var(--tm-editorial-card-hover-border); box-shadow: var(--tm-editorial-shadow-hover); transform: translateY(-3px); }
.page-block__collection-link:focus-visible { outline: 3px solid var(--tm-focus-ring); outline-offset: 3px; }
.page-block__collection-card { display: grid; grid-template-columns: 2rem minmax(0, 1fr); gap: var(--tm-space-3); }
.page-block__collection-index { color: var(--tm-action-primary); font-size: .875rem; font-weight: 800; }
.page-block__collection-body { display: grid; gap: var(--tm-space-2); }
.page-block__collection-heading { font-size: clamp(1.125rem, 2vw, 1.375rem); font-weight: 700; margin: 0; line-height: 1.35; }
.page-block__collection-summary { color: var(--tm-text-secondary); font-size: .95rem; line-height: 1.6; margin: 0; }
.page-block__skills { display: grid; gap: var(--tm-space-3); padding: 0; margin: 0; list-style: none; }
.page-block__skill-item { display: grid; gap: var(--tm-space-1); padding-block: var(--tm-space-4); border-block-start: 1px solid var(--tm-editorial-rule); }
.page-block__skill-item strong { font-size: clamp(1.125rem, 2vw, 1.375rem); }
.page-block__skill-item span { color: var(--tm-text-secondary); font-size: 1rem; line-height: 1.6; }
.page-block__social-links { display: flex; flex-wrap: wrap; gap: var(--tm-space-3); }
.page-block__social-item { display: inline-flex; align-items: center; min-block-size: var(--tm-control-min-size); padding-inline: var(--tm-space-4); border: 1px solid var(--tm-border-subtle); border-radius: var(--tm-radius-control); color: var(--tm-action-primary); font-weight: 700; text-decoration: none; transition: border-color var(--tm-motion-state) ease, background-color var(--tm-motion-state) ease; }
.page-block__social-item:hover { border-color: var(--tm-action-primary); background: var(--tm-interactive-surface-hover); }

/* New block types */
.page-block--divider { padding-block: 0; border-block-end: none; }
.page-block__divider { border: none; border-block-start: 1px solid var(--tm-editorial-rule); margin: 0; }

.page-block--spacer { padding-block: 0; border-block-end: none; }
.page-block__spacer { display: block; }

.page-block__quote { border-inline-start: 4px solid var(--tm-action-primary); margin: 0; padding: var(--tm-space-6); padding-inline-start: var(--tm-space-8); background: var(--tm-editorial-muted-surface); border-radius: var(--tm-radius-card); }
.page-block__quote-text { font-size: clamp(1.125rem, 2.5vw, 1.5rem); line-height: 1.6; margin: 0; font-style: italic; color: var(--tm-text-primary); }
.page-block__quote-attribution { margin-block-start: var(--tm-space-3); color: var(--tm-text-secondary); font-size: .95rem; font-style: normal; }

/* Section grid layouts */
.page-section__grid { display: grid; gap: var(--tm-space-6); }
.page-section__grid--two-column { grid-template-columns: repeat(2, 1fr); }
.page-section__grid--three-column { grid-template-columns: repeat(3, 1fr); }
.page-section__grid--four-column { grid-template-columns: repeat(4, 1fr); }

/* Column ratio presets (only apply to 2-column) */
.page-section__ratio--wide-narrow { grid-template-columns: 2fr 1fr; }
.page-section__ratio--narrow-wide { grid-template-columns: 1fr 2fr; }
.page-section__ratio--golden { grid-template-columns: 61fr 39fr; }
.page-section__ratio--quarter-three { grid-template-columns: 1fr 3fr; }

@media (prefers-reduced-motion: reduce) { .page-block__collection-link, .page-block__hero-media img { transition: none; } .page-block__collection-link:hover, .page-block__hero-media:hover img { transform: none; } }
@media (max-width: 599px) {
  .page-section__grid--two-column,
  .page-section__grid--three-column,
  .page-section__grid--four-column,
  .page-section__ratio--wide-narrow,
  .page-section__ratio--narrow-wide,
  .page-section__ratio--golden,
  .page-section__ratio--quarter-three { grid-template-columns: 1fr; }
}
@media (min-width: 600px) and (max-width: 899px) {
  .page-section__grid--three-column { grid-template-columns: repeat(2, 1fr); }
  .page-section__grid--four-column { grid-template-columns: repeat(2, 1fr); }
}
@media (min-width: 900px) { .page-block--has-hero-media .page-block__content { grid-template-columns: minmax(0, .88fr) minmax(24rem, 1.12fr); align-items: end; } .page-block__media-layout { grid-template-columns: minmax(0, 1fr) minmax(18rem, .8fr); } }
</style>
