# Algotic YAGNI

Apply these rules to every coding task in this project unless the user explicitly overrides them.

## Core Principle

Follow YAGNI: implement only what is required for the current, explicitly stated task. Do not build features, layers, abstractions, configuration, or infrastructure solely because they might be useful in the future.

The goal is not to minimize lines of code at any cost. The goal is to satisfy the current requirement completely, correctly, securely, readably, and maintainably while introducing the least unnecessary complexity.

## Task Mode

1. Match the user's requested mode. A request to explain, diagnose, review, or plan does not authorize file changes.
2. For a normal implementation request, inspect, plan briefly, implement, verify, and report in the same task.
3. Do not stop after presenting a plan when implementation was requested.
4. If the user says `Planning only` or `Do not modify files`, inspect and propose a plan without editing.

## Before Making Changes

1. Inspect the relevant code, project structure, architecture, tests, configuration, and installed dependencies before writing code.
2. Determine the current behavior and identify the smallest safe change that satisfies the task.
3. Check whether the requirement can be met by, in order:
   - Reusing existing project code.
   - Making a limited extension to an existing component.
   - Using the language standard library.
   - Using built-in framework, platform, browser, or operating-system capabilities.
   - Using a dependency already installed in the project.
4. Before deleting or materially changing existing code, find and inspect its usages.
5. Identify relevant constraints, likely side effects, and the checks needed to verify the change.
6. If the task is ambiguous, inspect the repository for evidence and make the most conservative reasonable assumption. Ask the user only when missing information would materially change the implementation or make a safe solution impossible.

## Implementation Rules

1. Make the smallest effective change that fully satisfies the explicit requirement.
2. Keep changes scoped to the task. Do not perform unrelated refactoring, cleanup, renaming, file movement, formatting, restructuring, or rewriting.
3. Create a new file, class, service, abstraction, interface, helper, configuration entry, dependency, or architectural layer only when it is genuinely necessary for this task.
4. Do not design or implement for hypothetical future requirements.
5. Do not introduce generalized architecture for a single current use case. Avoid new plugin systems, event buses, factories, registries, adapters, wrappers, or abstraction layers unless the existing architecture or explicit task requires them.
6. Do not remove existing code merely to make the implementation shorter or appear simpler.
7. Do not remove, disable, or change existing functionality unless the task explicitly requires it.
8. Do not remove something merely because it is unused by the code touched in this task; it may be used elsewhere.
9. Preserve the project's established architecture, conventions, patterns, and public contracts. Do not bypass an existing project abstraction solely because a shorter implementation is possible.
10. Prefer readable, direct code over clever or compressed code.
11. Do not use unsafe hardcoding, fragile shortcuts, silent failure, or low-quality temporary solutions to reduce code volume.
12. Avoid speculative configuration flags, extension points, optional modes, and fallback paths that are not required by the task.
13. Do not add comments, documentation, types, tests, or error branches for imaginary behavior. Add them where they are necessary to explain, protect, or verify current behavior.
14. Preserve unrelated user changes already present in the working tree. Do not overwrite or revert them.

## Safety Boundaries

Never use YAGNI as a reason to remove, weaken, omit, or bypass any relevant:

- Correctness or data integrity.
- Security controls.
- Authentication or authorization.
- Input validation or output encoding.
- Error handling or recovery behavior.
- Required edge-case handling.
- Privacy protections.
- Essential logging or observability.
- Type safety.
- Accessibility.
- Reliability or concurrency safety.
- Backward compatibility.
- Necessary tests.
- Code readability or maintainability.
- Explicit user requirements or acceptance criteria.

When reducing complexity conflicts with correctness, security, compatibility, or explicit requirements, prioritize correctness, security, compatibility, and the requirements.

## Dependencies

1. Before adding a dependency, verify that no suitable and maintainable solution exists using current project code, the standard library, built-in platform or framework features, or an existing dependency.
2. Add a dependency only when necessary for the current task and when its benefit outweighs its maintenance, security, and operational cost.
3. Do not replace or upgrade unrelated dependencies.
4. Do not modify lock files unless a required dependency change legitimately updates them.

## Public APIs, Data, and Infrastructure

1. Do not change public APIs, externally visible behavior, database schemas, environment configuration, global configuration, build systems, deployment pipelines, or infrastructure unless the task directly requires it.
2. Avoid breaking changes unless explicitly required. Preserve compatibility with existing consumers where possible.
3. If a database schema change is necessary, use the project's existing migration system, preserve existing data, and make the migration safe and reversible when supported.
4. Do not introduce new services, queues, caches, storage systems, environment variables, or operational components unless the current requirement cannot be correctly met without them.
5. Never perform destructive data or infrastructure operations unless the user explicitly requests and authorizes them.

## Testing and Verification

1. Run the smallest relevant existing test set first, followed by broader checks when the risk or project conventions justify them.
2. When adding behavior or fixing a bug, add or update focused tests that directly cover the change when the project has a relevant test structure.
3. Do not create unrelated test infrastructure or broad test rewrites unless necessary.
4. Verify affected edge cases, failure paths, security boundaries, and compatibility behavior where relevant.
5. Do not claim that a test or check passed unless it was actually run successfully.
6. If a check cannot be run, state what was not verified and why.

## Scope Control

1. Do not expand scope while implementing.
2. If new evidence shows that the task cannot be completed safely within the stated scope, explain the constraint before making a materially broader change.
3. If several valid solutions exist, choose the one that introduces the least unnecessary complexity, fits the existing architecture, preserves unrelated behavior, and needs the fewest new dependencies or operational components.
4. Keep process and explanations proportional to the task. For trivial changes, act directly and report concisely.

## Completion Report

After implementation, report only what is useful for review:

- What changed and which files were affected.
- Which relevant tests or checks were run and their results.
- Any important assumption, limitation, or unresolved risk.
- Any new dependency, migration, configuration, or public behavior change, with its necessity.

Do not add a speculative list of future enhancements. Do not present intentionally omitted hypothetical features as unfinished work.
