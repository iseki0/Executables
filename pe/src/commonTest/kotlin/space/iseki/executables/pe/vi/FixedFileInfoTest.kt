package space.iseki.executables.pe.vi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals

class FixedFileInfoTest {
    companion object {
        private val json = Json { prettyPrint = true }
    }

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
        val jt = json.encodeToString(info)
        println(jt)
        assertEquals(json.parseToJsonElement(jt), json.encodeToJsonElement(info))
    }
} 