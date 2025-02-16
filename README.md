# Executables

![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/iseki0/Executables/build.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/space.iseki.executables/executables-all)](https://central.sonatype.com/artifact/space.iseki.executables/executables-all)
![License](https://img.shields.io/github/license/iseki0/Executables)
[![codecov](https://codecov.io/gh/iseki0/Executables/graph/badge.svg?token=WYG654BF18)](https://codecov.io/gh/iseki0/Executables)

An executable file parsing library, written in Kotlin Multiplatform.

Currently, this library can:

- [x] Read PE basic information (COFF header, optional header, sections)
- [x] Read PE version information (aka. [VS_VERSIONINFO] structure)
- [ ] Read PE import/export symbol tables
- [ ] Read ELF basic information
- [ ] Read Macho basic information

> This project is currently in the early stages of development. This means it is still evolving and may undergo frequent
> updates and changes.

## Kotlin targets

- Kotlin/JVM
- Kotlin/JavaScript
- Kotlin/WasmJS (browser)

> If you need more targets, feel free to open an issue or pull request directly.

## Getting Started

### Add dependency

This project contains four modules on Maven Central:

- [space.iseki.executables:executables-common] contains the code shared by pe, elf, and macho
- [space.iseki.executables:executables-pe] contains the code for parsing PE files
- [space.iseki.executables:executables-all] (common + pe)

If the dependency size is not a concern, you can use executables-all directly.

#### Gradle

```kotlin
dependencies {
    implementation("space.iseki.executables:executables-all:0.0.1")
}
```

#### Maven

```xml

<dependency>
    <groupId>space.iseki.executables</groupId>
    <artifactId>executables-all-jvm</artifactId>
    <version>0.0.1</version>
</dependency>
```

[VS_VERSIONINFO]: https://learn.microsoft.com/en-us/windows/win32/menurc/vs-versioninfo

[space.iseki.executables:executables-common]: https://central.sonatype.com/artifact/space.iseki.executables/executables-common

[space.iseki.executables:executables-pe]: https://central.sonatype.com/artifact/space.iseki.executables/executables-pe

[space.iseki.executables:executables-all]: https://central.sonatype.com/artifact/space.iseki.executables/executables-all