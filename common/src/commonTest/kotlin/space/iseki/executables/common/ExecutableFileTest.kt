package space.iseki.executables.common

import kotlin.test.Test
import kotlin.test.assertEquals

class ExecutableFileTest {

    @Test
    @Suppress("DEPRECATION")
    fun testDetect() {
        assertEquals(ExecutableFile.PE, ExecutableFile.detect(ByteArrayDataAccessor(java_exe)))
        assertEquals(null, ExecutableFile.detect(ByteArrayDataAccessor(byteArrayOf(0, 0, 0, 0))))
        assertEquals(null, ExecutableFile.detect(ByteArrayDataAccessor(byteArrayOf(0, 0))))
    }
}
