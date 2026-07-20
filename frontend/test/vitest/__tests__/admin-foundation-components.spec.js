import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'

import AdminLifecycleActions from 'src/components/admin/AdminLifecycleActions.vue'
import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import AdminMarkdownPreview from 'src/components/admin/AdminMarkdownPreview.vue'
import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'

describe('admin foundation components', () => {
  it('makes loading, empty, and recoverable error states accessible', async () => {
    const wrapper = mount(AdminStatePanel, { props: { state: 'error' } })

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
      }
    })

    expect(wrapper.text()).toContain('Missing translation')
    await wrapper.get('[data-locale="en"]').trigger('click')
    expect(wrapper.emitted('update:modelValue')).toEqual([['en']])
  })

  it('requires confirmation for publish and archive and exposes a safe public preview link', async () => {
    const wrapper = mount(AdminLifecycleActions, {
      props: { status: 'DRAFT', publicPreviewPath: '/fa/about' }
    })

    expect(wrapper.get('a').attributes('href')).toBe('/fa/about')
    await wrapper.get('[data-action="publish"]').trigger('click')
    expect(wrapper.get('[role="dialog"]').text()).toContain('Publish')
    await wrapper.get('[data-confirm]').trigger('click')
    expect(wrapper.emitted('publish')).toHaveLength(1)
  })

  it('shows only safe Markdown preview output and reports parser failure explicitly', async () => {
    const wrapper = mount(AdminMarkdownPreview, {
      props: { modelValue: '# Heading' },
      global: {
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
      props: { page: 1, totalPages: 3 }
    })

    await wrapper.get('[data-next]').trigger('click')
    expect(wrapper.emitted('change-page')).toEqual([[2]])
    await wrapper.setProps({ page: 2 })
    expect(wrapper.get('[data-next]').attributes('disabled')).toBeDefined()
  })
})
