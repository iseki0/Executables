package space.iseki.executables.gzipdecoder

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeflateDecoderTest {
    @Test
    fun decodesStoredBlock() {
        val decoded = DeflateDecoder.decode(hex("010500faff48454c4c4f"))
        assertContentEquals("HELLO".encodeToByteArray(), decoded)
    }

    @Test
    fun decodesFixedHuffmanBlock() {
        val decoded = DeflateDecoder.decode(hex("cb48cdc9c957c840905c00"))
        assertContentEquals("hello hello hello\n".encodeToByteArray(), decoded)
    }

    @Test
    fun decodesDynamicHuffmanBlock() {
        val decoded = DeflateDecoder.decode(hex("4b4c2a4a4c4e4c4904520a89d8d900"))
        assertContentEquals("abracadabra abracadabra abracadabra".encodeToByteArray(), decoded)
    }

    @Test
    fun rejectsStoredBlockWithInvalidComplement() {
        val error = assertFailsWith<GzipDecodingException> {
            DeflateDecoder.decode(hex("010500fbff48454c4c4f"))
        }
        assertEquals("invalid stored block length", error.message)
    }

    @Test
    fun rejectsReservedBlockType() {
        val error = assertFailsWith<GzipDecodingException> {
            DeflateDecoder.decode(byteArrayOf(0x07))
        }
        assertEquals("reserved deflate block type", error.message)
    }

    @Test
    fun rejectsInvalidBackwardDistanceInOutputWindow() {
        val output = DeflateOutput()
        output.append('A'.code)

        val error = assertFailsWith<GzipDecodingException> {
            output.copyFromDistance(distance = 2, length = 3)
        }
        assertEquals("invalid backward distance: 2", error.message)
    }

    private fun hex(value: String): ByteArray {
        require(value.length % 2 == 0) { "hex string must have an even length" }
        return ByteArray(value.length / 2) { index ->
            value.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}
