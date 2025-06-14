@file:JvmName("-ElfPhdr")

package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.Address64
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u4
import space.iseki.executables.share.u8
import kotlin.jvm.JvmName

/**
 * Represents the ELF program header structure for both 32-bit and 64-bit formats.
 *
 * The program header describes a segment or other information the system needs
 * to prepare the program for execution. This unified class handles both 32-bit
 * and 64-bit versions of the ELF program header format, using the larger data
 * types to accommodate both formats.
 */
@Serializable
data class ElfPhdr internal constructor(
    /**
     * Indicates whether this is a 32-bit or 64-bit ELF file.
     * true for 64-bit, false for 32-bit.
     */
    val is64Bit: Boolean,

    /**
     * This member indicates what kind of segment this array element describes
     * or how to interpret the array element's information.
     */
    val pType: ElfPType,

    /**
     * This member holds a bit mask of flags relevant to the segment.
     */
    val pFlags: ElfPFlags,

    /**
     * This member holds the offset from the beginning of the file
     * at which the first byte of the segment resides.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val pOffset: ULong,

    /**
     * This member holds the virtual address at which the first byte
     * of the segment resides in memory.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val pVaddr: Address64,

    /**
     * On systems for which physical addressing is relevant, this member
     * is reserved for the segment's physical address.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val pPaddr: Address64,

    /**
     * This member holds the number of bytes in the file image of the segment.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val pFilesz: ULong,

    /**
     * This member holds the number of bytes in the memory image of the segment.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val pMemsz: ULong,

    /**
     * This member holds the value to which the segments are aligned in memory and in the file.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val pAlign: ULong,
) : ReadableStructure {

    override val fields: Map<String, Any>
        get() = mapOf(
            "is64Bit" to is64Bit,
            "pType" to pType,
            "pFlags" to pFlags,
            "pOffset" to pOffset,
            "pVaddr" to pVaddr,
            "pPaddr" to pPaddr,
            "pFilesz" to pFilesz,
            "pMemsz" to pMemsz,
            "pAlign" to pAlign,
        )

    companion object {
        /**
         * Parse an ELF program header from a byte array based on the ELF identification.
         *
         * @param bytes The byte array containing the ELF program header data.
         * @param off The offset in the byte array where the program header starts.
         * @param ident The ELF identification structure that indicates whether this is a 32-bit or 64-bit ELF file.
         * @return An instance of [ElfPhdr] representing the parsed program header.
         */
        fun parse(bytes: ByteArray, off: Int, ident: ElfIdentification, is64Bit: Boolean): ElfPhdr {
            return if (is64Bit) parse64(bytes, off, ident) else parse32(bytes, off, ident)
        }

        /**
         * Parse a 32-bit ELF program header from a byte array.
         */
        fun parse32(bytes: ByteArray, off: Int, ident: ElfIdentification): ElfPhdr {
            val le = ident.eiData == ElfData.ELFDATA2LSB
            return ElfPhdr(
                is64Bit = false,
                pType = ElfPType(bytes.u4(off + 0, le)),
                pOffset = bytes.u4(off + 4, le).toULong(),
                pVaddr = Address64(bytes.u4(off + 8, le).toULong()),
                pPaddr = Address64(bytes.u4(off + 12, le).toULong()),
                pFilesz = bytes.u4(off + 16, le).toULong(),
                pMemsz = bytes.u4(off + 20, le).toULong(),
                pFlags = ElfPFlags(bytes.u4(off + 24, le)),
                pAlign = bytes.u4(off + 28, le).toULong()
            )
        }

        /**
         * Parse a 64-bit ELF program header from a byte array.
         */
        fun parse64(bytes: ByteArray, off: Int, ident: ElfIdentification): ElfPhdr {
            val le = ident.eiData == ElfData.ELFDATA2LSB
            return ElfPhdr(
                is64Bit = true,
                pType = ElfPType(bytes.u4(off + 0, le)),
                pFlags = ElfPFlags(bytes.u4(off + 4, le)),
                pOffset = bytes.u8(off + 8, le),
                pVaddr = Address64(bytes.u8(off + 16, le)),
                pPaddr = Address64(bytes.u8(off + 24, le)),
                pFilesz = bytes.u8(off + 32, le),
                pMemsz = bytes.u8(off + 40, le),
                pAlign = bytes.u8(off + 48, le)
            )
        }
    }
}
