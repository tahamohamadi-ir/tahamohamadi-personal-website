# Research and Evidence Rules

## Purpose

Use this file when a task requires technical research, product research, SEO research, security research, or architectural decision-making.

## Research Principle

Do not make major technical or product decisions based only on assumption.

For important decisions, prefer:

1. Official documentation
2. Standards and specifications
3. Primary sources
4. Original technical references
5. Reputable engineering articles
6. Community posts only as supporting evidence

## When Research Is Required

Research is required for:

- choosing or changing major technology
- changing architecture
- adding infrastructure
- changing authentication strategy
- changing SEO rendering strategy
- changing database strategy
- adding external services
- implementing security-sensitive features
- implementing file upload
- implementing Telegram Bot
- implementing deployment strategy

## Research Protocol

Before research, define:

- research question
- decision to be made
- scope
- constraints
- candidate options
- success criteria

## Source Quality

Prefer sources in this order:

1. Official docs
2. Vendor documentation
3. Standards bodies
4. Academic or technical papers
5. Well-known engineering blogs
6. GitHub issues/discussions only for implementation caveats
7. Random blog posts only if corroborated

## Decision Output Format

For each researched decision, output:

- Decision ID
- Question
- Options
- Evidence summary
- Pros
- Cons
- Risks
- Recommendation
- Confidence level
- Sources checked
- Date checked

## Evidence Strength

Use these confidence levels:

- High: official docs or standards support the decision
- Medium: multiple reliable sources support the decision
- Low: limited or indirect evidence
- Uncertain: decision should be postponed or tested with a spike

## Bias Control

1. Do not cherry-pick sources.
2. Mention conflicting evidence.
3. Separate facts from recommendations.
4. State uncertainty clearly.
5. Do not overstate benchmarks.
6. Do not use outdated information for current tool behavior.

## Reproducibility

For research-heavy tasks, save:

- search queries
- sources checked
- access date
- decision notes
- final recommendation

Recommended location:

`docs/research/`
