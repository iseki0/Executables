package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure

@Serializable
data class Elf32Ehdr(
    val eType: ElfType,
    val eMachine: ElfMachine,
    val eVersion: Elf32Word,
    val eEntry: Elf32Addr,
    val ePhoff: Elf32Off,
    val eShoff: Elf32Off,
    val eFlags: Elf32Word,
    val eEhsize: Elf32Half,
    val ePhentsize: Elf32Half,
    val ePhnum: Elf32Half,
    val eShentsize: Elf32Half,
    val eShnum: Elf32Half,
    val eShstrndx: Elf32Half,
) : ReadableStructure, ElfEhdr {
    companion object {
        fun parse(bytes: ByteArray, off: Int, ident: ElfIdentification): Elf32Ehdr {
            val le = ident.eiData == ElfData.ELFDATA2LSB
            return Elf32Ehdr(
                eType = ElfType(if (le) bytes.u2l(off + 16) else bytes.u2b(off + 16)),
                eMachine = ElfMachine(if (le) bytes.u2l(off + 18) else bytes.u2b(off + 18)),
                eVersion = Elf32Word(if (le) bytes.u4e(off + 20) else bytes.u4b(off + 20)),
                eEntry = Elf32Addr(if (le) bytes.u4e(off + 24) else bytes.u4b(off + 24)),
                ePhoff = Elf32Off(if (le) bytes.u4e(off + 28) else bytes.u4b(off + 28)),
                eShoff = Elf32Off(if (le) bytes.u4e(off + 32) else bytes.u4b(off + 32)),
                eFlags = Elf32Word(if (le) bytes.u4e(off + 36) else bytes.u4b(off + 36)),
                eEhsize = Elf32Half(if (le) bytes.u2l(off + 40) else bytes.u2b(off + 40)),
                ePhentsize = Elf32Half(if (le) bytes.u2l(off + 42) else bytes.u2b(off + 42)),
                ePhnum = Elf32Half(if (le) bytes.u2l(off + 44) else bytes.u2b(off + 44)),
                eShentsize = Elf32Half(if (le) bytes.u2l(off + 46) else bytes.u2b(off + 46)),
                eShnum = Elf32Half(if (le) bytes.u2l(off + 48) else bytes.u2b(off + 48)),
                eShstrndx = Elf32Half(if (le) bytes.u2l(off + 50) else bytes.u2b(off + 50))
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

