# Executables

![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/iseki0/Executables/build.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/space.iseki.executables/executables-all)](https://central.sonatype.com/artifact/space.iseki.executables/executables-all)
![License](https://img.shields.io/github/license/iseki0/Executables)
[![codecov](https://codecov.io/gh/iseki0/Executables/graph/badge.svg?token=WYG654BF18)](https://codecov.io/gh/iseki0/Executables)

An executable file parsing library, written in Kotlin Multiplatform.

Currently, this library can:

- [x] PE: Read basic information (COFF header, optional header, sections)
- [x] PE: Read version information (aka. [VS_VERSIONINFO] structure)
- [x] PE: Read import/export symbol tables
- [x] PE: Read resources and section data
- [x] ELF: Read basic information (ELF Header, Section headers, Program headers)
- [x] ELF: Read symbol tables
- [x] ELF: Read section data
- [x] Macho: Read basic information
- [x] Chore: Jigsaw ready

> This project is currently in the early stages of development. This means it is still evolving and may undergo frequent
> updates and changes.
>
> Due to the current status of Kotlin/Multiplatform, it can be expected that for a period of time following the stable
> release, binary compatibility guarantees will only be provided for Kotlin/JVM.

## Kotlin targets

All targets supported.

## Getting Started

### Add dependency

This project contains several modules on Maven Central:

- [space.iseki.executables:executables-common] contains the code shared by pe, elf, and macho
- [space.iseki.executables:executables-pe] contains the code for parsing PE files
- [space.iseki.executables:executables-elf] contains the code for parsing ELF files
- [space.iseki.executables:executables-macho] contains the code for parsing Macho files
- [space.iseki.executables:executables-all] (common + pe + elf)

If the dependency size is not a concern, you can use executables-all directly.

#### Gradle

```kotlin
dependencies {
    implementation("space.iseki.executables:executables-all:0.0.17")
}
```

#### Maven

Since the project is in Kotlin Multiplatform, for Maven user you have to specify the platform explicitly.
(The `-jvm` suffix)

```xml

<dependency>
    <groupId>space.iseki.executables</groupId>
    <artifactId>executables-all-jvm</artifactId>
    <version>0.0.17</version>
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
    PEFile.open(file).use { peFile: PEFile ->
        println(peFile.coffHeader)
        println(peFile.summary)
        println(Json.encodeToString(peFile.summary))
        println(peFile.versionInfo)
    }
}
```

#### Detect the type of file

```kotlin
import java.nio.file.Path
import space.iseki.executables.common.ExecutableFile
import space.iseki.executables.common.ExecutableFileType

fun main() {
    val file = Path.of("C:\\System32\\notepad.exe")
    println(ExecutableFileType.detect(file))
}
```

#### Read PE file import symbols

> This API also works for ELF files.

```kotlin
import java.nio.file.Path
import space.iseki.executables.pe.PEFile

fun main() {
    val file = Path.of("C:\\System32\\notepad.exe")
    PEFile.open(file).use { peFile ->
        // Get all imported DLLs and functions
        peFile.importSymbols.forEach { symbol ->
            println("Imported: ${symbol.file}::${symbol.name}")
            if (symbol.isOrdinal) {
                println("  - Ordinal: ${symbol.ordinal}")
            }
        }
    }
}
```

#### Read PE file export symbols

> This API also works for ELF files.

```kotlin
import java.nio.file.Path
import space.iseki.executables.pe.PEFile

fun main() {
    val file = Path.of("C:\\System32\\kernel32.dll")
    PEFile.open(file).use { peFile ->
        // Get all exported functions
        val exportSymbols = peFile.exportSymbols
        println("Number of exported functions: ${exportSymbols.size}")

        // Print the first 10 exported functions
        exportSymbols.take(10).forEach { symbol ->
            println("  - ${symbol.name}, Ordinal: ${symbol.ordinal}")
            if (symbol.isForwarder) {
                println("    Forwards to: ${symbol.forwarderString}")
            }
        }
    }
}
```

#### Read ELF symbol tables

> Since PE splits the symbol table into import/export, this API is ELF specified.

```kotlin
import java.nio.file.Path
import space.iseki.executables.elf.ELFFile

fun main() {
    val file = Path.of("/bin/ls")  // ELF file on Linux system
    ELFFile.open(file).use { elfFile ->
        // Get all symbols
        elfFile.symbols.forEach { symbol ->
            val name = symbol.name ?: "<unnamed>"
            println("  - $name: Name=${symbol.name}, Binding=${symbol.binding}")
        }
    }
}
```

## References

- PE Format: https://docs.microsoft.com/en-us/windows/win32/debug/pe-format
- PE VS_VERSIONINFO: https://learn.microsoft.com/en-us/windows/win32/menurc/vs-versioninfo
- ELF Format: https://refspecs.linuxfoundation.org/elf/elf.pdf
- Macho Format: https://github.com/apple/darwin-xnu/blob/main/EXTERNAL_HEADERS/mach-o/loader.h

[VS_VERSIONINFO]: https://learn.microsoft.com/en-us/windows/win32/menurc/vs-versioninfo

[space.iseki.executables:executables-common]: https://central.sonatype.com/artifact/space.iseki.executables/executables-common

[space.iseki.executables:executables-pe]: https://central.sonatype.com/artifact/space.iseki.executables/executables-pe

[space.iseki.executables:executables-elf]: https://central.sonatype.com/artifact/space.iseki.executables/executables-elf

[space.iseki.executables:executables-macho]: https://central.sonatype.com/artifact/space.iseki.executables/executables-macho

[space.iseki.executables:executables-all]: https://central.sonatype.com/artifact/space.iseki.executables/executables-all
