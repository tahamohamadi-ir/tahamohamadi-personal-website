import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { Quasar } from 'quasar'
import { createSSRApp, h } from 'vue'
import { renderToString } from '@vue/server-renderer'
import { createMemoryHistory, createRouter, RouterView } from 'vue-router'
import { afterEach, describe, expect, it, vi } from 'vitest'

import routes from 'src/router/routes'
import { i18n } from 'src/boot/i18n'
import ContactPage from 'src/pages/public/ContactPage.vue'
import SkillsPage from 'src/pages/public/SkillsPage.vue'
import { HTTP_CLIENT_KEY, PUBLIC_API_KEY } from 'src/services/apiContext'

const skillResponse = (locale) => ({
  locale,
  items: [
    {
      key: 'vue',
      name: locale === 'fa' ? 'ویو' : 'Vue',
      description: locale === 'fa' ? 'توسعهٔ رابط کاربر' : 'User-interface development'
    },
    {
      key: 'java',
      name: 'Java',
      description: null
    }
  ],
  page: 0,
  size: 50,
  totalElements: 2,
  totalPages: 1
})

function createPageRouter(component, locale = 'en') {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { render: () => null } },
      {
        path: '/en/skills',
        component,
        meta: { locale: 'en', direction: 'ltr' }
      },
      {
        path: '/fa/skills',
        component,
        meta: { locale: 'fa', direction: 'rtl' }
      },
      {
        path: '/en/contact',
        component,
        meta: { locale: 'en', direction: 'ltr' }
      },
      {
        path: '/fa/contact',
        component,
        meta: { locale: 'fa', direction: 'rtl' }
      }
    ]
  })
}

async function mountPage(component, {
  locale = 'en',
  routePath,
  api,
  httpClient = { get: vi.fn().mockResolvedValue({}) },
  props = {},
  pinia = createPinia(),
  attachTo
} = {}) {
  i18n.global.locale.value = locale
  const router = createPageRouter(component)

  await router.push(routePath ?? `/${locale}/skills`)
  await router.isReady()

  return mount(component, {
    props,
    attachTo,
    global: {
      plugins: [Quasar, pinia, router, i18n],
      provide: {
        [PUBLIC_API_KEY]: api,
        [HTTP_CLIENT_KEY]: httpClient
      }
    }
  })
}

async function renderSkills(locale, api, pinia = createPinia()) {
  i18n.global.locale.value = locale
  const router = createPageRouter(SkillsPage)
  const app = createSSRApp({ render: () => h(RouterView) })

  app.use(pinia)
  app.use(router)
  app.use(i18n)
  app.provide(PUBLIC_API_KEY, api)

  await router.push(`/${locale}/skills`)
  await router.isReady()

  return {
    html: await renderToString(app),
    state: JSON.parse(JSON.stringify(pinia.state.value))
  }
}

async function renderContact(locale, api, httpClient) {
  i18n.global.locale.value = locale
  const router = createPageRouter(ContactPage)
  const app = createSSRApp({ render: () => h(RouterView) })

  app.use(router)
  app.use(i18n)
  app.provide(PUBLIC_API_KEY, api)
  app.provide(HTTP_CLIENT_KEY, httpClient)

  await router.push(`/${locale}/contact`)
  await router.isReady()

  return renderToString(app)
}

afterEach(() => {
  vi.restoreAllMocks()
})

function findNamedRoute(locale, pageKey) {
  const localeRoute = routes.find((route) => route.path === `/${locale}`)

  return localeRoute?.children.find(
    (route) => route.name === `${locale}-${pageKey}`
  )
}

describe('Skills and Contact route ownership', () => {
  it('assigns dedicated pages to the localized Skills and Contact routes', async () => {
    for (const locale of ['fa', 'en']) {
      for (const pageKey of ['skills', 'contact']) {
        const route = findNamedRoute(locale, pageKey)
        const component = await route.component()

        expect(component.default.name).toBe(
          `${pageKey === 'skills' ? 'Skills' : 'Contact'}Page`
        )
      }
    }
  })
})

describe('Skills page', () => {
  it('renders only the localized public API fields as a semantic list', async () => {
    const api = { getSkills: vi.fn().mockResolvedValue(skillResponse('en')) }
    const wrapper = await mountPage(SkillsPage, { api })

    await flushPromises()

    expect(api.getSkills).toHaveBeenCalledWith('en')
    expect(wrapper.findAll('h1')).toHaveLength(1)
    expect(wrapper.get('ul').findAll('li')).toHaveLength(2)
    expect(wrapper.text()).toContain('User-interface development')
    expect(wrapper.text()).not.toMatch(/proficiency|percent|level/i)
    expect(wrapper.html()).not.toMatch(/v-html|progress/i)
    wrapper.unmount()
  })

  it('uses canonical empty and recoverable-failure states, then retries', async () => {
    const api = {
      getSkills: vi.fn()
        .mockRejectedValueOnce({
          response: {
            status: 500,
            data: { code: 'INTERNAL_ERROR' }
          }
        })
        .mockResolvedValueOnce(skillResponse('en'))
    }
    const wrapper = await mountPage(SkillsPage, { api })

    await flushPromises()
    expect(wrapper.get('[role="alert"]').text()).toMatch(/unable to load/i)

    await wrapper.get('button').trigger('click')
    await flushPromises()
    expect(api.getSkills).toHaveBeenCalledTimes(2)
    expect(wrapper.text()).toContain('Vue')
    wrapper.unmount()

    const empty = await mountPage(SkillsPage, {
      api: { getSkills: vi.fn().mockResolvedValue({ items: [] }) }
    })
    await flushPromises()
    expect(empty.get('[role="status"]').text()).toMatch(/no content/i)
    empty.unmount()
  })

  it('serializes SSR data, avoids a hydration reload, and reloads after locale navigation', async () => {
    const serverApi = { getSkills: vi.fn().mockResolvedValue(skillResponse('en')) }
    const server = await renderSkills('en', serverApi)

    expect(serverApi.getSkills).toHaveBeenCalledWith('en')
    expect(server.html).toContain('User-interface development')
    expect(server.state['public-route-data'].entries).toHaveProperty('public:en:skills')

    const clientApi = {
      getSkills: vi.fn().mockImplementation((locale) => Promise.resolve(skillResponse(locale)))
    }
    const clientPinia = createPinia()
    clientPinia.state.value = server.state
    const wrapper = await mountPage(SkillsPage, {
      api: clientApi,
      pinia: clientPinia
    })

    await flushPromises()
    expect(clientApi.getSkills).not.toHaveBeenCalled()

    const router = wrapper.vm.$router
    await router.push('/fa/skills')
    await flushPromises()
    expect(clientApi.getSkills).toHaveBeenCalledWith('fa')
    expect(wrapper.text()).toContain('ویو')
    wrapper.unmount()
  })

  it('keeps concurrent SSR requests isolated by locale', async () => {
    const faApi = { getSkills: vi.fn().mockResolvedValue(skillResponse('fa')) }
    const enApi = { getSkills: vi.fn().mockResolvedValue(skillResponse('en')) }
    const [fa, en] = await Promise.all([
      renderSkills('fa', faApi),
      renderSkills('en', enApi)
    ])

    expect(fa.html).toContain('توسعهٔ رابط کاربر')
    expect(fa.html).not.toContain('User-interface development')
    expect(en.html).toContain('User-interface development')
    expect(en.html).not.toContain('توسعهٔ رابط کاربر')
  })

  it('contains no HTML sink, browser global, or fabricated proficiency UI', () => {
    const source = readFileSync(resolve(
      process.cwd(),
      'src/pages/public/SkillsPage.vue'
    ), 'utf8')

    expect(source).not.toMatch(/v-html|innerHTML|outerHTML|progress|window|document/i)
    expect(source).not.toMatch(/<main\b|<q-page\b/i)
  })
})

describe('Contact page', () => {
  it('renders an accessible labeled form without submitting during render', async () => {
    const api = { submitContact: vi.fn() }
    const httpClient = { get: vi.fn() }
    const wrapper = await mountPage(ContactPage, {
      api,
      httpClient,
      routePath: '/en/contact'
    })

    expect(wrapper.findAll('h1')).toHaveLength(1)
    expect(wrapper.get('label[for="contact-name"]').text()).toBe('Name')
    expect(wrapper.get('input[name="name"]').attributes('maxlength')).toBe('200')
    expect(wrapper.get('input[name="email"]').attributes('maxlength')).toBe('320')
    expect(wrapper.get('textarea[name="message"]').attributes('maxlength')).toBe('10000')
    expect(api.submitContact).not.toHaveBeenCalled()
    expect(httpClient.get).not.toHaveBeenCalled()
    wrapper.unmount()
  })

  it('renders its form during SSR without performing a contact mutation', async () => {
    const api = { submitContact: vi.fn() }
    const httpClient = { get: vi.fn() }
    const html = await renderContact('fa', api, httpClient)

    expect(html).toContain('form')
    expect(html).toContain('پیام')
    expect(api.submitContact).not.toHaveBeenCalled()
    expect(httpClient.get).not.toHaveBeenCalled()
  })

  it('validates required values locally and focuses the first invalid input', async () => {
    const wrapper = await mountPage(ContactPage, {
      api: { submitContact: vi.fn() },
      routePath: '/en/contact',
      attachTo: document.body
    })

    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(wrapper.get('[role="alert"]').text()).toMatch(/correct/i)
    expect(wrapper.get('#contact-name-error').text()).toMatch(/enter your name/i)
    expect(document.activeElement).toBe(wrapper.get('#contact-name').element)
    wrapper.unmount()
  })

  it('primes CSRF, submits the exact contract, and clears only after success', async () => {
    const api = { submitContact: vi.fn().mockResolvedValue({ status: 'RECEIVED' }) }
    const httpClient = { get: vi.fn().mockResolvedValue({}) }
    const wrapper = await mountPage(ContactPage, {
      api,
      httpClient,
      routePath: '/en/contact'
    })

    await wrapper.get('#contact-name').setValue('Taha Mohamadi')
    await wrapper.get('#contact-email').setValue('taha@example.test')
    await wrapper.get('#contact-message').setValue('A Unicode-safe message.')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(httpClient.get).toHaveBeenCalledWith('/api/v1/auth/csrf')
    expect(api.submitContact).toHaveBeenCalledWith({
      name: 'Taha Mohamadi',
      email: 'taha@example.test',
      message: 'A Unicode-safe message.',
      language: 'en'
    })
    expect(wrapper.get('[role="status"]').text()).toMatch(/received/i)
    expect(wrapper.get('#contact-name').element.value).toBe('')
    expect(wrapper.get('#contact-email').element.value).toBe('')
    expect(wrapper.get('#contact-message').element.value).toBe('')
    wrapper.unmount()
  })

  it('prevents a duplicate pending submission and retains values after failure', async () => {
    let resolveSubmission
    const pendingSubmission = new Promise((resolve) => {
      resolveSubmission = resolve
    })
    const api = { submitContact: vi.fn().mockReturnValue(pendingSubmission) }
    const wrapper = await mountPage(ContactPage, {
      api,
      routePath: '/en/contact'
    })

    await wrapper.get('#contact-name').setValue('Taha Mohamadi')
    await wrapper.get('#contact-email').setValue('taha@example.test')
    await wrapper.get('#contact-message').setValue('A message')
    await wrapper.get('form').trigger('submit')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(api.submitContact).toHaveBeenCalledTimes(1)
    expect(wrapper.get('button[type="submit"]').attributes('disabled')).toBeDefined()
    expect(wrapper.get('[role="status"]').text()).toMatch(/sending/i)
    resolveSubmission({ status: 'RECEIVED' })
    await flushPromises()
    wrapper.unmount()

    const failure = await mountPage(ContactPage, {
      api: { submitContact: vi.fn().mockRejectedValue(new Error('private backend exception')) },
      routePath: '/en/contact'
    })
    await failure.get('#contact-name').setValue('طه')
    await failure.get('#contact-email').setValue('taha@example.test')
    await failure.get('#contact-message').setValue('پیام')
    await failure.get('form').trigger('submit')
    await flushPromises()

    expect(failure.get('[role="alert"]').text()).toMatch(/could not be sent/i)
    expect(failure.text()).not.toContain('private backend exception')
    expect(failure.get('#contact-name').element.value).toBe('طه')
    expect(failure.get('#contact-message').element.value).toBe('پیام')
    failure.unmount()
  })

  it('maps server field validation to safe localized errors without losing user input', async () => {
    const wrapper = await mountPage(ContactPage, {
      api: {
        submitContact: vi.fn().mockRejectedValue({
          response: {
            status: 400,
            data: {
              code: 'VALIDATION_ERROR',
              fields: [{ field: 'email', message: 'private backend detail' }]
            }
          }
        })
      },
      routePath: '/en/contact'
    })

    await wrapper.get('#contact-name').setValue('Taha Mohamadi')
    await wrapper.get('#contact-email').setValue('taha@example.test')
    await wrapper.get('#contact-message').setValue('A message')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(wrapper.get('#contact-email-error').text()).toMatch(/valid email/i)
    expect(wrapper.text()).not.toContain('private backend detail')
    expect(wrapper.get('#contact-name').element.value).toBe('Taha Mohamadi')
    expect(wrapper.get('#contact-email').element.value).toBe('taha@example.test')
    wrapper.unmount()
  })

  it('has no contact-information or HTML-sink invention in the page source', () => {
    const source = readFileSync(resolve(
      process.cwd(),
      'src/pages/public/ContactPage.vue'
    ), 'utf8')

    expect(source).not.toMatch(/getSocialLinks|getContact|mailto:|v-html|innerHTML|console\./i)
    expect(source).not.toMatch(/<main\b|<q-page\b/i)
  })
})
