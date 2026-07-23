# Quasar Capability Matrix

This document maps Quasar capabilities to the public and admin surfaces. It is subordinate to `docs/design.md`.

| Capability | Public experience | Admin experience | Policy |
|---|---|---|---|
| `QLayout` / `QPageContainer` / `QPage` | Core shell | Core shell | Required |
| `QHeader` / `QFooter` | Branded wrapper | Branded functional shell | Required |
| `QDrawer` | Mobile navigation and contextual content | Primary navigation | Required |
| `QPageSticky` | Rare contextual action | Save/publish or contextual tools where justified | Use only as last child of layout parent |
| `QPageScroller` | Scroll-to-top | Optional | Use after measured need |
| `QBtn` | Through `TmButton` for repeated public patterns | Direct or wrapped | Public defaults may not leak |
| `QCard` | Limited, intentional entity cards | Common panels/cards | Prevent card soup |
| `QImg` | Project media and photography frames | Media preview | Wrap for stable ratios and error states |
| `QCarousel` | Only for genuine sequence/gallery | Rare | No automatic decorative carousel |
| `QTabs` / `QTabPanels` | Filters or content modes | Translation and editor modes | Use accessible labels |
| `QExpansionItem` | FAQ or dense supporting content | Settings and advanced fields | Do not hide essential public content |
| `QDialog` | Media/detail actions | Confirmation and editors | Shared dialog rules |
| `QMenu` / `QPopupProxy` | Compact secondary actions | Common | Keyboard and focus QA required |
| `QTooltip` | Nonessential clarification | Helpful for icon actions | Never hold essential information |
| `QTable` | Avoid | Primary data management | Responsive and keyboard QA |
| `QVirtualScroll` | Long archives only after measurement | Large datasets | Add only after profiling |
| `QForm` / fields | Contact and focused public forms | Primary editing system | Shared validation language |
| `QUploader` / `QFile` | None | Media management | Validate file type, size and state |
| `QSkeleton` | Branded content skeletons | Table/form skeletons | Skeleton must match final geometry |
| `QInnerLoading` | Local refresh | Operations | Do not block unrelated UI |
| `QIntersection` | Section reveal and lazy behavior | Limited | Reduced-motion safe |
| Quasar animations/transitions | Curated motion tokens | Minimal state motion | No arbitrary durations |
| Dark plugin | Infrastructure only in release one | Infrastructure only | No public toggle initially |
| Screen plugin | Behavioral changes | Behavioral/density changes | CSS remains primary layout mechanism |
| Meta plugin | Localized SSR SEO and JSON-LD | `noindex` | Required |
| Notify plugin | Contact/action feedback | CRUD and system feedback | Branded variants |
| Dialog plugin | Rare | Confirmation and quick actions | Prefer component dialogs for complex flows |
| Loading Bar | Route/data progress if measured | Useful | Avoid duplicate loading signals |
| Body screen classes | Off by default | Off by default | Enable only after measured need |
| App Extensions | Case-by-case audit | Case-by-case audit | Review maintenance, SSR, bundle, a11y and overwrite risk |
| Icon Genie | Production app icon pipeline | Same app | Use when icon assets are finalized |

## App Extension acceptance checklist

An extension may be introduced only when all answers are satisfactory:

1. Is the repository actively maintained?
2. Does it support the current Quasar and Vue versions?
3. Does it support Quasar CLI with Vite and SSR?
4. Is the bundle impact measured?
5. Is keyboard and screen-reader behavior acceptable?
6. Does it overwrite generated or manually maintained files?
7. Can the feature be implemented reliably with existing Quasar components?
8. Is its visual language fully overridable by design tokens?
9. Is removal documented?
10. Are license and supply-chain risks acceptable?

## Initial plugin target

The foundation plan may activate:

- Meta — already active;
- Dark — dark-ready infrastructure;
- Notify — admin and public operation feedback;
- Dialog — confirmation and simple modal actions;
- LoadingBar — only after route/data UX review.

No extension is approved by this document.
