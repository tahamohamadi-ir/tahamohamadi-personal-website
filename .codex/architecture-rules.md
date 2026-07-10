# Architecture Rules

## Architecture Style

This project uses a Modular Monolith architecture.

Do not introduce microservices in MVP.

## Core Rules

1. Organize backend code by feature modules.
2. Keep module boundaries clear.
3. Public APIs and Admin APIs must be separated.
4. Public pages must be SEO-friendly and AI-search-friendly.
5. Admin features must be simple and usable.
6. Do not introduce unnecessary infrastructure.
7. Do not add Redis, Elasticsearch, Kafka, Kubernetes, or external CMS in MVP.
8. Use PostgreSQL as the primary database.
9. Use Docker Compose for MVP deployment.
10. Keep the system easy to understand, test, deploy, and maintain.

## Backend Module Boundaries

Recommended modules:

- auth
- user
- content
- blog
- portfolio
- media
- seo
- analytics
- admin
- audit
- common

Each module may contain:

- controller
- service
- repository
- entity
- dto
- mapper
- validation
- tests

## Public vs Admin

Public APIs:

- Must expose only published content.
- Must not expose internal IDs when slugs are enough.
- Must support language parameter or language-based route.

Admin APIs:

- Must require authentication.
- Must enforce authorization.
- Must write audit logs for sensitive operations.

## Overengineering Control

Before adding any new technology, answer:

1. Is it required for MVP?
2. Can PostgreSQL or Spring Boot solve it simply?
3. Does it add operational complexity?
4. Does it delay launch?
5. Can it be postponed safely?

If the answer is uncertain, do not add it.
