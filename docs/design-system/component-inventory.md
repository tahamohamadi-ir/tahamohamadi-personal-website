# Component Inventory

This inventory defines the first component families. It prevents ad hoc components and keeps Codex and other agents aligned.

## 1. Shared primitives

| Component | Purpose | Likely Quasar base |
|---|---|---|
| `TmButton` | Governed primary, secondary, quiet and destructive actions | `QBtn` |
| `TmIconButton` | Accessible icon-only action | `QBtn` + `QIcon` |
| `TmLink` | Internal/external text links with consistent states | `RouterLink` / anchor |
| `TmTag` | Topic, technology and status metadata | `QChip` or semantic span |
| `TmDivider` | Controlled visual separation | `QSeparator` |
| `TmMediaFrame` | Stable image/video/diagram ratio, loading and failure | `QImg` / native media |
| `TmStatusPanel` | Loading, empty, error, offline and translation states | `QBanner`, `QSkeleton` |
| `TmDialog` | Branded modal shell with focus and action layout | `QDialog` |
| `TmSectionHeader` | Section title, description and optional action | semantic markup |
| `TmSurface` | Semantic surface variants | native container |
| `TmCard` | Base independent-entity card | optional `QCard` |

## 2. Public components

| Component | Responsibility |
|---|---|
| `PublicHeader` | Primary navigation, locale, responsive drawer, scroll state |
| `PublicFooter` | Secondary navigation, contact and social context |
| `HomeHero` | Brand positioning and primary actions |
| `PracticeRail` | Engineer, Researcher, Builder and HCI practice overview |
| `ProjectCard` | Image, domain, role, summary, outcome and destination |
| `FeaturedProject` | Large selected-work composition |
| `ResearchFeature` | Research question, method, status and artifact |
| `ArticleCard` | Category, title, summary, date and reading time |
| `PublicationRow` | Citation-focused publication presentation |
| `TimelineItem` | Experience, education and interdisciplinary path |
| `CapabilityGroup` | Skills grouped by outcomes, not progress bars |
| `PhotographyRail` | Curated visual sequence |
| `ContactCallout` | Strong final CTA |
| `ReadingProgress` | Article progress where useful |
| `TableOfContents` | Long-form navigation |
| `RelatedContent` | Related work, research or writing |

## 3. Admin components

| Component | Responsibility |
|---|---|
| `AdminShell` | Drawer, toolbar and routed page area |
| `AdminPageHeader` | Page title, context and primary action |
| `AdminDashboardMetric` | Real operational metric only |
| `AdminAttentionList` | Missing translations, failures and drafts needing action |
| `AdminDataTable` | Consistent table states, actions and responsive behavior |
| `AdminFormSection` | Grouped form fields with heading and help |
| `AdminTranslationTabs` | Locale completion and switching |
| `AdminLifecycleActions` | Save, preview, publish, archive |
| `AdminDirtyState` | Unsaved-change visibility and navigation protection |
| `AdminPreviewPanel` | Localized public preview |
| `AdminMediaPicker` | Select, upload and reuse media |
| `AdminSeoPanel` | SEO fields and preview |
| `AdminOperationStatus` | Success, failure and conflict feedback |

## 4. Variants

### `TmButton`

- `primary`
- `secondary`
- `quiet`
- `text`
- `destructive`

Sizes:

- `sm`
- `md`
- `lg`

Rules:

- no pill shape by default;
- no gradient;
- loading state preserves width;
- icons remain secondary to the label;
- one primary action per local region.

### `TmCard`

- `project`
- `article`
- `research`
- `media`
- `admin-panel`

Rules:

- the variant reflects information anatomy, not only color;
- interactive cards have one clear destination;
- nested cards are prohibited unless a real hierarchy requires them.

### `TmStatusPanel`

- `loading`
- `empty`
- `recoverable-error`
- `offline`
- `translation-unavailable`
- `forbidden`
- `conflict`
- `success`

## 5. File-boundary policy

Prefer focused files:

```text
frontend/src/components/
  shared/
  public/
    home/
    work/
    research/
    writing/
    about/
  admin/
  content/
```

Shared behavior belongs in composables or helpers, not copied across page components.

No page component should become a monolithic design-system implementation.
