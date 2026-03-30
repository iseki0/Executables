---
name: executables-abi-maintenance
description: Maintain ABI validation for the Executables repository, including Kotlin binary-compatibility baselines, Linux CI enforcement, and diagnosis of `checkLegacyAbi` failures. Use when public API may have changed, ABI baseline files under `files/api/` need updating, or GitHub Actions reports ABI-check failures.
---

# Executables ABI Maintenance

Use this skill when changing public API in `executables-files`, wiring ABI checks into CI, or debugging red ABI jobs.

Read [AGENTS.md](C:/Users/iseki/code/executables/AGENTS.md) and [files/build.gradle.kts](C:/Users/iseki/code/executables/files/build.gradle.kts) first.

## Baselines

- JVM baseline: [executables-files.api](C:/Users/iseki/code/executables/files/api/executables-files.api)
- KLIB baseline: [executables-files.klib.api](C:/Users/iseki/code/executables/files/api/executables-files.klib.api)
- CI wiring lives in [build.gradle.kts](C:/Users/iseki/code/executables/files/build.gradle.kts)

ABI work is not complete unless both sides are correct:

- `check` or CI actually runs ABI validation
- committed baseline files match the intended public API

## Workflow

1. Confirm whether the change is intended to affect public API.
2. Verify that `check` reaches `checkLegacyAbi` in the environment that matters.
3. If the API change is intentional, regenerate the ABI baselines.
4. Re-run the relevant check path.
5. Commit code and baseline updates together.

## Commands

On Windows PowerShell, use `--%` before Gradle task names when passing `-P...` properties.

### Fast local checks

```powershell
.\gradlew.bat :executables-files:jvmTest --stacktrace
.\gradlew.bat :executables-files:check --dry-run
```

### Simulate Linux CI ABI path

```powershell
$env:GITHUB_ACTIONS='true'
$env:RUNNER_OS='Linux'
.\gradlew.bat --% :executables-files:check --stacktrace -Pkotlin.native.enableKlibsCrossCompilation=true
```

Use this when:

- Linux CI is red but local Windows/macOS check is green
- you need to confirm `checkLegacyAbi` is really being reached
- you need the same KLIB cross-compilation path used in CI

### Refresh baselines after an intentional API change

```powershell
$env:GITHUB_ACTIONS='true'
$env:RUNNER_OS='Linux'
.\gradlew.bat --% :executables-files:updateLegacyAbi --stacktrace -Pkotlin.native.enableKlibsCrossCompilation=true
git diff -- files/api/executables-files.api files/api/executables-files.klib.api
```

## How To Read Failures

### `checkLegacyAbi` fails and `git diff files/api/...` shows expected API additions

The baseline is stale. Regenerate and commit the baseline files with the code change.

### `check --dry-run` never mentions `checkLegacyAbi`

The build is not enforcing ABI validation on that path. Fix the task wiring in [build.gradle.kts](C:/Users/iseki/code/executables/files/build.gradle.kts) before trusting green CI.

### Linux CI fails but local default check passes

You are probably missing CI-only environment and cross-compilation settings. Re-run with:

```powershell
$env:GITHUB_ACTIONS='true'
$env:RUNNER_OS='Linux'
.\gradlew.bat --% :executables-files:check --stacktrace -Pkotlin.native.enableKlibsCrossCompilation=true
```

### Baseline diff shows unrelated churn you did not intend

Stop and inspect whether:

- a real public API changed unexpectedly
- compiler/plugin upgrades changed rendered signatures
- task wiring started validating a path that had never been enforced before

Do not blindly accept ABI diff noise. Explain why each baseline change exists before committing it.

## Commit Discipline

- Commit source changes and ABI baseline updates in the same signed commit when they belong together.
- If the only repo change is refreshed ABI baselines after enabling enforcement, use a build-focused commit message.
- After pushing, monitor the full `Build` workflow until follow-up jobs such as `tool-build` finish.
