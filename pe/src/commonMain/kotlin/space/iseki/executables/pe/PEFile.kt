package space.iseki.executables.pe

import kotlinx.serialization.Serializable
import space.iseki.executables.common.DataAccessor
import space.iseki.executables.common.EOFException
import space.iseki.executables.common.FileFormat
import space.iseki.executables.common.IOException
import space.iseki.executables.common.OpenedFile
import space.iseki.executables.common.ReadableSection
import space.iseki.executables.pe.vi.PEVersionInfo
import space.iseki.executables.pe.vi.locateVersionInfo
import space.iseki.executables.pe.vi.parseVersionData

class PEFile private constructor(
    val coffHeader: CoffHeader,
    val standardHeader: StandardHeader,
    val windowsHeader: WindowsSpecifiedHeader,
    val sectionTable: List<SectionTableItem>,
    private val dataAccessor: DataAccessor,
) : AutoCloseable, OpenedFile {

    /**
     * Represents a summary of the pe file headers.
     *
     * @property coffHeader the coff header
     * @property standardHeader the standard header
     * @property windowsHeader the windows specific header
     * @property sectionTable the list of section table items
     */
    @Serializable
    class Summary(
        val coffHeader: CoffHeader,
        val standardHeader: StandardHeader,
        val windowsHeader: WindowsSpecifiedHeader,
        val sectionTable: List<SectionTableItem>,
    )

    /**
     * Returns a summary of the pe file headers.
     *
     * @return a [Summary] instance
     */
    val summary: Summary = Summary(coffHeader, standardHeader, windowsHeader, sectionTable)

    companion object : FileFormat<PEFile> {
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
                pos = signatureBuffer.getUInt(0).toLong()
                accessor.readFully(pos, signatureBuffer)
                val readSignature = signatureBuffer.getUInt(0)
                if (readSignature != PE_SIGNATURE_LE.toUInt()) {
                    throw PEFileException("Not a PE file, bad magic: 0x${readSignature.toHexString()}")
                }
            } catch (e: EOFException) {
                throw PEFileException("Not a PE file, unexpected EOF during read PE magic", e)
            }
            val coffHeader: CoffHeader
            try {
                val coffBuffer = ByteArray(CoffHeader.LENGTH)
                pos += 4
                accessor.readFully(pos, coffBuffer)
                coffHeader = CoffHeader.parse(coffBuffer, 0)
            } catch (e: EOFException) {
                throw PEFileException("Invalid PE file, unexpected EOF during read COFF header", e)
            }
            if (coffHeader.numbersOfSections > 96.toUShort()) {
                throw PEFileException("Invalid PE file, too many sections: " + coffHeader.numbersOfSections)
            }
            val standardHeader: StandardHeader
            val optionalHeader: WindowsSpecifiedHeader
            try {
                val optionalHeaderBuffer = ByteArray(coffHeader.sizeOfOptionalHeader.toULong().toInt())
                pos += CoffHeader.LENGTH
                accessor.readFully(pos, optionalHeaderBuffer)
                standardHeader = StandardHeader.parse(optionalHeaderBuffer, 0)
                optionalHeader =
                    WindowsSpecifiedHeader.parse(optionalHeaderBuffer, standardHeader.length(), standardHeader.magic)
            } catch (e: IndexOutOfBoundsException) {
                val s =
                    "Invalid PE file, IOBE during reading optional header, maybe the sizeOfOptionalHeader in COFF header is too small, COFF header: $coffHeader"
                throw PEFileException(s, e)
            } catch (e: EOFException) {
                throw PEFileException(
                    message = "Invalid PE file, unexpected EOF during read optional header [${Address32(pos.toUInt())} + ${coffHeader.sizeOfOptionalHeader}], COFF header: $coffHeader",
                    cause = e
                )
            }
            val sectionTableData = ByteArray(coffHeader.numbersOfSections.toInt() * SectionTableItem.LENGTH)
            pos += coffHeader.sizeOfOptionalHeader.toULong().toLong()
            try {
                accessor.readFully(pos, sectionTableData)
            } catch (e: EOFException) {
                throw PEFileException("Invalid PE file, unexpected EOF during read section table", e)
            }
            val sectionTableItemArray = Array(coffHeader.numbersOfSections.toInt()) {
                val off = it * SectionTableItem.LENGTH
                SectionTableItem.parse(sectionTableData, off)
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

    /**
     * Returns a section reader for the section with the given name.
     *
     * @param name the name of the section
     * @return a [SectionReader] if found; otherwise, null
     */
    internal fun sectionReader(name: String): SectionReader? {
        val section = sectionTable.firstOrNull { it.name == name } ?: return null
        return SectionReader(section)
    }

    internal inner class SectionReader internal constructor(val table: SectionTableItem) {

        fun copyBytes(rva: Address32, buf: ByteArray) = copyBytes(rva, buf, 0, buf.size)

        /**
         * @author ChatGPT
         */
        fun copyBytes(rva: Address32, buf: ByteArray, off: Int, len: Int) {
            check(len >= 0)
            // Request range: [reqStart, reqEnd)
            val reqStart: UInt = rva.value
            val reqEnd: UInt = reqStart + len.toUInt()

            // Section's virtual address range: [sectionStart, sectionEnd)
            val sectionStart: UInt = table.virtualAddress.value
            val sectionEnd: UInt = sectionStart + table.sizeOfRawData

            // Calculate intersection range: [copyStart, copyEnd)
            val copyStart: UInt = maxOf(reqStart, sectionStart)
            val copyEnd: UInt = minOf(reqEnd, sectionEnd)

            if (copyEnd <= copyStart) {
                return // No intersection, no processing needed
            }

            // Length of the intersection range (ensuring it doesn't exceed Int range)
            val copyLen: Int = (copyEnd - copyStart).toInt()

            // File reading position:
            // The section's starting address in memory (table.virtualAddress) corresponds to table.pointerToRawData in the file,
            // so the reading position is pointerToRawData + (copyStart - sectionStart)
            val filePos: Long = table.pointerToRawData.value.toLong() + (copyStart - sectionStart).toLong()

            // Starting offset in the destination buffer: offset between request range and intersection range
            val destOff: Int = off + (copyStart - reqStart).toInt()

            // Call dataAccessor to read data into the specified region of the buffer
            dataAccessor.readFully(filePos, buf, destOff, copyLen)
        }

    }

    private val sectionReaders = sectionTable.map { SectionReader(it) }.toTypedArray()

    /**
     * Copy bytes from the given RVA to the buffer.
     *
     * @param rva the relative virtual address
     * @param buf the buffer to copy to
     * @param off the offset in the buffer
     * @param len the length to copy
     */
    internal fun copyBytes(rva: Address32, buf: ByteArray, off: Int, len: Int) {
        for (sectionReader in sectionReaders) {
            sectionReader.copyBytes(rva, buf, off, len)
        }
    }

    /**
     * Copy bytes from the given RVA to the buffer.
     *
     * @param rva the relative virtual address
     * @param buf the buffer to copy to
     */
    internal fun copyBytes(rva: Address32, buf: ByteArray) {
        copyBytes(rva, buf, 0, buf.size)
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
        copyBytes(dirNodeAddr + 16, buf)
        return buildList(capacity = totalNodes) {
            for (off in buf.indices step 8) {
                val nameOrId = buf.getUInt(off)
                val dataRva = Address32(buf.getUInt(off + 4))
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
            copyBytes(namePtr, lenBuf)
            val nameLen = lenBuf.getUShort(0)
            val nameBuf = ByteArray(nameLen.toInt())
            copyBytes(namePtr + 2, nameBuf)
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
            copyBytes(dataRva + rsrcRva, fileBuf)
            val contentRva = Address32(fileBuf.getUInt(0))
            val size = fileBuf.getUInt(4)
            val codePage = CodePage(fileBuf.getUInt(8))
            return ResourceFile(name, resourceID, size, dataRva, codePage, contentRva)
        }
    }

    private fun readResourceDirectoryNode(dataRva: Address32): List<ResourceNode> {
        if (rsrcRva.value == 0u) return emptyList()
        val buf = ByteArray(16)
        copyBytes(dataRva + rsrcRva, buf)
        val characteristics = buf.getUInt(0)
        if (characteristics != 0u) throw PEFileException("Invalid resource directory node, characteristics is not 0, $dataRva")
//        val timeDateStamp = TimeDataStamp32(buf.getUInt(4))
//        val majorVersion = buf.getUShort(8)
//        val minorVersion = buf.getUShort(10)
        val numberOfNamedEntries = buf.getUShort(12)
        val numberOfIdEntries = buf.getUShort(14)
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
            copyBytes(contentRva, buf)
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

    override fun toString(): String {
        return "PEFile(dataAccessor=$dataAccessor)"
    }

    /**
     * Represents a PE section.
     */
    inner class Section internal constructor(
        val tableItem: SectionTableItem,
    ) : ReadableSection {
        private val peFile: PEFile
            get() = this@PEFile
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

        /**
         * Reads bytes from the section.
         *
         * @param sectionOffset the offset within the section to read from
         * @param buf the buffer to read into
         * @param bufOffset the offset within the buffer to read into
         * @param size the number of bytes to read
         * @throws IndexOutOfBoundsException if buffer offset or size is out of bounds
         * @throws IllegalArgumentException if size or section offset is negative
         * @throws IOException if reading from the file fails
         */
        override fun readBytes(
            sectionOffset: Long,
            buf: ByteArray,
            bufOffset: Int,
            size: Int,
        ) {
            // Check parameter validity
            if (bufOffset < 0 || bufOffset + size > buf.size) {
                throw IndexOutOfBoundsException("Buffer offset or size out of bounds: offset=$bufOffset, size=$size, buffer size=${buf.size}")
            }
            if (size < 0) {
                throw IllegalArgumentException("Size cannot be negative: $size")
            }
            if (sectionOffset < 0) {
                throw IllegalArgumentException("Section offset cannot be negative: $sectionOffset")
            }

            // Calculate actual readable bytes
            val availableBytes = maxOf(0L, sizeOfRawData.toLong() - sectionOffset)
            if (availableBytes <= 0) {
                // No data available to read
                return
            }

            // Calculate actual bytes to read
            val bytesToRead = minOf(availableBytes, size.toLong()).toInt()
            if (bytesToRead <= 0) {
                return
            }

            // Calculate actual position in file
            val filePosition = pointerToRawData.value.toLong() + sectionOffset

            // Read data from file
            try {
                dataAccessor.readFully(filePosition, buf, bufOffset, bytesToRead)
            } catch (e: IOException) {
                throw IOException("Failed to read section '$name' at offset $filePosition", e)
            }
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

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Section

            if (tableItem != other.tableItem) return false
            if (peFile != other.peFile) return false

            return true
        }

        override fun hashCode(): Int {
            var result = tableItem.hashCode()
            result = 31 * result + peFile.hashCode()
            return result
        }


    }
}
