@file:JvmName("-ElfEhdr")

package space.iseki.executables.elf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.iseki.executables.common.Address32
import space.iseki.executables.common.Address64
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.common.toAddr
import space.iseki.executables.share.u2
import space.iseki.executables.share.u4
import space.iseki.executables.share.u8
import kotlin.jvm.JvmName

/**
 * Represents the ELF file header structure for both 32-bit and 64-bit formats.
 *
 * The ELF header defines the file's organization. It contains information about
 * how to interpret the file, such as whether it's a 32-bit or 64-bit ELF file,
 * which machine architecture it targets, where to find the program header table
 * and section header table, and other essential metadata.
 *
 * This unified class handles both 32-bit and 64-bit versions of the ELF header format,
 * using the larger data types to accommodate both formats.
 */
@Serializable
@SerialName("ElfEhdr")
data class ElfEhdr internal constructor(
    /**
     * Indicates whether this is a 32-bit or 64-bit ELF file.
     * true for 64-bit, false for 32-bit.
     */
    val is64Bit: Boolean,

    /**
     * This member identifies the object file type.
     */
    val eType: ElfType,

    /**
     * This member's value specifies the required architecture for an individual file.
     *
     * Other values are reserved and will be assigned to new machines as necessary.
     * Processor-specific ELF names use the machine name to distinguish them. For
     * example, the flags mentioned below use the prefix EF_; a flag named WIDGET for
     * the EM_XYZ machine would be called EF_XYZ_WIDGET.
     */
    val eMachine: ElfMachine,

    /**
     * This member identifies the object file version.
     */
    val eVersion: UInt,

    /**
     * This member gives the virtual address to which the system first transfers control.
     *
     * If the file has no associated entry point, this member holds zero.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val eEntry: Address64,

    /**
     * This member holds the program header table's file offset in bytes.
     *
     * If the file has no program header table, this member holds zero.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val ePhoff: ULong,

    /**
     * This member holds the section header table's file offset in bytes.
     *
     * If the file has no section header table, this member holds zero.
     * For 32-bit ELF files, only the lower 32 bits are meaningful.
     */
    val eShoff: ULong,

    /**
     * This member holds processor-specific flags associated with the file.
     *
     * Flag names take the form EF_machine_flag.
     */
    val eFlags: UInt,

    /**
     * This member holds the ELF header's size in bytes.
     */
    val eEhsize: UShort,

    /**
     * This member holds the size in bytes of one entry in the file's program header table.
     *
     * All entries are the same size.
     */
    val ePhentsize: UShort,

    /**
     * This member holds the number of entries in the program header table.
     *
     * Thus the product of e_phentsize and e_phnum gives the table's size in bytes.
     * If a file has no program header table, e_phnum holds the value zero.
     */
    val ePhnum: UShort,

    /**
     * This member holds a section header's size in bytes.
     * A section header is one entry in the section header table; all entries are the same size.
     */
    val eShentsize: UShort,

    /**
     * This member holds the number of entries in the section header table.
     * Thus the product of e_shentsize and e_shnum gives the section header table's size in bytes.
     * If a file has no section header table, e_shnum holds the value zero.
     */
    val eShnum: UShort,

    /**
     * This member holds the section header table index of the entry associated with the
     * section name string table. If the file has no section name string table, this member
     * holds the value SHN_UNDEF.
     */
    val eShstrndx: UShort,
) : ReadableStructure {

    override val fields: Map<String, Any>
        get() = if (is64Bit) {
            mapOf(
                "is64Bit" to is64Bit,
                "eType" to eType,
                "eMachine" to eMachine,
                "eVersion" to eVersion,
                "eEntry" to eEntry,
                "ePhoff" to ePhoff,
                "eShoff" to eShoff,
                "eFlags" to eFlags,
                "eEhsize" to eEhsize,
                "ePhentsize" to ePhentsize,
                "ePhnum" to ePhnum,
                "eShentsize" to eShentsize,
                "eShnum" to eShnum,
                "eShstrndx" to eShstrndx,
            )
        } else {
            mapOf(
                "is64Bit" to is64Bit,
                "eType" to eType,
                "eMachine" to eMachine,
                "eVersion" to eVersion,
                "eEntry" to Address32(eEntry.value.toUInt()),
                "ePhoff" to ePhoff.toUInt(),
                "eShoff" to eShoff.toUInt(),
                "eFlags" to eFlags,
                "eEhsize" to eEhsize,
                "ePhentsize" to ePhentsize,
                "ePhnum" to ePhnum,
                "eShentsize" to eShentsize,
                "eShnum" to eShnum,
                "eShstrndx" to eShstrndx,
            )
        }

    companion object {

        fun parse(bytes: ByteArray, off: Int, ident: ElfIdentification): ElfEhdr {
            return if (ident.eiClass == ElfClass.ELFCLASS32) {
                parse32(bytes, off, ident)
            } else if (ident.eiClass == ElfClass.ELFCLASS64) {
                parse64(bytes, off, ident)
            } else {
                throw IllegalArgumentException("Unsupported ELF class: ${ident.eiClass}")
            }
        }

        /**
         * Parse a 32-bit ELF header from a byte array.
         */
        fun parse32(bytes: ByteArray, off: Int, ident: ElfIdentification): ElfEhdr {
            val le = ident.eiData == ElfData.ELFDATA2LSB
            return ElfEhdr(
                is64Bit = false,
                eType = ElfType(bytes.u2(off + 16, le)),
                eMachine = ElfMachine(bytes.u2(off + 18, le)),
                eVersion = bytes.u4(off + 20, le),
                eEntry = bytes.u4(off + 24, le).toULong().toAddr(),
                ePhoff = bytes.u4(off + 28, le).toULong(),
                eShoff = bytes.u4(off + 32, le).toULong(),
                eFlags = bytes.u4(off + 36, le),
                eEhsize = bytes.u2(off + 40, le),
                ePhentsize = bytes.u2(off + 42, le),
                ePhnum = bytes.u2(off + 44, le),
                eShentsize = bytes.u2(off + 46, le),
                eShnum = bytes.u2(off + 48, le),
                eShstrndx = bytes.u2(off + 50, le),
            )
        }

        /**
         * Parse a 64-bit ELF header from a byte array.
         */
        fun parse64(bytes: ByteArray, off: Int, ident: ElfIdentification): ElfEhdr {
            val le = ident.eiData == ElfData.ELFDATA2LSB
            return ElfEhdr(
                is64Bit = true,
                eType = ElfType(bytes.u2(off + 16, le)),
                eMachine = ElfMachine(bytes.u2(off + 18, le)),
                eVersion = bytes.u4(off + 20, le),
                eEntry = bytes.u8(off + 24, le).toAddr(),
                ePhoff = bytes.u8(off + 32, le),
                eShoff = bytes.u8(off + 40, le),
                eFlags = bytes.u4(off + 48, le),
                eEhsize = bytes.u2(off + 52, le),
                ePhentsize = bytes.u2(off + 54, le),
                ePhnum = bytes.u2(off + 56, le),
                eShentsize = bytes.u2(off + 58, le),
                eShnum = bytes.u2(off + 60, le),
                eShstrndx = bytes.u2(off + 62, le),
            )
        }
    }
}

/**
 * Validates the ELF header fields for consistency and correctness.
 *
 * @param fileSize The size of the file in bytes
 * @throws ElfFileException if the header is invalid
 */
internal fun ElfEhdr.validate(fileSize: Long) {
    // Validate e_type
    when (eType) {
        ElfType.ET_NONE -> throw ElfFileException("Invalid file type: ET_NONE", "type" to eType)
        ElfType.ET_REL, ElfType.ET_EXEC, ElfType.ET_DYN, ElfType.ET_CORE -> {}

        else -> {
            // Check if it's a processor-specific type (between ET_LOPROC and ET_HIPROC)
            val typeValue = eType.value.toInt()
            if (typeValue < ElfType.ET_LOPROC.value.toInt() || typeValue > ElfType.ET_HIPROC.value.toInt()) {
                throw ElfFileException("Unknown file type", "type" to eType)
            }
        }
    }

    // Validate e_version
    if (eVersion.toInt() != 1) {
        throw ElfFileException("Invalid ELF version, expected 1", "version" to eVersion.toInt())
    }

    // Validate e_ehsize (ELF header size)
    val expectedEhdrSize = if (is64Bit) 64 else 52

    if (eEhsize.toInt() != expectedEhdrSize) {
        throw ElfFileException(
            "Invalid ELF header size",
            "actual_size" to eEhsize.toInt(),
            "expected_size" to expectedEhdrSize,
        )
    }

    // Validate offsets are within file bounds
    if (ePhoff != 0UL && ePhoff.toLong() >= fileSize) {
        throw ElfFileException(
            "Program header table offset is beyond file end",
            "offset" to ePhoff,
            "file_size" to fileSize,
        )
    }
    if (eShoff != 0UL && eShoff.toLong() >= fileSize) {
        throw ElfFileException(
            "Section header table offset is beyond file end",
            "offset" to eShoff,
            "file_size" to fileSize,
        )
    }

    // Validate section header string table index
    val shnum = eShnum.toInt()
    val shstrndx = eShstrndx.toInt()

    if (shnum in 1..shstrndx) {
        throw ElfFileException(
            "Section header string table index is out of bounds",
            "index" to shstrndx,
            "section_count" to shnum,
        )
    }

    val phOffset: Long = this.ePhoff.toLong()
    val phEntSize: Int = this.ePhentsize.toInt()
    val phNum: Int = this.ePhnum.toInt()
    if (this.is64Bit) {
        if (phEntSize < 56) // Minimum size for 64-bit program header
            throw ElfFileException(
                "Invalid program header entry size, must be at least 56 bytes",
                "size" to phEntSize,
            )
    } else {
        if (phEntSize < 32) // Minimum size for 32-bit program header
            throw ElfFileException(
                "Invalid program header entry size, must be at least 32 bytes",
                "size" to phEntSize,
            )
    }
    val phSize = phEntSize * phNum
    if (phOffset + phSize > fileSize) {
        throw ElfFileException(
            "Program header table extends beyond file end",
            "offset" to phOffset,
            "size" to phSize,
            "file_size" to fileSize,
        )
    }

    val shOffset = this.eShoff.toLong()
    val shEntSize = this.eShentsize.toInt()
    if (this.is64Bit) {
        if (shEntSize < 64) // Minimum size for 64-bit section header
            throw ElfFileException(
                "Invalid section header entry size, must be at least 64 bytes",
                "size" to shEntSize,
            )
    } else {
        if (shEntSize < 40) // Minimum size for 32-bit section header
            throw ElfFileException(
                "Invalid section header entry size, must be at least 40 bytes",
                "size" to shEntSize,
            )
    }
    val shNum = this.eShnum.toInt()
    val shSize = shEntSize * shNum
    if (shOffset + shSize > fileSize) {
        throw ElfFileException(
            "Section header table extends beyond file end",
            "offset" to shOffset,
            "size" to shSize,
            "file_size" to fileSize,
        )
    }

}