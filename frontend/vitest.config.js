import { fileURLToPath, URL } from 'node:url'

import { quasar, transformAssetUrls } from '@quasar/vite-plugin'
import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vitest/config'

const fromRoot = (path) => fileURLToPath(new URL(path, import.meta.url))

export default defineConfig({
  plugins: [
    vue({
      template: { transformAssetUrls }
    }),
    quasar()
  ],
  resolve: {
    alias: {
      src: fromRoot('./src'),
      pages: fromRoot('./src/pages'),
      layouts: fromRoot('./src/layouts')
    }
  },
  test: {
    environment: 'happy-dom',
    include: ['test/vitest/__tests__/**/*.{test,spec}.js'],
    clearMocks: true,
    restoreMocks: true
  }
})
