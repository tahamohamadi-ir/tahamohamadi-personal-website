# Frontend Design System

Status: approved direction, implementation-ready rules. Color values in this document are **candidates** until their intended foreground/background pairs pass WCAG AA contrast verification.

## 1. Design Goals

- Present a credible academic, professional, and technical profile for PhD supervisors, recruiters, and peers.
- Make research, publications, resume material, and long-form writing easy to scan and read.
- Keep public pages semantic and SSR-readable for SEO and AI discovery.
- Keep the CMS admin calm, practical, and efficient for repeated content work.
- Support Persian RTL and English LTR as first-class layouts, not translations layered over one layout.
- Meet WCAG AA, start mobile-first, and avoid visual excess that competes with content.

## 2. Target Audiences

| Audience | Primary need | Design response |
|---|---|---|
| PhD supervisors and committees | Research fit, publications, CV | Clear hierarchy, evidence-led summaries, readable publication metadata |
| Recruiters and employers | Experience, skills, portfolio | Scannable resume sections and concise project outcomes |
| Technical peers and readers | Blog, code, projects | Comfortable long-form measure and stable technical formatting |
| General visitors | Identity and contact path | Immediate identity, plain navigation, and a clear contact path |
| Site owner/admin | Publish and maintain content | Dense but calm CRUD UI with obvious status and validation |

## 3. Visual Direction

Use **Academic Editorial Utility**: a light-first, content-led visual system with precise hierarchy, restrained surfaces, and a small number of meaningful accents. It should feel closer to a maintained research profile than a marketing site or generic SaaS dashboard.

- Canvas and text establish the hierarchy; color is reserved for action, links, and status.
- Use hairline borders before shadows. Use cards only for repeated items, modal tools, and genuinely framed admin controls.
- Keep public content unframed where possible. Blog prose, publications, and resume sections are editorial layouts, not card stacks.
- No dark mode is introduced by this document. A future dark theme requires a separate approved palette and contrast review.

## 4. Public-Site Layout System

- Use a full-width page band with a constrained inner container, maximum `1200px`.
- Public pages use a compact persistent header, visible language control, semantic `main`, and a footer with useful professional links.
- Default content width is `720px` for prose. A desktop contextual rail may be `280px` wide for on-page navigation, facts, or related work.
- Landing pages make the name, role, and academic/professional focus visible in the first viewport. The next section should remain visibly reachable below the fold.
- Public navigation remains text-labelled. A mobile drawer may replace desktop links; it must not hide essential language switching or contact access.

## 5. Admin Layout System

- Use a functional app shell: header, navigation drawer/sidebar, page title area, and one primary work surface.
- At desktop widths, show a persistent sidebar. At smaller widths, use a labelled navigation drawer.
- Favor tables, filters, forms, and status controls over decorative summaries. Use a maximum of one primary action per page.
- Preserve content status and translation state in list rows and edit headers. Do not rely on color alone.
- Admin is currently English/LTR in the implementation. Localizing the admin shell is an open product decision; content-language controls must still be explicit.

## 6. Responsive Grid and Breakpoints

| Range | Grid | Gutters | Rules |
|---|---:|---:|---|
| `0-599px` | 4 columns | `16px` | One content column; drawer navigation; no horizontal overflow |
| `600-1023px` | 8 columns | `24px` | Supporting content may form two columns when each remains readable |
| `1024-1439px` | 12 columns | `32px` | Public rail and persistent admin sidebar allowed |
| `1440px+` | 12 columns | `32px` | Keep max widths; increase whitespace rather than stretching prose |

Use CSS grid or Quasar layout primitives with stable tracks. Do not scale typography with viewport width. Long-form text should remain approximately 60-75 English characters per line and avoid edge-to-edge Persian paragraphs.

## 7. Color Tokens

All values below are candidates. They become approved only after contrast checks for every stated use and state.

| Token | Candidate | Intended use |
|---|---:|---|
| `canvas` | `#F8FAFC` | Application and public-page background |
| `surface` | `#FFFFFF` | Inputs, dialogs, and constrained item cards |
| `text-primary` | `#142033` | Main text and headings |
| `text-secondary` | `#465367` | Supporting text and metadata |
| `border-subtle` | `#D9E0E8` | Borders and dividers |
| `primary` | `#0F5C5C` | Primary action and active navigation |
| `link` | `#1D4ED8` | Inline links and secondary action emphasis |
| `success` | `#16794C` | Published/success state |
| `warning` | `#A15C00` | Draft or attention state |
| `danger` | `#B42318` | Error and destructive action |

Neutral surfaces must remain visibly distinct without a tinted or beige-heavy palette. Functional colors always require accompanying text, icon, or state label.

## 8. Typography Strategy for Persian and English

- Persian UI, headings, and prose: **Vazirmatn**, weights `400`, `500`, `600`, `700`.
- English UI and general content: **Source Sans 3**, weights `400`, `600`, `700`.
- **Source Serif 4** is an optional future treatment for long-form English article prose only; it is not a global heading font.
- Base text is `16px`. Persian body line-height is `1.9`; English body line-height is `1.65`.
- Use a fixed responsive scale: mobile H1 `32px`, desktop H1 `42px`; body text remains `16-18px`.
- Never use negative letter spacing for Persian. Avoid justified Persian paragraphs. Use locale-aware numerals and tabular figures only where data comparison needs them.

## 9. Spacing, Radius, Border, Elevation, and Motion Tokens

| Category | Tokens |
|---|---|
| Spacing | `4, 8, 12, 16, 24, 32, 48, 64, 96px` |
| Control target | minimum `44px` height and hit area |
| Radius | `4px` controls, `6px` item cards, `8px` maximum for dialogs |
| Border | `1px solid var(--tm-border-subtle)` as the default surface separator |
| Elevation | `none`, `low`, `modal`; shadows are reserved for overlays and dialogs |
| Motion | `120ms` press, `160ms` state, `220ms` enter/exit; opacity and transform only |

Motion must communicate state change, remain interruptible, and never block navigation. No decorative page choreography is part of the MVP.

## 10. Quasar Brand Variable Mapping

Map Quasar brand variables to semantic tokens after contrast verification:

| Quasar variable | Candidate token | Notes |
|---|---|---|
| `$primary` | `primary` | Main command and active state |
| `$secondary` | `link` | Secondary emphasis, not a duplicate primary CTA |
| `$accent` | `link` | Use sparingly for links and focused secondary emphasis |
| `$positive` | `success` | Include Published label/icon where relevant |
| `$negative` | `danger` | Destructive and validation failure states |
| `$warning` | `warning` | Draft and attention state |
| `$dark` | `text-primary` | Semantic ink token only; this does not enable dark mode |

Do not place raw hex values in component code. The Quasar mapping belongs in the brand/theme layer once token verification is complete.

## 11. CSS Custom-Property Mapping

Use semantic custom properties as the application-facing contract:

```css
:root {
  --tm-canvas: <candidate canvas>;
  --tm-surface: <candidate surface>;
  --tm-text-primary: <candidate text-primary>;
  --tm-text-secondary: <candidate text-secondary>;
  --tm-border-subtle: <candidate border-subtle>;
  --tm-action-primary: <candidate primary>;
  --tm-link: <candidate link>;
  --tm-success: <candidate success>;
  --tm-warning: <candidate warning>;
  --tm-danger: <candidate danger>;
}
```

Also expose spacing, radius, motion, z-index, and content-width tokens. Components consume semantic properties, never a visual role inferred from an arbitrary color name.

## 12. Component Rules

- Use Quasar components consistently for controls, feedback, dialogs, drawers, and layouts.
- Use one icon family with consistent stroke and optical weight. Icon-only buttons require accessible names and tooltips.
- Keep repeated content items visually stable with fixed media aspect ratios and predictable metadata placement.
- Do not place page sections inside cards or nest cards. Use cards for repeated records and framed tools only.
- Always define loading, empty, error, disabled, and success states for data-dependent components.

## 13. Button Hierarchy

- **Primary:** one main command per context; filled primary color; clear action verb.
- **Secondary:** outlined or tonal action; used beside, not competing with, the primary action.
- **Tertiary:** text/link action for low-emphasis paths.
- **Destructive:** danger styling, spatially separated, and confirmed for irreversible operations.
- Buttons use familiar icons when the action is recognisable. Text accompanies icons for primary commands and all mobile navigation controls.

## 14. Form and Validation States

- Every input has a persistent visible label, correct input type, helper text where needed, and a minimum `44px` target.
- Validate on blur or submit, not on every keystroke unless immediate feedback prevents harm.
- Errors appear adjacent to the field, explain cause and recovery, and use `aria-live` or an alert role where appropriate.
- After a failed submit, focus the first invalid field and provide a linked error summary for multi-field forms.
- Disabled, read-only, loading, and invalid states must be visually and semantically distinct.

## 15. Status and Feedback Patterns

- Content statuses: Draft, Published, Archived. Each uses label plus semantic color and icon.
- Loading: skeletons for content lists and pages; inline progress for short actions.
- Success: concise confirmation near the action or a polite toast that does not steal focus.
- Error: local explanation, retry/recovery action, and preserved form data where possible.
- Destructive operations: confirmation dialog with explicit consequence and safe cancel path.

## 16. Blog, Publication, Portfolio, and Resume Presentation Rules

- **Blog:** unframed readable prose, visible title/excerpt/date/author, semantic headings, code blocks isolated LTR, and related content after the article.
- **Publications:** structured citation data, status, venue, year, DOI/link, and accessible copy action. Prefer a list with metadata over visual tiles.
- **Portfolio:** repeated project items may use cards with a stable media ratio, outcome summary, role, technologies, and destination link.
- **Resume:** chronological sections with scan-friendly role, organization, dates, and concise achievements. Do not render the resume as a collection of decorative cards.

## 17. Accessibility Requirements

- Public pages use semantic landmarks, one H1, sequential headings, descriptive links, and SSR-readable text.
- Meaningful images have localized alt text; decorative images use empty alt text.
- All interaction is keyboard-operable without hover-only controls or gesture-only paths.
- Respect user zoom and text scaling. Text wraps before truncating; truncation requires a route to full content.
- Preserve visual and DOM order alignment, especially after RTL mirroring.

## 18. WCAG AA Contrast Requirements

- Normal text: at least `4.5:1` against its actual background.
- Large text: at least `3:1`.
- Interactive boundaries, focus indicators, icons, and non-text UI: at least `3:1` where required.
- Validate default, hover, active, focus, disabled, error, success, and selected states independently.
- Candidate tokens cannot be marked approved merely because their base pair passes; every component-state pair needs verification.

## 19. Keyboard and Focus Behavior

- Provide a skip link to main content on public pages.
- Use a visible `2-4px` focus indicator that is not removed or replaced by color-only feedback.
- Tab order follows visual order. Focus moves to main content after route changes.
- Drawers, menus, and dialogs trap focus while open, restore focus to the trigger when closed, and close with Escape when appropriate.
- Tooltips supplement labels; they never contain the only available action name.

## 20. Reduced-Motion Requirements

- Respect `prefers-reduced-motion` by removing nonessential transitions and all decorative animation.
- Do not animate layout dimensions, scrolling, or page navigation for effect.
- Keep essential feedback immediate and non-disorienting; opacity changes may remain only when they clarify state.

## 21. RTL/LTR Implementation Rules

- Persian routes set `lang="fa"` and `dir="rtl"`; English routes set `lang="en"` and `dir="ltr"`.
- Use logical CSS properties: `margin-inline`, `padding-inline`, `inset-inline`, `border-inline`, and `text-align: start`.
- Mirror directional controls, pagination, breadcrumbs, and navigation affordances. Do not mirror logos, media, code controls, or non-directional icons.
- Do not use left/right positioning for component layout except inside explicitly isolated LTR technical content.

## 22. Bidirectional Content Rules

- Code, URLs, DOIs, email addresses, hashes, identifiers, and version strings render in an LTR-isolated element inside Persian content.
- Dates and numerals follow the selected content locale; do not mix locale formatting silently in one metadata row.
- Keep citation punctuation, DOI values, and technical identifiers copyable without invisible direction marks.
- Tables with technical identifiers keep their semantic column order stable; align text with logical start/end rather than physical left/right.

## 23. Responsive Behavior

- Start with the smallest layout and add columns only when content remains readable.
- Public navigation changes to a labelled drawer on small screens; admin navigation becomes a drawer below desktop.
- Tables become stacked records or horizontally scrollable only inside a clearly labelled, bounded data region; page-level horizontal scrolling is forbidden.
- Keep headers, fixed actions, and drawers clear of safe areas and reserve page padding for them.
- Test at `375px`, `768px`, `1024px`, and `1440px`, including RTL at narrow widths.

## 24. Loading, Empty, Error, Success, and Offline States

| State | Required pattern |
|---|---|
| Loading | Skeleton matching the final layout; do not show a blank page for content loading |
| Empty | Plain explanation and the next relevant action; no decorative illustration dependency |
| Error | Explain what failed, preserve context, offer retry or recovery |
| Success | Brief local confirmation or polite toast; do not interrupt keyboard focus |
| Offline | Persistent but unobtrusive network notice, stale-data indication where relevant, and retry when connectivity returns |

## 25. Anti-Patterns to Avoid

- Generic AI purple gradients, glowing effects, bokeh blobs, glassmorphism, and decorative blur.
- Oversized hero type that hides research or professional information; no viewport-scaled display text.
- Excessive cards, nested cards, floating page sections, and arbitrary radius or shadow values.
- Heavy animation, auto-rotating carousels, parallax, or hover-only interaction.
- Beige/cream-heavy palettes, gray-on-gray text, raw hex values in components, and color-only status meaning.
- Latin-only typography assumptions, negative Persian tracking, justified Persian prose, and untranslated fallback content.
- Admin dashboards that prioritize ornament over the publish/edit workflow.

## 26. Design-Review Checklist

- [ ] Direction remains Academic Editorial Utility and content-first.
- [ ] Each candidate color/state pair has a recorded WCAG AA result.
- [ ] Persian and English render with correct `lang`, `dir`, font, and logical spacing.
- [ ] Public content has semantic headings, readable HTML, and no layout shift from media or fonts.
- [ ] Mobile layout has no page-level horizontal scroll and all targets are at least `44px`.
- [ ] Focus order, skip link, dialogs, errors, and reduced motion have been tested.
- [ ] Statuses include text/icon as well as color.
- [ ] Quasar mappings and CSS custom properties are used instead of raw component colors.
- [ ] No dark mode, new dependency, business feature, or UI framework change has been introduced without an explicit decision.

## 27. Open Decisions and Unresolved Items

1. Verify and approve every color token/state pair; candidates in this document are not final brand values.
2. Select font hosting, subsetting, loading strategy, and license review for Vazirmatn, Source Sans 3, and optional Source Serif 4.
3. Decide whether the admin shell itself will be bilingual or remain English/LTR while managing bilingual content.
4. Define exact logo, portrait/media art direction, favicon, and Open Graph asset standards.
5. Decide whether a dark mode is needed after MVP; do not infer it from Quasar defaults.
6. Define the SEO metadata preview, translation completeness, and status-chip component specifications when admin features begin.
7. Add visual regression, accessibility, and RTL/LTR viewport checks when public page implementation starts.
