import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { describe, expect, it } from 'vitest'

const source = readFileSync(
  resolve(process.cwd(), 'src/pages/admin/AdminFeaturedPage.vue'),
  'utf8'
)
const english = readFileSync(resolve(process.cwd(), 'src/i18n/en.js'), 'utf8')
const persian = readFileSync(resolve(process.cwd(), 'src/i18n/fa.js'), 'utf8')

describe('featured-content administrative copy', () => {
  it('keeps visible operational copy in the bilingual message catalogues', () => {
    expect(source).toContain("useI18n")
    expect(source).toContain("t('admin.featured.title')")
    expect(source).toContain("t('admin.featured.conflict')")
    expect(source).toContain("t('admin.featured.selectPublishedTarget')")
    expect(source).not.toContain('label="Save featured content"')
    expect(english).toContain('featured: { title:')
    expect(persian).toContain('featured: { title:')
  })
})
