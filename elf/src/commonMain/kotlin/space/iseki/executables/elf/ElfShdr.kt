package space.iseki.executables.elf

import kotlinx.serialization.Serializable

@Serializable
sealed interface ElfShdr {
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
}
