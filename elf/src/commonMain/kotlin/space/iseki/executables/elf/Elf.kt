package space.iseki.executables.elf

import space.iseki.executables.common.DataAccessor

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
) : AutoCloseable {

    companion object {
        /**
         * Opens and parses an ELF file from the given data accessor.
         *
         * @param accessor The data accessor that provides access to the file content
         * @return A new ELF file instance
         * @throws ElfFileException if the file format is invalid or unsupported
         */
        internal fun open(accessor: DataAccessor): ElfFile {
            val buf = ByteArray(16)
            accessor.readFully(0, buf)
            val ident = ElfIdentification.parse(buf, 0)
            val buf2 = ByteArray(ident.eiClass.ehdrSize)
            accessor.readFully(0, buf2)
            val ehdr = if (ident.eiClass == ElfClass.ELFCLASS32) {
                Elf32Ehdr.parse(buf2, 0, ident)
            } else if (ident.eiClass == ElfClass.ELFCLASS64) {
                Elf64Ehdr.parse(buf2, 0, ident)
            } else {
                throw ElfFileException("Invalid ElfClass: " + ident.eiClass)
            }
            return ElfFile(accessor, ident, ehdr)
        }
    }

    override fun close() {
        dataAccessor.close()
    }

}

internal val ElfClass.ehdrSize: Int
    get() = when (this) {
        ElfClass.ELFCLASS32 -> 52
        ElfClass.ELFCLASS64 -> 64
        else -> throw ElfFileException("Invalid ElfClass: $this")
    }

/**
 * Open an ELF file from the given bytes.
 *
 * @param bytes the bytes of the ELF file
 * @return the ELF file
 * @throws ElfFileException if the ELF file is invalid
 */
expect fun ElfFile(bytes: ByteArray): ElfFile
