package space.iseki.executables.gzipdecoder

import kotlin.test.Test
import kotlin.test.assertContentEquals

class GzipDecoderJvmParityTest {
    @Test
    fun facadeMatchesInternalImplementationForSuccessCases() {
        val inputs = listOf(
            "1f8b08080000000002ff73616d706c652e74787400cb48cdc9c957c840905c003b7c8adf12000000",
            "1f8b081e15cd5b0700030400414243447370656c6c2e747874006d616769630005604b4c2a4a4c4e4c4904520a89d8d90083de94f923000000",
            "1f8b08000000000000ff010500faff48454c4c4f366444c1050000001f8b08000000000000ffcb48cdc9c957c840905c003b7c8adf12000000",
        )

        for (hex in inputs) {
            val bytes = hex(hex)
            assertContentEquals(InternalGzipDecoder.decode(bytes), GzipDecoder.decode(bytes))
        }

        assertContentEquals(InternalGzipDecoder.decode(GzipTestFixtures.longTextGzip), GzipDecoder.decode(GzipTestFixtures.longTextGzip))
        assertContentEquals(InternalGzipDecoder.decode(GzipTestFixtures.longBinaryGzip), GzipDecoder.decode(GzipTestFixtures.longBinaryGzip))
    }

    private fun hex(value: String): ByteArray = GzipTestFixtures.hex(value)
}
