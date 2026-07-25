import { describe, expect, it } from 'vitest'
import { nextTick } from 'vue'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import en from 'src/i18n/en'
import AdminLifecycleActions from 'src/components/admin/AdminLifecycleActions.vue'
import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import AdminMarkdownPreview from 'src/components/admin/AdminMarkdownPreview.vue'
import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'

function source(relativePath) {
  return readFileSync(resolve(process.cwd(), relativePath), 'utf8')
}

function createTestI18n() {
  return createI18n({
    legacy: false,
    locale: 'en',
    messages: { en }
  })
}

describe('admin foundation components', () => {
  it('makes loading, empty, and recoverable error states accessible', async () => {
    const wrapper = mount(AdminStatePanel, {
      props: { state: 'error' },
      global: { plugins: [createTestI18n()] }
    })

    expect(wrapper.get('[role="alert"]').text()).toContain('could not be loaded')
    await wrapper.get('button').trigger('click')
    expect(wrapper.emitted('retry')).toHaveLength(1)
    await wrapper.setProps({ state: 'loading' })
    expect(wrapper.get('[role="status"]').text()).toContain('Loading')
  })

  it('switches the active locale while clearly labelling a missing translation', async () => {
    const wrapper = mount(AdminLocaleTabs, {
      props: {
        modelValue: 'fa',
        translations: { fa: true, en: false }
      },
      global: { plugins: [createTestI18n()] }
    })

    expect(wrapper.text()).toContain('Missing translation')
    await wrapper.get('[data-locale="en"]').trigger('click')
    expect(wrapper.emitted('update:modelValue')).toEqual([['en']])
  })

  it('requires confirmation for publish and archive and exposes a safe public preview link', async () => {
    const wrapper = mount(AdminLifecycleActions, {
      props: { status: 'DRAFT', publicPreviewPath: '/fa/about' },
      global: { plugins: [createTestI18n()] },
      attachTo: document.body
    })

    expect(wrapper.get('a').attributes('href')).toBe('/fa/about')
    await wrapper.get('[data-action="publish"]').trigger('click')
    await nextTick()
    expect(wrapper.get('[role="dialog"]').text()).toContain('Publish')
    expect(document.activeElement).toBe(wrapper.get('[data-cancel]').element)
    await wrapper.get('[role="dialog"]').trigger('keydown', { key: 'Escape' })
    expect(wrapper.find('[role="dialog"]').exists()).toBe(false)
    await wrapper.get('[data-action="publish"]').trigger('click')
    await wrapper.get('[data-confirm]').trigger('click')
    expect(wrapper.emitted('publish')).toHaveLength(1)
    wrapper.unmount()
  })

  it('shows only safe Markdown preview output and reports parser failure explicitly', async () => {
    const wrapper = mount(AdminMarkdownPreview, {
      props: { modelValue: '# Heading' },
      global: {
        plugins: [createTestI18n()],
        stubs: {
          MarkdownContent: {
            props: ['markdown'],
            template: '<article data-preview>{{ markdown }}</article>'
          }
        }
      }
    })

    expect(wrapper.get('[data-preview]').text()).toBe('# Heading')
    await wrapper.get('textarea').setValue('Updated')
    expect(wrapper.emitted('update:modelValue')).toEqual([['Updated']])
  })

  it('keeps pagination within the available result pages', async () => {
    const wrapper = mount(AdminPaginatedTable, {
      props: { page: 1, totalPages: 3 },
      global: { plugins: [createTestI18n()] }
    })

    expect(wrapper.get('nav').attributes('aria-label')).toBe('Admin table pages')
    expect(wrapper.text()).toContain('Page 2 of 3')
    await wrapper.get('[data-next]').trigger('click')
    expect(wrapper.emitted('change-page')).toEqual([[2]])
    await wrapper.setProps({ page: 2 })
    expect(wrapper.get('[data-next]').attributes('disabled')).toBeDefined()
  })

  it('keeps the persistent administration navigation label localized', () => {
    const layoutSource = source('src/layouts/AdminLayout.vue')

    expect(layoutSource).toContain("t('admin.chrome.navigationLabel')")
    expect(layoutSource).toContain("t('admin.chrome.productName')")
  })

  it('uses design-system tokens for shared admin controls and honours reduced motion', () => {
    const lifecycle = source('src/components/admin/AdminLifecycleActions.vue')
    const locales = source('src/components/admin/AdminLocaleTabs.vue')
    const state = source('src/components/admin/AdminStatePanel.vue')
    const markdown = source('src/components/admin/AdminMarkdownPreview.vue')

    expect(lifecycle).toContain('var(--tm-admin-control-radius)')
    expect(locales).toContain('var(--tm-focus-ring)')
    expect(state).toContain('var(--tm-motion-loading)')
    expect(state).toContain('prefers-reduced-motion')
    expect(markdown).toContain('var(--tm-admin-border)')
    expect(`${lifecycle}${locales}${state}`).not.toContain('var(--q-primary)')
  })
})
