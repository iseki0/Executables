package space.iseki.executables.macho

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class MachoTest {
    companion object {
        const val PATH = "src/fileAccessTest/resources/macho/tiny-macho"
    }
    @Test
    fun test() {
        // language=JSON
        val eH = """
            {"magic":"MH_CIGAM","cputype":7,"cpusubtype":3,"filetype":"MH_EXECUTE","ncmds":2,"sizeofcmds":136,"flags":["MH_PIE","MH_NO_HEAP_EXECUTION"],"isLittleEndian":true}
        """.trimIndent().let { Json.decodeFromString<MachoHeader>(it) }
        MachoFile.open(PATH).use {
            assertEquals(eH, it.header)
        }
    }

    @Test
    fun testReadCommands() {
        MachoFile.open(PATH).use {
            println(it.loaderCommands.toList())
        }
    }
}
