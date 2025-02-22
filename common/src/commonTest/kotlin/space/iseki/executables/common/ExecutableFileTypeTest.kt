package space.iseki.executables.common

import kotlin.test.Test
import kotlin.test.assertEquals

class ExecutableFileTypeTest {
    @Test
    fun test() {
        assertEquals(ExecutableFileType.PE, ExecutableFileType.detect(java_exe))
        assertEquals(null, ExecutableFileType.detect(byteArrayOf(0, 0, 0, 0)))
    }
}
