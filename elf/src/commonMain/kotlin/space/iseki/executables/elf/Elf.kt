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
    val programHeaders: List<ElfPhdr>,
    val sectionHeaders: List<ElfShdr>,
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

            // 读取 Program Header 表
            val programHeaders = when (ehdr) {
                is Elf32Ehdr -> {
                    if (ehdr.ePhoff.value != 0u && ehdr.ePhnum.value.toInt() > 0) {
                        val phSize = ehdr.ePhentsize.value.toInt() * ehdr.ePhnum.value.toInt()
                        val phBuffer = ByteArray(phSize)
                        accessor.readFully(ehdr.ePhoff.value.toLong(), phBuffer)
                        List(ehdr.ePhnum.value.toInt()) { i ->
                            Elf32Phdr.parse(phBuffer, i * ehdr.ePhentsize.value.toInt(), ident)
                        }
                    } else {
                        emptyList()
                    }
                }

                is Elf64Ehdr -> {
                    if (ehdr.ePhoff.value != 0UL && ehdr.ePhnum.value.toInt() > 0) {
                        val phSize = ehdr.ePhentsize.value.toInt() * ehdr.ePhnum.value.toInt()
                        val phBuffer = ByteArray(phSize)
                        accessor.readFully(ehdr.ePhoff.value.toLong(), phBuffer)
                        List(ehdr.ePhnum.value.toInt()) { i ->
                            Elf64Phdr.parse(phBuffer, i * ehdr.ePhentsize.value.toInt(), ident)
                        }
                    } else {
                        emptyList()
                    }
                }
            }

            // 读取 Section Header 表
            val le = ident.eiData == ElfData.ELFDATA2LSB
            val sectionHeaders = when (ehdr) {
                is Elf32Ehdr -> {
                    if (ehdr.eShoff.value != 0u && ehdr.eShnum.value.toInt() > 0) {
                        val shSize = ehdr.eShentsize.value.toInt() * ehdr.eShnum.value.toInt()
                        val shBuffer = ByteArray(shSize)
                        accessor.readFully(ehdr.eShoff.value.toLong(), shBuffer)
                        List(ehdr.eShnum.value.toInt()) { i ->
                            Elf32Shdr.parse(shBuffer, i * ehdr.eShentsize.value.toInt(), le)
                        }
                    } else {
                        emptyList()
                    }
                }

                is Elf64Ehdr -> {
                    if (ehdr.eShoff.value != 0UL && ehdr.eShnum.value.toInt() > 0) {
                        val shSize = ehdr.eShentsize.value.toInt() * ehdr.eShnum.value.toInt()
                        val shBuffer = ByteArray(shSize)
                        accessor.readFully(ehdr.eShoff.value.toLong(), shBuffer)
                        List(ehdr.eShnum.value.toInt()) { i ->
                            Elf64Shdr.parse(shBuffer, i * ehdr.eShentsize.value.toInt(), le)
                        }
                    } else {
                        emptyList()
                    }
                }
            }

            // 如果需要后续使用 programHeaders 和 sectionHeaders，可以考虑将它们保存到 ElfFile 对象中
            return ElfFile(accessor, ident, ehdr, programHeaders, sectionHeaders)
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
