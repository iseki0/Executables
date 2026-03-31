package space.iseki.executables.gzipdecoder

internal object InternalGzipDecoder {
    private const val ID1 = 0x1f
    private const val ID2 = 0x8b
    private const val METHOD_DEFLATE = 8

    private const val FHCRC = 0x02
    private const val FEXTRA = 0x04
    private const val FNAME = 0x08
    private const val FCOMMENT = 0x10
    private const val RESERVED_FLAGS_MASK = 0xE0

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

        val output = DeflateOutput(input.size.coerceAtLeast(1))
        var sawMember = false
        while (true) {
            val firstByte = reader.readAlignedByteOrNull() ?: break
            sawMember = true
            if (firstByte != ID1) {
                throw GzipDecodingException("invalid gzip magic")
            }
            decodeMember(reader, output)
        }

        if (!sawMember) {
            throw GzipDecodingException("empty gzip input")
        }
        return output.toByteArray()
    }

    private fun decodeMember(reader: BitReader, output: DeflateOutput) {
        val headerCrc = CRC32()
        headerCrc.update(ID1)

        val id2 = readHeaderByte(reader, headerCrc)
        if (id2 != ID2) {
            throw GzipDecodingException("invalid gzip magic")
        }

        val compressionMethod = readHeaderByte(reader, headerCrc)
        if (compressionMethod != METHOD_DEFLATE) {
            throw GzipDecodingException("unsupported compression method: $compressionMethod")
        }

        val flags = readHeaderByte(reader, headerCrc)
        if ((flags and RESERVED_FLAGS_MASK) != 0) {
            throw GzipDecodingException("invalid gzip flags: $flags")
        }

        repeat(4) { readHeaderByte(reader, headerCrc) }
        readHeaderByte(reader, headerCrc)
        readHeaderByte(reader, headerCrc)

        if ((flags and FEXTRA) != 0) {
            val xlenLow = readHeaderByte(reader, headerCrc)
            val xlenHigh = readHeaderByte(reader, headerCrc)
            val xlen = xlenLow or (xlenHigh shl 8)
            repeat(xlen) { readHeaderByte(reader, headerCrc) }
        }
        if ((flags and FNAME) != 0) {
            readZeroTerminatedField(reader, headerCrc)
        }
        if ((flags and FCOMMENT) != 0) {
            readZeroTerminatedField(reader, headerCrc)
        }
        if ((flags and FHCRC) != 0) {
            val expectedHeaderCrc = readUnsignedShortLittleEndian(reader)
            val actualHeaderCrc = (headerCrc.value() and 0xFFFFu).toInt()
            if (expectedHeaderCrc != actualHeaderCrc) {
                throw GzipDecodingException("gzip header crc mismatch")
            }
        }

        val memberData = DeflateDecoder.decode(reader)
        output.append(memberData)

        reader.alignToByteBoundary()
        val expectedDataCrc = readUnsignedIntLittleEndian(reader)
        val expectedSize = readUnsignedIntLittleEndian(reader)

        val actualDataCrc = CRC32().apply { update(memberData) }.value()
        if (expectedDataCrc != actualDataCrc) {
            throw GzipDecodingException("gzip crc32 mismatch")
        }
        if (expectedSize != memberData.size.toUInt()) {
            throw GzipDecodingException("gzip isize mismatch")
        }
    }

    private fun readHeaderByte(reader: BitReader, headerCrc: CRC32): Int {
        val value = reader.readAlignedByte()
        headerCrc.update(value)
        return value
    }

    private fun readZeroTerminatedField(reader: BitReader, headerCrc: CRC32) {
        while (true) {
            val value = readHeaderByte(reader, headerCrc)
            if (value == 0) return
        }
    }

    private fun readUnsignedShortLittleEndian(reader: BitReader): Int {
        val low = reader.readAlignedByte()
        val high = reader.readAlignedByte()
        return low or (high shl 8)
    }

    private fun readUnsignedIntLittleEndian(reader: BitReader): UInt {
        val b0 = reader.readAlignedByte().toUInt()
        val b1 = reader.readAlignedByte().toUInt()
        val b2 = reader.readAlignedByte().toUInt()
        val b3 = reader.readAlignedByte().toUInt()
        return b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
    }
}
