package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure

@Serializable
sealed interface ElfPhdr : ReadableStructure {
    val pType: ElfPType
    val pOffset: ElfPrimitive
    val pVaddr: ElfPrimitive
    val pPaddr: ElfPrimitive
    val pFilesz: ElfPrimitive
    val pMemsz: ElfPrimitive
    val pFlags: ElfPFlags
    val pAlign: ElfPrimitive

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
