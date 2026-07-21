import { primeCsrfToken } from './csrf'

export const ADMIN_LOGIN_ENDPOINT = '/api/v1/auth/login'
export const ADMIN_ME_ENDPOINT = '/api/v1/auth/me'
export const ADMIN_LOGOUT_ENDPOINT = '/api/v1/auth/logout'

export function createAdminAuthApi(httpClient) {
  return {
    async login(credentials) {
      await primeCsrfToken(httpClient)
      const response = await httpClient.post(
        ADMIN_LOGIN_ENDPOINT,
        credentials
      )

      return response.data
    },

    async currentUser() {
      const response = await httpClient.get(ADMIN_ME_ENDPOINT)

      return response.data
    },

    async logout() {
      await primeCsrfToken(httpClient)
      await httpClient.post(ADMIN_LOGOUT_ENDPOINT)
    }
  }
}
