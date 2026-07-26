import { describe, expect, it } from 'vitest'

import { documentToMarkdown, markdownToDocument, readingTimeMinutes } from 'src/services/articleDocument'

describe('article document adapter', () => {
  it('imports the supported legacy Markdown blocks without discarding content', () => {
    const markdown = `# Intro\n\nA paragraph.\n\n> A quote\n\n![Diagram](https://example.test/diagram.png \"Caption\")\n\n\`\`\`js\nconst answer = 42\n\`\`\`\n\n---`

    const document = markdownToDocument(markdown)

    expect(document.blocks.map((block) => block.type)).toEqual(['heading', 'paragraph', 'quote', 'markdown', 'code', 'divider'])
    expect(documentToMarkdown(document)).toBe(markdown)
  })

  it('preserves unsupported Markdown as an explicit Markdown block', () => {
    const markdown = '- one\n- two'

    expect(markdownToDocument(markdown).blocks).toEqual([{ type: 'markdown', value: markdown }])
  })

  it('exports new image blocks only through a managed public-media path', () => {
    expect(documentToMarkdown({ version: 1, blocks: [{ type: 'image', mediaId: '02345678-1234-4234-9234-123456789abc', alt: 'Diagram' }] }))
      .toBe('![Diagram](/api/v1/public/media/02345678-1234-4234-9234-123456789abc)')
    expect(documentToMarkdown({ version: 1, blocks: [{ type: 'image', alt: 'Missing asset' }] })).toBe('')
  })

  it('calculates reading time with a one-minute floor', () => {
    expect(readingTimeMinutes('one two three')).toBe(1)
    expect(readingTimeMinutes(Array.from({ length: 401 }, () => 'word').join(' '))).toBe(3)
  })
})
