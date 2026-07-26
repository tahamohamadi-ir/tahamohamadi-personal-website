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

The Blog lifecycle also supports `IN_REVIEW`. Admins can submit a draft for
review, return it to draft, or publish/schedule it after review. Every review
transition is version-checked, audited, and rejected with the standard
`STATE_CONFLICT` response when it is not valid for the current state.

Admin endpoints:

- `POST /api/v1/admin/blog/posts/{id}/submit-for-review?version={version}`
- `POST /api/v1/admin/blog/posts/{id}/return-to-draft?version={version}`

Blog translation freshness now has a first, source-of-truth slice. A post has
an explicit `sourceLanguage` (`fa` or `en`), selected when it is created or
saved. The API returns independently computed statuses for `fa` and `en`:
`MISSING`, `INCOMPLETE`, `COMPLETE`, or `OUTDATED`. When the source text
changes and the target text does not, only that target becomes `OUTDATED`; no
content is copied or overwritten. `sourceLanguage` is accepted as an optional
query parameter on Blog create/update endpoints, and its selection is visible
in the Admin Blog editor.

Pages now have the same server-side, immutable revision boundary. A snapshot
includes page metadata, both localized page records, and the full typed page
composition (sections, blocks, and localized block fields). It is captured at
creation and before metadata, lifecycle, block, or composition changes.

- `GET /api/v1/admin/pages/{id}/revisions`
- `GET /api/v1/admin/pages/{id}/revisions/{revisionId}`
- `POST /api/v1/admin/pages/{id}/revisions/{revisionId}/restore-as-draft?version={version}`

Page restore is copy-on-restore: it creates a separate `DRAFT` page, derives
non-conflicting page keys and locale slugs, recreates sections and blocks with
new identifiers, checks that referenced media is still active, and keeps the
source page untouched. Restore is version-checked and audited.

## Remaining Release 5 work

- Extend the same immutable model to Portfolio case studies.
- Add a cross-entity translation queue with filtering, source update time,
  comparison, and completion checklist.
- Add revision timeline, compare, and restore controls to the Admin UI.
