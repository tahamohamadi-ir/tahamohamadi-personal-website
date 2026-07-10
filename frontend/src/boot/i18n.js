import { boot } from 'quasar/wrappers'
import { createI18n } from 'vue-i18n'

import en from 'src/i18n/en'
import fa from 'src/i18n/fa'

export const i18n = createI18n({
  legacy: false,
  locale: 'en',
  fallbackLocale: 'en',
  messages: { en, fa }
})

export default boot(({ app }) => {
  app.use(i18n)
})
