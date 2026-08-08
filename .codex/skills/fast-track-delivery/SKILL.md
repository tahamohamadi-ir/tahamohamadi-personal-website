---
name: fast-track-delivery
description: Deliver a narrow, safe vertical slice quickly while making every deliberately deferred test, QA, security hardening, or operational risk visible and recoverable. Use for fast-track implementation, release triage, scope negotiation, or handoff work in TahaMohamadi.ir.
---

# Fast Track Delivery

## Overview

Use this skill when the priority is becoming operational quickly without misrepresenting untested or risky work as complete. It supplements project-specific engineering skills; it does not authorize a broader scope or a weaker security baseline.

Read [the delivery policy](../../../docs/governance/fast-track-delivery.md) and the relevant open items in [the canonical ledger](../../../docs/status/deferred-validation.md) before making a speed/quality tradeoff.

## Workflow

1. State the smallest user-visible vertical slice, real contracts, affected files, and target status: `implemented`, `operational`, or `release-verified`.
2. Classify the change against the non-deferrable gates: authentication/RBAC/CSRF, secrets, input/output safety, file/import safety, public publication state, data integrity, and deployment rollback.
3. Implement and verify the narrowest behavioral slice first. Do not invent APIs, DTOs, or fallback paths to make it appear done.
4. If a relevant check is not performed, add an entry to `docs/status/deferred-validation.md` using [the entry template](references/ledger-entry-template.md). Record what actually ran and what did not.
5. Hand off the exact status, commands/evidence, open ledger IDs, mitigation, and return trigger. Never say `release-verified` merely because the focused test passes.

## Non-deferrable boundary

Do not use fast-track delivery to skip or bypass:

- backend authentication, authorization, session, or CSRF controls;
- secrets handling, validation, sanitization, or safe output rendering;
- upload/import allowlists and dangerous-payload rejection;
- `PUBLISHED`-only public visibility and protected/noindex preview;
- migration/data integrity, required rollback, or the changed contract and primary flow.

An open `P0` or `P1` ledger item blocks operational release of its affected flow. Narrow scope, disable the flow, or fix it first.

## Keep the ledger useful

- Log a concrete risk, not a vague reminder.
- Include a real owner and a measurable return trigger; `later` is not a trigger.
- Do not delete resolved history. Change its status to `RESOLVED` and attach closure evidence.
- Do not transfer assumptions or deferred items from another repository without fresh local evidence.
