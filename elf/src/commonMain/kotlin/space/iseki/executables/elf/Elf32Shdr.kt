package space.iseki.executables.elf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.common.u4b
import space.iseki.executables.common.u4l

/**
 * Represents a section header in a 32-bit ELF file.
 *
 * The section header table is an array of these structures, with each entry describing
 * a section in the file. The ELF header's e_shoff member gives the byte offset from
 * the beginning of the file to the section header table.
 */
@Serializable
@SerialName("Elf32Shdr")
data class Elf32Shdr(
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
) : ReadableStructure, ElfShdr {
    companion object {
        fun parse(bytes: ByteArray, off: Int, le: Boolean): Elf32Shdr {
            return Elf32Shdr(
                shName = Elf32Word(if (le) bytes.u4l(off) else bytes.u4b(off)),
                shType = ElfSType(if (le) bytes.u4l(off + 4) else bytes.u4b(off + 4)),
                shFlags = ElfSFlags((if (le) bytes.u4l(off + 8) else bytes.u4b(off + 8)).toULong()),
                shAddr = Elf32Addr(if (le) bytes.u4l(off + 12) else bytes.u4b(off + 12)),
                shOffset = Elf32Off(if (le) bytes.u4l(off + 16) else bytes.u4b(off + 16)),
                shSize = Elf32Word(if (le) bytes.u4l(off + 20) else bytes.u4b(off + 20)),
                shLink = Elf32Word(if (le) bytes.u4l(off + 24) else bytes.u4b(off + 24)),
                shInfo = Elf32Word(if (le) bytes.u4l(off + 28) else bytes.u4b(off + 28)),
                shAddralign = Elf32Word(if (le) bytes.u4l(off + 32) else bytes.u4b(off + 32)),
                shEntsize = Elf32Word(if (le) bytes.u4l(off + 36) else bytes.u4b(off + 36))
            )
        }
    }
} 