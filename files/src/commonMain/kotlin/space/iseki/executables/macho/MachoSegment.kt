@file:JvmName("-MachoSegment")

package space.iseki.executables.macho

import space.iseki.executables.common.Address64
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.common.toAddr
import space.iseki.executables.macho.lc.MachoSectionFlag
import space.iseki.executables.macho.lc.Section
import space.iseki.executables.macho.lc.Section64
import space.iseki.executables.macho.lc.SegmentCommand
import space.iseki.executables.macho.lc.SegmentCommand64
import space.iseki.executables.share.toUnmodifiableList
import kotlin.jvm.JvmName

/**
 * Represents a segment(common of 32 and 64) in a Mach-O file.
 */
data class MachoSegment internal constructor(
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

    val sections: List<MachoSection> = emptyList(),
) : ReadableStructure {
    override val fields: Map<String, Any>
        get() = mapOf(
            "segName" to segName,
            "vmAddr" to vmAddr,
            "vmSize" to vmSize,
            "fileOff" to fileOff,
            "fileSize" to fileSize,
            "maxProt" to maxProt,
            "initProt" to initProt,
            "nsects" to nsects,
            "flags" to flags,
            "sections" to sections,
        )

}

internal fun MachoSegment(it: SegmentCommand) = MachoSegment(
    segName = it.segName,
    vmAddr = it.vmAddr.value.toULong().toAddr(),
    vmSize = it.vmSize.toULong(),
    fileOff = it.fileOff.toULong(),
    fileSize = it.fileSize.toULong(),
    maxProt = it.maxProt,
    initProt = it.initProt,
    nsects = it.nsects,
    flags = it.flags,
    sections = it.sections.map(::MachoSection).toUnmodifiableList(),
)

internal fun MachoSegment(it: SegmentCommand64) = MachoSegment(
    segName = it.segName,
    vmAddr = it.vmAddr,
    vmSize = it.vmSize,
    fileOff = it.fileOff,
    fileSize = it.fileSize,
    maxProt = it.maxProt,
    initProt = it.initProt,
    nsects = it.nsects,
    flags = it.flags,
    sections = it.sections.map(::MachoSection).toUnmodifiableList(),
)

/**
 * Represents a section (common of 32 and 64) in a Mach-O file.
 */
data class MachoSection internal constructor(
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
) : ReadableStructure {
    override val fields: Map<String, Any>
        get() = mapOf(
            "sectName" to sectName,
            "segName" to segName,
            "addr" to addr,
            "size" to size,
            "offset" to offset,
            "align" to align,
            "reloff" to reloff,
            "nreloc" to nreloc,
            "flags" to flags,
        )
}

internal fun MachoSection(it: Section) = MachoSection(
    sectName = it.sectName,
    segName = it.segName,
    addr = it.addr.value.toULong().toAddr(),
    size = it.size.toULong(),
    offset = it.offset,
    align = it.align,
    reloff = it.reloff,
    nreloc = it.nreloc,
    flags = it.flags,
)

internal fun MachoSection(it: Section64) = MachoSection(
    sectName = it.sectName,
    segName = it.segName,
    addr = it.addr,
    size = it.size,
    offset = it.offset,
    align = it.align,
    reloff = it.reloff,
    nreloc = it.nreloc,
    flags = it.flags,
)

