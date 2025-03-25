package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure

@Serializable
sealed interface ElfShdr : ReadableStructure {
    val shName: ElfPrimitive
    val shType: ElfSType
    val shFlags: ElfSFlags
    val shAddr: ElfPrimitive
    val shOffset: ElfPrimitive
    val shSize: ElfPrimitive
    val shLink: ElfPrimitive
    val shInfo: ElfPrimitive
    val shAddralign: ElfPrimitive
    val shEntsize: ElfPrimitive
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
