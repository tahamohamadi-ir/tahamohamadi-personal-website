import { onMounted, onServerPrefetch, ref, shallowRef, watch } from 'vue'

import { usePublicRouteDataStore } from 'src/stores/publicRouteData'
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
  initialData,
  ssrKey
}) {
  const routeData = ssrKey ? usePublicRouteDataStore() : null
  const resolveSsrKey = () => (
    typeof ssrKey === 'function' ? ssrKey() : ssrKey
  )
  const initialSsrKey = ssrKey ? resolveSsrKey() : null
  const restoredSnapshot = initialData === undefined && initialSsrKey
    ? routeData.entries[initialSsrKey] ?? null
    : null
  const data = shallowRef(initialData === undefined
    ? restoredSnapshot?.data ?? null
    : initialData)
  const state = ref(restoredSnapshot?.state ?? (
    typeof isEmpty === 'function' && isEmpty(data.value)
      ? 'empty'
      : null
  ))
  const error = ref(restoredSnapshot?.error ?? null)
  const hasInitialState = initialData !== undefined || restoredSnapshot !== null
  let operation = 0

  function saveSnapshot(key) {
    if (!routeData || !key) {
      return
    }

    routeData.write(key, {
      data: data.value,
      state: state.value,
      error: error.value
    })
  }

  async function execute({ refresh = false } = {}) {
    const currentOperation = ++operation
    const requestKey = ssrKey ? resolveSsrKey() : null
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
      saveSnapshot(requestKey)
    }
    catch (requestError) {
      if (currentOperation !== operation) {
        return
      }

      const normalizedError = normalizeApiError(requestError)

      error.value = normalizedError
      state.value = stateForError(normalizedError)
      saveSnapshot(requestKey)
    }
  }

  if (ssrKey) {
    onServerPrefetch(async () => {
      if (!hasInitialState) {
        await execute()
      }
    })

    onMounted(() => {
      if (restoredSnapshot !== null) {
        routeData.remove(initialSsrKey)
      }
    })

    if (typeof ssrKey === 'function') {
      watch(ssrKey, () => {
        void execute()
      })
    }
  }

  return {
    data,
    state,
    error,
    load: () => execute(),
    refresh: () => execute({ refresh: true }),
    hasInitialState
  }
}
