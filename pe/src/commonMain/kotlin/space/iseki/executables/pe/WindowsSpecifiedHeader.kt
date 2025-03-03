package space.iseki.executables.pe

import kotlinx.serialization.Serializable
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
data class WindowsSpecifiedHeader(
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

    override fun toString(): String = (fields.entries + rvaList.entries as Set<Map.Entry<String, Any>>).joinToString(
        "", "WindowsSpecifiedHeader(", ")"
    ) { (k, v) -> "   $k = $v,\n" }


    fun length(): Int {
        return if (magic == PE32Magic.PE32Plus) 112 - 24 else 96 - 28
    }

    companion object {
        @JvmStatic
        fun parse(bytes: ByteArray, offset: Int, magic: PE32Magic): WindowsSpecifiedHeader {
            val isPlus = magic == PE32Magic.PE32Plus
            val off = offset - if (isPlus) 24 else 28
            val imageBase =
                Address64(if (isPlus) bytes.u4l(off + 24).toLong() else bytes.u8l(off + 28).toLong())
            val sectionAlignment = bytes.u4l(off + 32)
            val fileAlignment = bytes.u4l(off + 36)
            val majorOperatingSystemVersion = bytes.u2l(off + 40)
            val minorOperatingSystemVersion = bytes.u2l(off + 42)
            val majorImageVersion = bytes.u2l(off + 44)
            val minorImageVersion = bytes.u2l(off + 46)
            val majorSubsystemVersion = bytes.u2l(off + 48)
            val minorSubsystemVersion = bytes.u2l(off + 50)
            val win32VersionValue = bytes.u4l(off + 52)
            val sizeOfImage = bytes.u4l(off + 56)
            val sizeOfHeaders = bytes.u4l(off + 60)
            val checkSum = bytes.u4l(off + 64)
            val subsystem = bytes.u2l(off + 68)
            val dllCharacteristics = bytes.u2l(off + 70)
            val sizeOfStackReserve = if (isPlus) bytes.u8l(off + 72) else bytes.u4l(off + 72).toULong()
            val sizeOfStackCommit = if (isPlus) bytes.u8l(off + 80) else bytes.u4l(off + 76).toULong()
            val sizeOfHeapReserve = if (isPlus) bytes.u8l(off + 88) else bytes.u4l(off + 80).toULong()
            val sizeOfHeapCommit = if (isPlus) bytes.u8l(off + 96) else bytes.u4l(off + 84).toULong()
            val loaderFlags = bytes.u4l(off + (if (isPlus) 104 else 88))
            val numberOfRvaAndSizes = bytes.u4l(off + (if (isPlus) 108 else 92)).toInt()
            val ddiOffset = if (isPlus) 112 else 96
            val exportTable = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 1)
            val importTable = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 2)
            val resourceTable = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 3)
            val exceptionTable = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 4)
            val certificateTable = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 5)
            val baseRelocationTable = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 6)
            val debug = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 7)
            val architecture = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 8)
            val globalPtr = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 9)
            val tlsTable = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 10)
            val loadConfigTable = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 11)
            val boundImport = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 12)
            val iat = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 13)
            val delayImportDescriptor = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 14)
            val clrRuntimeHeader = parseDdi(bytes, ddiOffset, numberOfRvaAndSizes, 15)
            return WindowsSpecifiedHeader(
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
                subsystem = WindowsSubsystems(subsystem.toShort()),
                dllCharacteristics = DllCharacteristics(dllCharacteristics),
                sizeOfStackReserve = sizeOfStackReserve,
                sizeOfStackCommit = sizeOfStackCommit,
                sizeOfHeapReserve = sizeOfHeapReserve,
                sizeOfHeapCommit = sizeOfHeapCommit,
                loaderFlags = loaderFlags,
                numbersOfRvaAndSizes = numberOfRvaAndSizes,
                exportTable = exportTable,
                importTable = importTable,
                resourceTable = resourceTable,
                exceptionTable = exceptionTable,
                certificateTable = certificateTable,
                baseRelocationTable = baseRelocationTable,
                debug = debug,
                architecture = architecture,
                globalPtr = globalPtr,
                tlsTable = tlsTable,
                loadConfigTable = loadConfigTable,
                boundImport = boundImport,
                iat = iat,
                delayImportDescriptor = delayImportDescriptor,
                clrRuntimeHeader = clrRuntimeHeader,
            )
        }

        private fun parseDdi(bytes: ByteArray, offset: Int, numbersOfRvaAndSizes: Int, index: Int): DataDirectoryItem {
            if (numbersOfRvaAndSizes < index) return DataDirectoryItem.ZERO
            return DataDirectoryItem.parse(bytes, offset + (index - 1) * 8)
        }
    }
}
