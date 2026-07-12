# Database Design

Source: `docs/master-plan.md` v2.0.

## Principles

| ID | Rule |
|---|---|
| DB-001 | PostgreSQL is the primary database |
| DB-002 | All schema changes use Flyway |
| DB-003 | Public content uses translation tables |
| DB-004 | Flexible settings may use JSONB |
| DB-005 | Core business data should not use JSONB without reason |
| DB-006 | Content tables support soft delete |
| DB-007 | Slugs are unique per language |
| DB-008 | Audit fields are required |
| DB-009 | Indexes are needed for slug, status, published_at, and search |
| DB-010 | ViewStat should be aggregate-friendly |

## Core Entities

| Entity | Purpose | MVP |
|---|---|---|
| User | Admin and future user identities | Yes |
| Role | RBAC roles | Yes |
| Permission | RBAC permissions | Yes |
| Page | Static/dynamic public pages | Yes |
| PageTranslation | Page language content | Yes |
| Post | Blog post lifecycle | Yes |
| PostTranslation | Blog language content and slugs | Yes |
| Category | Blog category | Yes |
| CategoryTranslation | Category language content | Yes |
| Tag | Blog tag | Yes |
| TagTranslation | Tag language content | Yes |
| MediaFile | Uploaded file metadata | Yes |
| PortfolioItem | Portfolio items | Yes |
| PortfolioItemTranslation | Portfolio language content | Yes |
| ResumeSection | Resume sections | Yes |
| Skill | Skills | Yes |
| TimelineItem | Academic/work timeline | Should |
| ResearchInterest | Research interests | Yes |
| Publication | Publications/books/articles | Yes |
| ContactMessage | Contact submissions | Yes |
| SocialLink | Social/profile links | Yes |
| ThemeSetting | Preset-based theme | Should |
| LayoutSetting | Preset-based layout | Could |
| MenuItem | Menus | Should |
| SliderItem | Slider content | Could |
| ViewStat | Lightweight analytics | Should |
| DownloadStat | Download analytics | Should |
| TelegramPublishLog | Telegram draft logs | Later |
| AuditLog | Sensitive operation log | Yes |
| SiteSetting | Global settings | Yes |

## Table Summary

| Table | Key Columns | Important Indexes |
|---|---|---|
| users | id, email, password_hash, status | unique(email), status |
| roles | id, name | unique(name) |
| permissions | id, code | unique(code) |
| pages | id, page_key, status | unique(page_key), status |
| page_translations | page_id, language_code, title, slug, content, seo_* | unique(language_code, slug) |
| posts | id, status, published_at, show_view_count | status, published_at |
| post_translations | post_id, language_code, title, slug, excerpt, content | unique(language_code, slug), FTS |
| categories | id, status | status |
| category_translations | category_id, language_code, name, slug | unique(language_code, slug) |
| tags | id, status | status |
| tag_translations | tag_id, language_code, name, slug | unique(language_code, slug) |
| media_files | id, storage_path, mime_type, size_bytes, visibility | mime_type, visibility |
| portfolio_items | id, status, sort_order | status, sort_order |
| publications | id, title, status, published_year, url | status, published_year |
| contact_messages | id, sender_email, status, created_at | status, created_at |
| social_links | id, platform, url, is_active | unique(platform) |
| view_stats | entity_type, entity_id, viewed_at | entity_type, entity_id, viewed_at |
| audit_logs | actor_user_id, action, entity_type, created_at | actor, action, entity |
| site_settings | key, value | unique(key), optional JSONB GIN |

## Translation Model

Use translation tables for pages, posts, categories, tags, portfolio items, and publications if needed. Each translation should carry language-specific title, slug, body/excerpt fields, and SEO metadata.

## Content State

Publishable content must support at least:

- Draft
- Published
- Archived

Public queries must only return Published content. Draft and Archived content must be excluded from public pages, search, sitemap, and feeds.

## Search

Use PostgreSQL Full Text Search for MVP blog/content search. Do not add Elasticsearch, Meilisearch, or Redis for MVP search.

## Persistence Implementation Status

Flyway migrations V1 through V7 implement the current PostgreSQL persistence foundation.

- V1: identity, roles, role assignments, and append-only audit events.
- V2: media asset metadata and localized media alt/caption records.
- V3: managed pages, localized pages, social links, and contact messages.
- V4: blog categories, posts, tags, explicit post mappings, and localized PostgreSQL FTS vectors.
- V5: skill taxonomy, localized skills, portfolio projects, and explicit project-skill mappings.
- V6: publications, resume entries, localized content, and per-locale resume documents.
- V7: manual, ordered featured-content records and reviewed repository access indexes.

All public-content repository queries require a requested locale and exclude deleted or unpublished parents. Hibernate runs with schema validation and integration tests use PostgreSQL 17 through Testcontainers.
