package space.iseki.executables.macho

import kotlinx.serialization.Serializable
import space.iseki.executables.share.u4b
import space.iseki.executables.share.u4l

/**
 * Represents a 32-bit Mach-O header.
 */
@Serializable
data class Macho32Header internal constructor(
    override val magic: UInt,
    override val cputype: UInt,
    override val cpusubtype: UInt,
    override val filetype: MachoFileType,
    override val ncmds: UInt,
    override val sizeofcmds: UInt,
    override val flags: MachoFlags,
    override val isLittleEndian: Boolean,
) : MachoHeader {

    companion object {
        /**
         * Parses a 32-bit Mach-O header from a byte array.
         *
         * @param bytes The byte array containing the Mach-O header
         * @param offset The offset within the byte array at which the header starts
         * @param littleEndian Whether the file is little-endian
         * @return A [Macho32Header] instance
         */
        fun parse(bytes: ByteArray, offset: Int, littleEndian: Boolean): Macho32Header {
            val readUInt = if (littleEndian) { pos: Int -> bytes.u4l(pos) } else { pos: Int -> bytes.u4b(pos) }
            return Macho32Header(
                magic = readUInt(offset),
                cputype = readUInt(offset + 4),
                cpusubtype = readUInt(offset + 8),
                filetype = MachoFileType(readUInt(offset + 12)),
                ncmds = readUInt(offset + 16),
                sizeofcmds = readUInt(offset + 20),
                flags = MachoFlags(readUInt(offset + 24)),
                isLittleEndian = littleEndian
            )
        }
    }
}
