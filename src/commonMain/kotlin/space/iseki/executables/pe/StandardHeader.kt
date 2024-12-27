package space.iseki.executables.pe

import kotlinx.serialization.Serializable

@Serializable
data class StandardHeader(
    val magic: PE32Magic,
    val majorLinkerVersion: Byte,
    val minorLinkerVersion: Byte,
    val sizeOfCode: UInt,
    val sizeOfInitializedData: UInt,
    val sizeOfUninitializedData: UInt,
    val addressOfEntryPoint: Address32,
    val baseOfCode: Address32,
    val baseOfData: Address32,
){
    init {
        if (magic == PE32Magic.PE32){
            require(baseOfData == Address32(0)) { "baseOfData must be 0 for PE32" }
        }
    }
    override fun toString(): String {
        return """
            |StandardHeader(
            |   magic = $magic,
            |   majorLinkerVersion = $majorLinkerVersion,
            |   minorLinkerVersion = $minorLinkerVersion,
            |   sizeOfCode = $sizeOfCode,
            |   sizeOfInitializedData = $sizeOfInitializedData,
            |   sizeOfUninitializedData = $sizeOfUninitializedData,
            |   addressOfEntryPoint = $addressOfEntryPoint,
            |   baseOfCode = $baseOfCode,
            |   baseOfData = ${if (magic == PE32Magic.PE32) "N/A" else baseOfData},
            |)
        """.trimMargin()
    }
}
