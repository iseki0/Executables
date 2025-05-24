package space.iseki.executables.pe

import kotlinx.serialization.Serializable
import space.iseki.executables.common.Address32
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u2l
import space.iseki.executables.share.u4l
import kotlin.jvm.JvmStatic

@Serializable
data class SectionTableItem internal constructor(
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

            val section = SectionTableItem(
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

            // 基本验证
            section.validate()

            return section
        }
    }

    /**
     * 验证节表项的有效性
     *
     * @throws PEFileException 如果节表项无效
     */
    internal fun validate() {
        // 检查节名称是否有效
        if (name.isEmpty()) {
            throw PEFileException("Invalid section name: empty name")
        }

        // 检查节的特性标志
        if (characteristics.value == 0) {
            throw PEFileException(
                message = "Invalid section characteristics: no flags set",
                arguments = listOf("section_name" to name)
            )
        }

        // 检查节的虚拟大小和原始数据大小
        if (virtualSize == 0u && sizeOfRawData > 0u) {
            throw PEFileException(
                message = "Invalid section: virtual size is 0 but raw data size is not 0",
                arguments = listOf(
                    "section_name" to name,
                    "raw_data_size" to sizeOfRawData.toString()
                )
            )
        }

        // 如果节有原始数据，检查指针是否有效
        if (sizeOfRawData > 0u) {
            if (pointerToRawData.value == 0u) {
                throw PEFileException(
                    message = "Invalid section: has raw data but pointer to raw data is 0",
                    arguments = listOf("section_name" to name)
                )
            }

            // 检查原始数据指针是否对齐到文件对齐边界（通常是512字节）
            if (pointerToRawData.value % 512u != 0u) {
                throw PEFileException(
                    message = "Invalid section: pointer to raw data is not aligned to file alignment boundary",
                    arguments = listOf("section_name" to name)
                )
            }
        }

        // 检查重定位和行号信息的一致性
        if (numberOfRelocations > 0u && pointerToRelocations.value == 0u) {
            throw PEFileException(
                message = "Invalid section: has relocations but pointer to relocations is 0",
                arguments = listOf("section_name" to name)
            )
        }

        if (numberOfLinenumbers > 0u && pointerToLinenumbers.value == 0u) {
            throw PEFileException(
                message = "Invalid section: has line numbers but pointer to line numbers is 0",
                arguments = listOf("section_name" to name)
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
}
