package space.iseki.executables.pe

import kotlinx.serialization.Serializable
import space.iseki.executables.common.Address32
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u2l
import space.iseki.executables.share.u4l
import kotlin.jvm.JvmStatic

/**
 * Represents the Standard (Optional) Header structure.
 *
 * @property magic The unsigned integer that identifies the state of the image file. The most common number is 0x10B,
 *                 which identifies it as a normal executable file. 0x107 identifies it as a ROM image,
 *                 and 0x20B identifies it as a PE32+ executable.
 * @property majorLinkerVersion The linker major version number.
 * @property minorLinkerVersion The linker minor version number.
 * @property sizeOfCode The size of the code (text) section, or the sum of all code sections if there are multiple sections.
 * @property sizeOfInitializedData The size of the initialized data section, or the sum of all such sections if there are multiple data sections.
 * @property sizeOfUninitializedData The size of the uninitialized data section (BSS), or the sum of all such sections if there are multiple BSS sections.
 * @property addressOfEntryPoint The address of the entry point relative to the image base when the executable file is loaded into memory.
 *                              For program images, this is the starting address. For device drivers, this is the address of the initialization function.
 *                              An entry point is optional for DLLs. When no entry point is present, this field must be zero.
 * @property baseOfCode The address that is relative to the image base of the beginning-of-code section when it is loaded into memory.
 * @property baseOfData The address that is relative to the image base of the beginning-of-data section when it is loaded into memory.
 *                      This field is only present in PE32 format, and is absent in PE32+.
 */
@Serializable
data class StandardHeader internal constructor(
    val magic: PE32Magic,
    val majorLinkerVersion: Byte,
    val minorLinkerVersion: Byte,
    val sizeOfCode: UInt,
    val sizeOfInitializedData: UInt,
    val sizeOfUninitializedData: UInt,
    val addressOfEntryPoint: Address32,
    val baseOfCode: Address32,
    val baseOfData: Address32,
) : ReadableStructure {
    init {
        if (magic == PE32Magic.PE32Plus) {
            require(baseOfData == Address32(0u)) { "baseOfData must be 0 for PE32+" }
        }
    }

    override val fields: Map<String, Any>
        get() = if (magic == PE32Magic.PE32) {
            mapOf(
                "magic" to magic,
                "majorLinkerVersion" to majorLinkerVersion,
                "minorLinkerVersion" to minorLinkerVersion,
                "sizeOfCode" to sizeOfCode,
                "sizeOfInitializedData" to sizeOfInitializedData,
                "sizeOfUninitializedData" to sizeOfUninitializedData,
                "addressOfEntryPoint" to addressOfEntryPoint,
                "baseOfCode" to baseOfCode,
                "baseOfData" to baseOfData,
            )
        } else {
            mapOf(
                "magic" to magic,
                "majorLinkerVersion" to majorLinkerVersion,
                "minorLinkerVersion" to minorLinkerVersion,
                "sizeOfCode" to sizeOfCode,
                "sizeOfInitializedData" to sizeOfInitializedData,
                "sizeOfUninitializedData" to sizeOfUninitializedData,
                "addressOfEntryPoint" to addressOfEntryPoint,
                "baseOfCode" to baseOfCode,
            )
        }

    fun length(): Int {
        return if (magic == PE32Magic.PE32) 28 else 24
    }

    /**
     * 验证标准头部的有效性
     *
     * @throws PEFileException 如果头部无效
     */
    internal fun validate() {
        // 检查魔数是否有效
        if (magic != PE32Magic.PE32 && magic != PE32Magic.PE32Plus) {
            throw PEFileException("Unsupported PE magic", "magic" to magic)
        }

//        // 检查代码段大小
//        if (sizeOfCode == 0u && magic != PE32Magic.PE32Plus) {
//            throw PEFileException("Invalid standard header: size of code is 0")
//        }
//
//        // 检查入口点地址
//        // 注意：DLL可以没有入口点，所以这里不做强制检查
//
//        // 检查代码基址
//        if (baseOfCode.value != 0u && sizeOfCode == 0u) {
//            throw PEFileException("Invalid standard header: base of code is not 0 but size of code is 0")
//        }

        // 检查数据基址（仅PE32）
        if (magic == PE32Magic.PE32) {
            if (baseOfData.value != 0u && sizeOfInitializedData == 0u && sizeOfUninitializedData == 0u) {
                throw PEFileException(
                    "Invalid standard header: base of data is not 0 but no data sections exist",
                    "base_of_data" to baseOfData,
                    "size_of_initialized_data" to sizeOfInitializedData,
                    "size_of_uninitialized_data" to sizeOfUninitializedData,
                )
            }
        } else {
            // PE32+
            if (baseOfData.value != 0u) {
                throw PEFileException(
                    "Invalid standard header: base of data must be 0 for PE32+",
                    "base_of_data" to baseOfData,
                )
            }
        }
    }

    companion object {
        const val MAX_LENGTH = 28

        @JvmStatic
        fun parse(bytes: ByteArray, offset: Int): StandardHeader {
            val magic = PE32Magic(bytes.u2l(offset).toShort())
            val majorLinkerVersion = bytes[offset + 2]
            val minorLinkerVersion = bytes[offset + 3]
            val sizeOfCode = bytes.u4l(offset + 4)
            val sizeOfInitializedData = bytes.u4l(offset + 8)
            val sizeOfUninitializedData = bytes.u4l(offset + 12)
            val addressOfEntryPoint = Address32(bytes.u4l(offset + 16))
            val baseOfCode = Address32(bytes.u4l(offset + 20))
            val baseOfData = if (magic == PE32Magic.PE32) Address32(bytes.u4l(offset + 24)) else Address32(0u)

            val header = StandardHeader(
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

            // 验证头部
            header.validate()

            return header
        }
    }
}
