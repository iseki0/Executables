package space.iseki.executables.sbom

import space.iseki.executables.common.FileFormat
import space.iseki.executables.common.detect
import space.iseki.executables.macho.MachoFile
import space.iseki.executables.pe.PEFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GoSBomTest {
    companion object {
        private const val GO117_PATH = "src/fileAccessTest/resources/sbom/go117"
        private const val NOTGO_PATH = "src/fileAccessTest/resources/sbom/notgo"
        private const val GO_EXE = "src/fileAccessTest/resources/pe/go.exe"
        private const val GO_MACHO = "src/fileAccessTest/resources/macho/go-hello"
    }

    /**
     * Tests that Go build information can be parsed from a Go 1.17 binary.
     */
    @Test
    fun testReadFromGo117() {
        val format = FileFormat.detect(GO117_PATH)
        assertNotNull(format, "File format should be detected")

        format.open(GO117_PATH).use { file ->
            // Test the new exception-throwing API
            val sbom = GoSBom.readFrom(file)

            // 打印实际的Go版本
            println("Actual Go version: ${sbom.buildInfo?.goVersion}")

            // Verify the basic information
            assertEquals("go1.17", sbom.buildInfo?.goVersion)
            assertNotNull(sbom.buildInfo?.main)

            // Verify the main module
            val main = sbom.buildInfo?.main
            assertNotNull(main)
            assertEquals("example.com/go117", main.path)
            assertEquals("(devel)", main.version)
            assertEquals("pkg:golang/example.com/go117@(devel)", main.purl)

            // Verify we have dependencies
            val deps = sbom.buildInfo?.deps
            assertNotNull(deps)
            assertTrue(deps.isEmpty())
        }

    }

    /**
     * Tests that the convenience method tryReadFrom correctly handles errors.
     */
    @Test
    fun testTryReadFromGo117() {
        val format = FileFormat.detect(GO117_PATH)
        assertNotNull(format, "File format should be detected")

        format.open(GO117_PATH).use { file ->
            // The convenience method should work too
            val sbom = GoSBom.readFromOrNull(file)
            assertNotNull(sbom)

            // 打印实际的Go版本
            println("Actual Go version in tryReadFrom: ${sbom.buildInfo?.goVersion}")

            assertEquals("go1.17", sbom.buildInfo?.goVersion)
        }
    }

    /**
     * Tests that the appropriate exception is thrown when parsing a non-Go file.
     */
    @Test
    fun testReadFromNotGo() {
        val format = FileFormat.detect(NOTGO_PATH)
        assertNotNull(format, "File format should be detected")

        format.open(NOTGO_PATH).use { file ->
            // Should throw an exception
            assertFailsWith<SBomNotFoundException> {
                GoSBom.readFrom(file)
            }
        }
    }

    /**
     * Tests that the convenience method handles a non-Go file gracefully.
     */
    @Test
    fun testTryReadFromNotGo() {
        val format = FileFormat.detect(NOTGO_PATH)
        assertNotNull(format, "File format should be detected")

        format.open(NOTGO_PATH).use { file ->
            // Should return null
            val sbom = GoSBom.readFromOrNull(file)
            assertNull(sbom)
        }
    }

    @Test
    fun testReadPE() {
        val expectedSettings = listOf(
            GoBuildSetting("-buildmode", "exe"),
            GoBuildSetting("-compiler", "gc"),
            GoBuildSetting("-trimpath", "true"),
            GoBuildSetting("CGO_ENABLED", "1"),
            GoBuildSetting("GOARCH", "amd64"),
            GoBuildSetting("GOOS", "windows"),
            GoBuildSetting("GOAMD64", "v1")
        )

        PEFile.open(GO_EXE).use { file ->
            val sbom = GoSBom.readFrom(file)
            assertNotNull(sbom)
            val buildInfo = sbom.buildInfo
            assertNotNull(buildInfo)
            assertEquals("go1.24.0", buildInfo.goVersion)
            assertNull(buildInfo.main)
            assertEquals(expectedSettings, buildInfo.settings)
            assertEquals("command-line-arguments", buildInfo.path)
        }
    }

    @Test
    fun testParseMod() {
        val expectedSettings = listOf(
            GoBuildSetting("-buildmode", "exe"),
            GoBuildSetting("-compiler", "gc"),
            GoBuildSetting("-trimpath", "true"),
            GoBuildSetting("CGO_ENABLED", "1"),
            GoBuildSetting("GOARCH", "amd64"),
            GoBuildSetting("GOOS", "windows"),
            GoBuildSetting("GOAMD64", "v1")
        )
        val sample = """
            build	-buildmode=exe
            build	-compiler=gc
            build	-trimpath=true
            build	CGO_ENABLED=1
            build	GOARCH=amd64
            build	GOOS=windows
            build	GOAMD64=v1
        """.trimIndent()
        assertEquals(expectedSettings, GoBuildInfo.parse(sample).settings)
    }

    @Test
    fun testReadMacho() {
        val sbom = MachoFile.open(GO_MACHO).use { file ->
            GoSBom.readFrom(file)
        }
        val settings = listOf(
            GoBuildSetting("-buildmode", "exe"),
            GoBuildSetting("-compiler", "gc"),
            GoBuildSetting("-trimpath", "true"),
            GoBuildSetting("CGO_ENABLED", "0"),
            GoBuildSetting("GOARCH", "amd64"),
            GoBuildSetting("GOOS", "darwin"),
            GoBuildSetting("GOAMD64", "v1"),
        )
        val expected = GoBuildInfo(
            goVersion = "go1.24.0",
            path = "command-line-arguments",
            main = null,
            settings = settings,
            deps = emptyList(),
        )
        assertEquals(expected, sbom.buildInfo)
    }

} 