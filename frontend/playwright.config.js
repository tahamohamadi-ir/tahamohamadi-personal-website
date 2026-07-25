import { defineConfig } from '@playwright/test'

const executablePath = process.env.TAHA_E2E_BROWSER_EXECUTABLE?.trim()
const recordVideo = process.env.TAHA_E2E_DISABLE_VIDEO !== 'true'

export default defineConfig({
  testDir: './test/e2e',
  timeout: 30_000,
  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 1 : 0,
  reporter: 'list',
  use: {
    baseURL: process.env.TAHA_E2E_BASE_URL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: recordVideo ? 'retain-on-failure' : 'off'
  },
  projects: [{
    name: 'chromium',
    use: {
      browserName: 'chromium',
      ...(executablePath
        ? { launchOptions: { executablePath } }
        : {})
    }
  }]
})
