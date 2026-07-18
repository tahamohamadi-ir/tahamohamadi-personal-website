import MarkdownIt from 'markdown-it'
import { sanitize } from 'isomorphic-dompurify'

export const RICH_CONTENT_POLICY_VERSION = 'ux-dec-011-m1-v1'

const allowedTags = Object.freeze([
  'p', 'br', 'strong', 'em', 'del', 'ul', 'ol', 'li', 'blockquote', 'pre',
  'code', 'hr', 'h2', 'h3', 'h4', 'h5', 'h6', 'a'
])

const forbiddenTags = Object.freeze([
  'h1', 'script', 'style', 'iframe', 'object', 'embed', 'form', 'input', 'svg'
])

const allowedAttributes = Object.freeze(['href'])
const forbiddenAttributes = Object.freeze([
  'style', 'id', 'name', 'target', 'rel', 'class', 'tabindex'
])

const allowedUriPattern = Object.freeze(/^(?:(?:https?):|\/(?!\/))/i)
const absoluteHttpUrlPattern = /^https?:\/\/[^\s/]+(?:[/?#]|$)/i
const rootRelativeUrlPattern = /^\/(?!\/)\S*$/

const sanitizerOptions = Object.freeze({
  ALLOWED_TAGS: allowedTags,
  ALLOWED_ATTR: allowedAttributes,
  FORBID_TAGS: forbiddenTags,
  FORBID_ATTR: forbiddenAttributes,
  ALLOW_DATA_ATTR: false,
  ALLOW_ARIA_ATTR: false,
  ALLOWED_URI_REGEXP: allowedUriPattern,
  RETURN_TRUSTED_TYPE: false
})

function isAllowedMarkdownLink (href) {
  if (typeof href !== 'string') {
    return false
  }

  const value = href.trim()

  return value !== '' && (
    absoluteHttpUrlPattern.test(value) || rootRelativeUrlPattern.test(value)
  )
}

function createParser () {
  const parser = new MarkdownIt({
    html: false,
    linkify: false,
    typographer: false
  })

  parser.disable(['table', 'image'])
  parser.validateLink = isAllowedMarkdownLink
  parser.renderer.rules.s_open = () => '<del>'
  parser.renderer.rules.s_close = () => '</del>'
  parser.core.ruler.after('block', 'normalize-route-owned-h1', (state) => {
    for (const token of state.tokens) {
      if (token.tag === 'h1') {
        token.type = token.type === 'heading_open' ? 'paragraph_open' : 'paragraph_close'
        token.tag = 'p'
      }
    }
  })

  return parser
}

export function renderSafeMarkdown (source) {
  if (source == null || (typeof source === 'string' && source.trim() === '')) {
    return { status: 'empty', html: '' }
  }

  if (typeof source !== 'string') {
    return { status: 'error', html: '' }
  }

  try {
    const parsedHtml = createParser().render(source)

    if (typeof parsedHtml !== 'string') {
      return { status: 'error', html: '' }
    }

    const sanitizedHtml = sanitize(parsedHtml, sanitizerOptions)

    if (typeof sanitizedHtml !== 'string') {
      return { status: 'error', html: '' }
    }

    return { status: 'ready', html: sanitizedHtml }
  } catch {
    return { status: 'error', html: '' }
  }
}
