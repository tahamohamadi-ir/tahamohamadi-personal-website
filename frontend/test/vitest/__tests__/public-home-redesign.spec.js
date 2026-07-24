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

const homeComponents = [
  'frontend/src/components/public/home/HomeHero.vue',
  'frontend/src/components/public/home/HomePractice.vue',
  'frontend/src/components/public/home/HomeFeatured.vue',
  'frontend/src/components/public/home/HomeResearchWriting.vue',
  'frontend/src/components/public/home/HomeClosing.vue'
]

const requiredHomeKeys = [
  'eyebrow',
  'headline',
  'lead',
  'viewWork',
  'exploreResearch',
  'currentFocusLabel',
  'currentFocus',
  'practiceLabel',
  'practiceValue',
  'availabilityLabel',
  'availability',
  'profileLabel',
  'featured',
  'socialLinks'
]

describe('public home editorial redesign', () => {
  it('creates five focused home composition units', () => {
    for (const componentPath of homeComponents) {
      const source = readProjectFile(componentPath)

      expect(source).toMatch(/<section\b/)
      expect(source).not.toMatch(/<main\b|<q-page\b/)
    }
  })

  it('composes the route-owned page while preserving API, SSR and safe Markdown', () => {
    const home = readProjectFile(
      'frontend/src/pages/public/PublicHomePage.vue'
    )

    for (const componentName of [
      'HomeHero',
      'HomePractice',
      'HomeFeatured',
      'HomeResearchWriting',
      'HomeClosing'
    ]) {
      expect(home).toMatch(
        new RegExp(`import ${componentName} .*${componentName}\\.vue`)
      )
      expect(home).toMatch(new RegExp(`<${componentName}\\b`))
    }

    expect(home.match(/<h1\b/g) ?? []).toHaveLength(1)
    expect(home).toMatch(/t\('shell\.siteName'\)/)
    expect(home).toMatch(/getHome\s*\(/)
    expect(home).toMatch(/MarkdownContent/)
    expect(home).toMatch(/featuredItems/)
    expect(home).toMatch(/socialLinks/)
    expect(home).toMatch(/props\.initialData\?\.locale/)
    expect(home).not.toMatch(/<style\b[^>]*\blang\s*=/)
    expect(home).not.toMatch(/v-html|innerHTML|outerHTML/)
  })

  it('uses the governed button component for public actions', () => {
    const sources = homeComponents
      .map((componentPath) => readProjectFile(componentPath))
      .join('\n')

    expect(sources).toMatch(/import TmButton /)
    expect(sources).toMatch(/<TmButton\b/)
    expect(sources).not.toMatch(/<q-btn\b/)
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

  it('keeps matching non-empty Persian and English editorial keys', () => {
    const dictionaries = [
      readProjectFile('frontend/src/i18n/en.js'),
      readProjectFile('frontend/src/i18n/fa.js')
    ]

    for (const dictionary of dictionaries) {
      for (const key of requiredHomeKeys) {
        expect(dictionary).toMatch(
          new RegExp(`\\b${key}\\s*:\\s*(['"]).+?\\1`)
        )
      }

      for (const key of ['work', 'writing']) {
        expect(dictionary).toMatch(
          new RegExp(`\\b${key}\\s*:\\s*(['"]).+?\\1`)
        )
      }
    }
  })

  it('preserves dynamic featured and social-link rendering', () => {
    const home = readProjectFile(
      'frontend/src/pages/public/PublicHomePage.vue'
    )
    const featured = readProjectFile(homeComponents[2])
    const closing = readProjectFile(homeComponents[4])

    expect(home).toMatch(/featuredPath/)
    expect(home).toMatch(/featuredEntries/)
    expect(featured).toMatch(/<router-link\b/)
    expect(featured).toMatch(/entry\.path/)
    expect(closing).toMatch(/socialLinks/)
    expect(closing).toMatch(/:href="link\.url"/)
    expect(closing).toMatch(/target="_blank"/)
  })

  it('uses editorial structure without template visual anti-patterns', () => {
    const sources = [
      readProjectFile(
        'frontend/src/pages/public/PublicHomePage.vue'
      ),
      readProjectFile(
        'frontend/src/components/public/SiteHeader.vue'
      ),
      readProjectFile(
        'frontend/src/components/public/SiteFooter.vue'
      ),
      ...homeComponents.map((componentPath) => (
        readProjectFile(componentPath)
      ))
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
      ...homeComponents.map((componentPath) => (
        readProjectFile(componentPath)
      ))
    ].join('\n')

    expect(sources).toMatch(/padding-inline\s*:/)
    expect(sources).toMatch(/border-block-(?:start|end)\s*:/)
    expect(sources).toMatch(/border-inline-start\s*:/)
    expect(sources).toMatch(/@media\s*\(\s*min-width\s*:/)
    expect(sources).toMatch(/<header\b/)
    expect(sources).toMatch(/<footer\b/)
    expect(sources).toMatch(/<nav\b/)
  })

  it('keeps the tablet home composition deliberate without empty archive tiles', () => {
    const header = readProjectFile(
      'frontend/src/components/public/SiteHeader.vue'
    )
    const hero = readProjectFile(
      'frontend/src/components/public/home/HomeHero.vue'
    )
    const closing = readProjectFile(
      'frontend/src/components/public/home/HomeClosing.vue'
    )

    expect(header).toMatch(/@media\s*\(min-width:\s*900px\)/)
    expect(header).toMatch(/@media\s*\(min-width:\s*1180px\)/)
    expect(hero).toMatch(
      /@media\s*\(min-width:\s*600px\)\s*and\s*\(max-width:\s*959px\)/
    )
    expect(hero).toMatch(
      /:global\(\[lang='fa'\]\s+\.home-hero__statement\)/
    )
    expect(closing).toMatch(/home-visual__archive-note/)
    expect(closing).not.toMatch(/home-visual__tile/)
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
