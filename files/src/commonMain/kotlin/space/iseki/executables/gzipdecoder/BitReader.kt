package space.iseki.executables.gzipdecoder

import space.iseki.executables.common.EOFException
import space.iseki.executables.common.IOException

internal class BitReader(
    private val source: (ByteArray) -> Int,
    inputBufferSize: Int = DEFAULT_INPUT_BUFFER_SIZE,
) {
    private val inputBuffer = ByteArray(inputBufferSize)
    private var inputPosition = 0
    private var inputLimit = 0

    private var bitBuffer = 0UL
    private var bufferedBitCount = 0

    var totalBitsRead: Long = 0
        private set

    fun readBit(): Int = readBits(1).toInt()

    fun readBits(bitCount: Int): UInt {
        require(bitCount in 0..32) { "bitCount must be in 0..32, got $bitCount" }
        if (bitCount == 0) return 0u
        ensureBits(bitCount)
        val mask = if (bitCount == 32) 0xFFFF_FFFFUL else (1UL shl bitCount) - 1UL
        val value = (bitBuffer and mask).toUInt()
        bitBuffer = bitBuffer shr bitCount
        bufferedBitCount -= bitCount
        totalBitsRead += bitCount
        return value
    }

    fun alignToByteBoundary() {
        val skippedBits = bufferedBitCount and 7
        if (skippedBits == 0) return
        bitBuffer = bitBuffer shr skippedBits
        bufferedBitCount -= skippedBits
        totalBitsRead += skippedBits
    }

    fun readUnsignedByte(): Int = readBits(8).toInt()

    fun readAlignedByte(): Int = readAlignedByteOrNull()
        ?: throw EOFException("Unexpected EOF while reading aligned byte")

    fun readAlignedByteOrNull(): Int? {
        alignToByteBoundary()
        val next = readRawByte()
        if (next < 0) {
            return null
        }
        totalBitsRead += 8
        return next
    }

    fun readAlignedBytes(dest: ByteArray, offset: Int = 0, length: Int = dest.size - offset) {
        require(offset >= 0) { "offset must be >= 0, got $offset" }
        require(length >= 0) { "length must be >= 0, got $length" }
        require(offset + length <= dest.size) { "offset + length exceeds destination size" }

        var writeIndex = offset
        val writeEnd = offset + length

        while (writeIndex < writeEnd) {
            dest[writeIndex++] = readAlignedByte().toByte()
        }
    }

    private fun ensureBits(bitCount: Int) {
        while (bufferedBitCount < bitCount) {
            val nextByte = readRawByte()
            if (nextByte < 0) {
                throw EOFException("Unexpected EOF while reading $bitCount bits")
            }
            bitBuffer = bitBuffer or (nextByte.toULong() shl bufferedBitCount)
            bufferedBitCount += 8
        }
    }

    private fun readRawByte(): Int {
        while (true) {
            if (inputPosition < inputLimit) {
                return inputBuffer[inputPosition++].toInt() and 0xFF
            }
            inputPosition = 0
            inputLimit = source(inputBuffer)
            if (inputLimit == -1) {
                return -1
            }
            if (inputLimit <= 0 || inputLimit > inputBuffer.size) {
                throw IOException("Invalid source read result: $inputLimit")
            }
        }
    }

    private companion object {
        const val DEFAULT_INPUT_BUFFER_SIZE = 512
    }
}
