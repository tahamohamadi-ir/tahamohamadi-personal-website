import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

import { useAdminAuthStore } from 'src/stores/adminAuth'

function response(data) {
  return Promise.resolve({ data })
}

function failedRequest(status, code, message = 'Request failed.') {
  return Promise.reject({
    response: {
      status,
      data: { code, message }
    }
  })
}

function adminUser(role = 'ADMIN') {
  return {
    id: 'admin-id',
    displayName: 'Taha Mohamadi',
    roles: [role]
  }
}

beforeEach(() => setActivePinia(createPinia()))

describe('admin authentication store', () => {
  it('restores an ADMIN session from the existing current-user endpoint', async () => {
    const store = useAdminAuthStore()
    const http = { get: (path) => {
      expect(path).toBe('/api/v1/auth/me')
      return response(adminUser())
    } }

    await store.restoreSession(http)

    expect(store.isAdmin).toBe(true)
    expect(store.status).toBe('ready')
  })

  it('accepts SUPER_ADMIN but rejects an authenticated unsupported role', async () => {
    const superAdmin = useAdminAuthStore()
    await superAdmin.restoreSession({ get: () => response(adminUser('SUPER_ADMIN')) })
    expect(superAdmin.isAdmin).toBe(true)

    const member = useAdminAuthStore()
    member.restored = false
    await member.restoreSession({ get: () => response(adminUser('USER')) })
    expect(member.isAdmin).toBe(false)
    expect(member.status).toBe('forbidden')
    expect(member.error).toMatchObject({ status: 403, code: 'FORBIDDEN' })
  })

  it('clears an expired session when current-user returns 401', async () => {
    const store = useAdminAuthStore()
    store.user = adminUser()

    await store.restoreSession({
      get: () => failedRequest(401, 'UNAUTHENTICATED', 'Session expired.')
    })

    expect(store.user).toBeNull()
    expect(store.status).toBe('anonymous')
    expect(store.error).toMatchObject({ status: 401, code: 'UNAUTHENTICATED' })
  })

  it('primes CSRF before login, then stores the returned administrator', async () => {
    const requests = []
    const store = useAdminAuthStore()
    const http = {
      get: (path) => {
        requests.push(['get', path])
        return response({})
      },
      post: (path, body) => {
        requests.push(['post', path, body])
        return response(adminUser())
      }
    }

    await store.login({ email: 'admin@example.test', password: 'secret' }, http)

    expect(requests).toEqual([
      ['get', '/api/v1/auth/csrf'],
      ['post', '/api/v1/auth/login', {
        email: 'admin@example.test', password: 'secret'
      }]
    ])
    expect(store.isAdmin).toBe(true)
  })

  it('keeps invalid-login credentials out of browser storage and clears the session', async () => {
    const store = useAdminAuthStore()
    store.user = adminUser()
    const http = {
      get: () => response({}),
      post: () => failedRequest(401, 'INVALID_CREDENTIALS', 'Invalid credentials.')
    }

    await store.login({ email: 'admin@example.test', password: 'wrong' }, http)

    expect(store.user).toBeNull()
    expect(store.status).toBe('anonymous')
    expect(store.error).toMatchObject({ code: 'INVALID_CREDENTIALS' })
  })

  it('primes CSRF and always clears local session state on logout', async () => {
    const requests = []
    const store = useAdminAuthStore()
    store.user = adminUser()
    const http = {
      get: (path) => {
        requests.push(['get', path])
        return response({})
      },
      post: (path) => {
        requests.push(['post', path])
        return response({})
      }
    }

    await store.logout(http)

    expect(requests).toEqual([
      ['get', '/api/v1/auth/csrf'],
      ['post', '/api/v1/auth/logout']
    ])
    expect(store.user).toBeNull()
    expect(store.status).toBe('anonymous')
  })
})
