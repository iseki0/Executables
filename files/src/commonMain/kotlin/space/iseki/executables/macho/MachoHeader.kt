package space.iseki.executables.macho

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u4
import space.iseki.executables.share.u4b

/**
 * Represents the Mach-O file header.
 *
 * The Mach-O header contains basic information about the file, such as target architecture,
 * file type, load command count, and flags.
 */
@Serializable
data class MachoHeader internal constructor(
    val magic: MachoMagic,
    val cputype: UInt,
    val cpusubtype: UInt,
    val filetype: MachoFileType,
    val ncmds: UInt,
    val sizeofcmds: UInt,
    val flags: MachoFlags,
    val reserved: UInt = 0u,
    val isLittleEndian: Boolean,
) : ReadableStructure {

    override val fields: Map<String, Any>
        get() = mapOf(
            "magic" to magic,
            "cputype" to cputype,
            "cpusubtype" to cpusubtype,
            "filetype" to filetype,
            "ncmds" to ncmds,
            "sizeofcmds" to sizeofcmds,
            "flags" to flags,
        )

    companion object {

        /**
         * Parses a Mach-O header from a byte array.
         *
         * @param bytes The byte array containing the Mach-O header
         * @param offset The offset within the byte array at which the header starts
         * @return A [MachoHeader] instance based on the magic number
         * @throws MachoFileException if the magic number is not recognized
         */
        fun parse(bytes: ByteArray, offset: Int): MachoHeader {
            val magic = MachoMagic(bytes.u4b(offset))
            if (!magic.isValid()) throw MachoFileException("Invalid magic number", "magic" to magic)

            val le = magic.isLittleEndian()

            return MachoHeader(
                magic = magic,
                cputype = bytes.u4(offset + 4, le),
                cpusubtype = bytes.u4(offset + 8, le),
                filetype = MachoFileType(bytes.u4(offset + 12, le)),
                ncmds = bytes.u4(offset + 16, le),
                sizeofcmds = bytes.u4(offset + 20, le),
                flags = MachoFlags(bytes.u4(offset + 24, le)),
                reserved = if (magic.is64Bit()) bytes.u4(offset + 28, le) else 0u,
                isLittleEndian = le,
            )
        }
    }
}

/**
 * Validates the Mach-O header fields for consistency and correctness.
 *
 * @param fileSize The size of the file in bytes
 * @throws MachoFileException if the header is invalid
 */
@OptIn(ExperimentalStdlibApi::class)
internal fun MachoHeader.validate(fileSize: Long) {

    // 验证文件大小是否至少能容纳头部
    val headerSize = if (magic.is64Bit()) 32 else 28

    if (fileSize < headerSize) {
        throw MachoFileException(
            "File too small to contain Mach-O header",
            "file_size" to fileSize,
            "header_size" to headerSize,
        )
    }

    // 验证加载命令
    val cmdSize = sizeofcmds.toInt()
    if (cmdSize < 0 || cmdSize > fileSize - headerSize) {
        throw MachoFileException("Invalid load commands size", "size" to cmdSize)
    }

    val cmdCount = ncmds.toInt()
    if (cmdCount <= 0 || cmdCount > 10000) { // 设置一个合理的上限
        throw MachoFileException("Invalid number of load commands", "count" to cmdCount)
    }

    // 验证 reserved 字段
    if (magic.is64Bit()) {
        // 64位版本必须有 reserved 字段
        if (reserved != 0u || !magic.isValid()) {
            throw MachoFileException(
                "Invalid reserved field in 64-bit header",
                "value" to "0x${reserved.toHexString()}",
            )
        }
    } else {
        // 32位版本不应该有 reserved 字段
        if (reserved != 0u || !magic.isValid()) {
            throw MachoFileException(
                "Reserved field should be 0 in 32-bit header",
                "value" to "0x${reserved.toHexString()}",
            )
        }
    }
}