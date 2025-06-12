package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.CStringReadingException
import space.iseki.executables.common.DataAccessor
import space.iseki.executables.common.DataAccessor.Companion.checkReadBounds
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
import space.iseki.executables.share.MemReader
import space.iseki.executables.share.cstrUtf8
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.min

/**
 * Represents an ELF file and provides access to its contents.
 *
 * This class encapsulates the functionality for opening, parsing, and closing ELF files.
 * It provides methods to read and interpret the ELF header and other structures within the file.
 * @property dataAccessor The data accessor that provides access to the file content
 * @property ident The identification of the ELF file
 * @property ehdr The ELF header of the file
 */
class ElfFile private constructor(
    private val dataAccessor: DataAccessor,
    val ident: ElfIdentification,
    val ehdr: ElfEhdr,
    val programHeaders: List<ElfPhdr>,
    val sectionHeaders: List<ElfShdr>,
) : AutoCloseable, OpenedFile, ReadableSectionContainer, ExportSymbolContainer, ImportSymbolContainer,
    VirtualMemoryReadable {

    companion object : FileFormat<ElfFile> {
        override fun toString(): String = "ELF"
        internal val ElfClass.ehdrSize: Int
            get() = when (this) {
                ElfClass.ELFCLASS32 -> 52
                ElfClass.ELFCLASS64 -> 64
                else -> throw ElfFileException("Invalid ElfClass", "class" to this)
            }

        /**
         * Reads section name string table and adds names to section headers.
         *
         * @param accessor The data accessor
         * @param ehdr The ELF header
         * @param sectionHeaders The original section headers list
         * @return Section headers list with names
         * @throws ElfFileException if reading section names fails
         */
        private fun readSectionNames(
            accessor: DataAccessor,
            ehdr: ElfEhdr,
            sectionHeaders: List<ElfShdr>,
        ): List<ElfShdr> {
            if (sectionHeaders.isEmpty()) return sectionHeaders

            val shstrndx = ehdr.eShstrndx.toInt()
            if (shstrndx < 0 || shstrndx >= sectionHeaders.size) {
                throw ElfFileException(
                    "Invalid section header string table index",
                    "index" to shstrndx,
                    "section_count" to sectionHeaders.size,
                )
            }

            val stringTableHeader = sectionHeaders[shstrndx]

            // Validate string table section type
            if (stringTableHeader.shType != ElfSType.SHT_STRTAB) {
                throw ElfFileException(
                    "Section header string table is not a string table",
                    "index" to shstrndx,
                    "type" to stringTableHeader.shType,
                )
            }

            if (stringTableHeader.shSize > Int.MAX_VALUE.toULong()) {
                throw ElfFileException("String table size exceeds maximum allowed", "size" to stringTableHeader.shSize)
            }
            val stringTableSize = stringTableHeader.shSize.toInt()
            if (stringTableSize <= 0) {
                throw ElfFileException("Invalid string table size", "size" to stringTableSize)
            }

            // Check if string table size is reasonable (arbitrary limit to prevent DoS)
            if (stringTableSize > 10 * 1024 * 1024) { // 10 MB limit
                throw ElfFileException("String table size too large", "size" to stringTableSize)
            }

            val stringTableOffset = stringTableHeader.shOffset.toLong()

            // Validate string table offset
            if (stringTableOffset < 0 || stringTableOffset + stringTableSize > accessor.size) {
                throw ElfFileException(
                    "String table extends beyond file end",
                    "offset" to stringTableOffset,
                    "size" to stringTableSize,
                    "file_size" to accessor.size,
                )
            }

            val stringTableData = ByteArray(stringTableSize)
            tryReading("string table", stringTableOffset, stringTableSize) {
                accessor.readFully(stringTableOffset, stringTableData)
            }

            // Verify first byte is null as per ELF spec
            if (stringTableSize > 0 && stringTableData[0] != 0.toByte()) {
                throw ElfFileException(
                    "Invalid string table: first byte is not null",
                    "first_byte" to stringTableData[0],
                    "table_size" to stringTableSize,
                )
            }

            // Add name to each section header
            return sectionHeaders.map { shdr ->
                val nameIndex = shdr.shName.toInt()
                if (nameIndex < 0 || nameIndex >= stringTableSize) {
                    throw ElfFileException(
                        "Invalid section name index", "index" to nameIndex, "string_table_size" to stringTableSize,
                    )
                }

                val name = runCatching { stringTableData.cstrUtf8(nameIndex) }.getOrElse {
                    throw ElfFileException("Failed to decode section name", "index" to nameIndex, cause = it)
                }

                // Create a new section header with name using the copy function
                shdr.copy(name = name)
            }
        }

        /**
         * Opens and parses an ELF file from the given data accessor.
         *
         * @param accessor The data accessor that provides access to the file content
         * @return A new ELF file instance
         * @throws ElfFileException if the file format is invalid or unsupported
         */
        @Throws(IOException::class)
        override fun open(accessor: DataAccessor): ElfFile {
            val buf = ByteArray(16)
            try {
                accessor.readFully(0, buf)
            } catch (e: IOException) {
                throw ElfFileException("Failed to read ELF identification bytes", cause = e)
            }

            val ident = tryByteArrayParsing("ELF Identification") { ElfIdentification.parse(buf, 0) }
            val buf2 = ByteArray(ident.eiClass.ehdrSize)
            try {
                accessor.readFully(0, buf2)
            } catch (e: EOFException) {
                throw ElfFileException(
                    "Failed to read ELF header",
                    "required_size" to ident.eiClass.ehdrSize,
                    cause = e,
                )
            }

            val ehdr = if (ident.eiClass == ElfClass.ELFCLASS32) {
                ElfEhdr.parse32(buf2, 0, ident)
            } else if (ident.eiClass == ElfClass.ELFCLASS64) {
                ElfEhdr.parse64(buf2, 0, ident)
            } else {
                throw ElfFileException("Invalid ElfClass", "class" to ident.eiClass)
            }

            // Validate ELF header fields
            ehdr.validate(accessor.size)

            // Read Program Header table
            val phOffset: Long = ehdr.ePhoff.toLong()
            val phEntSize: Int = ehdr.ePhentsize.toInt()
            val phNum: Int = ehdr.ePhnum.toInt()
            if (ehdr.is64Bit) {
                if (phEntSize < 56) // Minimum size for 64-bit program header
                    throw ElfFileException(
                        "Invalid program header entry size, must be at least 56 bytes",
                        "size" to phEntSize,
                    )
            } else {
                if (phEntSize < 32) // Minimum size for 32-bit program header
                    throw ElfFileException(
                        "Invalid program header entry size, must be at least 32 bytes",
                        "size" to phEntSize,
                    )
            }
            val phSize = phEntSize * phNum
            if (phOffset + phSize > accessor.size) {
                throw ElfFileException(
                    "Program header table extends beyond file end",
                    "offset" to phOffset,
                    "size" to phSize,
                    "file_size" to accessor.size,
                )
            }
            val phBuffer = ByteArray(phSize)
            try {
                if (phSize > 0) accessor.readFully(phOffset, phBuffer)
            } catch (e: IOException) {
                throw ElfFileException("Failed to read program headers", "offset" to phOffset, cause = e)
            }
            val programHeaders = tryByteArrayParsing("program headers") {
                List(phNum) { i ->
                    if (ehdr.is64Bit) {
                        ElfPhdr.parse64(phBuffer, i * phEntSize, ident)
                    } else {
                        ElfPhdr.parse32(phBuffer, i * phEntSize, ident)
                    }
                }
            }

            val le = ident.eiData == ElfData.ELFDATA2LSB
            // Read Section Header table
            val shOffset = ehdr.eShoff.toLong()
            val shEntSize = ehdr.eShentsize.toInt()
            val shNum = ehdr.eShnum.toInt()
            if (ehdr.is64Bit) {
                if (shEntSize < 64) // Minimum size for 64-bit section header
                    throw ElfFileException(
                        "Invalid section header entry size, must be at least 64 bytes",
                        "size" to shEntSize,
                    )
            } else {
                if (shEntSize < 40) // Minimum size for 32-bit section header
                    throw ElfFileException(
                        "Invalid section header entry size, must be at least 40 bytes",
                        "size" to shEntSize,
                    )
            }
            val shSize = shEntSize * shNum
            if (shOffset + shSize > accessor.size) {
                throw ElfFileException(
                    "Section header table extends beyond file end",
                    "offset" to shOffset,
                    "size" to shSize,
                    "file_size" to accessor.size,
                )
            }
            val shBuffer = ByteArray(shSize)
            tryReading("section headers", shOffset, shSize) {
                if (shSize > 0) accessor.readFully(shOffset, shBuffer)
            }
            val sectionHeaders = tryByteArrayParsing("section headers") {
                List(shNum) { i ->
                    if (ehdr.is64Bit) {
                        ElfShdr.parse64(shBuffer, i * shEntSize, le)
                    } else {
                        ElfShdr.parse32(shBuffer, i * shEntSize, le)
                    }
                }
            }

            // Read section name string table and add names to section headers
            val sectionHeadersWithNames = readSectionNames(accessor, ehdr, sectionHeaders)

            return ElfFile(accessor, ident, ehdr, programHeaders, sectionHeadersWithNames)
        }
    }

    override fun close() {
        dataAccessor.close()
    }

    override val rootHeaders: Map<String, ReadableStructure>
        get() = mapOf("ehdr" to ehdr, "ident" to ident)

    /**
     * Represents an ELF section.
     */
    inner class Section internal constructor(
        val sectionHeader: ElfShdr,
    ) : ReadableSection {

        private val elf: ElfFile
            get() = this@ElfFile

        override val name: String?
            get() = sectionHeader.name

        override val size: Long
            get() = if (sectionHeader.shType == ElfSType.SHT_NOBITS) {
                0
            } else sectionHeader.shSize.toLong()

        override val header: ReadableStructure
            get() = sectionHeader


        override fun toString(): String =
            "Section(name=$name, size=$size, type=${sectionHeader.shType}, flags=${sectionHeader.shFlags})"

        override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
            checkReadBounds(pos, buf, off, len)
            return dataAccessor.readAtMost(
                pos = pos + sectionHeader.shOffset.toLong(),
                buf = buf,
                off = off,
                len = min(len, size.toInt()),
            )
        }
    }

    /**
     * Returns a list of sections in the ELF file.
     *
     * @return A list of sections, unmodifiable
     */
    override val sections: List<Section> = object : AbstractList<Section>() {
        override val size: Int
            get() = sectionHeaders.size

        override fun get(index: Int): Section = Section(sectionHeaders[index])

    }

    private val vm = MemReader(dataAccessor).apply {
        programHeaders.filter { it.pType == ElfPType.PT_LOAD }.sortedBy { it.pVaddr.value }.forEach {
            mapMemory(
                vOff = it.pVaddr.value,
                fOff = it.pOffset,
                fSize = minOf(it.pFilesz, it.pMemsz),
            )
        }
    }

    /**
     * Represents a symbol in the ELF file
     *
     * @property name The name of the symbol
     * @property value The value (address) of the symbol
     * @property size The size of the symbol
     * @property binding The binding attribute of the symbol (LOCAL, GLOBAL, WEAK)
     * @property type The type attribute of the symbol (NOTYPE, OBJECT, FUNC, etc.)
     * @property visibility The visibility attribute of the symbol (DEFAULT, HIDDEN, PROTECTED)
     * @property sectionIndex The section index where this symbol is defined, or SHN_UNDEF if undefined
     * @property isUndefined Whether the symbol is undefined (imported)
     */
    @Serializable
    data class ElfSymbolInfo(
        val name: String,
        val value: ULong,
        val size: ULong,
        val binding: ElfSymBinding,
        val type: ElfSymType,
        val visibility: ElfSymVisibility,
        val sectionIndex: UShort,
        val isUndefined: Boolean,
    )

    /**
     * Get all symbols from the ELF file
     *
     * @return list of symbols
     */
    val symbols: List<ElfSymbolInfo> by lazy {
        parseSymbols()
    }

    /**
     * Get all export symbols from the ELF file
     *
     * Export symbols are defined in this file and can be referenced by other files.
     * They typically have a binding of GLOBAL or WEAK and are not undefined.
     *
     * @return list of export symbols
     */
    override val exportSymbols: List<ExportSymbol> by lazy {
        parseExportSymbols()
    }

    /**
     * Get all import symbols from the ELF file
     *
     * Import symbols are referenced by this file but defined elsewhere.
     * They are marked as undefined (SHN_UNDEF) in the symbol table.
     *
     * @return list of import symbols
     */
    override val importSymbols: List<ImportSymbol> by lazy {
        parseImportSymbols()
    }

    /**
     * Parse the symbol tables of the ELF file and extract export symbols
     *
     * @return list of export symbols
     */
    private fun parseExportSymbols(): List<ExportSymbol> {
        // Export symbols are defined in this file (non-UNDEF) and have a binding type of GLOBAL or WEAK
        return symbols.filter {
            !it.isUndefined && (it.binding == ElfSymBinding.STB_GLOBAL || it.binding == ElfSymBinding.STB_WEAK)
        }.map { sym ->
            ElfExportSymbol(
                name = sym.name,
                value = sym.value,
                size = sym.size,
                binding = sym.binding,
                type = sym.type,
                visibility = sym.visibility,
            )
        }
    }

    /**
     * Parse the symbol tables of the ELF file and extract import symbols
     *
     * @return list of import symbols
     */
    private fun parseImportSymbols(): List<ImportSymbol> {
        // Import symbols are undefined symbols (SHN_UNDEF)
        return symbols.filter {
            it.isUndefined && it.name.isNotEmpty()
        }.map { sym ->
            // In ELF, import symbols are typically not directly associated with a specific library file
            // Here we set the file property to an empty string, which may need to be obtained from the dynamic section in actual applications
            ElfImportSymbol(
                name = sym.name, file = "",  // ELF usually needs to be inferred from the dynamic section
                binding = sym.binding, type = sym.type,
            )
        }
    }

    /**
     * Parse the symbol tables of the ELF file and extract all symbols
     *
     * @return list of symbols
     */
    private fun parseSymbols(): List<ElfSymbolInfo> {
        val result = mutableListOf<ElfSymbolInfo>()

        // Find symbol table sections (.dynsym and .symtab)
        val symbolTableSections = sectionHeaders.filter {
            it.shType == ElfSType.SHT_SYMTAB || it.shType == ElfSType.SHT_DYNSYM
        }

        if (symbolTableSections.isEmpty()) {
            return emptyList()
        }

        val isLittleEndian = ident.eiData == ElfData.ELFDATA2LSB

        for (symSection in symbolTableSections) {
            // Find the associated string table section
            val stringTableIndex = symSection.shLink.toInt()
            if (stringTableIndex < 0 || stringTableIndex >= sectionHeaders.size) {
                throw ElfFileException(
                    "Invalid string table index", "index" to stringTableIndex, "total_sections" to sectionHeaders.size,
                )
            }

            val stringTableSection = sectionHeaders[stringTableIndex]

            // Read the string table
            val stringTableSize = stringTableSection.shSize.toInt()

            val stringTableData = ByteArray(stringTableSize)
            tryReading("string table", stringTableSection.shOffset.toLong(), stringTableSize) {
                dataAccessor.readFully(stringTableSection.shOffset.toLong(), stringTableData)
            }

            // Read the symbol table
            val symbolTableSize = symSection.shSize.toInt()

            val symbolTableData = ByteArray(symbolTableSize)
            tryReading("symbol table", symSection.shOffset.toLong(), symbolTableSize) {
                dataAccessor.readFully(symSection.shOffset.toLong(), symbolTableData)
            }

            // Parse the symbol table entries
            val entrySize = symSection.shEntsize.toInt()
            val numEntries = if (entrySize > 0) symbolTableSize / entrySize else 0

            for (i in 0 until numEntries) {
                val offset = i * entrySize

                // Ensure offset is within valid range
                if (offset + (if (ident.eiClass == ElfClass.ELFCLASS32) Elf32Sym.SIZE else Elf64Sym.SIZE) > symbolTableData.size) {
                    continue
                }

                val sym = try {
                    tryByteArrayParsing("symbol table entry") {
                        if (ident.eiClass == ElfClass.ELFCLASS32) {
                            Elf32Sym.parse(symbolTableData, offset, isLittleEndian)
                        } else {
                            Elf64Sym.parse(symbolTableData, offset, isLittleEndian)
                        }
                    }
                } catch (e: ElfFileException) {
                    continue // Skip this symbol if parsing fails
                }

                // Skip the first (NULL) symbol entry
                if (i == 0) {
                    continue
                }

                // Get symbol name from string table
                val nameOffset = sym.stName.toInt()

                // Allow name offset to be 0, but will get an empty string
                val name = if (nameOffset != 0) {
                    stringTableData.tryCStrUtf8(nameOffset, "parsing symbol table")
                } else ""

                // Add all symbols to the result list, including symbols with empty names
                result.add(
                    ElfSymbolInfo(
                        name = name,
                        value = sym.stValue,
                        size = sym.stSize,
                        binding = sym.getBinding(),
                        type = sym.getType(),
                        visibility = sym.getVisibility(),
                        sectionIndex = sym.stShndx,
                        isUndefined = sym.stShndx.toInt() == 0,
                    ),
                )
            }
        }

        return result
    }

    /**
     * Returns a DataAccessor that can be used to read from the ELF file's virtual memory.
     *
     * This function provides access to the ELF file's memory as it would be laid out when loaded by the OS.
     * It uses the program headers (segments) to map virtual addresses to file offsets.
     *
     * Note: This implementation does not consider whether segments would actually be loaded in memory
     * during ELF loading. Some segments might be modified (e.g., relocations applied) during actual loading,
     * but this function returns data from segments as if they were loaded without modification.
     *
     * @return a DataAccessor implementation backed by this ELF file's virtual memory
     */
    @Suppress("DuplicatedCode")
    override fun virtualMemory(): DataAccessor {
        return object : DataAccessor {
            override val size: Long get() = Long.MAX_VALUE

            override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
                checkReadBounds(pos, buf, off, len)

                if (len <= 0) return len

                vm.read(
                    pos = pos.toULong(),
                    buf = buf,
                    off = off,
                    len = len,
                )
                return len
            }

            override fun close() {
                // No need to close anything as we're just a wrapper
            }

            override fun toString(): String {
                return "VirtualMemoryDataAccessor(file=${this@ElfFile})"
            }
        }
    }

}

@OptIn(ExperimentalContracts::class)
private inline fun <R> tryByteArrayParsing(duringParsing: String, block: () -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return try {
        block()
    } catch (e: Exception) {
        handleByteArrayParsingExceptions(e, duringParsing)
    }
}

private fun handleByteArrayParsingExceptions(e: Exception, duringParsing: String): Nothing {
    when (e) {
        is IndexOutOfBoundsException,
        is IllegalArgumentException,
        is IllegalStateException,
            -> throw ElfFileException("Failed to parse, invalid format", "during" to duringParsing, cause = e)

        else -> throw e
    }
}

private fun ByteArray.tryCStrUtf8(offset: Int, during: String): String {
    try {
        return cstrUtf8(offset)
    } catch (e: Exception) {
        when (e) {
            is IndexOutOfBoundsException,
            is IllegalArgumentException,
            is IllegalStateException,
            is CStringReadingException,
                -> throw ElfFileException("Failed to read string", "during" to during, cause = e)

            else -> throw e
        }
    }
}

@OptIn(ExperimentalContracts::class)
private inline fun <R> tryReading(readingWhat: String, off: Long, size: Int, block: () -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return try {
        block()
    } catch (e: EOFException) {
        handleEOF(readingWhat, off, size, e)
    }
}

private fun handleEOF(readingWhat: String, off: Long, size: Int, e: EOFException): Nothing {
    if (off == -1L) {
        throw ElfFileException("Failed to read", "what" to readingWhat, cause = e)
    }
    throw ElfFileException("Failed to read", "what" to readingWhat, "offset" to off, "size" to size, cause = e)
}
