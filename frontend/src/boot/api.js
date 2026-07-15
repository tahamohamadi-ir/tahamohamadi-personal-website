import { boot } from 'quasar/wrappers'

import { installApiContext } from 'src/services/apiContext'

export default boot(({ app, ssrContext }) => {
  return installApiContext({
    app,
    ssrContext,
    isServer: Boolean(process.env.SERVER),
    env: process.env
  })
})
