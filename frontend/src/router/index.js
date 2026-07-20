import { route } from 'quasar/wrappers'
import { createMemoryHistory, createRouter, createWebHistory } from 'vue-router'

import { useAdminAuthStore } from 'src/stores/adminAuth'

import { createAdminNavigationGuard } from './adminNavigationGuard'
import routes from './routes'

export default route(({ store }) => {
  const createHistory = process.env.SERVER ? createMemoryHistory : createWebHistory
  const router = createRouter({
    history: createHistory(process.env.VUE_ROUTER_BASE),
    routes,
    scrollBehavior: () => ({ left: 0, top: 0 })
  })

  router.beforeEach(createAdminNavigationGuard(
    () => useAdminAuthStore(store),
    process.env.SERVER
  ))

  return router
})
