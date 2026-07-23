// Release one is intentionally light-only.
// Dark token infrastructure is available for internal QA, but no public
// preference or toggle is persisted until all page families pass visual QA.
import { Dark } from 'quasar'
import { boot } from 'quasar/wrappers'

export default boot(() => {
  Dark.set(false)
})
