package space.iseki.executables.elf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u2
import space.iseki.executables.share.u4

/**
 * Represents the ELF 32-bit file header structure.
 *
 * The ELF header defines the file's organization. It contains information about
 * how to interpret the file, such as whether it's a 32-bit or 64-bit ELF file,
 * which machine architecture it targets, where to find the program header table
 * and section header table, and other essential metadata.
 *
 * This class specifically handles the 32-bit version of the ELF header format.
 */
@Serializable
@SerialName("Elf32Ehdr")
data class Elf32Ehdr internal constructor(
    /**
     * This member identifies the object file type.
     */
    override val eType: ElfType,

    /**
     * This member's value specifies the required architecture for an individual file.
     *
     * Other values are reserved and will be assigned to new machines as necessary.
     * Processor-specific ELF names use the machine name to distinguish them. For
     * example, the flags mentioned below use the prefix EF_; a flag named WIDGET for
     * the EM_XYZ machine would be called EF_XYZ_WIDGET.
     */
    override val eMachine: ElfMachine,

    /**
     * This member identifies the object file version.
     */
    override val eVersion: Elf32Word,

    /**
     * This member gives the virtual address to which the system first transfers control.
     *
     * If the file has no associated entry point, this member holds zero.
     */
    override val eEntry: Elf32Addr,

    /**
     * This member holds the program header table's file offset in bytes.
     *
     * If the file has no program header table, this member holds zero.
     */
    override val ePhoff: Elf32Off,

    /**
     * This member holds the section header table's file offset in bytes.
     *
     * If the file has no section header table, this member holds zero.
     */
    override val eShoff: Elf32Off,

    /**
     * This member holds processor-specific flags associated with the file.
     *
     * Flag names take the form EF_machine_flag.
     */
    override val eFlags: Elf32Word,

    /**
     * This member holds the ELF header's size in bytes.
     */
    override val eEhsize: Elf32Half,

    /**
     * This member holds the size in bytes of one entry in the file's program header table.
     *
     * All entries are the same size.
     */
    override val ePhentsize: Elf32Half,

    /**
     * This member holds the number of entries in the program header table.
     *
     * Thus the product of e_phentsize and e_phnum gives the table's size in bytes.
     * If a file has no program header table, e_phnum holds the value zero.
     */
    override val ePhnum: Elf32Half,

    /**
     * This member holds a section header's size in bytes.
     * A section header is one entry in the section header table; all entries are the same size.
     */
    override val eShentsize: Elf32Half,

    /**
     * This member holds the number of entries in the section header table.
     * Thus the product of e_shentsize and e_shnum gives the section header table's size in bytes.
     * If a file has no section header table, e_shnum holds the value zero.
     */
    override val eShnum: Elf32Half,

    /**
     * This member holds the section header table index of the entry associated with the
     * section name string table. If the file has no section name string table, this member
     * holds the value SHN_UNDEF.
     */
    override val eShstrndx: Elf32Half,
) : ReadableStructure, ElfEhdr {
    companion object {
        fun parse(bytes: ByteArray, off: Int, ident: ElfIdentification): Elf32Ehdr {
            val le = ident.eiData == ElfData.ELFDATA2LSB
            return Elf32Ehdr(
                eType = ElfType(bytes.u2(off + 16, le)),
                eMachine = ElfMachine(bytes.u2(off + 18, le)),
                eVersion = Elf32Word(bytes.u4(off + 20, le)),
                eEntry = Elf32Addr(bytes.u4(off + 24, le)),
                ePhoff = Elf32Off(bytes.u4(off + 28, le)),
                eShoff = Elf32Off(bytes.u4(off + 32, le)),
                eFlags = Elf32Word(bytes.u4(off + 36, le)),
                eEhsize = Elf32Half(bytes.u2(off + 40, le)),
                ePhentsize = Elf32Half(bytes.u2(off + 42, le)),
                ePhnum = Elf32Half(bytes.u2(off + 44, le)),
                eShentsize = Elf32Half(bytes.u2(off + 46, le)),
                eShnum = Elf32Half(bytes.u2(off + 48, le)),
                eShstrndx = Elf32Half(bytes.u2(off + 50, le))
            )
        }
    }
}

