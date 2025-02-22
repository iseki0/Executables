package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure

@Serializable
data class Elf64Ehdr(
    val eType: ElfType,
    val eMachine: ElfMachine,
    val eVersion: Elf64Word,
    val eEntry: Elf64Addr,
    val ePhoff: Elf64Off,
    val eShoff: Elf64Off,
    val eFlags: Elf64Word,
    val eEhsize: Elf64Half,
    val ePhentsize: Elf64Half,
    val ePhnum: Elf64Half,
    val eShentsize: Elf64Half,
    val eShnum: Elf64Half,
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
