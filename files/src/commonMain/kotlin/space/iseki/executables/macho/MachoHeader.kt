package space.iseki.executables.macho

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u4b

/**
 * Represents the Mach-O file header.
 *
 * The Mach-O header contains basic information about the file, such as target architecture,
 * file type, load command count, and flags.
 */
@Serializable
sealed interface MachoHeader : ReadableStructure {
    /**
     * Magic number identifying this as a Mach-O file.
     */
    val magic: UInt

    /**
     * CPU type for which this file was generated.
     */
    val cputype: UInt

    /**
     * CPU subtype for which this file was generated.
     */
    val cpusubtype: UInt

    /**
     * Type of file (executable, object, core, etc.)
     */
    val filetype: MachoFileType

    /**
     * Number of load commands following the header.
     */
    val ncmds: UInt

    /**
     * Total size of all load commands.
     */
    val sizeofcmds: UInt

    /**
     * Flags specific to the file type.
     */
    val flags: MachoFlags

    /**
     * Returns whether this header represents a little-endian file.
     */
    val isLittleEndian: Boolean

    override val fields: Map<String, Any>
        get() = mapOf(
            "magic" to magic,
            "cputype" to cputype,
            "cpusubtype" to cpusubtype,
            "filetype" to filetype,
            "ncmds" to ncmds,
            "sizeofcmds" to sizeofcmds,
            "flags" to flags
        )

    companion object {
        // Magic constants for Mach-O files
        const val MH_MAGIC: UInt = 0xFEEDFACEu    // 32-bit big endian
        const val MH_CIGAM: UInt = 0xCEFAEDFEu    // 32-bit little endian
        const val MH_MAGIC_64: UInt = 0xFEEDFACFu // 64-bit big endian
        const val MH_CIGAM_64: UInt = 0xCFFAEDFEu // 64-bit little endian

        /**
         * Parses a Mach-O header from a byte array.
         *
         * @param bytes The byte array containing the Mach-O header
         * @param offset The offset within the byte array at which the header starts
         * @return A [MachoHeader] instance based on the magic number
         * @throws MachoFileException if the magic number is not recognized
         */
        fun parse(bytes: ByteArray, offset: Int): MachoHeader {
            return when (val magic = bytes.u4b(offset)) {
                MH_MAGIC -> {
                    // 32-bit big endian
                    Macho32Header.parse(bytes, offset, false)
                }

                MH_CIGAM -> {
                    // 32-bit little endian
                    Macho32Header.parse(bytes, offset, true)
                }

                MH_MAGIC_64 -> {
                    // 64-bit big endian
                    Macho64Header.parse(bytes, offset, false)
                }

                MH_CIGAM_64 -> {
                    // 64-bit little endian
                    Macho64Header.parse(bytes, offset, true)
                }

                else -> {
                    throw MachoFileException("Not a Mach-O file: invalid magic number: 0x${magic.toString(16)}")
                }
            }
        }
    }
}

/**
 * Validates the Mach-O header fields for consistency and correctness.
 *
 * @param fileSize The size of the file in bytes
 * @throws MachoFileException if the header is invalid
 */
internal fun MachoHeader.validate(fileSize: Long) {
    // 验证文件大小是否至少能容纳头部
    val headerSize = when (this) {
        is Macho32Header -> 28
        is Macho64Header -> 32
    }

    if (fileSize < headerSize) {
        throw MachoFileException("File too small to contain Mach-O header: $fileSize < $headerSize")
    }

    // 验证加载命令
    val cmdSize = sizeofcmds.toInt()
    if (cmdSize < 0 || cmdSize > fileSize - headerSize) {
        throw MachoFileException("Invalid load commands size: $cmdSize")
    }

    val cmdCount = ncmds.toInt()
    if (cmdCount <= 0 || cmdCount > 10000) { // 设置一个合理的上限
        throw MachoFileException("Invalid number of load commands: $cmdCount")
    }
} 