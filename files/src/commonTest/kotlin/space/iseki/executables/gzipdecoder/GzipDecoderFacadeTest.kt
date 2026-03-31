package space.iseki.executables.gzipdecoder

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFails

class GzipDecoderFacadeTest {
    @Test
    fun decodesSingleMember() {
        val decoded = GzipDecoder.decode(
            hex("1f8b08080000000002ff73616d706c652e74787400cb48cdc9c957c840905c003b7c8adf12000000")
        )
        assertContentEquals("hello hello hello\n".encodeToByteArray(), decoded)
    }

    @Test
    fun decodesMemberWithExtraCommentAndHeaderCrc() {
        val decoded = GzipDecoder.decode(
            hex("1f8b081e15cd5b0700030400414243447370656c6c2e747874006d616769630005604b4c2a4a4c4e4c4904520a89d8d90083de94f923000000")
        )
        assertContentEquals("abracadabra abracadabra abracadabra".encodeToByteArray(), decoded)
    }

    @Test
    fun decodesConcatenatedMembers() {
        val decoded = GzipDecoder.decode(
            hex("1f8b08000000000000ff010500faff48454c4c4f366444c1050000001f8b08000000000000ffcb48cdc9c957c840905c003b7c8adf12000000")
        )
        assertContentEquals("HELLOhello hello hello\n".encodeToByteArray(), decoded)
    }

    @Test
    fun rejectsInvalidData() {
        assertFails {
            GzipDecoder.decode(hex("008b08000000000000ff010500faff48454c4c4f366444c105000000"))
        }
    }

    @Test
    fun decodesLongTextSample() {
        val decoded = GzipDecoder.decode(GzipTestFixtures.longTextGzip)
        assertContentEquals(GzipTestFixtures.longTextExpected(), decoded)
    }

    @Test
    fun decodesLongBinarySample() {
        val decoded = GzipDecoder.decode(GzipTestFixtures.longBinaryGzip)
        assertContentEquals(GzipTestFixtures.longBinaryExpected(), decoded)
    }

    private fun hex(value: String): ByteArray = GzipTestFixtures.hex(value)
}
