package space.iseki.executables.elf

import space.iseki.executables.common.CommonFileException

/**
 * Represents an exception occurring during ELF file processing.
 *
 * @param message the detailed message describing the error
 * @param cause the underlying reason for the exception
 */
open class ElfFileException(message: String, cause: Throwable? = null) : CommonFileException(message, cause)

class ElfFileParsingException internal constructor(val process: String, val offset: Long, cause: Throwable? = null) :
    ElfFileException("", cause) {
    override val message: String
        get() = "Failed to parse $process at offset $offset: ${cause?.message}"
}

