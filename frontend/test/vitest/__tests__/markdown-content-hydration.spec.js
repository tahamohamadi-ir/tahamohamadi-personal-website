// @vitest-environment jsdom

import { createSSRApp, h, nextTick, ref } from 'vue'
import { renderToString } from '@vue/server-renderer'
import { afterEach, describe, expect, it, vi } from 'vitest'

import MarkdownContent from 'src/components/content/MarkdownContent.vue'

const activeForbiddenElementPattern = /<\/?(?:script|iframe|object|embed|form|input|svg|math|style|h1)\b/i
const activeForbiddenAttributePattern = /<[a-z][^>]*\s(?:on[\w-]+|style|id|name|class|tabindex|target|rel|data-[\w-]+|aria-[\w-]+)\s*=/i

function createMarkdownRoot (markdown) {
  return {
    setup: () => () => h(MarkdownContent, { markdown })
  }
}

function createReactiveMarkdownRoot (markdown) {
  const value = ref(markdown)

  return {
    value,
    root: {
      setup: () => () => h(MarkdownContent, { markdown: value.value })
    }
  }
}

async function renderMarkdownContent (root) {
  return renderToString(createSSRApp(root))
}

function createHydrationContainer (html, { lang = 'en', dir = 'ltr' } = {}) {
  const container = document.createElement('div')
  container.lang = lang
  container.dir = dir
  container.innerHTML = html
  document.body.append(container)

  return container
}

function richContentSnapshot (container) {
  const richContent = container.querySelector('.tm-rich-content')

  return {
    text: richContent?.textContent,
    headings: [...container.querySelectorAll('h2, h3, h4, h5, h6')]
      .map((heading) => `${heading.tagName}:${heading.textContent}`),
    links: [...container.querySelectorAll('a')]
      .map((link) => `${link.textContent}:${link.getAttribute('href')}`),
    code: [...container.querySelectorAll('code')].map((code) => code.textContent)
  }
}

function expectNoForbiddenActiveDom (container) {
  const renderedMarkdownHtml = container.querySelector('.tm-rich-content')?.innerHTML ?? container.innerHTML

  expect(renderedMarkdownHtml).not.toMatch(activeForbiddenElementPattern)
  expect(renderedMarkdownHtml).not.toMatch(activeForbiddenAttributePattern)

  for (const link of container.querySelectorAll('a')) {
    const href = link.getAttribute('href') ?? ''

    expect(href).toMatch(/^(?:https?:\/\/[^\s/]+(?:[/?#]|$)|\/(?!\/)\S*)$/i)
  }
}

function captureVueConsoleOutput () {
  const warnings = []
  const errors = []

  vi.spyOn(console, 'warn').mockImplementation((...args) => warnings.push(args.join(' ')))
  vi.spyOn(console, 'error').mockImplementation((...args) => errors.push(args.join(' ')))

  return { warnings, errors }
}

function expectNoHydrationFailure (capturedOutput) {
  const relevantOutput = [...capturedOutput.warnings, ...capturedOutput.errors]
    .filter((message) => /hydration|mismatch|invalid vnode|unhandled.*render|render.*error/i.test(message))

  expect(relevantOutput).toEqual([])
}

afterEach(() => {
  vi.restoreAllMocks()
  document.body.replaceChildren()
})

describe('MarkdownContent SSR hydration gate', () => {
  it('hydrates bilingual safe SSR markup without a mismatch and preserves semantic content', async () => {
    const markdown = [
      'متن فارسی برای بررسی جهت محتوا.',
      'English technical prose with **strong** and *emphasis*.',
      '## Supporting heading',
      '[External profile](https://example.com/profile)',
      '[Internal profile](/en/about)',
      'Inline `const ssr = true` code.'
    ].join('\n\n')
    const root = createMarkdownRoot(markdown)
    const serverHtml = await renderMarkdownContent(root)
    const container = createHydrationContainer(serverHtml, { lang: 'fa', dir: 'rtl' })
    const serverSnapshot = richContentSnapshot(container)
    const capturedOutput = captureVueConsoleOutput()

    createSSRApp(root).mount(container)
    await nextTick()

    expectNoHydrationFailure(capturedOutput)
    expect(container.querySelector('.tm-rich-content')).not.toBeNull()
    expect(richContentSnapshot(container)).toEqual(serverSnapshot)
    expect(container.querySelector('h2')?.textContent).toBe('Supporting heading')
    expect(container.querySelector('h1')).toBeNull()
    expect(container.querySelector('main, q-page')).toBeNull()
    expect(container.querySelector('a[href="https://example.com/profile"]')?.textContent).toBe('External profile')
    expect(container.querySelector('a[href="/en/about"]')?.textContent).toBe('Internal profile')
    expect(container.querySelector('code')?.textContent).toBe('const ssr = true')
    expect(container.querySelector('.tm-rich-content')?.hasAttribute('lang')).toBe(false)
    expect(container.querySelector('.tm-rich-content')?.hasAttribute('dir')).toBe(false)
    expect(container.lang).toBe('fa')
    expect(container.dir).toBe('rtl')
  })

  it('does not introduce unsafe content while hydrating malicious server markup', async () => {
    const markdown = [
      '# CMS title that must not become an H1',
      'Safe visible prose.',
      '<script>alert(1)</script>',
      '<svg onload="alert(1)"></svg>',
      '[unsafe](javascript:alert(1))'
    ].join('\n\n')
    const root = createMarkdownRoot(markdown)
    const serverHtml = await renderMarkdownContent(root)
    const container = createHydrationContainer(serverHtml)
    const serverSnapshot = richContentSnapshot(container)
    const capturedOutput = captureVueConsoleOutput()

    expectNoForbiddenActiveDom(container)
    expect(container.textContent).toContain('Safe visible prose.')

    createSSRApp(root).mount(container)
    await nextTick()

    expectNoHydrationFailure(capturedOutput)
    expectNoForbiddenActiveDom(container)
    expect(container.textContent).toContain('Safe visible prose.')
    expect(container.querySelector('h1')).toBeNull()
    expect(richContentSnapshot(container)).toEqual(serverSnapshot)
  })

  it('sanitizes reactive updates after hydration and handles blank then safe Markdown', async () => {
    const reactiveRoot = createReactiveMarkdownRoot('## Initial safe heading\n\n[internal](/about)')
    const serverHtml = await renderMarkdownContent(reactiveRoot.root)
    const container = createHydrationContainer(serverHtml)
    const capturedOutput = captureVueConsoleOutput()

    createSSRApp(reactiveRoot.root).mount(container)
    await nextTick()

    reactiveRoot.value.value = '<script>alert(1)</script>\n\n[unsafe](javascript:alert(1))\n\nSafe replacement prose.'
    await nextTick()

    expectNoForbiddenActiveDom(container)
    expect(container.textContent).toContain('Safe replacement prose.')

    reactiveRoot.value.value = ' \n\t '
    await nextTick()

    expect(container.querySelector('.tm-rich-content')).toBeNull()

    reactiveRoot.value.value = '## Safe return\n\n[external](https://example.com/return)'
    await nextTick()

    expect(container.querySelector('.tm-rich-content')).not.toBeNull()
    expect(container.querySelector('h2')?.textContent).toBe('Safe return')
    expect(container.querySelector('a[href="https://example.com/return"]')?.textContent).toBe('external')
    expectNoHydrationFailure(capturedOutput)
  })

  it('isolates concurrent SSR requests and remains deterministic when request order reverses', async () => {
    const requestA = 'درخواست فارسی A\n\n[پیوند داخلی](/fa/about)'
    const requestB = 'English request B\n\n<script>alert(1)</script>\n\n[unsafe](javascript:alert(1))'

    const [firstA, firstB] = await Promise.all([
      renderMarkdownContent(createMarkdownRoot(requestA)),
      renderMarkdownContent(createMarkdownRoot(requestB))
    ])
    const [secondB, secondA] = await Promise.all([
      renderMarkdownContent(createMarkdownRoot(requestB)),
      renderMarkdownContent(createMarkdownRoot(requestA))
    ])

    expect(firstA).toBe(secondA)
    expect(firstB).toBe(secondB)
    expect(firstA).toContain('درخواست فارسی A')
    expect(firstA).toContain('href="/fa/about"')
    expect(firstA).not.toContain('English request B')
    expect(firstB).toContain('English request B')
    expect(firstB).not.toContain('درخواست فارسی A')
    expect(firstB).not.toMatch(activeForbiddenElementPattern)
    expect(firstB).not.toMatch(/<a\b[^>]*href="javascript:/i)
  })
})
