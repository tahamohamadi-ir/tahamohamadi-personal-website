# Product Requirements

Source: `docs/master-plan.md` v2.0, summarized for implementation planning.

## Product Intent

TahaMohamadi.ir is a bilingual Persian/English personal, resume, blog, portfolio, publication, and lightweight CMS website. It must serve as an official, searchable, SEO-friendly, and AI-search-friendly profile for academic, professional, technical, and public audiences.

## Audiences

| ID | Audience | Primary Need | Key Pages |
|---|---|---|---|
| AUD-001 | PhD Supervisor | Research fit, publications, CV | `/en/research`, `/en/publications`, `/en/resume` |
| AUD-002 | University Committee | Academic and research profile | `/en/about`, `/en/publications` |
| AUD-003 | Recruiter / Employer | Skills, experience, projects | `/en/resume`, `/en/portfolio` |
| AUD-004 | Technical Peer | Projects, blog, GitHub | `/blog`, `/portfolio`, `/contact` |
| AUD-005 | Blog Reader | Searchable and categorized content | `/blog` |
| AUD-006 | Public Visitor | Quick identity and contact path | `/`, `/about`, `/contact` |
| AUD-007 | Admin / Site Owner | Content, SEO, media, theme, analytics | `/admin` |
| AUD-008 | Registered User | Possible restricted content later | `/login`, `/register` |

## Value Propositions

| ID | Value |
|---|---|
| VAL-001 | Live, updateable bilingual resume |
| VAL-002 | Professional presentation for PhD, employers, and technical peers |
| VAL-003 | Lightweight custom CMS without WordPress/headless CMS complexity |
| VAL-004 | LLM-friendly structure for AI-assisted development and discovery |
| VAL-005 | Future extensibility without overengineering |
| VAL-006 | SEO and AI visibility designed from the start |
| VAL-007 | Simple admin panel for content management |

## Roles

| ID | Role | Scope |
|---|---|---|
| ROLE-001 | Guest | View public content and submit contact messages |
| ROLE-002 | Registered User | Future-only restricted content role |
| ROLE-003 | Content Editor | Optional future editor role for draft/content work |
| ROLE-004 | Admin | Site/content management |
| ROLE-005 | Super Admin / Site Owner | Full ownership, users, backup/restore |

## Functional Requirements

| ID | Requirement | Priority | Acceptance Criteria |
|---|---|---|---|
| FR-LANGUAGE-001 | Language Selection | Must | Selecting language routes to `/fa` or `/en` |
| FR-LANDING-001 | Landing Page | Must | Persian and English content render correctly |
| FR-LANDING-002 | Featured Content | Must | Admin can choose manual or automatic featured items |
| FR-PROFILE-001 | Biography | Must | Biography text is CMS-managed |
| FR-PROFILE-002 | Full About | Must | About sections are ordered and scannable |
| FR-RESUME-001 | Resume Sections | Must | Admin can add and edit resume sections |
| FR-RESUME-002 | CV Download | Must | CV PDF can be replaced through media management |
| FR-SKILL-001 | Skill Management | Must | Skills support CRUD and categories |
| FR-TIMELINE-001 | Timeline | Should | Timeline sorts correctly by date |
| FR-RESEARCH-001 | Research Interests | Must | Research content supports PhD audience needs |
| FR-PUBLICATION-001 | Publications | Must | Publication status is visible |
| FR-CONTACT-001 | Contact Page | Must | Contact message is validated and stored |
| FR-CONTACT-002 | Social Links | Must | Social links are admin-managed |
| FR-BLOG-001 | Blog List | Must | Published posts list with pagination |
| FR-BLOG-002 | Blog Detail | Must | Language-specific slug is unique |
| FR-BLOG-003 | Category | Must | Category filtering works |
| FR-BLOG-004 | Tag | Must | Tag filtering works |
| FR-BLOG-005 | Search | Must | Title, excerpt, and content are searchable with PostgreSQL FTS |
| FR-BLOG-006 | Sort/Filter | Should | Query params validate correctly |
| FR-BLOG-007 | View Count | Should | View count display is configurable |
| FR-BLOG-008 | Attachments | Must | Files are stored securely |
| FR-BLOG-009 | Markdown/Rich Editor | Must | Admin can preview before publishing |
| FR-BLOG-010 | Math Formatting | Should | Scientific/math content renders correctly |
| FR-BLOG-011 | Syntax Highlighting | Should | Code blocks are readable |
| FR-PORTFOLIO-001 | Portfolio List | Must | Portfolio supports tab/category display |
| FR-PORTFOLIO-002 | Portfolio Detail | Should | Project details, links, and files display |
| FR-ADMIN-001 | Admin Login | Must | Login includes brute-force protection |
| FR-ADMIN-002 | Admin Dashboard | Must | Dashboard summarizes content and messages |
| FR-ADMIN-003 | Page Management | Must | Pages support draft, publish, archive |
| FR-ADMIN-004 | Post Management | Must | Posts support preview and publish flow |
| FR-ADMIN-005 | Media Management | Must | Uploads enforce allowlist and size limit |
| FR-ADMIN-006 | Menu Management | Should | Menu order and labels are language-aware |
| FR-ADMIN-007 | Theme Management | Should | Theme changes are preset-based |
| FR-ADMIN-008 | Layout Management | Could | Layouts are limited to presets |
| FR-ADMIN-009 | Slider Management | Could | Deferred until after MVP |
| FR-AUTH-001 | Authentication | Must | Login, logout, and current-user endpoint are secure |
| FR-AUTH-002 | Authorization | Must | RBAC is enforced on protected APIs |
| FR-I18N-001 | Multilingual Content | Must | Fallback policy is explicit, not silent |
| FR-SEO-001 | SEO Metadata | Must | Metadata renders in SSR public pages |
| FR-SEO-002 | Sitemap/Robots | Must | Valid sitemap and robots output |
| FR-ANALYTICS-001 | Basic Analytics | Should | Admin dashboard shows summary stats |
| FR-TELEGRAM-001 | Bot Draft Publishing | Could | Telegram can create drafts only, never direct publish |
| FR-AUDIT-001 | Audit Log | Must | Sensitive create/update/delete actions are logged |
| FR-BACKUP-001 | Backup/Restore | Should | Backup and restore scripts are documented |

## Non-Functional Requirements

| ID | Category | Requirement | Measurement |
|---|---|---|---|
| NFR-SEC-001 | Security | Admin APIs protected | 100% of admin endpoints require auth |
| NFR-SEC-002 | Security | XSS prevention | XSS tests pass |
| NFR-SEC-003 | Security | Secure uploads | Invalid files rejected |
| NFR-SEC-004 | Security | Brute-force protection | Repeated login attempts blocked |
| NFR-PERF-001 | Performance | LCP under 2.5s | Lighthouse |
| NFR-PERF-002 | Performance | API pagination | Max page size enforced |
| NFR-SEO-001 | SEO | SEO score 90+ | Lighthouse |
| NFR-AI-001 | AI Visibility | Structured content | Schema and semantic headings |
| NFR-A11Y-001 | Accessibility | A11Y score 90+ | Lighthouse/axe |
| NFR-I18N-001 | i18n | RTL/LTR correct | Visual tests |
| NFR-MAINT-001 | Maintainability | Modular code | Module boundaries respected |
| NFR-MAINT-002 | Simplicity | Avoid overengineering | No unnecessary infrastructure |
| NFR-SCALE-001 | Scalability | Future extension without rewrite | Modular monolith |
| NFR-OBS-001 | Observability | Structured logs | Request/error logs include request id |
| NFR-BACKUP-001 | Backup | Recoverable data | Restore drill |
| NFR-PRIV-001 | Privacy | Minimal user data | Data inventory |
| NFR-ADMIN-001 | Admin Usability | Publish under 10 minutes | Admin usability test |
| NFR-COMPAT-001 | Browser | Modern browser support | Smoke tests |
| NFR-MOBILE-001 | Mobile UX | Mobile-first layout | Responsive tests |

## Public i18n and SEO Rules

- Persian public URLs use `/fa/...` with `lang="fa"` and `dir="rtl"`.
- English public URLs use `/en/...` with `lang="en"` and `dir="ltr"`.
- Public content, menus, slugs, and metadata are language-aware.
- Missing translations must show a clear message and link to the available language.
- Public pages require semantic HTML, metadata, canonical URLs, hreflang, Open Graph, and relevant Schema.org JSON-LD.
