import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { describe, expect, it } from 'vitest'

const workingDirectory = process.cwd()
const projectRoot = (
  workingDirectory.split(/[\\/]/).pop().toLowerCase() === 'frontend'
)
  ? resolve(workingDirectory, '..')
  : workingDirectory

const dictionaryPaths = [
  'frontend/src/i18n/en.js',
  'frontend/src/i18n/fa.js'
]

const mojibakeMarkers = /[\u00c3\u00c2\u00e2\u00d8\u00d9\u00db\ufffd]/u

const readUtf8 = (projectRelativePath) => {
  const bytes = readFileSync(resolve(projectRoot, projectRelativePath))

  return new TextDecoder('utf-8', { fatal: true }).decode(bytes)
}

describe('public locale dictionary encoding', () => {
  it('stores the English and Persian dictionaries as clean UTF-8 source', () => {
    const dictionaries = dictionaryPaths.map(readUtf8)

    for (const dictionary of dictionaries) {
      expect(mojibakeMarkers.test(dictionary)).toBe(false)
    }

    expect(dictionaries[0]).toContain(
      'siteDescriptor: \'Human-Centered Systems Builder\''
    )
    expect(dictionaries[1]).toContain(
      'siteName: \'\u0637\u0647 \u0645\u062d\u0645\u062f\u06cc\''
    )
  })
})
