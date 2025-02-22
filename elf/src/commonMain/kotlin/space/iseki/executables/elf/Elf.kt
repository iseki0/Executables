package space.iseki.executables.elf

import space.iseki.executables.common.DataAccessor


class ElfFile private constructor(
    private val dataAccessor: DataAccessor,
    val ident: ElfIdentification,
    val ehdr: ElfEhdr,
) : AutoCloseable {

    companion object {
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
