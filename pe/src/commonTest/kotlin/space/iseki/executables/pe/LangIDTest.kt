package space.iseki.executables.pe

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for the LangID class
 */
class LangIDTest {
    companion object {
        private val json = Json { prettyPrint = true }
    }

    @Test
    fun testLangIDFromUShort() {
        // Test creating LangID from UShort
        val langID = LangID(0x0409u.toUShort())
        assertEquals("en-US", langID.toString())

        // Test unknown LangID
        val unknownLangID = LangID(0xFFFFu.toUShort())
        assertEquals("0xffff", unknownLangID.toString())
    }

    @Test
    fun testLangIDFromInt() {
        // Test creating LangID from Int
        val langID = LangID(0x0409.toUShort())
        assertEquals("en-US", langID.toString())
    }

    @Test
    fun testLangIDFromString() {
        // Test creating LangID from language code string
        val langID1 = LangID("en-US")
        assertEquals(0x0409u.toUShort(), langID1.value)

        // Test creating LangID from hexadecimal string
        val langID2 = LangID("0x0409")
        assertEquals(0x0409u.toUShort(), langID2.value)

        // Test invalid string
        assertFailsWith<IllegalArgumentException> {
            LangID("invalid-lang-id")
        }
    }

    @Test
    fun testLangIDSerialization() {
        // Test serialization and deserialization of LangID
        val original = LangID(0x0409u.toUShort())
        val serialized = json.encodeToString<LangID>(original)
        val deserialized = json.decodeFromString<LangID>(serialized)

        assertEquals(original, deserialized)
        assertEquals("en-US", serialized.replace("\"", ""))

        // Test deserialization from serialized string
        val fromString = json.decodeFromString<LangID>("\"en-US\"")
        assertEquals(original, fromString)
    }

    @Test
    fun testCommonLanguages() {
        // Test some common language IDs
        val testCases = mapOf(
            0x0409u.toUShort() to "en-US",
            0x0804u.toUShort() to "zh-CN",
            0x0404u.toUShort() to "zh-TW",
            0x0411u.toUShort() to "ja-JP",
            0x0412u.toUShort() to "ko-KR",
            0x040Cu.toUShort() to "fr-FR",
            0x0407u.toUShort() to "de-DE",
            0x0410u.toUShort() to "it-IT",
            0x0c0au.toUShort() to "es-ES"
        )

        for ((id, expected) in testCases) {
            val langID = LangID(id)
            assertEquals(expected, langID.toString())

            // Test reverse lookup
            val fromString = LangID(expected)
            assertEquals(id, fromString.value)
        }
    }
} 