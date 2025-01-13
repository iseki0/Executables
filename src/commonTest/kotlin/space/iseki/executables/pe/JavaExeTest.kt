package space.iseki.executables.pe

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals

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
        val jsonText = json.encodeToString(pe.summary)
        println(json.encodeToString(pe.summary))
        val je = Json.decodeFromString<JsonElement>(jsonText)
        assertEquals(java_exe_json, je)
    }
}