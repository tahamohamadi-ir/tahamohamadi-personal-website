import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { describe, expect, it } from 'vitest'

const workingDirectory = process.cwd()
const projectRoot = (
  workingDirectory.split(/[\\/]/).pop().toLowerCase() === 'frontend'
)
  ? resolve(workingDirectory, '..')
  : workingDirectory

const readProjectFile = (projectRelativePath) => {
  const filePath = resolve(projectRoot, projectRelativePath)

  if (!existsSync(filePath)) {
    throw new Error(`NOT_IMPLEMENTED:${projectRelativePath}`)
  }

  return readFileSync(filePath, 'utf8')
}

describe('public home editorial redesign', () => {
  it('composes the route-owned home from CMS data while preserving API, SSR and safe Markdown', () => {
    const home = readProjectFile(
      'frontend/src/pages/public/PublicHomePage.vue'
    )

    expect(home).toMatch(/PageBlockRenderer/)
    expect(home).toMatch(/:hero-heading-level="1"/)
    expect(home).toMatch(/MarkdownContent/)
    expect(home).toMatch(/hasLegacyManagedContent/)
    expect(home).toMatch(/getHome\s*\(/)
    expect(home).toMatch(/socialLinks/)
    expect(home).toMatch(/props\.initialData\?\.locale/)
    expect(home).not.toMatch(/<style\b[^>]*\blang\s*=/)
    expect(home).not.toMatch(/v-html|innerHTML|outerHTML/)
  })

  it('uses the approved public navigation order and keeps mobile behavior', () => {
    const header = readProjectFile(
      'frontend/src/components/public/SiteHeader.vue'
    )
    const orderedKeys = [
      "'work'",
      "'research'",
      "'writing'",
      "'about'",
      "'resume'",
      "'contact'"
    ]

    let previousIndex = -1

    for (const key of orderedKeys) {
      const currentIndex = header.indexOf(`key: ${key}`)

      expect(currentIndex).toBeGreaterThan(previousIndex)
      previousIndex = currentIndex
    }

    expect(header).toMatch(/openNavigation|closeNavigation/)
    expect(header).toMatch(/event\.key\s*!==\s*['"]Escape['"]/)
    expect(header).toMatch(/LanguageSwitch/)
    expect(header).toMatch(/mobileTrigger\.value\?\.\$el\?\.focus\?\./)
  })

  it('uses editorial structure without template visual anti-patterns', () => {
    const sources = [
      readProjectFile(
        'frontend/src/pages/public/PublicHomePage.vue'
      ),
      readProjectFile(
        'frontend/src/components/public/PageBlockRenderer.vue'
      ),
      readProjectFile(
        'frontend/src/components/public/SiteHeader.vue'
      ),
      readProjectFile(
        'frontend/src/components/public/SiteFooter.vue'
      )
    ].join('\n')

    expect(sources).not.toMatch(/linear-gradient|radial-gradient/)
    expect(sources).not.toMatch(/particle|custom-cursor|scroll-hijack/i)
    expect(sources).not.toMatch(/<q-card\b|<q-list\b/)
    expect(sources).not.toMatch(/#[0-9a-f]{3,8}\b/i)
    expect(sources).not.toMatch(/border-radius\s*:\s*999/)
  })

  it('uses logical properties, responsive layout and semantic landmarks', () => {
    const sources = [
      readProjectFile(
        'frontend/src/components/public/SiteHeader.vue'
      ),
      readProjectFile(
        'frontend/src/components/public/SiteFooter.vue'
      ),
      readProjectFile('frontend/src/components/public/PageBlockRenderer.vue')
    ].join('\n')

    expect(sources).toMatch(/padding-inline\s*:/)
    expect(sources).toMatch(/border-block-(?:start|end)\s*:/)
    expect(sources).toMatch(/border-inline-start\s*:/)
    expect(sources).toMatch(/@media\s*\(\s*min-width\s*:/)
    expect(sources).toMatch(/<header\b/)
    expect(sources).toMatch(/<footer\b/)
    expect(sources).toMatch(/<nav\b/)
  })

  it('keeps the shell responsive without empty archive tiles', () => {
    const header = readProjectFile(
      'frontend/src/components/public/SiteHeader.vue'
    )
    expect(header).toMatch(/@media\s*\(min-width:\s*900px\)/)
    expect(header).toMatch(/@media\s*\(min-width:\s*1180px\)/)
  })
})
describe('public home isolated SSR harnesses', () => {
  it('does not install the full Quasar plugin in isolated SSR harnesses', () => {
    const harnesses = [
      readProjectFile(
        'frontend/test/vitest/__tests__/public-rich-content-pages.spec.js'
      ),
      readProjectFile(
        'frontend/test/vitest/__tests__/public-rich-content-ssr-bridge.spec.js'
      )
    ]

    for (const harness of harnesses) {
      expect(harness).toMatch(
        /app\.config\.globalProperties\.\$q\s*=/
      )
      expect(harness).not.toMatch(/app\.use\(Quasar\)/)
    }
  })
})
