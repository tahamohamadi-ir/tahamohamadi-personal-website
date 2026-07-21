import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import AdminMediaPage from 'src/pages/admin/AdminMediaPage.vue'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { normalizeApiError } from 'src/services/httpClient'

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

function mountMediaPage(httpClient) {
  return mount(AdminMediaPage, {
    global: {
      provide: { [HTTP_CLIENT_KEY]: httpClient },
      stubs: {
        QPage: { template: '<main><slot /></main>' },
        QForm: qFormStub,
        QFile: qFileStub,
        QInput: { template: '<input>' },
        QBtn: { template: '<button><slot /></button>' },
        QBanner: { template: '<div role="alert"><slot /></div>' },
        QLinearProgress: true,
        QList: true,
        QItem: true,
        QItemSection: true,
        QItemLabel: true,
        QBadge: true,
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
})
