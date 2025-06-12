package space.iseki.executables.elf

import space.iseki.executables.common.DataAccessor
import space.iseki.executables.common.EOFException
import space.iseki.executables.share.cstrUtf8

internal class Shstrtab(val accessor: DataAccessor, val header: ElfShdr, shstrndx: Int) {
    val data: ByteArray

    init {
        if (header.shType != ElfSType.SHT_STRTAB) {
            throw ElfFileException(
                "Section header shstrtab is not a shstrtab",
                "index" to shstrndx,
                "type" to header.shType,
            )
        }

        if (header.shSize > Int.MAX_VALUE.toULong()) {
            throw ElfFileException("shstrtab size exceeds maximum allowed", "size" to header.shSize)
        }
        val stringTableSize = header.shSize.toInt()
        if (stringTableSize <= 0) {
            throw ElfFileException("Invalid shstrtab size", "size" to stringTableSize)
        }

        // Check if shstrtab size is reasonable (arbitrary limit to prevent DoS)
        if (stringTableSize > 10 * 1024 * 1024) { // 10 MB limit
            throw ElfFileException("shstrtab size too large", "size" to stringTableSize)
        }

        val stringTableOffset = header.shOffset.toLong()

        // Validate shstrtab offset
        if (stringTableOffset < 0 || stringTableOffset + stringTableSize > accessor.size) {
            throw ElfFileException(
                "shstrtab extends beyond file end",
                "offset" to stringTableOffset,
                "size" to stringTableSize,
                "file_size" to accessor.size,
            )
        }

        data = ByteArray(stringTableSize)
        try {
            accessor.readFully(stringTableOffset, data)
        } catch (e: EOFException) {
            throw ElfFileException(
                "Failed to read shstrtab data",
                "offset" to stringTableOffset,
                "size" to stringTableSize,
                "file_size" to accessor.size,
                cause = e,
            )
        }

    }

    /**
     * Retrieves the string at the specified index from the shstrtab.
     *
     * @param index The index of the string to retrieve.
     * @return The string at the specified index.
     * @throws ElfFileException If the index is out of bounds or if the string is malformed.
     */
    fun getStringAt(index: Int): String {
        if (index !in 0 until header.shSize.toInt()) {
            throw ElfFileException("String index out of bounds", "index" to index, "size" to header.shSize)
        }
        try {
            return data.cstrUtf8(index)
        } catch (e: Exception) {
            throw ElfFileException(
                "Failed to read string at index $index",
                "index" to index,
                "size" to header.shSize,
                cause = e,
            )
        }
    }
}

