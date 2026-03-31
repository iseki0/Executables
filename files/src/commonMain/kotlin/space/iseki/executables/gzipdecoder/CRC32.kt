package space.iseki.executables.gzipdecoder

internal class CRC32 {
    private var value = 0xFFFF_FFFFu

    fun update(byte: Int) {
        var crc = value xor (byte and 0xFF).toUInt()
        repeat(8) {
            crc = if ((crc and 1u) != 0u) {
                (crc shr 1) xor POLYNOMIAL
            } else {
                crc shr 1
            }
        }
        value = crc
    }

    fun update(bytes: ByteArray, offset: Int = 0, length: Int = bytes.size - offset) {
        require(offset >= 0) { "offset must be >= 0, got $offset" }
        require(length >= 0) { "length must be >= 0, got $length" }
        require(offset + length <= bytes.size) { "offset + length exceeds source size" }
        for (i in offset until offset + length) {
            update(bytes[i].toInt())
        }
    }

    fun value(): UInt = value xor 0xFFFF_FFFFu

    private companion object {
        const val POLYNOMIAL = 0xEDB8_8320u
    }
}

