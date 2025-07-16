package space.iseki.executables.common

import kotlinx.atomicfu.atomic
import platform.posix.sleep
import space.iseki.executables.pe.PEFile
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

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
        repeat(10) {
            repeat(3) { GC.collect() }
            if (UnmapHolder.nativeAccessCounter?.value == 0) return
            sleep(1u)
        }
        fail("UnmapHolder.nativeAccessCounter should be 0")
    }

}