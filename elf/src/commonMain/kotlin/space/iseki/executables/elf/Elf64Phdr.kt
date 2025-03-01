package space.iseki.executables.elf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure

@Serializable
@SerialName("Elf64Phdr")
data class Elf64Phdr(
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
                pType = ElfPType(if (le) bytes.u4l(off) else bytes.u4b(off)),
                pFlags = ElfPFlags(if (le) bytes.u4l(off + 4) else bytes.u4b(off + 4)),
                pOffset = Elf64Off(if (le) bytes.u8l(off + 8) else bytes.u8b(off + 8)),
                pVaddr = Elf64Addr(if (le) bytes.u8l(off + 16) else bytes.u8b(off + 16)),
                pPaddr = Elf64Addr(if (le) bytes.u8l(off + 24) else bytes.u8b(off + 24)),
                pFilesz = Elf64Xword(if (le) bytes.u8l(off + 32) else bytes.u8b(off + 32)),
                pMemsz = Elf64Xword(if (le) bytes.u8l(off + 40) else bytes.u8b(off + 40)),
                pAlign = Elf64Xword(if (le) bytes.u8l(off + 48) else bytes.u8b(off + 48))
            )
        }
    }

    override val fields: Map<String, Any>
        get() = mapOf(
            "pType" to pType,
            "pFlags" to pFlags,
            "pOffset" to pOffset,
            "pVaddr" to pVaddr,
            "pPaddr" to pPaddr,
            "pFilesz" to pFilesz,
            "pMemsz" to pMemsz,
            "pAlign" to pAlign
        )
} 