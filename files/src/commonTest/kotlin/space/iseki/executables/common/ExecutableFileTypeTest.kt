package space.iseki.executables.common

import space.iseki.executables.java_exe
import space.iseki.executables.pe.PEFile
import kotlin.test.Test
import kotlin.test.assertEquals

class ExecutableFileTypeTest {
    @Test
    fun test() {
        assertEquals(PEFile, FileFormat.detect(java_exe))
        try {
            assertEquals(PEFile, FileFormat.detect("src/commonTest/resources/java.exe"))
        } catch (_: UnsupportedOperationException) {
        }
        assertEquals(null, FileFormat.detect(byteArrayOf(0, 0, 0, 0)))
    }
}
