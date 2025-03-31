package space.iseki.executables.share

import space.iseki.executables.common.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClosableDataAccessorTest {

    @Test
    fun test() {
        var closed = false
        val o = object : ClosableDataAccessor() {
            override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
                wrapRead {
                    return 1
                }
            }

            override fun doClose() {
                closed = true
            }

            override val size: Long
                get() = 1

        }

        assertFalse(closed)
        assertEquals(1, o.readAtMost(1, ByteArray(1), 0, 1))
        assertFalse(closed)
        o.close()
        assertTrue(closed)
        assertFailsWith<IOException> { o.readAtMost(1, ByteArray(1), 0, 1) }
        assertFailsWith<IOException> { o.close() }
    }

    @Test
    fun test2() {
        var closed = false
        val o = object : ClosableDataAccessor() {
            override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
                wrapRead {
                    close()
                }
                return 1
            }

            override fun doClose() {
                closed = true
            }

            override val size: Long
                get() = 1

        }
        assertEquals(1, o.readAtMost(1, ByteArray(1), 0, 1))
        assertTrue(closed)
        assertFailsWith<IOException> { o.readAtMost(1, ByteArray(1), 0, 1) }
        assertFailsWith<IOException> { o.close() }
    }
}
