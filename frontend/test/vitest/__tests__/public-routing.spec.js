import { describe, expect, it } from 'vitest'
import routes from 'src/router/routes'

const M1_CHILDREN = [
  'about',
  'research',
  'skills',
  'resume',
  'blog',
  'blog/:slug',
  'portfolio',
  'portfolio/:slug',
  'publications',
  'publications/:slug',
  'contact'
]

function getLocaleRoute(locale) {
  return routes.find((route) => route.path === `/${locale}`)
}

function collectDescendantRoutes(route) {
  const descendants = []

  function walk(children = []) {
    for (const child of children) {
      descendants.push(child)
      walk(child.children)
    }
  }

  walk(route?.children)
  return descendants
}

function collectChildPaths(route) {
  return collectDescendantRoutes(route).map((child) => child.path)
}

function expectInheritedLocaleMetadata(route, locale, direction) {
  for (const child of collectDescendantRoutes(route)) {
    expect(child.meta?.locale ?? route.meta?.locale).toBe(locale)
    expect(child.meta?.direction ?? route.meta?.direction).toBe(direction)
  }
}

describe('public routing contract', () => {
  it('redirects root to /language', () => {
    const root = routes.find((route) => route.path === '/')
    expect(root).toBeDefined()
    expect(root.redirect).toBe('/language')
  })

  it('has a /language route', () => {
    const languageRoute = routes.find((route) => route.path === '/language')
    expect(languageRoute).toBeDefined()
  })

  it('has /fa locale root with RTL meta', () => {
    const faRoute = getLocaleRoute('fa')
    expect(faRoute).toBeDefined()
    expect(faRoute.meta?.locale).toBe('fa')
    expect(faRoute.meta?.direction).toBe('rtl')
  })

  it('has /en locale root with LTR meta', () => {
    const enRoute = getLocaleRoute('en')
    expect(enRoute).toBeDefined()
    expect(enRoute.meta?.locale).toBe('en')
    expect(enRoute.meta?.direction).toBe('ltr')
  })

  describe('Persian (/fa) M1 child routes', () => {
    it('contains all required M1 children under /fa', () => {
      const childPaths = collectChildPaths(getLocaleRoute('fa'))

      for (const required of M1_CHILDREN) {
        expect(childPaths).toContain(required)
      }
    })

    it('inherits fa/rtl metadata on every /fa descendant', () => {
      expectInheritedLocaleMetadata(getLocaleRoute('fa'), 'fa', 'rtl')
    })
  })

  describe('English (/en) M1 child routes', () => {
    it('contains all required M1 children under /en', () => {
      const childPaths = collectChildPaths(getLocaleRoute('en'))

      for (const required of M1_CHILDREN) {
        expect(childPaths).toContain(required)
      }
    })

    it('inherits en/ltr metadata on every /en descendant', () => {
      expectInheritedLocaleMetadata(getLocaleRoute('en'), 'en', 'ltr')
    })
  })

  describe('translation-unavailable route', () => {
    it('has a translation-unavailable route under /fa', () => {
      expect(collectChildPaths(getLocaleRoute('fa')))
        .toContain('translation-unavailable')
    })

    it('has a translation-unavailable route under /en', () => {
      expect(collectChildPaths(getLocaleRoute('en')))
        .toContain('translation-unavailable')
    })
  })

  describe('not-found route', () => {
    it('has a not-found catch-all route under /fa', () => {
      expect(collectChildPaths(getLocaleRoute('fa')))
        .toContain(':pathMatch(.*)*')
    })

    it('has a not-found catch-all route under /en', () => {
      expect(collectChildPaths(getLocaleRoute('en')))
        .toContain(':pathMatch(.*)*')
    })
  })

  describe('route meta direction consistency', () => {
    it('ensures /fa root propagates RTL direction to layout', () => {
      const faRoute = getLocaleRoute('fa')
      expect(faRoute.meta?.locale).toBe('fa')
      expect(faRoute.meta?.direction).toBe('rtl')
    })

    it('ensures /en root propagates LTR direction to layout', () => {
      const enRoute = getLocaleRoute('en')
      expect(enRoute.meta?.locale).toBe('en')
      expect(enRoute.meta?.direction).toBe('ltr')
    })
  })
})