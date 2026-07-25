# Taha Mohamadi Unified Design System

**Status:** Canonical design authority
**Version:** 1.0 — Foundation
**Decision date:** 2026-07-22
**Rendering strategy:** Quasar SSR, light-first, dark-ready
**Applies to:** Public website, content pages, interactive experiences, admin CMS, authentication and error surfaces

---

## 1. Document authority

This document is the single source of truth for the visual language, interaction model, information architecture and UI implementation of tahamohamadi.ir.

All implementation work by human developers, Codex, Antigravity, Hermes, OpenRouter models, UI/UX Pro Max or other agents must comply with this document.

### Governance rules

1. No new visible color may be introduced outside the documented token system.
2. No new radius, shadow, spacing value or animation duration may be introduced without updating this document and the machine-readable token file.
3. No public-facing Quasar component should expose default Quasar styling when a branded component family exists.
4. Quasar primitives remain allowed; visual decisions belong to the Taha Mohamadi design system.
5. The public experience and admin experience share one brand foundation but use different density and composition rules.
6. Public content must remain fully usable without animation.
7. Persian and English are first-class experiences. RTL is not a mirrored afterthought.
8. Dark mode infrastructure must exist from the foundation phase, but no public dark-mode toggle is shipped in the first release.
9. No agent may commit, push, merge, tag or deploy without explicit operator authorization for that exact action.

---

## 2. Product and professional context

The website represents an interdisciplinary professional identity spanning:

- software engineering and scalable systems;
- applied AI and human-centered AI research;
- building and shipping production systems;
- human-computer interaction and interaction design;
- information visualization and narrative dashboards;
- visual communication, architecture, UI/UX and photography.

The website must not look like:

- a generic developer portfolio;
- a neon AI landing page;
- an academic CV rendered as a website;
- an enterprise dashboard;
- an art portfolio with no technical depth;
- a default Quasar or Material template.

The website must communicate technical credibility, research depth, delivery capability and visual sensitivity in the first viewport.

---

## 3. Brand narrative

### Primary brand statement

> I research, design, and build complex systems for people.

### Supporting statement

> My work combines software engineering, artificial intelligence, human-computer interaction and visual communication to create reliable, understandable and useful digital systems.

### Persian positioning

> من سیستم‌های پیچیده را برای انسان‌ها قابل‌فهم، قابل‌استفاده و قابل‌اعتماد طراحی و پیاده‌سازی می‌کنم.

### Brand model

The professional identity is not four unrelated roles. It is one interdisciplinary practice expressed through four lenses:

1. **Engineer** — designs reliable architectures and production systems.
2. **Researcher** — investigates human-centered AI, HCI, XAI, wearable health and narrative data systems.
3. **Builder** — converts ambiguous problems into tested, deployed outcomes.
4. **Human-Centered Designer** — shapes interactions, information, visualization and user experience around human needs.

### Visual concept name

**Human-Centered Technical Editorial**

This concept combines:

- engineering precision;
- editorial hierarchy;
- research documentation;
- product and project evidence;
- image composition influenced by photography;
- restrained but meaningful interaction.

---

## 4. Brand personality

| Attribute | Meaning in the interface | Avoid |
|---|---|---|
| Precise | clear hierarchy, aligned grids, exact metadata | sterile bureaucracy |
| Human-centered | understandable copy, visible intent, accessible controls | decorative UX theatre |
| Intelligent | thoughtful structure and evidence | AI clichés and glowing neural graphics |
| Constructive | visible process, decisions and outcomes | vague capability claims |
| Curious | research questions, experiments and visual observations | random novelty |
| Calmly confident | strong typography and generous space | loud gradients and exaggerated claims |
| Visual | real imagery, diagrams, interfaces and photography | stock-like filler |
| Trustworthy | states, timestamps, source context and honest scope | fake metrics and fabricated proof |

---

## 5. Audiences and jobs to be done

### 5.1 Academic supervisors and research groups

They need to understand:

- research direction;
- methodological fit;
- current and planned work;
- technical implementation ability;
- publications and research outputs;
- collaboration potential.

Primary path:

`Home → Research → Research theme/project → Publication or prototype → Resume → Contact`

### 5.2 Technical recruiters and engineering leaders

They need to understand:

- production engineering depth;
- scale and reliability experience;
- role and ownership;
- architecture decisions;
- technologies and outcomes.

Primary path:

`Home → Selected work → Technical case study → Experience/skills → Resume → Contact`

### 5.3 HCI, UX and design collaborators

They need to understand:

- human-centered process;
- information and interaction design ability;
- visualization work;
- design rationale;
- visual practice.

Primary path:

`Home → HCI/Design work → Process and prototype → Visual archive/About → Contact`

### 5.4 Readers and students

They need to find:

- clear writing;
- topic categories;
- reading time;
- related articles;
- practical examples and research notes.

Primary path:

`Search/referral → Article → Related writing → Research/About`

### 5.5 Site administrator

The administrator needs to:

- find any content type quickly;
- create and edit translations;
- preview before publication;
- manage status and version conflicts;
- upload and reuse media;
- verify public output;
- recover from validation and network errors.

Primary path:

`Login → Dashboard → Content type → Edit/create → Preview → Validate translations → Publish → Verify public page`

---

## 6. Experience principles

### 6.1 One strong idea per viewport

Each major section must have one primary purpose, one visual focal point and no more than two competing calls to action.

### 6.2 Evidence before claims

Capabilities are demonstrated through projects, methods, decisions, artifacts and outcomes.

### 6.3 Editorial rhythm, not repeated card grids

Long pages must vary between:

- open text-led sections;
- image-led bands;
- project rails;
- structured lists;
- framed diagrams;
- occasional cards;
- dark or tinted emphasis sections.

### 6.4 Interaction must clarify

Motion may reveal hierarchy, state and continuity. It must not exist only to make the page feel busy.

### 6.5 Bilingual parity

Persian and English must have equivalent hierarchy, content availability, spacing quality and interaction completeness.

### 6.6 Admin efficiency over spectacle

The admin must be clean and branded, but speed, information density and predictable workflows have priority over visual expression.

---

## 7. Information architecture

### Primary public navigation

1. **Work**
2. **Research**
3. **Writing**
4. **About**
5. **Resume**
6. **Contact**
7. **FA / EN**

### Route mapping

The existing routes may remain during migration, but the visible information architecture should converge toward:

- `/[locale]`
- `/[locale]/work`
- `/[locale]/work/:slug`
- `/[locale]/research`
- `/[locale]/research/:slug` where a distinct research-project model exists
- `/[locale]/publications`
- `/[locale]/publications/:slug`
- `/[locale]/writing`
- `/[locale]/writing/:slug`
- `/[locale]/about`
- `/[locale]/resume`
- `/[locale]/photography` or `/[locale]/visual-archive` when sufficient curated content exists
- `/[locale]/contact`

### Navigation consolidation

- `Portfolio` becomes `Work`.
- `Blog` becomes `Writing`.
- `Skills` becomes a capability section inside About, Resume and relevant case studies.
- `Publications` remains directly addressable but is grouped under Research in primary navigation.
- Contact is visually treated as a clear action.
- Photography is initially part of About or Work and becomes a top-level route only when a curated collection is ready.

---

## 8. Surface model

The product uses one brand core and two surface systems.

### 8.1 Public experience

Characteristics:

- expressive;
- editorial;
- image-aware;
- spacious;
- narrative;
- interactive;
- varied section rhythm.

### 8.2 Admin experience

Characteristics:

- functional;
- dense but readable;
- data and form oriented;
- predictable;
- keyboard efficient;
- operationally explicit;
- minimally decorative.

### Shared foundations

Both surfaces share:

- brand colors;
- typography families;
- semantic status colors;
- focus treatment;
- icon family;
- spacing scale;
- component state language;
- motion timing;
- accessibility requirements;
- RTL/LTR behavior.

They do not share the same default density, card composition or page rhythm.

---

## 9. Theme strategy

### Release-one policy

- Light theme is the only user-visible theme in the first release.
- Dark tokens and `body--dark` mappings are implemented and tested.
- Quasar Dark Plugin is configured for future activation.
- No public toggle is rendered.
- Admin also launches light-first.
- Dark mode may be enabled internally during QA.

### Light theme character

- calm technical canvas;
- white primary surfaces;
- deep ink text;
- teal primary action;
- cobalt secondary emphasis;
- coral used only as a limited visual accent;
- subtle cool borders;
- restrained shadows.

### Dark theme character

- deep blue-black canvas;
- blue-gray surfaces;
- off-white text;
- softened teal and cobalt;
- no pure black;
- contrast maintained without glowing neon effects.

---

## 10. Color system

### 10.1 Primitive palette

| Token | Value | Role |
|---|---:|---|
| ink-950 | `#0D1B2A` | primary text and deep surfaces |
| ink-800 | `#26384A` | strong secondary text |
| slate-650 | `#445064` | secondary body text |
| slate-500 | `#68758A` | metadata |
| slate-300 | `#B8C2CF` | disabled or quiet borders |
| slate-200 | `#DDE3EA` | standard border |
| slate-100 | `#EDF1F5` | subtle divider and tinted area |
| canvas | `#F7F8FA` | application page background |
| surface | `#FFFFFF` | primary surface |
| teal-700 | `#0B6E69` | primary action and brand |
| teal-800 | `#075B57` | primary hover |
| teal-100 | `#DDEFEA` | teal tint |
| cobalt-600 | `#2457D6` | secondary emphasis and links |
| cobalt-700 | `#1D46B1` | link hover |
| cobalt-100 | `#E7EDFF` | cobalt tint |
| coral-500 | `#D96C4A` | visual accent only |
| coral-100 | `#F9E8E1` | visual tint |
| success | `#16805B` | success |
| warning | `#A15C00` | warning |
| danger | `#B42318` | destructive/error |
| info | `#1D70B8` | neutral information |

### 10.2 Semantic light tokens

- `--tm-bg-page: #F7F8FA`
- `--tm-bg-surface: #FFFFFF`
- `--tm-bg-surface-subtle: #EDF1F5`
- `--tm-bg-accent-soft: #DDEFEA`
- `--tm-text-primary: #0D1B2A`
- `--tm-text-secondary: #445064`
- `--tm-text-muted: #68758A`
- `--tm-border-default: #DDE3EA`
- `--tm-border-strong: #B8C2CF`
- `--tm-action-primary: #0B6E69`
- `--tm-action-primary-hover: #075B57`
- `--tm-action-secondary: #2457D6`
- `--tm-link: #2457D6`
- `--tm-link-hover: #1D46B1`
- `--tm-focus-ring: #2457D6`

### 10.3 Quasar brand mapping

- `primary`: `#0B6E69`
- `secondary`: `#2457D6`
- `accent`: `#D96C4A`
- `dark`: `#0B1117`
- `positive`: `#16805B`
- `negative`: `#B42318`
- `info`: `#1D70B8`
- `warning`: `#A15C00`

### Color restrictions

- Coral is not permitted for normal body text on white.
- Gradients are not a default brand primitive.
- A gradient may be approved for one specific visual or generated asset, but not for buttons, headings and cards by default.
- Status colors must never be the only signal; pair with icon and text.

---

## 11. Typography

### Families

- **Latin UI and display:** Manrope Variable
- **Persian UI and display:** Vazirmatn Variable
- **Technical metadata/code:** system monospace initially; IBM Plex Mono may be added only if self-hosted and performance-tested.

### Principles

- Use one sans-serif voice across headings and body; hierarchy comes from scale, weight and spacing.
- Persian type requires more line height than Latin.
- Display type is confident but not oversized advertising typography.
- Long-form content uses a restrained measure.

### Type scale

| Style | Desktop | Mobile | Weight | Use |
|---|---:|---:|---:|---|
| Display XL | 72/76 | 44/50 | 650–700 | home hero |
| Display L | 56/62 | 38/44 | 650–700 | major landing heading |
| H1 | 44/52 | 34/42 | 650–700 | inner page title |
| H2 | 32/40 | 28/36 | 650–700 | major section |
| H3 | 24/32 | 22/30 | 600–700 | card/panel title |
| H4 | 20/28 | 18/26 | 600–700 | subsection |
| Body L | 19/31 | 18/30 | 400–500 | lead text |
| Body | 16/27 | 16/27 | 400–500 | default |
| Body S | 14/22 | 14/22 | 400–600 | metadata |
| Label | 13/18 | 13/18 | 600–700 | UI label |
| Mono | 13/20 | 12/19 | 500 | technical metadata |

Persian line-height should be approximately 1.75–1.95 depending on size. Latin body line-height should be approximately 1.55–1.7.

### Reading measure

- Article body: 68–76 Latin characters per line.
- Persian prose: visually equivalent measure, generally 640–760px depending on font size.
- Metadata and code may exceed the prose width only inside dedicated frames.

---

## 12. Spacing, sizing and grid

### Spacing scale

`4, 8, 12, 16, 20, 24, 32, 40, 48, 64, 80, 96, 128`

No arbitrary spacing values should be introduced outside optical corrections smaller than 4px.

### Breakpoints

Use Quasar-compatible breakpoints as the behavioral baseline:

- XS: `< 600px`
- SM: `600–1023px`
- MD: `1024–1439px`
- LG: `1440–1919px`
- XL: `≥ 1920px`

CSS handles layout by default. `$q.screen` is reserved for behavior that cannot be expressed cleanly in CSS.

### Containers

- Public wide: `min(1440px, calc(100vw - gutters))`
- Public standard: `min(1200px, calc(100vw - gutters))`
- Public prose: `720px`
- Admin content: fluid with a readable max width where forms require it
- Admin tables: fluid and horizontally managed rather than artificially narrow

### Gutters

- Mobile: 16px
- Tablet: 24px
- Desktop: 32px
- Large desktop: 48px where the composition benefits

### Section spacing

- Public major section: 96–128px desktop, 64–80px mobile
- Public compact section: 64–80px desktop, 48–64px mobile
- Admin page: 24–32px
- Admin panel gap: 16–24px

---

## 13. Container taxonomy

### Section

A full composition region. Sections do not require borders or cards.

Examples:

- hero;
- selected work;
- research direction;
- visual archive;
- contact call to action.

### Surface

A visual background layer.

Variants:

- default;
- subtle;
- teal tint;
- cobalt tint;
- dark emphasis;
- elevated.

### Card

Use only for an independent entity with a distinct action or destination.

Examples:

- project;
- article;
- publication;
- research project;
- photography collection.

### Panel

Use for structured information or tools.

Examples:

- admin editor;
- filter controls;
- metadata;
- publishing state;
- case-study technical details.

### Frame

Use for visual and technical media.

Examples:

- project screenshot;
- browser/device preview;
- dashboard image;
- architecture diagram;
- data visualization;
- photograph.

### Card-soup prevention

Do not place every paragraph, statistic, skill and heading in separate cards. Use open layout, rules, lists and typographic grouping where the content does not represent an independent entity.

---

## 14. Shape, borders and elevation

### Radius

- Control: 8px
- Compact panel: 10px
- Card: 14px
- Feature card/media frame: 18px
- Dialog: 18px
- Pill: only for tags, filters and compact status; never as the default button shape

### Borders

- Default: 1px solid semantic border
- Strong: 1px solid strong border
- Focus: 2–3px visible ring outside the component
- Decorative hairline: allowed only when contrast remains visible

### Elevation

- Level 0: no shadow
- Level 1: subtle surface separation
- Level 2: hover or floating header
- Level 3: dialog/menu
- Public cards should rely primarily on border, contrast and composition; shadows stay restrained.

---

## 15. Iconography

- Use one coherent icon family through `@quasar/extras`.
- Prefer Material Symbols Rounded or an approved consistent set.
- Do not mix filled, outlined and arbitrary emoji icons.
- Icons must use `currentColor` where possible.
- Navigation icons are used only when they improve scanning.
- Public primary navigation remains primarily textual.
- Admin navigation may combine icon and label.
- Decorative icons may not replace real project imagery or diagrams.

---

## 16. Imagery and photography

### Image hierarchy

1. Real project screenshots and artifacts
2. Original diagrams and data visualizations
3. Original photography
4. Professional portrait
5. Carefully generated supporting visuals
6. Stock imagery only as a last resort

### Photography role

Photography communicates visual observation and authorship. It may appear in:

- hero or About;
- visual archive;
- article covers;
- section transitions;
- selected personal projects.

### Rules

- Every image needs a deliberate crop and aspect ratio.
- Use stable frame families: 16:10, 4:3, 3:2, 1:1 and portrait 4:5.
- Do not apply color overlays unless documented for that asset.
- Use edge fades or matching backgrounds rather than tinting images to force palette compliance.
- Images require descriptive alt text unless decorative.
- Generated placeholder imagery may not ship as final project evidence.

---

## 17. Data visualization

Visualizations must follow the same semantic token system.

Rules:

- prioritize comparison and comprehension;
- provide accessible descriptions;
- avoid rainbow palettes;
- reserve semantic colors for semantic meaning;
- support RTL labels where applicable;
- preserve readable labels at mobile sizes;
- provide tabular or textual alternatives for critical data;
- document uncertainty and missing data.

---

## 18. Motion and transitions

### Timing

- Micro interaction: 100–180ms
- State transition: 180–260ms
- Section/page entrance: 260–500ms
- Long cinematic transitions are not part of the default system.

### Easing

- Standard: `cubic-bezier(0.2, 0, 0, 1)`
- Enter: `cubic-bezier(0.16, 1, 0.3, 1)`
- Exit: `cubic-bezier(0.4, 0, 1, 1)`

### Allowed

- focused hover elevation;
- image scale up to 1.02;
- underline movement;
- drawer and dialog transitions;
- skeleton-to-content transition;
- route fade/translate with small distance;
- section reveal;
- media morph where continuity is meaningful;
- scroll-to-top;
- reading progress.

### Prohibited by default

- permanent particles;
- cursor replacement;
- sound;
- scroll hijacking;
- large parallax movement;
- endlessly animated text;
- simultaneous animation of every card;
- motion needed to understand content.

### Reduced motion

All motion must respect `prefers-reduced-motion`. Essential state changes remain visible without animation.

---

## 19. Accessibility

Baseline target: WCAG 2.2 AA.

Requirements:

- semantic landmarks;
- one meaningful H1 per page;
- keyboard-accessible navigation and dialogs;
- visible focus;
- 44px minimum target size for primary touch controls;
- correct labels and descriptions;
- correct status announcements;
- no color-only meaning;
- alt text and captions;
- reduced-motion support;
- contrast validation;
- logical focus after route changes;
- language and direction attributes updated per route;
- tables and complex visualizations supplied with accessible context.

Accessibility is a release requirement, not a final polish task.

---

## 20. RTL and bilingual behavior

### Principles

- Use CSS logical properties.
- Direction is controlled at the document and layout level.
- Icons indicating progression must mirror where semantically appropriate.
- Media content is not automatically mirrored.
- Technical identifiers remain LTR and isolated.
- Persian copy is written naturally, not translated word-for-word.
- Locale switching preserves the corresponding route where possible.
- Missing translation states must offer a clear alternate-language path.
- Every visual QA pass includes both Persian and English.

---

## 21. Quasar architecture

### Position

Quasar is the application framework and component/runtime foundation for both public and admin experiences.

The design system does not fight Quasar. It configures and composes it.

### Quasar-first rules

1. Use `QLayout`, `QPageContainer`, `QPage`, `QHeader`, `QFooter` and `QDrawer` for application shells.
2. Use Quasar plugins and utilities where they provide reliable behavior.
3. Use branded wrapper components for repeated visual patterns.
4. Direct `q-*` usage is permitted for page-specific structures, but styling must use design tokens.
5. Admin may use Quasar components more directly because utility and density are primary.
6. Public pages require deliberate composition and may not become default `QCard` grids.
7. App Extensions require an audit for maintenance, SSR, bundle size, accessibility and file-overwrite behavior before installation.

### Plugins and capabilities

Foundation targets:

- Meta;
- Dark;
- Notify;
- Dialog;
- Loading or LoadingBar where justified;
- Screen for behavioral decisions;
- QIntersection and transitions;
- Cookies only when needed;
- LocalStorage only for non-sensitive preferences.

### Screen policy

- CSS handles layout and visibility.
- `$q.screen` handles interaction mode, dialog mode, drawer behavior and complex density changes.
- Body screen classes are not enabled unless a measured requirement justifies their performance cost.

### Dark plugin policy

- Configure dark-ready tokens.
- Keep release-one public mode fixed to light.
- Do not render a toggle until dark visual QA is complete for all page families.

---

## 22. Component architecture

### Naming

Branded components use the `Tm` prefix.

Examples:

- `TmButton`
- `TmLink`
- `TmCard`
- `TmMediaFrame`
- `TmSectionHeader`
- `TmProjectCard`
- `TmArticleCard`
- `TmPublicationRow`
- `TmStatusPanel`

### Shared primitives

Shared primitives provide:

- tokens;
- accessibility defaults;
- interaction states;
- loading/disabled behavior;
- RTL behavior;
- theme behavior.

### Public components

Public components prioritize:

- hierarchy;
- imagery;
- storytelling;
- content relationships;
- discoverable actions.

### Admin components

Admin components prioritize:

- efficiency;
- validation;
- status;
- density;
- predictable action placement;
- autosave/dirty-state clarity where implemented;
- responsive data management.

### Wrapper policy

Create a wrapper when at least one condition is true:

- the pattern appears in three or more places;
- accessibility defaults must be centralized;
- visual variants must be governed;
- Quasar defaults need systematic branding;
- RTL behavior is nontrivial;
- loading/disabled states are repeated.

Do not create wrappers that merely rename a Quasar component without adding governance or behavior.

---

## 23. Public page templates

### 23.1 Home

1. Hero
2. Four-practice introduction
3. Selected work
4. Research direction
5. Latest writing
6. About/visual snapshot
7. Contact CTA
8. Footer

Hero requirements:

- communicates Engineer, Researcher, Builder and Human-Centered Designer;
- contains one primary and one secondary action;
- uses one strong visual;
- does not use fake metrics, pills or decorative dashboard widgets;
- reveals a preview of the next section on common laptop heights.

### 23.2 Work index

- clear introduction;
- optional filters;
- featured case study;
- project grid/list with varied but consistent media;
- role, domain and outcome metadata;
- no technology-logo wall.

### 23.3 Case study

1. Project statement
2. Role and context
3. Problem
4. Research/design/engineering process
5. Key decisions
6. Architecture or interaction artifacts
7. Outcome and limitations
8. Related work

### 23.4 Research

- research positioning;
- current questions;
- themes;
- active projects;
- selected publications;
- collaboration CTA.

### 23.5 Writing

- one featured article;
- topic filters;
- latest articles;
- title, description, category, date and reading time;
- RSS when available.

### 23.6 Article

- readable header;
- metadata;
- optional table of contents;
- prose and rich content;
- code and visualization frames;
- related writing;
- previous/next navigation.

### 23.7 About

- interdisciplinary narrative;
- visual timeline;
- capabilities;
- selected experience;
- photography/visual practice;
- values;
- resume/contact actions.

### 23.8 Resume

- web resume;
- downloadable PDF;
- experience;
- education;
- research;
- skills grouped by capability;
- selected evidence, not decorative progress bars.

### 23.9 Contact

- direct channels;
- concise expectations;
- real form;
- sending, success, validation and failure states;
- anti-spam behavior;
- privacy note.

---

## 24. Admin experience

### Admin layout

- persistent or responsive drawer;
- top toolbar;
- page title and context;
- primary action in a consistent location;
- global feedback;
- readable content area;
- route-aware breadcrumbs where depth warrants them.

### Admin dashboard

The dashboard should answer:

- what requires attention;
- what is draft, published or archived;
- which translations are missing;
- what changed recently;
- whether media or contact operations failed;
- where the administrator should go next.

### Content editor pattern

1. Entity status and version
2. Translation tabs
3. Core content
4. SEO and canonical fields
5. Media
6. Preview
7. Save
8. Publish/archive
9. Public verification

### Admin density

- controls are more compact than public controls;
- labels remain visible;
- destructive actions are separated;
- forms use sections and panels;
- large editors may use `QSplitter`;
- tables use `QTable` or `QVirtualScroll` where appropriate;
- mobile editing remains possible but desktop is the primary authoring environment.

---

## 25. States and feedback

Every data-driven component defines:

- initial loading;
- background refresh;
- empty;
- partial data;
- translation unavailable;
- validation error;
- recoverable request failure;
- offline/network failure;
- unauthorized/forbidden;
- conflict/version mismatch;
- success;
- destructive confirmation.

### Empty state policy

An empty state explains:

1. what is missing;
2. why it matters;
3. what action is available.

Public empty states should not expose internal CMS terminology.

### Error policy

- preserve user input;
- provide retry where meaningful;
- show field errors near fields;
- show operation-level errors at the operation boundary;
- log technical detail without exposing it to public users.

---

## 26. Content and microcopy

### Voice

- direct;
- specific;
- calm;
- evidence-based;
- technically accurate;
- understandable to non-specialists.

### Avoid

- “passionate about” without evidence;
- “cutting-edge”;
- “innovative solutions”;
- exaggerated scale claims;
- long hero paragraphs;
- invented testimonials;
- fabricated statistics.

### CTA language

Prefer:

- Explore the work
- Read the case study
- View research
- Read the article
- View resume
- Start a conversation

Avoid vague labels such as:

- Learn more
- Click here
- Discover now

unless context makes their destination unmistakable.

---

## 27. SEO and structured metadata

Each public route must define:

- localized title;
- localized description;
- canonical URL;
- alternate-language links;
- Open Graph metadata;
- Twitter metadata;
- appropriate structured data;
- noindex behavior for unavailable or administrative routes.

Structured-data candidates:

- Person;
- ProfilePage;
- Article;
- BlogPosting;
- ScholarlyArticle;
- CreativeWork;
- SoftwareApplication where accurate;
- BreadcrumbList.

Metadata must be generated during SSR.

---

## 28. Performance

Public goals:

- keep first viewport focused;
- lazy-load below-the-fold imagery;
- supply responsive image sizes;
- avoid unnecessary JavaScript for layout;
- prefer CSS transitions;
- audit Quasar imports and plugins;
- avoid installing App Extensions for capabilities already available locally;
- prevent font loading from blocking meaningful content;
- preserve SSR and hydration correctness.

Animations and visual richness do not justify poor loading behavior.

---

## 29. Design-system deliverables

The canonical design package consists of:

- `docs/design.md`
- `docs/design-system/design-tokens.json`
- `docs/design-system/quasar-capability-matrix.md`
- `docs/design-system/component-inventory.md`
- `docs/design-system/agent-ui-rules.md`
- `docs/design-system/visual-qa-checklist.md`
- implementation specifications and plans under `docs/superpowers/`

Future visual concepts and accepted screenshots must be stored under a versioned design-reference path and cited from this document.

---

## 30. Implementation sequence

1. Design documentation and governance
2. Machine-readable tokens
3. Quasar theme and Sass variables
4. Shared foundations
5. Shared branded primitives
6. Public shell
7. Home
8. Work and case studies
9. Research and publications
10. Writing and articles
11. About, resume and contact
12. Admin shell and high-value workflows
13. Dark-theme QA without public toggle
14. Visual regression, accessibility and performance QA

---

## 31. Definition of done

A design-system phase is complete only when:

- English and Persian are verified;
- desktop, tablet and mobile are verified;
- keyboard operation is verified;
- reduced motion is verified;
- light theme is production ready;
- dark tokens do not produce unreadable states;
- SSR build succeeds;
- unit tests pass;
- visual comparison is documented;
- no new ungoverned visual values are introduced;
- public pages do not look like default Quasar examples;
- admin remains faster and clearer than before.

---

## 32. Reference direction

The design direction is informed by the strongest patterns from:

- Apple: focal hierarchy and concise section composition;
- Mike Matas, Spencer Gabor, Perry Wang and Will Lenzen: work-led portfolios;
- Gabriel Valdivia: positioning, capabilities and structured case-study storytelling;
- Paul Stamatiou, Amitness, Notion and Help Scout: durable writing and editorial information architecture;
- Marco and other expressive personal sites: distinctive but restrained interaction;
- Digikala: native RTL behavior and Persian interface considerations;
- the provided portfolio sample: responsive section structure and interaction ideas, excluding particles, fake metrics and template-like gradient treatment;
- the professional CV: engineering, research, building, HCI, visual communication and photography.

These references inform principles. The implementation must remain original.
