package space.iseki.executables.elf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.iseki.executables.share.u4b
import space.iseki.executables.share.u4l
import space.iseki.executables.share.u8b
import space.iseki.executables.share.u8l

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
                shName = Elf64Word(if (le) bytes.u4l(off) else bytes.u4b(off)),
                shType = ElfSType(if (le) bytes.u4l(off + 4) else bytes.u4b(off + 4)),
                shFlags = ElfSFlags(if (le) bytes.u8l(off + 8) else bytes.u8b(off + 8)),
                shAddr = Elf64Addr(if (le) bytes.u8l(off + 16) else bytes.u8b(off + 16)),
                shOffset = Elf64Off(if (le) bytes.u8l(off + 24) else bytes.u8b(off + 24)),
                shSize = Elf64Xword(if (le) bytes.u8l(off + 32) else bytes.u8b(off + 32)),
                shLink = Elf64Word(if (le) bytes.u4l(off + 40) else bytes.u4b(off + 40)),
                shInfo = Elf64Word(if (le) bytes.u4l(off + 44) else bytes.u4b(off + 44)),
                shAddralign = Elf64Xword(if (le) bytes.u8l(off + 48) else bytes.u8b(off + 48)),
                shEntsize = Elf64Xword(if (le) bytes.u8l(off + 56) else bytes.u8b(off + 56))
            )
        }
    }
}