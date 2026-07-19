import { ref } from 'vue'
import { describe, expect, it, vi } from 'vitest'

const { useMeta } = vi.hoisted(() => ({ useMeta: vi.fn() }))

vi.mock('quasar', () => ({ useMeta }))

import {
  buildPublicSeoMeta,
  usePublicSeoMeta
} from 'src/composables/usePublicSeoMeta'

describe('public route SEO metadata', () => {
  it('re-evaluates DTO metadata when navigation replaces the route response', () => {
    const data = ref({
      locale: 'en',
      canonicalPath: '/en/blog/first',
      hreflang: [{ locale: 'en', path: '/en/blog/first' }],
      seo: { title: 'First', description: 'First description.' }
    })
    const state = ref(null)

    usePublicSeoMeta({ data, state })

    const resolveMeta = useMeta.mock.calls.at(-1)[0]
    expect(resolveMeta().title).toBe('First')

    data.value = {
      locale: 'fa',
      canonicalPath: '/fa/blog/دوم',
      hreflang: [{ locale: 'fa', path: '/fa/blog/دوم' }],
      seo: { title: 'دوم', description: 'شرح دوم.' }
    }

    expect(resolveMeta().title).toBe('دوم')
    expect(resolveMeta().link.canonical.href).toBe('/fa/blog/دوم')
  })

  it('uses only the public response SEO, canonical, and translated-route fields', () => {
    const meta = buildPublicSeoMeta({
      state: null,
      data: {
        locale: 'en',
        canonicalPath: '/en/blog/evidence-first',
        hreflang: [
          { locale: 'en', path: '/en/blog/evidence-first' },
          { locale: 'fa', path: '/fa/blog/شواهد-نخست' }
        ],
        seo: {
          title: 'Evidence first',
          description: 'A <plain-text> description.',
          openGraph: {
            title: 'Evidence first on the blog',
            description: 'A verified summary.',
            imageUrl: '/api/v1/public/media/evidence-first'
          }
        }
      }
    })

    expect(meta.title).toBe('Evidence first')
    expect(meta.meta.description).toEqual({
      name: 'description',
      content: 'A <plain-text> description.'
    })
    expect(meta.meta.robots).toBeUndefined()
    expect(meta.meta['og:title']).toEqual({
      property: 'og:title',
      content: 'Evidence first on the blog'
    })
    expect(meta.meta['og:image']).toEqual({
      property: 'og:image',
      content: '/api/v1/public/media/evidence-first'
    })
    expect(meta.link.canonical).toEqual({
      rel: 'canonical',
      href: '/en/blog/evidence-first'
    })
    expect(meta.link['alternate-en']).toEqual({
      rel: 'alternate',
      hreflang: 'en',
      href: '/en/blog/evidence-first'
    })
    expect(meta.link['alternate-fa']).toEqual({
      rel: 'alternate',
      hreflang: 'fa',
      href: '/fa/blog/شواهد-نخست'
    })
  })

  it('does not advertise invalid paths, absent translations, or invented Open Graph content', () => {
    const meta = buildPublicSeoMeta({
      state: null,
      data: {
        canonicalPath: 'https://untrusted.example/en/blog/evidence-first',
        hreflang: [
          { locale: 'en', path: '/en/blog/evidence-first' },
          { locale: 'fa', path: 'javascript:alert(1)' },
          { locale: 'de', path: '/de/blog/evidence-first' }
        ],
        seo: {
          title: null,
          description: null,
          openGraph: {
            title: null,
            description: null,
            imageUrl: null
          }
        }
      }
    })

    expect(meta.title).toBeUndefined()
    expect(meta.meta.description).toBeUndefined()
    expect(meta.meta['og:title']).toBeUndefined()
    expect(meta.meta['og:image']).toBeUndefined()
    expect(meta.link.canonical).toBeUndefined()
    expect(meta.link['alternate-en']).toEqual({
      rel: 'alternate',
      hreflang: 'en',
      href: '/en/blog/evidence-first'
    })
    expect(meta.link['alternate-fa']).toBeUndefined()
    expect(meta.link['alternate-de']).toBeUndefined()
  })

  it('uses a public response title or summary only when the optional SEO fields are absent', () => {
    const meta = buildPublicSeoMeta({
      state: null,
      data: {
        locale: 'fa',
        canonicalPath: '/fa/about',
        hreflang: [{ locale: 'fa', path: '/fa/about' }],
        title: 'درباره',
        summary: 'معرفی کوتاه',
        seo: { title: null, description: null }
      }
    })

    expect(meta.title).toBe('درباره')
    expect(meta.meta.description).toEqual({
      name: 'description',
      content: 'معرفی کوتاه'
    })
  })

  it('marks translation-unavailable and not-found states as noindex without publishing route links', () => {
    for (const state of ['translation-unavailable', 'not-found']) {
      const meta = buildPublicSeoMeta({
        state,
        data: {
          canonicalPath: '/en/blog/evidence-first',
          hreflang: [{ locale: 'en', path: '/en/blog/evidence-first' }],
          seo: { title: 'Evidence first', description: 'A summary.' }
        }
      })

      expect(meta.meta.robots).toEqual({ name: 'robots', content: 'noindex' })
      expect(meta.link.canonical).toBeUndefined()
      expect(meta.link['alternate-en']).toBeUndefined()
    }
  })
})
