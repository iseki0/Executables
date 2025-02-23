package space.iseki.executables.elf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure

@Serializable
@SerialName("Elf32Phdr")
data class Elf32Phdr(
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
                pType = ElfPType(if (le) bytes.u4e(off) else bytes.u4b(off)),
                pOffset = Elf32Off(if (le) bytes.u4e(off + 4) else bytes.u4b(off + 4)),
                pVaddr = Elf32Addr(if (le) bytes.u4e(off + 8) else bytes.u4b(off + 8)),
                pPaddr = Elf32Addr(if (le) bytes.u4e(off + 12) else bytes.u4b(off + 12)),
                pFilesz = Elf32Word(if (le) bytes.u4e(off + 16) else bytes.u4b(off + 16)),
                pMemsz = Elf32Word(if (le) bytes.u4e(off + 20) else bytes.u4b(off + 20)),
                pFlags = ElfPFlags(if (le) bytes.u4e(off + 24) else bytes.u4b(off + 24)),
                pAlign = Elf32Word(if (le) bytes.u4e(off + 28) else bytes.u4b(off + 28))
            )
        }
    }

    override val fields: Map<String, Any>
        get() = mapOf(
            "pType" to pType,
            "pOffset" to pOffset,
            "pVaddr" to pVaddr,
            "pPaddr" to pPaddr,
            "pFilesz" to pFilesz,
            "pMemsz" to pMemsz,
            "pFlags" to pFlags,
            "pAlign" to pAlign
        )
} 