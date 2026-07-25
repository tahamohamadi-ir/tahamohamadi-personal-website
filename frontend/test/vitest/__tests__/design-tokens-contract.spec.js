import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const frontendRoot = process.cwd()
const repositoryRoot = resolve(frontendRoot, '..')

const tokenFilePath = resolve(
  repositoryRoot,
  'docs',
  'design-system',
  'design-tokens.json'
)

const quasarVariablesPath = resolve(
  frontendRoot,
  'src',
  'css',
  'quasar.variables.scss'
)

const quasarConfigPath = resolve(
  frontendRoot,
  'quasar.config.js'
)

const themeBootPath = resolve(
  frontendRoot,
  'src',
  'boot',
  'theme.js'
)

const themesPath = resolve(
  frontendRoot,
  'src',
  'css',
  'foundations',
  '_themes.scss'
)

const appStylesPath = resolve(
  frontendRoot,
  'src',
  'css',
  'app.scss'
)

const sassTokenPaths = [
  '_primitive.scss',
  '_semantic-light.scss',
  '_semantic-dark.scss',
  '_typography.scss',
  '_layout.scss',
  '_motion.scss'
].map((fileName) => resolve(
  frontendRoot,
  'src',
  'css',
  'tokens',
  fileName
))

function loadDesignTokens () {
  return JSON.parse(readFileSync(tokenFilePath, 'utf8'))
}

function escapeRegExp (value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

describe('design token contract', () => {
  it('keeps the approved release strategy', () => {
    const tokens = loadDesignTokens()

    expect(tokens.strategy).toMatchObject({
      releaseTheme: 'light',
      darkInfrastructure: true,
      publicDarkToggle: false,
      quasarFirst: true
    })
  })

  it('keeps the approved Quasar brand mapping', () => {
    const tokens = loadDesignTokens()

    expect(tokens.quasarBrand).toEqual({
      primary: '#0B6E69',
      secondary: '#2457D6',
      accent: '#D96C4A',
      dark: '#0B1117',
      positive: '#16805B',
      negative: '#B42318',
      info: '#1D70B8',
      warning: '#A15C00'
    })
  })

  it('maps the approved brand colors into Quasar Sass variables', () => {
    const tokens = loadDesignTokens()
    const source = readFileSync(quasarVariablesPath, 'utf8')

    for (const [name, value] of Object.entries(tokens.quasarBrand)) {
      const declaration = new RegExp(
        `\\$${escapeRegExp(name)}:\\s*${escapeRegExp(value)};`
      )

      expect(source).toMatch(declaration)
    }

    expect(source).toContain(
      'docs/design-system/design-tokens.json'
    )
  })

  it('provides focused Sass token modules', () => {
    for (const filePath of sassTokenPaths) {
      const source = readFileSync(filePath, 'utf8')

      expect(source.length).toBeGreaterThan(40)
      expect(source).toContain(
        'docs/design-system/design-tokens.json'
      )
    }
  })

  it('fixes release one to light mode without a public preference', () => {
    const bootSource = readFileSync(themeBootPath, 'utf8')
    const configSource = readFileSync(quasarConfigPath, 'utf8')

    expect(bootSource).toContain("import { Dark } from 'quasar'")
    expect(bootSource).toContain('Dark.set(false)')
    expect(bootSource).not.toContain('localStorage')
    expect(bootSource).not.toContain('matchMedia')
    expect(bootSource).not.toContain("Dark.set('auto')")

    expect(configSource).toContain(
      "{ path: 'theme', server: false }"
    )
    expect(configSource).toMatch(
      /config:\s*\{\s*dark:\s*false\s*\}/
    )
    expect(configSource).toMatch(
      /plugins:\s*\[\s*'Meta',\s*'Dark'\s*\]/
    )
  })

  it('defines complete light and dark body theme selectors', () => {
    const source = readFileSync(themesPath, 'utf8')

    expect(source).toContain('.body--light')
    expect(source).toContain('.body--dark')
    expect(source).toContain('--tm-#{$name}: #{$value};')
    expect(source).toContain(
      '@include tm-theme-properties(light.$tm-semantic-light);'
    )
    expect(source).toContain(
      '@include tm-theme-properties(dark.$tm-semantic-dark);'
    )
    expect(source).toContain('--tm-canvas: var(--tm-bg-page);')
    expect(source).toContain(
      '--tm-border-subtle: var(--tm-border-default);'
    )
    expect(source).not.toContain('#000000')
  })

  it('loads global Sass foundations in the governed order', () => {
    const source = readFileSync(appStylesPath, 'utf8')
    const orderedUses = [
      "@use './tokens.scss';",
      "@use './foundations/reset';",
      "@use './foundations/themes';",
      "@use './typography.scss';",
      "@use './tokens/layout';",
      "@use './foundations/accessibility';",
      "@use './foundations/rtl';",
      "@use './foundations/utilities';"
    ]

    const positions = orderedUses.map((statement) => {
      expect(source).toContain(statement)
      return source.indexOf(statement)
    })

    expect(positions).toEqual([...positions].sort((a, b) => a - b))
  })

  it('moves global concerns into focused foundation modules', () => {
    const resetSource = readFileSync(
      resolve(frontendRoot, 'src', 'css', 'foundations', '_reset.scss'),
      'utf8'
    )
    const accessibilitySource = readFileSync(
      resolve(
        frontendRoot,
        'src',
        'css',
        'foundations',
        '_accessibility.scss'
      ),
      'utf8'
    )
    const rtlSource = readFileSync(
      resolve(frontendRoot, 'src', 'css', 'foundations', '_rtl.scss'),
      'utf8'
    )
    const utilitiesSource = readFileSync(
      resolve(
        frontendRoot,
        'src',
        'css',
        'foundations',
        '_utilities.scss'
      ),
      'utf8'
    )
    const appSource = readFileSync(appStylesPath, 'utf8')

    expect(resetSource).toContain('box-sizing: border-box')
    expect(resetSource).toContain('min-inline-size: 320px')
    expect(accessibilitySource).toContain(':focus-visible')
    expect(accessibilitySource).toContain(
      '@media (prefers-reduced-motion: reduce)'
    )
    expect(rtlSource).toContain('.tm-detail-page__identifier')
    expect(rtlSource).toContain('unicode-bidi: isolate')
    expect(utilitiesSource).toContain('.tm-container')
    expect(utilitiesSource).toContain('--tm-gutter-desktop')

    expect(appSource).not.toContain('*::before')
    expect(appSource).not.toContain(':focus-visible')
    expect(appSource).not.toContain(
      '@media (prefers-reduced-motion: reduce)'
    )

    expect(appSource).toContain('.public-shell')
    expect(appSource).toContain('.tm-editorial-page')
    expect(appSource).toContain('.tm-detail-page')
    expect(appSource).toContain('.tm-text-link')
  })

  it('keeps prohibited visual patterns disabled', () => {
    const tokens = loadDesignTokens()

    expect(tokens.restrictions).toMatchObject({
      defaultGradient: false,
      permanentParticles: false,
      fakeMetrics: false,
      customCursor: false,
      scrollHijacking: false,
      emojiAsPrimaryIcons: false
    })
  })

  it('defines complete light and dark semantic themes', () => {
    const tokens = loadDesignTokens()

    const requiredSemanticTokens = [
      'bgPage',
      'bgSurface',
      'bgSurfaceSubtle',
      'textPrimary',
      'textSecondary',
      'borderDefault',
      'actionPrimary',
      'actionPrimaryHover',
      'actionSecondary',
      'link',
      'linkHover',
      'focusRing'
    ]

    for (const themeName of ['light', 'dark']) {
      expect(tokens.semantic).toHaveProperty(themeName)

      for (const tokenName of requiredSemanticTokens) {
        expect(tokens.semantic[themeName]).toHaveProperty(tokenName)
        expect(tokens.semantic[themeName][tokenName]).toEqual(
          expect.any(String)
        )
        expect(
          tokens.semantic[themeName][tokenName].length
        ).toBeGreaterThan(0)
      }
    }
  })

  it('keeps shared layout and accessibility minimums', () => {
    const tokens = loadDesignTokens()

    expect(tokens.layout).toMatchObject({
      publicWideMaxPx: 1440,
      publicStandardMaxPx: 1200,
      proseMaxPx: 720,
      controlMinPx: 44
    })

    expect(tokens.primitive.breakpointPx).toEqual({
      sm: 600,
      md: 1024,
      lg: 1440,
      xl: 1920
    })
  })

  it('keeps the approved bilingual typography families', () => {
    const tokens = loadDesignTokens()

    expect(tokens.typography).toMatchObject({
      latin: 'Manrope Variable',
      persian: 'Vazirmatn Variable'
    })
  })
})
