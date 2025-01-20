package space.iseki.executables.pe

import kotlin.jvm.JvmStatic

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
) {

    override fun toString(): String {
        return """
            |WindowsSpecifiedHeader(
            |   magic = $magic,
            |   imageBase = ${if (magic == PE32Magic.PE32) Address32(imageBase.rawValue.toUInt()) else imageBase},
            |   sectionAlignment = $sectionAlignment,
            |   fileAlignment = $fileAlignment,
            |   majorOperatingSystemVersion = $majorOperatingSystemVersion,
            |   minorOperatingSystemVersion = $minorOperatingSystemVersion,
            |   majorImageVersion = $majorImageVersion,
            |   minorImageVersion = $minorImageVersion,
            |   majorSubsystemVersion = $majorSubsystemVersion,
            |   minorSubsystemVersion = $minorSubsystemVersion,
            |   win32VersionValue = $win32VersionValue,
            |   sizeOfImage = $sizeOfImage,
            |   sizeOfHeaders = $sizeOfHeaders,
            |   checkSum = $checkSum,
            |   subsystem = $subsystem,
            |   dllCharacteristics = $dllCharacteristics,
            |   sizeOfStackReserve = $sizeOfStackReserve,
            |   sizeOfStackCommit = $sizeOfStackCommit,
            |   sizeOfHeapReserve = $sizeOfHeapReserve,
            |   sizeOfHeapCommit = $sizeOfHeapCommit,
            |   loaderFlags = $loaderFlags,
            |   numbersOfRvaAndSizes = $numbersOfRvaAndSizes,
            |   exportTable = $exportTable,
            |   importTable = $importTable,
            |   resourceTable = $resourceTable,
            |   exceptionTable = $exceptionTable,
            |   certificateTable = $certificateTable,
            |   baseRelocationTable = $baseRelocationTable,
            |   debug = $debug,
            |   architecture = $architecture,
            |   globalPtr = $globalPtr,
            |   tlsTable = $tlsTable,
            |   loadConfigTable = $loadConfigTable,
            |   boundImport = $boundImport,
            |   iat = $iat,
            |   delayImportDescriptor = $delayImportDescriptor,
            |   clrRuntimeHeader = $clrRuntimeHeader,
            |)
        """.trimMargin()
    }

    fun length(): Int {
        return if (magic == PE32Magic.PE32Plus) 112 - 24 else 96 - 28
    }

    companion object {
        @JvmStatic
        fun parse(bytes: ByteArray, offset: Int, magic: PE32Magic): WindowsSpecifiedHeader {
            val isPlus = magic == PE32Magic.PE32Plus
            val off = offset - if (isPlus) 24 else 28
            val imageBase =
                Address64(if (isPlus) bytes.getUInt(off + 24).toLong() else bytes.getULong(off + 28).toLong())
            val sectionAlignment = bytes.getUInt(off + 32)
            val fileAlignment = bytes.getUInt(off + 36)
            val majorOperatingSystemVersion = bytes.getUShort(off + 40)
            val minorOperatingSystemVersion = bytes.getUShort(off + 42)
            val majorImageVersion = bytes.getUShort(off + 44)
            val minorImageVersion = bytes.getUShort(off + 46)
            val majorSubsystemVersion = bytes.getUShort(off + 48)
            val minorSubsystemVersion = bytes.getUShort(off + 50)
            val win32VersionValue = bytes.getUInt(off + 52)
            val sizeOfImage = bytes.getUInt(off + 56)
            val sizeOfHeaders = bytes.getUInt(off + 60)
            val checkSum = bytes.getUInt(off + 64)
            val subsystem = bytes.getUShort(off + 68)
            val dllCharacteristics = bytes.getUShort(off + 70)
            val sizeOfStackReserve = if (isPlus) bytes.getULong(off + 72) else bytes.getUInt(off + 72).toULong()
            val sizeOfStackCommit = if (isPlus) bytes.getULong(off + 80) else bytes.getUInt(off + 76).toULong()
            val sizeOfHeapReserve = if (isPlus) bytes.getULong(off + 88) else bytes.getUInt(off + 80).toULong()
            val sizeOfHeapCommit = if (isPlus) bytes.getULong(off + 96) else bytes.getUInt(off + 84).toULong()
            val loaderFlags = bytes.getUInt(off + (if (isPlus) 104 else 88))
            val numberOfRvaAndSizes = bytes.getUInt(off + (if (isPlus) 108 else 92)).toInt()
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
                dllCharacteristics = DllCharacteristics(dllCharacteristics.toShort()),
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
