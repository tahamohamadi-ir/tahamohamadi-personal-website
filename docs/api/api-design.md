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

| API ID | Method | Endpoint | Purpose | Roles |
|---|---|---|---|---|
| API-ADM-POST-001 | GET | `/api/v1/admin/posts` | Admin post list | Admin/Editor |
| API-ADM-POST-002 | POST | `/api/v1/admin/posts` | Create post | Admin/Editor |
| API-ADM-POST-003 | PUT | `/api/v1/admin/posts/{id}` | Update post | Admin/Editor |
| API-ADM-POST-004 | POST | `/api/v1/admin/posts/{id}/publish` | Publish post | Admin |
| API-ADM-PAGE-001 | GET | `/api/v1/admin/pages` | Page list | Admin |
| API-ADM-PAGE-002 | POST | `/api/v1/admin/pages` | Create page | Admin |
| API-ADM-MEDIA-001 | POST | `/api/v1/admin/media` | Upload media | Admin/Editor |
| API-ADM-SEO-001 | PUT | `/api/v1/admin/seo/{entityType}/{id}` | Update SEO metadata | Admin |
| API-ADM-ANL-001 | GET | `/api/v1/admin/analytics/summary` | Admin stats | Admin |

Admin APIs require authentication, authorization, input validation, and audit logging where sensitive.

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
