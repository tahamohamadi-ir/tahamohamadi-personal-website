import { configure } from 'quasar/wrappers'

export default configure(() => ({
  boot: ['i18n', 'pinia'],
  css: ['app.scss'],
  extras: ['material-icons'],
  build: {
    vueRouterMode: 'history'
  },
  framework: {
    plugins: []
  },
  ssr: {
    pwa: false,
    prodPort: 3000,
    middlewares: ['render']
  }
}))
