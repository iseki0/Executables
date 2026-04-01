package space.iseki.executables.share

import space.iseki.executables.common.ByteArrayDataAccessor
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class MemReaderTest {

    @Test
    fun readFillsUnmappedGapsWithZeroesAcrossSegments() {
        val reader = MemReader(ByteArrayDataAccessor(byteArrayOf(1, 2, 3, 4, 5, 6)))
        reader.mapMemory(vOff = 0x10u, fOff = 0u, fSize = 2u)
        reader.mapMemory(vOff = 0x14u, fOff = 2u, fSize = 3u)

        val buf = ByteArray(7)
        reader.read(pos = 0x10u, buf = buf, off = 0, len = buf.size)

        assertContentEquals(byteArrayOf(1, 2, 0, 0, 3, 4, 5), buf)
    }

    @Test
    fun readHonorsBufferOffsetAndLeavesOutsideRangeUntouched() {
        val reader = MemReader(ByteArrayDataAccessor(byteArrayOf(10, 11, 12, 13)))
        reader.mapMemory(vOff = 0x20u, fOff = 0u, fSize = 4u)

        val buf = byteArrayOf(99, 99, 99, 99, 99, 99)
        reader.read(pos = 0x20u, buf = buf, off = 1, len = 4)

        assertContentEquals(byteArrayOf(99, 10, 11, 12, 13, 99), buf)
    }

    @Test
    fun readReturnsZeroesForFullyUnmappedRange() {
        val reader = MemReader(ByteArrayDataAccessor(byteArrayOf(1, 2, 3)))
        reader.mapMemory(vOff = 0x10u, fOff = 0u, fSize = 3u)

        val buf = byteArrayOf(7, 7, 7, 7)
        reader.read(pos = 0x30u, buf = buf, off = 0, len = buf.size)

        assertContentEquals(byteArrayOf(0, 0, 0, 0), buf)
    }

    @Test
    fun mapMemoryIgnoresZeroSizedZeroOffsetSegment() {
        val reader = MemReader(ByteArrayDataAccessor(byteArrayOf(42)))
        reader.mapMemory(vOff = 0u, fOff = 0u, fSize = 0u)
        reader.mapMemory(vOff = 1u, fOff = 0u, fSize = 1u)

        val buf = ByteArray(1)
        reader.read(pos = 1u, buf = buf, off = 0, len = 1)

        assertContentEquals(byteArrayOf(42), buf)
    }

    @Test
    fun mapMemoryRejectsOutOfOrderSegments() {
        val reader = MemReader(ByteArrayDataAccessor(byteArrayOf()))
        reader.mapMemory(vOff = 0x20u, fOff = 0u, fSize = 2u)

        assertFailsWith<IllegalStateException> {
            reader.mapMemory(vOff = 0x10u, fOff = 2u, fSize = 2u)
        }
    }

    @Test
    fun mapMemoryRejectsOverlappingSegments() {
        val reader = MemReader(ByteArrayDataAccessor(byteArrayOf()))
        reader.mapMemory(vOff = 0x20u, fOff = 0u, fSize = 4u)

        assertFailsWith<IllegalStateException> {
            reader.mapMemory(vOff = 0x22u, fOff = 4u, fSize = 2u)
        }
    }
}
