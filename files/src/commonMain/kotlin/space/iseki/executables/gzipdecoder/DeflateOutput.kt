package space.iseki.executables.gzipdecoder

internal class DeflateOutput(initialCapacity: Int = 256) {
    private var buffer = ByteArray(initialCapacity)
    private var size0 = 0

    val size: Int
        get() = size0

    fun append(byte: Int) {
        ensureCapacity(size0 + 1)
        buffer[size0++] = byte.toByte()
    }

    fun append(bytes: ByteArray, offset: Int = 0, length: Int = bytes.size - offset) {
        require(offset >= 0) { "offset must be >= 0, got $offset" }
        require(length >= 0) { "length must be >= 0, got $length" }
        require(offset + length <= bytes.size) { "offset + length exceeds source size" }
        ensureCapacity(size0 + length)
        bytes.copyInto(buffer, destinationOffset = size0, startIndex = offset, endIndex = offset + length)
        size0 += length
    }

    fun copyFromDistance(distance: Int, length: Int) {
        if (distance <= 0) {
            throw GzipDecodingException("invalid backward distance: $distance")
        }
        if (distance > size0) {
            throw GzipDecodingException("invalid backward distance: $distance")
        }
        ensureCapacity(size0 + length)
        repeat(length) {
            buffer[size0] = buffer[size0 - distance]
            size0++
        }
    }

    fun toByteArray(): ByteArray = buffer.copyOf(size0)

    private fun ensureCapacity(required: Int) {
        if (required <= buffer.size) return
        var newSize = buffer.size.coerceAtLeast(1)
        while (newSize < required) {
            newSize *= 2
        }
        buffer = buffer.copyOf(newSize)
    }
}

