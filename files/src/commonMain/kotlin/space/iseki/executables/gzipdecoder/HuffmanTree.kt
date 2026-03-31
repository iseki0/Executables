package space.iseki.executables.gzipdecoder

internal class HuffmanTree private constructor(
    private val tables: List<Map<Int, Int>>,
) {
    fun decode(reader: BitReader): Int {
        var code = 0
        for (bitLength in 1..tables.size) {
            code = code or (reader.readBit() shl (bitLength - 1))
            val symbol = tables[bitLength - 1][code]
            if (symbol != null) {
                return symbol
            }
        }
        throw GzipDecodingException("invalid huffman code")
    }

    companion object {
        fun fromCodeLengths(codeLengths: IntArray, allowEmpty: Boolean = false): HuffmanTree? {
            val maxBits = codeLengths.maxOrNull() ?: 0
            if (maxBits == 0) {
                if (allowEmpty) return null
                throw GzipDecodingException("empty huffman tree")
            }

            val counts = IntArray(maxBits + 1)
            for (length in codeLengths) {
                if (length < 0) {
                    throw GzipDecodingException("negative huffman code length")
                }
                if (length > maxBits) {
                    throw GzipDecodingException("invalid huffman code length: $length")
                }
                if (length != 0) counts[length]++
            }

            var code = 0
            val nextCode = IntArray(maxBits + 1)
            for (bits in 1..maxBits) {
                code = (code + counts[bits - 1]) shl 1
                nextCode[bits] = code
            }

            val tables = List(maxBits) { mutableMapOf<Int, Int>() }
            for (symbol in codeLengths.indices) {
                val length = codeLengths[symbol]
                if (length == 0) continue
                val assignedCode = nextCode[length]++
                val reversedCode = reverseBits(assignedCode, length)
                val previous = tables[length - 1].put(reversedCode, symbol)
                if (previous != null) {
                    throw GzipDecodingException("duplicate huffman code")
                }
            }

            val availableCodes = 1 shl maxBits
            var usedSpace = 0
            for (bits in 1..maxBits) {
                usedSpace += counts[bits] shl (maxBits - bits)
            }
            if (usedSpace > availableCodes) {
                throw GzipDecodingException("oversubscribed huffman tree")
            }
            return HuffmanTree(tables)
        }

        private fun reverseBits(code: Int, bitLength: Int): Int {
            var result = 0
            var value = code
            repeat(bitLength) {
                result = (result shl 1) or (value and 1)
                value = value shr 1
            }
            return result
        }
    }
}
