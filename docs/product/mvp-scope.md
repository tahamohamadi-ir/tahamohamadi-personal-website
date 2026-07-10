# MVP Scope

Source: `docs/master-plan.md` v2.0, summarized to keep the MVP launchable.

## MVP Boundary

The MVP must launch quickly without damaging the future architecture. Keep the product small, bilingual, SEO-aware, secure for admin use, and backed by PostgreSQL and Docker Compose.

## Must Have

| Item | Related IDs | Reason |
|---|---|---|
| Language Selection | FR-LANGUAGE-001, FR-I18N-001 | Bilingual routing is core |
| Landing Page | FR-LANDING-001, FR-LANDING-002 | First impression and entry point |
| About/Profile | FR-PROFILE-001, FR-PROFILE-002 | Professional identity |
| Resume | FR-RESUME-001, FR-RESUME-002, FR-SKILL-001 | PhD and employer review |
| Research Interests | FR-RESEARCH-001 | PhD application value |
| Publications | FR-PUBLICATION-001 | Academic credibility |
| Blog Basic | FR-BLOG-001 through FR-BLOG-005, FR-BLOG-008, FR-BLOG-009 | Content publishing |
| Portfolio Basic | FR-PORTFOLIO-001 | Proof of work |
| Contact | FR-CONTACT-001, FR-CONTACT-002 | Conversion and networking |
| Admin Login | FR-ADMIN-001, FR-AUTH-001, FR-AUTH-002 | Secure administration |
| Admin CRUD | FR-ADMIN-003, FR-ADMIN-004 | Basic CMS |
| Media Management | FR-ADMIN-005 | Rich content support |
| Basic SEO | FR-SEO-001, FR-SEO-002, NFR-SEO-001, NFR-AI-001 | Discoverability |
| Audit Log | FR-AUDIT-001 | Sensitive operation tracking |
| PostgreSQL + Flyway | DEC-004, DB-001, DB-002 | Durable content and migrations |
| Docker Compose Deployment | DEC-008 | Simple MVP operations |
| Backup Script | FR-BACKUP-001, NFR-BACKUP-001 | Data safety |

## Should Have After MVP

- FR-TIMELINE-001: Timeline
- FR-BLOG-006: Sort/filter
- FR-BLOG-007: View count
- FR-BLOG-010: Math formatting
- FR-BLOG-011: Syntax highlighting
- FR-PORTFOLIO-002: Portfolio detail
- FR-ADMIN-006: Menu management
- FR-ADMIN-007: Theme presets
- FR-ANALYTICS-001: Basic analytics
- Publications structured export
- Better SEO dashboard

## Could Have Later

- FR-ADMIN-008: Preset-based layout management
- FR-ADMIN-009: Slider management
- FR-TELEGRAM-001: Telegram draft publishing
- Public user registration
- Advanced search
- Content versioning
- S3-compatible file storage
- `/llms.txt`

## Not Needed Now

Do not add these to MVP unless a new ADR explicitly changes the decision:

- Microservices
- Kubernetes
- Redis
- Elasticsearch or Meilisearch
- Kafka
- External/headless CMS
- Multi-tenant SaaS mode
- Drag-and-drop layout builder
- Public comments
- Payment
- Telegram direct publishing

## MVP Success Criteria

- Persian and English routes work with correct direction.
- Public pages render SEO-readable HTML.
- Admin authentication and authorization protect admin APIs.
- Draft, Published, and Archived states exist for publishable content.
- PostgreSQL migrations are versioned through Flyway.
- Docker Compose deployment path is documented and testable.
