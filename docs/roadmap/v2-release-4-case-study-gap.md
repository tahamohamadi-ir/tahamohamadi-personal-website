# V2 Release 4: Case-study gap inventory

**Status:** Ready for the next schema slice

## Confirmed current contract

`PortfolioProject` already supports a stable project key, draft/published/
archived lifecycle, bilingual title/slug/summary/body/SEO, cover media, date
range, public and repository URLs, ordering, and explicit skill relations.
The public detail route already reads the localized Markdown body.

## Confirmed gaps

The current contract has no representation for the following Release 4 facts:

- a localized role, client label, team description, or outcome;
- a typed fact list or media gallery distinct from the cover asset;
- relations from a project to publications;
- a case-study revision snapshot that includes the complete project aggregate.

## Next implementation slice

Add only the first group as additive, localized fields: role, client label,
team description, and outcome. It requires one Flyway migration, matching
Admin DTO validation, public projection, independent `fa`/`en` rendering, and
an integration test proving that incomplete translations are not substituted
from the other locale. Gallery and facts remain a separate aggregate so the
existing cover-media contract is not overloaded.
