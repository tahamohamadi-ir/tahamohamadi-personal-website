import { route } from 'quasar/wrappers'
import { createMemoryHistory, createRouter, createWebHistory } from 'vue-router'

import { createRequestApiContext } from 'src/services/apiContext'
import { useAdminAuthStore } from 'src/stores/adminAuth'

import { createAdminNavigationGuard } from './adminNavigationGuard'
import routes from './routes'

export default route(({ store, ssrContext }) => {
  const isServer = process.env.SERVER
  const createHistory = isServer ? createMemoryHistory : createWebHistory
  const serverHttpClient = isServer
    ? createRequestApiContext({
        isServer,
        env: process.env,
        ssrContext
      }).httpClient
    : null
  const router = createRouter({
    history: createHistory(process.env.VUE_ROUTER_BASE),
    routes,
    scrollBehavior: () => ({ left: 0, top: 0 })
  })

  router.beforeEach(createAdminNavigationGuard(
    () => useAdminAuthStore(store),
    isServer,
    () => serverHttpClient
  ))

  return router
})
