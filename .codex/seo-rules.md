# SEO and AI Visibility Rules

## SEO Goal

Public pages must be understandable by humans, search engines, and AI search systems.

## Public Page SEO Requirements

Every public page must have:

- title
- meta description
- canonical URL
- language-specific URL
- hreflang references where applicable
- Open Graph metadata
- semantic HTML
- correct heading hierarchy

## Required Public Pages

SEO must be handled for:

- `/fa`
- `/en`
- `/fa/about`
- `/en/about`
- `/fa/resume`
- `/en/resume`
- `/fa/research`
- `/en/research`
- `/fa/publications`
- `/en/publications`
- `/fa/blog`
- `/en/blog`
- blog detail pages
- `/fa/portfolio`
- `/en/portfolio`
- `/fa/contact`
- `/en/contact`

## Structured Data

Use Schema.org JSON-LD where applicable.

Recommended schemas:

- Person
- WebSite
- BlogPosting
- Article
- ScholarlyArticle
- CreativeWork
- ContactPage
- BreadcrumbList

## AI Visibility Rules

1. Public content must be rendered in readable HTML.
2. Do not rely only on client-side JavaScript for public content.
3. Use clear headings.
4. Use concise page summaries.
5. Use structured data.
6. Use descriptive internal links.
7. Avoid vague labels such as "click here".
8. Add alt text to meaningful images.
9. Keep profile, research, and publication pages explicit and structured.
10. Consider adding `/llms.txt` after MVP.

## Blog SEO Rules

Each blog post must have:

- language-specific slug
- title
- excerpt
- author
- published date
- updated date if changed
- categories
- tags
- canonical URL
- Open Graph image if available
- BlogPosting schema

## Multilingual SEO Rules

1. Persian and English pages must have separate URLs.
2. Each language version must have language-specific metadata.
3. Use hreflang for equivalent pages.
4. Do not silently duplicate Persian content as English.
5. Missing translations must be handled clearly.

## Sitemap Rules

Generate sitemap for:

- static public pages
- published blog posts
- portfolio items
- publications if they have public detail pages

Do not include:

- admin pages
- draft content
- archived content
- private media
