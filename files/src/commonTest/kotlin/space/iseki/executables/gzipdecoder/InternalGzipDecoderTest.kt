package space.iseki.executables.gzipdecoder

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InternalGzipDecoderTest {
    @Test
    fun decodesSingleMember() {
        val decoded = InternalGzipDecoder.decode(
            hex("1f8b08080000000002ff73616d706c652e74787400cb48cdc9c957c840905c003b7c8adf12000000")
        )
        assertContentEquals("hello hello hello\n".encodeToByteArray(), decoded)
    }

    @Test
    fun decodesMemberWithExtraCommentAndHeaderCrc() {
        val decoded = InternalGzipDecoder.decode(
            hex("1f8b081e15cd5b0700030400414243447370656c6c2e747874006d616769630005604b4c2a4a4c4e4c4904520a89d8d90083de94f923000000")
        )
        assertContentEquals("abracadabra abracadabra abracadabra".encodeToByteArray(), decoded)
    }

    @Test
    fun decodesConcatenatedMembers() {
        val decoded = InternalGzipDecoder.decode(
            hex("1f8b08000000000000ff010500faff48454c4c4f366444c1050000001f8b08000000000000ffcb48cdc9c957c840905c003b7c8adf12000000")
        )
        assertContentEquals("HELLOhello hello hello\n".encodeToByteArray(), decoded)
    }

    @Test
    fun rejectsInvalidMagic() {
        val error = assertFailsWith<GzipDecodingException> {
            InternalGzipDecoder.decode(hex("008b08000000000000ff010500faff48454c4c4f366444c105000000"))
        }
        assertEquals("invalid gzip magic", error.message)
    }

    @Test
    fun rejectsUnsupportedCompressionMethod() {
        val error = assertFailsWith<GzipDecodingException> {
            InternalGzipDecoder.decode(hex("1f8b00000000000000ff010500faff48454c4c4f366444c105000000"))
        }
        assertEquals("unsupported compression method: 0", error.message)
    }

    @Test
    fun rejectsHeaderCrcMismatch() {
        val error = assertFailsWith<GzipDecodingException> {
            InternalGzipDecoder.decode(
                hex("1f8b081e15cd5b0700030400414243447370656c6c2e747874006d616769630000604b4c2a4a4c4e4c4904520a89d8d90083de94f923000000")
            )
        }
        assertEquals("gzip header crc mismatch", error.message)
    }

    @Test
    fun rejectsDataCrcMismatch() {
        val error = assertFailsWith<GzipDecodingException> {
            InternalGzipDecoder.decode(
                hex("1f8b08080000000002ff73616d706c652e74787400cb48cdc9c957c840905c003a7c8adf12000000")
            )
        }
        assertEquals("gzip crc32 mismatch", error.message)
    }

    @Test
    fun rejectsIsizeMismatch() {
        val error = assertFailsWith<GzipDecodingException> {
            InternalGzipDecoder.decode(
                hex("1f8b08080000000002ff73616d706c652e74787400cb48cdc9c957c840905c003b7c8adf11000000")
            )
        }
        assertEquals("gzip isize mismatch", error.message)
    }

    @Test
    fun decodesLongTextSample() {
        val decoded = InternalGzipDecoder.decode(GzipTestFixtures.longTextGzip)
        assertContentEquals(GzipTestFixtures.longTextExpected(), decoded)
    }

    @Test
    fun decodesLongBinarySample() {
        val decoded = InternalGzipDecoder.decode(GzipTestFixtures.longBinaryGzip)
        assertContentEquals(GzipTestFixtures.longBinaryExpected(), decoded)
    }

    private fun hex(value: String): ByteArray = GzipTestFixtures.hex(value)
}
