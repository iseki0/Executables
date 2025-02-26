package space.iseki.executables.pe

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import space.iseki.executables.pe.vi.locateVersionInfo
import space.iseki.executables.pe.vi.parseVersionData
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JavaExeTest {

    private val json = Json { prettyPrint = true }

    @Test
    fun test() {
        println(java_exe.size)
        val pe = PEFile.open(java_exe)
        println(pe.coffHeader)
        println(pe.standardHeader)
        println(pe.windowsHeader)
        println(pe.sectionTable)
        val jsonText = json.encodeToString(pe.summary)
        println(json.encodeToString(pe.summary))
        val je = Json.decodeFromString<JsonElement>(jsonText)
        assertEquals(java_exe_json, je)
    }

    @Test
    fun testReadResourceTree() {
        var h = 0
        var c = 0
        var totalSize = 0
        PEFile.open(java_exe).use { pe ->
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
        PEFile.open(java_exe).use { pe ->
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

    @Test
    fun testLocateVersionInfo() {
        PEFile.open(java_exe).use { peFile ->
            val resNode = locateVersionInfo(peFile)
            println(resNode)
            assertEquals(
                "<FILE:ID=1033, CodePage=windows-1252, Size=804, ContentRVA=0x0000c51c> @0x00000290",
                resNode.toString()
            )
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testReadVersionInfoRsrc() {
        val ref = """
240334000000560053005f00560045005200530049004f004e005f0049004e00
46004f0000000000bd04effe0000010000001400010002000000140001000200
0300000000000000040000000100000000000000000000000000000082020000
010053007400720069006e006700460069006c00650049006e0066006f000000
5e02000001003000340030003900300034006200300000004000100001004300
6f006d00700061006e0079004e0061006d0065000000000041006d0061007a00
6f006e002e0063006f006d00200049006e0063002e0000005800180001004600
69006c0065004400650073006300720069007000740069006f006e0000000000
4f00700065006e004a0044004b00200050006c006100740066006f0072006d00
2000620069006e006100720079000000320009000100460069006c0065005600
65007200730069006f006e0000000000320030002e0030002e0032002e003100
000000003e000f000100460075006c006c002000560065007200730069006f00
6e000000320030002e0030002e0032002e0031002b00310030002d0046005200
000000002a000500010049006e007400650072006e0061006c004e0061006d00
650000006a00610076006100000000004600110001004c006500670061006c00
43006f007000790072006900670068007400000043006f007000790072006900
6700680074002000a90020003200300032003300000000003a00090001004f00
72006900670069006e0061006c00460069006c0065006e0061006d0065000000
6a006100760061002e006500780065000000000054001a000100500072006f00
64007500630074004e0061006d006500000000004f00700065006e004a004400
4b00200050006c006100740066006f0072006d002000320030002e0030002e00
32002e0031000000360009000100500072006f00640075006300740056006500
7200730069006f006e000000320030002e0030002e0032002e00310000000000
440000000100560061007200460069006c00650049006e0066006f0000000000
2400040000005400720061006e0073006c006100740069006f006e0000000000
0904b004
        """.trimIndent().replace(Regex("[\\r\\n ]"), "").hexToByteArray()
        val bytes = PEFile.open(java_exe).use { locateVersionInfo(it)!!.readAllBytes() }
        val hexFormat = HexFormat {
            this.bytes.bytesPerLine = 32
        }
        assertContentEquals(ref, bytes)
        println(bytes.toHexString(hexFormat))
    }

    @Test
    fun testParseVersionInfo() {
        PEFile.open(java_exe).use { peFile ->
            val resNode = locateVersionInfo(peFile)!!
            val vi = parseVersionData(resNode.readAllBytes(), 0)
            println(vi)
        }
    }
}