# Product Backlog

Source: `docs/master-plan.md` v2.0, split into focused backlog items.

## MVP Backlog

| Epic | Related IDs | User Story | Task | Priority | Complexity | Acceptance Criteria |
|---|---|---|---|---|---|---|
| Language | FR-LANGUAGE-001, FR-I18N-001 | As a guest, I want to choose a language so I see content correctly. | Build language page and redirect flow | Must | Low | `/fa` and `/en` redirects work |
| Home | FR-LANDING-001, FR-LANDING-002 | As a visitor, I want a clear landing page so I understand the site. | Home API/UI and featured content | Must | Medium | Hero and featured sections render in fa/en |
| Profile | FR-PROFILE-001, FR-PROFILE-002 | As a visitor, I want a complete profile so I understand Taha's background. | About/profile content model and page | Must | Medium | About sections are scannable |
| Resume | FR-RESUME-001, FR-RESUME-002, FR-SKILL-001 | As a recruiter, I want resume content so I can assess experience. | Resume module, skills, CV media | Must | Medium | Sections display and CV downloads |
| Research | FR-RESEARCH-001 | As a supervisor, I want research interests so I can assess fit. | Research page | Must | Low | Interests display in fa/en |
| Publications | FR-PUBLICATION-001 | As a supervisor, I want publications so I can assess credibility. | Publications module | Must | Medium | Publication status is visible |
| Blog | FR-BLOG-001 through FR-BLOG-005, FR-BLOG-008, FR-BLOG-009 | As a reader, I want posts so I can read and search content. | Blog list/detail/search/taxonomy | Must | Medium | Published posts are visible and searchable |
| Blog Admin | FR-ADMIN-004, FR-AUDIT-001 | As admin, I want draft/publish flow so I control content. | Post CRUD and publish workflow | Must | Medium | Draft, preview, publish, archive work |
| Media | FR-ADMIN-005, NFR-SEC-003 | As admin, I want uploads so I can enrich content. | Media module | Must | Medium | Upload validation rejects invalid files |
| Contact | FR-CONTACT-001, FR-CONTACT-002 | As visitor, I want a contact page so I can send a message. | Contact API/UI and social links | Must | Medium | Message is validated and stored |
| Auth/Admin | FR-ADMIN-001, FR-AUTH-001, FR-AUTH-002, NFR-SEC-001 | As admin, I want secure access so I can manage the site. | Login, logout, me, RBAC | Must | Medium | Guest admin access returns 401/403 |
| SEO | FR-SEO-001, FR-SEO-002, NFR-SEO-001, NFR-AI-001 | As owner, I want metadata so pages are indexed. | Metadata, schema, sitemap, robots | Must | Medium | SSR metadata/schema render |
| Deployment | DEC-008, FR-BACKUP-001, NFR-BACKUP-001 | As owner, I want deployable backups so launch is safe. | Compose, backup, restore docs | Must | Medium | Restore path is documented |

## Post-MVP Backlog

| Epic | Related IDs | Task | Priority | Notes |
|---|---|---|---|---|
| Timeline | FR-TIMELINE-001 | Timeline module | Should | Chronological profile enhancement |
| Blog UX | FR-BLOG-006, FR-BLOG-007, FR-BLOG-010, FR-BLOG-011 | Sort/filter, views, math, syntax highlighting | Should | Improve reading experience |
| Portfolio | FR-PORTFOLIO-002 | Portfolio detail pages | Should | Useful when project detail content grows |
| Admin UX | FR-ADMIN-006, FR-ADMIN-007 | Menu and theme presets | Should | Keep preset-based |
| Analytics | FR-ANALYTICS-001 | Basic dashboard stats | Should | Internal analytics preferred |
| Layout | FR-ADMIN-008 | Preset-only layout management | Could | Avoid open-ended builder |
| Slider | FR-ADMIN-009 | Slider management | Could | Deferred |
| Telegram | FR-TELEGRAM-001 | Telegram draft creation | Could | Draft only, no direct publish |
| Search | FR-BLOG-005 | Advanced search engine evaluation | Later | Use PostgreSQL FTS first |
| User Accounts | ROLE-002 | Public registration | Later | No strong MVP use case |

## Backlog Control Rules

- Every implementation task must cite at least one requirement ID.
- Any new infrastructure must be justified by ADR and MVP impact.
- Public bilingual behavior, SEO, and admin authorization are not optional.
- Telegram, layout builder, public registration, comments, and payment remain outside MVP.
