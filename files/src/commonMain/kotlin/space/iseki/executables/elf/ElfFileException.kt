package space.iseki.executables.elf

import space.iseki.executables.common.CommonFileException

/**
 * Represents an exception occurring during ELF file processing.
 *
 * @param message the detailed message describing the error
 * @param cause the underlying reason for the exception
 */
open class ElfFileException(message: String, cause: Throwable? = null) : CommonFileException(message, cause)
