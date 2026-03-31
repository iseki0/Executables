package space.iseki.executables.gzipdecoder

import space.iseki.executables.common.EOFException
import space.iseki.executables.common.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BitReaderTest {

    @Test
    fun readsBitsInDeflateOrderAcrossByteBoundaries() {
        val reader = bitReaderOf(
            byteArrayOf(
                0b1011_0010.toByte(),
                0b0110_0001.toByte(),
            )
        )

        assertEquals(0b010u, reader.readBits(3))
        assertEquals(0b10110u, reader.readBits(5))
        assertEquals(0b0001u, reader.readBits(4))
        assertEquals(0b0110u, reader.readBits(4))
        assertEquals(16L, reader.totalBitsRead)
    }

    @Test
    fun alignsToNextByteBoundary() {
        val reader = bitReaderOf(
            byteArrayOf(
                0b1110_0101.toByte(),
                0x7a,
                0x11,
            )
        )

        assertEquals(0b101u, reader.readBits(3))
        reader.alignToByteBoundary()
        assertEquals(8L, reader.totalBitsRead)
        assertEquals(0x7a, reader.readUnsignedByte())
        assertEquals(0x11, reader.readUnsignedByte())
    }

    @Test
    fun readsAlignedBytesAfterBitReads() {
        val reader = bitReaderOf(byteArrayOf(0xff.toByte(), 0x10, 0x20, 0x30), chunkSize = 1)

        assertEquals(0b1111u, reader.readBits(4))

        val bytes = ByteArray(3)
        reader.readAlignedBytes(bytes)

        assertEquals(listOf(0x10, 0x20, 0x30), bytes.map { it.toInt() and 0xFF })
        assertEquals(32L, reader.totalBitsRead)
    }

    @Test
    fun readsAlignedBytesIntoSubrange() {
        val reader = bitReaderOf(byteArrayOf(0x55, 0x66, 0x77))
        val bytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)

        reader.readAlignedBytes(bytes, offset = 1, length = 2)

        assertEquals(listOf(0x01, 0x55, 0x66, 0x04), bytes.map { it.toInt() and 0xFF })
        assertEquals(16L, reader.totalBitsRead)
    }

    @Test
    fun throwsEofWhenRequestedBitsAreUnavailable() {
        val reader = bitReaderOf(byteArrayOf(0x34, 0x12), chunkSize = 1)

        assertEquals(0x1234u, reader.readBits(16))
        assertFailsWith<EOFException> {
            reader.readBit()
        }
    }

    @Test
    fun rejectsInvalidZeroLengthSourceReads() {
        val reader = BitReader(source = { _ -> 0 })

        val exception = assertFailsWith<IOException> {
            reader.readBit()
        }
        assertEquals("Invalid source read result: 0", exception.message)
    }

    private fun bitReaderOf(bytes: ByteArray, chunkSize: Int = bytes.size.coerceAtLeast(1)): BitReader {
        var position = 0
        return BitReader(source = { dest: ByteArray ->
            if (position >= bytes.size) {
                -1
            } else {
                val toCopy = minOf(dest.size, chunkSize, bytes.size - position)
                bytes.copyInto(dest, destinationOffset = 0, startIndex = position, endIndex = position + toCopy)
                position += toCopy
                toCopy
            }
        })
    }
}
