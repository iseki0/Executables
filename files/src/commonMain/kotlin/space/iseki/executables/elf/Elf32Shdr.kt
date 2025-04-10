package space.iseki.executables.elf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.iseki.executables.share.u4

/**
 * Represents a section header in a 32-bit ELF file.
 *
 * The section header table is an array of these structures, with each entry describing
 * a section in the file. The ELF header's e_shoff member gives the byte offset from
 * the beginning of the file to the section header table.
 */
@Serializable
@SerialName("Elf32Shdr")
data class Elf32Shdr internal constructor(
    /**
     * Section name, index in string table.
     */
    override val shName: Elf32Word,

    /**
     * Section type.
     */
    override val shType: ElfSType,

    /**
     * Section flags.
     */
    override val shFlags: ElfSFlags,

    /**
     * Section virtual address at execution.
     */
    override val shAddr: Elf32Addr,

    /**
     * Section file offset.
     */
    override val shOffset: Elf32Off,

    /**
     * Section size in bytes.
     */
    override val shSize: Elf32Word,

    /**
     * Link to another section.
     */
    override val shLink: Elf32Word,

    /**
     * Additional section information.
     */
    override val shInfo: Elf32Word,

    /**
     * Section alignment.
     */
    override val shAddralign: Elf32Word,

    /**
     * Entry size if section holds table.
     */
    override val shEntsize: Elf32Word,

    /**
     * Section name string.
     */
    override val name: String? = null,
) : ElfShdr {
    companion object {
        fun parse(bytes: ByteArray, off: Int, le: Boolean): Elf32Shdr {
            return Elf32Shdr(
                shName = Elf32Word(bytes.u4(off + 0, le)),
                shType = ElfSType(bytes.u4(off + 4, le)),
                shFlags = ElfSFlags(bytes.u4(off + 8, le).toULong()),
                shAddr = Elf32Addr(bytes.u4(off + 12, le)),
                shOffset = Elf32Off(bytes.u4(off + 16, le)),
                shSize = Elf32Word(bytes.u4(off + 20, le)),
                shLink = Elf32Word(bytes.u4(off + 24, le)),
                shInfo = Elf32Word(bytes.u4(off + 28, le)),
                shAddralign = Elf32Word(bytes.u4(off + 32, le)),
                shEntsize = Elf32Word(bytes.u4(off + 36, le))
            )
        }
    }
} 