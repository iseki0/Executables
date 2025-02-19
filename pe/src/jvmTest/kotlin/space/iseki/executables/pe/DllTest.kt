package space.iseki.executables.pe

import space.iseki.executables.common.ByteArrayDataAccessor
import space.iseki.executables.common.ExecutableFile
import kotlin.test.Test
import kotlin.test.assertEquals

private val data = DllTest::class.java.classLoader.getResourceAsStream("cygintl-8.dll")!!.readAllBytes()
class DllTest{

    @Test
    fun testType(){
        assertEquals(ExecutableFile.PE, ExecutableFile.detect(ByteArrayDataAccessor(data)))
    }

    @Test
    fun testOpen(){
        PEFile.wrap(data).use { f->
            println(f.summary)
        }
    }
}