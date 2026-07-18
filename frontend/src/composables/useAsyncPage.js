import { ref, shallowRef } from 'vue'

import { normalizeApiError } from 'src/services/httpClient'

function stateForError(error) {
  if (error.code === 'TRANSLATION_UNAVAILABLE') {
    return 'translation-unavailable'
  }

  if (error.code === 'NETWORK_ERROR') {
    return 'offline'
  }

  return 'recoverable-failure'
}

export function useAsyncPage({
  api,
  load: loadOperation,
  isEmpty,
  initialData
}) {
  const data = shallowRef(initialData === undefined ? null : initialData)
  const state = ref(
    typeof isEmpty === 'function' && isEmpty(data.value)
      ? 'empty'
      : null
  )
  const error = ref(null)
  let operation = 0

  async function execute({ refresh = false } = {}) {
    const currentOperation = ++operation
    const hasPriorData = data.value !== null

    state.value = refresh && hasPriorData
      ? 'stale'
      : 'loading'
    error.value = null

    try {
      const result = await loadOperation(api)

      if (currentOperation !== operation) {
        return
      }

      data.value = result
      state.value = (
        typeof isEmpty === 'function' && isEmpty(result)
      )
        ? 'empty'
        : null
    }
    catch (requestError) {
      if (currentOperation !== operation) {
        return
      }

      const normalizedError = normalizeApiError(requestError)

      error.value = normalizedError
      state.value = stateForError(normalizedError)
    }
  }

  return {
    data,
    state,
    error,
    load: () => execute(),
    refresh: () => execute({ refresh: true })
  }
}
