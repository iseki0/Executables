package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure

@Serializable
sealed interface ElfPhdr : ReadableStructure {
    val pType: ElfPType
    val pOffset: Primitive
    val pVaddr: Primitive
    val pPaddr: Primitive
    val pFilesz: Primitive
    val pMemsz: Primitive
    val pFlags: ElfPFlags
    val pAlign: Primitive

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
