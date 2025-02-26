package space.iseki.executables.pe

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ByteArrayDataAccessor
import space.iseki.executables.common.DataAccessor
import space.iseki.executables.common.EOFException
import space.iseki.executables.common.FileFormat
import space.iseki.executables.common.IOException
import space.iseki.executables.common.OpenedFile
import space.iseki.executables.pe.vi.PEVersionInfo
import space.iseki.executables.pe.vi.locateVersionInfo
import space.iseki.executables.pe.vi.parseVersionData
import kotlin.jvm.JvmStatic

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
         * Wraps the given byte array into a [PEFile].
         *
         * @param data the byte array representing a pe file
         * @return a [PEFile] instance
         */
        @JvmStatic
        @Deprecated("Use PEFile(d: ByteArray) instead", ReplaceWith("PEFile(d)"), level = DeprecationLevel.HIDDEN)
        fun wrap(data: ByteArray) = open(ByteArrayDataAccessor(data))

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
            // 请求区间：[reqStart, reqEnd)
            val reqStart: UInt = rva.rawValue
            val reqEnd: UInt = reqStart + len.toUInt()

            // section 的虚拟地址区间：[sectionStart, sectionEnd)
            val sectionStart: UInt = table.virtualAddress.rawValue
            val sectionEnd: UInt = sectionStart + table.sizeOfRawData

            // 计算交集区间：[copyStart, copyEnd)
            val copyStart: UInt = maxOf(reqStart, sectionStart)
            val copyEnd: UInt = minOf(reqEnd, sectionEnd)

            if (copyEnd <= copyStart) {
                return // 无交集，不做处理
            }

            // 交集区间的长度（确保不会超过 Int 范围）
            val copyLen: Int = (copyEnd - copyStart).toInt()

            // 文件中对应的读取位置：
            // section 在内存中的起始地址 table.virtualAddress 对应文件中的位置为 table.pointerToRawData，
            // 因此读取位置为 pointerToRawData + (copyStart - sectionStart)
            val filePos: Long = table.pointerToRawData.rawValue.toLong() + (copyStart - sectionStart).toLong()

            // 目标 buffer 中写入数据的起始偏移：请求区间和交集区间之间的偏移量
            val destOff: Int = off + (copyStart - reqStart).toInt()

            // 调用 dataAccessor 读取数据到 buf 中指定区域
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
        if ((dataRva and 0x80000000u).rawValue != 0u) {
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
        if (rsrcRva.rawValue == 0u) return emptyList()
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

    @Suppress("EqualsOrHashCode")
    internal inner class ResourceRootNode : ResourceNode {
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

        override fun hashCode(): Int = dataRva.rawValue.toInt()
    }

    @Suppress("EqualsOrHashCode")
    internal inner class ResourceDirectory(
        override val name: String,
        override val id: UInt,
        override val dataRva: Address32,
    ) : ResourceNode {
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
            val buf = ByteArray(size.toInt())
            copyBytes(contentRva, buf)
            return buf
        }
    }

    val versionInfo: PEVersionInfo? by lazy {
        locateVersionInfo(this)?.readAllBytes()?.let { parseVersionData(it, 0) }
    }
}
