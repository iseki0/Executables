package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure

/**
 * Represents the ELF 64-bit file header structure.
 *
 * The ELF header defines the file's organization. It contains information about
 * how to interpret the file, such as whether it's a 32-bit or 64-bit ELF file,
 * which machine architecture it targets, where to find the program header table
 * and section header table, and other essential metadata.
 *
 * This class specifically handles the 64-bit version of the ELF header format.
 * The main difference from the 32-bit version is the size of certain fields,
 * which are expanded to accommodate 64-bit addresses and offsets.
 */
@Serializable
data class Elf64Ehdr(
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
    val eVersion: Elf64Word,

    /**
     * This member gives the virtual address to which the system first transfers control.
     *
     * If the file has no associated entry point, this member holds zero.
     */
    val eEntry: Elf64Addr,

    /**
     * This member holds the program header table's file offset in bytes.
     *
     * If the file has no program header table, this member holds zero.
     */
    val ePhoff: Elf64Off,

    /**
     * This member holds the section header table's file offset in bytes.
     *
     * If the file has no section header table, this member holds zero.
     */
    val eShoff: Elf64Off,

    /**
     * This member holds processor-specific flags associated with the file.
     *
     * Flag names take the form EF_machine_flag.
     */
    val eFlags: Elf64Word,

    /**
     * This member holds the ELF header's size in bytes.
     */
    val eEhsize: Elf64Half,

    /**
     * This member holds the size in bytes of one entry in the file's program header table.
     *
     * All entries are the same size.
     */
    val ePhentsize: Elf64Half,

    /**
     * This member holds the number of entries in the program header table.
     *
     * Thus the product of e_phentsize and e_phnum gives the table's size in bytes.
     * If a file has no program header table, e_phnum holds the value zero.
     */
    val ePhnum: Elf64Half,

    /**
     * This member holds a section header's size in bytes.
     * A section header is one entry in the section header table; all entries are the same size.
     */
    val eShentsize: Elf64Half,

    /**
     * This member holds the number of entries in the section header table.
     * Thus the product of e_shentsize and e_shnum gives the section header table's size in bytes.
     * If a file has no section header table, e_shnum holds the value zero.
     */
    val eShnum: Elf64Half,

    /**
     * This member holds the section header table index of the entry associated with the
     * section name string table. If the file has no section name string table, this member
     * holds the value SHN_UNDEF.
     */
    val eShstrndx: Elf64Half,
) : ReadableStructure, ElfEhdr {
    companion object {
        fun parse(bytes: ByteArray, off: Int, ident: ElfIdentification): Elf64Ehdr {
            val le = ident.eiData == ElfData.ELFDATA2LSB
            return Elf64Ehdr(
                eType = ElfType(if (le) bytes.u2l(off + 16) else bytes.u2b(off + 16)),
                eMachine = ElfMachine(if (le) bytes.u2l(off + 18) else bytes.u2b(off + 18)),
                eVersion = Elf64Word(if (le) bytes.u4e(off + 20) else bytes.u4b(off + 20)),
                eEntry = Elf64Addr(if (le) bytes.u8l(off + 24) else bytes.u8b(off + 24)),
                ePhoff = Elf64Off(if (le) bytes.u8l(off + 32) else bytes.u8b(off + 32)),
                eShoff = Elf64Off(if (le) bytes.u8l(off + 40) else bytes.u8b(off + 40)),
                eFlags = Elf64Word(if (le) bytes.u4e(off + 48) else bytes.u4b(off + 48)),
                eEhsize = Elf64Half(if (le) bytes.u2l(off + 52) else bytes.u2b(off + 52)),
                ePhentsize = Elf64Half(if (le) bytes.u2l(off + 54) else bytes.u2b(off + 54)),
                ePhnum = Elf64Half(if (le) bytes.u2l(off + 56) else bytes.u2b(off + 56)),
                eShentsize = Elf64Half(if (le) bytes.u2l(off + 58) else bytes.u2b(off + 58)),
                eShnum = Elf64Half(if (le) bytes.u2l(off + 60) else bytes.u2b(off + 60)),
                eShstrndx = Elf64Half(if (le) bytes.u2l(off + 62) else bytes.u2b(off + 62))
            )
        }
    }

    override val fields: Map<String, Any>
        get() = mapOf(
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
}
