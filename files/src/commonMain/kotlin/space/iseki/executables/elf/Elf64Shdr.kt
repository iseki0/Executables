package space.iseki.executables.elf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.iseki.executables.share.u4
import space.iseki.executables.share.u8

/**
 * Represents a section header in a 64-bit ELF file.
 *
 * The section header table is an array of these structures, with each entry describing
 * a section in the file. The ELF header's e_shoff member gives the byte offset from
 * the beginning of the file to the section header table.
 */
@Serializable
@SerialName("Elf64Shdr")
data class Elf64Shdr internal constructor(
    /**
     * Section name, index in string table.
     */
    override val shName: Elf64Word,

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
    override val shAddr: Elf64Addr,

    /**
     * Section file offset.
     */
    override val shOffset: Elf64Off,

    /**
     * Section size in bytes.
     */
    override val shSize: Elf64Xword,

    /**
     * Link to another section.
     */
    override val shLink: Elf64Word,

    /**
     * Additional section information.
     */
    override val shInfo: Elf64Word,

    /**
     * Section alignment.
     */
    override val shAddralign: Elf64Xword,

    /**
     * Entry size if section holds table.
     */
    override val shEntsize: Elf64Xword,

    /**
     * Section name string.
     */
    override val name: String? = null,
) : ElfShdr {
    companion object {
        fun parse(bytes: ByteArray, off: Int, le: Boolean): Elf64Shdr {
            return Elf64Shdr(
                shName = Elf64Word(bytes.u4(off + 0, le)),
                shType = ElfSType(bytes.u4(off + 4, le)),
                shFlags = ElfSFlags(bytes.u8(off + 8, le)),
                shAddr = Elf64Addr(bytes.u8(off + 16, le)),
                shOffset = Elf64Off(bytes.u8(off + 24, le)),
                shSize = Elf64Xword(bytes.u8(off + 32, le)),
                shLink = Elf64Word(bytes.u4(off + 40, le)),
                shInfo = Elf64Word(bytes.u4(off + 44, le)),
                shAddralign = Elf64Xword(bytes.u8(off + 48, le)),
                shEntsize = Elf64Xword(bytes.u8(off + 56, le))
            )
        }
    }
}