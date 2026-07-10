# Security Rules

## Security Baseline

Security must be designed from the beginning, especially for Admin Panel, authentication, file upload, and contact form.

## Authentication

1. Admin login is required for all admin routes.
2. Passwords must be securely hashed.
3. Login must be rate-limited.
4. Failed login attempts must be logged.
5. Session or token expiration must be enforced.

## Authorization

1. Use RBAC.
2. Guest users must never access admin APIs.
3. Admin-only APIs must check roles on the backend.
4. Frontend route guards are not enough.
5. Authorization must be tested with negative tests.

## Content Security

1. Sanitize Markdown and Rich Text output.
2. Prevent XSS in blog posts and pages.
3. Do not allow arbitrary script injection.
4. Use safe rendering for HTML content.

## File Upload

1. Validate file extension.
2. Validate MIME type.
3. Validate file size.
4. Store files with generated names.
5. Do not expose internal file paths.
6. Do not allow executable uploads.
7. Add alt text for public images.

## Contact Form

1. Validate name, email, and message.
2. Add rate limiting.
3. Add honeypot or captcha if spam appears.
4. Do not expose internal mail or server errors.

## Secrets

1. Never commit secrets.
2. Use environment variables.
3. Keep `.env` ignored.
4. Provide `.env.example` only.

## Audit Logging

Audit these actions:

- login success
- login failure
- create content
- update content
- delete content
- publish content
- upload media
- change settings
- change SEO metadata

## Security Headers

Production should use:

- HTTPS
- HSTS
- Content-Security-Policy
- X-Frame-Options
- X-Content-Type-Options
- Referrer-Policy
