# Fast-track ledger entry template

Use `docs/status/deferred-validation.md` as the canonical file. Keep the heading when closing an item; change its status to `RESOLVED` and add closure evidence.

```md
### AREA-ID — concise title

- **Status:** `OPEN`
- **Severity:** `P2`
- **Recorded:** `YYYY-MM-DD`
- **Scope / reference:** task, requirement, release, file, or flow
- **Completed evidence:** focused test, inspection, or QA actually performed
- **Deferred work:** exact test, security review, QA, or hardening not performed
- **Impact / risk:** user, data, security, or operational consequence
- **Current mitigation:** flag, limited scope, runbook, or `None`
- **Owner:** accountable role or person
- **Return trigger:** a dated milestone, before a named release, or measurable event
- **Closure criteria:** exact evidence required before `RESOLVED`
- **Closure evidence:** `—` until resolved
```

Never use a ledger entry to waive authentication, authorization, CSRF, secrets handling, input/output safety, safe file processing, public publication rules, or data integrity.
