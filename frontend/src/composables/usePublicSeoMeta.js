import { unref, useSSRContext, watch } from 'vue'
import { useMeta } from 'quasar'

const SUPPORTED_LOCALES = new Set(['fa', 'en'])
const NOINDEX_STATES = new Set([
  'not-found',
  'translation-unavailable'
])

function plainText(value) {
  return typeof value === 'string' && value.trim().length > 0
    ? value.trim()
    : undefined
}

function firstPlainText(...values) {
  for (const value of values) {
    const text = plainText(value)

    if (text) {
      return text
    }
  }

  return undefined
}

function publicPath(value, locale) {
  if (
    typeof value !== 'string' ||
    /[\r\n]/.test(value) ||
    !SUPPORTED_LOCALES.has(locale) ||
    !value.startsWith(`/${locale}`) ||
    (value.length > locale.length + 1 && value[locale.length + 1] !== '/')
  ) {
    return undefined
  }

  return value
}

function sameOriginPath(value) {
  if (
    typeof value !== 'string' ||
    !value.startsWith('/') ||
    value.startsWith('//') ||
    /[\r\n]/.test(value)
  ) {
    return undefined
  }

  return value
}

export function buildPublicSeoMeta({ data, state }) {
  const isNoindex = NOINDEX_STATES.has(state)
  const seo = data?.seo ?? {}
  const openGraph = seo.openGraph ?? {}
  const meta = {}
  const link = {}
  const title = !isNoindex
    ? firstPlainText(seo.title, data?.title, data?.page?.title)
    : undefined
  const description = !isNoindex
    ? firstPlainText(
      seo.description,
      data?.summary,
      data?.excerpt,
      data?.abstractText,
      data?.abstract,
      data?.page?.summary
    )
    : undefined

  if (description) {
    meta.description = { name: 'description', content: description }
  }

  const openGraphTitle = !isNoindex && plainText(openGraph.title)
  const openGraphDescription = !isNoindex && plainText(openGraph.description)
  const openGraphImage = !isNoindex && sameOriginPath(openGraph.imageUrl)

  if (openGraphTitle) {
    meta['og:title'] = { property: 'og:title', content: openGraphTitle }
  }

  if (openGraphDescription) {
    meta['og:description'] = {
      property: 'og:description',
      content: openGraphDescription
    }
  }

  if (openGraphImage) {
    meta['og:image'] = { property: 'og:image', content: openGraphImage }
  }

  if (isNoindex) {
    meta.robots = { name: 'robots', content: 'noindex' }
  }
  else {
    const canonicalPath = publicPath(
      data?.canonicalPath,
      data?.locale
    )

    if (canonicalPath) {
      link.canonical = { rel: 'canonical', href: canonicalPath }
    }

    for (const entry of data?.hreflang ?? []) {
      const locale = entry?.locale
      const path = publicPath(entry?.path, locale)

      if (path) {
        link[`alternate-${locale}`] = {
          rel: 'alternate',
          hreflang: locale,
          href: path
        }
      }
    }
  }

  return {
    ...(title ? { title } : {}),
    meta,
    link
  }
}

export function usePublicSeoMeta({ data, state }) {
  const resolveMeta = () => buildPublicSeoMeta({
    data: unref(data),
    state: unref(state)
  })

  useMeta(resolveMeta)

  if (import.meta.env.SSR) {
    const ssrContext = useSSRContext()

    watch([data, state], () => {
      ssrContext.__qMetaList.push(resolveMeta())
    }, { flush: 'sync' })
  }
}
