---
name: executables-release-notes
description: Write concise GitHub release notes for the Executables repository, focusing on user-visible changes, compatibility-relevant fixes, release assets, and short validation notes without changelog bloat.
---

# Executables Release Notes

Use this skill when writing GitHub Release notes for `iseki0/Executables`.

## Style

- Keep it short enough to scan in under a minute.
- Lead with user-visible changes.
- Mention compatibility-relevant dependency or behavior changes.
- Mention whether new `bin-tool` assets are included.
- Do not dump commit history.
- Do not include internal refactors unless they change release risk or user experience.

## Recommended Structure

Use 3 short sections in this order:

1. Summary
2. Highlights
3. Verification

## Template

```markdown
## Summary

This release updates `executables-files` to X.Y.Z and includes refreshed `bin-tool` binaries on the GitHub Release page.

## Highlights

- Added GitHub Release assets for `bin-tool` binaries built on Linux, Windows, and macOS CI.
- Updated `purlkt` from A.B.C to D.E.F.
- Adjusted Go SBOM purl expectations to match the newer canonical encoding behavior.

## Verification

- `jvmTest`
- `:bin-tool:build`
```

## Selection Rules

- If a change affects parsing results, call it out explicitly.
- If a dependency upgrade only changes canonical formatting, say that directly instead of framing it as a major feature.
- If the release contains no API-breaking change, avoid inventing a compatibility warning.

## Good Release Notes For This Repo

- Mention `executables-files` when the library changed.
- Mention `bin-tool` assets when release downloads changed.
- Mention specific formats or SBOM behavior only if users would notice.

## Avoid

- Long commit lists
- Generic phrases like "various improvements"
- Deep CI details unless they affect how users obtain artifacts
- Repeating the same point in multiple sections
