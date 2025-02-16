# Executables

![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/iseki0/Executables/build.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/space.iseki.executables/executables-all)](https://central.sonatype.com/artifact/space.iseki.executables/executables-all)
![License](https://img.shields.io/github/license/iseki0/Executables)
[![codecov](https://codecov.io/gh/iseki0/Executables/graph/badge.svg?token=WYG654BF18)](https://codecov.io/gh/iseki0/Executables)

An executable file parsing library, written in Kotlin Multiplatform.

Currently, this library can:

- [x] PE: Read basic information (COFF header, optional header, sections)
- [x] PE: Read version information (aka. [VS_VERSIONINFO] structure)
- [ ] PE: Read import/export symbol tables
- [ ] ELF: Read basic information
- [ ] Macho: Read basic information
- [x] Chore: Jigsaw ready

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
    implementation("space.iseki.executables:executables-all:0.0.2")
}
```

#### Maven

Since the project is in Kotlin Multiplatform, for Maven user you have to specify the platform explicitly.
(The `-jvm` suffix)

```xml

<dependency>
    <groupId>space.iseki.executables</groupId>
    <artifactId>executables-all-jvm</artifactId>
    <version>0.0.2</version>
</dependency>
```

### Samples

#### Print information of a PE file

```kotlin
import java.nio.file.Path
import kotlinx.serialization.json.Json
import space.iseki.executables.pe.PEFile

fun main() {
    val file = Path.of("C:\\System32\\notepad.exe")
    PEFile(file).use { peFile: PEFile ->
        println(peFile.coffHeader)
        println(peFile.summary)
        println(Json.encodeToString(PEFile.Summary.prettySerializer, peFile.summary))
        println(peFile.versionInfo)
    }
}
```

[VS_VERSIONINFO]: https://learn.microsoft.com/en-us/windows/win32/menurc/vs-versioninfo

[space.iseki.executables:executables-common]: https://central.sonatype.com/artifact/space.iseki.executables/executables-common

[space.iseki.executables:executables-pe]: https://central.sonatype.com/artifact/space.iseki.executables/executables-pe

[space.iseki.executables:executables-all]: https://central.sonatype.com/artifact/space.iseki.executables/executables-all