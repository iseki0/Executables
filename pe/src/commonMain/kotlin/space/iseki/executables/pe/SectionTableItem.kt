package space.iseki.executables.pe

import kotlin.jvm.JvmStatic

data class SectionTableItem(
    val name: String,
    val virtualSize: UInt,
    val virtualAddress: Address32,
    val sizeOfRawData: UInt,
    val pointerToRawData: Address32,
    val pointerToRelocations: Address32,
    val pointerToLinenumbers: Address32,
    val numberOfRelocations: UShort,
    val numberOfLinenumbers: UShort,
    val characteristics: SectionFlags,
) {
    companion object {
        const val LENGTH = 40

        @JvmStatic
        fun parse(bytes: ByteArray, offset: Int): SectionTableItem {
            val name = bytes.copyOfRange(offset, offset + 8).decodeToString().trimEnd('\u0000')
            val virtualSize = bytes.getUInt(offset + 8)
            val virtualAddress = Address32(bytes.getUInt(offset + 12))
            val sizeOfRawData = bytes.getUInt(offset + 16)
            val pointerToRawData = Address32(bytes.getUInt(offset + 20))
            val pointerToRelocations = Address32(bytes.getUInt(offset + 24))
            val pointerToLinenumbers = Address32(bytes.getUInt(offset + 28))
            val numberOfRelocations = bytes.getUShort(offset + 32)
            val numberOfLinenumbers = bytes.getUShort(offset + 34)
            val characteristics = SectionFlags(bytes.getUInt(offset + 36).toInt())
            return SectionTableItem(
                name = name,
                virtualSize = virtualSize,
                virtualAddress = virtualAddress,
                sizeOfRawData = sizeOfRawData,
                pointerToRawData = pointerToRawData,
                pointerToRelocations = pointerToRelocations,
                pointerToLinenumbers = pointerToLinenumbers,
                numberOfRelocations = numberOfRelocations,
                numberOfLinenumbers = numberOfLinenumbers,
                characteristics = characteristics,
            )
        }
    }

    override fun toString(): String {
        return """
            |SectionTableItem(
            |   name = $name,
            |   virtualSize = $virtualSize,
            |   virtualAddress = $virtualAddress,
            |   sizeOfRawData = $sizeOfRawData,
            |   pointerToRawData = $pointerToRawData,
            |   pointerToRelocations = $pointerToRelocations,
            |   pointerToLinenumbers = $pointerToLinenumbers,
            |   numberOfRelocations = $numberOfRelocations,
            |   numberOfLinenumbers = $numberOfLinenumbers,
            |   characteristics = $characteristics,
            |)
        """.trimMargin()
    }
}
