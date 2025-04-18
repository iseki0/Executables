package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.DataAccessor
import space.iseki.executables.common.DataAccessor.Companion.checkReadBounds
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
                else -> throw ElfFileException("Invalid ElfClass: $this")
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

            val shstrndx = ehdr.eShstrndx.castToInt()
            if (shstrndx < 0 || shstrndx >= sectionHeaders.size) {
                throw ElfFileException("Invalid section header string table index: $shstrndx, section count: ${sectionHeaders.size}")
            }

            val stringTableHeader = sectionHeaders[shstrndx]

            // Validate string table section type
            if (stringTableHeader.shType != ElfSType.SHT_STRTAB) {
                throw ElfFileException("Section header string table (index $shstrndx) is not a string table (type: ${stringTableHeader.shType})")
            }

            val stringTableSize = stringTableHeader.shSize.castToInt()
            if (stringTableSize <= 0) {
                throw ElfFileException("Invalid string table size: $stringTableSize")
            }

            // Check if string table size is reasonable (arbitrary limit to prevent DoS)
            if (stringTableSize > 10 * 1024 * 1024) { // 10 MB limit
                throw ElfFileException("String table size too large: $stringTableSize")
            }

            val stringTableOffset = stringTableHeader.shOffset.castToLong()

            // Validate string table offset
            if (stringTableOffset < 0 || stringTableOffset + stringTableSize > accessor.size) {
                throw ElfFileException("String table extends beyond file end: offset=$stringTableOffset, size=$stringTableSize, file size=${accessor.size}")
            }

            val stringTableData = ByteArray(stringTableSize)
            try {
                accessor.readFully(stringTableOffset, stringTableData)
            } catch (e: IOException) {
                throw ElfFileException(
                    "Failed to read string table at offset $stringTableOffset with size $stringTableSize", e
                )
            }

            // Verify first byte is null as per ELF spec
            if (stringTableSize > 0 && stringTableData[0] != 0.toByte()) {
                throw ElfFileException("Invalid string table: first byte is not null")
            }

            // Add name to each section header
            return sectionHeaders.map { shdr ->
                val nameIndex = shdr.shName.castToInt()
                if (nameIndex < 0 || nameIndex >= stringTableSize) {
                    throw ElfFileException("Invalid section name index: $nameIndex, string table size: $stringTableSize")
                }

                // Read null-terminated string
                val nameBytes = mutableListOf<Byte>()
                var i = nameIndex
                while (i < stringTableSize && stringTableData[i] != 0.toByte()) {
                    nameBytes.add(stringTableData[i])
                    i++
                }

                // Check if we found a null terminator
                if (i == stringTableSize) {
                    throw ElfFileException("Section name at index $nameIndex is not null-terminated")
                }

                val name = try {
                    nameBytes.toByteArray().decodeToString()
                } catch (e: Exception) {
                    throw ElfFileException("Failed to decode section name at index $nameIndex", e)
                }

                // Create a new section header with name using the auto-generated copy function
                when (shdr) {
                    is Elf32Shdr -> shdr.copy(name = name)
                    is Elf64Shdr -> shdr.copy(name = name)
                }
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
                throw ElfFileException("Failed to read ELF identification bytes", e)
            }

            val ident = ElfIdentification.parse(buf, 0)
            val buf2 = ByteArray(ident.eiClass.ehdrSize)
            try {
                accessor.readFully(0, buf2)
            } catch (e: IOException) {
                throw ElfFileException("Failed to read ELF header", e)
            }

            val ehdr = if (ident.eiClass == ElfClass.ELFCLASS32) {
                Elf32Ehdr.parse(buf2, 0, ident)
            } else if (ident.eiClass == ElfClass.ELFCLASS64) {
                Elf64Ehdr.parse(buf2, 0, ident)
            } else {
                throw ElfFileException("Invalid ElfClass: " + ident.eiClass)
            }

            // Validate ELF header fields
            ehdr.validate(accessor.size)

            // Read Program Header table
            val phOffset: Long = ehdr.ePhoff.castToLong()
            val phEntSize: Int = ehdr.ePhentsize.castToInt()
            val phNum: Int = ehdr.ePhnum.castToInt()
            when (ehdr) {
                is Elf32Ehdr -> if (phEntSize < 32) // Minimum size for 32-bit program header
                    throw ElfFileException("Invalid program header entry size: $phEntSize, must be at least 32 bytes")

                is Elf64Ehdr -> if (phEntSize < 56) // Minimum size for 64-bit program header
                    throw ElfFileException("Invalid program header entry size: $phEntSize, must be at least 56 bytes")
            }
            val phSize = phEntSize * phNum
            if (phOffset + phSize > accessor.size) {
                throw ElfFileException("Program header table extends beyond file end: offset=$phOffset, size=$phSize, file size=${accessor.size}")
            }
            val phBuffer = ByteArray(phSize)
            try {
                if (phSize > 0) accessor.readFully(phOffset, phBuffer)
            } catch (e: IOException) {
                throw ElfFileException("Failed to read program headers at offset $phOffset", e)
            }
            val programHeaders = try {
                List(phNum) { i ->
                    when (ehdr) {
                        is Elf32Ehdr -> Elf32Phdr.parse(phBuffer, i * phEntSize, ident)
                        is Elf64Ehdr -> Elf64Phdr.parse(phBuffer, i * phEntSize, ident)
                    }
                }
            } catch (e: Exception) {
                throw ElfFileException("Failed to parse program headers, invalid format", e)
            }

            val le = ident.eiData == ElfData.ELFDATA2LSB
            // Read Section Header table
            val shOffset = ehdr.eShoff.castToLong()
            val shEntSize = ehdr.eShentsize.castToInt()
            val shNum = ehdr.eShnum.castToInt()
            when (ehdr) {
                is Elf32Ehdr -> if (shEntSize < 40) // Minimum size for 32-bit section header
                    throw ElfFileException("Invalid section header entry size: $shEntSize, must be at least 40 bytes")

                is Elf64Ehdr -> if (shEntSize < 64) // Minimum size for 64-bit section header
                    throw ElfFileException("Invalid section header entry size: $shEntSize, must be at least 64 bytes")
            }
            val shSize = shEntSize * shNum
            if (shOffset + shSize > accessor.size) {
                throw ElfFileException("Section header table extends beyond file end: offset=$shOffset, size=$shSize, file size=${accessor.size}")
            }
            val shBuffer = ByteArray(shSize)
            try {
                if (shSize > 0) accessor.readFully(shOffset, shBuffer)
            } catch (e: IOException) {
                throw ElfFileException("Failed to read section headers at offset $shOffset", e)
            }
            val sectionHeaders = try {
                List(shNum) { i ->
                    when (ehdr) {
                        is Elf32Ehdr -> Elf32Shdr.parse(shBuffer, i * shEntSize, le)
                        is Elf64Ehdr -> Elf64Shdr.parse(shBuffer, i * shEntSize, le)
                    }
                }
            } catch (e: Exception) {
                throw ElfFileException("Failed to parse section headers, invalid format", e)
            }

            // Read section name string table and add names to section headers
            val sectionHeadersWithNames = try {
                readSectionNames(accessor, ehdr, sectionHeaders)
            } catch (e: Exception) {
                throw ElfFileException("Failed to read section names", e)
            }

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
            } else sectionHeader.shSize.castToLong()

        override val header: ReadableStructure
            get() = sectionHeader


        override fun toString(): String =
            "Section(name=$name, size=$size, type=${sectionHeader.shType}, flags=${sectionHeader.shFlags})"

        override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
            checkReadBounds(pos, buf, off, len)
            return dataAccessor.readAtMost(
                pos = pos + sectionHeader.shOffset.castToLong(),
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
        programHeaders.filter { it.pType == ElfPType.PT_LOAD }.sortedBy { it.pVaddr.castToULong() }.forEach {
            mapMemory(
                vOff = it.pVaddr.castToULong(),
                fOff = it.pOffset.castToULong(),
                fSize = minOf(it.pFilesz.castToULong(), it.pMemsz.castToULong()),
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
                visibility = sym.visibility
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
                binding = sym.binding, type = sym.type
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
            val stringTableIndex = symSection.shLink.castToInt()
            if (stringTableIndex < 0 || stringTableIndex >= sectionHeaders.size) {
                continue // Invalid string table index
            }

            val stringTableSection = sectionHeaders[stringTableIndex]

            // Read the string table
            val stringTableSize = stringTableSection.shSize.castToInt()

            val stringTableData = ByteArray(stringTableSize)
            try {
                dataAccessor.readFully(stringTableSection.shOffset.castToLong(), stringTableData)
            } catch (e: IOException) {
                continue // Skip this section if we can't read the string table
            }

            // Read the symbol table
            val symbolTableSize = symSection.shSize.castToInt()

            val symbolTableData = ByteArray(symbolTableSize)
            try {
                dataAccessor.readFully(symSection.shOffset.castToLong(), symbolTableData)
            } catch (e: IOException) {
                continue // Skip this section if we can't read the symbol table
            }

            // Parse the symbol table entries
            val entrySize = symSection.shEntsize.castToInt()
            val numEntries = if (entrySize > 0) symbolTableSize / entrySize else 0

            for (i in 0 until numEntries) {
                val offset = i * entrySize

                // Ensure offset is within valid range
                if (offset + (if (ident.eiClass == ElfClass.ELFCLASS32) Elf32Sym.SIZE else Elf64Sym.SIZE) > symbolTableData.size) {
                    continue
                }

                val sym = try {
                    if (ident.eiClass == ElfClass.ELFCLASS32) {
                        Elf32Sym.parse(symbolTableData, offset, isLittleEndian)
                    } else {
                        Elf64Sym.parse(symbolTableData, offset, isLittleEndian)
                    }
                } catch (e: Exception) {
                    continue // Skip this symbol if parsing fails
                }

                // Skip the first (NULL) symbol entry
                if (i == 0) {
                    continue
                }

                // Get symbol name from string table
                val nameOffset = sym.stName.toInt()

                // Allow name offset to be 0, but will get an empty string
                val name = if (nameOffset < 0 || nameOffset >= stringTableData.size) {
                    ""
                } else if (nameOffset == 0) {
                    ""
                } else {
                    readNullTerminatedString(stringTableData, nameOffset)
                }

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
                        isUndefined = sym.stShndx.toInt() == 0
                    )
                )
            }
        }

        return result
    }

    /**
     * Read a null-terminated string from a byte array
     *
     * @param data The byte array containing the string
     * @param offset The offset in the byte array where the string starts
     * @return The string
     */
    private fun readNullTerminatedString(data: ByteArray, offset: Int): String {
        val bytes = mutableListOf<Byte>()
        var i = offset
        while (i < data.size && data[i] != 0.toByte()) {
            bytes.add(data[i])
            i++
        }
        return bytes.toByteArray().decodeToString()
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

