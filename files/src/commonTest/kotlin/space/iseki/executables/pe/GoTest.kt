package space.iseki.executables.pe

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import space.iseki.executables.common.FileFormat
import space.iseki.executables.common.detect
import space.iseki.executables.common.openNativeFileDataAccessor
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class GoTest {
    companion object {
        const val PATH = "src/commonTest/resources/pe/go.exe"
    }

    @Test
    fun testOpen() {
        try {
            assertEquals(PEFile, FileFormat.detect(PATH))
            PEFile.open(PATH).use {}
        } catch (_: UnsupportedOperationException) {
        }
    }

    @Test
    fun testReadSections() {
        try {
            PEFile.open(PATH).use { file ->
                file.sections.forEach { println(it) }
                for (section in file.sections) {
                    val sName = section.name
                    val actual = ByteArray(section.size.toInt())
                    openNativeFileDataAccessor("$PATH.sections/$sName").use { it.readFully(0, actual) }
                    val expected = ByteArray(section.size.toInt())
                    section.readBytes(0, expected, 0, expected.size)
                    assertEquals(expected.size, actual.size, "Section $sName size mismatch")
                    assertContentEquals(expected, actual, "Section $sName mismatch")
                }
            }
        } catch (_: UnsupportedOperationException) {
        }
    }

    @Test
    fun testSummaryJson() {
        try {
            val buf = openNativeFileDataAccessor("$PATH.summary.json").use {
                val buf = ByteArray(it.size.toInt())
                it.readFully(0, buf)
                buf
            }
            PEFile.open(PATH).use { pe ->
                val actual = Json.encodeToJsonElement(pe.summary)
                val expect = Json.decodeFromString<JsonElement>(buf.decodeToString())
                assertEquals(expect, actual)
            }
        } catch (_: UnsupportedOperationException) {
        }
    }
}