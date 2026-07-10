# Internationalization Rules

## Supported Languages

The website supports:

- Persian: `fa`
- English: `en`

## URL Strategy

Use language-prefixed URLs:

- `/fa/...`
- `/en/...`

Do not use query parameters for language selection in public URLs.

## Direction

Persian:

- `lang="fa"`
- `dir="rtl"`

English:

- `lang="en"`
- `dir="ltr"`

## Content Rules

1. Public content must be language-aware.
2. Menus must be translated.
3. SEO metadata must be translated.
4. Slugs should be language-specific.
5. Admin must show translation status.
6. Missing translations must be handled clearly.

## Missing Translation Behavior

If English content is missing:

- Do not silently show Persian as English.
- Show a clear message.
- Provide link to Persian version if available.

If Persian content is missing:

- Show a clear message.
- Provide link to English version if available.

## Database Rules

Use translation tables for:

- pages
- posts
- categories
- tags
- portfolio items
- publications if needed

## SEO Rules

Each language version must have:

- language-specific title
- language-specific description
- canonical URL
- hreflang references
