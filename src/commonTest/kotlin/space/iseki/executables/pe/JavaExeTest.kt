package space.iseki.executables.pe

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import space.iseki.executables.pe.serialization.PEFileSummarySerializer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JavaExeTest {

    private val json = Json { prettyPrint = true }

    @Test
    fun test() {
        println(java_exe.size)
        val pe = PEFile.wrap(java_exe)
        println(pe.coffHeader)
        println(pe.standardHeader)
        println(pe.windowsHeader)
        println(pe.sectionTable)
        val jsonText = json.encodeToString(PEFileSummarySerializer, pe.summary)
        println(json.encodeToString(PEFileSummarySerializer, pe.summary))
        val je = Json.decodeFromString<JsonElement>(jsonText)
        assertEquals(java_exe_json, je)
    }

    @Test
    fun testReadResourceTree() {
        var h = 0
        var c = 0
        var totalSize = 0
        PEFile.wrap(java_exe).use { pe ->
            for (entry in pe.resourceRoot.walk()) {
                val indent = "  ".repeat(entry.nodePath.size - 1)
                println(indent + entry.node)
                h += c++ * entry.node.hashCode()
                totalSize += entry.node.size.toInt()
            }
        }
        assertEquals(180528, h)
        assertEquals(27543, totalSize)
    }

    @Test
    fun testReadSection() {
        PEFile.wrap(java_exe).use { pe ->
            val sr = pe.sectionReader(".text")
            assertNotNull(sr)
            sr.test(+0, "48 8D 05 B9 30 00 00 C3")
            sr.test(-4, "00 00 00 00 48 8D 05 B9")
            sr.test(+8, "48 89 5C 24 08 48 89 6C")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun PEFile.SectionReader.test(off: Int, hex: String) {
        val data = hex.replace(" ", "").hexToByteArray()
        val arr = ByteArray(data.size)
        copyBytes(table.virtualAddress + off, arr)
        assertContentEquals(data, arr)
    }
}