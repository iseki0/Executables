package space.iseki.executables.pe

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

        internal fun open(accessor: DataAccessor): PEFile {
            var pos = 0x3cL
            try {
                val signatureBuffer = byteArrayOf(0, 0, 0, 0)
                accessor.readFully(pos, signatureBuffer)
                pos = signatureBuffer.getUInt(0).toLong()
                accessor.readFully(pos, signatureBuffer)
                val readSignature = signatureBuffer.getUInt(0)
                if (readSignature != PE_SIGNATURE_LE.toUInt()) {
                    throw PEFileException("Not a PE file, bad magic: $readSignature")
                }
            } catch (e: PEEOFFileException) {
                throw PEFileException("Not a PE file, unexpected EOF during read PE magic", e)
            }
            val coffHeader: CoffHeader
            try {
                val coffBuffer = ByteArray(CoffHeader.LENGTH)
                pos += 4
                accessor.readFully(pos, coffBuffer)
                coffHeader = CoffHeader.parse(coffBuffer, 0)
            } catch (e: PEEOFFileException) {
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
            } catch (e: PEEOFFileException) {
                throw PEFileException(
                    message = "Invalid PE file, unexpected EOF during read optional header [${Address32(pos.toUInt())} + ${coffHeader.sizeOfOptionalHeader}], COFF header: $coffHeader",
                    cause = e
                )
            }
            val sectionTableData = ByteArray(coffHeader.numbersOfSections.toInt() * SectionTableItem.LENGTH)
            pos += coffHeader.sizeOfOptionalHeader.toULong().toLong()
            accessor.readFully(pos, sectionTableData)
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
}
