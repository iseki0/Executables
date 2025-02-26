package space.iseki.executables.pe

import space.iseki.executables.common.ByteArrayDataAccessor
import space.iseki.executables.common.ExecutableFileType
import kotlin.test.Test
import kotlin.test.assertEquals

private val data = DllTest::class.java.classLoader.getResourceAsStream("cygintl-8.dll")!!.readAllBytes()

class DllTest {

    @Test
    fun testType() {
        assertEquals(ExecutableFileType.PE, ExecutableFileType.detect(ByteArrayDataAccessor(data)))
    }

    @Test
    fun testOpen() {
        PEFile.open(data).use { f ->
            println(f.summary)
        }
    }
}