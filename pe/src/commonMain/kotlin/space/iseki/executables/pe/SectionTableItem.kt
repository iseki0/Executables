package space.iseki.executables.pe

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u2l
import space.iseki.executables.share.u4l
import kotlin.jvm.JvmStatic

@Serializable
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
) : ReadableStructure {
    companion object {
        const val LENGTH = 40

        @JvmStatic
        fun parse(bytes: ByteArray, offset: Int): SectionTableItem {
            val name = bytes.copyOfRange(offset, offset + 8).decodeToString().trimEnd('\u0000')
            val virtualSize = bytes.u4l(offset + 8)
            val virtualAddress = Address32(bytes.u4l(offset + 12))
            val sizeOfRawData = bytes.u4l(offset + 16)
            val pointerToRawData = Address32(bytes.u4l(offset + 20))
            val pointerToRelocations = Address32(bytes.u4l(offset + 24))
            val pointerToLinenumbers = Address32(bytes.u4l(offset + 28))
            val numberOfRelocations = bytes.u2l(offset + 32)
            val numberOfLinenumbers = bytes.u2l(offset + 34)
            val characteristics = SectionFlags(bytes.u4l(offset + 36).toInt())
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

    override val fields
        get() = mapOf(
            "name" to name,
            "virtualSize" to virtualSize,
            "virtualAddress" to virtualAddress,
            "sizeOfRawData" to sizeOfRawData,
            "pointerToRawData" to pointerToRawData,
            "pointerToRelocations" to pointerToRelocations,
            "pointerToLinenumbers" to pointerToLinenumbers,
            "numberOfRelocations" to numberOfRelocations,
            "numberOfLinenumbers" to numberOfLinenumbers,
            "characteristics" to characteristics,
        )

    override fun toString(): String =
        fields.entries.joinToString("", prefix = "SectionTableItem(", postfix = ")") { (k, v) -> "   $k = $v,\n" }
}
