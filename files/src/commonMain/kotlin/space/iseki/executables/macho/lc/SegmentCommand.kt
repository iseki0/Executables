package space.iseki.executables.macho.lc

import space.iseki.executables.common.Address32
import space.iseki.executables.macho.MachoLoadCommandType
import space.iseki.executables.macho.MachoSegmentFlag
import space.iseki.executables.macho.MachoVMProt
import space.iseki.executables.share.toUnmodifiableList
import space.iseki.executables.share.u4

/**
 * Represents a LC_SEGMENT load command in a 32-bit Mach-O file.
 *
 * Defines a segment that maps a range of file bytes into the address space
 * of the process at runtime, with optional section structures following it.
 */
data class SegmentCommand internal constructor(
    /**
     * Segment name (fixed 16-byte ASCII string, null-padded).
     */
    val segName: String,

    /**
     * Starting virtual memory address where the segment will be mapped.
     */
    val vmAddr: Address32,

    /**
     * Size of the segment in virtual memory.
     */
    val vmSize: UInt,

    /**
     * File offset where the segment’s data starts.
     */
    val fileOff: UInt,

    /**
     * Size of the segment’s data in the file.
     */
    val fileSize: UInt,

    /**
     * Maximum allowed memory protections for this segment.
     */
    val maxProt: MachoVMProt,

    /**
     * Initial memory protections when loaded.
     */
    val initProt: MachoVMProt,

    /**
     * Number of `section` structures that immediately follow this command.
     */
    val nsects: UInt,

    /**
     * Segment-level flags (e.g., SG_HIGHVM, SG_NORELOC).
     */
    val flags: MachoSegmentFlag,

    /**
     * Actual size of this load command as declared in the file.
     */
    override val size: UInt,

    /**
     * Always returns [MachoLoadCommandType.LC_SEGMENT].
     */
    override val type: MachoLoadCommandType = MachoLoadCommandType.LC_SEGMENT,

    val sections: List<Section>,
) : MachoLoadCommand {

    companion object {
        private const val BASE_SIZE = 56 // sizeof(segment_command)
        private const val SECTION_SIZE = 68

        /**
         * Parses a 32-bit LC_SEGMENT command from the given buffer.
         *
         * @param buf ByteArray of the full Mach-O file.
         * @param off Offset to the beginning of this load command.
         * @param le Whether the file is little-endian.
         * @throws InvalidLoaderCommandException if the structure is malformed.
         */
        internal fun parse(buf: ByteArray, off: Int, le: Boolean): SegmentCommand {
            val cmdsize = buf.u4(off + 4, le)
            val nsects = buf.u4(off + 48, le)
            val expectedSize = BASE_SIZE + nsects.toInt() * SECTION_SIZE
            if (cmdsize.toInt() < expectedSize) {
                throw InvalidLoaderCommandException(
                    cmd = MachoLoadCommandType(buf.u4(off, le)),
                    message = "Invalid segment_command: cmdsize ($cmdsize) < expected ($expectedSize)"
                )
            }

            val segName = buf.decodeToString(off + 8, off + 24).trimEnd('\u0000')
            val vmAddr = Address32(buf.u4(off + 24, le))
            val vmSize = buf.u4(off + 28, le)
            val fileOff = buf.u4(off + 32, le)
            val fileSize = buf.u4(off + 36, le)
            val maxProt = MachoVMProt(buf.u4(off + 40, le))
            val initProt = MachoVMProt(buf.u4(off + 44, le))
            val flags = MachoSegmentFlag(buf.u4(off + 52, le))
            val sections = Array(nsects.toInt()) { i ->
                Section.parse(buf, off + SECTION_SIZE * i + BASE_SIZE, le)
            }
            return SegmentCommand(
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
 * Represents a 32-bit section structure within a Mach-O segment.
 *
 * Corresponds to the native C structure `struct section`.
 */
data class Section internal constructor(
    /**
     * The name of the section (fixed-length 16-byte ASCII string).
     * Usually begins with `__`, e.g., `__text`, `__data`.
     */
    val sectName: String,

    /**
     * The name of the segment that this section belongs to.
     */
    val segName: String,

    /**
     * The virtual memory address where this section begins.
     */
    val addr: Address32,

    /**
     * The number of bytes of virtual memory occupied by this section.
     */
    val size: UInt,

    /**
     * The file offset to the start of this section.
     */
    val offset: UInt,

    /**
     * The section's alignment as a power of two.
     * For example, a value of 3 means 8-byte alignment (2^3 = 8).
     */
    val align: UInt,

    /**
     * The file offset of the first relocation entry for this section.
     */
    val reloff: UInt,

    /**
     * The number of relocation entries at [reloff].
     */
    val nreloc: UInt,

    /**
     * Section type and attributes.
     * Lower 8 bits = section type, upper 24 bits = section attributes.
     */
    val flags: MachoSectionFlag,

    /**
     * Reserved field for use by the section type.
     */
    val reserved1: UInt,

    /**
     * Reserved field for use by the section type.
     */
    val reserved2: UInt,
) {
    companion object {
        /**
         * Parses a [Section] from the given ByteArray at [off].
         *
         * @param buf The binary content of the Mach-O file.
         * @param off Offset into [buf] where this section starts.
         * @param le Whether to interpret fields as little-endian.
         * @return A parsed [Section] instance.
         */
        internal fun parse(buf: ByteArray, off: Int, le: Boolean): Section {
            val sectName = buf.decodeToString(off, off + 16).trimEnd('\u0000')
            val segName = buf.decodeToString(off + 16, off + 32).trimEnd('\u0000')
            val addr = buf.u4(off + 32, le)
            val size = buf.u4(off + 36, le)
            val offsetVal = buf.u4(off + 40, le)
            val align = buf.u4(off + 44, le)
            val reloff = buf.u4(off + 48, le)
            val nreloc = buf.u4(off + 52, le)
            val flags = MachoSectionFlag(buf.u4(off + 56, le))
            val reserved1 = buf.u4(off + 60, le)
            val reserved2 = buf.u4(off + 64, le)

            return Section(
                sectName = sectName,
                segName = segName,
                addr = Address32(addr),
                size = size,
                offset = offsetVal,
                align = align,
                reloff = reloff,
                nreloc = nreloc,
                flags = flags,
                reserved1 = reserved1,
                reserved2 = reserved2,
            )
        }
    }
}

