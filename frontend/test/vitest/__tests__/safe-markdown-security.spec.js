// @vitest-environment node

import { afterEach, describe, expect, it, vi } from 'vitest'

import {
  RICH_CONTENT_POLICY_VERSION,
  renderSafeMarkdown
} from 'src/components/content/safeMarkdown'

const activeForbiddenElementPattern = /<\/?(?:script|iframe|object|embed|form|input|svg|math|style|h1)\b/i
const activeForbiddenAttributePattern = /<[a-z][^>]*\s(?:on[\w-]+|style|id|name|class|tabindex|target|rel|data-[\w-]+|aria-[\w-]+)\s*=/i
const activeAnchorPattern = /<a\b[^>]*\shref="([^"]*)"[^>]*>/gi
const validAbsoluteHttpUrlPattern = /^https?:\/\/[^\s/]+(?:[/?#]|$)/i
const validRootRelativeUrlPattern = /^\/(?!\/)\S*$/

function expectNoForbiddenActiveMarkup (html) {
  expect(html).not.toMatch(activeForbiddenElementPattern)
  expect(html).not.toMatch(activeForbiddenAttributePattern)
}

function expectNoUnsafeActiveHref (html, diagnostic = {}) {
  for (const match of html.matchAll(activeAnchorPattern)) {
    const href = match[1]

    expect(
      validAbsoluteHttpUrlPattern.test(href) || validRootRelativeUrlPattern.test(href),
      JSON.stringify({
        fixture: diagnostic.label,
        sourceHref: diagnostic.href,
        renderedHtml: html,
        activeHref: href
      })
    ).toBe(true)
  }
}

function renderReadyMarkdown (source) {
  const result = renderSafeMarkdown(source)

  expect(result.status).toBe('ready')
  expect(typeof result.html).toBe('string')

  return result.html
}

afterEach(() => {
  vi.doUnmock('isomorphic-dompurify')
  vi.resetModules()
})

describe('safe Markdown security regression gate', () => {
  it('keeps the approved policy version available to regression tests', () => {
    expect(RICH_CONTENT_POLICY_VERSION).toBe('ux-dec-011-m1-v1')
  })

  it('keeps raw HTML fixtures inert without treating escaped text as active markup', () => {
    const source = [
      '<script>alert("script")</script>',
      '<img src="x" onerror="alert(1)">',
      '<svg onload="alert(1)"><circle></circle></svg>',
      '<math><mtext>math payload</mtext></math>',
      '<unknown-xml><payload /></unknown-xml>',
      '<iframe src="https://example.com"></iframe>',
      '<object data="https://example.com"></object><embed src="https://example.com">',
      '<form><input name="clobber"></form>',
      '<style>body { display: none }</style>',
      '<p style="color:red" onclick="alert(1)">visible prose</p>',
      '<div><script><em>malformed nested active tags</div></script>',
      '<!-- <script>alert("comment")</script> -->',
      '<p id="location" name="__proto__">DOM clobbering text</p>'
    ].join('\n\n')

    const html = renderReadyMarkdown(source)

    expectNoForbiddenActiveMarkup(html)
    expect(html).not.toMatch(/<unknown-xml\b|<img\b/i)
    expect(html).toContain('&lt;script&gt;alert')
    expect(html).toContain('visible prose')
    expect(html).toContain('DOM clobbering text')
  })

  it('normalizes ATX and Setext CMS H1 inputs while preserving H2 through H6', () => {
    const html = renderReadyMarkdown([
      '# ATX CMS title',
      'Setext CMS title\n===============',
      '## Two',
      '### Three',
      '#### Four',
      '##### Five',
      '###### Six',
      '# Another CMS H1'
    ].join('\n\n'))

    expectNoForbiddenActiveMarkup(html)
    expect(html).toContain('ATX CMS title')
    expect(html).toContain('Setext CMS title')
    expect(html).toContain('Another CMS H1')

    for (const level of [2, 3, 4, 5, 6]) {
      expect(html).toContain(`<h${level}>`)
    }
  })

  it('retains only approved active link destinations across encoded and malformed URL fixtures', () => {
    const allowedCases = [
      ['http', 'http://example.com'],
      ['https', 'https://example.com'],
      ['https path', 'https://example.com/path?query=1#fragment'],
      ['root', '/'],
      ['root path', '/about'],
      ['root query', '/path?query=1#fragment']
    ]
    const rejectedCases = [
      ['javascript', 'javascript:alert(1)'],
      ['mixed case javascript', 'JaVaScRiPt:alert(1)'],
      ['tab-obscured javascript', 'java\tscript:alert(1)'],
      ['carriage-return-obscured javascript', 'java\rscript:alert(1)'],
      ['newline-obscured javascript', 'java\nscript:alert(1)'],
      ['percent-encoded javascript', 'jav%61script:alert(1)'],
      ['entity-obscured javascript', 'java&#x73;cript:alert(1)'],
      ['data', 'data:text/html,unsafe'],
      ['vbscript', 'vbscript:msgbox(1)'],
      ['file', 'file:///etc/passwd'],
      ['mailto', 'mailto:person@example.com'],
      ['protocol relative', '//example.com/path'],
      ['relative', 'about'],
      ['dot relative', './about'],
      ['parent relative', '../about'],
      ['unknown scheme', 'custom:unsafe'],
      ['empty', ''],
      ['whitespace only', '   '],
      ['missing http host', 'https://'],
      ['missing http host with path', 'http:///path'],
      ['control character', 'https://example.com/\u0000unsafe']
    ]

    for (const [label, href] of allowedCases) {
      const html = renderReadyMarkdown(`[allowed ${label}](${href})`)

      expect(html).toContain(`href="${href}"`)
      expectNoUnsafeActiveHref(html, { label, href })
    }

    for (const [label, href] of rejectedCases) {
      const html = renderReadyMarkdown(`[rejected ${label}](${href})`)

      const activeHrefs = [...html.matchAll(activeAnchorPattern)].map((match) => match[1])
      const diagnostic = JSON.stringify({
        fixture: label,
        sourceHref: href,
        renderedHtml: html,
        activeHrefs
      })

      expectNoUnsafeActiveHref(html, { label, href })
      expect(html, diagnostic).not.toMatch(/<a\b[^>]*\shref=/i)
      expect(html).not.toMatch(/<a\b[^>]*\shref="(?:javascript|data|vbscript|file|mailto):/i)
    }
  })

  it('keeps inline and reference images and pipe tables non-executable', () => {
    const html = renderReadyMarkdown([
      '![inline image](https://example.com/image.png)',
      '![reference image][photo]',
      '',
      '[photo]: https://example.com/reference-image.png',
      '',
      '| Column | Value |',
      '| --- | --- |',
      '| Safe | Text |'
    ].join('\n'))

    expect(html).not.toMatch(/<img\b|<\/?(?:table|thead|tbody|tr|th|td)\b/i)
    expect(html).toContain('inline image')
    expect(html).toContain('reference image')
    expect(html).toContain('Column')
    expect(html).toContain('Safe')
  })

  it('keeps active-looking code content inert in semantic inline and fenced code', () => {
    const html = renderReadyMarkdown([
      'Inline `<script>alert(1)</script>` code.',
      '```html',
      '<svg onload="alert(1)"></svg>',
      '<script>alert(1)</script>',
      '```'
    ].join('\n\n'))

    expectNoForbiddenActiveMarkup(html)
    expect(html).toContain('<code>')
    expect(html).toContain('<pre><code')
    expect(html).toContain('&lt;script&gt;alert(1)&lt;/script&gt;')
  })

  it('is deterministic and isolates alternating safe and malicious renders without browser globals', () => {
    const safeSource = '## Safe heading\n\n[internal](/about)'
    const maliciousSource = '<script>alert(1)</script>\n\n[unsafe](javascript:alert(1))'

    expect(typeof globalThis.window).toBe('undefined')
    expect(typeof globalThis.document).toBe('undefined')

    const firstSafe = renderReadyMarkdown(safeSource)
    const malicious = renderReadyMarkdown(maliciousSource)
    const secondSafe = renderReadyMarkdown(safeSource)

    expect(firstSafe).toBe(secondSafe)
    expectNoForbiddenActiveMarkup(malicious)
    expectNoUnsafeActiveHref(malicious)
    expect(secondSafe).toContain('href="/about"')
    expect(typeof globalThis.window).toBe('undefined')
    expect(typeof globalThis.document).toBe('undefined')
  })
})

describe('safe Markdown renderer fail-closed behavior', () => {
  it.each([
    ['throws', () => { throw new Error('sanitizer failure') }],
    ['returns a non-string', () => ({ unsafe: 'parser output must not escape' })]
  ])('returns no HTML when sanitization %s', async (_label, sanitizeImplementation) => {
    vi.resetModules()
    vi.doMock('isomorphic-dompurify', () => ({
      sanitize: sanitizeImplementation
    }))

    const { renderSafeMarkdown: renderWithBrokenSanitizer } = await import('src/components/content/safeMarkdown')
    const source = '<script>alert("do not expose this source")</script>'
    const result = renderWithBrokenSanitizer(source)

    expect(result).toEqual({ status: 'error', html: '' })
    expect(result.html).not.toContain('do not expose this source')
    expect(result.html).not.toContain('<script>')
  })
})
