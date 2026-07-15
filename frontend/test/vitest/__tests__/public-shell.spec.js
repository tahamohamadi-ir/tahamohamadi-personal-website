import { existsSync, readFileSync } from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'

const workingDirectory = process.cwd()

const projectRoot = (
  path.basename(workingDirectory).toLowerCase() === 'frontend'
)
  ? path.dirname(workingDirectory)
  : workingDirectory

const readProjectFile = (projectRelativePath) => {
  const filePath = path.resolve(projectRoot, projectRelativePath)

  if (!existsSync(filePath)) {
    throw new Error(`NOT_IMPLEMENTED:${projectRelativePath}`)
  }

  return readFileSync(filePath, 'utf8')
}

const expectSemanticTokens = (source) => {
  const requiredTokens = [
    '--tm-canvas',
    '--tm-surface',
    '--tm-text-primary',
    '--tm-text-secondary',
    '--tm-border-subtle',
    '--tm-action-primary',
    '--tm-link',
    '--tm-success',
    '--tm-warning',
    '--tm-danger',
    '--tm-space-',
    '--tm-radius-',
    '--tm-motion-',
    '--tm-focus-ring',
    '--tm-page-max-width',
    '--tm-prose-max-width',
    '--tm-gutter-',
    '--tm-control-min-size'
  ]

  for (const token of requiredTokens) {
    expect(source).toContain(token)
  }
}

const findObjectBody = (source, objectKey) => {
  const objectStart = source.search(
    new RegExp(`\\b${objectKey}\\s*:\\s*\\{`)
  )

  expect(objectStart).not.toBe(-1)

  const firstBrace = source.indexOf('{', objectStart)
  let depth = 0

  for (let index = firstBrace; index < source.length; index += 1) {
    if (source[index] === '{') {
      depth += 1
    }

    if (source[index] === '}') {
      depth -= 1

      if (depth === 0) {
        return source.slice(firstBrace + 1, index)
      }
    }
  }

  throw new Error(`Dictionary object is not closed: ${objectKey}`)
}

const expectNonEmptyStringValue = (source, key) => {
  const valuePattern = new RegExp(
    `\\b${key}\\s*:\\s*(['\"])\\s*[^'\"\\s][^'\"]*\\1`
  )

  expect(source).toMatch(valuePattern)
}

describe('localized public application shell source contracts', () => {
  it('requires foundation stylesheets and imports them from the global stylesheet', () => {
    const appStyles = readProjectFile('frontend/src/css/app.scss')

    readProjectFile('frontend/src/css/tokens.scss')
    readProjectFile('frontend/src/css/typography.scss')

    expect(appStyles).toMatch(/@(use|import)\s+['\"][^'\"]*tokens(?:\.scss)?['\"]/)
    expect(appStyles).toMatch(/@(use|import)\s+['\"][^'\"]*typography(?:\.scss)?['\"]/)
  })

  it('defines complete semantic tokens for the public design system', () => {
    const tokens = readProjectFile('frontend/src/css/tokens.scss')

    expectSemanticTokens(tokens)
    expect(tokens).toMatch(/--tm-control-min-size\s*:\s*44px\s*;/)
  })

  it('requires the shared public-shell component inventory', () => {
    const requiredComponents = [
      'frontend/src/components/public/SkipLink.vue',
      'frontend/src/components/public/SiteHeader.vue',
      'frontend/src/components/public/SiteFooter.vue',
      'frontend/src/components/public/LanguageSwitch.vue'
    ]

    for (const componentPath of requiredComponents) {
      readProjectFile(componentPath)
    }
  })

  it('keeps ownership of the only main landmark in PublicLayout', () => {
    const layout = readProjectFile('frontend/src/layouts/PublicLayout.vue')
    const mainElements = layout.match(/<main\b[^>]*>/g) ?? []

    expect(mainElements).toHaveLength(1)

    const [mainElement] = mainElements
    expect(mainElement).toMatch(/\bid\s*=\s*['\"]main-content['\"]/)
    expect(mainElement).toMatch(/\btabindex\s*=\s*['\"]-1['\"]/)

    const mainStart = layout.indexOf(mainElement)
    const mainEnd = layout.indexOf('</main>', mainStart)

    expect(mainEnd).toBeGreaterThan(mainStart)
    expect(layout.slice(mainStart, mainEnd)).toMatch(/<router-view\b/)

    for (const pagePath of [
      'frontend/src/pages/public/PublicRoutePlaceholderPage.vue',
      'frontend/src/pages/public/TranslationUnavailablePage.vue',
      'frontend/src/pages/public/NotFoundPage.vue'
    ]) {
      expect(readProjectFile(pagePath)).not.toMatch(/<main\b/)
    }
  })

  it('requires a localized, visibly focusable skip link to main content', () => {
    const skipLink = readProjectFile('frontend/src/components/public/SkipLink.vue')

    expect(skipLink).toMatch(/useI18n/)
    expect(skipLink).toMatch(/<a\b[^>]*\bhref\s*=\s*['\"]#main-content['\"][^>]*>[\s\S]*\{\{\s*t\(/)
    expect(skipLink).toMatch(/:focus-visible\s*\{[\s\S]*?(?:outline|box-shadow|text-decoration|transform)\s*:/)
  })

  it('requires semantic, localized and mobile-complete header navigation', () => {
    const header = readProjectFile('frontend/src/components/public/SiteHeader.vue')

    expect(header).toMatch(/<header\b/)
    expect(header).toMatch(/<nav\b/)
    expect(header).toMatch(/(?:aria-label|:aria-label)\s*=\s*['\"][^'\"]*t\(/)
    expect(header).toMatch(/<(?:a|router-link)\b[^>]*>[\s\S]*?\{\{\s*t\(/)
    expect(header).toMatch(/openNavigation|closeNavigation/)
    expect(header).toMatch(/LanguageSwitch/)
    expect(header).toMatch(/navigation\.contact/)
  })

  it('requires a localized semantic footer without hard-coded social URLs', () => {
    const footer = readProjectFile('frontend/src/components/public/SiteFooter.vue')

    expect(footer).toMatch(/<footer\b/)
    expect(footer).toMatch(/useI18n/)
    expect(footer).toMatch(/\bt\(/)
    expect(footer).not.toMatch(/https?:\/\//)
  })

  it('requires language switching from API-provided alternate paths only', () => {
    const languageSwitch = readProjectFile(
      'frontend/src/components/public/LanguageSwitch.vue'
    )

    expect(languageSwitch).toMatch(/\balternatePath\b/)
    expect(languageSwitch).not.toMatch(/\.replace(?:All)?\s*\(/)
    expect(languageSwitch).toMatch(/(?:aria-label|:aria-label)\s*=\s*['\"][^'\"]*t\(/)
  })

  it('keeps matching, non-empty English and Persian shell dictionary keys', () => {
    const dictionaries = [
      readProjectFile('frontend/src/i18n/en.js'),
      readProjectFile('frontend/src/i18n/fa.js')
    ]
    const shellKeys = [
      'skipToContent',
      'primaryNavigation',
      'openNavigation',
      'closeNavigation',
      'switchLanguage',
      'siteName'
    ]
    const navigationKeys = [
      'home',
      'about',
      'research',
      'skills',
      'resume',
      'blog',
      'portfolio',
      'publications',
      'contact'
    ]
    const footerKeys = ['navigationLabel', 'rights']

    for (const dictionary of dictionaries) {
      for (const key of shellKeys) {
        expectNonEmptyStringValue(dictionary, key)
      }

      const navigation = findObjectBody(dictionary, 'navigation')
      const footer = findObjectBody(dictionary, 'footer')

      for (const key of navigationKeys) {
        expectNonEmptyStringValue(navigation, key)
      }

      for (const key of footerKeys) {
        expectNonEmptyStringValue(footer, key)
      }
    }
  })

  it('requires focus, motion, responsive, target-size and logical-spacing safeguards', () => {
    const globalStyles = [
      readProjectFile('frontend/src/css/app.scss'),
      readProjectFile('frontend/src/css/tokens.scss'),
      readProjectFile('frontend/src/css/typography.scss')
    ].join('\n')

    expect(globalStyles).toMatch(/:focus-visible\s*\{/)
    expect(globalStyles).not.toMatch(/outline\s*:\s*none\b/)
    expect(globalStyles).toMatch(/@media\s*\(\s*prefers-reduced-motion\s*:\s*reduce\s*\)/)
    expect(globalStyles).toMatch(/1200px/)

    for (const gutter of ['16px', '24px', '32px']) {
      expect(globalStyles).toContain(gutter)
    }

    expect(globalStyles).toMatch(/44px/)
    expect(globalStyles).toMatch(/(?:margin|padding|inset|border)-(?:inline|block)(?:-start|-end)?\s*:/)
  })

  it('forbids raw hexadecimal colors in public-shell components', () => {
    const publicShellFiles = [
      'frontend/src/components/public/SkipLink.vue',
      'frontend/src/components/public/SiteHeader.vue',
      'frontend/src/components/public/SiteFooter.vue',
      'frontend/src/components/public/LanguageSwitch.vue',
      'frontend/src/layouts/PublicLayout.vue'
    ]

    for (const filePath of publicShellFiles) {
      expect(readProjectFile(filePath)).not.toMatch(/#[0-9a-fA-F]{3,6}(?![0-9a-fA-F])/)
    }
  })
})
