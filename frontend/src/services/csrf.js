export const CSRF_ENDPOINT = '/api/v1/auth/csrf'

export async function primeCsrfToken(httpClient) {
  await httpClient.get(CSRF_ENDPOINT)
}
