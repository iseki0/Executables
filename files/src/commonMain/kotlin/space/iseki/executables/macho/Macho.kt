package space.iseki.executables.macho

import space.iseki.executables.common.DataAccessor
import space.iseki.executables.common.FileFormat
import space.iseki.executables.common.IOException
import space.iseki.executables.common.OpenedFile
import space.iseki.executables.common.ReadableStructure

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
) : AutoCloseable, OpenedFile {

    companion object : FileFormat<MachoFile> {
        override fun toString(): String = "Mach-O"

        @Throws(IOException::class)
        override fun open(accessor: DataAccessor): MachoFile {
            val buf = ByteArray(32) // 足够大以容纳任何 Mach-O 头部
            try {
                accessor.readFully(0, buf)
            } catch (e: IOException) {
                throw MachoFileException("Failed to read Mach-O header", e)
            }

            val header = MachoHeader.parse(buf, 0)
            // 验证头部
            header.validate(accessor.size)
            return MachoFile(accessor, header)
        }

    }

    override val rootHeaders: Map<String, ReadableStructure>
        get() = mapOf("mach" to header)

    override fun close() {
        dataAccessor.close()
    }

}

fun MachoMagic.isLittleEndian() = this == MachoMagic.MH_CIGAM || this == MachoMagic.MH_CIGAM_64
fun MachoMagic.is64Bit() = this == MachoMagic.MH_MAGIC_64 || this == MachoMagic.MH_CIGAM_64
fun MachoMagic.isValid() = this == MachoMagic.MH_MAGIC || this == MachoMagic.MH_CIGAM ||
        this == MachoMagic.MH_MAGIC_64 || this == MachoMagic.MH_CIGAM_64
