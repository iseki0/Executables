# AGENTS

## Project Purpose

This repository is a Kotlin Multiplatform executable parsing project.

- Primary deliverable: `executables-files`, a library for parsing PE, ELF, and Mach-O binaries.
- Secondary deliverable: `bin-tool`, a native CLI that prints executable metadata and Go SBOM/build info when present.
- Supporting build logic: `g` and `buildSrc`, used for source generation and shared Gradle conventions.

The library currently focuses on:

- detecting executable formats
- reading headers and sections
- reading import/export symbols where supported
- parsing PE resources and version info
- reading Go build info / SBOM-related metadata

## Repository Structure

### Root

- `settings.gradle.kts`: includes `executables-files`, `bin-tool`, and composite build `g`
- `build.gradle.kts`: root Dokka aggregation and common group/version/repository setup
- `gradle/libs.versions.toml`: central dependency and plugin version catalog
- `gradlew`, `gradlew.bat`: the only allowed Gradle entrypoints

### Main Modules

- `files`: main Kotlin Multiplatform library, published as `space.iseki.executables:executables-files`
- `bin-tool`: multiplatform native executable for inspecting binaries from the command line
- `g`: custom Gradle plugin build that generates Kotlin enum/flag sources from YAML definitions
- `buildSrc`: shared Gradle conventions applied across modules

## Source Layout

### Library Code

`files/src/commonMain/kotlin/space/iseki/executables` contains shared parsing logic:

- `common`: shared abstractions such as file accessors, sections, addresses, and format handling
- `pe`: PE/COFF parsing, resources, version info, import/export support
- `elf`: ELF headers, sections, symbol parsing
- `macho`: Mach-O headers, segments, load commands
- `sbom`: Go build info and SBOM-oriented parsing
- `share`: shared low-level byte and utility helpers

### Platform-Specific Code

- `files/src/jvmMain`: JVM file access implementations and JPMS packaging support
- `files/src/nonJvmMain`: common non-JVM helpers
- `files/src/nativeFileSupportedMain`, `nativeFileSupported2Main`, `nativeFileUnsupportedMain`, `mingwX64Main`: target-specific file access behavior

### Tests and Fixtures

- `files/src/commonTest`: format parser unit tests
- `files/src/fileAccessTest`: tests that exercise real sample binaries from `resources`
- `files/src/jvmTest`, `files/src/mingwX64Test`: platform-specific tests
- `files/src/**/resources`: sample PE / ELF / Mach-O / Go binaries used as fixtures

### Generated Code

- `files/src/commonMain/define`: YAML definitions for enums/flags
- `g/src/main/resources/*.ftl`: Freemarker templates for generated Kotlin code
- generated Kotlin is written into the build directory by task `tGenerateFlagFiles`

## Build and Tooling Notes

- The project uses Kotlin Multiplatform with JVM, JS, Wasm, Apple, Linux, Android Native, and MinGW targets.
- Shared Gradle conventions live in `buildSrc/src/main/kotlin/convention.gradle.kts`.
- `files` enables ABI validation and Maven publication signing.
- GPG command-line signing is configured in Gradle via `useGpgCmd()`.

Common commands:

- run tests: `./gradlew test`
- run JVM tests only: `./gradlew jvmTest`
- inspect tasks: `./gradlew tasks --all`
- build CLI targets through `bin-tool` tasks from `./gradlew`

On Windows PowerShell, use:

- `.\gradlew.bat test`
- `.\gradlew.bat jvmTest`

## Mandatory Working Rules

These rules are important for anyone operating in this repository.

### Git and Commits

- Never create a non-GPG-signed Git commit.
- When a commit is required, use GPG signing explicitly, for example: `git commit -S ...`
- If you are operating from an agent or sandboxed environment, directly request elevated execution for commit commands instead of trying to commit inside the sandbox first.

### Gradle

- Never use plain `gradle`.
- Always use the wrapper: `./gradlew` on Unix-like shells or `.\gradlew.bat` on Windows PowerShell.
- Do not use sandboxed Gradle for this repository.
- If Gradle needs to be run from an agent/sandboxed environment, directly request elevated execution and run it through `gradlew` / `gradlew.bat`.
- On Windows PowerShell, prefer `.\gradlew.bat --% ... -Pkey=value` when passing Gradle `-P` properties. PowerShell may misparse raw `-P...` arguments without `--%`.

### GitHub CLI

- `gh` is allowed and useful in this repository.
- In sandboxed environments, `gh` may fail to read authentication credentials unless it is run with elevated permissions.
- If `gh` is needed, prefer running it with elevation directly instead of assuming sandbox credentials will work.

## Practical Guidance For Future Agents

- Start by reading `README.md`, `settings.gradle.kts`, and the relevant module `build.gradle.kts`.
- Treat `files` as the main product and `bin-tool` as a thin consumer of the library.
- If editing parser behavior, check both `commonTest` and `fileAccessTest` coverage.
- If editing enum/flag definitions under `files/src/commonMain/define`, expect generated Kotlin outputs to change through `tGenerateFlagFiles`.
- Preserve multiplatform structure; platform-specific file access code is intentionally split by target capability.
- `files` uses Kotlin ABI validation with committed baselines in `files/api/executables-files.api` and `files/api/executables-files.klib.api`. If public API changes intentionally, update the baselines and commit them with the code change.
- ABI validation is enforced through Linux CI. When diagnosing ABI failures, prefer simulating CI with `GITHUB_ACTIONS=true`, `RUNNER_OS=Linux`, and `-Pkotlin.native.enableKlibsCrossCompilation=true`.
- A green `build` matrix does not always mean the full workflow is done; also wait for follow-up jobs such as `tool-build` when monitoring `Build` workflow completion.
