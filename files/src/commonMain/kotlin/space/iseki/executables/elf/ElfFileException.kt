package space.iseki.executables.elf

import space.iseki.executables.common.CommonFileException

/**
 * Represents an exception occurring during ELF file processing.
 *
 * @param message the detailed message describing the error
 * @param cause the underlying reason for the exception
 */
open class ElfFileException internal constructor(
    message: String,
    arguments: List<Pair<String, String>> = emptyList(),
    cause: Throwable? = null,
) : CommonFileException(message, arguments, cause) {
    internal constructor(
        message: String,
        cause: Throwable? = null,
    ) : this(message, emptyList(), cause)
}
