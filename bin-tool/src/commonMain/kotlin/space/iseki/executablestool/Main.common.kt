package space.iseki.executablestool

import platform.posix.exit
import space.iseki.executables.common.ExportSymbolContainer
import space.iseki.executables.common.FileFormat
import space.iseki.executables.common.ImportSymbolContainer
import space.iseki.executables.common.ReadableSectionContainer
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.common.detect
import space.iseki.executables.sbom.GoSBom

private val json = kotlinx.serialization.json.Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

internal fun handleCommand(args: List<String>) {
    if (args.size != 1) {
        println("Usage: <path to executable>")
        exit(1)
        return
    }
    val path = args[0]
    val format = FileFormat.detect(path)
    if (format == null) {
        println("Unknown file format")
        exit(1)
        return
    }
    format.open(path).use { file ->
        println("File path: $path")
        println("File format: $format")
        println("File headers:")
        file.rootHeaders.toList().forEach { (k, v) ->
            println("- $k:")
            printStructure(v, 2)
        }
        if (file is ReadableSectionContainer) {
            println("File sections:")
            for (section in file.sections) {
                println("- ${section.name}:")
                val header = section.header
                if (header != null) printStructure(header, 1)
            }
        }
        if (file is ExportSymbolContainer) {
            println("File export symbols:")
            for (exportSymbol in file.exportSymbols) {
                printStructure(exportSymbol, 1)
            }
        }
        if (file is ImportSymbolContainer) {
            println("File import symbols:")
            file.importSymbols.groupBy { it.file }.forEach { (file, symbols) ->
                println("- $file:")
                symbols.forEach {
                    println("  * ${it.name}")
                }
            }
        }
        val goSBom = GoSBom.readFromOrNull(file)
        if (goSBom != null) {
            println("File go sbom:")
            println(goSBom)
        }
    }

}

fun main(vararg args: String) {
    handleCommand(args.toList())
}

internal fun printStructure(r: ReadableStructure, index: Int) {
    r.fields.toList().forEach { (k, v) ->
        when (v) {
            is ReadableStructure -> {
                println("  ".repeat(index) + "$k:")
                printStructure(v, index + 1)
            }

            else -> {
                println("  ".repeat(index) + "$k: $v")
            }
        }
    }
}
