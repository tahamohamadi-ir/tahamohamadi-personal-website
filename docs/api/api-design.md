# API Design

Source: `docs/master-plan.md` v2.0.

## Principles

| ID | Rule |
|---|---|
| API-001 | Base path is `/api/v1` |
| API-002 | Public APIs live under `/public` |
| API-003 | Admin APIs live under `/admin` |
| API-004 | Auth APIs live under `/auth` |
| API-005 | Responses are consistent |
| API-006 | List endpoints require pagination |
| API-007 | Validation errors are field-level |
| API-008 | Admin write APIs create audit logs |
| API-009 | OpenAPI documentation is maintained |
| API-010 | JPA entities are never exposed directly |

## Public APIs

| API ID | Method | Endpoint | Purpose | Auth |
|---|---|---|---|---|
| API-PUB-001 | GET | `/api/v1/public/{lang}/home` | Home data | No |
| API-PUB-002 | GET | `/api/v1/public/{lang}/pages/{slug}` | Page detail | No |
| API-BLOG-001 | GET | `/api/v1/public/{lang}/posts` | Post list | No |
| API-BLOG-002 | GET | `/api/v1/public/{lang}/posts/{slug}` | Post detail | No |
| API-BLOG-003 | GET | `/api/v1/public/{lang}/categories` | Categories | No |
| API-BLOG-004 | GET | `/api/v1/public/{lang}/tags` | Tags | No |
| API-PORT-001 | GET | `/api/v1/public/{lang}/portfolio` | Portfolio list | No |
| API-CONTACT-001 | POST | `/api/v1/public/contact` | Send contact message | No |
| API-SEO-001 | GET | `/sitemap.xml` | Sitemap | No |
| API-SEO-002 | GET | `/robots.txt` | Robots | No |

Public endpoints must return only Published content and must respect language-specific slugs and missing translation behavior.

## Auth APIs

| API ID | Method | Endpoint | Purpose | Auth |
|---|---|---|---|---|
| API-AUTH-001 | POST | `/api/v1/auth/login` | Login | No |
| API-AUTH-002 | POST | `/api/v1/auth/logout` | Logout | Yes |
| API-AUTH-003 | GET | `/api/v1/auth/me` | Current user | Yes |

## Admin APIs

The verified Pack B content-management routes are all restricted to `ADMIN` or
`SUPER_ADMIN`; `EDITOR` is not an MVP authority. `GET` collections return
`PageResponse<DTO>` and all mutation/detail responses are DTOs.

| Area | Methods | Endpoint | Contract |
|---|---|---|---|
| Managed pages | GET, POST | `/api/v1/admin/pages` | Paged localized list; create |
| Managed pages | GET, PUT, DELETE | `/api/v1/admin/pages/{id}` | Detail, versioned update, versioned soft delete |
| Managed pages | POST | `/api/v1/admin/pages/{id}/{publish,archive}` | Versioned lifecycle transitions |
| Blog posts | GET, POST | `/api/v1/admin/blog/posts` | Paged list; localized draft create |
| Blog posts | GET, PUT, DELETE | `/api/v1/admin/blog/posts/{id}` | Detail, versioned update, versioned soft delete |
| Blog posts | POST | `/api/v1/admin/blog/posts/{id}/{publish,archive}` | Versioned lifecycle transitions |
| Blog categories | GET, POST | `/api/v1/admin/blog/categories` | Paged localized list; create |
| Blog categories | GET, PUT, DELETE | `/api/v1/admin/blog/categories/{id}` | Detail, versioned update, versioned deactivate |
| Blog tags | GET, POST | `/api/v1/admin/blog/tags` | Paged localized list; create |
| Blog tags | GET, PUT, DELETE | `/api/v1/admin/blog/tags/{id}` | Detail, versioned update, versioned deactivate |
| Skill categories | GET, POST | `/api/v1/admin/skills/categories` | Paged, sorted localized list; create |
| Skill categories | GET, PUT, DELETE | `/api/v1/admin/skills/categories/{id}` | Detail, versioned update, versioned deactivate |
| Skills | GET, POST | `/api/v1/admin/skills` | Paged, sorted localized list; create |
| Skills | GET, PUT, DELETE | `/api/v1/admin/skills/{id}` | Detail, versioned update, versioned deactivate |
| Portfolio projects | GET, POST | `/api/v1/admin/portfolio/projects` | Paged, sorted localized list; create with active ordered references |
| Portfolio projects | GET, PUT, DELETE | `/api/v1/admin/portfolio/projects/{id}` | Detail, versioned update, versioned soft delete |
| Portfolio projects | POST | `/api/v1/admin/portfolio/projects/{id}/{publish,archive}` | Versioned lifecycle transitions |
| Publications | GET, POST | `/api/v1/admin/publications` | Paged localized list; localized draft create |
| Publications | GET, PUT, DELETE | `/api/v1/admin/publications/{id}` | Detail, versioned update, versioned soft delete |
| Publications | POST | `/api/v1/admin/publications/{id}/{publish,archive}` | Versioned lifecycle transitions |
| Resume entries | GET, POST | `/api/v1/admin/resume/entries` | Paged localized list; localized draft create |
| Resume entries | GET, PUT, DELETE | `/api/v1/admin/resume/entries/{id}` | Detail, versioned update, versioned soft delete |
| Resume entries | POST | `/api/v1/admin/resume/entries/{id}/{publish,archive}` | Versioned lifecycle transitions |
| Resume documents | CRUD | `/api/v1/admin/resume/documents` | One managed document per locale, backed by an active media ID |
| Resume documents | POST | `/api/v1/admin/resume/documents/{id}/{publish,archive}` | Versioned lifecycle transitions |

For every verified Pack B list, `page >= 0` and `1 <= size <= 100`; supported
sorts are allow-listed and include an ID tie-breaker, while fixed-order lists
use repository-defined deterministic ordering. Mutations require the CSRF
token and write a sanitized, actor-attributed audit event. `version` is
returned by applicable detail/create/update contracts and is required for
updates, deactivation, deletion, publishing, and archiving.

Publication and resume public reads use their matching locale paths:
`GET /api/v1/public/{locale}/publications`,
`GET /api/v1/public/{locale}/publications/{slug}`, and
`GET /api/v1/public/{locale}/resume`. They expose only non-deleted Published
records in deterministic order; resume documents expose a public media URL,
never a storage key.

## Later APIs

| API ID | Method | Endpoint | Purpose | Auth |
|---|---|---|---|---|
| API-TEL-001 | POST | `/api/v1/telegram/webhook` | Telegram draft webhook | Secret |

Telegram must create drafts only and must not directly publish public content.

## Standard Error Response

```json
{
  "timestamp": "2026-07-09T10:00:00Z",
  "path": "/api/v1/admin/posts",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fields": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ],
  "requestId": "req-123"
}
```

## API Implementation Rules

- Use DTOs for request and response models.
- Use consistent pagination for list responses.
- Do not leak stack traces or internal details in public errors.
- Use field-level validation errors for forms.
- Keep public and admin DTOs separate when their data exposure differs.
