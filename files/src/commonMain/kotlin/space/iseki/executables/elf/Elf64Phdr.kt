package space.iseki.executables.elf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u4
import space.iseki.executables.share.u8

@Serializable
@SerialName("Elf64Phdr")
data class Elf64Phdr internal constructor(
    /**
     * This member indicates what kind of segment this array element describes
     * or how to interpret the array element's information.
     */
    override val pType: ElfPType,

    /**
     * This member holds a bit mask of flags relevant to the segment.
     * Note: In 64-bit format, this field is moved up compared to 32-bit format.
     */
    override val pFlags: ElfPFlags,

    /**
     * This member holds the offset from the beginning of the file
     * at which the first byte of the segment resides.
     */
    override val pOffset: Elf64Off,

    /**
     * This member holds the virtual address at which the first byte
     * of the segment resides in memory.
     */
    override val pVaddr: Elf64Addr,

    /**
     * On systems for which physical addressing is relevant, this member
     * is reserved for the segment's physical address.
     */
    override val pPaddr: Elf64Addr,

    /**
     * This member holds the number of bytes in the file image of the segment.
     */
    override val pFilesz: Elf64Xword,

    /**
     * This member holds the number of bytes in the memory image of the segment.
     */
    override val pMemsz: Elf64Xword,

    /**
     * This member holds the value to which the segments are aligned in memory and in the file.
     */
    override val pAlign: Elf64Xword,
) : ReadableStructure, ElfPhdr {
    companion object {
        fun parse(bytes: ByteArray, off: Int, ident: ElfIdentification): Elf64Phdr {
            val le = ident.eiData == ElfData.ELFDATA2LSB
            return Elf64Phdr(
                pType = ElfPType(bytes.u4(off + 0, le)),
                pFlags = ElfPFlags(bytes.u4(off + 4, le)),
                pOffset = Elf64Off(bytes.u8(off + 8, le)),
                pVaddr = Elf64Addr(bytes.u8(off + 16, le)),
                pPaddr = Elf64Addr(bytes.u8(off + 24, le)),
                pFilesz = Elf64Xword(bytes.u8(off + 32, le)),
                pMemsz = Elf64Xword(bytes.u8(off + 40, le)),
                pAlign = Elf64Xword(bytes.u8(off + 48, le))
            )
        }
    }
} 