package space.iseki.executables.pe

import kotlinx.serialization.Serializable
import space.iseki.executables.common.Address32
import space.iseki.executables.common.Address32Array
import space.iseki.executables.common.DataAccessor
import space.iseki.executables.common.EOFException
import space.iseki.executables.common.ExportSymbol
import space.iseki.executables.common.ExportSymbolContainer
import space.iseki.executables.common.FileFormat
import space.iseki.executables.common.IOException
import space.iseki.executables.common.ImportSymbol
import space.iseki.executables.common.ImportSymbolContainer
import space.iseki.executables.common.OpenedFile
import space.iseki.executables.common.ReadableSection
import space.iseki.executables.common.ReadableSectionContainer
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.common.VirtualMemoryReadable
import space.iseki.executables.common.toAddr
import space.iseki.executables.pe.vi.PEVersionInfo
import space.iseki.executables.pe.vi.locateVersionInfo
import space.iseki.executables.pe.vi.parseVersionData
import space.iseki.executables.share.MemReader
import space.iseki.executables.share.toUnmodifiableList
import space.iseki.executables.share.u2l
import space.iseki.executables.share.u4l
import space.iseki.executables.share.u8l
import kotlin.math.min

class PEFile private constructor(
    val coffHeader: CoffHeader,
    val standardHeader: StandardHeader,
    val windowsHeader: WindowsSpecifiedHeader,
    val sectionTable: List<SectionTableItem>,
    private val dataAccessor: DataAccessor,
) : AutoCloseable, OpenedFile, ReadableSectionContainer, ImportSymbolContainer, ExportSymbolContainer,
    VirtualMemoryReadable {

    /**
     * Represents a summary of the pe file headers.
     *
     * @property coffHeader the coff header
     * @property standardHeader the standard header
     * @property windowsHeader the windows specific header
     * @property sectionTable the list of section table items
     */
    @Serializable
    class Summary internal constructor(
        val coffHeader: CoffHeader,
        val standardHeader: StandardHeader,
        val windowsHeader: WindowsSpecifiedHeader,
        val sectionTable: List<SectionTableItem>,
    )

    override val rootHeaders: Map<String, ReadableStructure>
        get() = mapOf(
            "coffHeader" to coffHeader,
            "standardHeader" to standardHeader,
            "windowsHeader" to windowsHeader,
        )

    private val vm = MemReader(dataAccessor).apply {
        sectionTable.sortedBy { it.virtualAddress }.forEach {
            mapMemory(
                vOff = it.virtualAddress.value.toULong(),
                fOff = it.pointerToRawData.value.toULong(),
                fSize = min(it.sizeOfRawData.toULong(), it.virtualSize.toULong()),
            )
        }
    }

    /**
     * Returns a summary of the pe file headers.
     *
     * @return a [Summary] instance
     */
    val summary: Summary = Summary(coffHeader, standardHeader, windowsHeader, sectionTable)

    companion object : FileFormat<PEFile> {
        override fun toString(): String = "PE"
        private const val PE_SIGNATURE_LE = 0x00004550

        /**
         * Opens a pe file from the given data accessor.
         *
         * @param accessor the data accessor to read the file
         * @return a [PEFile] instance
         * @throws PEFileException if the file is not a valid pe file
         */
        @OptIn(ExperimentalStdlibApi::class)
        @Throws(IOException::class)
        override fun open(accessor: DataAccessor): PEFile {
            var pos = 0x3cL
            try {
                val signatureBuffer = byteArrayOf(0, 0, 0, 0)
                accessor.readFully(pos, signatureBuffer)
                pos = signatureBuffer.u4l(0).toLong()

                if (pos <= 0 || pos > 0x1000) {
                    throw PEFileException("PE signature offset out of range", "offset" to "0x${pos.toString(16)}")
                }

                accessor.readFully(pos, signatureBuffer)
                val readSignature = signatureBuffer.u4l(0)
                if (readSignature != PE_SIGNATURE_LE.toUInt()) {
                    throw PEFileException("Not a PE file, bad magic", "magic" to "0x${readSignature.toHexString()}")
                }
            } catch (e: EOFException) {
                throw PEFileException("Not a PE file, unexpected EOF during read PE magic", cause = e)
            }
            val coffHeader: CoffHeader
            try {
                val coffBuffer = ByteArray(CoffHeader.LENGTH)
                pos += 4
                accessor.readFully(pos, coffBuffer)
                coffHeader = CoffHeader.parse(coffBuffer, 0)
            } catch (e: EOFException) {
                throw PEFileException("Unexpected EOF during read COFF header", cause = e)
            }

            if (coffHeader.numbersOfSections == 0.toUShort()) {
                throw PEFileException("No sections found", "section_count" to coffHeader.numbersOfSections)
            }
            if (coffHeader.numbersOfSections > 96.toUShort()) {
                throw PEFileException("Too many sections", "section_count" to coffHeader.numbersOfSections)
            }

            if (coffHeader.sizeOfOptionalHeader < 28.toUShort()) {
                throw PEFileException("Optional header size too small", "size" to coffHeader.sizeOfOptionalHeader)
            }

            val standardHeader: StandardHeader
            val optionalHeader: WindowsSpecifiedHeader
            try {
                val optionalHeaderBuffer = ByteArray(coffHeader.sizeOfOptionalHeader.toULong().toInt())
                pos += CoffHeader.LENGTH
                accessor.readFully(pos, optionalHeaderBuffer)
                standardHeader = StandardHeader.parse(optionalHeaderBuffer, 0)

                if (standardHeader.magic != PE32Magic.PE32 && standardHeader.magic != PE32Magic.PE32Plus) {
                    throw PEFileException("Unsupported PE magic", "magic" to standardHeader.magic)
                }

                optionalHeader =
                    WindowsSpecifiedHeader.parse(optionalHeaderBuffer, standardHeader.length(), standardHeader.magic)

                if (optionalHeader.numbersOfRvaAndSizes > 16) {
                    throw PEFileException("Too many data directories", "count" to optionalHeader.numbersOfRvaAndSizes)
                }
            } catch (e: IndexOutOfBoundsException) {
                throw PEFileException(
                    "IOBE during reading optional header, maybe the sizeOfOptionalHeader in COFF header is too small",
                    "coff_header" to coffHeader,
                    cause = e,
                )
            } catch (e: EOFException) {
                throw PEFileException(
                    "Unexpected EOF during read optional header",
                    "position" to Address32(pos.toUInt()),
                    "size" to coffHeader.sizeOfOptionalHeader,
                    "coff_header" to coffHeader,
                    cause = e,
                )
            }
            val sectionTableData = ByteArray(coffHeader.numbersOfSections.toInt() * SectionTableItem.LENGTH)
            pos += coffHeader.sizeOfOptionalHeader.toULong().toLong()
            try {
                accessor.readFully(pos, sectionTableData)
            } catch (e: EOFException) {
                throw PEFileException(
                    "Unexpected EOF during read section table",
                    "position" to pos,
                    "size" to sectionTableData.size,
                    cause = e,
                )
            }
            val sectionTableItemArray = Array(coffHeader.numbersOfSections.toInt()) {
                val off = it * SectionTableItem.LENGTH
                SectionTableItem.parse(sectionTableData, off)
            }

            for (section in sectionTableItemArray) {
                if (section.virtualSize == 0u) {
                    throw PEFileException("Section has zero virtual size", "section_name" to section.name)
                }

                if (section.virtualAddress.value % 0x1000u != 0u) {
                    throw PEFileException(
                        "Section is not aligned to 4K boundary",
                        "section_name" to section.name,
                        "virtual_address" to section.virtualAddress,
                    )
                }

                if (section.sizeOfRawData > 0u && section.pointerToRawData.value == 0u) {
                    throw PEFileException(
                        "Section has raw data but no pointer to raw data",
                        "section_name" to section.name,
                    )
                }
            }

            return PEFile(
                coffHeader = coffHeader,
                standardHeader = standardHeader,
                windowsHeader = optionalHeader,
                sectionTable = sectionTableItemArray.toUnmodifiableList(),
                dataAccessor = accessor,
            )
        }
    }

    /**
     * Closes the underlying data accessor.
     */
    override fun close() {
        dataAccessor.close()
    }

    private val rsrcRva: Address32 = windowsHeader.resourceTable.virtualAddress
    val resourceRoot: ResourceNode = ResourceRootNode()

    private fun readResourceDirectoryChildren(
        numberOfNamedEntries: UShort,
        numberOfIdEntries: UShort,
        dirNodeAddr: Address32,
    ): List<ResourceNode> {
        val totalNodes = (numberOfNamedEntries + numberOfIdEntries).coerceAtMost(Int.MAX_VALUE.toUInt() / 8u).toInt()
        val buf = ByteArray(8 * totalNodes)
        readVirtualMemory(dirNodeAddr + 16, buf, 0, buf.size)
        return buildList(capacity = totalNodes) {
            for (off in buf.indices step 8) {
                val nameOrId = buf.u4l(off)
                val dataRva = Address32(buf.u4l(off + 4))
                add(readSubNode(dataRva, nameOrId))
            }
        }
    }

    private fun readSubNode(dataRva: Address32, nameOrId: UInt): ResourceNode {
        val name: String
        val resourceID: UInt
        if (nameOrId < 0x80000000u) {
            // by id
            resourceID = nameOrId
            name = "<ID:$nameOrId>"
        } else {
            // by name
            val namePtr = rsrcRva + (nameOrId and 0x7FFFFFFFu)
            val lenBuf = ByteArray(2)
            readVirtualMemory(namePtr, lenBuf, 0, lenBuf.size)
            val nameLen = lenBuf.u2l(0)
            val nameBuf = ByteArray(nameLen.toInt())
            readVirtualMemory(namePtr + 2, nameBuf, 0, nameBuf.size)
            val charArray = CharArray(nameLen.toInt())
            nameBuf.forEachIndexed { index, byte -> charArray[index] = byte.toInt().toChar() }
            resourceID = 0u
            name = charArray.concatToString()
        }
        if ((dataRva and 0x80000000u).value != 0u) {
            // directory
            return ResourceDirectory(name, resourceID, dataRva and 0x7FFFFFFFu)
        } else {
            // file
            val fileBuf = ByteArray(16)
            readVirtualMemory(dataRva + rsrcRva, fileBuf, 0, fileBuf.size)
            val contentRva = Address32(fileBuf.u4l(0))
            val size = fileBuf.u4l(4)
            val codePage = CodePage(fileBuf.u4l(8))
            return ResourceFile(name, resourceID, size, dataRva, codePage, contentRva)
        }
    }

    private fun readResourceDirectoryNode(dataRva: Address32): List<ResourceNode> {
        if (rsrcRva.value == 0u) return emptyList()
        val buf = ByteArray(16)
        readVirtualMemory(dataRva + rsrcRva, buf, 0, buf.size)
        val characteristics = buf.u4l(0)
        if (characteristics != 0u) throw PEFileException(
            "Invalid resource directory node, characteristics is not 0",
            "data_rva" to dataRva,
        )
//        val timeDateStamp = TimeDataStamp32(buf.u4l(4))
//        val majorVersion = buf.u2l(8)
//        val minorVersion = buf.u2l(10)
        val numberOfNamedEntries = buf.u2l(12)
        val numberOfIdEntries = buf.u2l(14)
        return readResourceDirectoryChildren(numberOfNamedEntries, numberOfIdEntries, dataRva + rsrcRva)
    }

    internal inner class ResourceRootNode : ResourceNode {

        override fun getPEFile(): PEFile = this@PEFile

        override val name: String
            get() = "<root>"
        override val id: UInt
            get() = 0u
        override val dataRva: Address32
            get() = Address32(0u)

        /**
         * Indicates whether this node represents a file.
         *
         * @return false as root node is not a file
         */
        override fun isFile(): Boolean = false

        /**
         * Returns the children of the root resource node.
         *
         * @return a list of resource nodes
         */
        override fun listChildren(): List<ResourceNode> {
            return readResourceDirectoryNode(dataRva)
        }

        /**
         * Returns the string representation of the root resource node.
         *
         * @return a string representation
         */
        override fun toString(): String = "<ROOT>"
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ResourceRootNode

            if (getPEFile() != other.getPEFile()) return false
            if (dataRva != other.dataRva) return false

            return true
        }

        override fun hashCode(): Int {
            var result = getPEFile().hashCode()
            result = 31 * result + dataRva.hashCode()
            return result
        }

    }

    internal inner class ResourceDirectory(
        override val name: String,
        override val id: UInt,
        override val dataRva: Address32,
    ) : ResourceNode {
        override fun getPEFile(): PEFile = this@PEFile

        /**
         * Indicates whether this node represents a file.
         *
         * @return false as directory is not a file
         */
        override fun isFile(): Boolean = false

        /**
         * Returns the children of this resource directory.
         *
         * @return a list of resource nodes
         */
        override fun listChildren(): List<ResourceNode> {
            return readResourceDirectoryNode(dataRva)
        }

        /**
         * Returns the string representation of this resource directory.
         *
         * @return a string representation
         */
        override fun toString(): String = "<DIR:${if (id == 0u) name else "ID=$id"}> @$dataRva"
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ResourceDirectory

            if (dataRva != other.dataRva) return false
            if (getPEFile() != other.getPEFile()) return false

            return true
        }

        override fun hashCode(): Int {
            var result = dataRva.hashCode()
            result = 31 * result + getPEFile().hashCode()
            return result
        }


    }

    internal inner class ResourceFile(
        override val name: String,
        override val id: UInt,
        override val size: UInt,
        override val dataRva: Address32,
        override val codePage: CodePage,
        val contentRva: Address32,
    ) : ResourceNode {
        override fun getPEFile(): PEFile = this@PEFile
        override fun isFile(): Boolean = true
        override fun toString(): String =
            "<FILE:${if (id == 0u) name else "ID=$id"}, CodePage=$codePage, Size=$size, ContentRVA=$contentRva> @$dataRva"


        override fun readAllBytes(): ByteArray {
            val buf = ByteArray(size.toInt())
            readVirtualMemory(contentRva, buf, 0, buf.size)
            return buf
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ResourceFile

            if (dataRva != other.dataRva) return false
            if (getPEFile() != other.getPEFile()) return false

            return true
        }

        override fun hashCode(): Int {
            var result = dataRva.hashCode()
            result = 31 * result + getPEFile().hashCode()
            return result
        }
    }

    val versionInfo: PEVersionInfo? by lazy {
        locateVersionInfo(this)?.readAllBytes()?.let { parseVersionData(it, 0) }
    }

    /**
     * Returns the sections of the PE file.
     *
     * @return a list of sections, unmodifiable
     */
    override val sections: List<Section> = object : AbstractList<Section>() {
        override val size: Int
            get() = sectionTable.size

        override fun get(index: Int): Section = Section(sectionTable[index])

    }

    override fun toString(): String {
        return "PEFile(dataAccessor=$dataAccessor)"
    }

    /**
     * Represents a PE section.
     */
    inner class Section internal constructor(
        val tableItem: SectionTableItem,
    ) : ReadableSection {
        override val name: String get() = tableItem.name
        val virtualSize: UInt get() = tableItem.virtualSize
        val virtualAddress: Address32 get() = tableItem.virtualAddress
        val sizeOfRawData: UInt get() = tableItem.sizeOfRawData
        val pointerToRawData: Address32 get() = tableItem.pointerToRawData
        val pointerToRelocations: Address32 get() = tableItem.pointerToRelocations
        val pointerToLinenumbers: Address32 get() = tableItem.pointerToLinenumbers
        val numberOfRelocations: UShort get() = tableItem.numberOfRelocations
        val numberOfLinenumbers: UShort get() = tableItem.numberOfLinenumbers
        val characteristics: SectionFlags get() = tableItem.characteristics

        override val size: Long
            get() = sizeOfRawData.toLong()

        override val header: ReadableStructure
            get() = tableItem

        override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
            DataAccessor.checkReadBounds(pos, buf, off, len)
            return dataAccessor.readAtMost(
                pos = pos + pointerToRawData.value.toLong(),
                buf = buf,
                off = off,
                len = min(len, sizeOfRawData.toInt()),
            )
        }

        /**
         * Returns a string representation of the section.
         *
         * @return a string representation
         */
        override fun toString(): String {
            val flagsStr = buildString {
                if (SectionFlags.IMAGE_SCN_MEM_READ in characteristics) append('R')
                if (SectionFlags.IMAGE_SCN_MEM_WRITE in characteristics) append('W')
                if (SectionFlags.IMAGE_SCN_MEM_EXECUTE in characteristics) append('X')
                if (SectionFlags.IMAGE_SCN_CNT_CODE in characteristics) append('C')
                if (SectionFlags.IMAGE_SCN_CNT_INITIALIZED_DATA in characteristics) append('I')
                if (SectionFlags.IMAGE_SCN_CNT_UNINITIALIZED_DATA in characteristics) append('U')
            }
            return "Section[$name, VirtualSize=$virtualSize, VirtualAddress=$virtualAddress, RawSize=$sizeOfRawData, RawOffset=$pointerToRawData, Flags=$flagsStr]"
        }

    }

    /**
     * Get all import symbols from the PE file
     */
    override val importSymbols: List<ImportSymbol> by lazy {
        parseImportSymbols()
    }

    /**
     * Parse the import table of the PE file and extract all import symbols
     *
     * @return list of import symbols
     */
    private fun parseImportSymbols(): List<ImportSymbol> {
        val importTable = windowsHeader.importTable

        // If import table is empty, return an empty list
        if (importTable == DataDirectoryItem.ZERO) {
            return emptyList()
        }

        val result = mutableListOf<ImportSymbol>()

        // Read the import directory table
        val importDirectoryRva = importTable.virtualAddress

        // Create a temporary buffer for reading data
        val buffer = ByteArray(20) // Import directory entry size is 20 bytes

        var currentImportDescriptorOffset = 0u

        // Process each import directory entry (each DLL)
        while (true) {
            // Read import directory entry
            readVirtualMemory(importDirectoryRva + currentImportDescriptorOffset, buffer, 0, 20)

            val importLookupTableRva = buffer.u4l(0)
            val timeDateStamp = buffer.u4l(4)
            val forwarderChain = buffer.u4l(8)
            val nameRva = buffer.u4l(12)
            val importAddressTableRva = buffer.u4l(16)

            // If all fields are 0, we've reached the end of the import directory table
            if (importLookupTableRva == 0u && timeDateStamp == 0u && forwarderChain == 0u && nameRva == 0u && importAddressTableRva == 0u) {
                break
            }

            // Read DLL name
            val dllName = readCString(Address32(nameRva))

            // If import lookup table RVA is 0, use import address table RVA
            val lookupTableRva = if (importLookupTableRva != 0u) importLookupTableRva else importAddressTableRva

            // Determine the size of import lookup table entries based on PE file type
            val is64Bit = standardHeader.magic == PE32Magic.PE32Plus
            val entrySize = if (is64Bit) 8 else 4

            var currentLookupOffset = 0u

            while (true) {
                // Read import lookup table entry
                val entryBuffer = ByteArray(entrySize)
                readVirtualMemory(Address32(lookupTableRva) + currentLookupOffset, entryBuffer, 0, entrySize)

                val entry = if (is64Bit) {
                    entryBuffer.u8l(0)
                } else {
                    entryBuffer.u4l(0).toULong()
                }

                // If entry is 0, we've reached the end of the import lookup table
                if (entry == 0uL) {
                    break
                }

                // Check the highest bit to determine if import is by ordinal or by name
                val isImportByOrdinal = if (is64Bit) {
                    (entry and 0x8000000000000000uL) != 0uL
                } else {
                    (entry and 0x80000000uL) != 0uL
                }

                if (isImportByOrdinal) {
                    // Import by ordinal
                    val ordinal = if (is64Bit) {
                        (entry and 0xFFFFuL).toUShort()
                    } else {
                        (entry and 0xFFFFuL).toUShort()
                    }

                    result.add(
                        PEImportSymbol(name = "#$ordinal", file = dllName, ordinal = ordinal, isOrdinal = true),
                    )
                } else {
                    // Import by name
                    val hintNameRva = if (is64Bit) {
                        (entry and 0x7FFFFFFFFFFFFFFFuL).toUInt()
                    } else {
                        (entry and 0x7FFFFFFFuL).toUInt()
                    }

                    // Read Hint (2 bytes)
                    val hintBuffer = ByteArray(2)
                    readVirtualMemory(Address32(hintNameRva), hintBuffer, 0, 2)

                    // Read symbol name
                    val symbolName = readCString(Address32(hintNameRva) + 2u)

                    result.add(
                        PEImportSymbol(name = symbolName, file = dllName, isOrdinal = false),
                    )
                }

                // Move to the next entry
                currentLookupOffset += entrySize.toUInt()
            }

            // Move to the next import directory entry
            currentImportDescriptorOffset += 20u
        }

        return result
    }

    /**
     * Read data from the virtual memory of the PE file
     *
     * @param rva relative virtual address
     * @param buffer destination buffer
     * @param offset buffer offset
     * @param length length to read
     */
    private fun readVirtualMemory(rva: Address32, buffer: ByteArray, offset: Int, length: Int) {
        // If length is 0, return immediately
        if (length <= 0) return

        // Track the number of bytes read
        var bytesRead = 0
        var currentOffset = offset
        var currentRva = rva.value

        // First check if the RVA is within the file header range
        if (currentRva < windowsHeader.sizeOfHeaders) {
            // Calculate how many bytes can be read from the file header
            val headerBytesToRead = minOf((windowsHeader.sizeOfHeaders - currentRva).toInt(), length)

            if (headerBytesToRead > 0) {
                try {
                    // For the file header, RVA directly corresponds to file offset
                    dataAccessor.readFully(currentRva.toLong(), buffer, currentOffset, headerBytesToRead)

                    // Update the number of bytes read and current offset
                    bytesRead += headerBytesToRead
                    currentOffset += headerBytesToRead
                    currentRva += headerBytesToRead.toUInt()

                    // If all requested bytes have been read, exit
                    if (bytesRead >= length) {
                        return
                    }
                } catch (e: IOException) {
                    // If reading fails, continue trying to read from sections
                }
            }
        }

        if (length > bytesRead) {
            vm.read(currentRva.toULong(), buffer, currentOffset, length - bytesRead)
        }
    }

    /**
     * Read a C-style string (null-terminated)
     *
     * @param rva relative virtual address
     * @return the string
     */
    private fun readCString(rva: Address32): String {
        // Initial buffer size, can be adjusted as needed
        val bufferSize = 64
        val buffer = ByteArray(bufferSize)
        var currentRva = rva
        var result = ByteArray(0)

        while (true) {
            // Read multiple bytes at once
            readVirtualMemory(currentRva, buffer, 0, bufferSize)

            // Look for null terminator
            var nullPos = -1
            for (i in 0 until bufferSize) {
                if (buffer[i] == 0.toByte()) {
                    nullPos = i
                    break
                }
            }

            if (nullPos >= 0) {
                // Found null terminator, add valid data from current buffer to result
                val newResult = ByteArray(result.size + nullPos)
                result.copyInto(newResult)
                buffer.copyInto(newResult, result.size, 0, nullPos)
                return newResult.decodeToString()
            } else {
                // No null terminator found, add entire buffer to result and continue reading
                val newResult = ByteArray(result.size + bufferSize)
                result.copyInto(newResult)
                buffer.copyInto(newResult, result.size)
                result = newResult
                currentRva += bufferSize.toUInt()
            }
        }
    }

    /**
     * Get all export symbols from the PE file
     */
    override val exportSymbols: List<ExportSymbol> by lazy {
        parseExportSymbols()
    }

    /**
     * Parse the export table of the PE file and extract all export symbols
     *
     * @return list of export symbols
     */
    private fun parseExportSymbols(): List<ExportSymbol> {
        val exportTable = windowsHeader.exportTable

        // If export table is empty, return an empty list
        if (exportTable == DataDirectoryItem.ZERO) {
            return emptyList()
        }

        val result = mutableListOf<ExportSymbol>()

        // Read the export directory table
        val exportDirectoryRva = exportTable.virtualAddress

        // Create a buffer for reading the export directory table (40 bytes)
        val directoryBuffer = ByteArray(40)
        readVirtualMemory(exportDirectoryRva, directoryBuffer, 0, directoryBuffer.size)

        // Parse the export directory table
        directoryBuffer.u4l(4)
        directoryBuffer.u2l(8)
        directoryBuffer.u2l(10)
        val nameRva = Address32(directoryBuffer.u4l(12))
        val ordinalBase = directoryBuffer.u4l(16)
        val addressTableEntries = directoryBuffer.u4l(20)
        val numberOfNamePointers = directoryBuffer.u4l(24)
        val exportAddressTableRva = Address32(directoryBuffer.u4l(28))
        val namePointerRva = Address32(directoryBuffer.u4l(32))
        val ordinalTableRva = Address32(directoryBuffer.u4l(36))

        // Read the DLL name
        readCString(nameRva)

        // Read the export address table
        val exportAddressTableBuf = ByteArray(addressTableEntries.toInt() * 4)
        readVirtualMemory(exportAddressTableRva, exportAddressTableBuf, 0, exportAddressTableBuf.size)
        val exportAddressTable = Address32Array(addressTableEntries.toInt()) { index ->
            exportAddressTableBuf.u4l(index * 4).toAddr()
        }

        // Read the name pointer table
        val namePointerBuf = ByteArray(numberOfNamePointers.toInt() * 4)
        readVirtualMemory(namePointerRva, namePointerBuf, 0, namePointerBuf.size)
        val namePointerTable = Address32Array(numberOfNamePointers.toInt()) { index ->
            namePointerBuf.u4l(index * 4).toAddr()
        }

        // Read the ordinal table
        val ordinalTableBuf = ByteArray(numberOfNamePointers.toInt() * 2)
        readVirtualMemory(ordinalTableRva, ordinalTableBuf, 0, ordinalTableBuf.size)
        val ordinalTable = UShortArray(numberOfNamePointers.toInt()) { index ->
            ordinalTableBuf.u2l(index * 2)
        }

        // Create a map of ordinals to names
        val ordinalToNameMap = mutableMapOf<UShort, String>()
        for (i in 0 until numberOfNamePointers.toInt()) {
            val name = readCString(namePointerTable[i])
            ordinalToNameMap[ordinalTable[i]] = name
        }

        // Process all exports in the address table
        for (i in 0 until addressTableEntries.toInt()) {
            val ordinal = i.toUShort()
            val rva = exportAddressTable[i]

            // Check if this is a forwarder RVA (within the export section)
            val isForwarder =
                rva.value >= exportDirectoryRva.value && rva.value < exportDirectoryRva.value + exportTable.size.toUInt()

            val name = ordinalToNameMap[ordinal] ?: "#${ordinal.toInt() + ordinalBase.toInt()}"

            if (isForwarder) {
                // This is a forwarder, read the forwarder string
                val forwarderString = readCString(rva)
                result.add(
                    PEExportSymbol(
                        name = name,
                        ordinal = ((ordinal + ordinalBase).toUShort()),
                        address = rva,
                        isForwarder = true,
                        forwarderString = forwarderString,
                    ),
                )
            } else {
                // Regular export
                result.add(
                    PEExportSymbol(name = name, ordinal = ((ordinal + ordinalBase).toUShort()), address = rva),
                )
            }
        }

        return result
    }

    /**
     * Returns a DataAccessor that can be used to read from the PE file's virtual memory.
     *
     * This function provides access to the PE file's memory as it would be laid out when loaded by the OS.
     * It allows reading from both the header and all sections at their virtual addresses.
     *
     * Note: This implementation does not consider whether sections would actually be loaded in memory
     * during PE loading. Some sections may not be loaded due to their characteristics (e.g.,
     * IMAGE_SCN_MEM_DISCARDABLE), but this function returns data from all sections as if they were loaded.
     *
     * @return a DataAccessor implementation backed by this PE file's virtual memory
     */
    @Suppress("DuplicatedCode")
    override fun virtualMemory(): DataAccessor {
        return object : DataAccessor {
            override val size: Long get() = Long.MAX_VALUE


            override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
                DataAccessor.checkReadBounds(pos, buf, off, len)

                if (len <= 0) return len
                if (pos >= size) return len
                if (pos > UInt.MAX_VALUE.toLong()) return len
                readVirtualMemory(Address32(pos.toUInt()), buf, off, len)
                return len
            }

            override fun close() {
                // No need to close anything as we're just a wrapper
            }

            override fun toString(): String {
                return "VirtualMemoryDataAccessor(file=${this@PEFile})"
            }
        }
    }

    private operator fun Address32.plus(relAddress: Address32): Address32 {
        val sum = this.value + relAddress.value
        if (sum < this.value) {
            throw ArithmeticException("Address32 overflow: $this + $relAddress wraps around")
        }
        return Address32(sum)
    }
}
