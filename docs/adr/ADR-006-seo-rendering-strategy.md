# ADR-006: SEO Rendering Strategy

## Status

Accepted for MVP.

## Context

Public pages must be understandable by humans, search engines, and AI search systems. The site needs bilingual URLs, language-specific metadata, canonical URLs, hreflang, Open Graph, semantic HTML, and Schema.org JSON-LD.

## Options

| Option | Fit | Notes |
|---|---|---|
| CSR only | Low | Public content would be weaker for SEO and AI visibility |
| SSR | High | Strong crawlability, more deployment complexity |
| SSG | Medium | Fast, but less convenient for dynamic CMS content |
| Hybrid | High | Allows SSR for public pages and CSR for admin |

## Decision

Use SSR/hybrid rendering for public pages. Admin pages may remain CSR and should be noindexed.

## Consequences

- Public content must be readable in rendered HTML.
- Public pages need title, meta description, canonical, hreflang, Open Graph, semantic headings, and JSON-LD where applicable.
- Sitemap excludes admin, draft, archived, and private content.

## Risks

SSR deployment and metadata correctness require testing.

## Follow-Up

Add SEO tests/checks for required public routes and consider `/llms.txt` after MVP.
