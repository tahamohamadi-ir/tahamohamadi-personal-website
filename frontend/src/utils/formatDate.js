const DATE_ONLY_PATTERN = /^\d{4}-\d{2}-\d{2}$/

function normalizeDate(value) {
  if (typeof value !== 'string' || value.trim().length === 0) {
    return null
  }

  const normalizedValue = DATE_ONLY_PATTERN.test(value)
    ? `${value}T00:00:00Z`
    : value
  const date = new Date(normalizedValue)

  return Number.isNaN(date.valueOf()) ? null : date
}

export function formatLocalizedDate(value, locale) {
  const date = normalizeDate(value)

  if (!date) {
    return value
  }

  return new Intl.DateTimeFormat(
    locale === 'fa' ? 'fa-IR-u-ca-persian' : 'en-GB',
    {
      dateStyle: 'medium',
      timeZone: 'UTC'
    }
  ).format(date)
}
