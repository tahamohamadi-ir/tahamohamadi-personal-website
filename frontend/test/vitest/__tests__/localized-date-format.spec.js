import { describe, expect, it } from 'vitest'

import { formatLocalizedDate } from 'src/utils/formatDate'

describe('localized public dates', () => {
  it('renders API dates in the active locale while preserving invalid values safely', () => {
    expect(formatLocalizedDate('2026-07-01T10:00:00Z', 'en'))
      .toContain('2026')
    expect(formatLocalizedDate('2026-07-01', 'fa'))
      .toMatch(/[\u06F0-\u06F9]/)
    expect(formatLocalizedDate('not-a-date', 'en')).toBe('not-a-date')
  })

  it('keeps date-only values stable regardless of the visitor time zone', () => {
    expect(formatLocalizedDate('2026-06-01', 'en'))
      .toContain('1 Jun 2026')
  })
})
