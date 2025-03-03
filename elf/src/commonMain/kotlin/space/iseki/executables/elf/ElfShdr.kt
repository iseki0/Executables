package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure

@Serializable
sealed interface ElfShdr : ReadableStructure {
    val shName: Primitive
    val shType: ElfSType
    val shFlags: ElfSFlags
    val shAddr: Primitive
    val shOffset: Primitive
    val shSize: Primitive
    val shLink: Primitive
    val shInfo: Primitive
    val shAddralign: Primitive
    val shEntsize: Primitive
    val name: String?

    override val fields: Map<String, Any>
        get() = mapOf(
            "shName" to shName,
            "shType" to shType,
            "shFlags" to shFlags,
            "shAddr" to shAddr,
            "shOffset" to shOffset,
            "shSize" to shSize,
            "shLink" to shLink,
            "shInfo" to shInfo,
            "shAddralign" to shAddralign,
            "shEntsize" to shEntsize,
            "name" to (name ?: "")
        )
}
