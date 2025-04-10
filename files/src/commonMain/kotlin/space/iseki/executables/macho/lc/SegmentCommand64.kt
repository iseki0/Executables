package space.iseki.executables.macho.lc

import space.iseki.executables.common.Address64
import space.iseki.executables.macho.MachoLoadCommandType
import space.iseki.executables.macho.MachoSegmentFlag
import space.iseki.executables.macho.MachoVMProt
import space.iseki.executables.share.u4
import space.iseki.executables.share.u8

/**
 * Represents a 64-bit segment load command (LC_SEGMENT_64) in a Mach-O binary.
 *
 * Corresponds to the native C structure `segment_command_64`.
 */
data class SegmentCommand64 internal constructor(
    /**
     * A C string specifying the name of the segment.
     * This is a fixed-length 16-byte ASCII string, possibly null-padded.
     */
    val segName: String,

    /**
     * The starting virtual memory address of this segment.
     */
    val vmAddr: Address64,

    /**
     * The number of bytes of virtual memory occupied by this segment.
     * Can be larger than [fileSize] if the segment is partially zero-filled.
     */
    val vmSize: ULong,

    /**
     * The offset in the file of the data to be mapped at [vmAddr].
     */
    val fileOff: ULong,

    /**
     * The number of bytes this segment occupies on disk.
     */
    val fileSize: ULong,

    /**
     * Maximum permitted virtual memory protections of this segment.
     */
    val maxProt: MachoVMProt,

    /**
     * Initial virtual memory protections of this segment.
     */
    val initProt: MachoVMProt,

    /**
     * Number of section_64 structures following this segment.
     */
    val nsects: UInt,

    /**
     * Flags that affect segment loading (e.g., SG_HIGHVM, SG_NORELOC).
     */
    val flags: MachoSegmentFlag,
) : MachoLoadCommand {

    /**
     * The size in bytes of this load command, including section_64 structures.
     * Calculated as 72 + nsects * 80 bytes.
     */
    override val size: UInt
        get() = 72u + nsects * 80u

    /**
     * The type of this load command (LC_SEGMENT_64).
     */
    override val type: MachoLoadCommandType
        get() = MachoLoadCommandType.LC_SEGMENT_64

    companion object {
        /**
         * Parses a Segment64 from the given ByteArray at [off].
         *
         * @param buf The binary Mach-O content.
         * @param off Offset in [buf] to start reading.
         * @param le Whether the byte order is little-endian.
         */
        internal fun parse(buf: ByteArray, off: Int, le: Boolean): SegmentCommand64 {
            val segName = buf.decodeToString(off + 8, off + 24).trimEnd('\u0000')
            val vmAddr = Address64(buf.u8(off + 24, le))
            val vmSize = buf.u8(off + 32, le)
            val fileOff = buf.u8(off + 40, le)
            val fileSize = buf.u8(off + 48, le)
            val maxProt = MachoVMProt(buf.u4(off + 56, le))
            val initProt = MachoVMProt(buf.u4(off + 60, le))
            val nsects = buf.u4(off + 64, le)
            val flags = MachoSegmentFlag(buf.u4(off + 68, le))

            return SegmentCommand64(
                segName = segName,
                vmAddr = vmAddr,
                vmSize = vmSize,
                fileOff = fileOff,
                fileSize = fileSize,
                maxProt = maxProt,
                initProt = initProt,
                nsects = nsects,
                flags = flags,
            )
        }
    }
}

/**
 * Represents a 64-bit section structure within a Mach-O segment.
 *
 * Corresponds to the native C structure `struct section_64`.
 */
data class Section64 internal constructor(
    /**
     * The name of the section (fixed-length 16-byte ASCII string).
     * Usually begins with `__`, such as `__text`, `__data`.
     */
    val sectName: String,

    /**
     * The name of the segment this section belongs to.
     */
    val segName: String,

    /**
     * The virtual memory address where this section begins.
     */
    val addr: Address64,

    /**
     * The size in bytes of virtual memory occupied by this section.
     */
    val size: ULong,

    /**
     * The offset in the file where this section's data begins.
     */
    val offset: UInt,

    /**
     * Byte alignment of this section, specified as a power of two.
     * For example, value `3` means 8-byte alignment (2^3).
     */
    val align: UInt,

    /**
     * File offset of the first relocation entry for this section.
     */
    val reloff: UInt,

    /**
     * Number of relocation entries for this section.
     */
    val nreloc: UInt,

    /**
     * Type and attributes of this section.
     * Lower 8 bits = section type, upper 24 bits = flags.
     */
    val flags: MachoSectionFlag,

    /**
     * Reserved field, usage depends on section type.
     */
    val reserved1: UInt,

    /**
     * Reserved field, usage depends on section type.
     */
    val reserved2: UInt,

    /**
     * Reserved field for 64-bit only (e.g., used in `__thread_vars`, etc.).
     */
    val reserved3: UInt,
) {
    companion object {
        /**
         * Parses a [Section64] from the given ByteArray at [off].
         *
         * @param buf The binary content of the Mach-O file.
         * @param off Offset in [buf] where the section_64 starts.
         * @param le Whether the data is little-endian.
         * @return A parsed [Section64] instance.
         */
        internal fun parse(buf: ByteArray, off: Int, le: Boolean): Section64 {
            val sectName = buf.decodeToString(off, off + 16).trimEnd('\u0000')
            val segName = buf.decodeToString(off + 16, off + 32).trimEnd('\u0000')
            val addr = buf.u8(off + 32, le)
            val size = buf.u8(off + 40, le)
            val offsetVal = buf.u4(off + 48, le)
            val align = buf.u4(off + 52, le)
            val reloff = buf.u4(off + 56, le)
            val nreloc = buf.u4(off + 60, le)
            val flags = MachoSectionFlag(buf.u4(off + 64, le))
            val reserved1 = buf.u4(off + 68, le)
            val reserved2 = buf.u4(off + 72, le)
            val reserved3 = buf.u4(off + 76, le)

            return Section64(
                sectName = sectName,
                segName = segName,
                addr = Address64(addr),
                size = size,
                offset = offsetVal,
                align = align,
                reloff = reloff,
                nreloc = nreloc,
                flags = flags,
                reserved1 = reserved1,
                reserved2 = reserved2,
                reserved3 = reserved3,
            )
        }
    }
}


