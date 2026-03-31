package space.iseki.executables.gzipdecoder

internal object DeflateDecoder {
    private val fixedLiteralLengthTree = HuffmanTree.fromCodeLengths(IntArray(288) { symbol ->
        when (symbol) {
            in 0..143 -> 8
            in 144..255 -> 9
            in 256..279 -> 7
            else -> 8
        }
    })!!

    private val fixedDistanceTree = HuffmanTree.fromCodeLengths(IntArray(32) { 5 })!!

    private val codeLengthAlphabetOrder = intArrayOf(16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15)

    private val lengthBases = intArrayOf(
        3, 4, 5, 6, 7, 8, 9, 10,
        11, 13, 15, 17,
        19, 23, 27, 31,
        35, 43, 51, 59,
        67, 83, 99, 115,
        131, 163, 195, 227,
        258,
    )

    private val lengthExtraBits = intArrayOf(
        0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 1, 1,
        2, 2, 2, 2,
        3, 3, 3, 3,
        4, 4, 4, 4,
        5, 5, 5, 5,
        0,
    )

    private val distanceBases = intArrayOf(
        1, 2, 3, 4, 5, 7, 9, 13, 17, 25,
        33, 49, 65, 97, 129, 193, 257, 385, 513, 769,
        1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577,
    )

    private val distanceExtraBits = intArrayOf(
        0, 0, 0, 0, 1, 1, 2, 2, 3, 3,
        4, 4, 5, 5, 6, 6, 7, 7, 8, 8,
        9, 9, 10, 10, 11, 11, 12, 12, 13, 13,
    )

    fun decode(input: ByteArray): ByteArray {
        var position = 0
        val reader = BitReader(source = { dest ->
            if (position >= input.size) {
                -1
            } else {
                val count = minOf(dest.size, input.size - position)
                input.copyInto(dest, destinationOffset = 0, startIndex = position, endIndex = position + count)
                position += count
                count
            }
        })
        return decode(reader)
    }

    fun decode(reader: BitReader): ByteArray {
        val output = DeflateOutput()
        var finalBlock = false
        while (!finalBlock) {
            finalBlock = reader.readBit() == 1
            when (reader.readBits(2).toInt()) {
                0 -> decodeStoredBlock(reader, output)
                1 -> decodeCompressedBlock(reader, output, fixedLiteralLengthTree, fixedDistanceTree)
                2 -> {
                    val trees = readDynamicTrees(reader)
                    decodeCompressedBlock(reader, output, trees.literalLengthTree, trees.distanceTree)
                }
                else -> throw GzipDecodingException("reserved deflate block type")
            }
        }
        return output.toByteArray()
    }

    private fun decodeStoredBlock(reader: BitReader, output: DeflateOutput) {
        reader.alignToByteBoundary()
        val len = readUnsignedShortLittleEndian(reader)
        val nlen = readUnsignedShortLittleEndian(reader)
        if ((len xor 0xFFFF) != nlen) {
            throw GzipDecodingException("invalid stored block length")
        }
        val buffer = ByteArray(len)
        reader.readAlignedBytes(buffer)
        output.append(buffer)
    }

    private fun decodeCompressedBlock(
        reader: BitReader,
        output: DeflateOutput,
        literalLengthTree: HuffmanTree,
        distanceTree: HuffmanTree?,
    ) {
        while (true) {
            when (val symbol = literalLengthTree.decode(reader)) {
                in 0..255 -> output.append(symbol)
                256 -> return
                in 257..285 -> {
                    val lengthIndex = symbol - 257
                    val length = lengthBases[lengthIndex] + readExtraBits(reader, lengthExtraBits[lengthIndex])
                    val actualDistanceTree = distanceTree ?: throw GzipDecodingException("missing distance tree")
                    val distanceSymbol = actualDistanceTree.decode(reader)
                    if (distanceSymbol !in 0 until distanceBases.size) {
                        throw GzipDecodingException("invalid distance symbol: $distanceSymbol")
                    }
                    val distance = distanceBases[distanceSymbol] + readExtraBits(reader, distanceExtraBits[distanceSymbol])
                    output.copyFromDistance(distance, length)
                }
                else -> throw GzipDecodingException("invalid literal/length symbol: $symbol")
            }
        }
    }

    private fun readDynamicTrees(reader: BitReader): DynamicTrees {
        val hlit = reader.readBits(5).toInt() + 257
        val hdist = reader.readBits(5).toInt() + 1
        val hclen = reader.readBits(4).toInt() + 4

        val codeLengthCodeLengths = IntArray(19)
        repeat(hclen) { index ->
            codeLengthCodeLengths[codeLengthAlphabetOrder[index]] = reader.readBits(3).toInt()
        }
        val codeLengthTree = HuffmanTree.fromCodeLengths(codeLengthCodeLengths)
            ?: throw GzipDecodingException("missing code length tree")

        val combinedCodeLengths = IntArray(hlit + hdist)
        var index = 0
        while (index < combinedCodeLengths.size) {
            when (val symbol = codeLengthTree.decode(reader)) {
                in 0..15 -> combinedCodeLengths[index++] = symbol
                16 -> {
                    if (index == 0) {
                        throw GzipDecodingException("repeat code length with no previous length")
                    }
                    val repeatCount = reader.readBits(2).toInt() + 3
                    val previous = combinedCodeLengths[index - 1]
                    repeat(repeatCount) {
                        if (index >= combinedCodeLengths.size) {
                            throw GzipDecodingException("code length repeat overruns alphabet")
                        }
                        combinedCodeLengths[index++] = previous
                    }
                }
                17 -> {
                    val repeatCount = reader.readBits(3).toInt() + 3
                    repeat(repeatCount) {
                        if (index >= combinedCodeLengths.size) {
                            throw GzipDecodingException("zero code length repeat overruns alphabet")
                        }
                        combinedCodeLengths[index++] = 0
                    }
                }
                18 -> {
                    val repeatCount = reader.readBits(7).toInt() + 11
                    repeat(repeatCount) {
                        if (index >= combinedCodeLengths.size) {
                            throw GzipDecodingException("long zero code length repeat overruns alphabet")
                        }
                        combinedCodeLengths[index++] = 0
                    }
                }
                else -> throw GzipDecodingException("invalid code length symbol: $symbol")
            }
        }

        val literalLengthTree = HuffmanTree.fromCodeLengths(combinedCodeLengths.copyOfRange(0, hlit))
            ?: throw GzipDecodingException("missing literal/length tree")
        val distanceTree = HuffmanTree.fromCodeLengths(
            combinedCodeLengths.copyOfRange(hlit, hlit + hdist),
            allowEmpty = true,
        )
        return DynamicTrees(literalLengthTree, distanceTree)
    }

    private fun readUnsignedShortLittleEndian(reader: BitReader): Int {
        val low = reader.readAlignedByte()
        val high = reader.readAlignedByte()
        return low or (high shl 8)
    }

    private fun readExtraBits(reader: BitReader, count: Int): Int = if (count == 0) 0 else reader.readBits(count).toInt()

    private data class DynamicTrees(
        val literalLengthTree: HuffmanTree,
        val distanceTree: HuffmanTree?,
    )
}

