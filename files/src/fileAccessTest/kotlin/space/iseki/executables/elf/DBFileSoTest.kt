package space.iseki.executables.elf

import space.iseki.executables.common.openNativeFileDataAccessor
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DBFileSoTest {
    companion object {
        const val PATH = "src/fileAccessTest/resources/elf/dbfile.so"
    }

    @Test
    fun testOpen() {
        ElfFile.open(PATH).close()
    }

    @Test
    fun testExportSymbols() {
        val symbols = listOf(
            ElfExportSymbol(
                name = "DB_File___unused",
                value = 41856u,
                size = 7u,
                binding = ElfSymBinding.STB_GLOBAL,
                type = ElfSymType.STT_FUNC,
                visibility = ElfSymVisibility.STV_DEFAULT
            ),
            ElfExportSymbol(
                name = "__getBerkeleyDBInfo",
                value = 41472u,
                size = 370u,
                binding = ElfSymBinding.STB_GLOBAL,
                type = ElfSymType.STT_FUNC,
                visibility = ElfSymVisibility.STV_DEFAULT
            ),
            ElfExportSymbol(
                name = "boot_DB_File",
                value = 41872u,
                size = 902u,
                binding = ElfSymBinding.STB_GLOBAL,
                type = ElfSymType.STT_FUNC,
                visibility = ElfSymVisibility.STV_DEFAULT
            ),
        )
        val file = ElfFile.open(PATH)
        assertEquals(symbols.size, file.exportSymbols.size)
        assertContentEquals(symbols, file.exportSymbols)
    }

    @Test
    fun testSectionReading() {
        ElfFile.open(PATH).use { file ->
            assertEquals(29, file.sections.size)
            for (section in file.sections) {
                if (section.name.isNullOrEmpty()) {
                    assertEquals(ElfSType.SHT_NULL, section.sectionHeader.shType)
                    continue
                }
                val sname = section.name.orEmpty()
                val actual = ByteArray(section.size.toInt())
                section.readBytes(0, actual, 0, actual.size)
                openNativeFileDataAccessor("$PATH.sections/$sname").use { da ->
                    val expected = ByteArray(section.size.toInt())
                    da.readAtMost(0, expected, 0, expected.size)
                    assertContentEquals(expected, actual, "Section $sname mismatch")
                }
            }
        }
    }

}
