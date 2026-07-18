# Design Foundation

## 1. Status and Scope

**Status: foundation approved for documentation; runtime implementation is partial.** This document consolidates the current design direction and implementation rules for TahaMohamadi.ir. It is a foundation for public M1 work and a bounded reference for a future admin interface; it is not a page redesign, a theme rebuild, or an admin-UI specification.

- **Approved now:** Modern Clean + Academic Editorial direction; light-first public presentation; semantic token consumption; bilingual RTL/LTR parity; accessible, responsive, SSR-safe public UI; restrained feedback and motion.
- **Current implementation observations:** `tokens.scss` supplies a compact light token set; `PublicLayout` owns the sole public `main#main-content`; the public shell, collections, and page-state components already express several of these conventions.
- **Pending implementation:** complete semantic token coverage, contrast acceptance, route-level content/data integration, full state integration, and browser/assistive-technology evidence.
- **Deferred visual-polish decisions:** final assets, font delivery, icon family, page-specific composition, motion details, and admin workflows.

This document does not claim that planned work is implemented or verified. It defines the foundation needed to deliver it without premature redesign.

## 2. Product and Design Context

TahaMohamadi.ir is a bilingual Persian/English personal, academic, professional, portfolio, publication, and writing site. It must help academic reviewers evaluate research and publications, employers scan skills and experience, technical readers read structured work, and visitors reach contact paths quickly.

The public site uses Vue 3, Quasar 2, Pinia, Vue Router, and SSR/hybrid rendering. Public content is CMS-backed and must remain factual, localized, semantic, and SSR-visible. The future admin area is outside M1; this foundation only records reusable form, table, dialog, and state rules it will need.

## 3. Design Direction

**Modern Clean + Academic Editorial** means a light, restrained, content-first system in which typography, reading measure, hierarchy, evidence, and deliberate whitespace do more work than decoration. It should feel credible for a research profile and publication archive, practical for a portfolio and resume, and professional without adopting generic corporate or SaaS styling.

Use visual refinement to clarify content and actions, not to market around missing content. The direction explicitly rejects generic AI-generated appearance, glassmorphism, decorative gradients, glow effects, excessive cards, arbitrary shadows, oversized empty hero areas, decorative motion, inconsistent accents, excessive density, and premature dark mode.

## 4. Design Principles

1. **Evidence before promotion.** Present supported identity, research, writing, projects, publications, and contact paths without invented claims or placeholders.
2. **Reading before decoration.** Use hierarchy, typography, measure, spacing, and dividers before cards, elevation, or ornament.
3. **Equal locales.** Persian RTL and English LTR are first-class experiences; unavailable translated content is explicit, never silently substituted.
4. **One clear task at a time.** Give each context one primary action and make secondary, tertiary, and destructive actions visibly subordinate.
5. **Stable, accessible behavior.** Preserve landmarks, headings, focus, keyboard operation, responsive reflow, and predictable state feedback.
6. **Proven repetition earns reuse.** Reuse shared primitives for repeated behavior; keep route-specific composition local until repetition is demonstrated.

## 5. Sources of Truth and Precedence

Accepted product and architectural decisions, accepted backend contracts, controlling plans, and repository rules remain binding. Within design implementation, use this precedence order:

1. Product and architectural decisions approved for this repository.
2. `docs/ui-ux/DESIGN.md` for human-readable design rules.
3. `frontend/src/css/tokens.scss` for runtime design tokens and executable values.
4. Shared Vue/Quasar components for implemented behavior.
5. Individual pages only for route-specific composition.

Higher sources win on conflict. `DESIGN.md` owns semantics, rationale, state, and governance; `tokens.scss` alone owns executable token names, values, and breakpoints. Do not create `MASTER.md`, `UI-RULES.md`, `STYLE-GUIDE.md`, `tokens.css`, another design-system directory, or another runtime token authority. Existing UI/UX records remain supporting canonical documentation and decision evidence; this file consolidates the foundation rather than replacing contract, plan, or implementation truth.

## 6. Color System

The current token file is intentionally compact and light-first. Components must consume semantic roles, never introduce raw color values.

| Semantic role | Current runtime evidence | Foundation rule / follow-up |
|---|---|---|
| Canvas/background | `--tm-canvas` | Approved light page canvas. |
| Surface | `--tm-surface` | Approved default surface for shell, controls, and bounded repeated records. |
| Elevated surface | No token | Prefer borders over elevation. Add only when a concrete dialog/tool use proves a distinct role; deferred. |
| Primary text | `--tm-text-primary` | Default readable content and headings. |
| Secondary and muted text | `--tm-text-secondary` | Current shared role; split only if contrast review proves distinct needs. |
| Border/subtle divider | `--tm-border-subtle` | Preferred separation mechanism. |
| Primary action | `--tm-action-primary` | Reserved for the primary action or active state in a context. |
| Inline link | `--tm-link` | For textual navigation and linked content. |
| Interactive hover/active | No semantic tokens | Do not add component-local literals; define semantic roles after state and contrast review. |
| Focus ring | `--tm-focus-ring` | Visible focus is mandatory and must meet non-text contrast intent. |
| Success, warning, danger | `--tm-success`, `--tm-warning`, `--tm-danger` | Pair each with text/icon and a non-colour cue. |
| Informational | No token | Deferred until a real information-state use is implemented. |
| Disabled foreground/background | No token | Deferred semantic pair; disabled state must remain programmatically disabled and explain unavailability where needed. |

Final contrast acceptance remains provisional (UX-DEC-007). No dark-mode token set is approved in M1.

## 7. Typography

Current implementation uses `Vazirmatn, Tahoma, Arial, sans-serif` for Persian and `'Source Sans 3', 'Segoe UI', Arial, sans-serif` for English, with browser fallbacks. Font hosting, licensing, subsetting, and production delivery are blocked decisions; do not assume locally available fonts are production-ready.

- Body text uses the current readable base and deliberately looser Persian line-height. Runtime font metrics and scale remain implementation-owned.
- Headings express document hierarchy, not promotional scale. Route pages own one visible H1; shared child components do not add another H1.
- Current page-title behavior is a practical baseline: bold, constrained to prose measure, and enlarged at the existing tablet breakpoint. Persian removes negative tracking and uses a looser heading line-height.
- Body, meta, navigation, and controls use readable regular/medium/bold weights; avoid ultra-light text and excessive weight jumps. Small/meta text remains legible and secondary, not merely faint.
- Prose stays within the runtime prose measure. Long Persian and English headings wrap; do not truncate essential titles. Use `overflow-wrap` where unbroken content can overflow.
- Dates, identifiers, URLs, email addresses, DOI values, code, and version strings remain copyable and LTR-isolated in Persian. Use locale formatting only when source data and implementation support it.

## 8. Spacing, Sizing, and Layout

Use the existing `--tm-space-*` scale rather than inventing one-off gaps. The runtime `--tm-page-max-width`, `--tm-prose-max-width`, `--tm-control-min-size`, `--tm-gutter-*`, and `--tm-header-height` tokens establish executable shell, reading, target, gutter, and chrome values.

- Start with one readable column; add columns only when the reading task remains clear.
- Use comfortable vertical section rhythm for content pages; use compact density only for dense metadata or future administration where scanning needs it.
- Header and footer preserve their existing token-based spacing. Fixed chrome or actions must reserve content space.
- Sparse and empty pages retain a modest reading structure and a useful next action, not a decorative hero or card stack.
- Preserve stable media aspect ratios and reserve space before content loads to prevent layout shift.

## 9. Borders, Radius, and Elevation

The current `--tm-radius-control`, `--tm-radius-card`, and `--tm-radius-dialog` tokens signal restrained hierarchy, not rounded decoration. Prefer `--tm-border-subtle` dividers and boundaries before adding elevation.

Elevation has no runtime token and is not a general card treatment. It may be introduced only for a justified overlay/tool surface after a semantic token and contrast/state review. Avoid arbitrary shadows, decorative blur, and nested surface framing.

## 10. Public Shell

`PublicLayout.vue` owns the public shell: skip link, `SiteHeader`, the sole `<main id="main-content" tabindex="-1">`, route view, and `SiteFooter`. Child pages and shared inline states must not create another `main` or `q-page`; route pages own their visible H1.

`SiteHeader` provides persistent localized navigation, active-route indication, language access, desktop navigation, and a labelled mobile drawer. The drawer must support keyboard operation, Escape, a visible close control, focus containment, and restoration to its trigger. `SiteFooter` provides secondary navigation and rights information without competing with primary content. Both use token-based, logical, direction-aware layout and remain SSR-deterministic.

## 11. Navigation and Language Switching

Navigation is labelled, deep-linkable, locale-aware, and persistent. The active route is distinguishable by more than position alone. On small screens, mobile navigation may use the existing labelled drawer; language and contact remain reachable.

`LanguageSwitch` changes to the target locale. For a translated detail page, an API-provided `alternatePath` is immutable and must be used unchanged. When absent, the only permitted recovery is the target locale root, and it must not be represented as a translation of the current detail page. Do not derive a translated route by replacing locale or slug text. Shared-header propagation of route-specific alternates remains planned (UX-DEC-013).

## 12. Shared Page Introduction

A public content page may use a compact introduction composed of:

- an eyebrow/category label only when it helps orientation;
- one visible H1;
- an optional concise lead;
- optional supported metadata or actions; and
- consistent separation before the first content section.

Use semantic markup, the runtime prose measure, and logical CSS. Do not require decorative hero content, oversized empty space, or generic value-proposition copy on every route.

## 13. Page and Data States

Use the canonical taxonomy from `09-interaction-and-page-states.md`. States are distinct, applicable only where relevant, and never an unexplained blank.

- **Initial loading:** use a hierarchy-matched skeleton that reserves space; replace content only when no prior data exists.
- **Refreshing/stale:** keep valid old content visible only when a cache exists, identify it as stale, and offer refresh feedback.
- **Empty:** name the domain-specific absence and offer a safe next action; do not reuse generic placeholder art.
- **Recoverable failure/offline:** explain the safe recovery and offer retry when appropriate; offline describes transport, not an empty collection.
- **Partial data:** show supported records, identify unavailable portions where material, and never invent missing facts.
- **Unavailable translation/not found:** replace unavailable content with localized recovery; only an API alternate may link to another localized detail.
- **Forms/admin where relevant:** distinguish submitting, validation, disabled, read-only, unauthorized, forbidden, success, and destructive confirmation.

`PageState.vue` and `TranslationUnavailable.vue` are implementation observations, not proof that every route is integrated or browser-verified.

## 14. Buttons, Links, and Interactive Controls

Use the fewest meaningful variants:

- **Primary:** one highest-priority command in its context, using the primary action role.
- **Secondary:** a bounded, visible alternative using the surface/border/link hierarchy.
- **Tertiary/text:** inline or low-emphasis action that remains plainly interactive.
- **Destructive:** visually and spatially separate, use danger semantics plus explicit confirmation when irreversible.

Links navigate; buttons perform actions. External links identify the destination where context requires it and preserve secure external-link behavior. Disabled controls use native disabled semantics, do not appear actionable, and have a reason where meaningful. Loading prevents duplicate actions without discarding user context. Icon-only controls require an accessible name. All controls need a visible `:focus-visible` treatment, keyboard operation, non-layout-shifting hover/press feedback, and a 44px minimum target.

## 15. Forms

Future forms, including contact and administration, use persistent visible labels, suitable input types, localized helper text, required indicators, and errors adjacent to the affected field. Group related fields with semantic structure and consistent token-scale gaps.

On failed submit, focus the first invalid field. Multi-field failures include a linked error summary. Preserve safely recoverable input, allow one active submission, and keep validation, disabled, read-only, unauthorized, forbidden, and success states distinct. Do not build admin form screens in this foundation batch or document API field schemas outside accepted contracts.

## 16. Tables and Dialogs

For future admin work, tables favor readable scanning over compression: clear headers, stable row hierarchy, deliberate compact/comfortable density, labelled sorting/filtering, and an explicit empty table state. On narrow screens, choose a tested responsive transformation or a horizontally scrollable data region only when tabular relationships require it; do not squeeze unreadable columns.

Dialogs contain a clear title, concise body, ordered actions, a visible close control, Escape handling where appropriate, focus containment, and trigger-focus restoration. Destructive actions require explicit confirmation. These are foundational requirements, not an approved complete admin design.

## 17. Collection and Content Presentation

Choose list, editorial sequence, grid, or card based on the content:

- **Blog and publications:** favor editorial lists with semantic headings, clear metadata, readable excerpts/abstracts, and restrained dividers. Publications preserve copyable identifiers and citation hierarchy.
- **Portfolio:** may use a responsive repeated-record grid when media and comparison help scanning; cards frame records, not every section.
- **Resume and research:** favor chronology, groups, prose, and headings over card stacks.
- **Skills:** use scannable groups; do not use colour alone to indicate proficiency.

Media is optional. Missing media, summaries, dates, metadata, or external links are omitted or represented by an explicit applicable state, never fabricated. Long titles wrap, metadata remains subordinate, pagination appears only for contract-supported collections, and loading/refreshing preserves layout stability.

## 18. Responsive Behavior

Use Quasar's existing breakpoint model and runtime breakpoints; do not create a parallel system. Review behavior at these practical classes:

| Class | Required behavior |
|---|---|
| Small mobile | One column, 44px controls, readable type, no page overflow, labelled drawer navigation. |
| Large mobile | Preserve one-column reading where appropriate; allow stable media and wrapped control groups. |
| Tablet | Increase gutter/space, introduce columns only where records remain readable. |
| Desktop | Persistent desktop navigation and constrained prose; avoid stretching long-form text. |
| Wide desktop | Keep shell and prose maxima; use whitespace for hierarchy, not oversized hero voids. |

Verify 375, 768, 1024, and 1440 logical pixels in both locales, including 200% zoom, before treating a behavior-changing implementation as accepted.

## 19. RTL and LTR

Use CSS logical properties (`inline`, `block`, `margin-inline`, `padding-block`, logical borders and alignment) instead of physical left/right styling. Directional navigation, breadcrumbs, and pagination mirror with locale direction; logos, media, code controls, DOI/URL copy controls, and non-directional icons do not.

Persian uses `lang="fa" dir="rtl"`; English uses `lang="en" dir="ltr"`. Do not create duplicated locale components. Mixed-language values use appropriate LTR isolation. Align form labels, metadata, navigation, and pagination by direction while keeping their semantic reading order coherent.

## 20. Accessibility

Accessibility is non-negotiable and targets WCAG 2.2 AA intent.

- Maintain semantic landmarks, one public main landmark, one visible H1 per route, and sequential headings.
- Keep skip-link and route-change focus behavior; all actions must be keyboard-operable with visual focus and visual/tab order alignment.
- Meet text and UI contrast intent; pair colour with text or icon for status, validation, selection, and feedback.
- Give meaningful images localized alternatives, decorative images empty alternatives, and icon-only controls accessible names.
- Use visible labels, announced nearby errors, first-invalid focus, and linked multi-error summaries for forms.
- Manage dialog/drawer focus, Escape, close controls, and focus restoration.
- Maintain 44px target minimum, wrapping/text scaling, 200% zoom reflow, and `prefers-reduced-motion` support.
- Announce important dynamic status safely with appropriate live-region behavior without stealing focus.

YAGNI never justifies removing any of these requirements.

## 21. Motion and Feedback

Motion is restrained, functional, interruptible, and token-timed. Approved uses are state changes, menu/dialog transitions, loading feedback, and hover/focus/press feedback. Prefer opacity and transform; avoid layout-shifting animation.

Do not add decorative continuous motion, scroll hijacking, large parallax, or motion-heavy landing patterns. Existing runtime motion tokens provide a compact press/state/enter scale; future additions must use semantic tokens and respect `prefers-reduced-motion`.

## 22. Imagery, Icons, and Media

Use approved, purpose-specific portrait, logo, project, publication, and Open Graph assets only. Reserve repeated media space, use responsive media behavior, and supply localized alt text for meaningful images. Do not use production placeholders for required assets.

Use one approved vector icon family with consistent stroke/filled hierarchy; the specific family is unresolved (UX-DEC-010). Do not use emoji as structural icons or introduce a new icon library without approval. Media must not be mirrored merely because the locale direction changes.

## 23. Component and CSS Conventions

- Create a shared component only after repeated behavior exists; otherwise keep markup and page composition local.
- Consume existing semantic tokens. Do not repeat raw colors, spacing, radii, timing, or breakpoints where a semantic token exists.
- Use logical CSS and scoped component styles where suitable; retain shared global rules only for true shell/typography behavior.
- Wrap Quasar components when the wrapper adds a stable semantic, accessibility, or contract boundary. Do not wrap Quasar merely to create a new abstraction layer.
- Public child pages and state components must not render `main`, `q-page`, H1, `lang`, or `dir`; `PublicLayout` owns landmark and locale shell responsibilities.
- Guard browser APIs and keep SSR/client DOM deterministic. Public data/API clients remain request-scoped and same-origin in the browser.
- Shared primitives need focused behavioral/source-contract tests for state, keyboard/focus, locale direction, landmark ownership, and relevant SSR behavior.

## 24. Design Governance

Add a design decision when it changes public semantics, direction, token roles, component conventions, accessibility behavior, or responsive/locale expectations. Update this document when such a human-readable rule changes; update `tokens.scss` only for approved runtime token changes and review affected contrast, states, responsive behavior, and SSR implications.

Document a necessary exception with its scope, rationale, owner, review trigger, and removal/revisit condition in the decision register. Hallmark is used only as an auditor for generic or incoherent visual risks; it does not redesign this product or create Hallmark artifacts. UI UX Pro Max recommendations are evaluated against repository evidence and applicable web constraints. YAGNI excludes speculative abstractions, dependencies, redesigns, and documentation sprawl, while preserving accessibility, locale parity, responsive behavior, states, and governance. Human/project decisions override generic skill guidance.

## 25. Known Gaps and Deferred Decisions

- Final Markdown/rich-content sanitizer decision and implementation are blocked (UX-DEC-011).
- Production font hosting, licensing, subsetting, and delivery are blocked (UX-DEC-008).
- Approved portrait, logo, favicon, project/publication, and Open Graph assets are blocked (UX-DEC-009).
- Final semantic state contrast acceptance is provisional (UX-DEC-007); runtime hover, active, informational, and disabled roles are incomplete.
- Icon family is unresolved (UX-DEC-010).
- Route-specific `alternatePath` propagation through the shared header is planned (UX-DEC-013).
- Canonical/hreflang detail-path alignment is unresolved (UX-DEC-012).
- Docker Compose on a VPS is the MVP deployment decision, but final production SSR/runtime behavior still needs environment-specific release verification.
- Realistic local seed data, final page-specific composition, final motion/asset direction, visual regression coverage, and broader automated accessibility evidence remain planned.
- Admin workflow, language, SEO, and detail behavior require a separate approved scope.

## 26. Foundation Implementation Backlog

**Documentation completed in this task:** DF-001. **Immediate foundation implementation:** DF-002 through DF-006. **QA work:** DF-003 and DF-008. **Deferred final visual polish and admin workflow work:** DF-006 and DF-007; neither authorizes a redesign.

| ID | Title | Objective | Affected files | Acceptance criteria | Dependencies | Explicit non-goals |
|---|---|---|---|---|---|---|
| DF-001 | Document foundation | Complete this canonical source of truth. | `docs/ui-ux/DESIGN.md` | Exact section structure; authority, status, and gaps are explicit. | Existing UI/UX suite and source audit. | Token/CSS or component changes. |
| DF-002 | Complete semantic state tokens | Add only proven hover, active, informational, and disabled semantic roles. | `frontend/src/css/tokens.scss`; affected shared components/tests | No raw state literals; contrast and state review recorded. | UX-DEC-007. | Dark mode or palette expansion. |
| DF-003 | Verify shell interaction | Establish browser evidence for mobile drawer, focus lifecycle, active navigation, and both locales. | Existing shell components and focused tests | Keyboard, Escape, focus restoration, 44px targets, and no overflow pass. | Human-run browser/test environment. | Shell redesign. |
| DF-004 | Integrate canonical states | Connect applicable API-backed routes to loading, empty, failure, offline, stale, unavailable-translation, and not-found behavior. | Public pages, loaders, `PageState.vue`, `TranslationUnavailable.vue`, tests | State priority and recovery match canonical taxonomy in both locales. | Frozen API fixtures/contracts. | Invented content or broad page redesign. |
| DF-005 | Establish page introductions | Apply the compact H1/lead/metadata contract to contract-backed routes as they are implemented. | Relevant public page components and tests | One H1, semantic introduction, locale parity, no decorative hero requirement. | Route content integration. | Marketing landing redesign. |
| DF-006 | Resolve content-safety and assets | Obtain approved sanitizer and real review assets before content/media green work. | Approved dependency/configuration and content/media components | No unsafe raw HTML; meaningful localized alt; stable media areas. | UX-DEC-009 and UX-DEC-011; owner approval. | Placeholder assets or unapproved dependency. |
| DF-007 | Admin foundations review | Apply forms/tables/dialogs rules only under a separate admin plan. | Future admin frontend scope | Approved workflow and role/CSRF contract precede implementation. | Separate admin specification. | Building admin UI in M1. |
| DF-008 | Final visual QA | Run responsive, RTL/LTR, accessibility, SSR/hydration, and contrast review before release. | Affected frontend routes/tests and evidence records | Required viewport/locale matrix and human-run evidence are recorded. | Implemented route content and test environment. | New visual direction. |

## 27. Acceptance Checklist

- [ ] `DESIGN.md` is the single human-readable foundation and does not create a competing design-system artifact.
- [ ] `tokens.scss` remains the sole executable token authority.
- [ ] Public composition is responsive, uses existing Quasar breakpoints, and has no page-level horizontal overflow.
- [ ] Persian RTL and English LTR have equal, logical, direction-safe behavior with no silent locale fallback.
- [ ] Landmarks, one H1, headings, focus, keyboard operation, contrast, 44px targets, reduced motion, and announcements meet the accessibility baseline.
- [ ] Header, footer, skip link, mobile navigation, active route, and language-switch contracts remain intact.
- [ ] Page introductions use a compact semantic H1/lead/metadata model rather than mandatory decorative heroes.
- [ ] Every applicable data/form state uses the canonical state taxonomy and safe recovery.
- [ ] Future forms, tables, and dialogs follow the documented accessibility and hierarchy rules.
- [ ] Collection presentation respects editorial reading patterns, optional media, metadata hierarchy, and supported data only.
- [ ] No competing documentation authority, runtime token file, Hallmark state, or full-page redesign is introduced in the foundation batch.
