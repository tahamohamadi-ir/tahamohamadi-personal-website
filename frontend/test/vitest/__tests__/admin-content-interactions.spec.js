import { describe, expect, it } from 'vitest'

import {
  canUsePublicPreview,
  createUnsavedChangesGuard,
  isMissingTranslation,
  isVersionConflict,
  mapValidationErrors,
  nextPage,
  supportedLifecycleActions,
  useAdminNotifications
} from 'src/composables/adminContentInteractions'

describe('admin content interactions', () => {
  it('maps backend field errors to their matching inputs', () => {
    expect(mapValidationErrors({
      fields: [
        { field: 'title', message: 'Title is required.' },
        { field: 'slug', message: 'Slug is already used.' }
      ]
    })).toEqual({
      title: 'Title is required.',
      slug: 'Slug is already used.'
    })
  })

  it('keeps an unavailable locale explicit instead of falling back', () => {
    expect(isMissingTranslation({ fa: true, en: false }, 'en')).toBe(true)
    expect(isMissingTranslation({ fa: true, en: false }, 'fa')).toBe(false)
  })

  it('limits lifecycle actions to transitions valid for the current status', () => {
    expect(supportedLifecycleActions('DRAFT')).toEqual(['publish', 'archive'])
    expect(supportedLifecycleActions('PUBLISHED')).toEqual(['archive'])
    expect(supportedLifecycleActions('ARCHIVED')).toEqual([])
  })

  it('identifies optimistic-lock conflicts without treating every error as a conflict', () => {
    expect(isVersionConflict({ status: 409, code: 'OPTIMISTIC_LOCK_CONFLICT' })).toBe(true)
    expect(isVersionConflict({ status: 409, code: 'CONFLICT' })).toBe(false)
  })

  it('only enables public previews for locale-prefixed public paths', () => {
    expect(canUsePublicPreview('/fa/about')).toBe(true)
    expect(canUsePublicPreview('/en/blog/example')).toBe(true)
    expect(canUsePublicPreview('/admin/pages')).toBe(false)
    expect(canUsePublicPreview('https://example.test/fa/about')).toBe(false)
  })

  it('prevents invalid page transitions and asks before discarding unsaved work', async () => {
    const guard = createUnsavedChangesGuard(async () => false)

    guard.markDirty()
    expect(await guard.confirmLeave()).toBe(false)
    guard.markSaved()
    expect(await guard.confirmLeave()).toBe(true)
    expect(nextPage(1, 3, 2)).toBe(2)
    expect(nextPage(1, 3, 1)).toBe(1)
  })

  it('makes save success and failure feedback explicit', () => {
    const notifications = useAdminNotifications()

    notifications.showSuccess('Saved.')
    expect(notifications.notification.value).toEqual({ type: 'success', message: 'Saved.' })
    notifications.showError('Save failed.')
    expect(notifications.notification.value).toEqual({ type: 'error', message: 'Save failed.' })
  })
})
