# Visual QA Checklist

Use this checklist for every public or admin UI pull request.

## 1. Concept fidelity

- [ ] The accepted concept or design reference is identified.
- [ ] Section order matches the reference.
- [ ] Visible copy matches approved copy.
- [ ] Typography hierarchy matches the design system.
- [ ] Palette and background temperature match.
- [ ] Media treatment matches: crop, mask, overlay, radius and shadow.
- [ ] The page does not fall back to default Quasar appearance.
- [ ] No unapproved card family, gradient, pill, badge or decorative widget was added.

## 2. Responsive QA

Verify at minimum:

- [ ] 360×800
- [ ] 390×844
- [ ] 768×1024
- [ ] 1024×768
- [ ] 1366×768
- [ ] 1440×900
- [ ] 1920×1080 when the layout has large-screen behavior

Check:

- [ ] no horizontal overflow;
- [ ] no clipped primary action;
- [ ] heading line breaks remain intentional;
- [ ] next-section preview remains balanced on common laptops;
- [ ] media crops remain meaningful;
- [ ] cards do not become excessively tall;
- [ ] admin tables and forms remain usable.

## 3. Bilingual QA

- [ ] English route verified.
- [ ] Persian route verified.
- [ ] `lang` and `dir` are correct.
- [ ] logical spacing works in both directions.
- [ ] progression icons mirror only where semantically correct.
- [ ] technical identifiers remain LTR.
- [ ] Persian typography and line height are comfortable.
- [ ] locale switch preserves equivalent content or presents a clear unavailable state.

## 4. Interaction QA

- [ ] hover is supplementary, not required;
- [ ] focus is visible;
- [ ] keyboard order is logical;
- [ ] Escape closes drawers/dialogs where expected;
- [ ] focus returns to the trigger;
- [ ] loading does not cause avoidable layout shift;
- [ ] retry actions work;
- [ ] destructive actions require clear confirmation;
- [ ] reduced-motion mode is usable.

## 5. Accessibility QA

- [ ] one meaningful H1;
- [ ] semantic landmarks;
- [ ] labels and descriptions;
- [ ] contrast AA;
- [ ] minimum target size;
- [ ] alt text;
- [ ] status announcements;
- [ ] no color-only meaning;
- [ ] table semantics;
- [ ] dialog names and focus trap;
- [ ] article headings form a logical outline.

## 6. Performance and SSR

- [ ] SSR HTML contains meaningful content.
- [ ] no hydration warnings.
- [ ] images use dimensions/aspect ratio.
- [ ] below-fold media is lazy-loaded.
- [ ] no unnecessary browser-only dependency.
- [ ] no unreviewed App Extension.
- [ ] route-level and component-level loading signals do not conflict.
- [ ] font loading is measured.

## 7. Admin-specific QA

- [ ] primary action location is consistent;
- [ ] save/publish states are unambiguous;
- [ ] unsaved changes are visible;
- [ ] field errors are local;
- [ ] operation errors are preserved;
- [ ] translation completeness is visible;
- [ ] public preview destination is correct;
- [ ] mobile authoring remains possible;
- [ ] desktop density remains efficient.

## 8. Evidence ledger

Record at least five concrete comparisons:

| Item | Design evidence | Render evidence | Result/fix |
|---|---|---|---|
| First viewport composition |  |  |  |
| Typography |  |  |  |
| Palette |  |  |  |
| Media framing |  |  |  |
| Responsive behavior |  |  |  |
