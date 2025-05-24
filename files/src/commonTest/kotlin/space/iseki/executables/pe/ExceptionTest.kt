package space.iseki.executables.pe

import space.iseki.executables.common.ByteArrayDataAccessor
import space.iseki.executables.java_exe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExceptionTest {

    @Test
    fun testEOFDuringPEMagicReading() {
        val accessor = ByteArrayDataAccessor(byteArrayOf(0, 0, 0, 0))
        val e = assertFailsWith<PEFileException> {
            PEFile.open(accessor)
        }
        assertEquals("Not a PE file, unexpected EOF during read PE magic", e.message)
    }

    @Test
    fun testBadPEMagic() {
        val copyOf = java_exe.copyOf()
        // break the magic
        copyOf[0x108] = 0
        copyOf[0x109] = 0
        val e = assertFailsWith<PEFileException> {
            PEFile.open(copyOf)
        }
        assertEquals("Not a PE file, bad magic", e.message)
        assertEquals(e.arguments, listOf("magic" to "0x00000000"))
    }

}