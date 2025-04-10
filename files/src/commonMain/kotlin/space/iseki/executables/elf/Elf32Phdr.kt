package space.iseki.executables.elf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u4

@Serializable
@SerialName("Elf32Phdr")
data class Elf32Phdr internal constructor(
    /**
     * This member indicates what kind of segment this array element describes
     * or how to interpret the array element's information.
     */
    override val pType: ElfPType,

    /**
     * This member holds the offset from the beginning of the file
     * at which the first byte of the segment resides.
     */
    override val pOffset: Elf32Off,

    /**
     * This member holds the virtual address at which the first byte
     * of the segment resides in memory.
     */
    override val pVaddr: Elf32Addr,

    /**
     * On systems for which physical addressing is relevant, this member
     * is reserved for the segment's physical address.
     */
    override val pPaddr: Elf32Addr,

    /**
     * This member holds the number of bytes in the file image of the segment.
     */
    override val pFilesz: Elf32Word,

    /**
     * This member holds the number of bytes in the memory image of the segment.
     */
    override val pMemsz: Elf32Word,

    /**
     * This member holds a bit mask of flags relevant to the segment.
     */
    override val pFlags: ElfPFlags,

    /**
     * This member holds the value to which the segments are aligned in memory and in the file.
     */
    override val pAlign: Elf32Word,
) : ReadableStructure, ElfPhdr {
    companion object {
        fun parse(bytes: ByteArray, off: Int, ident: ElfIdentification): Elf32Phdr {
            val le = ident.eiData == ElfData.ELFDATA2LSB
            return Elf32Phdr(
                pType = ElfPType(bytes.u4(off + 0, le)),
                pOffset = Elf32Off(bytes.u4(off + 4, le)),
                pVaddr = Elf32Addr(bytes.u4(off + 8, le)),
                pPaddr = Elf32Addr(bytes.u4(off + 12, le)),
                pFilesz = Elf32Word(bytes.u4(off + 16, le)),
                pMemsz = Elf32Word(bytes.u4(off + 20, le)),
                pFlags = ElfPFlags(bytes.u4(off + 24, le)),
                pAlign = Elf32Word(bytes.u4(off + 28, le))
            )
        }
    }
} 