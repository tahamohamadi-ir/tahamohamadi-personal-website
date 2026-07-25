import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { describe, expect, it } from 'vitest'

const source = readFileSync(
  resolve(process.cwd(), 'src/pages/admin/AdminContactMessagesPage.vue'),
  'utf8'
)

describe('admin contact message contract', () => {
  it('keeps the message body as plain text and uses the API sourceLanguage for direction', () => {
    expect(source).toContain("selected.value?.sourceLanguage === 'FA' ? 'rtl' : 'ltr'")
    expect(source).toContain(':dir="detailDirection"')
    expect(source).toContain('archiveConfirmationOpen')
    expect(source).toContain('<q-dialog v-model="archiveConfirmationOpen" persistent>')
    expect(source).not.toContain('v-html')
  })
})
