import { ref } from 'vue'

const LIFECYCLE_ACTIONS = Object.freeze({
  DRAFT: ['publish', 'archive'],
  PUBLISHED: ['archive'],
  ARCHIVED: []
})

export function mapValidationErrors(error) {
  if (!Array.isArray(error?.fields)) {
    return {}
  }

  return error.fields.reduce((mapped, fieldError) => {
    if (
      typeof fieldError?.field === 'string' &&
      typeof fieldError?.message === 'string'
    ) {
      mapped[fieldError.field] = fieldError.message
    }

    return mapped
  }, {})
}

export function isMissingTranslation(translations, locale) {
  return translations?.[locale] !== true
}

export function supportedLifecycleActions(status) {
  return LIFECYCLE_ACTIONS[status] ?? []
}

export function isVersionConflict(error) {
  return error?.status === 409 && error?.code === 'OPTIMISTIC_LOCK_CONFLICT'
}

export function canUsePublicPreview(path) {
  return typeof path === 'string' && /^\/(fa|en)(\/|$)/.test(path)
}

export function nextPage(currentPage, totalPages, requestedPage) {
  if (!Number.isInteger(requestedPage) || requestedPage < 0 || requestedPage >= totalPages) {
    return currentPage
  }

  return requestedPage
}

export function createUnsavedChangesGuard(confirmDiscard) {
  const isDirty = ref(false)

  return {
    isDirty,
    markDirty: () => { isDirty.value = true },
    markSaved: () => { isDirty.value = false },
    async confirmLeave() {
      return !isDirty.value || await confirmDiscard()
    }
  }
}

export function useAdminNotifications() {
  const notification = ref(null)

  function show(type, message) {
    notification.value = { type, message }
  }

  return {
    notification,
    showSuccess: (message) => show('success', message),
    showError: (message) => show('error', message),
    clear: () => { notification.value = null }
  }
}
