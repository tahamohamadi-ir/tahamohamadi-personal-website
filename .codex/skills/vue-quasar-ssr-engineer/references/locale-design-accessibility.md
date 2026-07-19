# Locale, design, and accessibility

Load for public presentation, responsive behavior, CSS, components, navigation, forms, drawers/dialogs, or accessibility review.

## Design-system constraints

- Follow Academic Editorial Utility: restrained, content-first, readable, and stable.
- Consume existing semantic tokens. Runtime values and custom properties belong in `frontend/src/css/tokens.scss`; do not add raw colour values to Vue components or arbitrary spacing when a token exists.
- Do not add gradients, glassmorphism, oversized hero typography, dark mode, decorative card stacks, or generic SaaS styling without an approved decision.
- Reserve stable media space and metadata placement for repeated records. Do not turn prose, sections, or resume chronology into card stacks.
- Use logical CSS properties and alignments. Mirror directional navigation, breadcrumbs, and pagination, but do not mirror logos, media, code controls, or nondirectional icons.

## Responsive and interaction

- Start mobile-first with a readable column and no page-level horizontal overflow. Review 375, 768, 1024, and 1440 logical pixels in both locales when the task needs browser evidence.
- Keep interactive targets at least 44px. Preserve visible focus and honour `prefers-reduced-motion`.
- A mobile navigation drawer needs a label, keyboard operation, an Escape close path, focus containment while open, and restoration to its trigger on close.

## Accessibility checklist

- Provide one H1, sequential headings, semantic landmarks, SSR-readable text, meaningful localized image alternatives, and descriptive links.
- Keep exactly one main landmark for localized public routes.
- Provide a localized skip link that moves focus to `#main-content`; route changes also focus that target.
- Make every action keyboard-operable. Do not rely on hover or gestures alone. Keep tab order aligned with visual order.
- Give icon-only controls an accessible name; a tooltip is not its only name.
- Keep visible labels and nearby announced field errors. On failed form submission, focus the first invalid field and use a linked error summary for multi-field forms.
- Convey status and validation with text or icon as well as colour. Support zoom, wrapping, text scaling, and reduced motion. Target WCAG AA.