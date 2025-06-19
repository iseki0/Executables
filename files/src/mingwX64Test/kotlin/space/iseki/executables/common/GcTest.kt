package space.iseki.executables.common

import kotlinx.atomicfu.atomic
import space.iseki.executables.pe.PEFile
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(NativeRuntimeApi::class)

class GcTest {
    init {
        UnmapHolder.nativeAccessCounter = atomic(0)
    }

    fun aa() {
        assertEquals(0, UnmapHolder.nativeAccessCounter?.value)
        val f = PEFile.open("C:/Windows/system32/notepad.exe")
        repeat(3) { GC.collect() }
        assertEquals(1, UnmapHolder.nativeAccessCounter?.value)
        println(f)
    }

    @Test
    fun testGC() {
        aa()
        repeat(3) { GC.collect() }
        assertEquals(0, UnmapHolder.nativeAccessCounter?.value)
    }

}