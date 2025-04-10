package space.iseki.executables.macho

import space.iseki.executables.common.Address32
import space.iseki.executables.share.u4
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class)
interface MachoLoadCommand {
    val size: UInt
    val type: MachoLoadCommandType

    /**
     * Specifies the 128-bit UUID for an image or its corresponding dSYM file.
     */
    data class Uuid internal constructor(private val value: kotlin.uuid.Uuid) : MachoLoadCommand {
        override val size: UInt
            get() = 24u
        override val type: MachoLoadCommandType
            get() = MachoLoadCommandType.LC_UUID

        fun kotlinUuid(): kotlin.uuid.Uuid = value

        companion object {
            internal fun parse(buf: ByteArray, off: Int, le: Boolean): Uuid =
                Uuid(kotlin.uuid.Uuid.fromByteArray(buf.sliceArray(off + 8 until off + 24)))
        }
    }

    /**
     * Specifies the range of bytes in a 32-bit Mach-O file that make up a segment.
     *
     * Those bytes are mapped by the loader into the address space of a program. Declared in /usr/include/mach-o/loader.h. See also segment_command_64.
     *
     */
    data class Segment internal constructor(
        val segName: String,
        val vmAddr: Address32,
        val vmSize: UInt,
        val fileOff: UInt,
        val fileSize: UInt,
        val maxProt: MachoVMProt,
        val initProt: MachoVMProt,
        val nsects: UInt,
        val flags: MachoSegmentFlag,
    ) : MachoLoadCommand {
        override val size: UInt
            get() = 32u + nsects * 12u
        override val type: MachoLoadCommandType
            get() = MachoLoadCommandType.LC_SEGMENT

        companion object {
            internal fun parse(buf: ByteArray, off: Int, le: Boolean): Segment {
                val segName = buf.decodeToString(off + 8, off + 16).trimEnd('\u0000')
                val vmAddr = Address32(buf.u4(off + 16, le))
                val vmSize = buf.u4(off + 20, le)
                val fileOff = buf.u4(off + 24, le)
                val fileSize = buf.u4(off + 28, le)
                val maxProt = MachoVMProt(buf.u4(off + 32, le))
                val initProt = MachoVMProt(buf.u4(off + 36, le))
                val nsects = buf.u4(off + 40, le)
                val flags = MachoSegmentFlag(buf.u4(off + 44, le))

                return Segment(segName, vmAddr, vmSize, fileOff, fileSize, maxProt, initProt, nsects, flags)
            }
        }
    }

}
