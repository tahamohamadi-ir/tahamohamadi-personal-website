# V2 Release 3: Article editor technical spike

**Status:** In progress

## Decision

The first editor increment uses a small local block-document adapter over the
existing bilingual `bodyMarkdown` contract. It imports supported Markdown as
paragraph, heading, quote, fenced code, divider, and explicit Markdown
blocks; legacy Markdown images, unknown list syntax, and tables remain an explicit Markdown block so no
legacy source is silently lost. Saving therefore continues through the
existing versioned Blog API without a migration or a parallel rich-content
source of truth.

## Rationale

- The public route uses a typed component renderer for allowlisted document
  blocks and the existing sanitized Markdown renderer for legacy content.
- The current API retains independent Persian and English source fields.
- No unreviewed editor dependency, raw HTML, or client-only public renderer is
  introduced.
- A later document-schema migration can persist this same block shape once
  revision history and import/export requirements are implemented.

## Persistence increment

`V14__add_blog_article_document.sql` stores an optional, versioned article
document alongside `body_markdown` per locale. The Admin API accepts only the
first-version allowlisted block types and caps the document at 200 blocks and
100,000 serialized characters. Image blocks contain a managed `mediaId`, not a
free-form URL. The API requires it to be an `INLINE` asset on the same post;
the public renderer accepts only a UUID-shaped ID and emits the established
public-media route. The media usage index and replacement workflow cover these
document references.

## First increment

- Block editing for text, headings, quotes, code, images, dividers, and
  explicit Markdown escape hatches.
- Keyboard-accessible move/remove controls, preview, and reading-time signal.
- A Markdown mode remains available for advanced and unsupported syntax.

## Known follow-ups

- Slash commands, paste cleanup, tables, gallery, TOC, recovery, revisions,
  structured data, and print styling remain subsequent Release 3 slices.
