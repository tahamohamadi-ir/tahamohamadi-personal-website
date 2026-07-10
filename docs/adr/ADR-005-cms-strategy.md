# ADR-005: CMS Strategy

## Status

Accepted for MVP.

## Context

The project needs focused content management for pages, blog posts, portfolio items, publications, resume sections, media, social links, and SEO metadata. It also needs bilingual content status and custom public rendering.

## Options

| Option | Fit | Notes |
|---|---|---|
| Custom Lightweight CMS | High | Full control over bilingual SEO, schema, and admin UX |
| Strapi | Medium | Powerful, but adds dependency and integration complexity |
| Directus | Medium | Useful data CMS, but broader than MVP needs |
| WordPress | Low/Medium | Mature CMS, but not aligned with custom app architecture |

## Decision

Build a custom lightweight CMS inside the modular monolith.

## Consequences

- CMS features must remain focused on the site needs.
- Publishable content uses Draft, Published, and Archived states.
- Admin UX should be simpler than a generic CMS.
- Layout and theme controls must be preset-based, not open-ended.

## Risks

Custom CMS work can expand beyond MVP.

## Follow-Up

Limit MVP CMS to pages, posts, media, social links, SEO metadata, and core publish workflows.
