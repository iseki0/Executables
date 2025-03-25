package space.iseki.executables.elf

import kotlin.test.Test

class DBFileSoTest {
    @Test
    fun testOpen() {
        try {
            ElfFile.open("src/commonTest/resources/elf/dbfile.so").close()
        } catch (_: UnsupportedOperationException) {
        }
    }

    @Test
    fun testExportSymbols() {
        try {
            val file = ElfFile.open("src/commonTest/resources/elf/dbfile.so")
            file.exportSymbols.forEach { println(it) }
        } catch (_: UnsupportedOperationException) {
        }
    }

}
