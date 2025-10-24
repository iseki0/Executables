package space.iseki.executables.macho

import space.iseki.executables.common.DataAccessor
import space.iseki.executables.common.EOFException
import space.iseki.executables.common.FileFormat
import space.iseki.executables.common.IOException
import space.iseki.executables.common.OpenedFile
import space.iseki.executables.common.ReadableSection
import space.iseki.executables.common.ReadableSectionContainer
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.common.VirtualMemoryReadable
import space.iseki.executables.macho.lc.DylibCommand
import space.iseki.executables.macho.lc.DylinkerCommand
import space.iseki.executables.macho.lc.MachoLoadCommand
import space.iseki.executables.macho.lc.SegmentCommand
import space.iseki.executables.macho.lc.SegmentCommand64
import space.iseki.executables.macho.lc.UnsupportedCommand
import space.iseki.executables.macho.lc.UuidCommand
import space.iseki.executables.share.MemReader
import space.iseki.executables.share.toUnmodifiableList
import space.iseki.executables.share.u4
import kotlin.math.min

/**
 * Represents a Mach-O file and provides access to its contents.
 *
 * This class encapsulates the functionality for opening, parsing, and closing Mach-O files.
 * It provides methods to read and interpret the Mach-O header and other structures within the file.
 * @property dataAccessor The data accessor that provides access to the file content
 * @property header The Mach-O header of the file
 */
class MachoFile private constructor(
    private val dataAccessor: DataAccessor,
    val header: MachoHeader,
) : AutoCloseable, OpenedFile, VirtualMemoryReadable, ReadableSectionContainer {

    companion object : FileFormat<MachoFile> {
        override fun toString(): String = "Mach-O"

        @Throws(IOException::class)
        override fun open(accessor: DataAccessor): MachoFile {
            val buf = ByteArray(32) // 足够大以容纳任何 Mach-O 头部
            try {
                accessor.readAtMost(0, buf)
            } catch (e: EOFException) {
                throw MachoFileException("Failed to read Mach-O header", cause = e)
            }

            val header = MachoHeader.parse(buf, 0)
            // 验证头部
            header.validate(accessor.size)

            val loadCommandBuf = ByteArray(header.sizeofcmds.toInt())
            try {
                accessor.readFully(if (header.magic.is64Bit()) 32 else 28, loadCommandBuf)
            } catch (e: EOFException) {
                throw MachoFileException(
                    "Failed to read Mach-O load commands",
                    "offset" to (if (header.magic.is64Bit()) 32 else 28),
                    "size" to header.sizeofcmds,
                    cause = e,
                )
            }

            return MachoFile(accessor, header)
        }

    }

    override val rootHeaders: Map<String, ReadableStructure>
        get() = mapOf("mach" to header)

    override fun close() {
        dataAccessor.close()
    }

    val loaderCommands by lazy {
        val buf = ByteArray(header.sizeofcmds.toInt())
        try {
            dataAccessor.readFully(if (header.magic.is64Bit()) 32 else 28, buf)
        } catch (e: EOFException) {
            throw MachoFileException(
                "Failed to read Mach-O loader commands, unexpected EOF",
                "offset" to (if (header.magic.is64Bit()) 32 else 28),
                "size" to header.sizeofcmds,
                cause = e,
            )
        }
        LoaderCommands(buf)
    }

    inner class Section(override val name: String, override val header: MachoSection) : ReadableSection {
        private val zero
            get() = header.offset == 0u || header.flags.type == MachoSectionType.S_ZEROFILL || header.flags.type == MachoSectionType.S_GB_ZEROFILL
        override val size: Long
            get() = if (zero) 0 else header.size.toLong()

        override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
            DataAccessor.checkReadBounds(pos, buf, off, len)
            if (zero) return 0
            return dataAccessor.readAtMost(
                pos = pos + header.offset.toLong(),
                buf = buf,
                off = off,
                len = min(len, size.toInt()),
            )
        }
    }

    override val sections: List<Section> by lazy {
        segments.flatMap { it.sections }.map { section ->
            Section(section.sectName, section)
        }
    }

    inner class LoaderCommands internal constructor(val bytes: ByteArray) : AbstractList<MachoLoadCommand>() {
        override val size get() = header.ncmds.toInt()
        override fun get(index: Int): MachoLoadCommand {
            if (index < 0 || index >= size) throw IndexOutOfBoundsException("Index $index out of bounds for length $size")
            val p = commandOffset[index]
            val le = header.magic.isLittleEndian()
            return when (val cmdType = MachoLoadCommandType(bytes.u4(p, le))) {
                MachoLoadCommandType.LC_SEGMENT -> SegmentCommand.parse(bytes, p, le)
                MachoLoadCommandType.LC_SEGMENT_64 -> SegmentCommand64.parse(bytes, p, le)
                MachoLoadCommandType.LC_UUID -> UuidCommand.parse(bytes, p, le)
                MachoLoadCommandType.LC_LOAD_DYLIB,
                MachoLoadCommandType.LC_ID_DYLIB,
                    -> DylibCommand.parse(bytes, p, le, cmdType)

                MachoLoadCommandType.LC_LOAD_DYLINKER,
                MachoLoadCommandType.LC_ID_DYLINKER,
                    -> DylinkerCommand.parse(bytes, p, le, cmdType)

                else -> UnsupportedCommand(bytes.u4(p + 4, le), cmdType)
            }
        }

        private val commandOffset = IntArray(header.ncmds.toInt()).also {
            var p = 0
            val le = header.magic.isLittleEndian()
            try {
                for (i in it.indices) {
                    it[i] = p
                    val cmdSize = bytes.u4(p + 4, le)
                    p += cmdSize.toInt()
                }
            } catch (_: IndexOutOfBoundsException) {
                throw MachoFileException(
                    "Failed to parse Mach-O loader commands, unexpected EOF",
                    "command_count" to header.ncmds,
                    "current_offset" to p,
                )
            }
        }
    }

    private val segments by lazy {
        loaderCommands.mapNotNull {
            when (it) {
                is SegmentCommand -> MachoSegment(it)
                is SegmentCommand64 -> MachoSegment(it)
                else -> null
            }
        }.toUnmodifiableList()
    }

    private val vm by lazy {
        val mr = MemReader(dataAccessor).apply {
            for (section in segments.asSequence().flatMap { it.sections }) {
                mapMemory(vOff = section.addr.value, fOff = section.offset.toULong(), fSize = section.size)
            }
        }

        object : DataAccessor {
            override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
                DataAccessor.checkReadBounds(pos, buf, off, len)
                mr.read(
                    pos = pos.toULong(),
                    buf = buf,
                    off = off,
                    len = len,
                )
                return len
            }

            override val size: Long
                get() = Long.MAX_VALUE

            override fun close() {}
        }
    }

    override fun virtualMemory(): DataAccessor = vm
}

fun MachoMagic.isLittleEndian() = this == MachoMagic.MH_CIGAM || this == MachoMagic.MH_CIGAM_64
fun MachoMagic.is64Bit() = this == MachoMagic.MH_MAGIC_64 || this == MachoMagic.MH_CIGAM_64
fun MachoMagic.isValid() =
    this == MachoMagic.MH_MAGIC || this == MachoMagic.MH_CIGAM || this == MachoMagic.MH_MAGIC_64 || this == MachoMagic.MH_CIGAM_64
