import { createPinia } from 'pinia'
import { createSSRApp, h } from 'vue'
import { renderToString } from '@vue/server-renderer'
import { createMemoryHistory, createRouter, RouterView } from 'vue-router'
import { flushPromises } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { i18n } from 'src/boot/i18n'
import PublicHomePage from 'src/pages/public/PublicHomePage.vue'
import AboutPage from 'src/pages/public/AboutPage.vue'
import ResearchPage from 'src/pages/public/ResearchPage.vue'
import { PUBLIC_API_KEY } from 'src/services/apiContext'

const ROUTES = [
  {
    path: '/fa',
    component: PublicHomePage,
    meta: { locale: 'fa', direction: 'rtl' },
    expected: ['getHome', 'fa'],
    stateKey: 'public:fa:home:home'
  },
  {
    path: '/en/about',
    component: AboutPage,
    meta: { locale: 'en', direction: 'ltr' },
    expected: ['getPage', 'en', 'about'],
    stateKey: 'public:en:page:about'
  },
  {
    path: '/fa/research',
    component: ResearchPage,
    meta: { locale: 'fa', direction: 'rtl' },
    expected: ['getPage', 'fa', 'research'],
    stateKey: 'public:fa:page:research'
  }
]

function responseFor(route, suffix = '') {
  const page = {
    title: route.path,
    summary: `SSR summary for ${route.path}${suffix}.`,
    bodyMarkdown: `## SSR content for ${route.path}${suffix}`
  }

  return route.expected[0] === 'getHome' ? { page } : page
}

function createRouteApplication(api, pinia = createPinia()) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', redirect: ROUTES[0].path },
      ...ROUTES.map(({ path, component, meta }) => ({ path, component, meta }))
    ]
  })
  const app = createSSRApp({ render: () => h(RouterView) })

  app.use(pinia)
  app.use(router)
  app.use(i18n)
  app.provide(PUBLIC_API_KEY, api)

  return { app, pinia, router }
}

async function renderRoute(route, api) {
  i18n.global.locale.value = route.meta.locale
  const { app, pinia, router } = createRouteApplication(api)

  await router.push(route.path)
  await router.isReady()

  return {
    html: await renderToString(app),
    state: JSON.parse(JSON.stringify(pinia.state.value))
  }
}

describe('public rich-content SSR route bridge', () => {
  it('fetches each direct public rich-content route during SSR and serializes only its route-owned result', async () => {
    for (const route of ROUTES) {
      const response = responseFor(route)
      const api = {
        getHome: vi.fn().mockResolvedValue(response),
        getPage: vi.fn().mockResolvedValue(response)
      }

      const { html, state } = await renderRoute(route, api)

      expect(api[route.expected[0]]).toHaveBeenCalledTimes(1)
      expect(api[route.expected[0]]).toHaveBeenCalledWith(...route.expected.slice(1))
      expect(html).toContain(route.expected[0] === 'getHome'
        ? response.page.summary
        : response.summary)
      expect(Object.keys(state['public-route-data'].entries))
        .toEqual([route.stateKey])
      expect(JSON.stringify(state)).toContain(route.path)
      expect(JSON.stringify(state)).not.toContain('getHome')
      expect(JSON.stringify(state)).not.toContain('getPage')
    }
  })

  it('keeps concurrent SSR route data isolated to each request-scoped Pinia instance', async () => {
    const [homeRoute, , researchRoute] = ROUTES
    const homeResponse = responseFor(homeRoute, ' FIRST')
    const researchResponse = responseFor(researchRoute, ' SECOND')
    const homeApi = {
      getHome: vi.fn().mockResolvedValue(homeResponse),
      getPage: vi.fn()
    }
    const researchApi = {
      getHome: vi.fn(),
      getPage: vi.fn().mockResolvedValue(researchResponse)
    }

    const [home, research] = await Promise.all([
      renderRoute(homeRoute, homeApi),
      renderRoute(researchRoute, researchApi)
    ])

    expect(home.html).toContain(homeResponse.page.summary)
    expect(home.html).not.toContain(researchResponse.summary)
    expect(research.html).toContain(researchResponse.summary)
    expect(research.html).not.toContain(homeResponse.page.summary)
    expect(home.state['public-route-data'].entries).toHaveProperty(homeRoute.stateKey)
    expect(research.state['public-route-data'].entries)
      .toHaveProperty(researchRoute.stateKey)
  })

  it('restores SSR data during hydration without a duplicate request, then loads once for client navigation', async () => {
    const initialRoute = ROUTES[1]
    const initialResponse = responseFor(initialRoute)
    const serverApi = {
      getHome: vi.fn(),
      getPage: vi.fn().mockResolvedValue(initialResponse)
    }
    const server = await renderRoute(initialRoute, serverApi)
    const clientApi = {
      getHome: vi.fn(),
      getPage: vi.fn((locale, slug) => Promise.resolve({
        title: slug,
        summary: `Client summary for ${locale}/${slug}.`,
        bodyMarkdown: `## Client content for ${locale}/${slug}`
      }))
    }
    const clientPinia = createPinia()
    clientPinia.state.value = JSON.parse(JSON.stringify(server.state))
    const { app, router } = createRouteApplication(clientApi, clientPinia)
    const container = document.createElement('div')
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})

    try {
      i18n.global.locale.value = initialRoute.meta.locale
      container.innerHTML = server.html
      document.body.append(container)
      await router.push(initialRoute.path)
      await router.isReady()
      app.mount(container)
      await flushPromises()

      expect(clientApi.getPage).not.toHaveBeenCalled()
      expect(container.textContent).toContain(initialResponse.summary)
      expect(clientPinia.state.value['public-route-data'].entries).toEqual({})

      await router.push('/fa/research')
      await flushPromises()

      expect(clientApi.getPage).toHaveBeenCalledTimes(1)
      expect(clientApi.getPage).toHaveBeenCalledWith('fa', 'research')
      expect(container.textContent).toContain('Client summary for fa/research.')
      expect(consoleError.mock.calls.flat().join(' ')).not.toMatch(/hydration/i)
    }
    finally {
      app.unmount()
      container.remove()
      consoleError.mockRestore()
    }
  })

  it('serializes normalized SSR failure state without leaking raw request details', async () => {
    const route = ROUTES[1]
    const api = {
      getHome: vi.fn(),
      getPage: vi.fn().mockRejectedValue({
        response: {
          status: 404,
          data: {
            code: 'TRANSLATION_UNAVAILABLE',
            alternatePaths: ['/fa/pages/about'],
            internalException: 'do-not-serialize'
          }
        }
      })
    }

    const { html, state } = await renderRoute(route, api)
    const serializedState = JSON.stringify(state)

    expect(html).toContain('href="/fa/pages/about"')
    expect(serializedState).toContain('TRANSLATION_UNAVAILABLE')
    expect(serializedState).not.toContain('do-not-serialize')
  })
})

afterEach(() => {
  i18n.global.locale.value = 'en'
})
