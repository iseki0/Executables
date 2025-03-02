package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure

@Serializable
sealed interface ElfEhdr : ReadableStructure {
    val eType: ElfType
    val eMachine: ElfMachine
    val eVersion: Primitive
    val eEntry: Primitive
    val ePhoff: Primitive
    val eShoff: Primitive
    val eFlags: Primitive
    val eEhsize: Primitive
    val ePhentsize: Primitive
    val ePhnum: Primitive
    val eShentsize: Primitive
    val eShnum: Primitive
    val eShstrndx: Primitive

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
