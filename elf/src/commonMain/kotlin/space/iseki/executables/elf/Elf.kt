package space.iseki.executables.elf

import space.iseki.executables.common.DataAccessor
import space.iseki.executables.common.FileFormat
import space.iseki.executables.common.IOException
import space.iseki.executables.common.OpenedFile
import space.iseki.executables.common.ReadableSection

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
) : AutoCloseable, OpenedFile {

    companion object : FileFormat<ElfFile> {
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
            sectionHeaders: List<ElfShdr>
        ): List<ElfShdr> {
            if (sectionHeaders.isEmpty()) return sectionHeaders

            val shstrndx = ehdr.eShstrndx.castToInt()
            if (shstrndx < 0 || shstrndx >= sectionHeaders.size) {
                throw ElfFileException("Invalid section header string table index: $shstrndx, section count: ${sectionHeaders.size}")
            }

            val stringTableHeader = sectionHeaders[shstrndx]
            val stringTableSize = stringTableHeader.shSize.castToInt()
            if (stringTableSize <= 0) {
                throw ElfFileException("Invalid string table size: $stringTableSize")
            }

            val stringTableOffset = stringTableHeader.shOffset.castToLong()
            val stringTableData = ByteArray(stringTableSize)
            try {
                accessor.readFully(stringTableOffset, stringTableData)
            } catch (e: IOException) {
                throw ElfFileException("Failed to read string table at offset $stringTableOffset with size $stringTableSize", e)
            }

            // Add name to each section header
            return sectionHeaders.map { shdr ->
                val nameIndex = shdr.shName.castToInt()
                if (nameIndex < 0 || nameIndex >= stringTableSize) {
                    return@map shdr
                }

                // Read null-terminated string
                val nameBytes = mutableListOf<Byte>()
                var i = nameIndex
                while (i < stringTableSize && stringTableData[i] != 0.toByte()) {
                    nameBytes.add(stringTableData[i])
                    i++
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
                    else -> shdr // Should not happen
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

            // Read Program Header table
            val programHeaders = when (ehdr) {
                is Elf32Ehdr -> {
                    if (ehdr.ePhoff.value != 0u && ehdr.ePhnum.value.toInt() > 0) {
                        val phSize = ehdr.ePhentsize.value.toInt() * ehdr.ePhnum.value.toInt()
                        val phBuffer = ByteArray(phSize)
                        try {
                            accessor.readFully(ehdr.ePhoff.value.toLong(), phBuffer)
                        } catch (e: IOException) {
                            throw ElfFileException("Failed to read program headers at offset ${ehdr.ePhoff.value}", e)
                        }
                        try {
                            List(ehdr.ePhnum.value.toInt()) { i ->
                                Elf32Phdr.parse(phBuffer, i * ehdr.ePhentsize.value.toInt(), ident)
                            }
                        } catch (e: Exception) {
                            throw ElfFileException("Failed to parse program headers, invalid format", e)
                        }
                    } else {
                        emptyList()
                    }
                }

                is Elf64Ehdr -> {
                    if (ehdr.ePhoff.value != 0UL && ehdr.ePhnum.value.toInt() > 0) {
                        val phSize = ehdr.ePhentsize.value.toInt() * ehdr.ePhnum.value.toInt()
                        val phBuffer = ByteArray(phSize)
                        try {
                            accessor.readFully(ehdr.ePhoff.value.toLong(), phBuffer)
                        } catch (e: IOException) {
                            throw ElfFileException("Failed to read program headers at offset ${ehdr.ePhoff.value}", e)
                        }
                        try {
                            List(ehdr.ePhnum.value.toInt()) { i ->
                                Elf64Phdr.parse(phBuffer, i * ehdr.ePhentsize.value.toInt(), ident)
                            }
                        } catch (e: Exception) {
                            throw ElfFileException("Failed to parse program headers, invalid format", e)
                        }
                    } else {
                        emptyList()
                    }
                }
            }

            // Read Section Header table
            val le = ident.eiData == ElfData.ELFDATA2LSB
            val sectionHeaders = when (ehdr) {
                is Elf32Ehdr -> {
                    if (ehdr.eShoff.value != 0u && ehdr.eShnum.value.toInt() > 0) {
                        val shSize = ehdr.eShentsize.value.toInt() * ehdr.eShnum.value.toInt()
                        val shBuffer = ByteArray(shSize)
                        try {
                            accessor.readFully(ehdr.eShoff.value.toLong(), shBuffer)
                        } catch (e: IOException) {
                            throw ElfFileException("Failed to read section headers at offset ${ehdr.eShoff.value}", e)
                        }
                        try {
                            List(ehdr.eShnum.value.toInt()) { i ->
                                Elf32Shdr.parse(shBuffer, i * ehdr.eShentsize.value.toInt(), le)
                            }
                        } catch (e: Exception) {
                            throw ElfFileException("Failed to parse section headers, invalid format", e)
                        }
                    } else {
                        emptyList()
                    }
                }

                is Elf64Ehdr -> {
                    if (ehdr.eShoff.value != 0UL && ehdr.eShnum.value.toInt() > 0) {
                        val shSize = ehdr.eShentsize.value.toInt() * ehdr.eShnum.value.toInt()
                        val shBuffer = ByteArray(shSize)
                        try {
                            accessor.readFully(ehdr.eShoff.value.toLong(), shBuffer)
                        } catch (e: IOException) {
                            throw ElfFileException("Failed to read section headers at offset ${ehdr.eShoff.value}", e)
                        }
                        try {
                            List(ehdr.eShnum.value.toInt()) { i ->
                                Elf64Shdr.parse(shBuffer, i * ehdr.eShentsize.value.toInt(), le)
                            }
                        } catch (e: Exception) {
                            throw ElfFileException("Failed to parse section headers, invalid format", e)
                        }
                    } else {
                        emptyList()
                    }
                }
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

    /**
     * Represents an ELF section.
     */
    inner class Section internal constructor(
        val sectionHeader: ElfShdr,
    ) : ReadableSection {

        override val name: String?
            get() = sectionHeader.name

        override val size: Long
            get() = sectionHeader.shSize.castToLong()

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
            val availableBytes = maxOf(0L, this.size - sectionOffset)
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
            val filePosition = sectionHeader.shOffset.castToLong() + sectionOffset

            // Read data from file
            try {
                dataAccessor.readFully(filePosition, buf, bufOffset, bytesToRead)
            } catch (e: IOException) {
                throw IOException("Failed to read section '${name ?: "unnamed"}' at offset $filePosition", e)
            }
        }

    }

}

