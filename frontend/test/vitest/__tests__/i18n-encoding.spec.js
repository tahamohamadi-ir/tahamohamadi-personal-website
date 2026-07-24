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
    expect(dictionaries[0]).toContain(
      'eyebrow: \'Engineer \u00b7 Researcher \u00b7 HCI Designer\''
    )
    expect(dictionaries[0]).toContain(
      'currentFocus: \'Human\u2013AI interaction, explainable systems, and wearable intelligence.\''
    )
    expect(dictionaries[0]).toContain(
      'practiceValue: \'Research \u2192 systems design \u2192 production software.\''
    )
    expect(dictionaries[0]).toContain(
      'title: \'Architecture \u2192 visual communication \u2192 software systems\''
    )
    expect(dictionaries[0]).toContain(
      'availability: \'Research collaboration \u00b7 Selected product work\''
    )
    expect(dictionaries[1]).toContain(
      'siteName: \'\u0637\u0647 \u0645\u062d\u0645\u062f\u06cc\''
    )
    expect(dictionaries[1]).toContain(
      'headline: \'\u0633\u06cc\u0633\u062a\u0645\u200c\u0647\u0627\u06cc \u067e\u06cc\u0686\u06cc\u062f\u0647 \u0631\u0627 \u0628\u0631\u0627\u06cc \u0627\u0646\u0633\u0627\u0646\u200c\u0647\u0627 \u067e\u0698\u0648\u0647\u0634\u060c \u0637\u0631\u0627\u062d\u06cc \u0648 \u067e\u06cc\u0627\u062f\u0647\u200c\u0633\u0627\u0632\u06cc \u0645\u06cc\u200c\u06a9\u0646\u0645.\''
    )
  })
})
