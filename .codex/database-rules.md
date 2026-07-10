# Database Rules

## Database

Use PostgreSQL as the primary database.

## Migration

1. Use Flyway for all schema changes.
2. Never change production schema manually.
3. Every schema change must have a migration file.
4. Migration names must be clear and sequential.

## Core Modeling Rules

1. Use normalized tables for core content.
2. Use JSONB only for flexible settings.
3. Do not store main business data only in JSONB.
4. Use translation tables for multilingual content.
5. Use soft delete for admin-managed content.
6. Add audit fields to important tables.

## Required Common Fields

Important tables should include:

- id
- created_at
- updated_at
- created_by
- updated_by
- deleted_at
- is_active

## Translation Rules

Translatable content must use separate translation tables.

Examples:

- page + page_translation
- post + post_translation
- portfolio_item + portfolio_item_translation
- category + category_translation
- tag + tag_translation

Each translation table must include:

- parent_id
- language_code
- title/name
- slug when applicable
- content/description when applicable
- seo_title
- seo_description

## Index Rules

Add indexes for:

- slug
- language_code
- status
- published_at
- email
- entity_type + entity_id
- created_at
- updated_at

## Constraint Rules

1. User email must be unique.
2. Slug must be unique per language and content type.
3. Status fields must be controlled.
4. Foreign keys must be explicit.
5. Required fields must be NOT NULL.
