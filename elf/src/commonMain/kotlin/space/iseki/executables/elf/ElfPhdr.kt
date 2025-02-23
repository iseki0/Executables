package space.iseki.executables.elf

import kotlinx.serialization.Serializable

@Serializable
sealed interface ElfPhdr {
    val pType: ElfPType
    val pOffset: Primitive
    val pVaddr: Primitive
    val pPaddr: Primitive
    val pFilesz: Primitive
    val pMemsz: Primitive
    val pFlags: ElfPFlags
    val pAlign: Primitive
}
