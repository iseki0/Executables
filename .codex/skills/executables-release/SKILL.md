---
name: executables-release
description: Release the Executables repository, including preflight checks, signed commit/tag workflow, Maven Central publication via tag push, GitHub Release publication, bin-tool asset publishing, and post-release verification.
---

# Executables Release

Use this skill when releasing `iseki0/Executables`.

## Preconditions

- Work from [master](C:/Users/iseki/code/executables).
- Read [AGENTS.md](C:/Users/iseki/code/executables/AGENTS.md), [publish.yml](C:/Users/iseki/code/executables/.github/workflows/publish.yml), [release-bin-tool.yml](C:/Users/iseki/code/executables/.github/workflows/release-bin-tool.yml), and [update-version.yml](C:/Users/iseki/code/executables/.github/workflows/update-version.yml).
- Never use plain `gradle`. Use `./gradlew` or `.\gradlew.bat`.
- Never create an unsigned commit. Use `git commit -S`.

## What The Release Does

- Pushing tag `vX.Y.Z` triggers [publish.yml](C:/Users/iseki/code/executables/.github/workflows/publish.yml).
- That workflow publishes `executables-files` to Maven Central and deploys Dokka.
- Publishing the GitHub Release triggers [release-bin-tool.yml](C:/Users/iseki/code/executables/.github/workflows/release-bin-tool.yml).
- That workflow builds `bin-tool` on `ubuntu-latest`, `windows-latest`, and `macos-latest`, packages the binaries, and attaches them to the GitHub Release.
- Publishing the GitHub Release also triggers [update-version.yml](C:/Users/iseki/code/executables/.github/workflows/update-version.yml), which updates `README.md` examples on `master`.

## Preflight

Run the smallest checks that cover release risk:

```powershell
.\gradlew.bat jvmTest --stacktrace
.\gradlew.bat :bin-tool:build --stacktrace
git status --short
git log --oneline origin/master..HEAD
```

If dependency behavior changed, update tests before release. Do not ship with known red tests.

## Release Steps

1. Finalize code and docs on `master`.
2. Commit changes with GPG signing.

```powershell
git add .
git commit -S -m "chore: release prep vX.Y.Z"
```

3. Push `master`.

```powershell
git push origin master
```

4. Create and push the signed tag.

```powershell
git tag -s vX.Y.Z -m "vX.Y.Z"
git push origin vX.Y.Z
```

5. Wait for `Publish` workflow for that tag to succeed.
6. Draft concise release notes.
7. Publish the GitHub Release for `vX.Y.Z`.

Example:

```powershell
gh release create vX.Y.Z --repo iseki0/Executables --title "vX.Y.Z" --notes-file RELEASE_NOTES.md
```

8. Wait for `Release bin-tool` and `Update Version in README` workflows to finish.
9. Verify:

- Maven Central shows `space.iseki.executables:executables-files:X.Y.Z`
- GitHub Release contains packaged `bin-tool` assets
- README version examples were bumped by automation

## Verification Commands

```powershell
gh run list --repo iseki0/Executables --limit 10
gh release view vX.Y.Z --repo iseki0/Executables --json assets,url
git fetch origin
git log --oneline origin/master -n 3
```

## Failure Modes

- Tag pushed before `master` push: release content does not match branch head.
- Release published before writing notes carefully: poor release note quality becomes public history.
- `Publish` succeeds but GitHub Release is not published: `bin-tool` assets and README update will not run.
- GitHub Release exists but has no assets: inspect `Release bin-tool` workflow rather than `Publish`.

## Notes

- Version comes from the tag. There is no permanent version bump file in the repo.
- `README.md` examples are updated after publishing the GitHub Release, not before.
- Keep the release note factual and brief. For note-writing guidance, use `executables-release-notes`.
