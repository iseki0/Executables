@file:JvmName("-ElfShdr")

package space.iseki.executables.elf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.iseki.executables.common.Address32
import space.iseki.executables.common.Address64
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u4
import space.iseki.executables.share.u8
import kotlin.jvm.JvmName

/**
 * Represents the ELF section header structure for both 32-bit and 64-bit formats.
 *
 * The section header table is an array of these structures, with each entry describing
 * a section in the file. This unified class handles both 32-bit and 64-bit versions
 * of the ELF section header format, using the larger data types to accommodate both formats.
 */
@Serializable
@SerialName("ElfShdr")
data class ElfShdr internal constructor(
    /**
     * Indicates whether this is a 32-bit or 64-bit ELF file.
     * true for 64-bit, false for 32-bit.
     */
    val is64Bit: Boolean,

    /**
     * Section name, index in string table.
     */
    val shName: UInt,

    /**
     * Section type.
     */
    val shType: ElfSType,

    /**
     * Section flags.
     */
    val shFlags: ElfSFlags,

    /**
     * Section virtual address at execution.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val shAddr: Address64,

    /**
     * Section file offset.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val shOffset: ULong,

    /**
     * Section size in bytes.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val shSize: ULong,

    /**
     * Link to another section.
     */
    val shLink: UInt,

    /**
     * Additional section information.
     */
    val shInfo: UInt,

    /**
     * Section alignment.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val shAddralign: ULong,

    /**
     * Entry size if section holds table.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val shEntsize: ULong,

    /**
     * Section name string.
     */
    val name: String? = null,
) : ReadableStructure {

    override val fields: Map<String, Any>
        get() = if (is64Bit) {
            mapOf(
                "is64Bit" to is64Bit,
                "shName" to shName,
                "shType" to shType,
                "shFlags" to shFlags,
                "shAddr" to shAddr,
                "shOffset" to shOffset,
                "shSize" to shSize,
                "shLink" to shLink,
                "shInfo" to shInfo,
                "shAddralign" to shAddralign,
                "shEntsize" to shEntsize,
                "name" to (name ?: "")
            )
        } else {
            mapOf(
                "is64Bit" to is64Bit,
                "shName" to shName,
                "shType" to shType,
                "shFlags" to shFlags,
                "shAddr" to Address32(shAddr.value.toUInt()),
                "shOffset" to shOffset.toUInt(),
                "shSize" to shSize.toUInt(),
                "shLink" to shLink,
                "shInfo" to shInfo,
                "shAddralign" to shAddralign.toUInt(),
                "shEntsize" to shEntsize.toUInt(),
                "name" to (name ?: "")
            )
        }

    companion object {

        fun parse(bytes: ByteArray, off: Int, le: Boolean, is64Bit: Boolean): ElfShdr {
            return if (is64Bit) parse64(bytes, off, le) else parse32(bytes, off, le)
        }
        /**
         * Parse a 32-bit ELF section header from a byte array.
         */
        fun parse32(bytes: ByteArray, off: Int, le: Boolean): ElfShdr {
            return ElfShdr(
                is64Bit = false,
                shName = bytes.u4(off + 0, le),
                shType = ElfSType(bytes.u4(off + 4, le)),
                shFlags = ElfSFlags(bytes.u4(off + 8, le).toULong()),
                shAddr = Address64(bytes.u4(off + 12, le).toULong()),
                shOffset = bytes.u4(off + 16, le).toULong(),
                shSize = bytes.u4(off + 20, le).toULong(),
                shLink = bytes.u4(off + 24, le),
                shInfo = bytes.u4(off + 28, le),
                shAddralign = bytes.u4(off + 32, le).toULong(),
                shEntsize = bytes.u4(off + 36, le).toULong()
            )
        }

        /**
         * Parse a 64-bit ELF section header from a byte array.
         */
        fun parse64(bytes: ByteArray, off: Int, le: Boolean): ElfShdr {
            return ElfShdr(
                is64Bit = true,
                shName = bytes.u4(off + 0, le),
                shType = ElfSType(bytes.u4(off + 4, le)),
                shFlags = ElfSFlags(bytes.u8(off + 8, le)),
                shAddr = Address64(bytes.u8(off + 16, le)),
                shOffset = bytes.u8(off + 24, le),
                shSize = bytes.u8(off + 32, le),
                shLink = bytes.u4(off + 40, le),
                shInfo = bytes.u4(off + 44, le),
                shAddralign = bytes.u8(off + 48, le),
                shEntsize = bytes.u8(off + 56, le)
            )
        }
    }
}
