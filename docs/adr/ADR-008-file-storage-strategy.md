# ADR-008: File Storage Strategy

## Status

Accepted for MVP.

## Context

The CMS needs media uploads for CV files, images, blog attachments, portfolio assets, and publication-related files. The MVP deployment target is a VPS with Docker Compose.

## Options

| Option | Fit | Notes |
|---|---|---|
| Local volume | High | Simple for VPS MVP |
| S3-compatible storage | Medium | Useful later, but adds setup cost |
| MinIO | Medium | Good abstraction test, but extra service for MVP |

## Decision

Use local volume storage for MVP behind a StorageService abstraction.

## Consequences

- Docker Compose must persist media volume.
- Backups must include database and media files.
- Upload validation is mandatory.
- Future migration to S3-compatible storage remains possible.

## Risks

Local storage makes scaling and migration more manual later.

## Follow-Up

Design media metadata and storage paths so S3-compatible storage can be added without changing public APIs.
