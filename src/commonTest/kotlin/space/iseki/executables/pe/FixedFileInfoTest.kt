package space.iseki.executables.pe

import kotlinx.serialization.json.Json
import space.iseki.executables.pe.serializer.FixedFileInfoSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

class FixedFileInfoTest {
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testParse() {
        val bytes = """
            |BD 04 EF FE 00 00 01 00 
            |00 00 14 00 01 00 02 00 00 00 14 00 01 00 02 00 
            |03 00 00 00 00 00 00 00 04 00 00 00 01 00 00 00 
            |00 00 00 00 00 00 00 00 00 00 00 00 
        """.trimMargin().replace(Regex("""[\r\n ]"""), "").hexToByteArray()
        val info = FixedFileInfo.parse(bytes, 0)
        assertEquals(FixedFileInfo.LENGTH, bytes.size)
        val jt = Json { prettyPrint = true }.encodeToString(FixedFileInfoSerializer, info)
        println(jt)
        assertEquals(Json.parseToJsonElement(jt), Json.encodeToJsonElement(FixedFileInfoSerializer, info))
    }
} 