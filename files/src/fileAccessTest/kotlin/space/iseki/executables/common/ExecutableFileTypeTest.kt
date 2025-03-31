package space.iseki.executables.common

import space.iseki.executables.elf.ElfFile
import space.iseki.executables.java_exe
import space.iseki.executables.macho.MachoFile
import space.iseki.executables.pe.PEFile
import kotlin.test.Test
import kotlin.test.assertEquals

class ExecutableFileTypeTest {
    @Test
    fun testPE() {
        assertEquals(PEFile, FileFormat.detect(java_exe))
        assertEquals(PEFile, FileFormat.detect("src/fileAccessTest/resources/pe/java.exe"))

    }

    @Test
    fun testElf() {
        assertEquals(ElfFile, FileFormat.detect("src/fileAccessTest/resources/elf/hello"))
    }

    @Test
    fun testMacho() {
        assertEquals(MachoFile, FileFormat.detect("src/fileAccessTest/resources/macho/tiny-macho"))
    }

    @Test
    fun testNull() {
        assertEquals(null, FileFormat.detect(byteArrayOf(0, 0, 0, 0)))
        assertEquals(null, FileFormat.detect(byteArrayOf()))
    }
}
