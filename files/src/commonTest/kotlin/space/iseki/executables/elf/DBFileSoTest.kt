package space.iseki.executables.elf

import space.iseki.executables.common.openNativeFileDataAccessor
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

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

    @Test
    fun testSectionReading() {
        try {
            ElfFile.open("src/commonTest/resources/elf/dbfile.so").use { file ->
                println(file.sections)
                for (section in file.sections) {
                    if (section.name.isNullOrEmpty()) {
                        assertEquals(ElfSType.SHT_NULL, section.sectionHeader.shType)
                        continue
                    }
                    val sname = section.name.orEmpty()
                    val actual = ByteArray(section.size.toInt())
                    section.readBytes(0, actual, 0, actual.size)
                    openNativeFileDataAccessor("src/commonTest/resources/elf/dbfile.so.sections/$sname").use { da ->
                        val expected = ByteArray(section.size.toInt())
                        da.readAtMost(0, expected, 0, expected.size)
                        assertContentEquals(expected, actual, "Section $sname mismatch")
                    }
                }
            }
        } catch (_: UnsupportedOperationException) {
        }
    }

}
