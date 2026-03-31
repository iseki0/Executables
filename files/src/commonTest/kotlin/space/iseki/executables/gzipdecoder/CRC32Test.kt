package space.iseki.executables.gzipdecoder

import kotlin.test.Test
import kotlin.test.assertEquals

class CRC32Test {
    @Test
    fun computesKnownCrc32ForAsciiPayload() {
        val crc = CRC32()
        crc.update("123456789".encodeToByteArray())

        assertEquals(0xcbf43926u, crc.value())
    }

    @Test
    fun supportsIncrementalUpdates() {
        val crc = CRC32()
        crc.update("1234".encodeToByteArray())
        crc.update("56789".encodeToByteArray())

        assertEquals(0xcbf43926u, crc.value())
    }

    @Test
    fun supportsSliceUpdates() {
        val crc = CRC32()
        val bytes = byteArrayOf(0x00, 0x31, 0x32, 0x33, 0x34, 0x00)

        crc.update(bytes, offset = 1, length = 4)

        assertEquals(0x9be3e0a3u, crc.value())
    }
}
