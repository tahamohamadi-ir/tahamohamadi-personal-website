# Testing Rules

## Testing Philosophy

Testing is required to keep the project maintainable, secure, and safe for AI-assisted development.

Every feature must include tests that match its risk level.

## Required Test Types

Backend:

- Unit tests for services
- Integration tests for repositories
- API tests for controllers
- Security tests for protected endpoints
- Validation tests for DTOs

Frontend:

- Unit tests for utilities and stores
- Component tests for reusable components
- Page-level smoke tests
- E2E tests for critical user flows

Project-wide:

- SEO tests for public pages
- Accessibility checks
- Regression tests before release
- Smoke tests after deployment

## Backend Test Rules

1. Service logic must be unit tested.
2. Repository behavior must be integration tested when custom queries exist.
3. Admin APIs must have unauthorized and forbidden tests.
4. Public APIs must never return draft or archived content.
5. Validation errors must be tested.
6. File upload validation must be tested.
7. Contact form abuse scenarios must be tested.

## Frontend Test Rules

1. Public routes must render without crashing.
2. Language switching must be tested.
3. RTL/LTR behavior must be tested.
4. Loading, empty, error, and success states must be tested.
5. Admin route guards must be tested.
6. Forms must validate required fields.
7. Blog list and blog detail pages must be smoke tested.

## E2E Critical Flows

Required MVP E2E flows:

1. Guest selects Persian language.
2. Guest selects English language.
3. Guest views landing page.
4. Guest views blog list.
5. Guest opens blog detail.
6. Guest submits contact form.
7. Admin logs in.
8. Admin creates draft post.
9. Admin publishes post.
10. Published post appears on public blog.

## Test Quality Rules

1. Tests must be deterministic.
2. Tests must not depend on production data.
3. Tests must use clear names.
4. Tests must cover both success and failure cases.
5. Do not add fragile tests that depend on visual details unless required.
6. Prefer behavior-based assertions.

## Definition of Tested

A feature is considered tested when:

- Unit tests pass.
- Integration tests pass if database/API behavior changed.
- E2E smoke flow passes if public or admin flow changed.
- Security negative tests pass for protected operations.
