# Executables

![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/iseki0/Executables/build.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/space.iseki.executables/executables)](https://central.sonatype.com/artifact/space.iseki.executables/executables)
![License](https://img.shields.io/github/license/iseki0/Executables)
[![codecov](https://codecov.io/gh/iseki0/Executables/graph/badge.svg?token=WYG654BF18)](https://codecov.io/gh/iseki0/Executables)

> This project is currently in the early stages of development. This means it is still evolving and may undergo frequent
> updates and changes.

Kotlin multiplatform executable file parsing library. Currently only provides basic support for PE format.

## Kotlin targets

- Kotlin/JVM
- Kotlin/JavaScript
- Kotlin/WasmJS (browser)

## Getting Started

For JVM:
```kotlin
import java.nio.file.Path

fun main() {
    val file = Path.of("path/to/file.exe")
    PEFile(file).use { pe: PEFile ->
        println(pe.summary) // print summary information(COFF, OptionalHeader, Sections)
    }
}
```

For JavaScript:
```typescript
import f from 'executables.js'

export const doDump = (peFileData: Uint8Array) => 
    console.log(f.space.iseki.executables.pe.dumpHeaderJson(peFileData, true /* pretty JSON */))

```


