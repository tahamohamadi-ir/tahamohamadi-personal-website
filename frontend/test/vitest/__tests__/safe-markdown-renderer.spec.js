// @vitest-environment node

import { describe, expect, it } from 'vitest'

import {
  RICH_CONTENT_POLICY_VERSION,
  renderSafeMarkdown
} from 'src/components/content/safeMarkdown'

describe('safe Markdown renderer', () => {
  it('keeps the UX-DEC-011 policy version stable', () => {
    expect(RICH_CONTENT_POLICY_VERSION).toBe('ux-dec-011-m1-v1')
  })

  it('returns empty for null, undefined, and blank Markdown', () => {
    for (const source of [null, undefined, '', ' \n\t ']) {
      expect(renderSafeMarkdown(source)).toEqual({ status: 'empty', html: '' })
    }
  })

  it('fails closed for non-string, non-null input', () => {
    for (const source of [{ markdown: 'unsafe' }, ['unsafe'], 42, true]) {
      expect(renderSafeMarkdown(source)).toEqual({ status: 'error', html: '' })
    }
  })

  it('renders the approved prose, list, quote, and code Markdown', () => {
    const result = renderSafeMarkdown([
      'Safe paragraph with **strong**, *emphasis*, and ~~deleted~~ text.',
      '- One',
      '- Two',
      '> A safe quotation',
      '`inline code`',
      '```js',
      'const safe = true',
      '```'
    ].join('\n\n'))

    expect(result.status).toBe('ready')
    expect(result.html).toContain('<p>Safe paragraph with <strong>strong</strong>, <em>emphasis</em>, and <del>deleted</del> text.</p>')
    expect(result.html).toMatch(/<ul>\s*<li>One<\/li>\s*<li>Two<\/li>\s*<\/ul>/)
    expect(result.html).toContain('<blockquote>\n<p>A safe quotation</p>\n</blockquote>')
    expect(result.html).toContain('<p><code>inline code</code></p>')
    expect(result.html).toContain('<pre><code>const safe = true')
  })

  it('retains H2 through H6 but normalizes Markdown H1 to paragraph semantics', () => {
    const result = renderSafeMarkdown([
      '# CMS title',
      '## Two',
      '### Three',
      '#### Four',
      '##### Five',
      '###### Six'
    ].join('\n\n'))

    expect(result).toMatchObject({ status: 'ready' })
    expect(result.html).toContain('<p>CMS title</p>')
    expect(result.html).not.toMatch(/<\/?h1\b/i)

    for (const level of [2, 3, 4, 5, 6]) {
      expect(result.html).toContain(`<h${level}>`)
    }
  })

  it('keeps raw HTML inert and removes active or forbidden elements and attributes', () => {
    const result = renderSafeMarkdown([
      '<script>alert(1)</script>',
      '<iframe src="https://example.com"></iframe>',
      '<svg onload="alert(1)"></svg>',
      '<p style="color:red" id="clobber" name="clobber" class="x" tabindex="0" data-x="x" aria-label="x" onclick="alert(1)">Prose</p>'
    ].join('\n\n'))

    expect(result).toMatchObject({ status: 'ready' })
    expect(result.html).not.toMatch(/<\/?(?:script|iframe|svg)\b/i)
    expect(result.html).not.toMatch(/<[a-z][^>]*\s(?:on\w+|style|id|name|class|tabindex|data-[\w-]+|aria-[\w-]+)=/i)
    expect(result.html).toContain('&lt;svg onload=')
    expect(result.html).toContain('Prose')
  })

  it('keeps only approved HTTP, HTTPS, and root-relative active links', () => {
    const result = renderSafeMarkdown([
      '[HTTPS](https://example.com/profile)',
      '[HTTP](http://example.com/profile)',
      '[Internal](/en/about)'
    ].join('\n\n'))

    expect(result.html).toContain('href="https://example.com/profile"')
    expect(result.html).toContain('href="http://example.com/profile"')
    expect(result.html).toContain('href="/en/about"')
  })

  it('never retains active href values for unsupported URL schemes or paths', () => {
    for (const href of [
      'javascript:alert(1)',
      'java&#x73;cript:alert(1)',
      '&#x09;javascript:alert(1)',
      'data:text/html,unsafe',
      'vbscript:msgbox(1)',
      'file:///etc/passwd',
      'mailto:person@example.com',
      '//example.com/path',
      'custom:unsafe',
      'relative/path'
    ]) {
      const result = renderSafeMarkdown(`[unsafe](${href})`)

      expect(result.html).not.toMatch(/<a\b[^>]*\shref=/i)
    }
  })

  it('does not render tables or images as HTML elements', () => {
    const result = renderSafeMarkdown([
      '| Column | Value |',
      '| --- | --- |',
      '| Safe | Text |',
      '![Unapproved image](https://example.com/image.png)'
    ].join('\n'))

    expect(result.html).not.toMatch(/<\/?table\b|<img\b/i)
  })

  it('is deterministic without creating browser globals or exposing a raw fallback', () => {
    const source = '[safe](https://example.com)\n\n<script>alert(1)</script>'

    expect(typeof globalThis.window).toBe('undefined')
    expect(typeof globalThis.document).toBe('undefined')

    const first = renderSafeMarkdown(source)
    const second = renderSafeMarkdown(source)

    expect(first).toEqual(second)
    expect(first.html).not.toContain('<script>')
    expect(first.html).not.toContain('href="javascript:')
    expect(typeof globalThis.window).toBe('undefined')
    expect(typeof globalThis.document).toBe('undefined')
  })
})
