# ADR-012: Media Variants and Focal Point

## Status

Deferred after the Release 1 spike; revisit before enabling image variants or a CDN.

## Context

The current MVP allowlist accepts JPEG, PNG, WebP and PDF. Each asset has one original binary behind the storage abstraction and public media is addressed by its stable asset identifier. No measured production image corpus, CDN budget or responsive-art-direction requirement is available yet.

Adding focal points, generated variants, AVIF and CDN routing now would require a new media-variant data model, migration and storage lifecycle. It would also need cache invalidation for replacement/archive, deterministic rendering contracts, backup changes and an operational rollback path. Those costs are not justified by a verified user need in the current phase.

## Decision

Keep the original-asset model for Release 1.

- No focal-point fields, image-crop UI, derivative-generation jobs, AVIF conversion or CDN integration are introduced now.
- Public renderers continue to use the CMS-provided media URL and their existing explicit dimensions/object-fit rules.
- Media replacement keeps the asset identifier boundary and archive behavior already provided by the media service.
- Example images for development must enter through the Media Library/fixture flow; public components must not gain static example image URLs or fixed editorial copy.

## Rollback and revisit trigger

This is a deferral, so rollback means retaining the current original asset and removing any later variant feature behind its dedicated rollout gate. Reopen the decision only with measured source-image dimensions, traffic/cache data, storage budget, target responsive breakpoints and a migration/cache-invalidation plan. The first variant implementation must include an explicit rollback owner and an on/off behavior test.

## Consequences

- Initial delivery remains small and storage operations stay predictable.
- Hero and card crops may be less tailored than a focal-point system would permit.
- A future implementation must not mutate or overwrite original uploads; variants must be derived, traceable and removable independently.
