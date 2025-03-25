package space.iseki.executables.elf

import kotlinx.serialization.json.Json
import kotlin.test.Test

class DBFileSoTest {
    private val json = Json {
        prettyPrint = true
    }

    @Test
    fun testOpen() {
        ElfFile.open(dbFileData)
    }

    @Test
    fun testExportSymbols() {
        val file = ElfFile.open(dbFileData)
        file.exportSymbols.forEach { println(it) }
    }

}
