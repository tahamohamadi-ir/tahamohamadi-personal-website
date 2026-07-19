// @vitest-environment node

import { existsSync, readFileSync } from 'node:fs'
import path from 'node:path'
import MarkdownIt from 'markdown-it'
import { sanitize } from 'isomorphic-dompurify'
import { describe, expect, it } from 'vitest'

const workingDirectory = process.cwd()
const projectRoot = path.basename(workingDirectory).toLowerCase() === 'frontend'
  ? path.dirname(workingDirectory)
  : workingDirectory

const readProjectFile = (projectRelativePath) => {
  const filePath = path.resolve(projectRoot, projectRelativePath)

  if (!existsSync(filePath)) {
    throw new Error(`NOT_IMPLEMENTED:${projectRelativePath}`)
  }

  return readFileSync(filePath, 'utf8')
}

const readProjectJson = (projectRelativePath) => JSON.parse(
  readProjectFile(projectRelativePath)
)

const allowedTags = Object.freeze([
  'p', 'br', 'strong', 'em', 'del', 'ul', 'ol', 'li', 'blockquote', 'pre',
  'code', 'hr', 'h2', 'h3', 'h4', 'h5', 'h6', 'a'
])

const sanitizerOptions = Object.freeze({
  ALLOWED_TAGS: allowedTags,
  ALLOWED_ATTR: Object.freeze(['href']),
  ALLOW_DATA_ATTR: false,
  ALLOW_ARIA_ATTR: false,
  ALLOWED_URI_REGEXP: /^(?:(?:https?):|\/(?!\/))/i
})

const createParser = () => new MarkdownIt({
  html: false,
  linkify: false,
  typographer: false
})

const renderSafeMarkdown = (source) => sanitize(
  createParser().render(source),
  sanitizerOptions
)

describe('rich-content dependency compatibility contract', () => {
  it('keeps the approved direct dependency pins, lockfile records, and installed MIT manifests', () => {
    const packageManifest = readProjectJson('frontend/package.json')
    const packageLock = readProjectJson('frontend/package-lock.json')
    const markdownItManifest = readProjectJson('frontend/node_modules/markdown-it/package.json')
    const domPurifyManifest = readProjectJson(
      'frontend/node_modules/isomorphic-dompurify/package.json'
    )

    expect(packageManifest.dependencies['markdown-it']).toBe('14.3.0')
    expect(packageManifest.dependencies['isomorphic-dompurify']).toBe('3.18.0')
    expect(packageLock.packages[''].dependencies['markdown-it']).toBe('14.3.0')
    expect(packageLock.packages[''].dependencies['isomorphic-dompurify']).toBe('3.18.0')
    expect(packageLock.packages['node_modules/markdown-it'].version).toBe('14.3.0')
    expect(
      packageLock.packages['node_modules/isomorphic-dompurify'].version
    ).toBe('3.18.0')
    expect(markdownItManifest.version).toBe('14.3.0')
    expect(domPurifyManifest.version).toBe('3.18.0')
    expect(markdownItManifest.license).toBe('MIT')
    expect(domPurifyManifest.license).toBe('MIT')
  })

  it('supports Node ESM imports without browser globals', () => {
    expect(process.release.name).toBe('node')
    expect(typeof MarkdownIt).toBe('function')
    expect(typeof sanitize).toBe('function')
    expect(typeof globalThis.window).toBe('undefined')
    expect(typeof globalThis.document).toBe('undefined')

    sanitize('<p>safe prose</p>', sanitizerOptions)

    expect(typeof globalThis.window).toBe('undefined')
    expect(typeof globalThis.document).toBe('undefined')
  })

  it('uses the approved Markdown parser settings and preserves only safe Markdown links', () => {
    const parser = createParser()
    const rendered = parser.render([
      '<script>alert(1)</script>',
      'Safe prose with **strong** and *emphasis*.',
      '## Supporting heading',
      '[Safe HTTPS](https://example.com/profile)',
      '[Safe internal](/en/about)',
      '[Unsafe JavaScript](javascript:alert(1))'
    ].join('\n\n'))

    expect(parser.options.html).toBe(false)
    expect(parser.options.linkify).toBe(false)
    expect(parser.options.typographer).toBe(false)
    expect(rendered).toContain('&lt;script&gt;alert(1)&lt;/script&gt;')
    expect(rendered).toContain('<strong>strong</strong>')
    expect(rendered).toContain('<h2>Supporting heading</h2>')
    expect(rendered).toContain('href="https://example.com/profile"')
    expect(rendered).toContain('href="/en/about"')
    expect(rendered).not.toMatch(/href="javascript:/i)
  })

  it('applies the UX-DEC-011 test-local allowlist and URL policy', () => {
    const sanitized = sanitize([
      '<h1>Route-owned heading</h1>',
      '<h2>Allowed heading</h2><h3>Three</h3><h4>Four</h4><h5>Five</h5><h6>Six</h6>',
      '<p id="clobber" name="clobber" style="color:red" data-test="x" aria-label="x" onclick="alert(1)">Safe prose <strong>strong</strong> <em>emphasis</em> <del>deleted</del><br>remains.</p>',
      '<ul><li>Unordered item</li></ul><ol><li>Ordered item</li></ol><blockquote>Quoted prose</blockquote><pre><code>const safe = true</code></pre><hr>',
      '<script>alert(1)</script><style>body{display:none}</style>',
      '<iframe src="https://example.com"></iframe><object></object><embed><form><input><svg></svg>',
      '<a href="https://example.com/profile" target="_blank" rel="author">HTTPS</a>',
      '<a href="http://example.com/profile">HTTP</a>',
      '<a href="/en/about">Internal</a>'
    ].join(''), sanitizerOptions)

    expect(sanitized).not.toMatch(/<\/?(?:h1|script|style|iframe|object|embed|form|input|svg)\b/i)
    expect(sanitized).not.toMatch(/\s(?:on\w+|style|id|name|target|rel|data-[\w-]+|aria-[\w-]+)=/i)
    expect(sanitized).toContain('<h2>Allowed heading</h2>')
    expect(sanitized).toContain('<h6>Six</h6>')
    expect(sanitized).toContain('<strong>strong</strong>')
    expect(sanitized).toContain('<em>emphasis</em>')
    expect(sanitized).toContain('<del>deleted</del>')
    expect(sanitized).toContain('<ul><li>Unordered item</li></ul>')
    expect(sanitized).toContain('<ol><li>Ordered item</li></ol>')
    expect(sanitized).toContain('<blockquote>Quoted prose</blockquote>')
    expect(sanitized).toContain('<pre><code>const safe = true</code></pre>')
    expect(sanitized).toContain('<hr>')
    expect(sanitized).toContain('href="https://example.com/profile"')
    expect(sanitized).toContain('href="http://example.com/profile"')
    expect(sanitized).toContain('href="/en/about"')

    for (const unsafeHref of [
      'javascript:alert(1)',
      'java&#x73;cript:alert(1)',
      '&#x09;javascript:alert(1)',
      'data:text/html,unsafe',
      'vbscript:msgbox(1)',
      'file:///etc/passwd',
      'mailto:person@example.com',
      '//example.com/path',
      'custom:unsafe'
    ]) {
      const unsafeLink = sanitize(
        `<a href="${unsafeHref}">unsafe link text</a>`,
        sanitizerOptions
      )

      expect(unsafeLink).not.toMatch(/\shref=/i)
      expect(unsafeLink).toContain('unsafe link text')
    }
  })

  it('proves the bounded parse-to-sanitize capability and deterministic output', () => {
    const markdownSource = [
      '# CMS heading must not become the route H1',
      'Safe prose with **strong** and *emphasis*.',
      '## Allowed supporting heading',
      '- Safe list item',
      '> Safe quotation',
      '`Safe code`',
      '[Safe HTTPS](https://example.com/profile)',
      '[Safe internal](/en/about)',
      '[Unsafe JavaScript](javascript:alert(1))',
      '<script>alert(1)</script>',
      '<iframe src="https://example.com"></iframe>'
    ].join('\n\n')

    const firstOutput = renderSafeMarkdown(markdownSource)
    const secondOutput = renderSafeMarkdown(markdownSource)

    expect(firstOutput).toBe(secondOutput)
    expect(firstOutput).not.toMatch(/<\/?(?:h1|script|style|iframe|object|embed|form|input|svg)\b/i)
    expect(firstOutput).not.toMatch(/<a\b[^>]*\shref="(?:javascript|data|vbscript|file|mailto):/i)
    expect(firstOutput).toContain('Safe prose')
    expect(firstOutput).toContain('<h2>Allowed supporting heading</h2>')
    expect(firstOutput).toContain('<ul>')
    expect(firstOutput).toContain('<blockquote>')
    expect(firstOutput).toContain('<code>Safe code</code>')
    expect(firstOutput).toContain('href="https://example.com/profile"')
    expect(firstOutput).toContain('href="/en/about"')
  })
})
