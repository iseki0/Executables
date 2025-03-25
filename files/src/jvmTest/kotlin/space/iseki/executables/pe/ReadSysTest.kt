package space.iseki.executables.pe

import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.pathString
import kotlin.streams.asSequence
import kotlin.test.Test

class ReadSysTest {

    @TestFactory
    @Execution(ExecutionMode.CONCURRENT)
    fun test(): List<DynamicTest> {
        Assumptions.assumeTrue(System.getProperty("os.name").contains("windows", ignoreCase = true))
        val windir = System.getenv("windir")
        println(windir)
        val extSet = setOf("exe", "dll", "sys")
        return Files.list(Path.of(windir).resolve("System32"))
            .asSequence()
            .filter { it.extension.lowercase() in extSet }
            .take(1000)
            .map { path ->
                listOf(DynamicTest.dynamicTest(path.fileName.pathString + " - Path") {
                    println(path)
                    PEFile.open(path).use { peFile ->
                        println(peFile)
                        peFile.versionInfo?.stringFileInfo?.strings.orEmpty().forEach { (k, v) -> println("$k: $v") }
                    }
                }, DynamicTest.dynamicTest(path.fileName.pathString + " - File") {
                    println(path)
                    PEFile.open(path.toFile()).use { peFile ->
                        println(peFile)
                        println(peFile.importSymbols)
                        println(peFile.exportSymbols)
                        peFile.versionInfo?.stringFileInfo?.strings.orEmpty().forEach { (k, v) -> println("$k: $v") }
                    }
                })
            }
            .flatten()
            .toList()
    }

    @Test
    fun testCryptdlgDll() {
        // this file contains zero length string item
        PEFile.open(Path.of("src/jvmTest/resources/cryptdlg.dll")).use { file ->
            file.versionInfo?.stringFileInfo?.strings.orEmpty().forEach { (k, v) -> println("$k: $v") }
            for (importSymbol in file.importSymbols) {
                println(importSymbol)
            }
            for (exportSymbol in file.exportSymbols) {
                println(exportSymbol)
            }
        }
    }
}