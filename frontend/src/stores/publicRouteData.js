import { defineStore } from 'pinia'

export const usePublicRouteDataStore = defineStore('public-route-data', {
  state: () => ({
    entries: {}
  }),

  actions: {
    write(key, snapshot) {
      this.entries[key] = snapshot
    },

    remove(key) {
      delete this.entries[key]
    }
  }
})
