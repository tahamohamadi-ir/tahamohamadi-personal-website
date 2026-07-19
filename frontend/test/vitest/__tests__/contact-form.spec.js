import { describe, expect, it, vi } from 'vitest'

import {
  createHttpClient,
  XSRF_COOKIE_NAME,
  XSRF_HEADER_NAME
} from 'src/services/httpClient'

import {
  CSRF_ENDPOINT,
  primeCsrfToken
} from 'src/services/csrf'

import {
  CONTACT_ENDPOINT,
  createPublicApi
} from 'src/services/publicApi'

import {
  contactReceiptResponse,
  csrfErrorResponse
} from '../../fixtures/public-api'

const contactPayload = {
  name: 'Taha Mohamadi',
  email: 'taha@example.test',
  message: 'A test contact message.',
  language: 'en'
}

function createFakeHttpClient() {
  return {
    get: vi.fn(),
    post: vi.fn()
  }
}

describe('public contact CSRF contract', () => {
  it('uses the backend XSRF cookie and header names', () => {
    const client = createHttpClient()

    expect(client.defaults.withCredentials).toBe(true)
    expect(client.defaults.withXSRFToken).toBe(true)
    expect(client.defaults.xsrfCookieName)
      .toBe(XSRF_COOKIE_NAME)

    expect(client.defaults.xsrfHeaderName)
      .toBe(XSRF_HEADER_NAME)

    expect(XSRF_COOKIE_NAME).toBe('XSRF-TOKEN')
    expect(XSRF_HEADER_NAME).toBe('X-XSRF-TOKEN')
  })

  it('primes the CSRF cookie through the dedicated GET endpoint', async () => {
    const httpClient = createFakeHttpClient()

    httpClient.get.mockResolvedValue({
      status: 204,
      data: null
    })

    await expect(primeCsrfToken(httpClient))
      .resolves.toBeUndefined()

    expect(httpClient.get)
      .toHaveBeenCalledTimes(1)

    expect(httpClient.get)
      .toHaveBeenCalledWith(CSRF_ENDPOINT)
  })

  it('submits the exact contact payload and returns only the receipt', async () => {
    const httpClient = createFakeHttpClient()

    httpClient.post.mockResolvedValue({
      status: 201,
      data: contactReceiptResponse
    })

    const api = createPublicApi(httpClient)

    const receipt = await api.submitContact(contactPayload)

    expect(httpClient.post)
      .toHaveBeenCalledTimes(1)

    expect(httpClient.post)
      .toHaveBeenCalledWith(
        CONTACT_ENDPOINT,
        contactPayload
      )

    expect(receipt).toEqual(contactReceiptResponse)

    expect(Object.keys(receipt).sort()).toEqual([
      'id',
      'status',
      'submittedAt'
    ])

    expect(receipt).not.toHaveProperty('email')
    expect(receipt).not.toHaveProperty('message')
  })

  it('does not automatically retry a failed contact mutation', async () => {
    const httpClient = createFakeHttpClient()

    const csrfFailure = {
      response: {
        status: 403,
        data: csrfErrorResponse
      }
    }

    httpClient.post.mockRejectedValue(csrfFailure)

    const api = createPublicApi(httpClient)

    await expect(
      api.submitContact(contactPayload)
    ).rejects.toBe(csrfFailure)

    expect(httpClient.post)
      .toHaveBeenCalledTimes(1)
  })
})