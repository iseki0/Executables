package space.iseki.executables.macho.lc

import space.iseki.executables.common.Address64
import space.iseki.executables.macho.MachoLoadCommandType
import space.iseki.executables.macho.MachoSegmentFlag
import space.iseki.executables.macho.MachoVMProt
import space.iseki.executables.share.toUnmodifiableList
import space.iseki.executables.share.u4
import space.iseki.executables.share.u8

/**
 * Represents a LC_SEGMENT_64 load command in a 64-bit Mach-O file.
 *
 * Defines a segment that maps part of the file into virtual memory at runtime.
 * May be followed by `nsects` number of section_64 structures.
 */
data class SegmentCommand64 internal constructor(
    /**
     * Segment name (null-padded 16-byte ASCII string).
     */
    val segName: String,

    /**
     * Starting virtual memory address of the segment.
     */
    val vmAddr: Address64,

    /**
     * Size of segment in virtual memory.
     */
    val vmSize: ULong,

    /**
     * File offset to the segment’s data.
     */
    val fileOff: ULong,

    /**
     * Number of bytes occupied by the segment on disk.
     */
    val fileSize: ULong,

    /**
     * Maximum virtual memory protections allowed.
     */
    val maxProt: MachoVMProt,

    /**
     * Initial virtual memory protections at load time.
     */
    val initProt: MachoVMProt,

    /**
     * Number of `section_64` structures following this command.
     */
    val nsects: UInt,

    /**
     * Segment-level flags (SG_HIGHVM, SG_NORELOC, etc).
     */
    val flags: MachoSegmentFlag,

    /**
     * Actual size of the load command as read from file.
     */
    override val size: UInt,

    /**
     * Always [MachoLoadCommandType.LC_SEGMENT_64].
     */
    override val type: MachoLoadCommandType = MachoLoadCommandType.LC_SEGMENT_64,
    val sections: List<Section64>,
) : MachoLoadCommand {

    companion object {
        private const val BASE_SIZE = 72     // sizeof(segment_command_64)
        private const val SECTION_SIZE = 80  // sizeof(section_64)

        /**
         * Parses a 64-bit LC_SEGMENT_64 command from the given buffer.
         *
         * @param buf The full Mach-O file content.
         * @param off Offset to the beginning of this command.
         * @param le Whether the file is little-endian.
         * @throws InvalidLoaderCommandException if command is malformed.
         */
        internal fun parse(buf: ByteArray, off: Int, le: Boolean): SegmentCommand64 {
            val cmdsize = buf.u4(off + 4, le)
            val nsects = buf.u4(off + 64, le)
            val expectedSize = BASE_SIZE + nsects.toInt() * SECTION_SIZE
            if (cmdsize.toInt() < expectedSize) {
                throw InvalidLoaderCommandException(
                    cmd = MachoLoadCommandType(buf.u4(off, le)),
                    message = "Invalid segment_command_64: cmdsize ($cmdsize) < expected ($expectedSize)"
                )
            }

            val segName = buf.decodeToString(off + 8, off + 24).trimEnd('\u0000')
            val vmAddr = Address64(buf.u8(off + 24, le))
            val vmSize = buf.u8(off + 32, le)
            val fileOff = buf.u8(off + 40, le)
            val fileSize = buf.u8(off + 48, le)
            val maxProt = MachoVMProt(buf.u4(off + 56, le))
            val initProt = MachoVMProt(buf.u4(off + 60, le))
            val flags = MachoSegmentFlag(buf.u4(off + 68, le))

            val sections = Array(nsects.toInt()) { i ->
                Section64.parse(buf, off + BASE_SIZE + i * SECTION_SIZE, le)
            }
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
                size = cmdsize,
                sections = sections.toUnmodifiableList(),
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


