package space.iseki.executables.macho

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u4b
import space.iseki.executables.share.u4l

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
            if (!magic.isValid()) throw MachoFileException("Invalid magic number: $magic")

            val readUInt = if (magic.isLittleEndian()) {
                { pos: Int -> bytes.u4l(pos) }
            } else {
                { pos: Int -> bytes.u4b(pos) }
            }

            return MachoHeader(
                magic = magic,
                cputype = readUInt(offset + 4),
                cpusubtype = readUInt(offset + 8),
                filetype = MachoFileType(readUInt(offset + 12)),
                ncmds = readUInt(offset + 16),
                sizeofcmds = readUInt(offset + 20),
                flags = MachoFlags(readUInt(offset + 24)),
                reserved = if (magic.is64Bit()) readUInt(offset + 28) else 0u,
                isLittleEndian = magic.isLittleEndian()
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

    // 验证 reserved 字段
    if (magic.is64Bit()) {
        // 64位版本必须有 reserved 字段
        if (reserved != 0u || !magic.isValid()) {
            throw MachoFileException("Invalid reserved field in 64-bit header: 0x${reserved.toHexString()}")
        }
    } else {
        // 32位版本不应该有 reserved 字段
        if (reserved != 0u || !magic.isValid()) {
            throw MachoFileException("Reserved field should be 0 in 32-bit header: 0x${reserved.toHexString()}")
        }
    }
}