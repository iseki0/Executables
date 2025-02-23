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
}
