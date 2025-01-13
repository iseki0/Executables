package space.iseki.executables.pe

import kotlin.test.Test

class PEFilesTest {

    @Test
    fun testDumpHeaderJson(){
        println(dumpHeaderJson(java_exe, true))
    }
}
