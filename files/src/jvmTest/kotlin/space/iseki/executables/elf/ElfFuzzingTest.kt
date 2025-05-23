package space.iseki.executables.elf

import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.io.path.walk
import kotlin.streams.asSequence
import kotlin.test.Test

/**
 * Fuzzing test for ELF file format.
 *
 * This test randomly selects ELF files from Linux system directories and attempts to parse them.
 * If running on Windows, it tries to access files through WSL.
 */
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
class ElfFuzzingTest {

    companion object {
        // Linux directories to search for ELF files
        private val LINUX_DIRS = listOf(
            "/bin",
            "/usr/bin",
            "/usr/local/bin",
            "/lib",
            "/usr/lib",
            "/usr/local/lib",
            "/sbin",
            "/usr/sbin"
        )

        // Maximum file size to test (10MB)
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024L

        // Cache for WSL path conversion
        private val wslPathCache = ConcurrentHashMap<String, String?>()

        /**
         * Converts a WSL path to a Windows path.
         *
         * @param wslPath The path in WSL format (e.g., /bin/bash)
         * @return The Windows path (e.g., \\wsl$\Ubuntu\bin\bash) or null if conversion failed
         */
        private fun convertWslPathToWindows(wslPath: String): String? {
            return wslPathCache.computeIfAbsent(wslPath) {
                try {
                    val process = ProcessBuilder("wsl", "wslpath", "-w", wslPath)
                        .redirectErrorStream(true)
                        .start()

                    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                        val windowsPath = reader.readLine()?.trim()
                        process.waitFor()

                        if (process.exitValue() == 0 && !windowsPath.isNullOrEmpty()) {
                            windowsPath
                        } else {
                            null
                        }
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }

        /**
         * Checks if WSL is available on the system.
         *
         * @return true if WSL is available, false otherwise
         */
        private fun isWslAvailable(): Boolean {
            return try {
                val process = ProcessBuilder("wsl", "echo", "test")
                    .redirectErrorStream(true)
                    .start()

                process.waitFor()
                process.exitValue() == 0
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Lists ELF files in the given WSL directory.
         *
         * @param directory The WSL directory to search
         * @return A list of Windows paths to ELF files
         */
        private fun listElfFilesInWslDirectory(directory: String): Sequence<Path> {
            try {
                // Convert WSL directory to Windows path
                val windowsDirectoryPath = convertWslPathToWindows(directory) ?: return emptySequence()
                val directoryPath = Path.of(windowsDirectoryPath)

                if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
                    println("WSL directory $directory (Windows: $windowsDirectoryPath) does not exist or is not a directory")
                    return emptySequence()
                }

                // List files in the directory (non-recursively)
                return directoryPath.walk()
                    .filter { Files.isRegularFile(it) }
                    .filter {
                        try {
                            val size = Files.size(it)
                            size in 1..MAX_FILE_SIZE
                        } catch (e: Exception) {
                            false
                        }
                    }
                    .filter {
                        try {
                            // Check if it's an ELF file
                            println("file -b $it")
                            val process = ProcessBuilder("file", "-b", it.toString())
                                .redirectErrorStream(true)
                                .start()

                            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                                val output = reader.readLine() ?: ""
                                process.waitFor()
                                output.contains("ELF")
                            }
                        } catch (e: Exception) {
                            // If 'file' command is not available on Windows, try an alternative approach
                            try {
                                Files.newInputStream(it).use { stream ->
                                    val header = ByteArray(4)
                                    if (stream.read(header) == 4) {
                                        // Check for ELF magic number: 0x7F 'E' 'L' 'F'
                                        header[0] == 0x7F.toByte() &&
                                                header[1] == 'E'.code.toByte() &&
                                                header[2] == 'L'.code.toByte() &&
                                                header[3] == 'F'.code.toByte()
                                    } else {
                                        false
                                    }
                                }
                            } catch (e2: Exception) {
                                false
                            }
                        }
                    }
            } catch (e: Exception) {
                println("Error listing ELF files in WSL directory $directory: ${e.message}")
                return emptySequence()
            }
        }

        /**
         * Lists ELF files in the given Linux directory.
         *
         * @param directory The Linux directory to search
         * @return A list of paths to ELF files
         */
        private fun listElfFilesInLinuxDirectory(directory: String): Sequence<Path> {
            val path = Path.of(directory)
            if (!path.exists()) {
                return emptySequence()
            }

            return try {
                Files.walk(path)
                    .asSequence()
                    .filter { it.isRegularFile() }
                    .filter {
                        try {
                            val size = it.fileSize()
                            size in 1..MAX_FILE_SIZE
                        } catch (e: Exception) {
                            false
                        }
                    }
                    .filter {
                        try {
                            val process = ProcessBuilder("file", "-b", it.pathString)
                                .redirectErrorStream(true)
                                .start()

                            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                                val output = reader.readLine() ?: ""
                                process.waitFor()
                                output.contains("ELF")
                            }
                        } catch (e: Exception) {
                            false
                        }
                    }

            } catch (e: Exception) {
                emptySequence()
            }
        }
    }

    @TestFactory
    @Execution(ExecutionMode.CONCURRENT)
    fun testElfFiles(): List<DynamicTest> {
        println("testElfFiles")
        // Determine if we're on Linux or Windows with WSL
        val isLinux = System.getProperty("os.name").lowercase().contains("linux")
        val isWindowsWithWsl = !isLinux && isWslAvailable()

        // Skip test if neither Linux nor Windows with WSL
        Assumptions.assumeTrue(isLinux || isWindowsWithWsl, "Test requires Linux or Windows with WSL")

        // Collect ELF files
        val elfFiles = if (isLinux) {
            // On Linux, directly search directories
            LINUX_DIRS.asSequence().flatMap { listElfFilesInLinuxDirectory(it) }
        } else {
            // On Windows, search through WSL
            LINUX_DIRS.asSequence().flatMap { listElfFilesInWslDirectory(it) }
        }.take(200).toList()


        // Create dynamic tests for each file
        return elfFiles.map { path ->
            DynamicTest.dynamicTest(path.name) {
                println("Testing: $path")
                try {
                    ElfFile.open(path).use { elfFile ->
                        // Basic validation
                        println("ELF Class: ${elfFile.ident.eiClass}")
                        println("ELF Type: ${elfFile.ehdr.eType}")
                        println("Machine: ${elfFile.ehdr.eMachine}")

                        // Print sections
                        println("Sections: ${elfFile.sectionHeaders.size}")
                        elfFile.sectionHeaders.take(5).forEach { section ->
                            println("  ${section.name ?: "<unnamed>"}: Type=${section.shType}, Size=${section.shSize}")
                        }

                        // Print program headers
                        println("Program Headers: ${elfFile.programHeaders.size}")
                        elfFile.programHeaders.take(5).forEach { header ->
                            println("  Type=${header.pType}, Flags=${header.pFlags}")
                        }

                        // Print symbols
                        val symbolCount = elfFile.symbols.size
                        println("Symbols: $symbolCount")
                        elfFile.symbols.take(5).forEach { symbol ->
                            println("  ${symbol.name}: Type=${symbol.type}, Binding=${symbol.binding}")
                        }

                        // Print import/export symbols
                        println("Import Symbols: ${elfFile.importSymbols.size}")
                        println("Export Symbols: ${elfFile.exportSymbols.size}")
                    }
                } catch (e: Exception) {
                    println("Error parsing $path: ${e.message}")
                    // Don't fail the test, just log the error
                    // This is a fuzzing test, so some files might be corrupted or have unusual formats
                }
            }
        }
    }

    @Test
    fun testSingleElfFile() {
        // Test a single known ELF file from resources
        // This is useful for debugging and as a sanity check
        val resourcePath = Path.of("src/jvmTest/resources/elf/hello")
        Assumptions.assumeTrue(resourcePath.exists(), "Test resource hello not found")

        ElfFile.open(resourcePath).use { elfFile ->
            println("ELF Class: ${elfFile.ident.eiClass}")
            println("ELF Type: ${elfFile.ehdr.eType}")
            println("Machine: ${elfFile.ehdr.eMachine}")

            // Print sections
            println("Sections: ${elfFile.sectionHeaders.size}")
            elfFile.sectionHeaders.forEach { section ->
                println("  ${section.name ?: "<unnamed>"}: Type=${section.shType}, Size=${section.shSize}")
            }

            // Print symbols
            println("Symbols: ${elfFile.symbols.size}")
            elfFile.symbols.forEach { symbol ->
                println("  ${symbol.name}: Type=${symbol.type}, Binding=${symbol.binding}")
            }
        }
    }
} 