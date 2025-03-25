package space.iseki.executables.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NativeFileDataAccessorTest {

    @Test
    fun testRead() {
        val o = NativeFileDataAccessor("src/mingwX64Test/resources/test_read")
        o.use {
            val buf = ByteArray(10)
            val read = it.readAtMost(0, buf, 0, 10)
            assertTrue(read > 0)
            val s = buf.decodeToString()
            assertEquals("0123456789", s)
            val read2 = it.readAtMost(3, buf, 0, 10)
            buf.sliceArray(0 until read2).decodeToString().trim().also { s ->
                assertEquals("3456789", s)
            }

            val largeBuf = ByteArray(100)
            val read3 = it.readAtMost(0, largeBuf, 0, 100)
            assertTrue(read3 > 0)
            val s3 = largeBuf.sliceArray(0 until read3).decodeToString()
            assertEquals("0123456789", s3.trim())
        }
        assertFailsWith<IOException> {
            o.readAtMost(100, ByteArray(10), 0, 10)
        }.also { println(it.message) }

    }

    @Test
    fun testThrow() {
        val e = assertFailsWith<IOException> {
            NativeFileDataAccessor("a_file_shouldnot_exists")
        }
        assertTrue(e.message) { "errno = 2" in e.message.orEmpty() }
    }
}