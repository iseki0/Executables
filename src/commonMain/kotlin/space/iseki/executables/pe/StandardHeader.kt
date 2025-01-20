package space.iseki.executables.pe

import kotlin.jvm.JvmStatic

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
) {
    init {
        if (magic == PE32Magic.PE32) {
            require(baseOfData == Address32(0u)) { "baseOfData must be 0 for PE32" }
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

    fun length(): Int {
        return if (magic == PE32Magic.PE32) 28 else 24
    }

    companion object {
        const val MAX_LENGTH = 28

        @JvmStatic
        fun parse(bytes: ByteArray, offset: Int): StandardHeader {
            val magic = PE32Magic(bytes.getUShort(offset).toShort())
            val majorLinkerVersion = bytes[offset + 2]
            val minorLinkerVersion = bytes[offset + 3]
            val sizeOfCode = bytes.getUInt(offset + 4)
            val sizeOfInitializedData = bytes.getUInt(offset + 8)
            val sizeOfUninitializedData = bytes.getUInt(offset + 12)
            val addressOfEntryPoint = Address32(bytes.getUInt(offset + 16))
            val baseOfCode = Address32(bytes.getUInt(offset + 20))
            val baseOfData = if (magic == PE32Magic.PE32) Address32(bytes.getUInt(offset + 24)) else Address32(0u)
            return StandardHeader(
                magic,
                majorLinkerVersion,
                minorLinkerVersion,
                sizeOfCode,
                sizeOfInitializedData,
                sizeOfUninitializedData,
                addressOfEntryPoint,
                baseOfCode,
                baseOfData,
            )
        }
    }
}
