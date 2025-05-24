package space.iseki.executables.pe

import kotlinx.serialization.Serializable
import space.iseki.executables.common.Address32
import space.iseki.executables.common.Address64
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u2l
import space.iseki.executables.share.u4l
import space.iseki.executables.share.u8l
import kotlin.jvm.JvmStatic

/**
 * Represents the Windows-Specific fields of the Optional Header.
 *
 * @property magic The format of the image file (PE32 or PE32+).
 * @property imageBase The preferred address of the first byte of image when loaded into memory; must be a multiple of 64 K.
 *                    The default for DLLs is 0x10000000. The default for Windows CE EXEs is 0x00010000.
 *                    The default for Windows NT, Windows 2000, Windows XP, Windows 95, Windows 98, and Windows Me is 0x00400000.
 * @property sectionAlignment The alignment (in bytes) of sections when they are loaded into memory.
 *                          It must be greater than or equal to FileAlignment.
 *                          The default is the page size for the architecture.
 * @property fileAlignment The alignment factor (in bytes) that is used to align the raw data of sections in the image file.
 *                       The value should be a power of 2 between 512 and 64 K, inclusive. The default is 512.
 *                       If the SectionAlignment is less than the architecture's page size,
 *                       then FileAlignment must match SectionAlignment.
 * @property majorOperatingSystemVersion The major version number of the required operating system.
 * @property minorOperatingSystemVersion The minor version number of the required operating system.
 * @property majorImageVersion The major version number of the image.
 * @property minorImageVersion The minor version number of the image.
 * @property majorSubsystemVersion The major version number of the subsystem.
 * @property minorSubsystemVersion The minor version number of the subsystem.
 * @property win32VersionValue Reserved, must be zero.
 * @property sizeOfImage The size (in bytes) of the image, including all headers, as the image is loaded in memory.
 *                      It must be a multiple of SectionAlignment.
 * @property sizeOfHeaders The combined size of an MS-DOS stub, PE header, and section headers rounded up to a multiple of FileAlignment.
 * @property checkSum The image file checksum. The algorithm for computing the checksum is incorporated into IMAGHELP.DLL.
 *                   The following are checked for validation at load time: all drivers, any DLL loaded at boot time,
 *                   and any DLL that is loaded into a critical Windows process.
 * @property subsystem The subsystem that is required to run this image. For more information, see [WindowsSubsystems].
 * @property dllCharacteristics For more information, see [DllCharacteristics].
 * @property sizeOfStackReserve The size of the stack to reserve. Only SizeOfStackCommit is committed;
 *                             the rest is made available one page at a time until the reserve size is reached.
 * @property sizeOfStackCommit The size of the stack to commit.
 * @property sizeOfHeapReserve The size of the local heap space to reserve. Only SizeOfHeapCommit is committed;
 *                            the rest is made available one page at a time until the reserve size is reached.
 * @property sizeOfHeapCommit The size of the local heap space to commit.
 * @property loaderFlags Reserved, must be zero.
 * @property numbersOfRvaAndSizes The number of data-directory entries in the remainder of the optional header.
 *                               Each describes a location and size.
 *
 * Data Directory Entries:
 * @property exportTable The export table address and size. For more information see .edata Section (Image Only).
 * @property importTable The import table address and size. For more information, see The .idata Section.
 * @property resourceTable The resource table address and size. For more information, see The .rsrc Section.
 * @property exceptionTable The exception table address and size. For more information, see The .pdata Section.
 * @property certificateTable The attribute certificate table address and size. For more information, see The Attribute Certificate Table (Image Only).
 * @property baseRelocationTable The base relocation table address and size. For more information, see The .reloc Section (Image Only).
 * @property debug The debug data starting address and size. For more information, see The .debug Section.
 * @property architecture Reserved, must be 0.
 * @property globalPtr The RVA of the value to be stored in the global pointer register. The size member of this structure must be set to zero.
 * @property tlsTable The thread local storage (TLS) table address and size. For more information, see The .tls Section.
 * @property loadConfigTable The load configuration table address and size. For more information, see The Load Configuration Structure (Image Only).
 * @property boundImport The bound import table address and size.
 * @property iat The import address table address and size. For more information, see Import Address Table.
 * @property delayImportDescriptor The delay import descriptor address and size. For more information, see Delay-Load Import Tables (Image Only).
 * @property clrRuntimeHeader The CLR runtime header address and size. For more information, see The .cormeta Section (Object Only).
 */
@Serializable
data class WindowsSpecifiedHeader internal constructor(
    val magic: PE32Magic,
    val imageBase: Address64,
    val sectionAlignment: UInt,
    val fileAlignment: UInt,
    val majorOperatingSystemVersion: UShort,
    val minorOperatingSystemVersion: UShort,
    val majorImageVersion: UShort,
    val minorImageVersion: UShort,
    val majorSubsystemVersion: UShort,
    val minorSubsystemVersion: UShort,
    val win32VersionValue: UInt,
    val sizeOfImage: UInt,
    val sizeOfHeaders: UInt,
    val checkSum: UInt,
    val subsystem: WindowsSubsystems,
    val dllCharacteristics: DllCharacteristics,
    val sizeOfStackReserve: ULong,
    val sizeOfStackCommit: ULong,
    val sizeOfHeapReserve: ULong,
    val sizeOfHeapCommit: ULong,
    val loaderFlags: UInt,
    val numbersOfRvaAndSizes: Int,
    val exportTable: DataDirectoryItem,
    val importTable: DataDirectoryItem,
    val resourceTable: DataDirectoryItem,
    val exceptionTable: DataDirectoryItem,
    val certificateTable: DataDirectoryItem,
    val baseRelocationTable: DataDirectoryItem,
    val debug: DataDirectoryItem,
    val architecture: DataDirectoryItem,
    val globalPtr: DataDirectoryItem,
    val tlsTable: DataDirectoryItem,
    val loadConfigTable: DataDirectoryItem,
    val boundImport: DataDirectoryItem,
    val iat: DataDirectoryItem,
    val delayImportDescriptor: DataDirectoryItem,
    val clrRuntimeHeader: DataDirectoryItem,
) : ReadableStructure {

    /**
     * Get all fields of the structure
     *
     * For convenient, the `magic` will be a field in the map, but it doesn't exist in the original structure.
     * @return a map of field names to their values
     */
    override val fields: Map<String, Any>
        get() = buildMap(capacity = 22 + numbersOfRvaAndSizes) {
            put("magic", magic)
            put("imageBase", imageBase)
            put("sectionAlignment", sectionAlignment)
            put("fileAlignment", fileAlignment)
            put("majorOperatingSystemVersion", majorOperatingSystemVersion)
            put("minorOperatingSystemVersion", minorOperatingSystemVersion)
            put("majorImageVersion", majorImageVersion)
            put("minorImageVersion", minorImageVersion)
            put("majorSubsystemVersion", majorSubsystemVersion)
            put("minorSubsystemVersion", minorSubsystemVersion)
            put("win32VersionValue", win32VersionValue)
            put("sizeOfImage", sizeOfImage)
            put("sizeOfHeaders", sizeOfHeaders)
            put("checkSum", checkSum)
            put("subsystem", subsystem)
            put("dllCharacteristics", dllCharacteristics)
            put("sizeOfStackReserve", sizeOfStackReserve)
            put("sizeOfStackCommit", sizeOfStackCommit)
            put("sizeOfHeapReserve", sizeOfHeapReserve)
            put("sizeOfHeapCommit", sizeOfHeapCommit)
            put("loaderFlags", loaderFlags)
            put("numbersOfRvaAndSizes", numbersOfRvaAndSizes)
            fillRvaMap()
        }

    private fun MutableMap<String, in DataDirectoryItem>.fillRvaMap() {
        if (numbersOfRvaAndSizes > 0) put("exportTable", exportTable)
        if (numbersOfRvaAndSizes > 1) put("importTable", importTable)
        if (numbersOfRvaAndSizes > 2) put("resourceTable", resourceTable)
        if (numbersOfRvaAndSizes > 3) put("exceptionTable", exceptionTable)
        if (numbersOfRvaAndSizes > 4) put("certificateTable", certificateTable)
        if (numbersOfRvaAndSizes > 5) put("baseRelocationTable", baseRelocationTable)
        if (numbersOfRvaAndSizes > 6) put("debug", debug)
        if (numbersOfRvaAndSizes > 7) put("architecture", architecture)
        if (numbersOfRvaAndSizes > 8) put("globalPtr", globalPtr)
        if (numbersOfRvaAndSizes > 9) put("tlsTable", tlsTable)
        if (numbersOfRvaAndSizes > 10) put("loadConfigTable", loadConfigTable)
        if (numbersOfRvaAndSizes > 11) put("boundImport", boundImport)
        if (numbersOfRvaAndSizes > 12) put("iat", iat)
        if (numbersOfRvaAndSizes > 13) put("delayImportDescriptor", delayImportDescriptor)
        if (numbersOfRvaAndSizes > 14) put("clrRuntimeHeader", clrRuntimeHeader)
    }

    val rvaList
        get() = buildMap(numbersOfRvaAndSizes) { fillRvaMap() }


    fun length(): Int {
        return if (magic == PE32Magic.PE32Plus) 112 - 24 else 96 - 28
    }

    companion object {
        @JvmStatic
        fun parse(bytes: ByteArray, offset: Int, magic: PE32Magic): WindowsSpecifiedHeader {
            val isPE32Plus = magic == PE32Magic.PE32Plus
            val imageBase: Address64
            var pos = offset
            if (isPE32Plus) {
                imageBase = Address64(bytes.u8l(pos))
                pos += 8
            } else {
                imageBase = Address64(bytes.u4l(pos).toULong())
                pos += 4
            }
            val sectionAlignment = bytes.u4l(pos)
            pos += 4
            val fileAlignment = bytes.u4l(pos)
            pos += 4
            val majorOperatingSystemVersion = bytes.u2l(pos)
            pos += 2
            val minorOperatingSystemVersion = bytes.u2l(pos)
            pos += 2
            val majorImageVersion = bytes.u2l(pos)
            pos += 2
            val minorImageVersion = bytes.u2l(pos)
            pos += 2
            val majorSubsystemVersion = bytes.u2l(pos)
            pos += 2
            val minorSubsystemVersion = bytes.u2l(pos)
            pos += 2
            val win32VersionValue = bytes.u4l(pos)
            pos += 4
            val sizeOfImage = bytes.u4l(pos)
            pos += 4
            val sizeOfHeaders = bytes.u4l(pos)
            pos += 4
            val checkSum = bytes.u4l(pos)
            pos += 4
            val subsystem = WindowsSubsystems(bytes.u2l(pos).toShort())
            pos += 2
            val dllCharacteristics = DllCharacteristics(bytes.u2l(pos))
            pos += 2
            val sizeOfStackReserve: ULong
            val sizeOfStackCommit: ULong
            val sizeOfHeapReserve: ULong
            val sizeOfHeapCommit: ULong
            if (isPE32Plus) {
                sizeOfStackReserve = bytes.u8l(pos)
                pos += 8
                sizeOfStackCommit = bytes.u8l(pos)
                pos += 8
                sizeOfHeapReserve = bytes.u8l(pos)
                pos += 8
                sizeOfHeapCommit = bytes.u8l(pos)
                pos += 8
            } else {
                sizeOfStackReserve = bytes.u4l(pos).toULong()
                pos += 4
                sizeOfStackCommit = bytes.u4l(pos).toULong()
                pos += 4
                sizeOfHeapReserve = bytes.u4l(pos).toULong()
                pos += 4
                sizeOfHeapCommit = bytes.u4l(pos).toULong()
                pos += 4
            }
            val loaderFlags = bytes.u4l(pos)
            pos += 4
            val numberOfRvaAndSizes = bytes.u4l(pos).toInt()
            pos += 4
            val dataDirectories = Array(16) {
                if (it < numberOfRvaAndSizes && pos + 8 <= bytes.size) {
                    val virtualAddress = Address32(bytes.u4l(pos))
                    val size = bytes.u4l(pos + 4)
                    pos += 8
                    DataDirectoryItem(virtualAddress.value, size)
                } else {
                    DataDirectoryItem.ZERO
                }
            }
            val header = WindowsSpecifiedHeader(
                magic = magic,
                imageBase = imageBase,
                sectionAlignment = sectionAlignment,
                fileAlignment = fileAlignment,
                majorOperatingSystemVersion = majorOperatingSystemVersion,
                minorOperatingSystemVersion = minorOperatingSystemVersion,
                majorImageVersion = majorImageVersion,
                minorImageVersion = minorImageVersion,
                majorSubsystemVersion = majorSubsystemVersion,
                minorSubsystemVersion = minorSubsystemVersion,
                win32VersionValue = win32VersionValue,
                sizeOfImage = sizeOfImage,
                sizeOfHeaders = sizeOfHeaders,
                checkSum = checkSum,
                subsystem = subsystem,
                dllCharacteristics = dllCharacteristics,
                sizeOfStackReserve = sizeOfStackReserve,
                sizeOfStackCommit = sizeOfStackCommit,
                sizeOfHeapReserve = sizeOfHeapReserve,
                sizeOfHeapCommit = sizeOfHeapCommit,
                loaderFlags = loaderFlags,
                numbersOfRvaAndSizes = numberOfRvaAndSizes,
                exportTable = dataDirectories[0],
                importTable = dataDirectories[1],
                resourceTable = dataDirectories[2],
                exceptionTable = dataDirectories[3],
                certificateTable = dataDirectories[4],
                baseRelocationTable = dataDirectories[5],
                debug = dataDirectories[6],
                architecture = dataDirectories[7],
                globalPtr = dataDirectories[8],
                tlsTable = dataDirectories[9],
                loadConfigTable = dataDirectories[10],
                boundImport = dataDirectories[11],
                iat = dataDirectories[12],
                delayImportDescriptor = dataDirectories[13],
                clrRuntimeHeader = dataDirectories[14],
            )

            // 验证头部
            header.validate()

            return header
        }
    }

    /**
     * 验证Windows特定头部的有效性
     *
     * @throws PEFileException 如果头部无效
     */
    internal fun validate() {
        // 检查节对齐和文件对齐
        if (sectionAlignment < fileAlignment) {
            throw PEFileException(
                message = "Invalid Windows header: section alignment is less than file alignment",
                arguments = listOf(
                    "section_alignment" to sectionAlignment.toString(),
                    "file_alignment" to fileAlignment.toString()
                )
            )
        }

        // 检查文件对齐是否是2的幂次方
        if (fileAlignment == 0u || (fileAlignment and (fileAlignment - 1u)) != 0u) {
            throw PEFileException(
                message = "Invalid Windows header: file alignment is not a power of 2",
                arguments = listOf("file_alignment" to fileAlignment.toString())
            )
        }

        // 检查文件对齐是否在有效范围内（通常是512到64K）
        if (fileAlignment < 512u || fileAlignment > 65536u) {
            throw PEFileException(
                message = "Invalid Windows header: file alignment is out of range (512-65536)",
                arguments = listOf("file_alignment" to fileAlignment.toString())
            )
        }

        // 检查节对齐是否是2的幂次方
        if (sectionAlignment == 0u || (sectionAlignment and (sectionAlignment - 1u)) != 0u) {
            throw PEFileException(
                message = "Invalid Windows header: section alignment is not a power of 2",
                arguments = listOf("section_alignment" to sectionAlignment.toString())
            )
        }

        // 检查镜像基址是否对齐到64K边界
        if (imageBase.value % 65536u != 0uL) {
            throw PEFileException(
                message = "Invalid Windows header: image base is not aligned to 64K boundary",
                arguments = listOf("image_base" to imageBase.value.toString())
            )
        }

        // 检查镜像大小是否对齐到节对齐边界
        if (sizeOfImage % sectionAlignment != 0u) {
            throw PEFileException(
                message = "Invalid Windows header: size of image is not aligned to section alignment",
                arguments = listOf(
                    "size_of_image" to sizeOfImage.toString(),
                    "section_alignment" to sectionAlignment.toString()
                )
            )
        }

        // 检查头部大小是否对齐到文件对齐边界
        if (sizeOfHeaders % fileAlignment != 0u) {
            throw PEFileException(
                message = "Invalid Windows header: size of headers is not aligned to file alignment",
                arguments = listOf(
                    "size_of_headers" to sizeOfHeaders.toString(),
                    "file_alignment" to fileAlignment.toString()
                )
            )
        }

        // 检查子系统版本
        if (majorSubsystemVersion == 0.toUShort() && minorSubsystemVersion == 0.toUShort()) {
            throw PEFileException(
                message = "Invalid Windows header: subsystem version is 0.0",
                arguments = listOf(
                    "major_version" to majorSubsystemVersion.toString(),
                    "minor_version" to minorSubsystemVersion.toString()
                )
            )
        }

        // 检查数据目录数量
        if (numbersOfRvaAndSizes < 0 || numbersOfRvaAndSizes > 16) {
            throw PEFileException(
                message = "Invalid Windows header: number of RVA and sizes is out of range (0-16)",
                arguments = listOf("count" to numbersOfRvaAndSizes.toString())
            )
        }

        // 检查保留字段
        if (win32VersionValue != 0u) {
            throw PEFileException(
                message = "Invalid Windows header: win32VersionValue is not zero",
                arguments = listOf("value" to win32VersionValue.toString())
            )
        }

        if (loaderFlags != 0u) {
            throw PEFileException(
                message = "Invalid Windows header: loaderFlags is not zero",
                arguments = listOf("value" to loaderFlags.toString())
            )
        }
    }
}
