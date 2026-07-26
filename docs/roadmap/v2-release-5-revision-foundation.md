# V2 Release 5: Blog revision foundation

**Status:** In progress

## Delivered slice

`V15__create_blog_post_revisions.sql` introduces immutable, numbered snapshots
for Blog posts. The Admin Blog API now records a snapshot at creation and
before each update, publish, or archive transition.

The initial Admin endpoints are:

- `GET /api/v1/admin/blog/posts/{id}/revisions`
- `GET /api/v1/admin/blog/posts/{id}/revisions/{revisionId}`
- `POST /api/v1/admin/blog/posts/{id}/revisions/{revisionId}/restore-as-draft?version={version}`

Restore is deliberately copy-on-restore: it creates a new `DRAFT` post using
the snapshot, derives unique localized slugs, retains the snapshot's managed
media and document data, and leaves the source post (including a published
post) unchanged. The operation is version-checked and audited.

## Remaining Release 5 work

- Extend the same immutable model to Pages and Portfolio case studies.
- Add lifecycle states, scheduled publishing, and translation freshness.
- Add revision timeline, compare, and restore controls to the Admin UI.
