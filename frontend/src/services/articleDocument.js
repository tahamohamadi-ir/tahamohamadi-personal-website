const SUPPORTED_BLOCK_TYPES = new Set(['paragraph', 'heading', 'quote', 'code', 'image', 'divider', 'markdown'])

export function markdownToDocument(markdown = '') {
  const lines = String(markdown).replace(/\r\n?/g, '\n').split('\n')
  const blocks = []
  let index = 0

  while (index < lines.length) {
    if (!lines[index].trim()) { index += 1; continue }

    if (lines[index].startsWith('```')) {
      const language = lines[index].slice(3).trim()
      const body = []
      index += 1
      while (index < lines.length && !lines[index].startsWith('```')) body.push(lines[index++])
      if (index < lines.length) index += 1
      blocks.push({ type: 'code', language, value: body.join('\n') })
      continue
    }

    const heading = lines[index].match(/^(#{1,6})\s+(.+)$/)
    if (heading) { blocks.push({ type: 'heading', level: heading[1].length, value: heading[2] }); index += 1; continue }

    const image = lines[index].match(/^!\[([^\]]*)\]\((\S+)(?:\s+"([^"]*)")?\)$/)
    // Imported Markdown URLs are intentionally retained as Markdown. New image
    // blocks may only point to a selected asset from the managed media library.
    if (image) { blocks.push({ type: 'markdown', value: lines[index] }); index += 1; continue }

    if (/^(---|\*\*\*|___)$/.test(lines[index].trim())) { blocks.push({ type: 'divider' }); index += 1; continue }

    if (lines[index].startsWith('> ')) {
      const quote = []
      while (index < lines.length && lines[index].startsWith('> ')) quote.push(lines[index++].slice(2))
      blocks.push({ type: 'quote', value: quote.join('\n') })
      continue
    }

    if (/^(?:[-+*]\s+|\d+\.\s+|\|)/.test(lines[index])) {
      const raw = []
      while (index < lines.length && lines[index].trim()) raw.push(lines[index++])
      blocks.push({ type: 'markdown', value: raw.join('\n') })
      continue
    }

    const paragraph = []
    while (index < lines.length && lines[index].trim() && !/^(#{1,6}\s+|```|> |!\[|---$|\*\*\*$|___$|[-+*]\s+|\d+\.\s+|\|)/.test(lines[index])) paragraph.push(lines[index++])
    if (paragraph.length) blocks.push({ type: 'paragraph', value: paragraph.join('\n') })
  }

  return { version: 1, blocks }
}

export function documentToMarkdown(document) {
  const blocks = Array.isArray(document?.blocks) ? document.blocks : []
  return blocks.filter((block) => SUPPORTED_BLOCK_TYPES.has(block?.type)).map((block) => {
    switch (block.type) {
      case 'heading': return `${'#'.repeat(Math.min(6, Math.max(1, Number(block.level) || 2)))} ${block.value ?? ''}`.trimEnd()
      case 'quote': return String(block.value ?? '').split('\n').map((line) => `> ${line}`).join('\n')
      case 'code': return `\`\`\`${block.language ?? ''}\n${block.value ?? ''}\n\`\`\``
      case 'image': return block.mediaId ? `![${block.alt ?? ''}](/api/v1/public/media/${block.mediaId}${block.caption ? ` "${block.caption}"` : ''})` : ''
      case 'divider': return '---'
      default: return String(block.value ?? '')
    }
  }).filter(Boolean).join('\n\n')
}

export function readingTimeMinutes(markdown, wordsPerMinute = 200) {
  const words = String(markdown ?? '').trim().split(/\s+/).filter(Boolean).length
  return Math.max(1, Math.ceil(words / wordsPerMinute))
}
