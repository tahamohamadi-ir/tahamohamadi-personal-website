import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createI18n } from 'vue-i18n'

import AdminMediaPage from 'src/pages/admin/AdminMediaPage.vue'
import AdminMediaSelector from 'src/components/admin/AdminMediaSelector.vue'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { normalizeApiError } from 'src/services/httpClient'
import en from 'src/i18n/en'

const { primeCsrfToken } = vi.hoisted(() => ({
  primeCsrfToken: vi.fn()
}))

vi.mock('src/services/csrf', () => ({
  primeCsrfToken
}))

const qFileStub = {
  emits: ['update:modelValue'],
  template: '<input type="file" @change="$emit(\'update:modelValue\', $event.target.files[0])">'
}

const qFormStub = {
  emits: ['submit'],
  template: '<form @submit.prevent="$emit(\'submit\', $event)"><slot /></form>'
}

function createTestI18n() {
  return createI18n({ legacy: false, locale: 'en', messages: { en } })
}

function mountMediaPage(httpClient) {
  return mount(AdminMediaPage, {
    global: {
      plugins: [createTestI18n()],
      provide: { [HTTP_CLIENT_KEY]: httpClient },
      stubs: {
        QPage: { template: '<main><slot /></main>' },
        QForm: qFormStub,
        QFile: qFileStub,
        QInput: { props: ['modelValue'], emits: ['update:modelValue'], template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)">' },
        QSelect: { template: '<div><slot /></div>' },
        QBtn: { template: '<button><slot /></button>' },
        QBanner: { template: '<div role="alert"><slot /></div>' },
        QLinearProgress: true,
        QList: true,
        QItem: true,
        QItemSection: true,
        QItemLabel: true,
        QBadge: true,
        QDialog: true,
        QCard: true,
        QCardSection: true,
        QCardActions: true,
        AdminPaginatedTable: true,
        AdminStatePanel: true
      }
    }
  })
}

function selectFile(input, file) {
  Object.defineProperty(input.element, 'files', { configurable: true, value: [file] })
  return input.trigger('change')
}

describe('admin media upload limits', () => {
  beforeEach(() => {
    primeCsrfToken.mockReset()
    primeCsrfToken.mockResolvedValue(undefined)
  })

  it('exposes a shared policy that accepts a valid image and rejects an oversized image', async () => {
    const policy = await import('src/services/mediaUploadPolicy').catch(() => null)

    expect(policy).not.toBeNull()
    expect(policy.validateMediaUpload(new File([new Uint8Array(1024)], 'valid.png', { type: 'image/png' }))).toBeNull()
    expect(policy.validateMediaUpload(new File([new Uint8Array(10 * 1024 * 1024 + 1)], 'large.png', { type: 'image/png' })))
      .toBe('File exceeds the supported size limit.')
    expect(policy.validateMediaUpload(new File([new Uint8Array(1024)], 'valid.pdf', { type: 'application/pdf' }))).toBeNull()
    expect(policy.validateMediaUpload(new File([new Uint8Array(20 * 1024 * 1024 + 1)], 'large.pdf', { type: 'application/pdf' })))
      .toBe('File exceeds the supported size limit.')
  })

  it('rejects an oversized selection before network work and allows one valid retry', async () => {
    const httpClient = {
      get: vi.fn().mockResolvedValue({ data: { items: [], page: 0, totalPages: 0 } }),
      post: vi.fn().mockResolvedValue({ data: { id: 'media-id', mimeType: 'image/png' } })
    }
    const wrapper = mountMediaPage(httpClient)
    await flushPromises()
    const input = wrapper.get('input[type="file"]')

    await selectFile(input, new File([new Uint8Array(10 * 1024 * 1024 + 1)], 'large.png', { type: 'image/png' }))
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(primeCsrfToken).not.toHaveBeenCalled()
    expect(httpClient.post).not.toHaveBeenCalled()
    expect(wrapper.get('[role="alert"]').text()).toBe('File exceeds the supported size limit.')

    await selectFile(input, new File([new Uint8Array(1024)], 'valid.png', { type: 'image/png' }))
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(primeCsrfToken).toHaveBeenCalledTimes(1)
    expect(httpClient.post).toHaveBeenCalledTimes(1)
    wrapper.unmount()
  })

  it('maps raw edge and structured backend 413 responses to the same safe size error', () => {
    const edge = normalizeApiError({ response: { status: 413, data: '<html>nginx</html>' } })
    const backend = normalizeApiError({
      response: {
        status: 413,
        data: { code: 'MEDIA_TOO_LARGE', message: 'Uploaded file was rejected' }
      }
    })

    expect(edge).toMatchObject({ code: 'MEDIA_TOO_LARGE', message: 'File exceeds the supported size limit.' })
    expect(backend).toMatchObject({ code: 'MEDIA_TOO_LARGE', message: 'File exceeds the supported size limit.' })
  })

  it('sends the library search query as a bounded server-side list filter', async () => {
    const httpClient = {
      get: vi.fn().mockResolvedValue({ data: { items: [{ id: 'asset-id', originalFilename: 'portrait.png', mimeType: 'image/png', status: 'ACTIVE' }], page: 0, totalPages: 1 } })
    }
    const wrapper = mountMediaPage(httpClient)
    await flushPromises()

    await wrapper.findAll('input')[5].setValue('portrait')
    await flushPromises()

    expect(httpClient.get).toHaveBeenCalledWith('/api/v1/admin/media', {
      params: { page: 0, size: 20, query: 'portrait', type: undefined, status: undefined }
    })
    wrapper.unmount()
  })

  it('presents active media by filename and MIME type in reusable selectors', async () => {
    const httpClient = {
      get: vi.fn().mockResolvedValue({
        data: {
          items: [
            { id: 'asset-id', originalFilename: 'portrait.png', mimeType: 'image/png', status: 'ACTIVE' },
            { id: 'inactive-id', originalFilename: 'old.png', mimeType: 'image/png', status: 'ARCHIVED' }
          ]
        }
      })
    }
    const wrapper = mount(AdminMediaSelector, {
      global: {
        plugins: [createTestI18n()],
        provide: { [HTTP_CLIENT_KEY]: httpClient },
        stubs: {
          QSelect: { props: ['options'], template: '<output>{{ options[0]?.label }}</output>' },
          QInput: { props: ['modelValue'], emits: ['update:modelValue'], template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)">' },
          QBtn: { template: '<button><slot /></button>' },
          QForm: qFormStub,
          QFile: qFileStub,
          QLinearProgress: true
        }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('portrait.png (image/png)')
    expect(wrapper.text()).not.toContain('old.png')
    expect(httpClient.get).toHaveBeenCalledWith('/api/v1/admin/media', {
      params: {
        page: 0,
        size: 20,
        status: 'ACTIVE',
        query: undefined,
        type: undefined
      }
    })
    wrapper.unmount()
  })

  it('uploads a valid in-flow selection and selects the returned asset', async () => {
    const httpClient = {
      get: vi.fn().mockResolvedValue({ data: { items: [], page: 0, totalPages: 0 } }),
      post: vi.fn().mockResolvedValue({ data: { id: 'new-asset-id' } })
    }
    const wrapper = mount(AdminMediaSelector, {
      props: { allowedTypes: ['image'] },
      global: {
        plugins: [createTestI18n()],
        provide: { [HTTP_CLIENT_KEY]: httpClient },
        stubs: {
          QForm: qFormStub,
          QFile: qFileStub,
          QInput: { template: '<input>' },
          QSelect: { template: '<div />' },
          QBtn: { template: '<button><slot /></button>' },
          QLinearProgress: true
        }
      }
    })
    await flushPromises()

    await selectFile(wrapper.get('input[type="file"]'), new File([new Uint8Array(1024)], 'new.png', { type: 'image/png' }))
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(primeCsrfToken).toHaveBeenCalledWith(httpClient)
    expect(httpClient.post).toHaveBeenCalledWith('/api/v1/admin/media', expect.any(FormData), expect.any(Object))
    expect(wrapper.emitted('update:modelValue')).toContainEqual(['new-asset-id'])
    wrapper.unmount()
  })
})
