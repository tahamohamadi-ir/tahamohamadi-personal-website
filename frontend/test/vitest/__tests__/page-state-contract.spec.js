import { existsSync } from 'node:fs'
import { resolve } from 'node:path'

import { mount } from '@vue/test-utils'
import { Quasar } from 'quasar'
import { createMemoryHistory, createRouter } from 'vue-router'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { i18n } from 'src/boot/i18n'

const workingDirectory = process.cwd()
const projectRoot = (
  workingDirectory.split(/[\\/]/).pop().toLowerCase() === 'frontend'
)
  ? resolve(workingDirectory, '..')
  : workingDirectory

const PAGE_STATE_PATH = 'frontend/src/components/public/PageState.vue'
const TRANSLATION_UNAVAILABLE_PATH = (
  'frontend/src/components/public/TranslationUnavailable.vue'
)

const CANONICAL_PAGE_STATES = [
  'loading',
  'empty',
  'recoverable-failure',
  'offline',
  'stale',
  'translation-unavailable'
]

async function loadContractComponent(projectRelativePath) {
  const filePath = resolve(projectRoot, projectRelativePath)

  if (!existsSync(filePath)) {
    throw new Error(`NOT_IMPLEMENTED:${projectRelativePath}`)
  }

  const componentModule = await vi.importActual(filePath)

  return componentModule.default
}

async function mountLocalized(component, { locale = 'en', props = {} } = {}) {
  i18n.global.locale.value = locale

  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/:pathMatch(.*)*', component: { template: '<div />' } }]
  })

  await router.push(`/${locale}`)
  await router.isReady()

  return mount(component, {
    props,
    global: {
      plugins: [Quasar, router, i18n]
    }
  })
}

async function mountPageState(state, locale = 'en') {
  const PageState = await loadContractComponent(PAGE_STATE_PATH)

  return mountLocalized(PageState, {
    locale,
    props: { state }
  })
}

function expectInlineSharedState(wrapper) {
  expect(wrapper.find('main').exists()).toBe(false)
  expect(wrapper.find('#main-content').exists()).toBe(false)
  expect(wrapper.find('.q-page').exists()).toBe(false)
  expect(wrapper.findAll('h1')).toHaveLength(0)
  expect(wrapper.findAll('[lang], [dir]')).toHaveLength(0)
  expect(wrapper.find('[role="dialog"]').exists()).toBe(false)
  expect(wrapper.find('[aria-modal="true"]').exists()).toBe(false)
}

afterEach(() => {
  i18n.global.locale.value = 'en'
})

describe('canonical PageState contract', () => {
  it('renders loading as a localized inline status without taking landmark or H1 ownership', async () => {
    const wrapper = await mountPageState(CANONICAL_PAGE_STATES[0])

    expectInlineSharedState(wrapper)
    expect(wrapper.get('[role="status"]').text()).toMatch(/loading/i)
  })

  it('renders empty as a visible localized status with a safe next-step explanation', async () => {
    const wrapper = await mountPageState(CANONICAL_PAGE_STATES[1])

    expectInlineSharedState(wrapper)
    expect(wrapper.get('[role="status"]').text()).toMatch(/no|empty|available/i)
  })

  it('renders recoverable failure as an alert with a keyboard-operable retry control', async () => {
    const wrapper = await mountPageState(CANONICAL_PAGE_STATES[2])

    expectInlineSharedState(wrapper)
    expect(wrapper.get('[role="alert"]').text()).toMatch(/try|again|failed|error/i)

    const retry = wrapper.get('button')
    expect(retry.attributes('type')).toBe('button')
    expect(retry.text()).toMatch(/retry|try again/i)

    await retry.trigger('click')
    expect(wrapper.emitted('retry')).toHaveLength(1)
  })

  it('renders offline as a localized transport status with recovery that does not claim missing content', async () => {
    const wrapper = await mountPageState(CANONICAL_PAGE_STATES[3])

    expectInlineSharedState(wrapper)
    expect(wrapper.get('[role="status"]').text()).toMatch(/offline|connection|network/i)

    const retry = wrapper.get('button')
    expect(retry.attributes('type')).toBe('button')
    expect(retry.text()).toMatch(/retry|try again/i)

    await retry.trigger('click')
    expect(wrapper.emitted('retry')).toHaveLength(1)
  })

  it('renders stale data as a clear status with a keyboard-operable refresh control', async () => {
    const wrapper = await mountPageState(CANONICAL_PAGE_STATES[4])

    expectInlineSharedState(wrapper)
    expect(wrapper.get('[role="status"]').text()).toMatch(/stale|outdated|refresh/i)

    const refresh = wrapper.get('button')
    expect(refresh.attributes('type')).toBe('button')
    expect(refresh.text()).toMatch(/refresh/i)

    await refresh.trigger('click')
    expect(wrapper.emitted('retry')).toHaveLength(1)
  })

  it('renders unavailable translation as a localized alert without source-locale fallback', async () => {
    const wrapper = await mountPageState(CANONICAL_PAGE_STATES[5], 'fa')

    expectInlineSharedState(wrapper)
    expect(wrapper.get('[role="alert"]').text()).toMatch(/[\u0600-\u06ff]/)
    expect(wrapper.text()).not.toContain('Translation unavailable')
  })

  it('keeps every canonical shared state localized in Persian without duplicating page direction ownership', async () => {
    for (const state of CANONICAL_PAGE_STATES) {
      const wrapper = await mountPageState(state, 'fa')

      expectInlineSharedState(wrapper)
      expect(wrapper.text()).toMatch(/[\u0600-\u06ff]/)
    }
  })
})

describe('canonical TranslationUnavailable contract', () => {
  it('uses the API-provided alternatePath unchanged as localized accessible recovery', async () => {
    const TranslationUnavailable = await loadContractComponent(
      TRANSLATION_UNAVAILABLE_PATH
    )
    const alternatePath = '/fa/portfolio/پروژه-نمونه'
    const wrapper = await mountLocalized(TranslationUnavailable, {
      locale: 'en',
      props: {
        alternatePath,
        targetLocale: 'fa'
      }
    })

    expectInlineSharedState(wrapper)
    expect(wrapper.get('[role="alert"]').text()).toMatch(/translation unavailable/i)

    const recovery = wrapper.get('a')
    expect(recovery.attributes('href')).toBe(alternatePath)
    expect(recovery.text()).not.toHaveLength(0)
  })

  it('uses only the target locale root without alternatePath and does not present it as a detail translation', async () => {
    const TranslationUnavailable = await loadContractComponent(
      TRANSLATION_UNAVAILABLE_PATH
    )
    const wrapper = await mountLocalized(TranslationUnavailable, {
      locale: 'en',
      props: {
        alternatePath: null,
        targetLocale: 'en'
      }
    })

    expectInlineSharedState(wrapper)

    const recovery = wrapper.get('a')
    expect(recovery.attributes('href')).toBe('/en')
    expect(recovery.text()).toMatch(/home/i)
    expect(recovery.text()).not.toMatch(/translation|translated/i)
  })
})
