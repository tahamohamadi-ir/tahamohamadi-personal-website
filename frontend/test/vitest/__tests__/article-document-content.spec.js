// @vitest-environment node

import { describe, expect, it } from 'vitest'
import { createSSRApp, h } from 'vue'
import { renderToString } from '@vue/server-renderer'

import ArticleDocumentContent from 'src/components/content/ArticleDocumentContent.vue'

describe('article document public renderer', () => {
  it('renders only an approved managed media URL and never an arbitrary image source', async () => {
    const html = await renderToString(createSSRApp({
      render: () => h(ArticleDocumentContent, {
        document: {
          version: 1,
          blocks: [
            { type: 'heading', level: 1, value: 'Document heading' },
            { type: 'image', mediaId: '02345678-1234-4234-9234-123456789abc', alt: 'Managed image', caption: 'Caption' },
            { type: 'image', mediaId: 'https://example.test/unmanaged.png', alt: 'Unsafe image' }
          ]
        }
      })
    }))

    expect(html).toMatch(/<h2\b[^>]*id="article-heading-0"[^>]*>Document heading<\/h2>/)
    expect(html).toContain('src="/api/v1/public/media/02345678-1234-4234-9234-123456789abc"')
    expect(html).toContain('Managed image')
    expect(html).not.toContain('example.test')
  })
})
