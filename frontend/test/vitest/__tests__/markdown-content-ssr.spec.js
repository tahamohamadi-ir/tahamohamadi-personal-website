// @vitest-environment node

import { existsSync, readFileSync, readdirSync } from 'node:fs'
import { resolve } from 'node:path'

import { createSSRApp, h } from 'vue'
import { renderToString } from '@vue/server-renderer'
import { afterEach, describe, expect, it, vi } from 'vitest'

import MarkdownContent from 'src/components/content/MarkdownContent.vue'

const renderSafeMarkdownMock = vi.hoisted(() => vi.fn())

vi.mock('src/components/content/safeMarkdown', async (importOriginal) => {
  const actual = await importOriginal()

  return {
    ...actual,
    renderSafeMarkdown: (source) => {
      const implementation = renderSafeMarkdownMock.getMockImplementation()

      return implementation ? implementation(source) : actual.renderSafeMarkdown(source)
    }
  }
})

const workingDirectory = process.cwd()
const projectRoot = workingDirectory.split(/[\\/]/).pop().toLowerCase() === 'frontend'
  ? resolve(workingDirectory, '..')
  : workingDirectory

const componentPath = 'frontend/src/components/content/MarkdownContent.vue'

function readProjectFile (projectRelativePath) {
  const filePath = resolve(projectRoot, projectRelativePath)

  if (!existsSync(filePath)) {
    throw new Error(`NOT_IMPLEMENTED:${projectRelativePath}`)
  }

  return readFileSync(filePath, 'utf8')
}

function renderMarkdownContent (markdown, slots = {}) {
  return renderToString(createSSRApp({
    render: () => h(MarkdownContent, { markdown }, slots)
  }))
}

afterEach(() => {
  renderSafeMarkdownMock.mockReset()
})

describe('MarkdownContent SSR boundary', () => {
  it('renders sanitized safe prose and allowed headings deterministically on the server', async () => {
    const markdown = [
      '# CMS route title',
      'Safe **prose**.',
      '## Supporting heading',
      '[HTTPS](https://example.com)',
      '[Internal](/en/about)',
      '[Unsafe](javascript:alert(1))'
    ].join('\n\n')

    const first = await renderMarkdownContent(markdown)
    const second = await renderMarkdownContent(markdown)

    expect(first).toBe(second)
    expect(first).toContain('<div class="tm-rich-content">')
    expect(first).toContain('<p>Safe <strong>prose</strong>.</p>')
    expect(first).toContain('<h2>Supporting heading</h2>')
    expect(first).toContain('href="https://example.com"')
    expect(first).toContain('href="/en/about"')
    expect(first).not.toMatch(/<\/?h1\b/i)
    expect(first).not.toMatch(/<a\b[^>]*href="javascript:/i)
  })

  it('renders no prose wrapper for empty Markdown', async () => {
    const html = await renderMarkdownContent(' \n\t ')

    expect(html).not.toContain('tm-rich-content')
  })

  it('renders only the route-provided named error slot for a safe renderer error', async () => {
    renderSafeMarkdownMock.mockReturnValue({ status: 'error', html: '' })

    const html = await renderMarkdownContent(
      'valid Markdown input',
      { error: () => h('p', { class: 'route-error' }, 'Localized route error') }
    )

    expect(html).toContain('<p class="route-error">Localized route error</p>')
    expect(html).not.toContain('tm-rich-content')
  })

  it('contains only the approved sink and no page or locale ownership', () => {
    const source = readProjectFile(componentPath)

    expect(source.match(/\bv-html\b/g) ?? []).toHaveLength(1)
    expect(source).not.toMatch(/<main\b|<q-page\b|<h1\b/i)
    expect(source).not.toMatch(/\b(?:lang|dir)\s*=/)
    expect(source).toMatch(/<slot\b(?=[^>]*\bname\s*=\s*['"]error['"])[^>]*\/>/)
  })

  it('leaves MarkdownContent as the only production HTML sink', () => {
    const sourceRoot = resolve(projectRoot, 'frontend/src')
    const sinkMatches = readdirSync(sourceRoot, { recursive: true })
      .filter((entry) => /\.(?:js|vue)$/.test(entry))
      .map((entry) => ({
        path: String(entry).replaceAll('\\', '/'),
        source: readFileSync(resolve(sourceRoot, entry), 'utf8')
      }))
      .flatMap(({ path, source }) => (
        source.match(/v-html|innerHTML|outerHTML|insertAdjacentHTML/g) ?? []
      ).map((sink) => ({ path, sink })))

    expect(sinkMatches).toEqual([{ path: 'components/content/MarkdownContent.vue', sink: 'v-html' }])
  })
})
