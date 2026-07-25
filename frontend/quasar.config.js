import { configure } from 'quasar/wrappers'

export default configure((ctx) => {
  const isSsrDevelopment = ctx.dev && ctx.mode.ssr
  const backendOrigin = process.env.TAHA_BACKEND_ORIGIN?.trim()

  if (isSsrDevelopment && !backendOrigin) {
    throw new Error('TAHA_BACKEND_ORIGIN must be set for SSR development.')
  }

  return {
    boot: [
      { path: 'theme', server: false },
      'i18n',
      'api'
    ],
    css: ['app.scss'],
    extras: ['material-icons'],
    build: {
      vueRouterMode: 'history'
    },
    framework: {
      config: {
        dark: false
      },
      plugins: ['Meta', 'Dark']
    },
    devServer: {
      strictPort: true,
      ...(isSsrDevelopment
        ? {
            proxy: {
              '/api': {
                target: backendOrigin,
                changeOrigin: true
              }
            }
          }
        : {})
    },
    ssr: {
      pwa: false,
      prodPort: 3000,
      middlewares: ['render']
    }
  }
})
