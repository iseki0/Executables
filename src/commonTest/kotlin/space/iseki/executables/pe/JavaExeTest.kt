package space.iseki.executables.pe

import kotlinx.serialization.json.Json
import kotlin.test.Test

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
        println(json.encodeToString(pe.summary))
    }
}