import { Quasar } from 'quasar'
import { mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import { beforeEach, describe, expect, it } from 'vitest'

import { i18n } from 'src/boot/i18n'
import PublicLayout from 'src/layouts/PublicLayout.vue'
import LanguagePage from 'src/pages/LanguagePage.vue'
import routes from 'src/router/routes'

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes
  })
}

async function mountPublicLayoutAt(path) {
  const router = createTestRouter()
  await router.push(path)
  await router.isReady()

  return mount(PublicLayout, {
    global: {
      plugins: [Quasar, router, i18n]
    }
  })
}

describe('frontend foundation', () => {
  beforeEach(() => {
    i18n.global.locale.value = 'en'
  })

  it('contains the current language, public, and admin entry routes', () => {
    const paths = routes.map((route) => route.path)

    expect(paths).toEqual(expect.arrayContaining(['/language', '/fa', '/en', '/admin']))
  })

  it('redirects the root route to the language selector', () => {
    const rootRoute = routes.find((route) => route.path === '/')

    expect(rootRoute.redirect).toBe('/language')
  })

  it('renders the existing Persian and English language choices', () => {
    const router = createTestRouter()
    const wrapper = mount(LanguagePage, {
      global: {
        plugins: [Quasar, router, i18n]
      }
    })

    expect(wrapper.text()).toContain('Persian')
    expect(wrapper.text()).toContain('English')
  })

  it('uses RTL for the current Persian public route', async () => {
    const wrapper = await mountPublicLayoutAt('/fa')

    expect(wrapper.get('main').attributes('dir')).toBe('rtl')
    expect(wrapper.get('main').attributes('lang')).toBe('fa')
  })

  it('uses LTR for the current English public route', async () => {
    const wrapper = await mountPublicLayoutAt('/en')

    expect(wrapper.get('main').attributes('dir')).toBe('ltr')
    expect(wrapper.get('main').attributes('lang')).toBe('en')
  })

  it('initializes translations for Persian and English', () => {
    expect(i18n.global.availableLocales).toEqual(expect.arrayContaining(['fa', 'en']))
  })
})
