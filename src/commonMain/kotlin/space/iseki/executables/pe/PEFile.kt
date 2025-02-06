package space.iseki.executables.pe

import space.iseki.executables.EOFException
import kotlin.jvm.JvmStatic

class PEFile private constructor(
    val coffHeader: CoffHeader,
    val standardHeader: StandardHeader,
    val windowsHeader: WindowsSpecifiedHeader,
    val sectionTable: List<SectionTableItem>,
    private val dataAccessor: DataAccessor,
) : AutoCloseable {
    data class Summary(
        val coffHeader: CoffHeader,
        val standardHeader: StandardHeader,
        val windowsHeader: WindowsSpecifiedHeader,
        val sectionTable: List<SectionTableItem>,
    )

    val summary: Summary = Summary(coffHeader, standardHeader, windowsHeader, sectionTable)

    companion object {
        private const val PE_SIGNATURE_LE = 0x00004550

        @JvmStatic
        fun wrap(data: ByteArray) = open(ByteArrayDataAccessor(data))

        @OptIn(ExperimentalStdlibApi::class)
        internal fun open(accessor: DataAccessor): PEFile {
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

    override fun close() {
        dataAccessor.close()
    }

    internal fun sectionReader(name: String): SectionReader? {
        val section = sectionTable.firstOrNull { it.name == name } ?: return null
        return SectionReader(section)
    }

    private val rsrcSectionReader = sectionReader(".rsrc")


    internal inner class SectionReader internal constructor(val table: SectionTableItem) {

        fun copyBytes(rva: Address32, buf: ByteArray) = copyBytes(rva, buf, 0, buf.size)

        fun copyBytes(rva: Address32, buf: ByteArray, off: Int, len: Int) {
            var rva0 = rva
            var off0 = off
            var len0 = len
            if (rva0 < table.virtualAddress) {
                val delta = (table.virtualAddress - rva0).toInt()
                if (delta !in 0..<len) {
                    return
                }
                len0 -= delta
                off0 += delta
                rva0 = table.virtualAddress
            }
            val readBegin = table.pointerToRawData + (rva0 - table.virtualAddress)
            val readSize = len0.toUInt().coerceAtMost(table.sizeOfRawData - (rva0 - table.virtualAddress).rawValue)
            dataAccessor.readFully(readBegin.rawValue.toLong(), buf, off0, readSize.toInt())
        }
    }

    private val rsrcRva: Address32 = windowsHeader.resourceTable.virtualAddress
    val resourceRoot: ResourceNode = ResourceRootNode()

    private fun readResourceDirectoryChildren(
        numberOfNamedEntries: UShort,
        numberOfIdEntries: UShort,
        dirNodeAddr: Address32,
    ): List<ResourceNode> {
        requireNotNull(rsrcSectionReader)
        val totalNodes = (numberOfNamedEntries + numberOfIdEntries).coerceAtMost(Int.MAX_VALUE.toUInt() / 8u).toInt()
        val buf = ByteArray(8 * totalNodes)
        rsrcSectionReader.copyBytes(dirNodeAddr + 16, buf)
        return buildList(capacity = totalNodes) {
            for (off in buf.indices step 8) {
                val nameOrId = buf.getUInt(off)
                val dataRva = Address32(buf.getUInt(off + 4))
                add(readSubNode(dataRva, nameOrId))
            }
        }
    }

    private fun readSubNode(dataRva: Address32, nameOrId: UInt): ResourceNode {
        requireNotNull(rsrcSectionReader)
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
            rsrcSectionReader.copyBytes(namePtr, lenBuf)
            val nameLen = lenBuf.getUShort(0)
            val nameBuf = ByteArray(nameLen.toInt())
            rsrcSectionReader.copyBytes(namePtr + 2, nameBuf)
            val charArray = CharArray(nameLen.toInt())
            nameBuf.forEachIndexed { index, byte -> charArray[index] = byte.toInt().toChar() }
            resourceID = 0u
            name = charArray.concatToString()
        }
        if ((dataRva and 0x80000000u).rawValue != 0u) {
            // directory
            return ResourceDirectory(name, resourceID, dataRva and 0x7FFFFFFFu)
        } else {
            // file
            val fileBuf = ByteArray(16)
            rsrcSectionReader.copyBytes(dataRva + rsrcRva, fileBuf)
            val contentRva = Address32(fileBuf.getUInt(0))
            val size = fileBuf.getUInt(4)
            val codePage = CodePage(fileBuf.getUInt(8))
            return ResourceFile(name, resourceID, size, dataRva, codePage, contentRva)
        }
    }

    private fun readResourceDirectoryNode(dataRva: Address32): List<ResourceNode> {
        requireNotNull(rsrcSectionReader)
        val buf = ByteArray(16)
        rsrcSectionReader.copyBytes(dataRva + rsrcRva, buf)
        val characteristics = buf.getUInt(0)
        if (characteristics != 0u) throw PEFileException("Invalid resource directory node, characteristics is not 0, $dataRva")
//        val timeDateStamp = TimeDataStamp32(buf.getUInt(4))
//        val majorVersion = buf.getUShort(8)
//        val minorVersion = buf.getUShort(10)
        val numberOfNamedEntries = buf.getUShort(12)
        val numberOfIdEntries = buf.getUShort(14)
        return readResourceDirectoryChildren(numberOfNamedEntries, numberOfIdEntries, dataRva + rsrcRva)
    }

    @Suppress("EqualsOrHashCode")
    internal inner class ResourceRootNode : ResourceNode {
        override val name: String
            get() = "<root>"
        override val id: UInt
            get() = 0u
        override val dataRva: Address32
            get() = Address32(0u)

        override fun isFile(): Boolean = false
        override fun listChildren(): List<ResourceNode> {
            rsrcSectionReader ?: return emptyList()
            return readResourceDirectoryNode(dataRva)
        }

        override fun toString(): String = "<ROOT>"

        override fun hashCode(): Int = dataRva.rawValue.toInt()
    }

    @Suppress("EqualsOrHashCode")
    internal inner class ResourceDirectory(
        override val name: String,
        override val id: UInt,
        override val dataRva: Address32,
    ) : ResourceNode {
        override fun isFile(): Boolean = false

        override fun listChildren(): List<ResourceNode> {
            return readResourceDirectoryNode(dataRva)
        }

        override fun toString(): String = "<DIR:${if (id == 0u) name else "ID=$id"}> @$dataRva"
        override fun hashCode(): Int = dataRva.rawValue.toInt()
    }

    @Suppress("EqualsOrHashCode")
    internal inner class ResourceFile(
        override val name: String,
        override val id: UInt,
        override val size: UInt,
        override val dataRva: Address32,
        override val codePage: CodePage,
        val contentRva: Address32,
    ) : ResourceNode {
        override fun isFile(): Boolean = true
        override fun toString(): String =
            "<FILE:${if (id == 0u) name else "ID=$id"}, CodePage=$codePage, Size=$size, ContentRVA=$contentRva> @$dataRva"

        override fun hashCode(): Int = dataRva.rawValue.toInt()

        override fun readAllBytes(): ByteArray {
            rsrcSectionReader ?: return ByteArray(0)
            val buf = ByteArray(size.toInt())
            rsrcSectionReader.copyBytes(contentRva, buf)
            return buf
        }
    }
}
